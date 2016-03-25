/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.internal.propertyliteral;

import static net.bytebuddy.matcher.ElementMatchers.named;

import java.util.function.Function;

import org.hibernate.HibernateException;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * @author Gunnar Morling
 */
public class PropertyLiteralHelper {

	//TODO cache proxies

	public static <T> String getPropertyName(Class<T> type, Function<T, ?> propertyLiteral) {
		T capturer = getPropertyLiteralCapturer( type );
		propertyLiteral.apply( capturer );
		return ( (PropertyLiteralCapturer) capturer ).getLastInvokedProperty();
	}

	private static <T> T /* & PropertyLiteralCapturer */ getPropertyLiteralCapturer(Class<T> type) {
		DynamicType.Builder<?> builder = new ByteBuddy()
				.subclass( type.isInterface() ? Object.class : type );

		if ( type.isInterface() ) {
			builder = builder.implement( type );
		}

		Class<?> proxyType = new ByteBuddy()
			.subclass( type )
			.implement( PropertyLiteralCapturer.class )
			.defineField( "lastInvokedProperty", String.class, Visibility.PRIVATE )
			.method( ElementMatchers.any() )
				.intercept( MethodDelegation.to( PropertyLiteralCapturingInterceptor.class ) )
			.method( named( "setLastInvokedProperty" ).or( named( "getLastInvokedProperty" ) ) )
				.intercept( FieldAccessor.ofBeanProperty() )
			.make()
			.load(PropertyLiteralHelper.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
			.getLoaded();

		try {
			@SuppressWarnings("unchecked")
			Class<T> typed = (Class<T>) proxyType;
			return typed.newInstance();
		}
		catch (InstantiationException | IllegalAccessException e) {
			throw new HibernateException( "Couldn't instantiate proxy for property literal dereferencing", e );
		}
	}
}
