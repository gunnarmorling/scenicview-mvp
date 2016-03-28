/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.config;

import java.util.function.Function;

/**
 * Callback for configuring denormalization tasks. Configured through the {@link ScenicViewProperties#CONFIGURATOR}
 * property.
 *
 * @author Gunnar Morling
 */
public interface DenormalizationJobConfigurator {

	void configure(Builder builder);

	public interface Builder {

		/**
		 * Creates a new named denormalization job, to be configured through subsequent calls of the fluent API,
		 * finalized by a call to {@link EntityBuildingContext#build()}.
		 */
		JobBuildingContext newDenormalizationJob(String name);
	}

	public interface JobBuildingContext {

		<T> EntityBuildingContext<T> withAggregateRoot(Class<T> aggregateRootType);
	}

	public interface EntityBuildingContext<T> {

		EntityBuildingContext<T> includingAssociation(Function<T, ?> associationProperty);

		EntityBuildingContext<T> withConnectionId(String connectionId);

		EntityBuildingContext<T> withCollectionName(String collectionName);

		void build();
	}
}
