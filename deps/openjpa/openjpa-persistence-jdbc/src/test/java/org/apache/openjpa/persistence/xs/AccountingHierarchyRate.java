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
package org.apache.openjpa.persistence.xs;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "PM_ACCOUNTING_HIERARCHY_RATE_TEST")
@IdClass(AccountingHierarchyRateOpenJPAKey.class)
public class AccountingHierarchyRate implements Serializable {

	private static final long serialVersionUID = 538926265319989492L;

	private String id;
	private AccountingHierarchy accountingHierarchy;

	private BigDecimal currentRate;
	private BigDecimal budgetRate;

	public AccountingHierarchyRate() {
	}

	public AccountingHierarchyRate(String id, AccountingHierarchy accountingHierarchy, 
	        BigDecimal currentRate, BigDecimal budgetRate) {
		this.id = id;
		this.accountingHierarchy = accountingHierarchy;
		this.currentRate = currentRate;
		this.budgetRate = budgetRate;
	}

	@Id
	@Column(name = "id", length = 20, nullable = false)
	public String getId() {
		return id;
	}

	@Id
	@ManyToOne(targetEntity = AccountingHierarchy.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "acc_hier", nullable = false)
	public AccountingHierarchy getAccountingHierarchy() {
		return accountingHierarchy;
	}

	@Column(name = "current_rate", nullable = true, precision = 12, scale = 4)
	public BigDecimal getCurrentRate() {
		return currentRate;
	}

	@Column(name = "budget_rate", nullable = true, precision = 12, scale = 4)
	public BigDecimal getBudgetRate() {
		return budgetRate;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setAccountingHierarchy(AccountingHierarchy accountingHierarchy) {
		this.accountingHierarchy = accountingHierarchy;
	}

	public void setCurrentRate(BigDecimal currentRate) {
		this.currentRate = currentRate;
	}

	public void setBudgetRate(BigDecimal budgetRate) {
		this.budgetRate = budgetRate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		result = prime * result + ((getAccountingHierarchy() == null) ? 0 : 
		    getAccountingHierarchy().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof AccountingHierarchyRate))
			return false;
		AccountingHierarchyRate other = (AccountingHierarchyRate) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		if (getAccountingHierarchy() == null) {
			if (other.getAccountingHierarchy() != null)
				return false;
		} else if (!getAccountingHierarchy().equals(other.getAccountingHierarchy()))
			return false;
		return true;
	}
}
