/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.server.cxf;

import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.config.sys.Service;
import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.jee.oejb3.PojoDeployment;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testing.RandomPort;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.jws.WebService;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import jakarta.xml.ws.soap.SOAPFaultException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import static jakarta.xml.bind.annotation.XmlAccessType.FIELD;
import static org.junit.Assert.assertEquals;

@EnableServices("jaxws")
@RunWith(ApplicationComposer.class)
public class MaxChildTest {
    @RandomPort("http")
    private URL root;

    @Test
    public void passing() throws MalformedURLException {
        assertEquals(0,
                jakarta.xml.ws.Service.create(new URL(root.toExternalForm() + "app/ws?wsdl"), new QName("http://cxf.server.openejb.apache.org/", "SimpleContractImplService"))
                        .getPort(SimpleContract.class)
                        .test(new Root())
                        .getChildren().size());
    }

    @Test
    public void tooBig() throws MalformedURLException {
        try {
            final Root root = new Root();
            for (int i = 0; i < 2; i++) {
                root.getChildren().add(new Child());
            }
            jakarta.xml.ws.Service.create(new URL(this.root.toExternalForm() + "app/ws?wsdl"), new QName("http://cxf.server.openejb.apache.org/", "SimpleContractImplService"))
                    .getPort(SimpleContract.class)
                    .test(root);
        } catch (final SOAPFaultException e) {
            assertEquals("Unmarshalling Error: Maximum Number of Child Elements limit (1) Exceeded ", e.getMessage());
        }
    }

    @Module
    public AppModule app() {
        final String jarLocation = "target/" + getClass().getSimpleName();
        return new AppModule(Thread.currentThread().getContextClassLoader(), jarLocation, new Application(), true) {{
            getEjbModules().add(new EjbModule(new EjbJar("app"), new OpenejbJar() {{
                getPojoDeployment().add(new PojoDeployment() {{
                    setClassName(SimpleContractImpl.class.getName());
                    getProperties().setProperty("cxf.jaxws.properties", "cxfLargeMsgSize");
                }});
            }}));
            getWebModules().add(new WebModule(
                    new WebApp().contextRoot("app").addServlet("ws", SimpleContractImpl.class.getName(), "/ws"),
                    "app",
                    Thread.currentThread().getContextClassLoader(),
                    jarLocation, "app"
            ));
            getServices().add(new Service() {{
                setId("cxfLargeMsgSize");
                setClassName(Properties.class.getName());
                getProperties().setProperty("org.apache.cxf.stax.maxChildElements", "1");
            }});
        }};
    }

    @WebService
    public interface SimpleContract {
        Root test(Root root);
    }

    @WebService
    public static class SimpleContractImpl implements SimpleContract {
        @Override
        public Root test(final Root root) {
            return root;
        }
    }

    @XmlRootElement
    @XmlAccessorType(FIELD)
    public static class Root {
        @XmlElement
        private Collection<Child> children;

        public Collection<Child> getChildren() {
            return children == null ? (children = new ArrayList<>()) : children;
        }
    }

    @XmlAccessorType(FIELD)
    public static class Child {
        @XmlElement
        private String name;

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }
    }
}
