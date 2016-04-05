/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.cassandra.test;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Arrays;
import java.util.Iterator;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.scenicview.cassandra.internal.CassandraDenormalizationBackend;
import org.hibernate.scenicview.config.DenormalizationJobConfigurator;
import org.hibernate.scenicview.internal.backend.BackendManager;
import org.hibernate.scenicview.test.poc.model.Actor;
import org.hibernate.scenicview.test.poc.model.Genre;
import org.hibernate.scenicview.test.poc.model.Money;
import org.hibernate.scenicview.test.poc.model.Movie;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class DenormalizationPoCIT {

	public static class MyDenormalizationJobConfig implements DenormalizationJobConfigurator {
		@Override
		public void configure(Builder builder) {
			builder.newDenormalizationJob( "ActorsWithMoviesAndGenreAndRating" )
				.withAggregateRoot( Actor.class )
					.withAssociation( Actor::getFavoriteGenre )
					.withAssociation( Actor::getPlayedIn )
						.includingId( true )
					.withAssociation( Actor::getRatings )
				.usingCollectionName( "ActorWithDependencies" )
				.usingConnectionId( "some-cassandra" )
				.build();
		}
	}

	private EntityManagerFactory entityManagerFactory;

	@Before
	public void init() {
		entityManagerFactory = Persistence.createEntityManagerFactory( "scenicViewTestPu" );

		Session session = getCassandraSession();

		session.execute(
				"CREATE TABLE IF NOT EXISTS ActorWithDependencies (" +
						"id bigint," +
						"name text," +
						"salary_currency text," +
						"salary_amount bigint," +
						"favoriteGenre_name text," +
						"favoriteGenre_suitedForChildren boolean," +
						"ratings list<int>," +
						"playedIn_id bigint," +
						"playedIn_name text," +
						"playedIn_yearOfRelease int," +
						"PRIMARY KEY (id, playedIn_id)" +
				");"
		);
	}

	@After
	public void destroy() {
		Session session = getCassandraSession();

		session.execute( "DROP TABLE ActorWithDependencies;" );

		if ( entityManagerFactory != null ) {
			entityManagerFactory.close();
		}
	}

	@Test
	public void canDenormalize() throws Exception {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		Movie movie1 = new Movie();
		movie1.setName( "It happened in the winter" );
		movie1.setYearOfRelease( 1980 );
		entityManager.persist( movie1 );

		Movie movie2 = new Movie();
		movie2.setName( "If you knew" );
		movie2.setYearOfRelease( 1965 );
		entityManager.persist( movie2 );

		Genre thriller = new Genre();
		thriller.setName( "Thriller" );
		thriller.setSuitedForChildren( false );
		entityManager.persist( thriller );

		Actor frank = new Actor();
		frank.setName( "Franky" );
		frank.setFavoriteGenre( thriller );
		frank.getPlayedIn().add( movie1 );
		frank.getPlayedIn().add( movie2 );
		frank.setSalary( new Money( 98, "¥" ) );
		frank.getRatings().addAll( Arrays.asList( 9, 8, 5, 4 ) );
		entityManager.persist( frank );

		entityManager.getTransaction().commit();
		entityManager.close();

		ResultSet results = getCassandraSession().execute( "SELECT * FROM ActorWithDependencies" );
		Iterator<Row> it = results.iterator();

		Row row = it.next();
		assertThat( row.getLong( "id" ) ).isEqualTo( frank.getId() );
		assertThat( row.getString( "name" ) ).isEqualTo( "Franky" );
		assertThat( row.getString( "salary_currency" ) ).isEqualTo( "¥" );
		assertThat( row.getLong( "salary_amount" ) ).isEqualTo( 98 );
		assertThat( row.getString( "favoriteGenre_name" ) ).isEqualTo( "Thriller" );
		assertThat( row.getBool( "favoriteGenre_suitedForChildren" ) ).isEqualTo( false );
		assertThat( row.getList( "ratings", Integer.class ) ).containsOnly( 9, 8, 5, 4 );
		assertThat( row.getLong( "playedIn_id" ) ).isEqualTo( movie1.getId() );
		assertThat( row.getString( "playedIn_name" ) ).isEqualTo( "It happened in the winter" );
		assertThat( row.getInt( "playedIn_yearOfRelease" ) ).isEqualTo( 1980 );

		row = it.next();

		assertThat( row.getLong( "id" ) ).isEqualTo( frank.getId() );
		assertThat( row.getString( "name" ) ).isEqualTo( "Franky" );
		assertThat( row.getString( "salary_currency" ) ).isEqualTo( "¥" );
		assertThat( row.getLong( "salary_amount" ) ).isEqualTo( 98 );
		assertThat( row.getString( "favoriteGenre_name" ) ).isEqualTo( "Thriller" );
		assertThat( row.getBool( "favoriteGenre_suitedForChildren" ) ).isEqualTo( false );
		assertThat( row.getList( "ratings", Integer.class ) ).containsOnly( 9, 8, 5, 4 );
		assertThat( row.getLong( "playedIn_id" ) ).isEqualTo( movie2.getId() );
		assertThat( row.getString( "playedIn_name" ) ).isEqualTo( "If you knew" );
		assertThat( row.getInt( "playedIn_yearOfRelease" ) ).isEqualTo( 1965 );

		assertThat( it.hasNext() ).isFalse();
	}

	// TODO Always need value for clustering columns

//	@Test
//	public void canDenormalizeWithNulls() throws Exception {
//		EntityManager entityManager = entityManagerFactory.createEntityManager();
//		entityManager.getTransaction().begin();
//
//		Actor harrison = new Actor();
//		harrison.setName( "Harrison" );
//		entityManager.persist( harrison );
//
//		entityManager.getTransaction().commit();
//		entityManager.close();
//
//		ResultSet results = getCassandraSession().execute( "SELECT * FROM ActorWithDependencies" );
//		Iterator<Row> it = results.iterator();
//
//		Row row = it.next();
//		assertThat( row.getLong( "id" ) ).isEqualTo( harrison.getId() );
//		assertThat( row.getString( "name" ) ).isEqualTo( "Harrison" );
//
//		assertThat( it.hasNext() ).isFalse();
//	}

	private Session getCassandraSession() {
		CassandraDenormalizationBackend backend = (CassandraDenormalizationBackend) entityManagerFactory.unwrap( SessionFactoryImplementor.class )
				.getServiceRegistry()
				.getService( BackendManager.class )
				.getBackend( "some-cassandra" );

		Session session = backend.getConnection().getSession();
		return session;
	}
}
