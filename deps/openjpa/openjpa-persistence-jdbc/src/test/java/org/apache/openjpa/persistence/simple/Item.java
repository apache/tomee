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
package org.apache.openjpa.persistence.simple;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "I_ITEM")
public class Item implements Serializable {

    private static final long serialVersionUID = 489786296539819572L;
    
    public int itemId;
    public String itemName;
    public java.math.BigDecimal itemPrice;
    public String itemData;

    @Column(name = "I_DATA", table = "I_ITEM")
    public String getItemData() {
        return itemData;
    }

    public void setItemData(String itemData) {
        this.itemData = itemData;
    }

    @Id
    @Column(name = "I_ID", table = "I_ITEM")
    @GeneratedValue(strategy = GenerationType.AUTO)
    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    @Column(name = "I_NAME", table = "I_ITEM")
    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    @Basic
    @Column(name = "I_PRICE", table = "I_ITEM")
    public java.math.BigDecimal getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(java.math.BigDecimal itemPrice) {
        this.itemPrice = itemPrice;
    }

}
