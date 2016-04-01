/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.mongodb.internal;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;
import org.hibernate.scenicview.spi.backend.DenormalizationBackend;
import org.hibernate.scenicview.spi.backend.model.ColumnSequence;
import org.hibernate.scenicview.spi.backend.model.DenormalizationTaskHandler;
import org.hibernate.scenicview.spi.backend.model.DenormalizationTaskSequence;
import org.hibernate.scenicview.spi.backend.model.TreeTraversalSequence;
import org.hibernate.scenicview.spi.backend.model.UpsertTask;
import org.hibernate.scenicview.spi.backend.type.TypeProvider;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

/**
 * @author Gunnar Morling
 *
 */
public class MongoDbDenormalizationBackend implements DenormalizationBackend {

	private static final Pattern CONNECTION = Pattern.compile( "(.*?):(.*?):(.*?)" );

	private final Connection connection;
	private final MongoDatabase database;

	public MongoDbDenormalizationBackend(String connectionUri) {
		this.connection = createConnection( connectionUri );
		this.database = connection.client.getDatabase( connection.databaseName );
	}

	private static Connection createConnection(String connectionUri) {
		Matcher matcher = CONNECTION.matcher( connectionUri );
		if ( !matcher.matches() ) {
			throw new IllegalArgumentException( "Unexpected connection string: " + connectionUri );
		}

		ServerAddress address = new ServerAddress( matcher.group( 1 ), Integer.valueOf( matcher.group( 2 ) ) );
		return new Connection(
				new MongoClient( address ),
				matcher.group( 3 )
		);
	}

	@Override
	public void stop() {
		connection.client.close();
	}

	@Override
	public void process(DenormalizationTaskSequence tasks) {
		tasks.forEach( new DocumentCreationAndInsertionHandler() );
	}

	@Override
	public TypeProvider getTypeProvider() {
		return new MongoDbTypeProvider();
	}

	public Connection getConnection() {
		return connection;
	}

	private class DocumentCreationAndInsertionHandler implements DenormalizationTaskHandler {

		@Override
		public void handleUpsert(UpsertTask upsert) {
			Document document = getAsDocument( upsert.getObjectTree() );
			Document id = getId( upsert.getPrimaryKey() );

			UpdateOptions options = new UpdateOptions();
			options.upsert( true );

			database.getCollection( upsert.getCollectionName() ).replaceOne( id, document, options );
		}

		private Document getAsDocument(TreeTraversalSequence sequence) {
			Deque<Object> hierarchy = new ArrayDeque<>();

			sequence.forEach(
					null,
					( event, context ) -> {
						switch( event.getType() ) {
							case AGGREGATE_ROOT_START:
								hierarchy.push( toDocument( event.getColumnSequence() ) );
								break;
							case AGGREGATE_ROOT_END:
								break;
							case OBJECT_START:
								Document document = toDocument( event.getColumnSequence() );
								addToParent( hierarchy, document, event.getName() );
								hierarchy.push( document );
								break;
							case OBJECT_END:
								hierarchy.pop();
								break;
							case COLLECTION_START:
								List<Object> list = new ArrayList<>();
								addToParent( hierarchy, list, event.getName() );
								hierarchy.push( list );
								break;
							case COLLECTION_END:
								List<Object> builtArray = (List<Object>) hierarchy.pop();

								// Don't keep empty arrays in the final object
								if ( builtArray.size() == 0 ) {
									( (Document) hierarchy.getLast() ).remove( event.getName() );
								}
								break;
						}
			} );

			return (Document) hierarchy.pop();
		}

		private void addToParent(Deque<Object> hierarchy, Object child, String name) {
			Object parent = hierarchy.peek();

			if ( parent instanceof List ) {
				if ( child instanceof Document && ( (Document) child ).keySet().size() == 1 ) {
					// Adding single column directly as array element
					( (List<Object>) parent ).add( ( (Document) child ).entrySet().iterator().next().getValue() );
				}
				else {
					( (List<Object>) parent ).add( child );
				}
			}
			else {
				( (Document) parent ).put( name, child );
			}
		}

		private Document toDocument(ColumnSequence properties) {
			Document document = new Document();

			properties.forEach( ( name, value ) -> {
				document.put( name, value );
			} );

			return document;
		}

		private Document getId(ColumnSequence primaryKey) {
			Document values = new Document();

			primaryKey.forEach( (name, value) -> {
				values.put( name, value );
			} );

			Document id = new Document();
			id.put( "_id", values.size() == 1 ? values.values().iterator().next() : values );

			return id;
		}
	}

	public static class Connection {
		private final MongoClient client;
		private final String databaseName;

		public Connection(MongoClient client, String databaseName) {
			this.client = client;
			this.databaseName = databaseName;
		}

		public MongoClient getClient() {
			return client;
		}

		public String getDatabaseName() {
			return databaseName;
		}
	}
}
