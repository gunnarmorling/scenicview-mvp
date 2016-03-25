/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.test.poc.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author Gunnar Morling
 */
@Entity
public class Genre {

	@Id
	@GeneratedValue
	private long id;
	private String name;
	private boolean suitedForChildren;

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

	public boolean isSuitedForChildren() {
		return suitedForChildren;
	}

	public void setSuitedForChildren(boolean suitedForChildren) {
		this.suitedForChildren = suitedForChildren;
	}
}
