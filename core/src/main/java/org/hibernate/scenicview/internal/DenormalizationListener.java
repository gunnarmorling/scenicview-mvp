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

import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.scenicview.internal.backend.BackendManager;
import org.hibernate.scenicview.internal.backend.DenormalizationQueues;
import org.hibernate.scenicview.internal.denormalizer.Denormalizer;
import org.hibernate.scenicview.internal.job.JobManager;
import org.hibernate.scenicview.internal.stereotypes.ThreadSafe;
import org.hibernate.scenicview.internal.transaction.TransactionContextManager;
import org.hibernate.scenicview.spi.backend.model.DenormalizationTask;

/**
 * @author Gunnar Morling
 *
 */
public class DenormalizationListener implements PostInsertEventListener, PostUpdateEventListener {

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
		DenormalizationQueues queues = getQueues( event.getSession() );
		List<Denormalizer> denormalizers = getDenormalizers( event.getPersister() );

		for ( Denormalizer denormalizer : denormalizers ) {
			Supplier<DenormalizationTask> task = denormalizer.getTaskForInsert( event );
			queues.getQueue( denormalizer.getConfig().getConnectionId() ).add( task );
		}
	}

	@Override
	public void onPostUpdate(PostUpdateEvent event) {
		DenormalizationQueues queues = getQueues( event.getSession() );
		List<Denormalizer> denormalizers = getDenormalizers( event.getPersister() );

		for ( Denormalizer denormalizer : denormalizers ) {
			Supplier<DenormalizationTask> task = denormalizer.getTaskForUpdate( event );
			queues.getQueue( denormalizer.getConfig().getConnectionId() ).add( task );
		}
	}

	@Override
	public boolean requiresPostCommitHanding(EntityPersister persister) {
		return true;
	}

	private DenormalizationQueues getQueues(EventSource eventSource) {
		return transactionContextManager.getTransactionContext( eventSource )
				.computeIfAbsent(
						DenormalizationQueues.class,
						t -> new DenormalizationQueues( backendManager)
		);
	}

	private List<Denormalizer> getDenormalizers(EntityPersister entityPersister) {
		return denormalizersByType.computeIfAbsent(
			entityPersister.getEntityName(),
			entityName -> {
				return jobManager.getJobs( entityName )
					.stream()
					.map( job -> {
						return new Denormalizer(
								(AbstractEntityPersister) entityPersister,
								backendManager.getBackend( job.getConnectionId() ).getTypeProvider(),
								job
						);
					} )
					.collect( Collectors.toList() );
			}
		);
	}
}
