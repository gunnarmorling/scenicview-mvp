/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.cassandra.internal;

import java.util.List;

/**
 * One or more row to be written to the datastore. Each row represents one set of bind parameters for the given
 * collection's prepared statement.
 *
 * @author Gunnar Morling
 */
public interface Rows extends Iterable<List<Object>> {

	List<String> getColumnNames();

	/**
	 * Merges the given other row(s) into this one. This can reflect in
	 * <ul>
	 * <li>adding columns but keeping the number of rows unaltered (e.g. when adding a row representing a single
	 * embeddable component to the row representing the embedding entity) or in</li>
	 * <li>adding rows and columns (when adding the rows of an association to the row row representing the embedding
	 * entity); think of it as creating a Cartesian product</li>
	 * </ul>
	 *
	 * @param name The (qualified) name of the property through which the rows to be added were reached
	 * @param rows The row(s) to add
	 * @return A merged view
	 */
	Rows amend(String name, Rows rows);
}
