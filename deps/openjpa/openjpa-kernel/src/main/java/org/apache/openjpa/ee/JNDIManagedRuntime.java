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
package org.apache.openjpa.ee;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.transaction.TransactionManager;

/**
 * Implementation of the {@link ManagedRuntime} interface that uses JNDI to
 * find the TransactionManager.
 *
 * @author Abe White
 */
public class JNDIManagedRuntime extends AbstractManagedRuntime
    implements ManagedRuntime {

    private String _tmLoc = "java:/TransactionManager";
    private TransactionManager _tm = null;

    /**
     * Return the location of the {@link TransactionManager} in JNDI.
     */
    public String getTransactionManagerName() {
        return _tmLoc;
    }

    /**
     * Set the location of the {@link TransactionManager} in JNDI.
     */
    public void setTransactionManagerName(String name) {
        _tmLoc = name;
        _tm = null;  // reset the cached TM
    }

    /**
     * Return the cached TransactionManager instance.
     */
    public TransactionManager getTransactionManager() throws Exception {

        if (_tm == null) {
            Context ctx = new InitialContext();
            try {
                _tm = (TransactionManager) ctx.lookup(_tmLoc);
            } finally {
                ctx.close();
            }
        }
        return _tm;
    }

    public void setRollbackOnly(Throwable cause)
        throws Exception {
        // there is no generic support for setting the rollback cause
        getTransactionManager().getTransaction().setRollbackOnly();
    }

    public Throwable getRollbackCause()
        throws Exception {
        // there is no generic support for setting the rollback cause
        return null;
    }
}
