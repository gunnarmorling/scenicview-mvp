/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.cassandra.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.scenicview.spi.backend.DenormalizationBackend;
import org.hibernate.scenicview.spi.backend.model.DenormalizationTaskSequence;
import org.hibernate.scenicview.spi.backend.type.TypeProvider;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

/**
 * @author Gunnar Morling
 *
 */
public class CassandraDenormalizationBackend implements DenormalizationBackend {

	private static final Pattern CONNECTION = Pattern.compile( "(.*?):(.*?):(.*?)" );

	final Connection connection;

	public CassandraDenormalizationBackend(String connectionUri) {
		this.connection = createConnection( connectionUri );
	}

	private static Connection createConnection(String connectionUri) {
		Matcher matcher = CONNECTION.matcher( connectionUri );
		if ( !matcher.matches() ) {
			throw new IllegalArgumentException( "Unexpected connection string: " + connectionUri );
		}

		String host = matcher.group( 1 );
		int port = Integer.valueOf( matcher.group( 2 ) );
		String keyspace = matcher.group( 3 );

		Cluster cluster = Cluster.builder()
				.addContactPoint( host )
				.withPort( port )
				.build();

		createKeyspaceIfNotExisting( cluster, keyspace );

		return new Connection( cluster, keyspace );
	}

	private static void createKeyspaceIfNotExisting(Cluster cluster, String keyspace) {
		Session session = cluster.connect();
		session.execute( "CREATE KEYSPACE IF NOT EXISTS " + keyspace + " WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1 }" );
		session.close();
	}

	@Override
	public void stop() {
		connection.getSession().close();
		connection.getCluster().close();
	}

	@Override
	public void process(DenormalizationTaskSequence tasks) {
		tasks.forEach( new CasandraDenormalizationTaskHandler( this.connection ) );
	}

	@Override
	public TypeProvider getTypeProvider() {
		return new CassandraTypeProvider();
	}

	public Connection getConnection() {
		return connection;
	}
}
