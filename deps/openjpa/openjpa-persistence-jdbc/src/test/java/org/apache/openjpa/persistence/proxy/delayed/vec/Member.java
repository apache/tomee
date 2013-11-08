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
package org.apache.openjpa.persistence.proxy.delayed.vec;

import java.util.Collection;
import java.util.Vector;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import org.apache.openjpa.persistence.proxy.delayed.IAccount;
import org.apache.openjpa.persistence.proxy.delayed.IMember;

@Embeddable
public class Member implements IMember {

    private String name;
    
    @OneToMany(fetch=FetchType.LAZY, mappedBy="userIdent", targetEntity=Account.class)
    private Vector<IAccount> accounts;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setAccounts(Collection<IAccount> accounts) {
        this.accounts = (Vector<IAccount>)accounts;
    }

    public Collection<IAccount> getAccounts() {
        return accounts;
    }
}
