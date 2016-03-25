/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.internal.model;

import java.util.function.Supplier;

import org.hibernate.scenicview.spi.backend.model.ColumnSequence;
import org.hibernate.scenicview.spi.backend.model.DenormalizationTaskHandler;
import org.hibernate.scenicview.spi.backend.model.TreeTraversalSequence;
import org.hibernate.scenicview.spi.backend.model.UpsertTask;

/**
 * @author Gunnar Morling
 *
 */
public class UpsertTaskImpl implements UpsertTask {

	private final String collectionName;
	private final Supplier<ColumnSequence> primaryKey;
	private final Supplier<TreeTraversalSequence> tree;

	public UpsertTaskImpl(String collectionName, Supplier<ColumnSequence> primaryKey, Supplier<TreeTraversalSequence> tree) {
		this.collectionName = collectionName;
		this.primaryKey = primaryKey;
		this.tree = tree;
	}

	@Override
	public String getCollectionName() {
		return collectionName;
	}

	@Override
	public void accept(DenormalizationTaskHandler handler) {
		handler.handleUpsert( this );
	}

	@Override
	public ColumnSequence getPrimaryKey() {
		return primaryKey.get();
	}

	@Override
	public TreeTraversalSequence getObjectTree() {
		return tree.get();
	}
}
