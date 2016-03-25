/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.internal.type;

import org.hibernate.scenicview.spi.backend.type.SingleColumnDenormalizationBackendType.SingleColumnValueReceiver;

/**
 * @author Gunnar Morling
 */
public class SingleColumnValueReceiverAdapter implements SingleColumnValueReceiver {

	Object result;

	@Override
	public void set(Object value) {
		result = value;
	}

	public Object getResult() {
		return result;
	}
}
