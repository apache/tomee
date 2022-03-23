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

package org.apache.openejb.core;

import org.apache.openejb.core.ivm.naming.Reference;

import javax.naming.NamingException;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ManagedConnectionFactory;
/*
  This reference object is used for wrappering ManagedConnectionFactory objects that
  manufacture resource specific connection factories. When the getObject( ) method is 
  invoked the factory is created and passed back as the return value.

  In addition, dynamic resolution and special conditions can be encapsulated
  in the implementation object.

*/

/**
 * @org.apache.xbean.XBean element="connectorRef"
 */
public class ConnectorReference extends Reference {
    private final ConnectionManager conMngr;
    private final ManagedConnectionFactory mngedConFactory;

    public ConnectorReference(final ConnectionManager manager, final ManagedConnectionFactory factory) {
        conMngr = manager;
        mngedConFactory = factory;
    }

    public Object getObject() throws NamingException {
        try {
            final Object connection = mngedConFactory.createConnectionFactory(conMngr);
            return connection;
        } catch (final ResourceException re) {
            throw (NamingException) new NamingException("Could not create ConnectionFactory from " + mngedConFactory.getClass()).initCause(re);
        }
    }

    public ConnectionManager getConnectionManager() {
        return conMngr;
    }
}