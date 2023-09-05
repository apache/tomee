/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.rs.client.invocationbuilder;

import java.util.Locale;
import java.io.InputStream;
import java.io.IOException;

import ee.jakarta.tck.ws.rs.common.client.JaxrsCommonClient;
import ee.jakarta.tck.ws.rs.common.client.JdkLoggingFilter;
import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

import jakarta.ws.rs.client.AsyncInvoker;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 */
@ExtendWith(ArquillianExtension.class)
public class JAXRSClientIT extends JaxrsCommonClient {

  private static final long serialVersionUID = -8097693127928445210L;

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_ee_rs_client_invocationbuilder_web/resource");
  }

  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : "+testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : "+testInfo.getDisplayName());
  }

  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException{

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/client/invocationbuilder/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_rs_client_invocationbuilder_web.war");
    archive.addClasses(TSAppConfig.class, Resource.class, JaxrsUtil.class);
    archive.setWebXML(new StringAsset(webXml));
    return archive;

  }


  static final String[] METHODS = { "delete", "get", "options" };

  static final String[] ENTITY_METHODS = { "put", "post" };

  /* Run test */
  /*
   * @testName: acceptLanguageByLocalesTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:524;
   * 
   * @test_Strategy: Add acceptable languages.
   */
  @Test
  public void acceptLanguageByLocalesTest() throws Fault {
    Invocation.Builder builder = createBuilderForMethod("languages");
    builder = builder.acceptLanguage(Locale.GERMAN, Locale.ITALIAN,
        Locale.FRENCH);
    String response = builder.buildGet().invoke(String.class);
    String error = "Expected locale was not found in the response:";
    assertContainsIgnoreCase(response, Locale.GERMAN, error, response);
    assertContainsIgnoreCase(response, Locale.ITALIAN, error, response);
    assertContainsIgnoreCase(response, Locale.FRENCH, error, response);
  }

  /*
   * @testName: acceptLanguageByStringsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:525;
   * 
   * @test_Strategy: Add acceptable languages.
   */
  @Test
  public void acceptLanguageByStringsTest() throws Fault {
    Invocation.Builder builder = createBuilderForMethod("languages");
    builder = builder.acceptLanguage(langToString(Locale.GERMAN),
        langToString(Locale.ITALIAN), langToString(Locale.FRENCH));
    String response = builder.buildGet().invoke(String.class);
    String error = "Expected locale was not found in the response:";
    assertContainsIgnoreCase(response, Locale.GERMAN, error, response);
    assertContainsIgnoreCase(response, Locale.ITALIAN, error, response);
    assertContainsIgnoreCase(response, Locale.FRENCH, error, response);
  }

  /*
   * @testName: asyncTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:526;
   * 
   * @test_Strategy: Access the asynchronous uniform request invocation
   * interface to asynchronously invoke the built request.
   */
  @Test
  public void asyncTest() throws Fault {
    Invocation.Builder builder = createBuilderForMethod("forbid");
    AsyncInvoker async = builder.async();
    assertTrue(async != null, "Builder.async() does not work properly");
  }

  /*
   * @testName: buildTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:527;
   * 
   * @test_Strategy: Build a request invocation using an arbitrary request
   * method name.
   */
  @Test
  public void buildTest() throws Fault {
    String error = "Unexpected response returned:";
    for (String method : METHODS) {
      Invocation.Builder builder = createBuilderForMethod(method);
      String response = builder.build(method.toUpperCase())
          .invoke(String.class);
      assertContainsIgnoreCase(response, method, error, response);
    }
  }

  /*
   * @testName: buildWithEntityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:528;
   * 
   * @test_Strategy: Build a request invocation using an arbitrary request
   * method name and request entity
   */
  @Test
  public void buildWithEntityTest() throws Fault {
    String error = "Unexpected response returned:";
    for (String method : ENTITY_METHODS) {
      Invocation.Builder builder = createBuilderForMethod(method);
      Entity<String> entity = createEntity(method);
      String response = builder.build(method.toUpperCase(), entity)
          .invoke(String.class);
      assertContainsIgnoreCase(response, method, error, response);
    }
  }

  /*
   * @testName: buildDeleteTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:529;
   * 
   * @test_Strategy: Build a DELETE request invocation.
   */
  @Test
  public void buildDeleteTest() throws Fault {
    String error = "Unexpected response returned:";
    Invocation.Builder builder = createBuilderForMethod("delete");
    String response = builder.buildDelete().invoke(String.class);
    assertContainsIgnoreCase(response, "delete", error, response);
  }

  /*
   * @testName: buildGetTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:530;
   * 
   * @test_Strategy: Build a GET request invocation.
   */
  @Test
  public void buildGetTest() throws Fault {
    String error = "Unexpected response returned:";
    Invocation.Builder builder = createBuilderForMethod("get");
    String response = builder.buildGet().invoke(String.class);
    assertContainsIgnoreCase(response, "get", error, response);
  }

  /*
   * @testName: buildPostTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:531;
   * 
   * @test_Strategy: Build a POST request invocation.
   */
  @Test
  public void buildPostTest() throws Fault {
    String error = "Unexpected response returned:";
    Invocation.Builder builder = createBuilderForMethod("post");
    Entity<String> entity = createEntity("post");
    String response = builder.buildPost(entity).invoke(String.class);
    assertContainsIgnoreCase(response, "post", error, response);
  }

  /*
   * @testName: buildPutTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:532;
   * 
   * @test_Strategy: Build a PUT request invocation.
   */
  @Test
  public void buildPutTest() throws Fault {
    String error = "Unexpected response returned:";
    Invocation.Builder builder = createBuilderForMethod("put");
    Entity<String> entity = createEntity("put");
    String response = builder.buildPut(entity).invoke(String.class);
    assertContainsIgnoreCase(response, "put", error, response);
  }

  /*
   * @testName: cacheControlTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:533;
   * 
   * @test_Strategy: Set the cache control data of the message.
   */
  @Test
  public void cacheControlTest() throws Fault {
    String error = "Unexpected response returned:";
    Invocation.Builder builder = createBuilderForMethod("headerstostring");
    CacheControl control = new CacheControl();
    control.setMaxAge(2);
    String response = builder.cacheControl(control).buildGet()
        .invoke(String.class).toLowerCase();
    assertContainsIgnoreCase(response, "max-age", error, response);
  }

  /*
   * @testName: cookieCookieTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:535;
   * 
   * @test_Strategy: Add a cookie to be set.
   */
  @Test
  public void cookieCookieTest() throws Fault {
    String error = "Unexpected response returned:";
    Invocation.Builder builder = createBuilderForMethod("cookie");
    Cookie cookie = new Cookie("tck", "cts");
    String response = builder.cookie(cookie).buildGet().invoke(String.class);
    assertContainsIgnoreCase(response, "cts", error, response);
  }

  /*
   * @testName: cookieStringStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:536;
   * 
   * @test_Strategy: Add a cookie to be set.
   */
  @Test
  public void cookieStringStringTest() throws Fault {
    String error = "Unexpected response returned:";
    Invocation.Builder builder = createBuilderForMethod("cookie");
    String response = builder.cookie("tck", "cts").buildGet()
        .invoke(String.class);
    assertContainsIgnoreCase(response, "cts", error, response);
  }

  /*
   * @testName: headerObjectTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:537;
   * 
   * @test_Strategy: Add an arbitrary header.
   */
  @Test
  public void headerObjectTest() throws Fault {
    String error = "Unexpected response returned:";
    Invocation.Builder builder = createBuilderForMethod("headerstostring");
    String response = builder.header("tck-header", "cts-header").buildGet()
        .invoke(String.class);
    assertContainsIgnoreCase(response, "tck-header", error, response);
    assertContainsIgnoreCase(response, "cts-header", error, response);
  }

  /*
   * @testName: headersMultivaluedMapTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:538;
   * 
   * @test_Strategy: Replaces all existing headers with the newly supplied
   * headers.
   */
  @Test
  public void headersMultivaluedMapTest() throws Fault {
    String error = "Unexpected response returned:";
    Invocation.Builder builder = createBuilderForMethod("headerstostring");
    MultivaluedMap<String, Object> map = new MultivaluedHashMap<String, Object>();
    map.add("tck-header", "cts-header");
    String response = builder.header("unexpected-header", "unexpected-header")
        .headers(map).buildGet().invoke(String.class);
    assertTrue(!response.contains("unexpected-header"),
        "unexpected-header found in the response");
    assertContainsIgnoreCase(response, "tck-header", error, response);
    assertContainsIgnoreCase(response, "cts-header", error, response);
  }

  /*
   * @testName: headersMultivaluedMapIsNullReplacesAllTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:538;
   * 
   * @test_Strategy: headers - new headers to be set, if null all existing
   * headers will be removed.
   */
  @Test
  public void headersMultivaluedMapIsNullReplacesAllTest() throws Fault {
    Invocation.Builder builder = createBuilderForMethod("headerstostring");
    String response = builder.header("unexpected-header", "unexpected-header")
        .headers((MultivaluedMap<String, Object>) null).buildGet()
        .invoke(String.class);
    assertTrue(!response.contains("unexpected-header"),
        "unexpected-header found in the response");
  }

  // ////////////////////////////////////////////////////////////////////

  protected String getUrl(String method) {
    StringBuilder url = new StringBuilder();
    url.append("http://").append(_hostname).append(":").append(_port);
    url.append(getContextRoot()).append("/").append(method);
    return url.toString();
  }

  /**
   * Create Invocation.Builder for given resource method and start time
   */
  protected Invocation.Builder createBuilderForMethod(String methodName) {
    Client client = ClientBuilder.newClient();
    client.register(new JdkLoggingFilter(false));
    WebTarget target = client.target(getUrl(methodName));
    Invocation.Builder builder = target.request();
    return builder;
  }

  protected <T> Entity<T> createEntity(T entity) {
    return Entity.entity(entity, MediaType.WILDCARD_TYPE);
  }

  /**
   * @return simulates toLanguageTag() for java prior version 7
   */
  protected String langToString(Locale language) {
    return language.toString().replace("_", "-");
  }

}
