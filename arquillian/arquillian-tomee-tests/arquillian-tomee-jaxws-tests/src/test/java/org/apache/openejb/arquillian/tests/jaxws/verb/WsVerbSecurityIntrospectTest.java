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
package org.apache.openejb.arquillian.tests.jaxws.verb;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Service;
import org.apache.catalina.Valve;
import org.apache.catalina.core.StandardServer;
import org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.apache.tomee.loader.TomcatHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

@RunWith(Arquillian.class)
public class WsVerbSecurityIntrospectTest {

    @Deployment
    public static WebArchive war() {
        final String ejbJar =
                "<ejb-jar xmlns=\"http://java.sun.com/xml/ns/javaee\" version=\"3.1\" metadata-complete=\"false\">\n" +
                "  <enterprise-beans>\n" +
                "    <session>\n" +
                "      <ejb-name>GreeterBean</ejb-name>\n" +
                "      <service-endpoint>org.apache.openejb.arquillian.tests.jaxws.verb.GreeterWs</service-endpoint>\n" +
                "      <ejb-class>org.apache.openejb.arquillian.tests.jaxws.verb.GreeterBean</ejb-class>\n" +
                "      <session-type>Singleton</session-type>\n" +
                "      <transaction-type>Container</transaction-type>\n" +
                "    </session>\n" +
                "  </enterprise-beans>\n" +
                "</ejb-jar>";

        final String openejbJar =
                "<openejb-jar xmlns=\"http://www.openejb.org/xml/ns/openejb-jar-2.1\">\n" +
                "  <enterprise-beans>\n" +
                "    <session>\n" +
                "      <ejb-name>GreeterBean</ejb-name>\n" +
                "      <web-service-address>/ws/Greeter</web-service-address>\n" +
                "      <web-service-security>\n" +
                "        <security-realm-name/>\n" +
                "        <transport-guarantee>NONE</transport-guarantee>\n" +
                "        <auth-method>BASIC</auth-method>\n" +
                "      </web-service-security>\n" +
                "    </session>\n" +
                "  </enterprise-beans>\n" +
                "</openejb-jar>";

        final String webservices =
                "<webservices xmlns=\"http://java.sun.com/xml/ns/j2ee\" version=\"1.1\">\n" +
                "  <webservice-description>\n" +
                "    <webservice-description-name>GreeterService</webservice-description-name>\n" +
                "    <port-component>\n" +
                "      <port-component-name>GreeterPort</port-component-name>\n" +
                "      <wsdl-port>GreeterPort</wsdl-port>\n" +
                "      <service-endpoint-interface>org.apache.openejb.arquillian.tests.jaxws.verb.GreeterWs</service-endpoint-interface>\n" +
                "      <service-impl-bean><ejb-link>GreeterBean</ejb-link></service-impl-bean>\n" +
                "    </port-component>\n" +
                "  </webservice-description>\n" +
                "</webservices>";

        return ShrinkWrap.create(WebArchive.class, "WsVerbSecurityWar.war")
                .addClasses(GreeterWs.class, GreeterBean.class)
                .addAsWebInfResource(new StringAsset(ejbJar), "ejb-jar.xml")
                .addAsWebInfResource(new StringAsset(openejbJar), "openejb-jar.xml")
                .addAsWebInfResource(new StringAsset(webservices), "webservices.xml");
    }

    @Test
    public void dumpSecurity() {
        final StandardServer server = TomcatHelper.getServer();
        for (final Service service : server.findServices()) {
            if (!(service.getContainer() instanceof Engine)) {
                continue;
            }
            final Engine engine = (Engine) service.getContainer();
            final Container host = engine.findChild(engine.getDefaultHost());
            for (final Container child : host.findChildren()) {
                if (!(child instanceof Context) || !child.getName().contains("WsVerbSecurity")) {
                    continue;
                }
                final Context context = (Context) child;
                System.out.println(">>> CONTEXT " + context.getName());

                final LoginConfig loginConfig = context.getLoginConfig();
                System.out.println(">>>   loginConfig = " + (loginConfig == null ? "null"
                        : loginConfig.getAuthMethod() + " realm=" + loginConfig.getRealmName()));

                final SecurityConstraint[] constraints = context.findConstraints();
                System.out.println(">>>   constraints = " + constraints.length);
                for (final SecurityConstraint sc : constraints) {
                    for (final SecurityCollection collection : sc.findCollections()) {
                        System.out.println(">>>     collection name=" + collection.getName()
                                + " patterns=" + Arrays.toString(collection.findPatterns())
                                + " methods=" + Arrays.toString(collection.findMethods())
                                + " omitted=" + Arrays.toString(collection.findOmittedMethods())
                                + " authRoles=" + Arrays.toString(sc.findAuthRoles())
                                + " authConstraint=" + sc.getAuthConstraint());
                    }
                }

                for (final Valve valve : context.getPipeline().getValves()) {
                    System.out.println(">>>   valve = " + valve.getClass().getName());
                }
            }
        }
    }
}
