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
package org.apache.openejb.core.ivm.naming;

import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.loader.SystemInstance;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class JndiReference implements Reference {

    private Context context;
    private Hashtable envProperties;
    private String jndiName;
    private String contextJndiName;

    /*
    * This constructor is used when the object to be referenced is accessible through 
    * some other JNDI name space. The context is provided and the lookup name, but the 
    * object is not resolved until it's requested. 
    */
    public JndiReference(javax.naming.Context linkedContext, String jndiName) {
        this.context = linkedContext;
        this.jndiName = jndiName;
    }

    /*
    */
    public JndiReference(String contextJndiName, String jndiName) {
        this.contextJndiName = contextJndiName;
        this.jndiName = jndiName;
    }

    public JndiReference(Hashtable envProperties, String jndiName) {
        if (envProperties == null || envProperties.size() == 0) {
            this.envProperties = null;
        } else {
            this.envProperties = envProperties;
        }
        this.jndiName = jndiName;
    }

    public Object getObject() throws NamingException {
        Context externalContext = getContext();
        synchronized (externalContext) {
            /* According to the JNDI SPI specification multiple threads may not access the same JNDI 
            Context *instance* concurrently. Since we don't know the origines of the federated context we must
            synchonrize access to it.  JNDI SPI Sepecifiation 1.2 Section 2.2
            */
            return externalContext.lookup(jndiName);
        }
    }

    protected Context getContext() throws NamingException {
        if (context == null) {
            if (contextJndiName != null) {
                ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
                context = (Context) containerSystem.getJNDIContext().lookup(contextJndiName);
            } else {
                context = new InitialContext(envProperties);
            }
        }
        return context;
    }
}
