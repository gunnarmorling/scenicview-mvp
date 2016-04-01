/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.cassandra.internal;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.hibernate.scenicview.spi.backend.model.ColumnSequence;
import org.hibernate.scenicview.spi.backend.model.DenormalizationTaskHandler;
import org.hibernate.scenicview.spi.backend.model.TreeTraversalSequence.AssociationElementKind;
import org.hibernate.scenicview.spi.backend.model.TreeTraversalSequence.AssociationKind;
import org.hibernate.scenicview.spi.backend.model.UpsertTask;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;

class CasandraDenormalizationTaskHandler implements DenormalizationTaskHandler {

	private final Connection connection;

	CasandraDenormalizationTaskHandler(Connection connection) {
		this.connection = connection;
	}

	@Override
	public void handleUpsert(UpsertTask upsert) {
		CassandraTreeTraversalContext context = new CassandraTreeTraversalContext();
		context.values.push( getObjectRow( upsert.getPrimaryKey() ) );

		upsert.getObjectTree().forEach(
			context,
			( event, ctx ) -> {
				switch( event.getType() ) {
					case AGGREGATE_ROOT_START:
						ctx.values.push( context.values.pop().amend( null, getObjectRow( event.getColumnSequence() ) ) );
						break;
					case AGGREGATE_ROOT_END:
						break;
					case OBJECT_START:
						if ( event.getName() != null ) {
							ctx.path.push( event.getName() );
						}

						ctx.values.push( getObjectRow( event.getColumnSequence() ) );
						break;
					case OBJECT_END:
						addToParent( ctx, ctx.values.pop() );

						if ( event.getName() != null ) {
							ctx.path.pop();
						}

						break;
					case COLLECTION_START:
						ctx.path.push( event.getName() );
						ctx.values.push( getCollectionRows( event.getAssociationKind(), event.getAssociationElementKind() ) );
						break;
					case COLLECTION_END:
						addToParent( ctx, ctx.values.pop() );
						ctx.path.pop();
						break;
				}
		} );

		Rows rows = context.values.pop();

		// TODO cache prepared statement
		StringBuilder insertBuilder = new StringBuilder();
		insertBuilder.append( "INSERT INTO " );
		insertBuilder.append( upsert.getCollectionName() );
		insertBuilder.append( " ( " );
		insertBuilder.append( String.join( ", ", rows.getColumnNames() ) );
		insertBuilder.append( " ) VALUES ( " );
		insertBuilder.append( IntStream.range( 0, rows.getColumnNames().size() ).mapToObj( i -> "?" ).collect( Collectors.joining( ", " ) ) );
		insertBuilder.append( " )" );

		System.out.println( insertBuilder );

		PreparedStatement statement = connection.getSession().prepare( insertBuilder.toString() );

		for( List<Object> row : rows ) {
			BoundStatement bound = statement.bind( row.toArray() );
			connection.getSession().execute( bound );
		}
	}

	private void addToParent(CassandraTreeTraversalContext context, Rows child) {
		Rows parent = context.values.pop();
		parent = parent.amend( getAsString( context.path ), child );
		context.values.push( parent );
	}

	private ObjectRow getObjectRow(ColumnSequence columns) {
		List<String> columnNames = new ArrayList<>();
		List<Object> values = new ArrayList<>();

		columns.forEach( ( name, value ) -> {
			columnNames.add( name );
			values.add( value );
		} );

		return new ObjectRow( columnNames, values );
	}

	private Rows getCollectionRows(AssociationKind associationKind, AssociationElementKind associationElementKind) {
		// TODO Handle all sorts of collections/associations
		if ( ( associationKind == AssociationKind.LIST || associationKind == AssociationKind.BAG)
				&& associationElementKind == AssociationElementKind.BASIC ) {

			return new BasicListRow();
		}
		else {
			return new AssociationRows();
		}
	}

	private String getAsString(Deque<String> path) {
		StringJoiner prefixBuilder = new StringJoiner( "_", "", "" );
		prefixBuilder.setEmptyValue( "" );

		Iterator<String> pathNodes = path.descendingIterator();
		while ( pathNodes.hasNext() ) {
			prefixBuilder.add( pathNodes.next() );
		}
		return prefixBuilder.toString();
	}

	public static class CassandraTreeTraversalContext {
		Deque<String> path = new ArrayDeque<>();
		Deque<Rows> values = new ArrayDeque<>();
	}
}