/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.spi.backend.type;

/**
 * Implementations map between Java property types and the equivalent column representation in specific backends.
 * <p>
 * Implementations working on a single column only (the vast majority of types), should be derived from
 * {@link SingleColumnDenormalizationBackendType}.
 *
 * @author Gunnar Morling
 */
public interface DenormalizationBackendType<T> {

	ColumnWriter<T> getWriter();

	@FunctionalInterface
	public interface ColumnWriter<T> {

		void set(T value, ColumnValueReceiver receiver);
	}

	@FunctionalInterface
	public interface ColumnValueReceiver {

		void set(int index, Object value);
	}
}
