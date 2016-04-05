/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.internal.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author Gunnar Morling
 *
 */
public class PropertyPath {

	private final LinkedList<String> propertyPath;

	public PropertyPath() {
		propertyPath = new LinkedList<>();
	}

	public PropertyPath(List<String> propertyPath) {
		this.propertyPath = new LinkedList<>( propertyPath );
	}

	public PropertyPath(PropertyPath parent, String property) {
		LinkedList<String> propertyPath = new LinkedList<>( parent.propertyPath );
		propertyPath.add( property );

		this.propertyPath = propertyPath;
	}


	public String getLast() {
		return propertyPath.isEmpty() ? null : propertyPath.getLast();
	}

	public boolean beginsWith(PropertyPath path) {
		for ( int i = 0; i < path.propertyPath.size(); i++ ) {
			if ( i > propertyPath.size() - 1 ) {
				return false;
			}

			if ( ! Objects.equals( path.propertyPath.get( i ), ( propertyPath.get( i ) ) ) ) {
				return false;
			}
		}

		return true;
	}

	@Override
	public String toString() {
		return String.join( ".", propertyPath );
	}
}
