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

import java.lang.reflect.Method;
import javax.transaction.TransactionManager;

/**
 * {@link ManagedRuntime} implementation for SunONE.
 *
 * @author Marc Prud'hommeaux
 */
public class SunOneManagedRuntime extends AbstractManagedRuntime
    implements ManagedRuntime {

    private Method _switchMeth;
    private Method _txManagerMeth;

    public SunOneManagedRuntime()
        throws ClassNotFoundException, NoSuchMethodException {
        Class swtch = Class.forName("com.sun.enterprise.Switch");
        _switchMeth = swtch.getMethod("getSwitch", (Class[]) null);
        _txManagerMeth = swtch.getMethod("getTransactionManager",
            (Class[]) null);
    }

    public TransactionManager getTransactionManager()
        throws Exception {
        // return Switch.getSwitch ().getTransactionManager ();
        Object sw = _switchMeth.invoke(null, (Object[]) null);
        return (TransactionManager) _txManagerMeth.invoke(sw, (Object[]) null);
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
