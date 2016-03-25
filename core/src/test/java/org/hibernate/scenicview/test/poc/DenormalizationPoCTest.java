/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.test.poc;

import static org.fest.assertions.Assertions.assertThat;
import static org.hibernate.scenicview.testutil.JsonHelper.assertJsonEquals;

import java.util.Arrays;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.hibernate.scenicview.config.DenormalizationJobConfigurator;
import org.hibernate.scenicview.test.poc.model.Actor;
import org.hibernate.scenicview.test.poc.model.Genre;
import org.hibernate.scenicview.test.poc.model.Money;
import org.hibernate.scenicview.test.poc.model.Movie;
import org.hibernate.scenicview.testutil.simplejsonbackend.SimpleJsonStoreBackend;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DenormalizationPoCTest {

	public static class MyDenormalizationJobConfig implements DenormalizationJobConfigurator {
		@Override
		public void configure(Builder builder) {
			builder.newDenormalizationJob( "ActorsWithMoviesAndGenreAndRatings" )
				.withAggregateRoot( Actor.class )
					.includingAssociation( Actor::getFavoriteGenre )
					.includingAssociation( Actor::getPlayedIn )
					.includingAssociation( Actor::getRatings )

					// TODO All basic props are included by default; enable to exclude some
					// .excludingProperty( Actor::getSomeBasicProp )

					// TODO The id is used as PK in the denormalized version, too; Allow to
					// use another property (or Lambda etc.) for stores without secondary index
					// support
					// .withId( Actor::getEmail )

				.withCollectionName( "ActorWithDependencies" )
				.withConnectionId( "some-map" )
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
		SimpleJsonStoreBackend.getStore().clear();

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
		entityManager.persist( movie1 );

		Movie movie2 = new Movie();
		movie2.setName( "If you knew" );
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

		Map<String, String> actorsAsJson = SimpleJsonStoreBackend.getStore().get( "ActorWithDependencies" );
		assertThat( actorsAsJson ).hasSize( 1 );

		String frankAsJson = actorsAsJson.get( "(id=" + frank.getId() + ")" );
		assertJsonEquals(
				"{" +
					"'name' : 'Franky'," +
					"'salary_amount' : 98," +
					"'salary_currency' : '¥'," +
					"'playedIn' : [" +
						"'It happened in the winter'," +
						"'If you knew'" +
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
				frankAsJson
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

		Map<String, String> actorsAsJson = SimpleJsonStoreBackend.getStore().get( "ActorWithDependencies" );
		assertThat( actorsAsJson ).hasSize( 1 );

		String harrisonAsJson = actorsAsJson.get( "(id=" + harrison.getId() + ")" );
		assertJsonEquals(
				"{" +
					"'name' : 'Harrison'" +
				"}",
				harrisonAsJson
		);
	}
}
