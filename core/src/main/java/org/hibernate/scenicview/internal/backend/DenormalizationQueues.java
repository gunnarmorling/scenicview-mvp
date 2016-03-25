/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.internal.backend;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.scenicview.internal.stereotypes.ThreadScoped;
import org.hibernate.scenicview.internal.transaction.TransactionContext.TransactionAware;

/**
 * Keeps a {@link DenormalizationQueue} with denormalization tasks per logical connection. Bound to one transaction.
 *
 * @author Gunnar Morling
 */
@ThreadScoped
public class DenormalizationQueues implements TransactionAware {

	private final BackendManager backendManager;
	private final Map<String, DenormalizationQueue> queuesByConnectionId;

	public DenormalizationQueues(BackendManager backendManager) {
		this.backendManager = backendManager;
		queuesByConnectionId = new HashMap<>();
	}

	public DenormalizationQueue getQueue(String connectionId) {
		return queuesByConnectionId.computeIfAbsent(
			connectionId,
			ci -> { return new DenormalizationQueue( backendManager.getBackend( connectionId ) ); }
		);
	}

	@Override
	public void onCommit() {
		for ( DenormalizationQueue queue : queuesByConnectionId.values() ) {
			queue.flush();
		}
	}
}
