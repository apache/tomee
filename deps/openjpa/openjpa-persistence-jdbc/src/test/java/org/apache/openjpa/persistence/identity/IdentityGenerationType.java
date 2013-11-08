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
package org.apache.openjpa.persistence.identity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Using a class type (Long) instead of a primitive type (long)
 * used to cause a problem with a GenerationType of IDENTITY.
 * This was resolved via revision 453016.  We can use this testcase
 * for regression purposes.
 *
 * @author Kevin Sutter
 */
@Entity
public class IdentityGenerationType {

    private Long orderId;
    private String someData;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long getOrderId() {
        return orderId;
    }
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    public String getSomeData() {
        return someData;
    }
    public void setSomeData(String someData) {
        this.someData = someData;
    }
}

