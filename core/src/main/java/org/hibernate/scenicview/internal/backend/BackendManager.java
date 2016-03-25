/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.internal.backend;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.scenicview.internal.stereotypes.Immutable;
import org.hibernate.scenicview.spi.backend.DenormalizationBackend;
import org.hibernate.scenicview.spi.backend.DenormalizationBackendFactory;
import org.hibernate.service.Service;
import org.hibernate.service.spi.Stoppable;

/**
 * @author Gunnar Morling
 *
 */
public class BackendManager implements Service, Stoppable {

	@Immutable
	private final Map<String, DenormalizationBackendFactory> factories;

	@Immutable
	private final Map<String, BackendConnectionDescriptor> connectionDescriptors;

	private final ConcurrentMap<String, DenormalizationBackend> backendsByConnectionId;

	BackendManager(Map<?, ?> properties, ClassLoaderService classLoaderService) {
		factories = Collections.unmodifiableMap( loadFactories( classLoaderService ) );
		connectionDescriptors = Collections.unmodifiableMap( getConnectionDescriptors( properties ) );

		backendsByConnectionId = new ConcurrentHashMap<>();
	}

	private static Map<String,DenormalizationBackendFactory> loadFactories(ClassLoaderService classLoaderService) {
		Map<String,DenormalizationBackendFactory> factories = new HashMap<>();

		for( DenormalizationBackendFactory factory : classLoaderService.loadJavaServices( DenormalizationBackendFactory.class ) ) {
			DenormalizationBackendFactory existing = factories.get( factory.getName() );
			if ( existing != null ) {
				throw new IllegalStateException( "Found multiple factories with same name '" + existing.getName() + "': " + existing + ", " + factory );
			}

			factories.put( factory.getName(), factory );
		}

		return factories;
	}

	private static Map<String, BackendConnectionDescriptor> getConnectionDescriptors(Map<?, ?> properties) {
		return properties.entrySet()
				.stream()
				.map( e -> new AbstractMap.SimpleEntry<>( e.getKey().toString(), e.getValue().toString() ) )
				.filter( e -> e.getKey().startsWith( "hibernate.scenicview.connection" ) )
				.collect(
						Collectors.toMap(
								e -> e.getKey().substring( "hibernate.scenicview.connection".length() + 1 ),
								e -> new BackendConnectionDescriptor( e.getValue() )
						)
				);
	}

	@Override
	public void stop() {
		for ( DenormalizationBackend backend : backendsByConnectionId.values() ) {
			backend.stop();
		}
	}

	public DenormalizationBackend getBackend(String connectionId) {
		return backendsByConnectionId.computeIfAbsent( connectionId, cd -> {
			BackendConnectionDescriptor connectionDescriptor = connectionDescriptors.get( connectionId );
			if ( connectionDescriptor == null ) {
				throw new IllegalArgumentException( "Unknown connection: " + connectionId );
			}
			DenormalizationBackendFactory factory = factories.get( connectionDescriptor.getBackend() );
			if ( factory == null ) {
				throw new IllegalArgumentException( "Unknown backend: " + connectionDescriptor.getBackend() );
			}

			return factory.createNewBackend( connectionDescriptor.getConnection() );
		} );
	}
}
