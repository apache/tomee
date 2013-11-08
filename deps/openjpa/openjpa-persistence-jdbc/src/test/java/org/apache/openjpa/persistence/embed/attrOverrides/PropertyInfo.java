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
package org.apache.openjpa.persistence.embed.attrOverrides;

import java.math.BigDecimal;
import javax.persistence.*;

@Embeddable 
public class PropertyInfo {
    protected Integer parcelNumber;
    protected Integer size;
    protected BigDecimal tax;
    
    public Integer getParcelNumber() {
    	return parcelNumber;
    }
    
    public void setParcelNumber(Integer p) {
    	parcelNumber = p;
    }
    
    public Integer getSize() {
    	return size;
    }
    
    public void setSize(Integer size) {
    	this.size = size;
    }
    
    public BigDecimal getTax() {
    	return tax;
    }
    
    public void setTax(BigDecimal tax) {
    	this.tax = tax;
    }
}
