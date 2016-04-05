/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.internal.bootstrap;

import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.scenicview.internal.backend.BackendManagerInitiator;
import org.hibernate.scenicview.internal.transaction.TransactionContextManagerInitiator;
import org.hibernate.service.spi.ServiceContributor;

/**
 * Registers ScenicView's service initiators. Discovered through Java's service loader.
 *
 * @author Gunnar Morling
 */
public class ScenicViewServiceRegistryInitializer implements ServiceContributor {

	@Override
	public void contribute(StandardServiceRegistryBuilder serviceRegistryBuilder) {
		serviceRegistryBuilder.addInitiator( BackendManagerInitiator.INSTANCE );
		serviceRegistryBuilder.addInitiator( TransactionContextManagerInitiator.INSTANCE );
	}
}
