/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.spi.backend.type;

import org.hibernate.scenicview.internal.type.SingleColumnValueReceiverAdapter;

/**
 * @author Gunnar Morling
 *
 */
public interface SingleColumnDenormalizationBackendType<T> extends DenormalizationBackendType<T> {

	@Override
	SingleColumnWriter<T> getWriter();

	@FunctionalInterface
	public interface SingleColumnWriter<T> extends ColumnWriter<T> {

		@Override
		public default void set(T value, ColumnValueReceiver receiver) {
			SingleColumnValueReceiverAdapter adapter = new SingleColumnValueReceiverAdapter();
			set( value, adapter );
			receiver.set( 0, adapter.getResult() );
		}

		public abstract void set(T value, SingleColumnValueReceiver receiver);
	}

	@FunctionalInterface
	public interface SingleColumnValueReceiver {
		void set(Object value);
	}
}
