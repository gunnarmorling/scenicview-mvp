/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.cassandra.internal;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Gunnar Morling
 *
 */
public class MultipliedRows implements Rows {

	List<String> columnNames;
	List<List<Object>> values;

	public MultipliedRows(List<String> columnNames, List<List<Object>> values) {
		this.columnNames = columnNames;
		this.values = values;
	}

	@Override
	public Iterator<List<Object>> iterator() {
		return values.iterator();
	}

	@Override
	public List<String> getColumnNames() {
		return columnNames;
	}

	@Override
	public Rows amend(String name, Rows rows) {
		if ( rows instanceof ObjectRow ) {
			String prefix = name != null ? name + "_" : "";

			for(String column : rows.getColumnNames() ) {
				columnNames.add( prefix + column );
			}

			List<Object> toAdd = rows.iterator().next();

			for ( List<Object> row : values ) {
				row.addAll( toAdd );
			}

			return this;
		}

		throw new IllegalArgumentException();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append( String.join( " | ", columnNames ) );
		sb.append( "\n" );
		sb.append(
				String.join(
						"\n",
						values.stream()
							.map( v -> Objects.toString( v ) )
							.collect( Collectors.toList() )
				)
		);

		return sb.toString();
	}
}
