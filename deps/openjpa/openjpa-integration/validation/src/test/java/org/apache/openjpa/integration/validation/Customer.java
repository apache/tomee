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
package org.apache.openjpa.integration.validation;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

@Entity(name="VCustomer")
public class Customer extends Person implements ICustomer, Serializable {
    @Transient
    private static final long serialVersionUID = 1L;

    @OneToOne(fetch=FetchType.LAZY)
    private Address shippingAddress;

    @OneToOne(fetch=FetchType.LAZY)
    private Address billingAddress;


    public void setShippingAddress(IAddress shippingAddress) {
        this.shippingAddress = (Address) shippingAddress;
    }

    public IAddress getShippingAddress() {
        return this.shippingAddress;
    }


    public void setBillingAddress(IAddress billingAddress) {
        this.billingAddress = (Address) billingAddress;
    }

    public IAddress getBillingAddress() {
        return this.billingAddress;
    }
}
