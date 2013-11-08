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

package org.apache.openjpa.persistence.criteria.results;

import java.math.BigDecimal;

import javax.persistence.*;

@Entity
@SqlResultSetMapping(name="selectShipRateMapping", 
    entities=@EntityResult(entityClass=org.apache.openjpa.persistence.criteria.results.ShipRate.class,
                           fields = {@FieldResult(name="shipRateId", column = "id"),  
                            @FieldResult(name="billedAsWeight", column = "RBLWGT")}) )
//Try to create a result set with different column name 
//than the attribute name defined in the result entity
@NamedNativeQuery(name = "selectShipRateQuery", 
query = "SELECT shipRateId as id, billedAsWeight as RBLWGT from ShipRate", 
resultSetMapping="selectShipRateMapping")
public class ShipRate {    
    @Id
    long shipRateId;
    
    public ShipRate(long shipRateId, BigDecimal billedAsWeight) {
        super();
        this.shipRateId = shipRateId;
        this.billedAsWeight = billedAsWeight;
    }

    private BigDecimal billedAsWeight;
}
