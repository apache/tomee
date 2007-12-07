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

/**
 * This example demostrates the use of the injection of environment entries
 * using <b>Resource</b> annotation.
 * 
 * "EJB Core Contracts and Requirements" specification section 16.4.1.1.
 * 
 * @version $Rev$ $Date$
 */

@Remote
public class InvoiceBean implements Invoice {

    int maxLineItems;

    private List<LineItem> items = new ArrayList<LineItem>();

    private int itemCount;

    /**
     * Injects the <b>maxLineItems</b> simple environment entry through bean
     * method.
     * 
     * The JavaBeans property name (not the method name) is used as the default
     * JNDI name. By default, the JavaBeans propery name is combined with the
     * name of the class in which the annotation is used and is used directly as
     * the name in the bean's naming context. JNDI name for this entry would
     * be
     * java:comp/env/org.apache.openejb.examples.resource.InvoiceBean/maxLineItems
     * 
     * Refer "EJB Core Contracts and Requirements" specification section 16.2.2.
     * 
     * @param maxLineItems
     */
    @Resource
    public void setMaxLineItems(int maxLineItems) {
        this.maxLineItems = maxLineItems;
    }

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
