/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.internal.job;

import java.util.Collections;
import java.util.Set;

import org.hibernate.scenicview.internal.stereotypes.Immutable;

/**
 * A configuration of a denormalization job.
 *
 * @author Gunnar Morling
 */
public class DenormalizationJob {

	private final String name;
	private final String aggregateRootTypeName;

	// TODO support multiple levels
	@Immutable
	private final Set<String> includedAssociations;
	private final String collectionName;
	private final String connectionId;

	public DenormalizationJob(String name, String aggregateRootTypeName, Set<String> includedAssociations, String collectionName, String connectionId) {
		this.name = name;
		this.aggregateRootTypeName = aggregateRootTypeName;
		this.includedAssociations = Collections.unmodifiableSet( includedAssociations );
		this.collectionName = collectionName;
		this.connectionId = connectionId;
	}

	public String getName() {
		return name;
	}

	public String getAggregateRootTypeName() {
		return aggregateRootTypeName;
	}

	public Set<String> getIncludedAssociations() {
		return includedAssociations;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public String getConnectionId() {
		return connectionId;
	}
}
