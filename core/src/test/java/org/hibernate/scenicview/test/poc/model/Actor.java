/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.test.poc.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Type;

/**
 * @author Gunnar Morling
 */
@Entity
public class Actor {

	@Id
	@GeneratedValue
	private long id;

	private String name;

	@ManyToOne
	private Genre favoriteGenre;

	@ManyToMany
	private Set<Movie> playedIn = new HashSet<>();

	@Type( type = "org.hibernate.scenicview.test.poc.type.MoneyUserType" )
	@Columns(columns = {
			@Column(name = "salary_amount"),
			@Column(name = "salary_currency")
	})
	private Money salary;

	@ElementCollection
	private List<Integer> ratings = new ArrayList<>();

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Genre getFavoriteGenre() {
		return favoriteGenre;
	}

	public void setFavoriteGenre(Genre favoriteGenre) {
		this.favoriteGenre = favoriteGenre;
	}

	public Set<Movie> getPlayedIn() {
		return playedIn;
	}

	public void setPlayedIn(Set<Movie> playedIn) {
		this.playedIn = playedIn;
	}

	public Money getSalary() {
		return salary;
	}

	public void setSalary(Money salary) {
		this.salary = salary;
	}

	public List<Integer> getRatings() {
		return ratings;
	}

	public void setRatings(List<Integer> ratings) {
		this.ratings = ratings;
	}
}
