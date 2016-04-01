/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.internal.model;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Objects;
import java.util.Set;

import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.event.spi.EventSource;
import org.hibernate.persister.collection.BasicCollectionPersister;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.scenicview.spi.backend.model.ColumnSequence;
import org.hibernate.scenicview.spi.backend.model.TreeTraversalSequence;
import org.hibernate.scenicview.spi.backend.type.DenormalizationBackendType;
import org.hibernate.scenicview.spi.backend.type.DenormalizationBackendType.ColumnValueReceiver;
import org.hibernate.scenicview.spi.backend.type.TypeProvider;
import org.hibernate.type.CollectionType;
import org.hibernate.type.ListType;
import org.hibernate.type.MapType;
import org.hibernate.type.SetType;
import org.hibernate.type.Type;

/**
 * @author Gunnar Morling
 *
 */
public class EntityStateBasedTreeTraversalSequence implements TreeTraversalSequence {

	private final TypeProvider typeProvider;
	private final Deque<EventWithTree> backlog = new ArrayDeque<>();
	private final EventSource session;
	private final Set<String> includedAssociations;

	public EntityStateBasedTreeTraversalSequence(TypeProvider typeProvider, Object[] state, Set<String> includedAssociations, EventSource session, EntityPersister persister) {
		this.typeProvider = typeProvider;
		this.session = session;
		this.includedAssociations = includedAssociations;
		pushObject( state, new EntityPropertiesMetadata( (AbstractEntityPersister) persister ), null, true );
	}

	@Override
	public <T> void forEach(T traversalContext, TreeTraversalEventConsumer<T> consumer) {
		EventWithTree current;

		while( !backlog.isEmpty() ) {
			current = backlog.pollFirst();

			if ( current.tree instanceof Object[] ) {
				Object[] state = (Object[]) current.tree;
				for( int i = 0; i < state.length; i++ ) {
					if ( current.metadata.getPropertyType( i ).isCollectionType() && includedAssociations.contains( current.metadata.getPropertyName( i ) ) ) {
						CollectionPersister collectionPersister = session.getPersistenceContext().getCollectionEntry( (PersistentCollection) state[i] ).getCurrentPersister();

						pushCollection(
								(PersistentCollection) state[i],
								current.metadata.getPropertyName( i ),
								collectionPersister
						);
					}
					else if ( current.metadata.getPropertyType( i ).isAssociationType() && includedAssociations.contains( current.metadata.getPropertyName( i ) ) ) {
						EntityEntry entityEntry = session.getPersistenceContext().getEntry( state[i] );

						if ( entityEntry == null ) {
							continue;
						}

						Object[] newState = entityEntry.getLoadedState();
						EntityPersister persister = session.getEntityPersister( session.getEntityName( state[i] ), state[i] );
						pushObject( newState, new EntityPropertiesMetadata( (AbstractEntityPersister) persister ), current.metadata.getPropertyName( i ), false );
					}
				}
			}
			else if ( current.tree instanceof PersistentCollection ) {
				CollectionPersister collectionPersister = session.getPersistenceContext().getCollectionEntry( (PersistentCollection) current.tree ).getCurrentPersister();

				if ( collectionPersister.getElementDefinition().getType().isEntityType() ) {
					EntityPersister elementPersister = collectionPersister.getElementDefinition().toEntityDefinition().getEntityPersister();

					for ( Object element : (Iterable<?>) current.tree ) {
						Object[] newState = elementPersister.getPropertyValues( element );
						pushObject( newState, new EntityPropertiesMetadata( (AbstractEntityPersister) elementPersister ), null, false );
					}
				}
				// basic collection
				else {
					for ( Object element : (Iterable<?>) current.tree ) {
						pushBasic( element, new BasicCollectionElementPropertiesMetadata( ( (BasicCollectionPersister) collectionPersister ) ), null );
					}
				}
			}


			consumer.consume( current, traversalContext );
		}
	}

	private void pushObject(Object[] tree, PropertiesMetadata metadata, String name, boolean aggregateRoot) {
		backlog.addFirst( new EventWithTree( aggregateRoot ? EventType.AGGREGATE_ROOT_END : EventType.OBJECT_END, name, null, null, null, null ) );
		backlog.addFirst( new EventWithTree( aggregateRoot ? EventType.AGGREGATE_ROOT_START : EventType.OBJECT_START, name, null, null, tree, metadata ) );
	}

	private void pushBasic(Object tree, PropertiesMetadata metadata, String name) {
		backlog.addFirst( new EventWithTree( EventType.OBJECT_END, name, null, null, null, null ) );
		backlog.addFirst( new EventWithTree( EventType.OBJECT_START, name, null, null, tree, metadata ) );
	}

	private void pushCollection(PersistentCollection collection, String name, CollectionPersister collectionPersister) {
		AssociationElementKind associationElementKind = getAssociationElementKind( collectionPersister.getCollectionMetadata().getElementType() );
		AssociationKind associationKind = getAssociationKind( collectionPersister );

		backlog.addFirst( new EventWithTree( EventType.COLLECTION_END, name, associationKind, associationElementKind, null, null ) );
		backlog.addFirst( new EventWithTree( EventType.COLLECTION_START, name, associationKind, associationElementKind, collection, null ) );
	}

	private AssociationKind getAssociationKind(CollectionPersister collectionPersister) {
		CollectionType collectionType = collectionPersister.getCollectionType();

		if ( collectionType.isArrayType() ) {
			return AssociationKind.LIST;
		}
		else if ( collectionType instanceof SetType ) {
			return AssociationKind.SET;
		}
		else if ( collectionType instanceof MapType ) {
			return AssociationKind.MAP;
		}
		else if ( collectionType instanceof ListType ) {
			return AssociationKind.LIST;
		}
		else {
			return AssociationKind.BAG;
		}
	}

