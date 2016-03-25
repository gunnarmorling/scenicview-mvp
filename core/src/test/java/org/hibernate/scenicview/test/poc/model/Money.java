/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.test.poc.model;


/**
 * @author Gunnar Morling
 *
 */
public class Money {

	private long amount;
	private String currency;

	Money() {
	}

	public Money(Money original) {
		this.amount = original.amount;
		this.currency = original.currency;
	}

	public Money(long amount, String currency) {
		this.amount = amount;
		this.currency = currency;
	}

	public long getAmount() {
		return amount;
	}

	public void setAmount(long amount) {
		this.amount = amount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) ( amount ^ ( amount >>> 32 ) );
		result = prime * result + ( ( currency == null ) ? 0 : currency.hashCode() );
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		Money other = (Money) obj;
		if ( amount != other.amount )
			return false;
		if ( currency == null ) {
			if ( other.currency != null )
				return false;
		}
		else if ( !currency.equals( other.currency ) )
			return false;
		return true;
	}
}
