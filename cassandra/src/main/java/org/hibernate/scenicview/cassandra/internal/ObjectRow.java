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
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Gunnar Morling
 *
 */
public class ObjectRow implements Rows {

	private final List<String> columnNames;
	private final List<Object> values;

	public ObjectRow(List<String> columnNames, List<Object> values) {
		this.columnNames = columnNames;
		this.values = values;
	}

	@Override
	public Iterator<List<Object>> iterator() {
		return Collections.singletonList( values ).iterator();
	}

	@Override
	public Rows amend(String name, Rows rows) {
		String prefix = name != null ? name + "_" : "";

		if ( rows instanceof ObjectRow ) {
			ObjectRow other = (ObjectRow) rows;

			for ( int i = 0; i < other.columnNames.size(); i++ ) {
				columnNames.add( prefix + other.columnNames.get( i ) );
				values.add( other.values.get( i ) );
			}

			return this;
		}
		else if ( rows instanceof BasicListRow ) {
			BasicListRow other = (BasicListRow) rows;

			columnNames.add( name );
			values.add( other.iterator().next() );

			return this;
		}
		else if ( rows instanceof AssociationRows ) {
			if ( !rows.iterator().hasNext() ) {
				return this;
			}

			AssociationRows other = (AssociationRows) rows;

			List<String> columnNames = new ArrayList<>( this.columnNames );

			for ( String columnName : rows.getColumnNames() ) {
				columnNames.add( prefix + columnName );
			}

			List<List<Object>> values = new ArrayList<>();

			Iterator<List<Object>> it = other.iterator();
			while ( it.hasNext() ) {
				List<Object> newRow = new ArrayList<>( this.values );
				newRow.addAll( it.next() );
				values.add( newRow );
			}

			return new MultipliedRows( columnNames, values );
		}

		throw new IllegalArgumentException();
	}

	@Override
	public List<String> getColumnNames() {
		return columnNames;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append( String.join( " | ", columnNames ) );
		sb.append( "\n" );
		sb.append( String.join( " , ", values.stream().map( v -> Objects.toString( v ) ).collect( Collectors.toList() ) ) );

		return sb.toString();
	}
}
