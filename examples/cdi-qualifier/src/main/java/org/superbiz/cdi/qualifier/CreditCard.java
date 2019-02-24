package org.superbiz.cdi.qualifier;

/**
 * 
 * Implementation for {@link Payment} interfaces
 * <br>
 * This implementation have a qualifier annotation to help CDI choose the implementation correct.
 * 
 * */
@PaymentQualifier(type=PaymentType.CREDITCARD)
public class CreditCard implements Payment {

	@Override
	public String pay() {
		
		return "creditCard";
	}
}
