/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.test.component.event.normal;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;

import org.apache.webbeans.test.annotation.binding.Role;
import org.apache.webbeans.test.component.IPayment;
import org.apache.webbeans.test.component.PaymentProcessorComponent;
import org.apache.webbeans.test.event.LoggedInEvent;

@RequestScoped
public class ComponentWithObserves2
{
    private IPayment payment;

    private String user;

    /** This gets set via the TransactionalInterceptor */
    public static boolean hasBeenIntercepted;

    public void afterLogin(@Observes @Role(value = "USER") LoggedInEvent event, PaymentProcessorComponent payment)
    {
        hasBeenIntercepted = false;
        this.payment = payment.getPaymentCheck();
        this.user = event.getUserName();
    }

    /**
     * Test if observer functions can be intercepted
     */
    @Transactional
    public void afterAdminLogin(@Observes @Role(value = "ADMIN") LoggedInEvent event, PaymentProcessorComponent payment)
    {
        hasBeenIntercepted = true;
        this.payment = payment.getPaymentCheck();
        this.user = event.getUserName();
    }

    public String getUser()
    {
        return user;
    }

    public IPayment getPayment()
    {
        return this.payment;
    }
}
