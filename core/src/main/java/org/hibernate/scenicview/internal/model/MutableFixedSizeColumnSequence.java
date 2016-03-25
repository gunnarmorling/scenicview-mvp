/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.internal.model;

import org.hibernate.scenicview.spi.backend.model.ColumnSequence;
import org.hibernate.scenicview.spi.backend.type.DenormalizationBackendType.ColumnValueReceiver;

/**
 * @author Gunnar Morling
 */
public class MutableFixedSizeColumnSequence implements ColumnValueReceiver, ColumnSequence {

	private final String[] columnNames;
	private final Object[] columnValues;

	public MutableFixedSizeColumnSequence(String[] columnNames) {
		this.columnNames = columnNames;
		this.columnValues = new Object[columnNames.length];
	}

	@Override
	public void set(int index, Object value) {
		columnValues[index] = value;
	}

	public Object[] getColumnValues() {
		return columnValues;
	}

	@Override
	public void forEach(ColumnValueConsumer consumer) {
		for ( int i = 0; i < columnNames.length; i++ ) {
			Object value = columnValues[i];

			if ( value != null ) {
				consumer.consumeValue( columnNames[i], value );
			}
		}
	}
}
