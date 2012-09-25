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
package org.apache.webbeans.test.component;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.apache.webbeans.test.annotation.binding.Check;

@RequestScoped
public class PostConstructComponent
{
    private @Inject @Check(type = "CHECK") IPayment payment;

    @SuppressWarnings("unused")
    private IPayment p = null;

    @PostConstruct
    public void init()
    {
        this.p = payment;

    }

    public IPayment getP()
    {
        return payment;
    }

    /**
     * @return the payment
     */
    public IPayment getPayment()
    {
        return payment;
    }

    /**
     * @param payment the payment to set
     */
    public void setPayment(IPayment payment)
    {
        this.payment = payment;
    }

    /**
     * @param p the p to set
     */
    public void setP(IPayment p)
    {
        this.p = p;
    }

}
