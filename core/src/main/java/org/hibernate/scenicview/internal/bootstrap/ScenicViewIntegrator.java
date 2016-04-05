/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.internal.bootstrap;

import java.util.Map;

import org.hibernate.boot.Metadata;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.scenicview.internal.DenormalizationListener;
import org.hibernate.scenicview.internal.backend.BackendManager;
import org.hibernate.scenicview.internal.job.JobManager;
import org.hibernate.scenicview.internal.job.JobManagerFactory;
import org.hibernate.scenicview.internal.transaction.TransactionContextManager;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

/**
 * @author Gunnar Morling
 *
 */
public class ScenicViewIntegrator implements Integrator {

	@Override
	public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
		EventListenerRegistry listenerRegistry = serviceRegistry.getService( EventListenerRegistry.class );

		// TODO JobManager is not a service for now as we cannot easily customize services needing access to metadata
		Map<?, ?> settings = serviceRegistry.getService( ConfigurationService.class ).getSettings();
		JobManager jobManager = new JobManagerFactory( metadata, settings, serviceRegistry ).getJobManager();

		BackendManager backendManager = serviceRegistry.getService( BackendManager.class );
		TransactionContextManager transactionContextManager = serviceRegistry.getService( TransactionContextManager.class );

		DenormalizationListener denormalizationListener = new DenormalizationListener( jobManager, backendManager, transactionContextManager );

		listenerRegistry.appendListeners( EventType.POST_INSERT, denormalizationListener );
		listenerRegistry.appendListeners( EventType.POST_UPDATE, denormalizationListener );
	}

	@Override
	public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
	}
}
