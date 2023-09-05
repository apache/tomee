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

package ee.jakarta.tck.ws.rs.api.client.webtarget;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
public class JAXRSClientIT extends JAXRSCommonClient {

  private static final long serialVersionUID = 5527613586671403135L;

  private static final String ENCODED = "%42%5A%61%7a%2F%%21";

  private static final String SLASHED = "%42%5A%61%7a/%%21";

  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : "+testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : "+testInfo.getDisplayName());
  }

  /*
   * @testName: getUriTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:608;
   * 
   * @test_Strategy: Get the URI identifying the resource.
   */
  @Test
  public void getUriTest() throws Fault {
    WebTarget target = createWebTarget();
    URI uri = target.getUri();
    assertContains(uri.toASCIIString(), URL);
    logMsg("URI", uri, "contains", URL);
  }

  /*
   * @testName: getUriBuilderTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:609;
   * 
   * @test_Strategy: Get the URI builder initialized with the URI of the current
   * resource target. The returned URI builder is detached from the target, i.e.
   * any updates in the URI builder MUST NOT have any effects on the URI of the
   * originating target.
   */
  @Test
  public void getUriBuilderTest() throws Fault {
    WebTarget target = createWebTarget();
    URI uri = target.getUriBuilder().build();
    assertContains(uri.toASCIIString(), URL);
    logMsg("URI", uri, "contains", URL);
  }

  /*
   * @testName: getUriBuilderIsDetachedTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:609;
   * 
   * @test_Strategy: Get the URI builder initialized with the URI of the current
   * resource target. The returned URI builder is detached from the target, i.e.
   * any updates in the URI builder MUST NOT have any effects on the URI of the
   * originating target.
   */
  @Test
  public void getUriBuilderIsDetachedTest() throws Fault {
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(URL);
    UriBuilder builder = target.getUriBuilder();
    client.close(); // The way to affect original target
    URI uri = builder.build();
    assertContains(uri.toASCIIString(), URL);
    logMsg("URI", uri, "contains", URL);
  }

  /*
   * @testName: matrixParamTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:610;
   * 
   * @test_Strategy: Create a new instance by appending a matrix parameter to
   * the existing set of matrix parameters of the current final segment of the
   * URI of the current target instance. If multiple values are supplied the
   * parameter will be added once per value.
   */
  @Test
  public void matrixParamTest() throws Fault {
    WebTarget target = createWebTarget();
    target = target.matrixParam("matrix", "arg1", "arg2", "arg3");
    assertConfigurationSnapshot(target);
    URI uri = target.getUri();
    assertContains(uri.toASCIIString(), ";matrix=arg1");
    assertContains(uri.toASCIIString(), ";matrix=arg2");
    assertContains(uri.toASCIIString(), ";matrix=arg3");
    logMsg("URI", uri, "contains given matrix params");
  }

  /*
   * @testName: matrixParamOnTwoSegmentsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:610; JAXRS:JAVADOC:612;
   * 
   * @test_Strategy: Create a new instance by appending a matrix parameter to
   * the existing set of matrix parameters of the current final segment of the
   * URI of the current target instance. Note that the matrix parameters are
   * tied to a particular path segment; appending a value to an existing matrix
   * parameter name will not affect the position of the matrix parameter in the
   * URI path.
   * 
   * Create a new instance by appending path to the URI of the current target
   * instance.
   */
  @Test
  public void matrixParamOnTwoSegmentsTest() throws Fault {
    WebTarget target = createWebTarget();
    target = target.matrixParam("matrix1", "segment1");
    assertConfigurationSnapshot(target);
    target = target.path("path");
    assertConfigurationSnapshot(target);
    target = target.matrixParam("matrix2", "segment2");
    assertConfigurationSnapshot(target);
    URI uri = target.getUri();
    assertUriContains(uri, ";matrix1=segment1/path;matrix2=segment2");
    logMsg("URI", uri, "contains given matrix params");
  }

  /*
   * @testName:
   * matrixParamWithNullValueRemovesParamsWithTheNameOnMoreSegmentsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:610; JAXRS:JAVADOC:612;
   * 
   * @test_Strategy: Create a new instance by appending a matrix parameter to
   * the existing set of matrix parameters of the current final segment of the
   * URI of the current target instance.
   *
   * In case a single null value is entered, all parameters with that name in
   * the current final path segment are removed (if present) from the collection
   * of last segment matrix parameters inherited from the current target.
   * 
   * Note that the matrix parameters are tied to a particular path segment;
   * appending a value to an existing matrix parameter name will not affect the
   * position of the matrix parameter in the URI path.
   * 
   * Create a new instance by appending path to the URI of the current target
   * instance.
   */
  @Test
  public void matrixParamWithNullValueRemovesParamsWithTheNameOnMoreSegmentsTest()
      throws Fault {
    WebTarget target = createWebTarget();
    target = target.matrixParam("matrix1", "segment1");
    assertConfigurationSnapshot(target);
    target = target.path("path1");
    assertConfigurationSnapshot(target);
    target = target.matrixParam("matrix2", "segment1");
    target = target.matrixParam("matrix2", new Object[] { null });
    assertConfigurationSnapshot(target);
    target = target.path("path2");
    assertConfigurationSnapshot(target);
    target = target.matrixParam("matrix1", "segment1");
    target = target.matrixParam("matrix1", new Object[] { null });
    assertConfigurationSnapshot(target);
    target = target.path("path3");
    URI uri = target.getUri();
    assertUriContains(uri, ";matrix1=segment1/path1/path2/path3");
    logMsg("URI", uri, "contains given matrix params");
  }

  /*
   * @testName: matrixParamThrowsNPEOnNameTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:610;
   * 
   * @test_Strategy: throws NullPointerException if the name or any of the
   * values is null.
   */
  @Test
  public void matrixParamThrowsNPEOnNameTest() throws Fault {
    WebTarget target = createWebTarget();
    try {
      target.matrixParam(null, "segment1");
      throw new Fault("NullPointerException has not been thrown");
    } catch (NullPointerException npe) {
      logMsg("NullPointerException has been thrown as expected", npe);
    }
  }

  /*
   * @testName: matrixParamThrowsNPEOnFirstArgIsNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:610;
   * 
   * @test_Strategy: throws NullPointerException - if there are multiple values
   * present and any of those values is null.
   */
  @Test
  public void matrixParamThrowsNPEOnFirstArgIsNullTest() throws Fault {
    WebTarget target = createWebTarget();
    try {
      target.matrixParam("matrix", null, "segment1");
      throw new Fault("NullPointerException has not been thrown");
    } catch (NullPointerException npe) {
      logMsg("NullPointerException has been thrown as expected", npe);
    }
  }

  /*
   * @testName: matrixParamThrowsNPEOnSecondArgIsNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:610;
   * 
   * @test_Strategy: throws NullPointerException - if there are multiple values
   * present and any of those values is null.
   */
  @Test
  public void matrixParamThrowsNPEOnSecondArgIsNullTest() throws Fault {
    WebTarget target = createWebTarget();
    try {
      target.matrixParam("matrix", "segment1", null);
      throw new Fault("NullPointerException has not been thrown");
    } catch (NullPointerException npe) {
      logMsg("NullPointerException has been thrown as expected", npe);
    }
  }

  /*
   * @testName: pathTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:612;
   * 
   * @test_Strategy: Create a new instance by appending path to the URI of the
   * current target instance. When constructing the final path, a '/' separator
   * will be inserted between the existing path and the supplied path if
   * necessary. Existing '/' characters are preserved thus a single value can
   * represent multiple URI path segments.
   */
  @Test
  public void pathTest() throws Fault {
    WebTarget target = createWebTarget();
    target = target.path("a/").path("/b/").path("/c/").path("d").path("e");
    assertConfigurationSnapshot(target);
    URI uri = target.getUri();
    assertUriContains(uri, "/a/b/c/d/e");
    logMsg("URI", uri, "contains given path");
  }

  /*
   * @testName: pathThrowsNPEOnNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:612;
   * 
   * @test_Strategy: throws NullPointerException if path is null.
   */
  @Test
  public void pathThrowsNPEOnNullTest() throws Fault {
    WebTarget target = createWebTarget();
    try {
      target.path(null);
      throw new Fault("NullPointerException has not been thrown");
    } catch (NullPointerException npe) {
      logMsg("NullPointerException has been thrown as expected", npe);
    }
  }

  /*
   * @testName: queryParamTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:618;
   * 
   * @test_Strategy: Create a new instance by adding a query parameter to the
   * URI of the current target instance. If multiple values are supplied the
   * parameter will be added once per value.
   */
  @Test
  public void queryParamTest() throws Fault {
    WebTarget target = createWebTarget();
    target = target.queryParam("paramName", new StringBuffer().append("value1"),
        new StringBuilder().append("value2"), "value3");
    assertConfigurationSnapshot(target);
    URI uri = target.getUri();
    assertUriContains(uri,
        "?paramName=value1&paramName=value2&paramName=value3");
    logMsg("URI", uri, "contains given query parameter");
  }

  /*
   * @testName: queryParamNullValueTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:618;
   * 
   * @test_Strategy: In case a single null value is entered, all parameters with
   * that name are removed (if present) from the collection of query parameters
   * inherited from the current target.
   */
  @Test
  public void queryParamNullValueTest() throws Fault {
    WebTarget target = createWebTarget();
    target = target.path("path").queryParam("paramName",
        new StringBuffer().append("value1"),
        new StringBuilder().append("value2"), "value3");
    assertConfigurationSnapshot(target);
    target = target.queryParam("param", (Object[]) null).path("path2");
    URI uri = target.getUri();
    assertUriContains(uri, "/path/path2");
    logMsg("#paramName(name, null) removed values as expected");
  }

  /*
   * @testName: queryParamThrowsNPEOnNullNameTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:618;
   * 
   * @test_Strategy: throws NullPointerException if the parameter name is null
   * or if there are multiple values present and any of those values is null.
   */
  @Test
  public void queryParamThrowsNPEOnNullNameTest() throws Fault {
    WebTarget target = createWebTarget();
    try {
      target.queryParam(null, "lane");
      throw new Fault("NullPointerException has not been thrown");
    } catch (NullPointerException npe) {
      logMsg("NullPointerException has been thrown as expected", npe);
    }
  }

  /*
   * @testName: queryParamThrowsNPEOnNullValueTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:618;
   * 
   * @test_Strategy: throws NullPointerException if the parameter name is null
   * or if there are multiple values present and any of those values is null.
   */
  @Test
  public void queryParamThrowsNPEOnNullValueTest() throws Fault {
    WebTarget target = createWebTarget();
    try {
      target.queryParam("param", new Object[] { null, "" });
      throw new Fault("NullPointerException has not been thrown");
    } catch (NullPointerException npe) {
      logMsg("NullPointerException has been thrown as expected", npe);
    }
  }

  /*
   * @testName: requestNoArgTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:622;
   * 
   * @test_Strategy: Start building a request to the targeted web resource.
   */
  @Test
  public void requestNoArgTest() throws Fault {
    WebTarget target = createWebTarget();
    Response response = target.request().buildGet().invoke();
    String body = response.readEntity(String.class);
    assertContains(body, URL);
    assertContains(body, MediaType.WILDCARD);
  }

  /*
   * @testName: requestStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:623;
   * 
   * @test_Strategy: Start building a request to the targeted web resource and
   * define the accepted response media types.
   */
  @Test
  public void requestStringTest() throws Fault {
    WebTarget target = createWebTarget();
    Response response = target.request(MediaType.APPLICATION_ATOM_XML,
        MediaType.APPLICATION_JSON, MediaType.TEXT_XML).buildGet().invoke();
    String body = response.readEntity(String.class);
    assertContains(body, URL);
    assertContains(body, MediaType.APPLICATION_ATOM_XML);
    assertContains(body, MediaType.APPLICATION_JSON);
    assertContains(body, MediaType.TEXT_XML);
  }

  /*
   * @testName: requestMediaTypeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:624;
   * 
   * @test_Strategy: Start building a request to the targeted web resource and
   * define the accepted response media types.
   */
  @Test
  public void requestMediaTypeTest() throws Fault {
    WebTarget target = createWebTarget();
    Response response = target
        .request(MediaType.APPLICATION_ATOM_XML_TYPE,
            MediaType.APPLICATION_JSON_TYPE, MediaType.TEXT_XML_TYPE)
        .buildGet().invoke();
    String body = response.readEntity(String.class);
    assertContains(body, URL);
    assertContains(body, MediaType.APPLICATION_ATOM_XML);
    assertContains(body, MediaType.APPLICATION_JSON);
    assertContains(body, MediaType.TEXT_XML);
  }

  /*
   * @testName: resolveTemplateTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:940;
   * 
   * @test_Strategy: Create a new instance by resolving a URI template with a
   * given name in the URI of the current target instance using a supplied
   * value. In case a template name or value is entered a NullPointerException
   * is thrown. A snapshot of the present configuration of the current (parent)
   * target instance is taken and is inherited by the newly constructed (child)
   * target instance.
   */
  @Test
  public void resolveTemplateTest() throws Fault {
    WebTarget target = createWebTarget();
    target = target.path("{path}").resolveTemplate("path", "lane");
    assertConfigurationSnapshot(target);
    URI uri = target.getUri();
    assertUriContains(uri, "/lane");
    logMsg("URI", uri, "contains given path parameter");
  }

  /*
   * @testName: resolveTemplateThrowsNPEOnNullNameTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:940;
   * 
   * @test_Strategy: Create a new instance by resolving a URI template with a
   * given name in the URI of the current target instance using a supplied
   * value. In case a template name or value is entered a NullPointerException
   * is thrown. A snapshot of the present configuration of the current (parent)
   * target instance is taken and is inherited by the newly constructed (child)
   * target instance.
   */
  @Test
  public void resolveTemplateThrowsNPEOnNullNameTest() throws Fault {
    WebTarget target = createWebTarget();
    try {
      target.path("{path}").resolveTemplate(null, "lane");
      throw new Fault("NullPointerException has not been thrown");
    } catch (NullPointerException npe) {
      logMsg("NullPointerException has been thrown as expected", npe);
    }
  }

  /*
   * @testName: resolveTemplateThrowsNPEOnNullValueTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:940;
   * 
   * @test_Strategy: Create a new instance by resolving a URI template with a
   * given name in the URI of the current target instance using a supplied
   * value. In case a template name or value is entered a NullPointerException
   * is thrown. A snapshot of the present configuration of the current (parent)
   * target instance is taken and is inherited by the newly constructed (child)
   * target instance.
   */
  @Test
  public void resolveTemplateThrowsNPEOnNullValueTest() throws Fault {
    WebTarget target = createWebTarget();
    try {
      target.path("{path}").resolveTemplate("path", null);
      throw new Fault("NullPointerException has not been thrown");
    } catch (NullPointerException npe) {
      logMsg("NullPointerException has been thrown as expected", npe);
    }
  }

  /*
   * @testName: resolveTemplateWithBooleanFalseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:942;
   * 
   * @test_Strategy: Create a new instance by resolving a URI template with a
   * given name in the URI of the current target instance using a supplied
   * value. In case a template name or value is entered a NullPointerException
   * is thrown. A snapshot of the present configuration of the current (parent)
   * target instance is taken and is inherited by the newly constructed (child)
   * target instance.
   * 
   * if true, the slash ('/') characters in template values will be encoded if
   * the template is placed in the URI path component, otherwise the slash
   * characters will not be encoded in path templates.
   */
  @Test
  public void resolveTemplateWithBooleanFalseTest() throws Fault {
    WebTarget target = createWebTarget();
    target = target.path("{path}").resolveTemplate("path", ENCODED, false);
    assertConfigurationSnapshot(target);
    URI uri = target.getUri();
    assertUriContains(uri, "/" + ENCODED.replace("%", "%25"));
    logMsg("URI", uri, "contains given path parameter");
  }

  /*
   * @testName: resolveTemplateWithBooleanTrueTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:942;
   * 
   * @test_Strategy: Create a new instance by resolving a URI template with a
   * given name in the URI of the current target instance using a supplied
   * value. In case a template name or value is entered a NullPointerException
   * is thrown. A snapshot of the present configuration of the current (parent)
   * target instance is taken and is inherited by the newly constructed (child)
   * target instance.
   * 
   * if true, the slash ('/') characters in template values will be encoded if
   * the template is placed in the URI path component, otherwise the slash
   * characters will not be encoded in path templates.
   */
  @Test
  public void resolveTemplateWithBooleanTrueTest() throws Fault {
    WebTarget target = createWebTarget();
    target = target.path("{path}").resolveTemplate("path", SLASHED, true);
    assertConfigurationSnapshot(target);
    URI uri = target.getUri();
    assertUriContains(uri,
        "/" + SLASHED.replace("%", "%25").replace("/", "%2F"));
    logMsg("URI", uri, "contains given path parameter");
  }

  /*
   * @testName: resolveTemplateWithBooleanTrueThrowsNPEOnNullNameTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:942;
   * 
   * @test_Strategy: Create a new instance by resolving a URI template with a
   * given name in the URI of the current target instance using a supplied
   * value. In case a template name or value is entered a NullPointerException
   * is thrown. A snapshot of the present configuration of the current (parent)
   * target instance is taken and is inherited by the newly constructed (child)
   * target instance.
   */
  @Test
  public void resolveTemplateWithBooleanTrueThrowsNPEOnNullNameTest()
      throws Fault {
    WebTarget target = createWebTarget();
    try {
      target.path("{path}").resolveTemplate(null, "lane", true);
      throw new Fault("NullPointerException has not been thrown");
    } catch (NullPointerException npe) {
      logMsg("NullPointerException has been thrown as expected", npe);
    }
  }

  /*
   * @testName: resolveTemplateWithBooleanTrueThrowsNPEOnNullValueTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:942;
   * 
   * @test_Strategy: Create a new instance by resolving a URI template with a
   * given name in the URI of the current target instance using a supplied
   * value. In case a template name or value is entered a NullPointerException
   * is thrown. A snapshot of the present configuration of the current (parent)
   * target instance is taken and is inherited by the newly constructed (child)
   * target instance.
   */
  @Test
  public void resolveTemplateWithBooleanTrueThrowsNPEOnNullValueTest()
      throws Fault {
    WebTarget target = createWebTarget();
    try {
      target.path("{path}").resolveTemplate("path", null, true);
      throw new Fault("NullPointerException has not been thrown");
    } catch (NullPointerException npe) {
      logMsg("NullPointerException has been thrown as expected", npe);
    }
  }

  /*
   * @testName: resolveTemplateWithBooleanFalseThrowsNPEOnNullNameTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:942;
   * 
   * @test_Strategy: Create a new instance by resolving a URI template with a
   * given name in the URI of the current target instance using a supplied
   * value. In case a template name or value is entered a NullPointerException
   * is thrown. A snapshot of the present configuration of the current (parent)
   * target instance is taken and is inherited by the newly constructed (child)
   * target instance.
   */
  @Test
  public void resolveTemplateWithBooleanFalseThrowsNPEOnNullNameTest()
      throws Fault {
    WebTarget target = createWebTarget();
    try {
      target.path("{path}").resolveTemplate(null, "lane", false);
      throw new Fault("NullPointerException has not been thrown");
    } catch (NullPointerException npe) {
      logMsg("NullPointerException has been thrown as expected", npe);
    }
  }

  /*
   * @testName: resolveTemplateWithBooleanFalseThrowsNPEOnNullValueTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:942;
   * 
   * @test_Strategy: Create a new instance by resolving a URI template with a
   * given name in the URI of the current target instance using a supplied
   * value. In case a template name or value is entered a NullPointerException
   * is thrown. A snapshot of the present configuration of the current (parent)
   * target instance is taken and is inherited by the newly constructed (child)
   * target instance.
   */
  @Test
  public void resolveTemplateWithBooleanFalseThrowsNPEOnNullValueTest()
      throws Fault {
    WebTarget target = createWebTarget();
    try {
      target.path("{path}").resolveTemplate("path", null, false);
      throw new Fault("NullPointerException has not been thrown");
    } catch (NullPointerException npe) {
      logMsg("NullPointerException has been thrown as expected", npe);
    }
  }

  /*
   * @testName: resolveTemplateFromEncodedTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:944;
   * 
   * @test_Strategy: Create a new WebTarget instance by resolving a URI template
   * with a given name in the URI of the current target instance using a
   * supplied encoded value. A template with a matching name will be replaced by
   * the supplied value. Value is converted to String using its toString()
   * method and is then encoded to match the rules of the URI component to which
   * they pertain. All % characters in the stringified values that are not
   * followed by two hexadecimal numbers will be encoded.
   */
  @Test
  public void resolveTemplateFromEncodedTest() throws Fault {
    WebTarget target = createWebTarget();
    StringBuilder sb = new StringBuilder();
    sb.append(ENCODED);
    target = target.path("{path}").resolveTemplateFromEncoded("path", sb);
    assertConfigurationSnapshot(target);
    URI uri = target.getUri();
    assertUriContains(uri, "/" + ENCODED.replace("%%", "%25%"));
    logMsg("URI", uri, "contains given path parameter");
  }

  /*
   * @testName: resolveTemplateFromEncodedThrowsNPEForNullNameTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:944;
   * 
   * @test_Strategy: NullPointerException - if the resolved template name or
   * value is null.
   */
  @Test
  public void resolveTemplateFromEncodedThrowsNPEForNullNameTest()
      throws Fault {
    WebTarget target = createWebTarget();
    try {
      target.path("{path}").resolveTemplateFromEncoded(null, "xyz");
      throw new Fault("NullPointerException has not been thrown");
    } catch (NullPointerException npe) {
      logMsg("NullPointerException has been thrown as expected", npe);
    }
  }

  /*
   * @testName: resolveTemplateFromEncodedThrowsNPEForNullValueTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:944;
   * 
   * @test_Strategy: NullPointerException - if the resolved template name or
   * value is null.
   */
  @Test
  public void resolveTemplateFromEncodedThrowsNPEForNullValueTest()
      throws Fault {
    WebTarget target = createWebTarget();
    try {
      target.path("{path}").resolveTemplateFromEncoded("path", null);
      throw new Fault("NullPointerException has not been thrown");
    } catch (NullPointerException npe) {
      logMsg("NullPointerException has been thrown as expected", npe);
    }
  }

  /*
   * @testName: resolveTemplatesTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:946;
   * 
   * @test_Strategy: Create a new WebTarget instance by resolving one or more
   * URI templates in the URI of the current target instance using supplied
   * name-value pairs. A call to the method with an empty parameter map is
   * ignored, i.e. same WebTarget instance is returned. A snapshot of the
   * present configuration of the current (parent) target instance is taken and
   * is inherited by the newly constructed (child) target instance.
   */
  @Test
  public void resolveTemplatesTest() throws Fault {
    Map<String, Object> map = new TreeMap<String, Object>();
    map.put("path", new StringBuilder().append("lane"));
    map.put("highway", new StringBuffer().append("route66"));
    map.put("sidewalk", "pavement");

    WebTarget target = createWebTarget();
    target = target.path("{path}").path("{highway}").path("{sidewalk}");
    target = target.resolveTemplates(map);

    assertConfigurationSnapshot(target);
    URI uri = target.getUri();
    assertUriContains(uri, "/lane/route66/pavement");
    logMsg("URI", uri, "contains given path parameters");
  }

  /*
   * @testName: resolveTemplatesReturnsTheSameTargetTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:946;
   * 
   * @test_Strategy: A call to the method with an empty parameter map is
   * ignored, i.e. same WebTarget instance is returned.
   */
  @Test
  public void resolveTemplatesReturnsTheSameTargetTest() throws Fault {
    WebTarget target = createWebTarget();
    target = target.path("{path}");
    WebTarget other = target.resolveTemplates(new TreeMap<String, Object>());
    assertEquals(target, other,
        "#pathParams did not return the same target when the input map is empty");
    logMsg("#pathParams returned the same traget wehn empty as expected");
  }

  /*
   * @testName: resolveTemplatesNullValueTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:946;
   * 
   * @test_Strategy: NullPointerException - if the name-value map or any of the
   * names or values in the map is null.
   */
  @Test
  public void resolveTemplatesNullValueTest() throws Fault {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("path", null);
    WebTarget target = createWebTarget();
    try {
      target.path("{path}").resolveTemplates(map);
      throw new Fault("NullPointerException has not been thrown");
    } catch (NullPointerException npe) {
      logMsg("NullPointerException has been thrown as expected", npe);
    }
  }

  /*
   * @testName: resolveTemplatesNullNameTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:946;
   * 
   * @test_Strategy: NullPointerException - if the name-value map or any of the
   * names or values in the map is null.
   */
  @Test
  public void resolveTemplatesNullNameTest() throws Fault {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put(null, "xyz");
    WebTarget target = createWebTarget();
    try {
      target.path("{path}").resolveTemplates(map);
      throw new Fault("NullPointerException has not been thrown");
    } catch (NullPointerException npe) {
      logMsg("NullPointerException has been thrown as expected", npe);
    }
  }

  /*
   * @testName: resolveTemplatesNullMapTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:946;
   * 
   * @test_Strategy: NullPointerException - if the name-value map or any of the
   * names or values in the map is null.
   */
  @Test
  public void resolveTemplatesNullMapTest() throws Fault {
    WebTarget target = createWebTarget();
    try {
      target.path("{path}").resolveTemplates((Map<String, Object>) null);
      throw new Fault("NullPointerException has not been thrown");
    } catch (NullPointerException npe) {
      logMsg("NullPointerException has been thrown as expected", npe);
    }
  }

  /*
   * @testName: resolveTemplatesWithBooleanTrueTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:948;
   * 
   * @test_Strategy: Create a new WebTarget instance by resolving one or more
   * URI templates in the URI of the current target instance using supplied
   * name-value pairs. A call to the method with an empty parameter map is
   * ignored, i.e. same WebTarget instance is returned. A snapshot of the
   * present configuration of the current (parent) target instance is taken and
   * is inherited by the newly constructed (child) target instance.
   */
  @Test
  public void resolveTemplatesWithBooleanTrueTest() throws Fault {
    Map<String, Object> map = new TreeMap<String, Object>();
    map.put("path", new StringBuilder().append("lane"));
    map.put("highway", new StringBuffer().append("route66"));
    map.put("sidewalk", SLASHED);

    WebTarget target = createWebTarget();
    target = target.path("{path}").path("{highway}").path("{sidewalk}");
    target = target.resolveTemplates(map, true);

    assertConfigurationSnapshot(target);
    URI uri = target.getUri();
    assertUriContains(uri,
        "/lane/route66/" + SLASHED.replace("%", "%25").replace("/", "%2F"));
    logMsg("URI", uri, "contains given path parameters");
  }

  /*
   * @testName: resolveTemplatesWithBooleanFalseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:948;
   * 
   * @test_Strategy: Create a new WebTarget instance by resolving one or more
   * URI templates in the URI of the current target instance using supplied
   * name-value pairs. A call to the method with an empty parameter map is
   * ignored, i.e. same WebTarget instance is returned. A snapshot of the
   * present configuration of the current (parent) target instance is taken and
   * is inherited by the newly constructed (child) target instance.
   */
  @Test
  public void resolveTemplatesWithBooleanFalseTest() throws Fault {
    Map<String, Object> map = new TreeMap<String, Object>();
    map.put("path", new StringBuilder().append("lane"));
    map.put("highway", new StringBuffer().append("route66"));
    map.put("sidewalk", "pavement");

    WebTarget target = createWebTarget();
    target = target.path("{path}").path("{highway}").path("{sidewalk}");
    target = target.resolveTemplates(map, false);

    assertConfigurationSnapshot(target);
    URI uri = target.getUri();
    assertUriContains(uri, "/lane/route66/pavement");
    logMsg("URI", uri, "contains given path parameters");
  }

  /*
   * @testName: resolveTemplatesWithBooleanTrueReturnsTheSameTargetTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:948;
   * 
   * @test_Strategy: A call to the method with an empty parameter map is
   * ignored, i.e. same WebTarget instance is returned.
   */
  @Test
  public void resolveTemplatesWithBooleanTrueReturnsTheSameTargetTest()
      throws Fault {
    WebTarget target = createWebTarget();
    target = target.path("{path}");
    WebTarget other = target.resolveTemplates(new TreeMap<String, Object>(),
        true);
    assertEquals(target, other,
        "#pathParams did not return the same target when the input map is empty");
    logMsg("#pathParams returned the same traget wehn empty as expected");
  }

  /*
   * @testName: resolveTemplatesWithBooleanFalseReturnsTheSameTargetTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:948;
   * 
   * @test_Strategy: A call to the method with an empty parameter map is
   * ignored, i.e. same WebTarget instance is returned.
   */
  @Test
  public void resolveTemplatesWithBooleanFalseReturnsTheSameTargetTest()
      throws Fault {
    WebTarget target = createWebTarget();
    target = target.path("{path}");
    WebTarget other = target.resolveTemplates(new TreeMap<String, Object>(),
        false);
    assertEquals(target, other,
        "#pathParams did not return the same target when the input map is empty");
    logMsg("#pathParams returned the same traget wehn empty as expected");
  }

  /*
   * @testName: resolveTemplatesWithBooleanNullValueTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:948;
   * 
   * @test_Strategy: NullPointerException - if the name-value map or any of the
   * names or values in the map is null.
   */
  @Test
  public void resolveTemplatesWithBooleanNullValueTest() throws Fault {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("path", null);
    WebTarget target = createWebTarget();
    try {
      target.path("{path}").resolveTemplates(map, true);
      throw new Fault("NullPointerException has not been thrown");
    } catch (NullPointerException npe) {
      logMsg("NullPointerException has been thrown as expected", npe);
    }
  }

  /*
   * @testName: resolveTemplatesWithBooleanNullNameTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:948;
   * 
   * @test_Strategy: NullPointerException - if the name-value map or any of the
   * names or values in the map is null.
   */
  @Test
  public void resolveTemplatesWithBooleanNullNameTest() throws Fault {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put(null, "xyz");
    WebTarget target = createWebTarget();
    try {
      target.path("{path}").resolveTemplates(map, false);
      throw new Fault("NullPointerException has not been thrown");
    } catch (NullPointerException npe) {
      logMsg("NullPointerException has been thrown as expected", npe);
    }
  }

  /*
   * @testName: resolveTemplatesWithBooleanNullMapTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:948;
   * 
   * @test_Strategy: NullPointerException - if the name-value map or any of the
   * names or values in the map is null.
   */
  @Test
  public void resolveTemplatesWithBooleanNullMapTest() throws Fault {
    WebTarget target = createWebTarget();
    try {
      target.path("{path}").resolveTemplates((Map<String, Object>) null, false);
      throw new Fault("NullPointerException has not been thrown");
    } catch (NullPointerException npe) {
      logMsg("NullPointerException has been thrown as expected", npe);
    }
  }

  /*
   * @testName: resolveTemplatesFromEncodedTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:950;
   * 
   * @test_Strategy: Create a new WebTarget instance by resolving a URI template
   * with a given name in the URI of the current target instance using a
   * supplied encoded value. A template with a matching name will be replaced by
   * the supplied value. Value is converted to String using its toString()
   * method and is then encoded to match the rules of the URI component to which
   * they pertain. All % characters in the stringified values that are not
   * followed by two hexadecimal numbers will be encoded.
   */
  @Test
  public void resolveTemplatesFromEncodedTest() throws Fault {
    WebTarget target = createWebTarget();
    StringBuilder sb = new StringBuilder();
    sb.append(ENCODED);
    HashMap<String, Object> map = new HashMap<String, Object>();
    map.put("path", sb);
    target = target.path("{path}").resolveTemplatesFromEncoded(map);
    assertConfigurationSnapshot(target);
    URI uri = target.getUri();
    assertUriContains(uri, "/" + ENCODED.replace("%%", "%25%"));
    logMsg("URI", uri, "contains given path parameter");
  }

  /*
   * @testName: resolveTemplatesFromEncodedReturnsTheSameTargetTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:950;
   * 
   * @test_Strategy: A call to the method with an empty parameter map is
   * ignored, i.e. same WebTarget instance is returned.
   */
  @Test
  public void resolveTemplatesFromEncodedReturnsTheSameTargetTest()
      throws Fault {
    WebTarget target = createWebTarget();
    target = target.path("{path}");
    WebTarget other = target
        .resolveTemplatesFromEncoded(new TreeMap<String, Object>());
    assertEquals(target, other,
        "#pathParams did not return the same target when the input map is empty");
    logMsg("#pathParams returned the same traget wehn empty as expected");
  }

  /*
   * @testName: resolveTemplatesFromEncodedThrowsNPEForNullNameTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:950;
   * 
   * @test_Strategy: NullPointerException - if the name-value map or any of the
   * names or encoded values in the map is null.
   */
  @Test
  public void resolveTemplatesFromEncodedThrowsNPEForNullNameTest()
      throws Fault {
    WebTarget target = createWebTarget();
    HashMap<String, Object> map = new HashMap<String, Object>();
    map.put(null, "xyz");
    try {
      target.path("{path}").resolveTemplatesFromEncoded(map);
      throw new Fault("NullPointerException has not been thrown");
    } catch (NullPointerException npe) {
      logMsg("NullPointerException has been thrown as expected", npe);
    }
  }

  /*
   * @testName: resolveTemplatesFromEncodedThrowsNPEForNullValueTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:950;
   * 
   * @test_Strategy: NullPointerException - if the name-value map or any of the
   * names or encoded values in the map is null.
   */
  @Test
  public void resolveTemplatesFromEncodedThrowsNPEForNullValueTest()
      throws Fault {
    WebTarget target = createWebTarget();
    HashMap<String, Object> map = new HashMap<String, Object>();
    map.put("path", null);
    try {
      target.path("{path}").resolveTemplatesFromEncoded(map);
      throw new Fault("NullPointerException has not been thrown");
    } catch (NullPointerException npe) {
      logMsg("NullPointerException has been thrown as expected", npe);
    }
  }

  /*
   * @testName: resolveTemplatesFromEncodedThrowsNPEForNullMapTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:950;
   * 
   * @test_Strategy: NullPointerException - if the name-value map or any of the
   * names or encoded values in the map is null.
   */
  @Test
  public void resolveTemplatesFromEncodedThrowsNPEForNullMapTest()
      throws Fault {
    WebTarget target = createWebTarget();
    try {
      target.path("{path}")
          .resolveTemplatesFromEncoded((TreeMap<String, Object>) null);
      throw new Fault("NullPointerException has not been thrown");
    } catch (NullPointerException npe) {
      logMsg("NullPointerException has been thrown as expected", npe);
    }
  }

  // ///////////////////////////////////////////////////////////////////////

  /**
   * Simulates server side
   * 
   * @return Response containing request method and entity
   */
  protected static ClientRequestFilter createRequestFilter() {
    ClientRequestFilter filter = new ClientRequestFilter() {
      @Override
      public void filter(ClientRequestContext ctx) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(ctx.getMethod()).append(";");
        sb.append(ctx.getUri().toASCIIString()).append(";");
        if (ctx.hasEntity())
          sb.append(ctx.getEntity()).append(";");
        List<MediaType> list = ctx.getAcceptableMediaTypes();
        for (MediaType type : list)
          sb.append(type).append(";");
        Response r = Response.ok(sb.toString()).build();
        ctx.abortWith(r);
      }
    };
    return filter;
  }

  protected WebTarget createWebTarget() throws Fault {
    Client client = ClientBuilder.newClient();
    client.register(FILTER);
    WebTarget target = client.target(URL);
    assertConfigurationSnapshot(target);
    return target;
  }

  protected static void assertContains(String string, String substring)
      throws Fault {
    assertTrue(string.toLowerCase().contains(substring.toLowerCase()), string +
        " does not contain expected " + substring);
    logMsg("Found expected", substring);
  }

  protected static//
  void assertUriContains(URI uri, String suffix) throws Fault {
    String normalizedUri = uri.toASCIIString().replace(" ", "").replace("%2f",
        "%2F");
    assertContains(normalizedUri, URL + suffix);
  }

  /**
   * Whenever a new instance of WebTarget is created, one should check the
   * Configuration is inherited, i.e. it contains FILTER instance.
   * 
   * This is because javadoc says: A snapshot of the present configuration of
   * the current (parent) target instance is taken and is inherited by the newly
   * constructed (child) target instance.
   */
  protected static void assertConfigurationSnapshot(WebTarget target)
      throws Fault {
    Collection<Object> filters = target.getConfiguration().getInstances();
    assertTrue(filters.contains(FILTER),
        "The snapshot of configuration has not been taken");
  }

  protected static final String URL = "http://cts.tck:888/resource";

  protected static final ClientRequestFilter FILTER = createRequestFilter();
}
