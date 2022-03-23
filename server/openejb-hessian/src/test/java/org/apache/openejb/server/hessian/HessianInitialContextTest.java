/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.server.hessian;

import org.apache.openejb.client.hessian.HessianInitialContextFactory;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.Remote;
import jakarta.ejb.Singleton;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@EnableServices({"hessian", "httpejbd"})
@RunWith(ApplicationComposer.class)
public class HessianInitialContextTest {

    private static int port = -1;

    @BeforeClass
    public static void beforeClass() {
        port = NetworkUtil.getNextAvailablePort();
    }

    @Configuration
    public Properties props() {
        return new PropertiesBuilder().p("httpejbd.port", Integer.toString(port)).build();
    }

    @Module
    public Class<?>[] classes() {
        return new Class<?>[]{Server.class};
    }

    @Test
    public void client() throws Exception {
        final MyApi client = MyApi.class.cast(
            new InitialContext(new PropertiesBuilder()
                .p(Context.INITIAL_CONTEXT_FACTORY, HessianInitialContextFactory.class.getName())
                .p(Context.PROVIDER_URL, "http://127.0.0.1:" + port + "/HessianInitialContextTest/hessian/")
                .build())
                .lookup("Server"));

        final Out out = client.call(new In("test"));
        assertThat(out, instanceOf(Out.class));
        assertEquals("test", out.value);
    }

    @Remote
    public static interface MyApi {
        Out call(In in);
    }

    @Singleton
    public static class Server implements MyApi {
        @Override
        public Out call(final In in) {
            return new Out(in.value);
        }
    }

    public static class In {
        private String value;

        public In(final String value) {
            this.value = value;
        }
    }

    public static class Out {
        private String value;

        public Out(final String value) {
            this.value = value;
        }
    }
}
