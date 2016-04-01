/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.cassandra.internal;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public class Connection {
	private final Cluster cluster;
	private final Session session;
	private final String keyspace;

	public Connection(Cluster cluster, String keyspace) {
		this.cluster = cluster;
		this.session = cluster.connect( keyspace );
		this.keyspace = keyspace;
	}

	public Cluster getCluster() {
		return cluster;
	}

	public Session getSession() {
		return session;
	}

	public String getKeyspace() {
		return keyspace;
	}
}
