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
package org.apache.openejb;

import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.client.JNDIContext;
import org.apache.openejb.client.RemoteInitialContextFactory;
import org.apache.openejb.client.serializer.EJBDSerializer;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.DeploymentsResolver;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.ServiceDaemon;
import org.apache.openejb.server.ejbd.EjbServer;
import org.junit.Test;

import jakarta.ejb.EJBException;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.Serializable;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class SerializerTest {

    @Test
    public void invoke() throws Exception {
        invokeRemote(new Properties() {{
            setProperty("serializer", MySerializer.class.getName());
        }}, MySerializer.class.getName());
    }

    @Test
    public void ensureItFailBecauseNotSerializeByDefault() throws Exception {
        try {
            invokeRemote(new Properties(), null);
        } catch (final EJBException e) {
            assertThat(e.getCause(), instanceOf(IllegalArgumentException.class));
            assertThat(e.getCause().getMessage(), startsWith("Object is not serializable"));
        }
    }

    private void invokeRemote(final Properties serverProps, final String serializer) throws Exception {
        final EjbServer ejbServer = new EjbServer();

        final Properties initProps = new Properties();
        initProps.put(DeploymentsResolver.DEPLOYMENTS_CLASSPATH_PROPERTY, "false");
        OpenEJB.init(initProps, new ServerFederation());
        ejbServer.init(serverProps);

        final ServiceDaemon serviceDaemon = new ServiceDaemon(ejbServer, 0, "localhost");
        serviceDaemon.start();

        final int port = serviceDaemon.getPort();

        final Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
        final ConfigurationFactory config = new ConfigurationFactory();

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(AnEjbRemote.class));
        assembler.createApplication(config.configureApplication(ejbJar));

        try {

            final Properties props = new Properties();
            props.put(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
            props.put(Context.PROVIDER_URL, "ejbd://127.0.0.1:" + port);
            if (serializer != null) {
                props.put(JNDIContext.SERIALIZER, serializer);
            }
            final Context context = new InitialContext(props);
            final AnInterfaceRemote client = AnInterfaceRemote.class.cast(context.lookup("AnEjbRemoteRemote"));
            assertNotNull(client);

            final OutputNotSerializable out = client.call(new InputNotSerilizable("cloud"));
            assertEquals("cloud", out.name);
        } finally {
            serviceDaemon.stop();
            OpenEJB.destroy();
        }
    }

    @Remote
    public static interface AnInterfaceRemote {

        OutputNotSerializable call(InputNotSerilizable input);
    }

    @Stateless
    public static class AnEjbRemote implements AnInterfaceRemote {

        @Override
        public OutputNotSerializable call(InputNotSerilizable input) {
            return new OutputNotSerializable(input.rename);
        }
    }

    public static class InputNotSerilizable {

        public String rename;

        public InputNotSerilizable(final String cloud) {
            rename = cloud;
        }
    }

    public static class OutputNotSerializable {

        public String name;

        public OutputNotSerializable(final String rename) {
            name = rename;
        }
    }

    public static class MySerializer implements EJBDSerializer {

        @Override
        public Serializable serialize(final Object o) {
            if (InputNotSerilizable.class.isInstance(o)) {
                return InputNotSerilizable.class.cast(o).rename;
            }
            return OutputNotSerializable.class.cast(o).name;
        }

        @Override
        public Object deserialize(final Serializable o, final Class<?> clazz) {
            final String cast = String.class.cast(o);
            if (InputNotSerilizable.class.equals(clazz)) {
                return new InputNotSerilizable(cast);
            }
            return new OutputNotSerializable(cast);
        }
    }
}
