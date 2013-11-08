/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.persistence.identity.entityasidentity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="EAIAccount")
@IdClass(AccountId.class)
public class Account {
    @Id
    @Column(name="account_id")
    private Integer accountId;
    
    @Id
    @Column(name="group_id")
    private Integer groupId;
    
    @Id
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="accountHolder_id")
    private Person accountHolder;
    
    private long balanceInDollars;
    private int  balanceInCents;
    private boolean accountLocked;
    
    public Account() {
        
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Person getAccountHolder() {
        return accountHolder;
    }

    public void setAccountHolder(Person accountHolder) {
        this.accountHolder = accountHolder;
    }

    public long getBalanceInDollars() {
        return balanceInDollars;
    }

    public void setBalanceInDollars(long balanceInDollars) {
        this.balanceInDollars = balanceInDollars;
    }

    public int getBalanceInCents() {
        return balanceInCents;
    }

    public void setBalanceInCents(int balanceInCents) {
        this.balanceInCents = balanceInCents;
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }
    
    
}
