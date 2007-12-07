/**
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
package org.apache.openejb.examples.injection;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.Stateful;

/**
 * This example demostrates the use of the injection of environment entries
 * using <b>Resource</b> annotation.
 * 
 * "EJB Core Contracts and Requirements" specification section 16.4.1.1.
 * 
 * Resource annotation is used to annotate the maxLineItems and default value of
 * 10 is assigned. Deployer can modify the values of the environment entries at
 * deploy time in deployment descriptor.
 * 
 * @version $Rev$ $Date$
 */

@Stateful
@Remote
public class PurchaseOrderBean implements PurchaseOrder {

    @Resource
    int maxLineItems = 10;

    private List<LineItem> items = new ArrayList<LineItem>();

    private int itemCount;

    public void addLineItem(LineItem item) throws TooManyItemsException {
        if (item == null) {
            throw new IllegalArgumentException("Line item must not be null");
        }

        if (itemCount <= maxLineItems) {
            items.add(item);
            itemCount++;
        } else {
            throw new TooManyItemsException("Number of items exceeded the maximum limit");
        }
    }

    public int getMaxLineItems() {
        return this.maxLineItems;
    }

}
