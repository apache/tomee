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
package org.apache.openjpa.persistence.criteria;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="CR_CUST")
public class Customer {
    @Id
    @GeneratedValue
	private long id;
    
	private String firstName;
	private String lastName;
	private String name;
	
	@OneToMany(mappedBy="customer")
	private Set<Order> orders = new HashSet<Order>(); 
	
	private int status;
	private int balanceOwed;
	
	@OneToOne
	private Address address;
	
	private int filledOrderCount;
	
    private long accountNum;
	
	@OneToMany
	private List<Account> accounts = new ArrayList<Account>();
	
    @Enumerated
    @Basic
    private CreditRating creditRating;

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Set<Order> getOrders() {
        return orders;
    }
    
    public void setOrders(Set<Order> orders) {
        this.orders = orders;
    }

    public long getId() {
        return id;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    public long getAccountNum() {
        return accountNum;
    }
    
    public void setAccountNum(long accountNum) {
        this.accountNum = accountNum;
    }

    public int getBalanceOwed() {
        return balanceOwed;
    }
    
    public void setBalanceOwed(int balanceOwed) {
        this.balanceOwed = balanceOwed;
    }
	
    public int getFilledOrderCount() {
        return filledOrderCount;
    }
    
    public void setFilledOrderCount(int filledOrderCount) {
        this.filledOrderCount = filledOrderCount;
    }
    
    public List<Account> getAccounts() {
        return accounts;
    }
    
    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public CreditRating getRating() {
        return creditRating;
    }
    
    public void setRating(CreditRating rating) {
        this.creditRating = rating;
    }
    
    public enum CreditRating { POOR, GOOD, EXCELLENT };
}
