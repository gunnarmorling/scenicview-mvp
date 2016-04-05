/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.internal.job;

import java.util.Map;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.scenicview.config.DenormalizationJobConfigurator;
import org.hibernate.service.spi.ServiceRegistryImplementor;

/**
 * @author Gunnar Morling
 *
 */
public class JobManagerFactory {

	private final Metadata metadata;
	private final Map<?, ?> configurationValues;
	private final ServiceRegistryImplementor registry;

	public JobManagerFactory(Metadata metadata, Map<?, ?> configurationValues, ServiceRegistryImplementor registry) {
		this.metadata = metadata;
		this.configurationValues = configurationValues;
		this.registry = registry;
	}

	public JobManager getJobManager() {
		DenormalizationJobConfigurator jobConfig = getJobConfig( configurationValues, registry.getService( ClassLoaderService.class ) );
		return new JobManager( metadata, jobConfig );
	}

	private DenormalizationJobConfigurator getJobConfig(Map<?, ?> configurationValues, ClassLoaderService classLoaderService) {
		String configTypeName = (String) configurationValues.get( "hibernate.scenicview.jobconfig" );

		if ( configTypeName == null ) {
			return NoOpJobConfig.INSTANCE;
		}

		Class<? extends DenormalizationJobConfigurator> configType = classLoaderService.classForName( configTypeName );

		try {
			return configType.newInstance();
		}
		catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException( "Could not instantiate config type: " + configType );
		}
	}

	private static class NoOpJobConfig implements DenormalizationJobConfigurator {

		private static NoOpJobConfig INSTANCE = new NoOpJobConfig();

		@Override
		public void configure(Builder builder) {
		}
	}
}
