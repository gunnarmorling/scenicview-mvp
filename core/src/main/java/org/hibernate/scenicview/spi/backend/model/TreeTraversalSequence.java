/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.spi.backend.model;

/**
 * A sequence of events representing the traversal of an object tree.
 *
 * @author Gunnar Morling
 */
public interface TreeTraversalSequence {

	/**
	 * Invokes the given consumer for each event of this traversal sequence, in sequential order.
	 */
	<T> void forEach(T traversalContext, TreeTraversalEventConsumer<T> consumer);

	public interface TreeTraversalEventConsumer<T> {

		/**
		 * Invoked during traversal by the tree walking routine.
		 *
		 * @param eventType The type of a single event
		 * @param name The property name of the traversed property or {@code null} when stepping into an object which is
		 * part of a collection or the event is of type {@link EventType#AGGREGATE_ROOT_START} or
		 * {@link EventType#AGGREGATE_ROOT_END}.
		 * @param columns The columns of the traversed object if the current even is
		 * {@link EventType#AGGREGATE_ROOT_START} or {@link EventType#COLLECTION_START}; {@code null} otherwise.
		 */
		// TODO: Expose property name and column names?
		void consume(TreeTraversalEvent event, T context);
	}

	public interface TreeTraversalEvent {
		EventType getType();
		String getName();
		ColumnSequence getColumnSequence();
		AssociationKind getAssociationKind();
		AssociationElementKind getAssociationElementKind();
	}

	public enum AssociationKind {
		BAG,
		LIST,
		SET,
		MAP;
	}

	public enum AssociationElementKind {
		BASIC,
		EMBEDDABLE,
		ENTITY
	}

	public enum EventType {
		/**
		 * Signals the beginning of the traversal. Only the very first event is of this type.
		 */
		AGGREGATE_ROOT_START,

		/**
		 * Signals the end of the traversal. Only the very last event is of this type.
		 */
		AGGREGATE_ROOT_END,

		/**
		 * Signals stepping into an associated object.
		 */
		OBJECT_START,

		/**
		 * Signals returning from an associated object.
		 */
		OBJECT_END,

		/**
		 * Signals stepping into a collection of associated objects or a basic element collection.
		 */
		COLLECTION_START,

		/**
		 * Signals returning from a collection.
		 */
		COLLECTION_END;
	}
}
