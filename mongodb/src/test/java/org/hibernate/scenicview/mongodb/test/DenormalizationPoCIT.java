/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.mongodb.test;

import static org.fest.assertions.Assertions.assertThat;
import static org.hibernate.scenicview.testutil.JsonHelper.assertJsonEqualsIgnoringUnknownFields;

import java.util.Arrays;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.bson.Document;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.scenicview.config.DenormalizationJobConfigurator;
import org.hibernate.scenicview.internal.backend.BackendManager;
import org.hibernate.scenicview.mongodb.internal.MongoDbDenormalizationBackend;
import org.hibernate.scenicview.mongodb.internal.MongoDbDenormalizationBackend.Connection;
import org.hibernate.scenicview.test.poc.model.Actor;
import org.hibernate.scenicview.test.poc.model.Genre;
import org.hibernate.scenicview.test.poc.model.Location;
import org.hibernate.scenicview.test.poc.model.Money;
import org.hibernate.scenicview.test.poc.model.Movie;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.client.MongoCollection;

public class DenormalizationPoCIT {

	public static class MyDenormalizationJobConfig implements DenormalizationJobConfigurator {
		@Override
		public void configure(Builder builder) {
			builder.newDenormalizationJob( "ActorsWithMoviesAndGenreAndRating" )
				.withAggregateRoot( Actor.class )
					.withAssociation( Actor::getFavoriteGenre )
					.withCollection( Actor::getPlayedIn )
						.includingCollection( Movie::getFilmedAt )
					.withCollection( Actor::getRatings )
				.usingCollectionName( "ActorWithDependencies" )
				.usingConnectionId( "some-mongo" )
				.build();
		}
	}

	private EntityManagerFactory entityManagerFactory;

	@Before
	public void init() {
		entityManagerFactory = Persistence.createEntityManagerFactory( "scenicViewTestPu" );
	}

	@After
	public void destroy() {
		getActorsWithDependencies().drop();

		if ( entityManagerFactory != null ) {
			entityManagerFactory.close();
		}
	}

	@Test
	public void canDenormalize() throws Exception {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		Location location1 = new Location( "Orlando", 27.8 );
		entityManager.persist( location1 );

		Location location2 = new Location( "Helsinki", 8.9 );
		entityManager.persist( location2 );

		Movie movie1 = new Movie();
		movie1.setName( "It happened in the winter" );
		movie1.setYearOfRelease( 1980 );
		movie1.getFilmedAt().add( location1 );
		movie1.getFilmedAt().add( location2 );
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

		MongoCollection<Document> actorsDenormalized = getActorsWithDependencies();
		assertThat( actorsDenormalized.count() ).isEqualTo( 1 );

		Document frankDenormalized = actorsDenormalized.find( new Document( "_id", frank.getId() ) ).iterator().next();

		assertJsonEqualsIgnoringUnknownFields(
				"{" +
					"'name' : 'Franky'," +
					// TODO type differs for long
					// "'salary_amount' : 98," +
					"'salary_currency' : '¥'," +
					"'playedIn' : [" +
						"{" +
							" 'name' : 'It happened in the winter'," +
							" 'yearOfRelease' : 1980," +
							" 'filmedAt' : [" +
								"{" +
									" 'name' : 'Orlando'," +
									" 'averageTemperature' : 27.8" +
								"}," +
								"{" +
									" 'name' : 'Helsinki'," +
									" 'averageTemperature' : 8.9" +
								"}" +
							"]" +
						"}," +
						"{" +
							" 'name' : 'If you knew'," +
							" 'yearOfRelease' : 1965" +
						"}" +
					"]," +
					"'ratings' : [" +
						"4," +
						"5," +
						"8," +
						"9" +
					"]," +
					"'favoriteGenre' : {" +
						"'name' : 'Thriller'," +
						"'suitedForChildren' : false" +
					"}" +
				"}",
				frankDenormalized.toJson()
		);
	}

	@Test
	public void canDenormalizeWithNulls() throws Exception {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		Actor harrison = new Actor();
		harrison.setName( "Harrison" );
		entityManager.persist( harrison );

		entityManager.getTransaction().commit();
		entityManager.close();

		MongoCollection<Document> actorsDenormalized = getActorsWithDependencies();
		assertThat( actorsDenormalized.count() ).isEqualTo( 1 );

		Document harrisonDenormalized = actorsDenormalized.find( new Document( "_id", harrison.getId() ) ).iterator().next();

		assertJsonEqualsIgnoringUnknownFields(
				"{" +
					"'name' : 'Harrison'" +
				"}",
				harrisonDenormalized.toJson()
		);
	}

	private MongoCollection<Document> getActorsWithDependencies() {
		MongoDbDenormalizationBackend mongoBackend = (MongoDbDenormalizationBackend) entityManagerFactory.unwrap( SessionFactoryImplementor.class )
			.getServiceRegistry()
			.getService( BackendManager.class )
			.getBackend( "some-mongo" );

		Connection connection = mongoBackend.getConnection();

		return connection.getClient()
				.getDatabase( connection.getDatabaseName() )
				.getCollection( "ActorWithDependencies" );
	}
}
