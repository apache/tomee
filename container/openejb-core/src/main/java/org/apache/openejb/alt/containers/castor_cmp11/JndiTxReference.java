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
package org.apache.openejb.alt.containers.castor_cmp11;

import org.apache.openejb.core.ivm.naming.Reference;

import javax.naming.NamingException;
import javax.transaction.TransactionManager;
/*
  This Reference type is used only by the Castor JDO CMP 1.1 container.
  It allows the TransactionManager to be discovered at runtime, which is 
  needed because its not yet available when the container is being constructed
  and the Reference is being bound to the JNDI name space of the deployment.
  See the init( ) method of the CastorCMP11_EntityContainer. 
*/

public class JndiTxReference extends Reference {

    private final TransactionManager transactionManager;

    public JndiTxReference(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public Object getObject() throws NamingException {
        return transactionManager;
    }

}