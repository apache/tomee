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
package org.apache.openejb.arquillian.tests.jaxws.config;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
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

@RunWith(Arquillian.class)
public class MaxChildTest {
    @ArquillianResource
    private URL root;

    @Deployment(testable = false)
    public static WebArchive app() {
        return ShrinkWrap.create(WebArchive.class, "app.war")
                .addClasses(MaxChildTest.class, SimpleContract.class, SimpleContractImpl.class, Root.class, Child.class)
                .setWebXML(new StringAsset("<web-app xmlns=\"https://jakarta.ee/xml/ns/jakartaee\" version=\"6.0\">" +
                        "<servlet><servlet-name>ws</servlet-name><servlet-class>" + SimpleContractImpl.class.getName() + "</servlet-class></servlet>" +
                        "<servlet-mapping><servlet-name>ws</servlet-name><url-pattern>/ws</url-pattern></servlet-mapping>" +
                        "</web-app>"))
                .addAsWebInfResource(new StringAsset(
                        "<openejb-jar>\n" +
                        "  <pojo-deployment class-name=\"" + SimpleContractImpl.class.getName() + "\">\n" +
                        "    <properties>\n" +
                        "      cxf.jaxws.properties = cxfLargeMsgSize\n" +
                        "    </properties>\n" +
                        "  </pojo-deployment>\n" +
                        "</openejb-jar>\n"), "openejb-jar.xml")
                .addAsWebInfResource(new StringAsset(
                        "<resources>\n" +
                        "  <Service id=\"cxfLargeMsgSize\" class-name=\"" + Properties.class.getName() + "\">\n" +
                        "    org.apache.cxf.stax.maxChildElements = 1\n" +
                        "  </Service>\n" +
                        "</resources>\n"), "resources.xml");
    }

    @Test
    public void passing() throws MalformedURLException {
        assertEquals(0,
                jakarta.xml.ws.Service.create(new URL(root.toExternalForm() + "ws?wsdl"), new QName("http://config.jaxws.tests.arquillian.openejb.apache.org/", "SimpleContractImplService"))
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
            jakarta.xml.ws.Service.create(new URL(this.root.toExternalForm() + "ws?wsdl"), new QName("http://config.jaxws.tests.arquillian.openejb.apache.org/", "SimpleContractImplService"))
                    .getPort(SimpleContract.class)
                    .test(root);
        } catch (final SOAPFaultException e) {
            assertEquals("Unmarshalling Error: Maximum Number of Child Elements limit (1) Exceeded ", e.getMessage());
        }
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
