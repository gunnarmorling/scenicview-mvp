/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.internal.backend;

import java.util.Map;

import org.hibernate.boot.registry.StandardServiceInitiator;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.service.spi.ServiceRegistryImplementor;

/**
 * @author Gunnar Morling
 *
 */
public class BackendManagerInitiator implements StandardServiceInitiator<BackendManager> {

	public static final BackendManagerInitiator INSTANCE = new BackendManagerInitiator();

	@Override
	public Class<BackendManager> getServiceInitiated() {
		return BackendManager.class;
	}

	@Override
	public BackendManager initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
		return new BackendManager( configurationValues, registry.getService( ClassLoaderService.class ) );
	}
}
