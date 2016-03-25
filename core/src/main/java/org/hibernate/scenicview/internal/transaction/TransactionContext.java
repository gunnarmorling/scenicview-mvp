/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.internal.transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Container for transaction-scoped payload.
 *
 * @author Gunnar Morling
 */
public class TransactionContext {

	private final Map<Class<?>, TransactionAware> clients = new HashMap<>();

	public <T extends TransactionAware> T computeIfAbsent(Class<T> type, Function<Class<T>, T> provider) {
		@SuppressWarnings("unchecked")
		T client = (T) clients.computeIfAbsent( type, k -> { return provider.apply( type ); } );
		return client;
	}

	public void onCommit() {
		for ( TransactionAware client : clients.values() ) {
			client.onCommit();
		}
	}

	public interface TransactionAware {
		public void onCommit();
	}
}
