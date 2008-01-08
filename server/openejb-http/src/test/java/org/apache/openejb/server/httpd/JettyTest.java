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
package org.apache.openejb.server.httpd;

import java.util.Properties;

import junit.framework.TestCase;
import org.apache.openejb.core.CoreContainerSystem;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;

public class JettyTest extends TestCase {
//    public void testJettyImpl() throws Exception {
//        SystemInstance.get().setComponent(ContainerSystem.class, new CoreContainerSystem());
//
//        Properties props = new Properties();
//        props.setProperty("impl", "Jetty");
//
//        HttpEjbServer server = new HttpEjbServer();
//        server.init(props);
//
//        assertTrue("SystemInstance.get().getComponent(HttpServer.class) should be an instance of JettyHttpServer",
//                SystemInstance.get().getComponent(HttpServer.class) instanceof JettyHttpServer);
//    }

    public void testOpenEJBImpl() throws Exception {
        SystemInstance.get().setComponent(ContainerSystem.class, new CoreContainerSystem());

        Properties props = new Properties();
        props.setProperty("impl", "openejb");

        HttpEjbServer server = new HttpEjbServer();
        server.init(props);

        assertTrue("SystemInstance.get().getComponent(HttpServer.class) should be an instance of OpenEJBHttpServer",
                SystemInstance.get().getComponent(HttpServer.class) instanceof OpenEJBHttpServer);
    }

//    public void testJettyClassCheck() throws Exception {
//        SystemInstance.get().setComponent(ContainerSystem.class, new CoreContainerSystem());
//
//        Properties props = new Properties();
//
//        HttpEjbServer server = new HttpEjbServer();
//        server.init(props);
//
//        assertTrue("SystemInstance.get().getComponent(HttpServer.class) should be an instance of JettyHttpServer",
//                SystemInstance.get().getComponent(HttpServer.class) instanceof JettyHttpServer);
//    }

//    public void testUnknownImpl() throws Exception {
//        SystemInstance.get().setComponent(ContainerSystem.class, new CoreContainerSystem());
//
//        Properties props = new Properties();
//        props.setProperty("impl", "fake");
//
//        try {
//            HttpEjbServer server = new HttpEjbServer();
//            server.init(props);
//            fail("expected IllegalArgumentException");
//        } catch (IllegalArgumentException expected) {
//        }
//    }
}
