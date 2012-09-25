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
package org.apache.webbeans.test.component.decorator.clean;

import java.math.BigDecimal;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Default;


@RequestScoped
@Default
public class AccountComponent implements Account
{
    private BigDecimal amount;
    private BigDecimal balance;

    public void deposit(BigDecimal amount)
    {

    }

    public BigDecimal getBalance()
    {
        return this.balance;
    }

    public void withdraw(BigDecimal amount)
    {

    }

    /**
     * @return the amount
     */
    public BigDecimal getAmount()
    {
        return amount;
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    /**
     * @param balance the balance to set
     */
    public void setBalance(BigDecimal balance)
    {
        this.balance = balance;
    }

}
