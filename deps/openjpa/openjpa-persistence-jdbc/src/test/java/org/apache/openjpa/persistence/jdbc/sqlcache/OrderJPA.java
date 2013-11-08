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
package org.apache.openjpa.persistence.jdbc.sqlcache;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * A simple entity for a complex test.
 * This entity is used to test complex parameterization and reparametrization of Prepared Queries.
 * 
 * @author Pinaki Poddar
 *
 */
@Entity
public class OrderJPA {
    @Id
    long OrderId;
    int CustomerId;
    int DistrictId;
    int WarehouseId;
    
    
    public long getOrderId() {
        return OrderId;
    }
    public void setOrderId(long orderId) {
        OrderId = orderId;
    }
    public int getCustomerId() {
        return CustomerId;
    }
    public void setCustomerId(int customerId) {
        CustomerId = customerId;
    }
    public int getDistrictId() {
        return DistrictId;
    }
    public void setDistrictId(int districtId) {
        DistrictId = districtId;
    }
    public int getWarehouseId() {
        return WarehouseId;
    }
    public void setWarehouseId(int warehouseId) {
        WarehouseId = warehouseId;
    }
}
