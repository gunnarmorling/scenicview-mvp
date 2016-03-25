/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.spi.backend.model;

/**
 * @author Gunnar Morling
 *
 */
public interface TreeTraversalSequence {

	void forEach(TreeTraversalEventConsumer consumer);

	public interface TreeTraversalEventConsumer {
		// TODO: Expose property name and column names?
		void consume(EventType eventType, String name, ColumnSequence columns);
	}

	public enum EventType {
		AGGREGATE_ROOT_START,
		AGGREGATE_ROOT_END,
		OBJECT_START,
		OBJECT_END,
		COLLECTION_START,
		COLLECTION_END;
	}
}
