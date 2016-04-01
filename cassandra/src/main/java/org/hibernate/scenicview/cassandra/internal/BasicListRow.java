/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.cassandra.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Gunnar Morling
 *
 */
public class BasicListRow implements Rows {

	private String columnName;
	private final List<Object> values;

	public BasicListRow() {
		this.values = new ArrayList<>();
	}

	@Override
	public Iterator<List<Object>> iterator() {
		return Collections.singleton( values ).iterator();
	}

	@Override
	public List<String> getColumnNames() {
		return Collections.singletonList( columnName );
	}

	@Override
	public Rows amend(String name, Rows rows) {
		if ( rows instanceof ObjectRow ) {
			if ( columnName == null ) {
				columnName = rows.getColumnNames().iterator().next();
			}

			values.add( ( (ObjectRow) rows ).iterator().next().iterator().next() );
			return this;
		}

		throw new IllegalArgumentException();
	}

	@Override
	public String toString() {
		return columnName + "\n" + values;
	};
}
