/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.data.test.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class Product {

    @Id
    private String productNum;

    private String name;

    private Double price;

    @Version
    private long versionNum;

    public Product() {
    }

    public Product(final String productNum, final String name, final Double price) {
        this.productNum = productNum;
        this.name = name;
        this.price = price;
    }

    public static Product of(final String name, final Double price, final String productNum) {
        return new Product(productNum, name, price);
    }

    public String getProductNum() {
        return productNum;
    }

    public void setProductNum(final String productNum) {
        this.productNum = productNum;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(final Double price) {
        this.price = price;
    }

    public long getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(final long versionNum) {
        this.versionNum = versionNum;
    }

    @Override
    public String toString() {
        return "Product[" + productNum + ", " + name + ", " + price + ", v" + versionNum + "]";
    }
}
