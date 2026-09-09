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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

/**
 * The EJB-in-WAR counterpart to {@link WsVerbSecurityJarTest}: the same singleton EJB web service,
 * declaring BASIC auth, but packaged in a WAR so that {@code TomcatWsRegistry#addWsContainer} takes
 * the {@code addServlet} route into the existing web application context.
 *
 * <p>Both verbs assert the desired behaviour - an unauthenticated call is challenged with 401 -
 * so the module's result matrix shows how the two deployment styles differ on HTTP-method scoping.
 * On a tree where the addServlet route applies no security to the endpoint, both fail; where it
 * secures the mapping without restricting methods, both pass (unlike the JAR route, which leaves
 * every verb except GET/POST uncovered).</p>
 */
@RunWith(Arquillian.class)
public class WsVerbSecurityWarTest {

    private static final String SOAP_REQUEST =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
            "                  xmlns:ns=\"http://verb.jaxws.tests.arquillian.openejb.apache.org/\">\n" +
            "  <soapenv:Header/>\n" +
            "  <soapenv:Body>\n" +
            "    <ns:greet><name>world</name></ns:greet>\n" +
            "  </soapenv:Body>\n" +
            "</soapenv:Envelope>";

    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive war() {
        final String ejbJar =
                "<ejb-jar xmlns=\"http://java.sun.com/xml/ns/javaee\"\n" +
                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/ejb-jar_3_1.xsd\"\n" +
                "         version=\"3.1\" metadata-complete=\"false\">\n" +
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
                "<webservices xmlns=\"http://java.sun.com/xml/ns/j2ee\"\n" +
                "             xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "             xsi:schemaLocation=\"http://java.sun.com/xml/ns/j2ee http://www.ibm.com/webservices/xsd/j2ee_web_services_1_1.xsd\"\n" +
                "             version=\"1.1\">\n" +
                "  <webservice-description>\n" +
                "    <webservice-description-name>GreeterService</webservice-description-name>\n" +
                "    <port-component>\n" +
                "      <port-component-name>GreeterPort</port-component-name>\n" +
                "      <wsdl-port>GreeterPort</wsdl-port>\n" +
                "      <service-endpoint-interface>org.apache.openejb.arquillian.tests.jaxws.verb.GreeterWs</service-endpoint-interface>\n" +
                "      <service-impl-bean>\n" +
                "        <ejb-link>GreeterBean</ejb-link>\n" +
                "      </service-impl-bean>\n" +
                "    </port-component>\n" +
                "  </webservice-description>\n" +
                "</webservices>";

        final String webXml =
                "<web-app xmlns=\"https://jakarta.ee/xml/ns/jakartaee\"\n" +
                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd\"\n" +
                "         version=\"6.0\">\n" +
                "  <display-name>WsVerbSecurityWar</display-name>\n" +
                "</web-app>";

        return ShrinkWrap.create(WebArchive.class, "WsVerbSecurityWar.war")
                .addClasses(GreeterWs.class, GreeterBean.class)
                .addAsWebInfResource(new StringAsset(ejbJar), "ejb-jar.xml")
                .addAsWebInfResource(new StringAsset(openejbJar), "openejb-jar.xml")
                .addAsWebInfResource(new StringAsset(webservices), "webservices.xml")
                .setWebXML(new StringAsset(webXml));
    }

    @Test
    public void postWithoutCredentialsIsUnauthorized() throws Exception {
        final int status = call("POST", SOAP_REQUEST);
        assertEquals("An unauthenticated POST must be challenged with 401. Actual: " + status,
                HTTP_UNAUTHORIZED, status);
    }

    @Test
    public void deleteWithoutCredentialsIsUnauthorized() throws Exception {
        final int status = call("DELETE", null);
        assertEquals("An unauthenticated DELETE must be challenged with 401, but it bypassed "
                + "authentication (a non-401 status means it reached the servlet). Actual: " + status,
                HTTP_UNAUTHORIZED, status);
    }

    private int call(final String method, final String body) throws Exception {
        String root = base.toExternalForm();
        if (!root.endsWith("/")) {
            root += "/";
        }
        // WEBSERVICE_SUB_CONTEXT (/webservices) + the web-service-address (/ws/Greeter)
        final HttpURLConnection connection = (HttpURLConnection) new URL(root + "webservices/ws/Greeter").openConnection();
        try {
            connection.setRequestMethod(method);
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            if (body != null) {
                connection.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
                connection.setRequestProperty("SOAPAction", "\"\"");
                connection.setDoOutput(true);
                try (final OutputStream out = connection.getOutputStream()) {
                    out.write(body.getBytes(UTF_8));
                }
            }
            return connection.getResponseCode();
        } finally {
            connection.disconnect();
        }
    }
}
