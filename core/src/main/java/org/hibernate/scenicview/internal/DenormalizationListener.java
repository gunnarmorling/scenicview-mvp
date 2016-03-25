/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.internal;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.scenicview.internal.backend.BackendManager;
import org.hibernate.scenicview.internal.backend.DenormalizationQueues;
import org.hibernate.scenicview.internal.denormalizer.Denormalizer;
import org.hibernate.scenicview.internal.job.JobManager;
import org.hibernate.scenicview.internal.stereotypes.ThreadSafe;
import org.hibernate.scenicview.internal.transaction.TransactionContext;
import org.hibernate.scenicview.internal.transaction.TransactionContextManager;
import org.hibernate.scenicview.spi.backend.model.DenormalizationTask;

/**
 * @author Gunnar Morling
 *
 */
public class DenormalizationListener implements PostInsertEventListener {

	@ThreadSafe
	private final JobManager jobManager;

	@ThreadSafe
	private final BackendManager backendManager;

	@ThreadSafe
	private final TransactionContextManager transactionContextManager;

	// TODO Move to SF-scoped service
	private final ConcurrentMap<String, List<Denormalizer>> denormalizersByType;

	public DenormalizationListener(JobManager jobManager, BackendManager backendManager, TransactionContextManager transactionContextManager) {
		this.jobManager = jobManager;
		this.backendManager = backendManager;
		this.transactionContextManager = transactionContextManager;
		this.denormalizersByType = new ConcurrentHashMap<>();
	}

	@Override
	public void onPostInsert(PostInsertEvent event) {
		TransactionContext txContext = transactionContextManager.getTransactionContext( event.getSession() );
		DenormalizationQueues queues = txContext.computeIfAbsent( DenormalizationQueues.class, (t) -> { return new DenormalizationQueues( backendManager ); } );

		List<Denormalizer> denormalizers = denormalizersByType.computeIfAbsent(
			event.getPersister().getEntityName(),
			entityName -> {
				return jobManager.getJobs( entityName )
					.stream()
					.map( job -> {
						return new Denormalizer(
								(AbstractEntityPersister) event.getPersister(),
								backendManager.getBackend( job.getConnectionId() ).getTypeProvider(),
								job
						);
					} )
					.collect( Collectors.toList() );
			}
		);

		for ( Denormalizer denormalizer : denormalizers ) {
			Supplier<DenormalizationTask> task = denormalizer.handleInsert( event );
			queues.getQueue( denormalizer.getConfig().getConnectionId() ).add( task );
		}
	}

	@Override
	public boolean requiresPostCommitHanding(EntityPersister persister) {
		return true;
	}
}
