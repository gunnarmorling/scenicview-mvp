/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.test.poc.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

/**
 * @author Gunnar Morling
 */
@Entity
public class Movie {

	@Id
	@GeneratedValue
	private long id;

	private String name;

	private int yearOfRelease;

	@ManyToMany
	private List<Location> filmedAt = new ArrayList<>();

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

	public int getYearOfRelease() {
		return yearOfRelease;
	}

	public void setYearOfRelease(int yearOfRelease) {
		this.yearOfRelease = yearOfRelease;
	}

	public List<Location> getFilmedAt() {
		return filmedAt;
	}

	public void setFilmedAt(List<Location> filmedAt) {
		this.filmedAt = filmedAt;
	}
}
