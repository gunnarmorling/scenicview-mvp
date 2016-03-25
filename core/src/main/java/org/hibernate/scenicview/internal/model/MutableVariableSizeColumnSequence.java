/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.scenicview.spi.backend.model.ColumnSequence;
import org.hibernate.scenicview.spi.backend.type.DenormalizationBackendType.ColumnValueReceiver;

class MutableVariableSizeColumnSequence implements ColumnValueReceiver, ColumnSequence {

	private final List<String> columnNames;
	private final List<Object> columnValues;
	private int base = 0;

	MutableVariableSizeColumnSequence() {
		this.columnNames = new ArrayList<>();
		this.columnValues = new ArrayList<>();
	}

	public void addColumnNames(String[] columnNames) {
		for ( String column : columnNames ) {
			this.columnNames.add( column );
			this.columnValues.add( null );
		}

		base = this.columnNames.size() - columnNames.length;
	}

	@Override
	public void set(int index, Object value) {
		columnValues.set( base + index, value );
	}

	@Override
	public void forEach(ColumnValueConsumer consumer) {
		for ( int i = 0; i < columnNames.size(); i++ ) {
			Object value = columnValues.get( i );

			if ( value != null ) {
				consumer.consumeValue( columnNames.get( i ), value );
			}
		}
	}
}