	private AssociationElementKind getAssociationElementKind(Type collectionElementType) {
		if ( collectionElementType.isEntityType() ) {
			return AssociationElementKind.ENTITY;
		}
		else if ( collectionElementType.isComponentType() ) {
			return AssociationElementKind.EMBEDDABLE;
		}
		else {
			return AssociationElementKind.BASIC;
		}
	}

	private class EventWithTree extends TreeTraversalEventBase {

		private final Object tree;
		private final PropertiesMetadata metadata;

		public EventWithTree(EventType type, String name, AssociationKind associationKind, AssociationElementKind associationElementKind, Object tree, PropertiesMetadata metadata) {
			super( type, name, associationKind, associationElementKind );

			this.tree = tree;
			this.metadata = metadata;
		}

		@Override
		public ColumnSequence getColumnSequence() {
			if ( tree instanceof PersistentCollection ) {
				return null;
			}
			if ( tree instanceof Object[] ) {
				return new EntityStateBasedColumnSequence( typeProvider, (Object[]) tree, metadata );
			}
			else {
				return new BasicElementColumnSequence( typeProvider, tree, metadata );
			}
		}

		@Override
		public String toString() {
			String treeAsString = tree instanceof Object[] ? Arrays.toString( (Object[]) tree ) : Objects.toString( tree );
			return "EventWithTree [type=" + getType() + ", tree=" + treeAsString + "]";
		}
	}


	private static class EntityStateBasedColumnSequence implements ColumnSequence {

		private final TypeProvider typeProvider;
		private final Object[] objectState;
		private final PropertiesMetadata metadata;

		public EntityStateBasedColumnSequence(TypeProvider typeProvider, Object[] objectState, PropertiesMetadata metadata) {
			this.typeProvider = typeProvider;
			this.objectState = objectState;
			this.metadata = metadata;
		}

		@Override
		public void forEach(ColumnValueConsumer consumer) {
			MutableVariableSizeColumnSequence receiver = new MutableVariableSizeColumnSequence();

			for ( int i = 0; i < objectState.length; i++ ) {
				if ( metadata.getPropertyType( i ).isAssociationType() ) {
					continue;
				}

				receiver.addColumnNames( metadata.getColumnNames( i ) );

				DenormalizationBackendType<?> type = typeProvider.getType( metadata.getPropertyType( i ).getReturnedClass() );
				set( type, objectState[i], receiver );
			}

			receiver.forEach( consumer );
		}

		@SuppressWarnings("unchecked")
		private <T> void set(DenormalizationBackendType<?> type, Object value, ColumnValueReceiver receiver) {
			DenormalizationBackendType<T> typedType = (DenormalizationBackendType<T>) type;
			T typedValue = (T) value;
			typedType.getWriter().set( typedValue, receiver );
		}
	}

	/**
	 * "Column sequence" for an element of a basic collection.
	 *
	 * @author Gunnar Morling
	 */
	private static class BasicElementColumnSequence implements ColumnSequence {

		private final TypeProvider typeProvider;
		private final Object value;
		private final PropertiesMetadata metadata;

		public BasicElementColumnSequence(TypeProvider typeProvider, Object value, PropertiesMetadata metadata) {
			this.typeProvider = typeProvider;
			this.value = value;
			this.metadata = metadata;
		}

		@Override
		public void forEach(ColumnValueConsumer consumer) {
			String[] columnNames = metadata.getColumnNames( 0 );
			MutableFixedSizeColumnSequence receiver = new MutableFixedSizeColumnSequence( columnNames );
			DenormalizationBackendType<?> type = typeProvider.getType( metadata.getPropertyType( 0 ).getReturnedClass() );

			set( type, value, receiver );

			for ( int i = 0; i < columnNames.length; i++ ) {
				consumer.consumeValue( columnNames[i], receiver.getColumnValues()[i] );
			}
		}

		@SuppressWarnings("unchecked")
		private <T> void set(DenormalizationBackendType<?> type, Object value, ColumnValueReceiver receiver) {
			DenormalizationBackendType<T> typedType = (DenormalizationBackendType<T>) type;
			T typedValue = (T) value;
			typedType.getWriter().set( typedValue, receiver );
		}
	}

	private interface PropertiesMetadata {
		Type getPropertyType(int index);
		String getPropertyName(int index);
		String[] getColumnNames(int index);
	}

	private static class EntityPropertiesMetadata implements PropertiesMetadata {

		private final AbstractEntityPersister persister;

		public EntityPropertiesMetadata(AbstractEntityPersister persister) {
			this.persister = persister;
		}

		@Override
		public Type getPropertyType(int index) {
			return persister.getPropertyTypes()[index];
		}

		@Override
		public String getPropertyName(int index) {
			return persister.getPropertyNames()[index];
		}

		@Override
		public String[] getColumnNames(int index) {
			return persister.getPropertyColumnNames( index );
		}
	}

	private static class BasicCollectionElementPropertiesMetadata implements PropertiesMetadata {

		private final BasicCollectionPersister persister;

		public BasicCollectionElementPropertiesMetadata(BasicCollectionPersister persister) {
			this.persister = persister;
		}

		@Override
		public Type getPropertyType(int index) {
			return persister.getElementType();
		}

		@Override
		public String getPropertyName(int index) {
			return null;
		}

		@Override
		public String[] getColumnNames(int index) {
			return persister.getElementColumnNames();
		}
	}
}
