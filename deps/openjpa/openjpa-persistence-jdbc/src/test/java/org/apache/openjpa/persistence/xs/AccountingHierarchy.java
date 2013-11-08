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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "PM_ACCOUNTING_HIERARCHY_TEST")
public class AccountingHierarchy implements Serializable {

    private static final long serialVersionUID = -1759978020595211326L;

    private String code;
    private String shortDesc;

    private List<AccountingHierarchyRate> accRateList = new ArrayList<AccountingHierarchyRate>(0);

    private Long version;

    public AccountingHierarchy() {
    }

    public AccountingHierarchy(String code) {
        this.code = code;
    }

    public AccountingHierarchy(String code, String shortDesc) {
        this.code = code;
        this.shortDesc = shortDesc;
    }

    public AccountingHierarchy(String code, String shortDesc, String hierarchyType) {
        this.code = code;
        this.shortDesc = shortDesc;
    }

    @Id
    @Column(name = "code", length = 20)
    public String getCode() {
        return code;
    }

    @Column(name = "short_desc", nullable = false, length = 50)
    public String getShortDesc() {
        return shortDesc;
    }

    @OneToMany(mappedBy = "accountingHierarchy", fetch = FetchType.EAGER, 
        targetEntity = AccountingHierarchyRate.class, cascade = CascadeType.ALL, orphanRemoval = true)
    public List<AccountingHierarchyRate> getAccRateList() {
        return accRateList;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setShortDesc(String shortDesc) {
        this.shortDesc = shortDesc;
    }

    public void setAccRateList(List<AccountingHierarchyRate> accRateList) {
        this.accRateList = accRateList;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getCode() == null) ? 0 : getCode().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof AccountingHierarchy))
            return false;
        AccountingHierarchy other = (AccountingHierarchy) obj;
        if (getCode() == null) {
            if (other.getCode() != null)
                return false;
        } else if (!getCode().equals(other.getCode()))
            return false;
        return true;
    }

    @Transient
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "AccountingHierarchy [getCode()=" + getCode() + ", getShortDesc()=" + getShortDesc() + "]";
    }

}
