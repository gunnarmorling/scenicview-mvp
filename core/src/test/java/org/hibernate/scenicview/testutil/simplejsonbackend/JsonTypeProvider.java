/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.testutil.simplejsonbackend;

import org.hibernate.scenicview.spi.backend.type.DenormalizationBackendType;
import org.hibernate.scenicview.spi.backend.type.SingleColumnDenormalizationBackendType;
import org.hibernate.scenicview.spi.backend.type.TypeProvider;
import org.hibernate.scenicview.test.poc.model.Money;

import com.google.gson.JsonPrimitive;

/**
 * @author Gunnar Morling
 */
final class JsonTypeProvider implements TypeProvider {

	@Override
	public DenormalizationBackendType<?> getType(Class<?> propertyType) {
		if ( propertyType == String.class ) {
			return new StringDenormalizationBackendType();
		}
		else if ( Number.class.isAssignableFrom( propertyType ) || propertyType == byte.class || propertyType == short.class || propertyType == int.class
				|| propertyType == long.class || propertyType == float.class || propertyType == double.class ) {
			return new NumberDenormalizationBackendType();
		}
		else if ( propertyType == boolean.class || propertyType == Boolean.class ) {
			return new BooleanDenormalizationBackendType();
		}
		// TODO user type
		else if ( propertyType == Money.class ) {
			return new MoneyDenormalizationBackendType();
		}
		else {
			return TypeProvider.super.getType( propertyType );
		}
	}

	private static class StringDenormalizationBackendType implements SingleColumnDenormalizationBackendType<String> {

		@Override
		public SingleColumnWriter<String> getWriter() {
			return ( value, receiver ) -> {
				if ( value != null ) {
					receiver.set( new JsonPrimitive( value ) );
				}
			};
		}
	}

	private static class NumberDenormalizationBackendType implements SingleColumnDenormalizationBackendType<Number> {

		@Override
		public SingleColumnWriter<Number> getWriter() {
			return ( value, receiver ) -> {
				if ( value != null ) {
					receiver.set( new JsonPrimitive( value ) );
				}
			};
		}
	}

	private static class BooleanDenormalizationBackendType implements SingleColumnDenormalizationBackendType<Boolean> {

		@Override
		public SingleColumnWriter<Boolean> getWriter() {
			return ( value, receiver ) -> {
				if ( value != null ) {
					receiver.set( new JsonPrimitive( value ) );
				}
			};
		}
	}

	private static class MoneyDenormalizationBackendType implements DenormalizationBackendType<Money> {

		@Override
		public ColumnWriter<Money> getWriter() {
			return ( value, receiver ) -> {
				if ( value == null ) {
					return;
				}

				receiver.set( 0, new JsonPrimitive( value.getAmount() ) );
				receiver.set( 1, new JsonPrimitive( value.getCurrency() ) );
			};
		}
	}
}