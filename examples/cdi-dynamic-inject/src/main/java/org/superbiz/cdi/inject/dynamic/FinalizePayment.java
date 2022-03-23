/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.cdi.inject.dynamic;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

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
