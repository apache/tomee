package org.superbiz.cdi.inject.dynamic;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.superbiz.cdi.qualifier.Cash;
import org.superbiz.cdi.qualifier.CreditCard;
import org.superbiz.cdi.qualifier.Payment;
import org.superbiz.cdi.qualifier.PaymentType;

/**
 * 
 * This class show how the CDI choose dynamically your implementation injected using: 
 * <li> {@link Instance} object
 * <li> Annotation {@link Any}
 * <br>
 * Each implementation exist a {@link PostConstruct} annotation to show when the Component is instance. 
 * 
 * */
public class FinalizePayment {

	@Inject
	@Any
	private Instance<Payment> paymentsLazy;
	
	Payment paymentChoosed;
	
	public String finishWithCash() {

		paymentChoosed = paymentsLazy.select(Cash.class).get();
		return paymentChoosed.pay();
	}
	
	public String finishWithCreditCard() {

		paymentChoosed = paymentsLazy.select(CreditCard.class).get();
		return paymentChoosed.pay();
	}

	public String finishByQualifier(PaymentType type) {

		paymentChoosed = paymentsLazy.select( new PayByQualifier(type) ).get();
		
		return paymentChoosed.pay();
	}
}
