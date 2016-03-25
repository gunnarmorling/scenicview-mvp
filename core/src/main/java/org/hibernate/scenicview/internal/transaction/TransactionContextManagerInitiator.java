/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.internal.transaction;

import java.util.Map;

import org.hibernate.boot.registry.StandardServiceInitiator;
import org.hibernate.service.spi.ServiceRegistryImplementor;

/**
 * @author Gunnar Morling
 *
 */
public class TransactionContextManagerInitiator implements StandardServiceInitiator<TransactionContextManager> {

	public static final TransactionContextManagerInitiator INSTANCE = new TransactionContextManagerInitiator();

	@Override
	public Class<TransactionContextManager> getServiceInitiated() {
		return TransactionContextManager.class;
	}

	@Override
	public TransactionContextManager initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
		return new TransactionContextManager();
	}
}
