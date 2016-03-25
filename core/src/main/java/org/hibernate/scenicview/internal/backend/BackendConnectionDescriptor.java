/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.internal.backend;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class BackendConnectionDescriptor {

	private static final Pattern CONNECTION_URL = Pattern.compile( "scenicview:(.*?):(.*)" );

	private final String backend;
	private final String connection;

	public BackendConnectionDescriptor(String connectionUrl) {
		Matcher matcher = CONNECTION_URL.matcher( connectionUrl );
		if ( !matcher.matches() ) {
			throw new IllegalArgumentException( "No valid connection URL: " + connectionUrl );
		}

		this.backend = matcher.group( 1 );
		this.connection = matcher.group( 2 );
	}

	public String getBackend() {
		return backend;
	}

	public String getConnection() {
		return connection;
	}

	@Override
	public String toString() {
		return "BackendConnectionDescriptor [backend=" + backend + ", connection=" + connection + "]";
	}
}
