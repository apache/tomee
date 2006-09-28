/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.resource.jdbc;

import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.ResourceException;
import javax.security.auth.Subject;
import java.util.Set;
import java.io.PrintWriter;
import java.io.Serializable;

/**
 * @version $Revision$ $Date$
 */
public class ManagedConnectionFactoryAdapter implements javax.resource.spi.ManagedConnectionFactory, Serializable {

    private final ManagedConnectionFactory factory;

    public ManagedConnectionFactoryAdapter(ManagedConnectionFactory factory) {
        this.factory = factory;
    }

    public Object createConnectionFactory(ConnectionManager connectionManager) throws ResourceException {
        return factory.createConnectionFactory(connectionManager);
    }

    public Object createConnectionFactory() throws ResourceException {
        return factory.createConnectionFactory();
    }

    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        return factory.createManagedConnection(subject, connectionRequestInfo);
    }

    public ManagedConnection matchManagedConnections(Set set, Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        return factory.matchManagedConnections(set, subject, connectionRequestInfo);
    }

    public void setLogWriter(PrintWriter printWriter) throws ResourceException {
        factory.setLogWriter(printWriter);
    }

    public PrintWriter getLogWriter() throws ResourceException {
        return factory.getLogWriter();
    }

    public int hashCode() {
        return factory.hashCode();
    }

    public boolean equals(Object o) {
        return factory.equals(o);
    }
}
