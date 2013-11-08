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

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.lib.conf.Configurable;
import org.apache.openjpa.lib.conf.Configuration;

/**
 * Implementation of the {@link ManagedRuntime} interface that uses
 * a static method call to find the TransactionManager.
 *  For example, to configure it to use IBM Websphere's TransactionManager,
 * use the method:<br />
 * <code>com.ibm.ejs.jts.jta.JTSXA.getTransactionManager</code>
 *
 * @author Marc Prud'hommeaux
 */
public class InvocationManagedRuntime extends AbstractManagedRuntime
    implements ManagedRuntime, Configurable {

    private String _methodName = null;
    private String _clazz = null;
    private transient Method _method = null;
    private OpenJPAConfiguration _conf = null;

    /**
     * Return the method to invoke to get the {@link TransactionManager}.
     */
    public String getTransactionManagerMethod() {
        return _methodName;
    }

    /**
     * Set the method to invoke to get the {@link TransactionManager}.
     *  E.g.: com.ibm.ejs.jts.jta.JTSXA.getTransactionManager
     */
    public void setTransactionManagerMethod(String methodName) {
        _clazz = methodName.substring(0, methodName.lastIndexOf('.'));
        _methodName = methodName.substring(methodName.lastIndexOf('.') + 1);
        _method = null;
    }

    public TransactionManager getTransactionManager()
        throws Exception {
        if (_method == null) {
            ClassLoader loader = _conf.getClassResolverInstance().
                getClassLoader(getClass(), null);
            _method = Class.forName(_clazz, true, loader)
                .getMethod(_methodName, null);
        }
        return (TransactionManager) _method.invoke(null, null);
    }

    public void setConfiguration(Configuration conf) {
        _conf = (OpenJPAConfiguration) conf;
    }

    public void startConfiguration() {
    }

    public void endConfiguration() {
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
