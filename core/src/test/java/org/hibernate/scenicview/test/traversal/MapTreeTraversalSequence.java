/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.test.traversal;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.scenicview.internal.model.TreeTraversalEventBase;
import org.hibernate.scenicview.spi.backend.model.ColumnSequence;
import org.hibernate.scenicview.spi.backend.model.TreeTraversalSequence;

/**
 * A {@link TreeTraversalSequence} for testing purposes, based on object models represented as a map of maps, with lists
 * of maps for associations.
 *
 * @author Gunnar Morling
 */
public class MapTreeTraversalSequence implements TreeTraversalSequence {

	private final Deque<EventImpl> backlog = new ArrayDeque<>();

	public MapTreeTraversalSequence(Map<String, Object> tree) {
		push( tree, null, true );
	}

	@Override
	public <T> void forEach(T context, TreeTraversalEventConsumer<T> consumer) {
		EventImpl current;

		while( !backlog.isEmpty() ) {
			current = backlog.pollFirst();

			if ( current.tree instanceof Map ) {
				for ( Entry<String, Object> property : ( (Map<String, Object>) current.tree ).entrySet() ) {
					if ( property.getValue() instanceof Map ) {
						push( (Map<String, Object>) property.getValue(), property.getKey(), false );
					}
					else if ( property.getValue() instanceof List ) {
						push( (List<Map<String, Object>>) property.getValue(), property.getKey() );
					}
				}
			}
			else if ( current.tree instanceof List ) {
				for ( Object property : ( (List<Object>) current.tree ) ) {
					if ( property instanceof Map ) {
						push( (Map<String, Object>) property, null, false );
					}
					else if ( property instanceof List ) {
						push( (List<Map<String, Object>>) property, null );
					}
				}
			}

			consumer.consume( current, context );
		}
	}
	private void push(Map<String, Object> tree, String name, boolean aggregateRoot) {
		backlog.addFirst( new EventImpl( aggregateRoot ? EventType.AGGREGATE_ROOT_END : EventType.OBJECT_END, name, null, null, null ) );
		backlog.addFirst( new EventImpl( aggregateRoot ? EventType.AGGREGATE_ROOT_START : EventType.OBJECT_START, name, null, null, tree ) );
	}

	private void push(List<Map<String, Object>> list, String name) {
		backlog.addFirst( new EventImpl( EventType.COLLECTION_END, name, AssociationKind.LIST, AssociationElementKind.ENTITY, null ) );
		backlog.addFirst( new EventImpl( EventType.COLLECTION_START, name, AssociationKind.LIST, AssociationElementKind.ENTITY, list ) );
	}

	private static class EventImpl extends TreeTraversalEventBase {

		private final Object tree;

		public EventImpl(EventType type, String name, AssociationKind associationKind, AssociationElementKind associationElementKind, Object tree) {
			super( type, name, associationKind, associationElementKind );
			this.tree = tree;
		}

		@Override
		public ColumnSequence getColumnSequence() {
			return tree instanceof Map ? new MapBasedColumnSequence( (Map<String, Object>) tree ) : null;
		}
	}

	private static class MapBasedColumnSequence implements ColumnSequence {

		private final Map<String, Object> columns;

		public MapBasedColumnSequence(Map<String, Object> columns) {
			this.columns = columns;
		}

		@Override
		public void forEach(ColumnValueConsumer consumer) {
			columns.entrySet()
				.stream()
				.filter( e -> {
					return !( e.getValue() instanceof Map || e.getValue() instanceof List );
				} )
				.forEach( e -> {
					consumer.consumeValue( e.getKey(), e.getValue() );
				} );
		}
	}
}
