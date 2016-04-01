/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.internal.model;

import org.hibernate.scenicview.spi.backend.model.TreeTraversalSequence.AssociationElementKind;
import org.hibernate.scenicview.spi.backend.model.TreeTraversalSequence.AssociationKind;
import org.hibernate.scenicview.spi.backend.model.TreeTraversalSequence.EventType;
import org.hibernate.scenicview.spi.backend.model.TreeTraversalSequence.TreeTraversalEvent;

public abstract class TreeTraversalEventBase implements TreeTraversalEvent {

	private final EventType type;
	private final String name;
	private final AssociationKind associationKind;
	private final AssociationElementKind associationElementKind;

	public TreeTraversalEventBase(EventType type, String name, AssociationKind associationKind, AssociationElementKind associationElementKind) {
		this.type = type;
		this.name = name;
		this.associationKind = associationKind;
		this.associationElementKind = associationElementKind;
	}

	@Override
	public EventType getType() {
		return type;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public AssociationKind getAssociationKind() {
		return associationKind;
	}

	@Override
	public AssociationElementKind getAssociationElementKind() {
		return associationElementKind;
	}
}
