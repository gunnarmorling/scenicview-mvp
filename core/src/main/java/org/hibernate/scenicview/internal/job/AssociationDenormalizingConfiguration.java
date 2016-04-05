/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.internal.job;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.scenicview.internal.model.PropertyPath;

/**
 * @author Gunnar Morling
 *
 */
public class AssociationDenormalizingConfiguration {

	private final PropertyPath propertyPath;
	private final boolean includeId;

	private AssociationDenormalizingConfiguration(PropertyPath propertyPath, boolean includeId) {
		this.propertyPath = propertyPath;
		this.includeId = includeId;
	}

	public PropertyPath getPropertyPath() {
		return propertyPath;
	}

	public boolean isIncludeId() {
		return includeId;
	}

	@Override
	public String toString() {
		return "AssociationDenormalizingConfiguration [propertyPath=" + propertyPath + ", includeId=" + includeId + "]";
	}

	static class AssociationDenormalizingConfigurationBuilder {

		private final List<String> propertyPath;
		private boolean includeId;

		public AssociationDenormalizingConfigurationBuilder(String name) {
			propertyPath = new ArrayList<>();
			propertyPath.add( name );
		}

		AssociationDenormalizingConfigurationBuilder includeId(boolean includeId) {
			this.includeId = includeId;
			return this;
		}

		public void add(String name) {
			propertyPath.add( name );
		}

		AssociationDenormalizingConfiguration build() {
			return new AssociationDenormalizingConfiguration( new PropertyPath( propertyPath ), includeId );
		}
	}
}
