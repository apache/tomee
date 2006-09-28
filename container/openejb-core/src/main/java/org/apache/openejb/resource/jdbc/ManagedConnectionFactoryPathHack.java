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
package org.apache.openejb.resource.jdbc;

import org.apache.openejb.loader.SystemInstance;

import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.ResourceException;
import javax.security.auth.Subject;
import java.util.Properties;
import java.io.File;

public class ManagedConnectionFactoryPathHack extends ManagedConnectionFactoryAdapter {

    public ManagedConnectionFactoryPathHack(ManagedConnectionFactory factory) {
        super(factory);
    }

    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        Properties systemProperties = System.getProperties();
        synchronized (systemProperties) {
            String userDir = systemProperties.getProperty("user.dir");
            try {
                File base = SystemInstance.get().getBase().getDirectory();
                systemProperties.setProperty("user.dir", base.getAbsolutePath());
                return super.createManagedConnection(subject, connectionRequestInfo);
            } finally {
                systemProperties.setProperty("user.dir", userDir);
            }
        }
    }
}
