/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.jee.ejbjar;

import java.util.List;
import java.util.ArrayList;

/**
 * @version $Revision$ $Date$
 */
public class ContainerTransaction {
    private String id;
    private List<String> description = new ArrayList<String>();
    private Method method;
    private TransactionAttributeType transactionAttribute;

    public ContainerTransaction() {
    }

    public ContainerTransaction(Method method, TransactionAttributeType transactionAttribute) {
        this.method = method;
        this.transactionAttribute = transactionAttribute;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public TransactionAttributeType getTransactionAttribute() {
        return transactionAttribute;
    }

    public void setTransactionAttribute(TransactionAttributeType transactionAttribute) {
        this.transactionAttribute = transactionAttribute;
    }
}
