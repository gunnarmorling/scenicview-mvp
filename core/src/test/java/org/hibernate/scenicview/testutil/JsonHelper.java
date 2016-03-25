/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.testutil;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

/**
 * Helper functionality around JSON documents.
 *
 * @author Gunnar Morling
 */
public class JsonHelper {

	private JsonHelper() {
	}

	public static void assertJsonEquals(String expectedJson, String actualJson) {
		assertJsonEquals( expectedJson, actualJson, JSONCompareMode.NON_EXTENSIBLE );
	}

	public static void assertJsonEqualsIgnoringUnknownFields(String expectedJson, String actualJson) {
		assertJsonEquals( expectedJson, actualJson, JSONCompareMode.LENIENT );
	}

	private static void assertJsonEquals(String expectedJson, String actualJson, JSONCompareMode mode) {
		try {
			JSONCompareResult result = JSONCompare.compareJSON( expectedJson, actualJson, mode );

			if ( result.failed() ) {
				throw new AssertionError( result.getMessage() + "; Actual: " + actualJson );
			}
		}
		catch (JSONException e) {
			throw new RuntimeException( e );
		}
	}
}
