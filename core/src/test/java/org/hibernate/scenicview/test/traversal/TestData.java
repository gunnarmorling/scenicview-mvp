/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.test.traversal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Gunnar Morling
 */
public class TestData {

	public static Map<String, Object> getPersonWithAddress() {
		Map<String, Object> country = new HashMap<>();
		country.put( "name", "Germany" );

		Map<String, Object> address = new HashMap<>();
		address.put( "city", "Hamburg" );
		address.put( "street", "Hauptstr. 12" );
		address.put( "country", country );

		Map<String, Object> person = new HashMap<>();
		person.put( "firstName", "Bob" );
		person.put( "lastName", "Wayne" );
		person.put( "address", address );

		return person;
	}

	public static Map<String, Object> getPersonWithAddressAndHobbiesAndContacts() {
		Map<String, Object> contacts = new HashMap<>();
		contacts.put( "phone", "123/456" );
		contacts.put( "email", "bob@example.com" );

		List<Map<String, Object>> hobbies = new ArrayList<>();
		hobbies.add( Collections.singletonMap( "name", "Golf" ) );
		hobbies.add( Collections.singletonMap( "name", "Music" ) );
		hobbies.add( Collections.singletonMap( "name", "Fishing" ) );

		Map<String, Object> person = getPersonWithAddress();
		person.put( "hobbies", hobbies );
		person.put( "contacts", contacts );

		return person;
	}
}
