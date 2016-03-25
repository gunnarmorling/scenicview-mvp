/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.internal.propertyliteral;

import java.lang.reflect.Method;

import org.hibernate.HibernateException;

import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

/**
 * Intercepts all calls to property literal deference proxies and stores the name of the given property liteal in the
 * capturer.
 *
 * @author Gunnar Morling
 */
public class PropertyLiteralCapturingInterceptor {

	@RuntimeType
	public static Object intercept(@This PropertyLiteralCapturer capturer, @Origin Method method) {
		capturer.setLastInvokedProperty( getPropertyName( method ) );
		return null;
	}

	private static String getPropertyName(Method method) {
		final boolean hasGetterSignature = method.getParameterTypes().length == 0
				&& method.getReturnType() != null;

		String name = method.getName();
		String propName = null;

		if ( hasGetterSignature ) {
			if ( name.startsWith( "get" ) && hasGetterSignature ) {
				propName = name.substring( 3, 4 ).toLowerCase() + name.substring( 4 );
			}
			else if ( name.startsWith( "is" ) && hasGetterSignature ) {
				propName = name.substring( 2, 3 ).toLowerCase() + name.substring( 3 );
			}
		}
		else {
			throw new HibernateException( "Only property getter methods are expected to be passed" );
		}

		return propName;
	}
}