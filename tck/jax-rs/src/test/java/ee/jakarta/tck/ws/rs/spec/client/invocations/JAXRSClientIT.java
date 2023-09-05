/*
 * Copyright (c) 2012, 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package ee.jakarta.tck.ws.rs.spec.client.invocations;

import java.net.URI;

import ee.jakarta.tck.ws.rs.common.client.JaxrsCommonClient;
import ee.jakarta.tck.ws.rs.common.client.JdkLoggingFilter;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import java.io.InputStream;
import java.io.IOException;

import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 */
@ExtendWith(ArquillianExtension.class)
public class JAXRSClientIT extends JaxrsCommonClient {

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_spec_client_invocations_web/resource");
  }

 
  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException{
    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/spec/client/invocations/web.xml.template");
    String webXml = editWebXmlString(inStream);
    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_spec_client_invocations_web.war");
    archive.addClasses(TSAppConfig.class, Resource.class);
    archive.setWebXML(new StringAsset(webXml));
    return archive;
  }

  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : "+testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : "+testInfo.getDisplayName());
  }


  /* Run test */
  /*
   * @testName: synchronousTest
   * 
   * @assertion_ids: JAXRS:SPEC:71;
   * 
   * @test_Strategy: The mapping calls Invocation.invoke() to execute the
   * invocation synchronously; asynchronous execution is also supported by
   * calling Invocation.submit().
   */
  @Test
  public void synchronousTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "call"));
    setProperty(Property.SEARCH_STRING, Resource.class.getName());
    invoke();
  }

  /*
   * @testName: asynchronousTest
   * 
   * @assertion_ids: JAXRS:SPEC:71;
   * 
   * @test_Strategy: The mapping calls Invocation.invoke() to execute the
   * invocation synchronously; asynchronous execution is also supported by
   * calling Invocation.submit().
   */
  @Test
  public void asynchronousTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "call"));
    setProperty(Property.SEARCH_STRING, Resource.class.getName());
    setAsynchronousProcessing();
    invoke();
  }

  /*
   * @testName: invocationFromLinkTextXmlMediaTypeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:411; JAXRS:JAVADOC:788;
   * 
   * @test_Strategy: Build an invocation builder from a link. It uses the URI
   * and the type of the link to initialize the invocation builder. The type is
   * used as the initial value for the HTTP Accept header, if present.
   * 
   * 
   * Build an invocation from a link. The method and URI are obtained from the
   * link. The HTTP Accept header is initialized to the value of the "produces"
   * parameter in the link. If the operation requires an entity, use the
   * overloaded form of this method. This method will throw an
   * java.lang.IllegalArgumentException if there is not enough information to
   * build an invocation (e.g. no HTTP method or entity when required).
   * 
   * Create a new link instance initialized from an existing URI.
   */
  @Test
  public void invocationFromLinkTextXmlMediaTypeTest() throws Fault {
    Response r = invocationFromLinkWithMediaType(MediaType.TEXT_XML);
    checkResposeForMessage(MediaType.TEXT_XML, r);
  }

  /*
   * @testName: invocationFromLinkApplicationJsonMediaTypeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:411; JAXRS:JAVADOC:788;
   * 
   * @test_Strategy: Build an invocation from a link. The method and URI are
   * obtained from the link. The HTTP Accept header is initialized to the value
   * of the "produces" parameter in the link. If the operation requires an
   * entity, use the overloaded form of this method.
   * 
   * Create a new link instance initialized from an existing URI.
   */
  @Test
  public void invocationFromLinkApplicationJsonMediaTypeTest() throws Fault {
    Response r = invocationFromLinkWithMediaType(MediaType.APPLICATION_JSON);
    checkResposeForMessage(MediaType.APPLICATION_JSON, r);
  }

  /*
   * @testName: invocationFromLinkTwoMediaTypesTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:411; JAXRS:JAVADOC:788;
   * 
   * @test_Strategy: Build an invocation from a link. The method and URI are
   * obtained from the link. The HTTP Accept header is initialized to the value
   * of the "produces" parameter in the link. If the operation requires an
   * entity, use the overloaded form of this method.
   * 
   * Create a new link instance initialized from an existing URI.
   */
  @Test
  public void invocationFromLinkTwoMediaTypesTest() throws Fault {
    String type1 = MediaType.APPLICATION_ATOM_XML;
    String type2 = MediaType.TEXT_HTML;
    Response r = invocationFromLinkWithMediaType(type1 + "," + type2);
    r.bufferEntity();
    checkResposeForMessage(type1, r);
    checkResposeForMessage(type2, r);
  }

  // /////////////////////////////////////////////////////////////////////////
  protected Response invocationFromLinkWithMediaType(String mediaType)
      throws Fault {
    String url = "mediatype";
    Client client = ClientBuilder.newClient();
    client.register(new JdkLoggingFilter(false));
    URI uri = UriBuilder.fromPath(getUrl(url)).build();
    Link link = Link.fromUri(uri).type(mediaType).build();
    Invocation i = client.invocation(link).buildGet();
    Response response = i.invoke();
    return response;
  }

  protected void checkResposeForMessage(String message, Response response)
      throws Fault {
    String body = response.readEntity(String.class);
    boolean containsMediaType = body.contains(message);
    assertTrue(containsMediaType == true,
        "The HTTP Accept header does not contain"+ message);
  }

  protected String getUrl(String method) {
    StringBuilder url = new StringBuilder();
    url.append("http://").append(_hostname).append(":").append(_port);
    url.append(getContextRoot()).append("/").append(method);
    return url.toString();
  }
}
