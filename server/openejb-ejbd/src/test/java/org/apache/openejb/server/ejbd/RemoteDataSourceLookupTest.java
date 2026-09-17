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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.ejbd;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.client.Client;
import org.apache.openejb.client.DataSourceMetaData;
import org.apache.openejb.client.JNDIRequest;
import org.apache.openejb.client.JNDIResponse;
import org.apache.openejb.client.RequestMethodCode;
import org.apache.openejb.client.ResponseCodes;
import org.apache.openejb.client.ServerMetaData;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.ServiceDaemon;
import org.apache.openejb.spi.ContainerSystem;
import org.junit.Test;

import jakarta.resource.Referenceable;
import javax.naming.Context;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RemoteDataSourceLookupTest {
    private static final String OPTION = "openejb.ejbd.datasource-metadata";
    private static final String PASSWORD = "synthetic-datasource-password";

    @Test
    public void disabledByDefault() throws Exception {
        checkLookup(null);
    }

    @Test
    public void explicitlyDisabled() throws Exception {
        checkLookup("false");
    }

    @Test
    public void explicitlyEnabled() throws Exception {
        checkLookup("true");
    }

    private void checkLookup(final String option) throws Exception {
        final Properties properties = new Properties();
        properties.setProperty("openejb.deployments.classpath", "false");
        properties.setProperty("openejb.jdbc.datasource-creator", "dbcp");
        properties.setProperty("LookupDS", "new://Resource?type=DataSource");
        properties.setProperty("LookupDS.JdbcDriver", "org.hsqldb.jdbcDriver");
        properties.setProperty("LookupDS.JdbcUrl", "jdbc:hsqldb:mem:lookup-test");
        properties.setProperty("LookupDS.UserName", "lookup-user");
        properties.setProperty("LookupDS.Password", PASSWORD);
        properties.setProperty("LookupDS.InitialSize", "0");
        properties.setProperty("LookupDS.JtaManaged", "false");
        if (option != null) {
            properties.setProperty(OPTION, option);
        }

        ServiceDaemon daemon = null;
        try {
            OpenEJB.init(properties, new ServerFederation());
            final Context local = SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext();
            final Object managed = local.lookup("openejb/Resource/LookupDS");
            assertTrue(managed instanceof DataSource);
            // Exercise both the server resource fallback and an application-client binding.
            local.bind("openejb/client/lookup-client/comp/env/jdbc", managed);
            final AtomicBoolean referenceRead = new AtomicBoolean();
            final DataSource referenceDataSource = (DataSource) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{DataSource.class, Referenceable.class}, (proxy, method, args) -> {
                    if ("getReference".equals(method.getName())) {
                        referenceRead.set(true);
                        final Reference reference = new Reference(DataSource.class.getName());
                        reference.add(new StringRefAddr("password", PASSWORD));
                        return reference;
                    }
                    if ("toString".equals(method.getName())) {
                        return "ReferenceDataSource";
                    }
                    if ("hashCode".equals(method.getName())) {
                        return System.identityHashCode(proxy);
                    }
                    if ("equals".equals(method.getName())) {
                        return proxy == args[0];
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
            local.bind("openejb/Resource/ReferenceDS", referenceDataSource);

            final EjbServer server = new EjbServer();
            server.init(new Properties());
            daemon = new ServiceDaemon(server, 0, "127.0.0.1");
            daemon.start();
            final ServerMetaData remote = new ServerMetaData(new URI("ejbd://127.0.0.1:" + daemon.getPort()));

            // No authentication request is sent: the server must enforce the option itself.
            final JNDIResponse resource = lookup(remote, "LookupDS", null);
            final JNDIResponse application = lookup(remote, "comp/env/jdbc", "lookup-client");
            final JNDIResponse reference = lookup(remote, "ReferenceDS", null);
            if ("true".equals(option)) {
                for (final JNDIResponse response : new JNDIResponse[]{resource, application}) {
                    assertEquals(ResponseCodes.JNDI_DATA_SOURCE, response.getResponseCode());
                    final DataSourceMetaData metadata = (DataSourceMetaData) response.getResult();
                    assertEquals("lookup-user", metadata.getDefaultUserName());
                    assertEquals(PASSWORD, metadata.getDefaultPassword());
                }
                assertEquals(String.valueOf(reference.getResult()), ResponseCodes.JNDI_REFERENCE, reference.getResponseCode());
                assertEquals(PASSWORD, ((Reference) reference.getResult()).get("password").getContent());
                assertTrue(referenceRead.get());
            } else {
                for (final JNDIResponse response : new JNDIResponse[]{resource, application, reference}) {
                    assertEquals(ResponseCodes.JNDI_NAMING_EXCEPTION, response.getResponseCode());
                }
                assertFalse(referenceRead.get());
            }
            assertEquals(ResponseCodes.JNDI_NOT_FOUND, lookup(remote, "MissingLookupDS", null).getResponseCode());
            assertTrue(local.lookup("openejb/Resource/LookupDS") instanceof DataSource);
        } finally {
            try {
                if (daemon != null) {
                    daemon.stop();
                }
            } finally {
                OpenEJB.destroy();
            }
        }
    }

    private JNDIResponse lookup(final ServerMetaData server, final String name, final String module) throws Exception {
        final JNDIRequest request = new JNDIRequest(RequestMethodCode.JNDI_LOOKUP, name);
        request.setModuleId(module);
        final JNDIResponse response = new JNDIResponse();
        Client.request(request, response, server);
        return response;
    }
}
