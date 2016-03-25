/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.internal.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.hibernate.scenicview.config.DenormalizationJobConfigurator;
import org.hibernate.scenicview.config.DenormalizationJobConfigurator.Builder;
import org.hibernate.scenicview.config.DenormalizationJobConfigurator.EntityBuildingContext;
import org.hibernate.scenicview.config.DenormalizationJobConfigurator.JobBuildingContext;
import org.hibernate.scenicview.internal.propertyliteral.PropertyLiteralHelper;
import org.hibernate.scenicview.internal.stereotypes.Immutable;
import org.hibernate.service.Service;

/**
 * Provides access to all denormalization jobs configured.
 *
 * @author Gunnar Morling
 */
public class JobManager implements Service {

	@Immutable
	Map<String, List<DenormalizationJob>> denormalizationJobsByEntity;

	public JobManager(DenormalizationJobConfigurator configurator) {
		denormalizationJobsByEntity = Collections.unmodifiableMap( initializeJobs( configurator ) );
	}

	private Map<String, List<DenormalizationJob>> initializeJobs(DenormalizationJobConfigurator configurator) {
		BuilderImpl builder = new BuilderImpl();
		configurator.configure( builder );
		return builder.denormalizationJobsByEntity;
	}

	public List<DenormalizationJob> getJobs(String entityName) {
		return denormalizationJobsByEntity.getOrDefault( entityName, Collections.emptyList() );
	}

	private class BuilderImpl implements Builder {

		private final Map<String, List<DenormalizationJob>> denormalizationJobsByEntity = new HashMap<>();

		@Override
		public JobBuildingContext newDenormalizationJob(String name) {
			return new DenormalizationJobBuilder( name );
		}

		private class DenormalizationJobBuilder implements JobBuildingContext {

			private final String name;

			public DenormalizationJobBuilder(String name) {
				this.name = name;
			}

			@Override
			public <T> EntityBuildingContext<T> withAggregateRoot(Class<T> aggregateRootType) {
				return new TypedDenormalizationJobBuilder<>( name, aggregateRootType );
			}
		}

		private class TypedDenormalizationJobBuilder<T> implements EntityBuildingContext<T> {

			private final String name;
			private final Class<T> aggregateRootType;
			private final Set<String> includedAssociations = new HashSet<>();
			private String collectionName;
			String connectionId;

			public TypedDenormalizationJobBuilder(String name, Class<T> aggregateRootType) {
				this.name = name;
				this.aggregateRootType = aggregateRootType;
			}

			@Override
			public EntityBuildingContext<T> includingAssociation(Function<T, ?> associationProperty) {
				includedAssociations.add( PropertyLiteralHelper.getPropertyName( aggregateRootType, associationProperty ) );
				return this;
			}

			@Override
			public EntityBuildingContext<T> withConnectionId(String connectionId) {
				this.connectionId = connectionId;
				return this;
			}

			@Override
			public EntityBuildingContext<T> withCollectionName(String collectionName) {
				this.collectionName = collectionName;
				return this;
			}

			@Override
			public void build() {
				DenormalizationJob job = new DenormalizationJob(
						name,
						aggregateRootType.getName(),
						includedAssociations,
						collectionName,
						connectionId
				);

				denormalizationJobsByEntity.computeIfAbsent( job.getAggregateRootTypeName(), n -> new ArrayList<>() )
					.add( job );
			}
		}
	}
}
