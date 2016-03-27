/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.internal.denormalizer;

import java.io.Serializable;
import java.util.function.Supplier;

import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.scenicview.internal.job.DenormalizationJob;
import org.hibernate.scenicview.internal.model.EntityStateBasedTreeTraversalSequence;
import org.hibernate.scenicview.internal.model.MutableFixedSizeColumnSequence;
import org.hibernate.scenicview.internal.model.UpsertTaskImpl;
import org.hibernate.scenicview.spi.backend.model.ColumnSequence;
import org.hibernate.scenicview.spi.backend.model.DenormalizationTask;
import org.hibernate.scenicview.spi.backend.type.DenormalizationBackendType;
import org.hibernate.scenicview.spi.backend.type.TypeProvider;

/**
 * @author Gunnar Morling
 *
 */
public class Denormalizer {

	private final AbstractEntityPersister persister;
	private final TypeProvider typeProvider;
	private final DenormalizationJob config;
	private final DenormalizationBackendType<?> idType;

	public Denormalizer(AbstractEntityPersister entityPersister, TypeProvider typeProvider, DenormalizationJob config) {
		this.persister = entityPersister;
		this.typeProvider = typeProvider;
		this.config = config;
		this.idType = typeProvider.getType( persister.getIdentifierType().getReturnedClass() );
	}

	public Supplier<DenormalizationTask> getTaskForInsert(PostInsertEvent event) {
		return () -> {
			return new UpsertTaskImpl(
					config.getCollectionName(),
					() -> {
						return getIdColumnSequence( event.getId() );
					},
					() -> {
						return new EntityStateBasedTreeTraversalSequence(
							typeProvider,
							event.getState(),
							config.getIncludedAssociations(),
							event.getSession(),
							event.getPersister() );
					}
			);
		};
	}

	public Supplier<DenormalizationTask> getTaskForUpdate(PostUpdateEvent event) {
		return () -> {
			return new UpsertTaskImpl(
					config.getCollectionName(),
					() -> {
						return getIdColumnSequence( event.getId() );
					},
					() -> {
						return new EntityStateBasedTreeTraversalSequence(
							typeProvider,
							event.getState(),
							config.getIncludedAssociations(),
							event.getSession(),
							event.getPersister() );
					}
			);
		};
	}

	public DenormalizationJob getConfig() {
		return config;
	}

	private <T> ColumnSequence getIdColumnSequence(Serializable id) {
		String[] columnNames = persister.getIdentifierColumnNames();
		MutableFixedSizeColumnSequence receiver = new MutableFixedSizeColumnSequence( columnNames );

		T typedValue = (T) id;
		DenormalizationBackendType<T> typedType = (DenormalizationBackendType<T>) idType;
		typedType.getWriter().set( typedValue, receiver );

		return receiver;
	}
}
