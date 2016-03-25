/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.test.traversal;

import java.util.ArrayDeque;
import java.util.Deque;

import org.hibernate.scenicview.spi.backend.model.ColumnSequence;
import org.hibernate.scenicview.spi.backend.model.TreeTraversalSequence;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Gunnar Morling
 *
 */
public class JsonCreationTest {

	@Test
	public void canCreateTree() throws Exception {
		JsonElement jsonObject = getAsJsonObject( new MapTreeTraversalSequence( TestData.getPersonWithAddressAndHobbiesAndContacts() ) );
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		System.out.println( gson.toJson( jsonObject ) );
	}

	private JsonElement getAsJsonObject(TreeTraversalSequence sequence) {
		Deque<JsonElement> hierarchy = new ArrayDeque<>();

		sequence.forEach( ( event, name, properties ) -> {
			switch( event ) {
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
					hierarchy.pop();
					break;
			}
		} );

		return hierarchy.pop();
	}

	private void addToParent(Deque<JsonElement> hierarchy, JsonElement child, String name) {
		JsonElement parent = hierarchy.peek();

		if ( parent.isJsonArray() ) {
			parent.getAsJsonArray().add( child );
		}
		else {
			parent.getAsJsonObject().add( name, child );
		}
	}

	private JsonObject toJsonObject(ColumnSequence properties) {
		JsonObject jsonObject = new JsonObject();

		properties.forEach( ( name, value ) -> {
			jsonObject.addProperty( name, (String) value );
		} );

		return jsonObject;
	}
}
