/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.config;

import java.util.function.Function;

public interface DenormalizationJobConfigurator {

	void configure(Builder builder);

	public interface Builder {
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
