/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.cassandra.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Gunnar Morling
 *
 */
public class AssociationRows implements Rows {

	List<String> columnNames;
	List<List<Object>> values;

	public AssociationRows() {
		columnNames = new ArrayList<>();
		values = new ArrayList<>();
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
		if ( columnNames.isEmpty() ) {
			columnNames.addAll( rows.getColumnNames() );
		}

		if ( rows instanceof ObjectRow ) {
			Iterator<List<Object>> it = rows.iterator();

			while ( it.hasNext() ) {
				values.add( it.next() );
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
