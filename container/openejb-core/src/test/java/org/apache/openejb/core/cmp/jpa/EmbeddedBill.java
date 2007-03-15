/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core.cmp.jpa;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class EmbeddedBill {
    private static final long serialVersionUID = 1L;

    private EmbeddedBillPk pk;
    private String billDescription;

    @EmbeddedId
    public EmbeddedBillPk getPk() {
        return pk;
    }

    public void setPk(EmbeddedBillPk key) {
        pk = key;
    }

    public String getBillDescription() {
        return billDescription;
    }

    public void setBillDescription(String description) {
        billDescription = description;
    }
}
