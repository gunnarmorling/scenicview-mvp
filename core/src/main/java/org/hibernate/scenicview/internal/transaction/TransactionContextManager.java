/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.internal.transaction;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.hibernate.Transaction;
import org.hibernate.event.spi.EventSource;
import org.hibernate.service.Service;

/**
 * @author Gunnar Morling
 *
 */
public class TransactionContextManager implements Service {

	private final ConcurrentMap<Transaction, TransactionContext> contextsByTx = new ConcurrentHashMap<>();

	public TransactionContext getTransactionContext(EventSource eventSource) {
		return contextsByTx.computeIfAbsent(
			eventSource.getTransaction(),
			tx -> {
				TransactionContext context = new TransactionContext();

				// on commit, trigger context commit hook and remove context
				eventSource.getActionQueue().registerProcess( (success, session) -> {
					try {
						if ( success ) {
							context.onCommit();
						}
					}
					finally {
						contextsByTx.remove( tx );
					}
				} );

				return context;
			}
		);
	}
}
