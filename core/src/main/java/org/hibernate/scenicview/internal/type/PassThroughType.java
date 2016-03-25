/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.internal.type;

import org.hibernate.scenicview.spi.backend.type.DenormalizationBackendType;

/**
 * @author Gunnar Morling
 *
 */
public class PassThroughType implements DenormalizationBackendType<Object> {

	public static final PassThroughType INSTANCE = new PassThroughType();

	private PassThroughType() {
	}

	@Override
	public ColumnWriter<Object> getWriter() {
		return PassThroughWriter.INSTANCE;
	}

	private static class PassThroughWriter implements ColumnWriter<Object> {

		private static final PassThroughWriter INSTANCE = new PassThroughWriter();

		@Override
		public void set(Object value, ColumnValueReceiver receiver) {
			if ( value == null ) {
				return;
			}

			receiver.set( 0, value );
		}
	}
}
