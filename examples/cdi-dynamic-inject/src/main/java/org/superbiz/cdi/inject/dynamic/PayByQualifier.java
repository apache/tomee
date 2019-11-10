package org.superbiz.cdi.inject.dynamic;

import javax.enterprise.inject.Instance;
import javax.enterprise.util.AnnotationLiteral;

import org.superbiz.cdi.qualifier.PaymentQualifier;
import org.superbiz.cdi.qualifier.PaymentType;

/**
 * 
 * This class will help the CDI the choose correct implementation using qualifier.
 * <br>
 * To use <b>.select</b> of the method of the {@link Instance} object with qualifier, is need create a Class the extends {@link AnnotationLiteral}
 * 
 * */
public class PayByQualifier extends AnnotationLiteral<PaymentQualifier> implements PaymentQualifier {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private PaymentType type;

	public PayByQualifier(PaymentType type) {
		
		this.type = type;
	}
	
	@Override
	public PaymentType type() {
		return type;
	}
}
