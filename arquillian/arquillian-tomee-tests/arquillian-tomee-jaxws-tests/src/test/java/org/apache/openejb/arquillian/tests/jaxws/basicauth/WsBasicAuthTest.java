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
package org.apache.openejb.arquillian.tests.jaxws.basicauth;

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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Demonstrates a gap in the "common EJB-in-WAR packaging" route through
 * {@link org.apache.tomee.webservices.TomcatWsRegistry#addWsContainer}.
 *
 * <p>A singleton EJB is exposed over SOAP and declares that its endpoint requires BASIC
 * authentication via {@code openejb-jar.xml}'s {@code <web-service-security>} element. That
 * declaration flows all the way to {@code addWsContainer} as the {@code authMethod} argument.</p>
 *
 * <p>When the same bean is packaged in a plain JAR it is deployed into a fresh, generated
 * context by {@code deployInFakeWebapp()} / {@code createNewContext()}, which honours
 * {@code authMethod} by installing a {@code LoginConfig}, a security constraint and a
 * {@code BasicAuthenticator}. But when it is packaged in a WAR the web application context
 * already exists, so {@code addWsContainer} takes the {@code addServlet(...)} branch — which
 * never receives {@code authMethod}/{@code realmName}/{@code transportGuarantee} and therefore
 * applies no security at all. The declared BASIC requirement is silently dropped.</p>
 *
 * <p>This is not merely cosmetic. A WAR is commonly secured with FORM login so its pages behave
 * like a normal web application; a Tomcat context has a single authenticator, so that same FORM
 * mechanism cannot protect a SOAP endpoint (a SOAP client cannot follow an HTML login form). The
 * web service therefore relies on its own BASIC declaration to be enforced — which, on this path,
 * it is not.</p>
 *
 * <p>The test asserts the <em>desired</em> behaviour: an unauthenticated SOAP call must be
 * rejected with a 401 BASIC challenge. Against an unpatched server it fails (the call is served
 * with HTTP 200), documenting the gap; with the fix that threads the declared auth into the
 * {@code addServlet} path it passes.</p>
 */
@RunWith(Arquillian.class)
public class WsBasicAuthTest {

    private static final String SOAP_REQUEST =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
            "                  xmlns:ns=\"http://basicauth.jaxws.tests.arquillian.openejb.apache.org/\">\n" +
            "  <soapenv:Header/>\n" +
            "  <soapenv:Body>\n" +
            "    <ns:greet>\n" +
            "      <name>world</name>\n" +
            "    </ns:greet>\n" +
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
                "      <service-endpoint>org.apache.openejb.arquillian.tests.jaxws.basicauth.GreeterWs</service-endpoint>\n" +
                "      <ejb-class>org.apache.openejb.arquillian.tests.jaxws.basicauth.GreeterBean</ejb-class>\n" +
                "      <session-type>Singleton</session-type>\n" +
                "      <transaction-type>Container</transaction-type>\n" +
                "    </session>\n" +
                "  </enterprise-beans>\n" +
                "</ejb-jar>";

        // The web service declares its own BASIC auth requirement. There is deliberately NO
        // security-constraint in web.xml: the point is that the endpoint must be protected by
        // virtue of this declaration alone, exactly as it would be for a JAR-packaged bean.
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
                "      <service-endpoint-interface>org.apache.openejb.arquillian.tests.jaxws.basicauth.GreeterWs</service-endpoint-interface>\n" +
                "      <service-impl-bean>\n" +
                "        <ejb-link>GreeterBean</ejb-link>\n" +
                "      </service-impl-bean>\n" +
                "    </port-component>\n" +
                "  </webservice-description>\n" +
                "</webservices>";

        // A minimal, unsecured web.xml: this is an ordinary WAR that happens to host a SOAP EJB.
        final String webXml =
                "<web-app xmlns=\"https://jakarta.ee/xml/ns/jakartaee\"\n" +
                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd\"\n" +
                "         version=\"6.0\">\n" +
                "  <display-name>WsBasicAuthTest</display-name>\n" +
                "</web-app>";

        return ShrinkWrap.create(WebArchive.class, "WsBasicAuthTest.war")
                .addClasses(GreeterWs.class, GreeterBean.class)
                .addAsWebInfResource(new StringAsset(ejbJar), "ejb-jar.xml")
                .addAsWebInfResource(new StringAsset(openejbJar), "openejb-jar.xml")
                .addAsWebInfResource(new StringAsset(webservices), "webservices.xml")
                .setWebXML(new StringAsset(webXml));
    }

    @Test
    public void soapWithoutCredentialsIsUnauthorized() throws Exception {
        final HttpURLConnection connection = (HttpURLConnection) endpoint().openConnection();
        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
            connection.setRequestProperty("SOAPAction", "\"\"");
            connection.setDoOutput(true);

            try (final OutputStream out = connection.getOutputStream()) {
                out.write(SOAP_REQUEST.getBytes(UTF_8));
            }

            final int status = connection.getResponseCode();
            assertEquals("The web service declares BASIC auth, so an unauthenticated SOAP call "
                    + "must be rejected with a 401 - not served. Actual status: " + status,
                    HTTP_UNAUTHORIZED, status);

            final String challenge = connection.getHeaderField("WWW-Authenticate");
            assertNotNull("A 401 must carry a WWW-Authenticate challenge", challenge);
            assertTrue("Expected a BASIC challenge but was: " + challenge,
                    challenge.toLowerCase().startsWith("basic"));
        } finally {
            connection.disconnect();
        }
    }

    private URL endpoint() throws Exception {
        String root = base.toExternalForm();
        if (!root.endsWith("/")) {
            root += "/";
        }
        // WEBSERVICE_SUB_CONTEXT (/webservices) + the web-service-address (/ws/Greeter)
        return new URL(root + "webservices/ws/Greeter");
    }
}
