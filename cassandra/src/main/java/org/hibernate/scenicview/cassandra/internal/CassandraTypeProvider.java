/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.cassandra.internal;

import org.hibernate.scenicview.spi.backend.type.DenormalizationBackendType;
import org.hibernate.scenicview.spi.backend.type.TypeProvider;
import org.hibernate.scenicview.test.poc.model.Money;

/**
 * @author Gunnar Morling
 */
class CassandraTypeProvider implements TypeProvider {

	@Override
	public DenormalizationBackendType<?> getType(Class<?> propertyType) {
		// TODO user type
		if ( propertyType == Money.class ) {
			return new MoneyDenormalizationBackendType();
		}
		else {
			return TypeProvider.super.getType( propertyType );
		}
	}

	private static class MoneyDenormalizationBackendType implements DenormalizationBackendType<Money> {

		@Override
		public ColumnWriter<Money> getWriter() {
			return ( value, receiver ) -> {
				if ( value == null ) {
					return;
				}

				receiver.set( 0, value.getAmount() );
				receiver.set( 1, value.getCurrency() );
			};
		}
	}
}
