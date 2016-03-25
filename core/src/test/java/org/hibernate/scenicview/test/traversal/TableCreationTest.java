/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.test.traversal;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

import org.hibernate.scenicview.spi.backend.model.ColumnSequence;
import org.hibernate.scenicview.spi.backend.model.TreeTraversalSequence;
import org.junit.Test;

/**
 * @author Gunnar Morling
 *
 */
public class TableCreationTest {

	@Test
	public void canCreateTable() throws Exception {
		List<List<String>> table = getAsTable( new MapTreeTraversalSequence( TestData.getPersonWithAddress() ) );
		System.out.println( table );
	}

	private List<List<String>> getAsTable(TreeTraversalSequence sequence) {
		List<String> row = new ArrayList<>();

		Deque<String> path = new ArrayDeque<>();

		sequence.forEach( ( event, name, properties ) -> {
			switch( event ) {
				case AGGREGATE_ROOT_START:
					addProperties( properties, row, path );
					break;
				case AGGREGATE_ROOT_END:
					break;
				case OBJECT_START:
					path.push( name );
					addProperties( properties, row, path );
					break;
				case OBJECT_END:
					path.pop();
					break;
				case COLLECTION_START:
					throw new UnsupportedOperationException();
				case COLLECTION_END:
					throw new UnsupportedOperationException();
			}
		} );

		return Collections.singletonList( row );
	}

	private void addProperties(ColumnSequence properties, List<String> row, Deque<String> path) {
		String prefix = getAsString( path );

		properties.forEach( ( name, value ) -> {
			row.add( prefix + name + "=" + (String) value );
		} );
	}

	private String getAsString(Deque<String> path) {
		StringJoiner prefixBuilder = new StringJoiner( ".", "", "." );
		prefixBuilder.setEmptyValue( "" );

		Iterator<String> pathNodes = path.descendingIterator();
		while ( pathNodes.hasNext() ) {
			prefixBuilder.add( pathNodes.next() );
		}
		return prefixBuilder.toString();
	}
}
