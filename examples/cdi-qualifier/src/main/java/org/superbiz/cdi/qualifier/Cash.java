package org.superbiz.cdi.qualifier;

/**
 * 
 * Implementation for {@link Payment} interfaces
 * <br>
 * This implementation have a qualifier annotation to help CDI choose the implementation correct.
 * 
 * */
@PaymentQualifier(type=PaymentType.CASH)
public class Cash implements Payment {

	@Override
	public String pay() {
		
		return "cash";
	}
}
