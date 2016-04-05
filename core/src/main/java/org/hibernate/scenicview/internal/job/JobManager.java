/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.internal.job;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hibernate.boot.Metadata;
import org.hibernate.scenicview.config.DenormalizationJobConfigurator;
import org.hibernate.scenicview.config.DenormalizationJobConfigurator.AssociationBuildingContext;
import org.hibernate.scenicview.config.DenormalizationJobConfigurator.Builder;
import org.hibernate.scenicview.config.DenormalizationJobConfigurator.EntityBuildingContext;
import org.hibernate.scenicview.config.DenormalizationJobConfigurator.JobBuildingContext;
import org.hibernate.scenicview.internal.job.AssociationDenormalizingConfiguration.AssociationDenormalizingConfigurationBuilder;
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

	public JobManager(Metadata metadata, DenormalizationJobConfigurator configurator) {
		denormalizationJobsByEntity = Collections.unmodifiableMap( initializeJobs( metadata, configurator ) );
	}

	private static Map<String, List<DenormalizationJob>> initializeJobs(Metadata metadata, DenormalizationJobConfigurator configurator) {
		BuilderImpl builder = new BuilderImpl( metadata );
		configurator.configure( builder );
		return builder.denormalizationJobsByEntity;
	}

	public List<DenormalizationJob> getJobs(String entityName) {
		return denormalizationJobsByEntity.getOrDefault( entityName, Collections.emptyList() );
	}

	private static class BuilderImpl implements Builder {

		private final Metadata metadata;

		private final Map<String, List<DenormalizationJob>> denormalizationJobsByEntity = new HashMap<>();

		public BuilderImpl(Metadata metadata) {
			this.metadata = metadata;
		}

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
				return new EntityBuildingContextImpl<>( new DenormalizationJobBuildingContext<>( name, aggregateRootType ) );
			}
		}

		private class EntityBuildingContextImpl<T> implements EntityBuildingContext<T> {

			protected final DenormalizationJobBuildingContext<T> jobBuildingContext;

			public EntityBuildingContextImpl(DenormalizationJobBuildingContext<T> jobBuildingContext) {
				this.jobBuildingContext = jobBuildingContext;
			}

			@Override
			public <A> AssociationBuildingContext<T, A> withAssociation(Function<T, A> associationProperty) {
				String propertyName = addAssociation( associationProperty );

				@SuppressWarnings("unchecked")
				Class<A> propertyType = metadata.getReferencedPropertyType( jobBuildingContext.aggregateRootType.getName(), propertyName )
						.getReturnedClass();

				return new AssociationBuildingContextImpl<>( propertyType, jobBuildingContext );
			}

			@Override
			public <A> AssociationBuildingContext<T, A> withCollection(Function<T, Collection<A>> associationProperty) {
				String propertyName = addAssociation( associationProperty );

				@SuppressWarnings("unchecked")
				Class<A> propertyType = metadata.getCollectionBinding( jobBuildingContext.aggregateRootType.getName() + "." + propertyName )
						.getElement()
						.getType()
						.getReturnedClass();

				return new AssociationBuildingContextImpl<>( propertyType, jobBuildingContext );
			}

			@Override
			public <A> AssociationBuildingContext<T, A> withMap(Function<T, Map<?, A>> associationProperty) {
				// TODO
				throw new UnsupportedOperationException( "Not implemented yet" );
			}

			@Override
			public EntityBuildingContext<T> usingConnectionId(String connectionId) {
				jobBuildingContext.connectionId = connectionId;
				return this;
			}

			@Override
			public EntityBuildingContext<T> usingCollectionName(String collectionName) {
				jobBuildingContext.collectionName = collectionName;
				return this;
			}

			@Override
			public void build() {
				jobBuildingContext.build();
			}

			private <A> String addAssociation(Function<T, A> associationProperty) {
				String propertyName = PropertyLiteralHelper.getPropertyName( jobBuildingContext.aggregateRootType, associationProperty );

				jobBuildingContext.currentAssociation = new AssociationDenormalizingConfigurationBuilder( propertyName );
				jobBuildingContext.includedAssociations.put( propertyName, jobBuildingContext.currentAssociation );

				return propertyName;
			}
		}

		private class AssociationBuildingContextImpl<E, A> extends EntityBuildingContextImpl<E> implements AssociationBuildingContext<E, A> {

			private final Class<A> associationTargetType;

			private AssociationBuildingContextImpl(Class<A> associationTargetType, DenormalizationJobBuildingContext<E> jobBuildingContext) {
				super( jobBuildingContext );
				this.associationTargetType = associationTargetType;
			}

			@Override
			public EntityBuildingContext<E> includingId(boolean includeId) {
				jobBuildingContext.currentAssociation.includeId( includeId );
				return this;
			}

			@Override
			public <B> AssociationBuildingContext<E, B> includingAssociation(Function<A, B> associationProperty) {
				String propertyName = PropertyLiteralHelper.getPropertyName( associationTargetType, associationProperty );
				jobBuildingContext.currentAssociation.add( propertyName );

				@SuppressWarnings("unchecked")
				Class<B> propertyType = metadata.getReferencedPropertyType( associationTargetType.getName(), propertyName )
						.getReturnedClass();

				return new AssociationBuildingContextImpl<>( propertyType, jobBuildingContext );
			}

			@Override
			public <B> AssociationBuildingContext<E, B> includingCollection(Function<A, Collection<B>> associationProperty) {
				String propertyName = PropertyLiteralHelper.getPropertyName( associationTargetType, associationProperty );
				jobBuildingContext.currentAssociation.add( propertyName );

				@SuppressWarnings("unchecked")
				Class<B> propertyType = metadata.getCollectionBinding( associationTargetType.getName() + "." + propertyName )
						.getElement()
						.getType()
						.getReturnedClass();

				return new AssociationBuildingContextImpl<>( propertyType, jobBuildingContext );
			}

			@Override
			public <B> AssociationBuildingContext<E, B> includingMap(Function<A, Map<?, B>> associationProperty) {
				// TODO
				throw new UnsupportedOperationException( "Not implemented yet" );
			}
		}

		private class DenormalizationJobBuildingContext<T> {

			private final String name;
			private final Class<T> aggregateRootType;
			private final Map<String, AssociationDenormalizingConfigurationBuilder> includedAssociations = new HashMap<>();

			private AssociationDenormalizingConfigurationBuilder currentAssociation;
			private String collectionName;
			private String connectionId;

			public DenormalizationJobBuildingContext(String name, Class<T> aggregateRootType) {
				this.name = name;
				this.aggregateRootType = aggregateRootType;
			}

			public void build() {
				DenormalizationJob job = new DenormalizationJob(
						name,
						aggregateRootType.getName(),
						includedAssociations.values()
							.stream()
							.map( v -> v.build() )
							.collect( Collectors.toSet() ),
						collectionName,
						connectionId
				);

				denormalizationJobsByEntity.computeIfAbsent( job.getAggregateRootTypeName(), n -> new ArrayList<>() )
					.add( job );
			}
		}
	}
}
