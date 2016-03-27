/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.test.poc.type;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.scenicview.test.poc.model.Money;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;

/**
 * @author Gunnar Morling
 *
 */
public class MoneyUserType implements CompositeUserType {

	@Override
	public String[] getPropertyNames() {
		return new String[] { "amount", "currency" };
	}

	@Override
	public Type[] getPropertyTypes() {
		return new Type[] { StandardBasicTypes.LONG, StandardBasicTypes.STRING };
	}

	@Override
	public Object getPropertyValue(Object component, int property) throws HibernateException {
		return property == 0 ? ( (Money) component ).getAmount() : ( (Money) component ).getCurrency();
	}

	@Override
	public void setPropertyValue(Object component, int property, Object value) throws HibernateException {
		throw new UnsupportedOperationException( "Not implemented" );
	}

	@Override
	public Class<Money> returnedClass() {
		return Money.class;
	}

	@Override
	public boolean equals(Object x, Object y) throws HibernateException {
		return Objects.equals( x, y );
	}

	@Override
	public int hashCode(Object x) throws HibernateException {
		throw new UnsupportedOperationException( "Not implemented" );
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
		return new Money( rs.getLong( names[0] ), rs.getString( names[1] ) );
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
		if ( value != null ) {
			StandardBasicTypes.LONG.nullSafeSet( st, ( (Money) value ).getAmount(), index, session );
			StandardBasicTypes.STRING.nullSafeSet( st, ( (Money) value ).getCurrency(), index + 1, session );
		}
		else {
			StandardBasicTypes.LONG.nullSafeSet( st, null, index, session );
			StandardBasicTypes.STRING.nullSafeSet( st, null, index + 1, session );
		}
	}

	@Override
	public Object deepCopy(Object value) throws HibernateException {
		return value != null ? new Money( (Money) value ) : null;
	}

	@Override
	public boolean isMutable() {
		return true;
	}

	@Override
	public Serializable disassemble(Object value, SessionImplementor session) throws HibernateException {
		throw new UnsupportedOperationException( "Not implemented" );
	}

	@Override
	public Object assemble(Serializable cached, SessionImplementor session, Object owner) throws HibernateException {
		throw new UnsupportedOperationException( "Not implemented" );
	}

	@Override
	public Object replace(Object original, Object target, SessionImplementor session, Object owner) throws HibernateException {
		throw new UnsupportedOperationException( "Not implemented" );
	}
}
