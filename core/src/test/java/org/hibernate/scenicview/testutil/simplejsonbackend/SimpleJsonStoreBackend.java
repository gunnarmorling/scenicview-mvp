/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.testutil.simplejsonbackend;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.scenicview.spi.backend.DenormalizationBackend;
import org.hibernate.scenicview.spi.backend.model.ColumnSequence;
import org.hibernate.scenicview.spi.backend.model.DenormalizationTaskHandler;
import org.hibernate.scenicview.spi.backend.model.DenormalizationTaskSequence;
import org.hibernate.scenicview.spi.backend.model.TreeTraversalSequence;
import org.hibernate.scenicview.spi.backend.model.UpsertTask;
import org.hibernate.scenicview.spi.backend.type.TypeProvider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * @author Gunnar Morling
 */
public class SimpleJsonStoreBackend implements DenormalizationBackend {

	private static Map<String, Map<String, String>> store = new HashMap<>();

	@Override
	public void process(DenormalizationTaskSequence tasks) {
		tasks.forEach( new JsonCreatingDenormalizationTaskHandler() );
	}

	private class JsonCreatingDenormalizationTaskHandler implements DenormalizationTaskHandler {

		@Override
		public void handleUpsert(UpsertTask upsert) {
			Map<String, String> collection = store.computeIfAbsent(
					upsert.getCollectionName(),
					(collectionName) -> { return new HashMap<>(); }
			);

			JsonElement jsonObject = getAsJsonObject( upsert.getObjectTree() );
			Gson gson = new GsonBuilder().setPrettyPrinting().create();

			String value = gson.toJson( jsonObject );
			String primaryKey = getPrimaryKeyAsString( upsert.getPrimaryKey() );

			collection.put( primaryKey, value );
		}
	}

	// TODO Expose not via static
	public static Map<String, Map<String, String>> getStore() {
		return store;
	}

	private String getPrimaryKeyAsString(ColumnSequence primaryKey) {
		StringBuilder builder = new StringBuilder();

		primaryKey.forEach( (name, value) -> {
			builder.append( "(" ).append( name ).append( "=" ).append( value ).append( ")" );
		} );

		return builder.toString();
	}

	@Override
	public TypeProvider getTypeProvider() {
		return new JsonTypeProvider();
	}

	private JsonElement getAsJsonObject(TreeTraversalSequence sequence) {
		Deque<JsonElement> hierarchy = new ArrayDeque<>();

		sequence.forEach( ( eventType, name, properties ) -> {
			switch( eventType ) {
				case AGGREGATE_ROOT_START:
					hierarchy.push( toJsonObject( properties ) );
					break;
				case AGGREGATE_ROOT_END:
					break;
				case OBJECT_START:
					JsonObject jsonObject = toJsonObject( properties );
					addToParent( hierarchy, jsonObject, name );
					hierarchy.push( jsonObject );
					break;
				case OBJECT_END:
					hierarchy.pop();
					break;
				case COLLECTION_START:
					JsonArray jsonArray = new JsonArray();
					addToParent( hierarchy, jsonArray, name );
					hierarchy.push( jsonArray );
					break;
				case COLLECTION_END:
					JsonArray builtArray = hierarchy.pop().getAsJsonArray();

					// Don't keep empty arrays in the final JSON
					if ( builtArray.size() == 0 ) {
						hierarchy.getLast().getAsJsonObject().remove( name );
					}
					break;
			}
		} );

		return hierarchy.pop();
	}

	private void addToParent(Deque<JsonElement> hierarchy, JsonElement child, String name) {
		JsonElement parent = hierarchy.peek();

		if ( parent.isJsonArray() ) {
			if ( child.isJsonObject() && child.getAsJsonObject().entrySet().size() == 1 ) {
				// Adding single column directly as array element
				parent.getAsJsonArray().add( child.getAsJsonObject().entrySet().iterator().next().getValue() );
			}
			else {
				parent.getAsJsonArray().add( child );
			}
		}
		else {
			parent.getAsJsonObject().add( name, child );
		}
	}

	private JsonObject toJsonObject(ColumnSequence properties) {
		JsonObject jsonObject = new JsonObject();

		properties.forEach( ( name, value ) -> {
			jsonObject.add( name, (JsonPrimitive) value );
		} );

		return jsonObject;
	}
}
