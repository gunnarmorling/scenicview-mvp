/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.internal.job;


/**
 * @author Gunnar Morling
 *
 */
public class AssociationDenormalizingConfiguration {

	private final String name;
	private final boolean includeId;

	private AssociationDenormalizingConfiguration(String name, boolean includeId) {
		this.name = name;
		this.includeId = includeId;
	}

	public String getName() {
		return name;
	}

	public boolean isIncludeId() {
		return includeId;
	}

	@Override
	public String toString() {
		return "AssociationDenormalizingConfiguration [name=" + name + ", includeId=" + includeId + "]";
	}

	static class AssociationDenormalizingConfigurationBuilder {

		private final String name;
		private boolean includeId;

		public AssociationDenormalizingConfigurationBuilder(String name) {
			this.name = name;
		}

		AssociationDenormalizingConfigurationBuilder includeId(boolean includeId) {
			this.includeId = includeId;
			return this;
		}

		AssociationDenormalizingConfiguration build() {
			return new AssociationDenormalizingConfiguration( name, includeId );
		}
	}
}
