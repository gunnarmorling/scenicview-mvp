/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.spi.backend.model;

/**
 * An iterable sequence of named column values.
 *
 * @author Gunnar Morling
 */
public interface ColumnSequence {

	void forEach(ColumnValueConsumer consumer);

	public interface ColumnValueConsumer {

		void consumeValue(String name, Object value);
	}
}
