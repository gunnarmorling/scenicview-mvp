/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.config;

import java.util.Collection;
import java.util.Map;
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

	public interface EntityBuildingContext<E> {

		<A> AssociationBuildingContext<E, A> withAssociation(Function<E, A> associationProperty);

		<A> AssociationBuildingContext<E, A> withCollection(Function<E, Collection<A>> associationProperty);

		<A> AssociationBuildingContext<E, A> withMap(Function<E, Map<?, A>> associationProperty);

		EntityBuildingContext<E> usingConnectionId(String connectionId);

		EntityBuildingContext<E> usingCollectionName(String collectionName);

		void build();
	}

	public interface AssociationBuildingContext<E, A> extends EntityBuildingContext<E> {

		EntityBuildingContext<E> includingId(boolean includeId);

		<B> AssociationBuildingContext<E, B> includingAssociation(Function<A, B> associationProperty);

		<B> AssociationBuildingContext<E, B> includingCollection(Function<A, Collection<B>> associationProperty);

		<B> AssociationBuildingContext<E, B> includingMap(Function<A, Map<?, B>> associationProperty);
	}
}
