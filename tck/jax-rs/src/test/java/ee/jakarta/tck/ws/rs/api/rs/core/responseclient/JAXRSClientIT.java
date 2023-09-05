/*
 * Copyright (c) 2007, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.api.rs.core.responseclient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.common.client.JaxrsCommonClient;
import ee.jakarta.tck.ws.rs.common.provider.StringBean;
import ee.jakarta.tck.ws.rs.common.provider.StringBeanRuntimeDelegate;
import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.Link.Builder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.Response.StatusType;
import jakarta.ws.rs.core.Variant;
import jakarta.ws.rs.ext.RuntimeDelegate;

public class JAXRSClientIT extends JAXRSCommonClient {

  private static final long serialVersionUID = -2343034378084516380L;

  // name it to ensure sorting
  protected final Response.Status[] resp_status = { Response.Status.OK,
      Response.Status.CREATED, Response.Status.ACCEPTED,
      Response.Status.NO_CONTENT, Response.Status.RESET_CONTENT,
      Response.Status.PARTIAL_CONTENT, Status.MULTIPLE_CHOICES,
      Response.Status.MOVED_PERMANENTLY,
      Response.Status.FOUND, Response.Status.SEE_OTHER,
      Response.Status.NOT_MODIFIED, Response.Status.USE_PROXY,
      Response.Status.TEMPORARY_REDIRECT, Status.PERMANENT_REDIRECT,
      Response.Status.BAD_REQUEST,
      Response.Status.UNAUTHORIZED, Response.Status.PAYMENT_REQUIRED,
      Response.Status.FORBIDDEN, Response.Status.NOT_FOUND,
      Response.Status.METHOD_NOT_ALLOWED, Response.Status.NOT_ACCEPTABLE,
      Response.Status.PROXY_AUTHENTICATION_REQUIRED,
      Response.Status.REQUEST_TIMEOUT, Response.Status.CONFLICT,
      Response.Status.GONE, Response.Status.LENGTH_REQUIRED,
      Response.Status.PRECONDITION_FAILED,
      Response.Status.REQUEST_ENTITY_TOO_LARGE,
      Response.Status.REQUEST_URI_TOO_LONG,
      Response.Status.UNSUPPORTED_MEDIA_TYPE,
      Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE,
      Response.Status.EXPECTATION_FAILED, Response.Status.PRECONDITION_REQUIRED,
      Response.Status.TOO_MANY_REQUESTS,
      Response.Status.REQUEST_HEADER_FIELDS_TOO_LARGE,
      Response.Status.UNAVAILABLE_FOR_LEGAL_REASONS,
      Response.Status.INTERNAL_SERVER_ERROR, Response.Status.NOT_IMPLEMENTED,
      Response.Status.BAD_GATEWAY, Response.Status.SERVICE_UNAVAILABLE,
      Response.Status.GATEWAY_TIMEOUT,
      Response.Status.HTTP_VERSION_NOT_SUPPORTED,
      Response.Status.NETWORK_AUTHENTICATION_REQUIRED };

  // name it to ensure sorting
  protected final int[] status_codes = { 200, 201, 202, 204, 205, 206, 300, 301, 302,
      303, 304, 305, 307, 308, 400, 401, 402, 403, 404, 405, 406, 407, 408, 409, 410,
      411, 412, 413, 414, 415, 416, 417, 428, 429, 431, 451, 500, 501, 502, 503, 504,
      505, 511 };

  // name it to ensure sorting
  protected final Response.Status.Family[] status_family = {
      Response.Status.Family.SUCCESSFUL, Response.Status.Family.SUCCESSFUL,
      Response.Status.Family.SUCCESSFUL, Response.Status.Family.SUCCESSFUL,
      Response.Status.Family.SUCCESSFUL, Response.Status.Family.SUCCESSFUL,
      Response.Status.Family.REDIRECTION, Response.Status.Family.REDIRECTION,
      Response.Status.Family.REDIRECTION, Response.Status.Family.REDIRECTION,
      Response.Status.Family.REDIRECTION, Response.Status.Family.REDIRECTION,
      Response.Status.Family.REDIRECTION, Response.Status.Family.REDIRECTION,
      Response.Status.Family.CLIENT_ERROR,
      Response.Status.Family.CLIENT_ERROR, Response.Status.Family.CLIENT_ERROR,
      Response.Status.Family.CLIENT_ERROR, Response.Status.Family.CLIENT_ERROR,
      Response.Status.Family.CLIENT_ERROR, Response.Status.Family.CLIENT_ERROR,
      Response.Status.Family.CLIENT_ERROR, Response.Status.Family.CLIENT_ERROR,
      Response.Status.Family.CLIENT_ERROR, Response.Status.Family.CLIENT_ERROR,
      Response.Status.Family.CLIENT_ERROR, Response.Status.Family.CLIENT_ERROR,
      Response.Status.Family.CLIENT_ERROR, Response.Status.Family.CLIENT_ERROR,
      Response.Status.Family.CLIENT_ERROR, Response.Status.Family.CLIENT_ERROR,
      Response.Status.Family.CLIENT_ERROR, Response.Status.Family.CLIENT_ERROR,
      Response.Status.Family.CLIENT_ERROR, Response.Status.Family.CLIENT_ERROR,
      Response.Status.Family.CLIENT_ERROR, Response.Status.Family.SERVER_ERROR,
      Response.Status.Family.SERVER_ERROR, Response.Status.Family.SERVER_ERROR,
      Response.Status.Family.SERVER_ERROR, Response.Status.Family.SERVER_ERROR,
      Response.Status.Family.SERVER_ERROR,
      Response.Status.Family.SERVER_ERROR };

  protected final Response.Status.Family[] status_family_list = {
      Response.Status.Family.CLIENT_ERROR, Response.Status.Family.INFORMATIONAL,
      Response.Status.Family.OTHER, Response.Status.Family.REDIRECTION,
      Response.Status.Family.SERVER_ERROR, Response.Status.Family.SUCCESSFUL };

  protected final String[] status = { "OK", "Created", "Accepted", "No Content",
      "Reset Content", "Partial Content", "Multiple Choices", "Moved Permanently", "Found",
      "See Other", "Not Modified", "Use Proxy", "Temporary Redirect", "Permanent Redirect",
      "Bad Request", "Unauthorized", "Payment Required", "Forbidden",
      "Not Found", "Method Not Allowed", "Not Acceptable",
      "Proxy Authentication Required", "Request Timeout", "Conflict", "Gone",
      "Length Required", "Precondition Failed", "Request Entity Too Large",
      "Request-URI Too Long", "Unsupported Media Type",
      "Requested Range Not Satisfiable", "Expectation Failed",
      "Precondition Required", "Too Many Requests",
      "Request Header Fields Too Large", "Unavailable For Legal Reasons", "Internal Server Error",
      "Not Implemented", "Bad Gateway", "Service Unavailable",
      "Gateway Timeout", "HTTP Version Not Supported",
      "Network Authentication Required" };


  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : "+testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : "+testInfo.getDisplayName());
  }

  /*
   * @class.setup_props: webServerHost; webServerPort; 
   */
  /* Run test */

  /*
   * @testName: okTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:131; JAXRS:JAVADOC:141; JAXRS:JAVADOC:123;
   * JAXRS:JAVADOC:124; JAXRS:JAVADOC:125;
   * 
   * @test_Strategy: Create an instance of Response using Response.ok().build()
   * verify that correct status code is returned
   */
  @Test
  public void okTest1() throws Fault {
    VerificationResult result;
    Response response = null;
    int status = 200;
    response = Response.ok().build();
    result = verifyStatus(response, status);
    logMsg(result.message);
    assertResultTrue(result);
  }

  /*
   * @testName: okTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:132; JAXRS:JAVADOC:141; JAXRS:JAVADOC:123;
   * JAXRS:JAVADOC:124; JAXRS:JAVADOC:125;
   * 
   * @test_Strategy: Create an instance of Response using
   * Response.ok(String).build() verify that correct status code is returned
   */
  @Test
  public void okTest2() throws Fault {
    VerificationResult result;
    Response resp = null;
    int status = 200;
    String content = "Test only";
    resp = Response.ok(content).build();
    result = verifyContent(resp, content);
    result.append(verifyStatus(resp, status));
    logMsg(result.message);
    assertResultTrue(result);
  }

  /*
   * @testName: okTest3
   * 
   * @assertion_ids: JAXRS:JAVADOC:134; JAXRS:JAVADOC:141; JAXRS:JAVADOC:123;
   * JAXRS:JAVADOC:124; JAXRS:JAVADOC:125;
   * 
   * @test_Strategy: Create an instance of Response using Response.ok(String,
   * String).build() verify that correct status code is returned
   */
  @Test
  public void okTest3() throws Fault {
    VerificationResult result;
    Response resp = null;
    int status = 200;
    String content = "Test only";
    String type = MediaType.TEXT_PLAIN;
    resp = Response.ok(content, type).build();
    result = verifyContent(resp, content);
    result.append(verifyStatus(resp, status));
    result.append(verifyContentType(resp, Collections.singletonList(type)));
    logMsg(result.message);
    assertResultTrue(result);
  }

  /*
   * @testName: okTest4
   * 
   * @assertion_ids: JAXRS:JAVADOC:133; JAXRS:JAVADOC:141; JAXRS:JAVADOC:123;
   * JAXRS:JAVADOC:124; JAXRS:JAVADOC:125;
   * 
   * @test_Strategy: Create an instance of Response using Response.ok(String,
   * MediaType).build() verify that correct status code is returned
   */
  @Test
  public void okTest4() throws Fault {
    VerificationResult result;
    Response resp = null;
    int status = 200;
    String content = "Test only";
    String type = MediaType.TEXT_PLAIN;
    MediaType mt = new MediaType(MediaType.TEXT_PLAIN_TYPE.getType(),
        MediaType.TEXT_PLAIN_TYPE.getSubtype());
    resp = Response.ok(content, mt).build();
    result = verifyContent(resp, content);
    result.append(verifyStatus(resp, status));
    result.append(verifyContentType(resp, Collections.singletonList(type)));
    logMsg(result.message);
    assertResultTrue(result);
  }

  /*
   * @testName: okTest5
   * 
   * @assertion_ids: JAXRS:JAVADOC:135; JAXRS:JAVADOC:141; JAXRS:JAVADOC:123;
   * JAXRS:JAVADOC:124; JAXRS:JAVADOC:125; JAXRS:JAVADOC:91; JAXRS:JAVADOC:268;
   * JAXRS:JAVADOC:267; JAXRS:JAVADOC:266; JAXRS:JAVADOC:265; JAXRS:JAVADOC:263;
   * JAXRS:JAVADOC:264;
   * 
   * @test_Strategy: Create an instance of Response using Response.ok(String,
   * Variant).build() verify that correct status code is returned
   */
  @Test
  public void okTest5() throws Fault {
    VerificationResult result = new VerificationResult();
    Response resp = null;
    int status = 200;
    String content = "Test Only";
    List<String> encoding = Arrays.asList("gzip", "compress");

    MediaType mt = new MediaType("text", "plain");
    List<Variant> vts = getVariantList(encoding, mt);

    for (int i = 0; i < vts.size(); i++) {
      Variant vt = vts.get(i);
      resp = Response.ok(content, vt).build();
      result.append(verifyContent(resp, content));
      result.append(verifyStatus(resp, status));
      result.append(verifyEncoding(resp, encoding));
      result.append(verifyLanguage(resp, getLangList()));
      result.append(
          verifyContentType(resp, Collections.singletonList(mt.toString())));
    }
    logMsg(result.message);
    assertResultTrue(result);
  }

  /*
   * @testName: noContentTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:126; JAXRS:JAVADOC:141; JAXRS:JAVADOC:123;
   * JAXRS:JAVADOC:124; JAXRS:JAVADOC:125;
   * 
   * @test_Strategy: Create an instance of Response using
   * Response.noContent().build() verify that correct status code is returned
   */
  @Test
  public void noContentTest() throws Fault {
    VerificationResult result;

    Response resp = null;
    int status = 204;

    resp = Response.noContent().build();
    result = verifyStatus(resp, status);
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: notAcceptableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:127; JAXRS:JAVADOC:141; JAXRS:JAVADOC:123;
   * JAXRS:JAVADOC:124; JAXRS:JAVADOC:125; JAXRS:JAVADOC:91; JAXRS:JAVADOC:268;
   * JAXRS:JAVADOC:267; JAXRS:JAVADOC:266; JAXRS:JAVADOC:265; JAXRS:JAVADOC:263;
   * JAXRS:JAVADOC:264;
   * 
   * @test_Strategy: Create an instance of Response using
   * Response.notAcceptable(vts).build() verify that correct status code is
   * returned
   */
  @Test
  public void notAcceptableTest() throws Fault {
    VerificationResult result;

    Response resp = null;
    int status = 406;

    List<String> encoding = Arrays.asList("gzip", "compress");

    MediaType mt = new MediaType("text", "plain");
    List<Variant> vts = getVariantList(encoding, mt);

    resp = Response.notAcceptable(vts).build();
    result = verifyStatus(resp, status);
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: notModifiedTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:128; JAXRS:JAVADOC:141; JAXRS:JAVADOC:123;
   * JAXRS:JAVADOC:124; JAXRS:JAVADOC:125;
   * 
   * @test_Strategy: Create an instance of Response using
   * Response.notModified().build() verify that correct status code is returned
   */
  @Test
  public void notModifiedTest1() throws Fault {
    VerificationResult result;

    Response resp = null;
    int status = 304;
    resp = Response.notModified().build();

    result = verifyStatus(resp, status);
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: notModifiedTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:130; JAXRS:JAVADOC:141; JAXRS:JAVADOC:123;
   * JAXRS:JAVADOC:124; JAXRS:JAVADOC:125;
   * 
   * @test_Strategy: Create an instance of Response using
   * Response.notModified(String).build() verify that correct status code is
   * returned
   */
  @Test
  public void notModifiedTest2() throws Fault {
    VerificationResult result;
    Response resp = null;
    int status = 304;
    String tags = "TestOnly";
    HashMap<String, String> expected_map = new HashMap<String, String>();
    expected_map.put("ETAG", tags);

    resp = Response.notModified(tags).build();
    result = verifyStatus(resp, status);
    result.append(verifyHeaders(resp, expected_map));
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: notModifiedTest3
   * 
   * @assertion_ids: JAXRS:JAVADOC:129; JAXRS:JAVADOC:141; JAXRS:JAVADOC:123;
   * JAXRS:JAVADOC:124; JAXRS:JAVADOC:125;
   * 
   * @test_Strategy: Create an instance of Response using
   * Response.notModified(EntityTag).build() verify that correct status code is
   * returned
   */
  @Test
  public void notModifiedTest3() throws Fault {
    VerificationResult result;
    Response resp = null;
    int status = 304;
    String value = "TestOnly";

    EntityTag et = new EntityTag(value);

    HashMap<String, String> expected_map = new HashMap<String, String>();
    expected_map.put("ETAG", value);

    resp = Response.notModified(et).build();
    result = verifyStatus(resp, status);
    result.append(verifyHeaders(resp, expected_map));
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: statusTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:139; JAXRS:JAVADOC:123; JAXRS:JAVADOC:124;
   * JAXRS:JAVADOC:125; JAXRS:SPEC:14.2;
   * 
   * @test_Strategy: Create an instance of Response using
   * Response.status(int).build() verify that correct status code is returned
   */
  @Test
  public void statusTest1() throws Fault {
    VerificationResult result = new VerificationResult();
    Response resp = null;

    for (int status : status_codes) {
      resp = Response.status(status).build();
      result.append(verifyStatus(resp, status));
    }
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: statusTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:138; JAXRS:JAVADOC:123; JAXRS:JAVADOC:124;
   * JAXRS:JAVADOC:125; JAXRS:SPEC:14.2;
   * 
   * @test_Strategy: Create an instance of Response using
   * Response.status(Response.Status).build() verify that correct status code is
   * returned
   */
  @Test
  public void statusTest2() throws Fault {
    VerificationResult result = new VerificationResult();
    Response resp = null;

    for (int i = 0; i < status_codes.length; i++) {
      resp = Response.status(resp_status[i]).build();
      result.append(verifyStatus(resp, status_codes[i]));
    }
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: statusTest3
   * 
   * @assertion_ids: JAXRS:JAVADOC:139; JAXRS:JAVADOC:131; JAXRS:JAVADOC:123;
   * JAXRS:JAVADOC:124; JAXRS:JAVADOC:125; JAXRS:SPEC:14.2;
   * 
   * @test_Strategy: Create an instance of Response using
   * Response.ResponseBuilder.status(int).build() verify that correct status
   * code is returned
   */
  @Test
  public void statusTest3() throws Fault {
    VerificationResult result = new VerificationResult();
    Response resp = null;

    for (int status : status_codes) {
      resp = Response.ok().status(status).build();
      result.append(verifyStatus(resp, status));
    }
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: statusTest4
   * 
   * @assertion_ids: JAXRS:JAVADOC:138; JAXRS:JAVADOC:131; JAXRS:JAVADOC:123;
   * JAXRS:JAVADOC:124; JAXRS:JAVADOC:125; JAXRS:SPEC:14.2;
   * 
   * @test_Strategy: Create an instance of Response using
   * Response.ResponseBuilder.status(Response.Status).build() verify that
   * correct status code is returned
   */
  @Test
  public void statusTest4() throws Fault {
    VerificationResult result = new VerificationResult();
    Response resp = null;

    for (int i = 0; i < status_codes.length; i++) {
      resp = Response.ok().status(resp_status[i]).build();
      result.append(verifyStatus(resp, status_codes[i]));
    }
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: createdTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:121; JAXRS:JAVADOC:141; JAXRS:JAVADOC:123;
   * JAXRS:JAVADOC:124; JAXRS:JAVADOC:125;
   * 
   * @test_Strategy: Create an instance of Response using
   * Response.created(URI).build() verify that correct status code is returned
   */
  @Test
  public void createdTest() throws Fault {
    VerificationResult result = new VerificationResult();

    List<String> uri_expected = Arrays.asList("mailto:java-net@java.sun.com",
        "news:comp.lang.java", "urn:isbn:096139210x",
        "http://java.sun.com/j2se/1.3/",
        "docs/guide/collections/designfaq.html#28",
        "../../../demo/jfc/SwingSet2/src/SwingSet2.java", "file:///~/calendar");

    URI test_uri = null;
    for (String uri_string : uri_expected) {
      try {
        test_uri = new URI(uri_string);
      } catch (URISyntaxException ex) {
        result.message.append("Unexpected exception thrown:")
            .append(ex.getMessage());
        result.pass = false;
      }
      Response resp = Response.created(test_uri).build();

      HashMap<String, String> expected_map = new HashMap<String, String>();
      expected_map.put("Location", uri_string);
      result.append(verifyStatus(resp, 201));
      result.append(verifyHeaders(resp, expected_map));
    }
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: serverErrorTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:137; JAXRS:JAVADOC:141; JAXRS:JAVADOC:123;
   * JAXRS:JAVADOC:124; JAXRS:JAVADOC:125;
   * 
   * @test_Strategy: Create an instance of Response using
   * Response.serverError().build() verify that correct status code is returned
   */
  @Test
  public void serverErrorTest() throws Fault {
    VerificationResult result;

    Response resp = Response.serverError().build();
    result = verifyStatus(resp, 500);
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: seeOtherTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:136; JAXRS:JAVADOC:141; JAXRS:JAVADOC:123;
   * JAXRS:JAVADOC:124; JAXRS:JAVADOC:125;
   * 
   * @test_Strategy: Create an instance of Response using
   * Response.seeOther(URI).build() verify that correct status code is returned
   */
  @Test
  public void seeOtherTest() throws Fault {
    VerificationResult result = new VerificationResult();

    URI test_uri = null;
    try {
      test_uri = new URI("http://java.sun.com/j2se/1.3/");
    } catch (URISyntaxException ex) {
      result.message.append("Unexpected exception thrown:")
          .append(ex.getMessage());
      result.pass = false;
    }
    Response resp = Response.seeOther(test_uri).build();

    HashMap<String, String> expected_map = new HashMap<String, String>();
    expected_map.put("Location", "http://java.sun.com/j2se/1.3/");
    result.append(verifyStatus(resp, 303));
    result.append(verifyHeaders(resp, expected_map));
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: temporaryRedirectTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:140; JAXRS:JAVADOC:141; JAXRS:JAVADOC:123;
   * JAXRS:JAVADOC:124; JAXRS:JAVADOC:125;
   * 
   * @test_Strategy: Create an instance of Response using
   * Response.temporaryRedirect(URI).build() verify that correct status code is
   * returned
   */
  @Test
  public void temporaryRedirectTest() throws Fault {
    VerificationResult result = new VerificationResult();

    URI test_uri = null;
    try {
      test_uri = new URI("http://java.sun.com/j2se/1.3/");
    } catch (URISyntaxException ex) {
      result.message.append("Unexpected exception thrown:")
          .append(ex.getMessage());
      result.pass = false;
    }
    Response resp = Response.temporaryRedirect(test_uri).build();

    HashMap<String, String> expected_map = new HashMap<String, String>();
    expected_map.put("Location", "http://java.sun.com/j2se/1.3/");
    result.append(verifyStatus(resp, 307));
    result.append(verifyHeaders(resp, expected_map));
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: fromResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:122; JAXRS:JAVADOC:141; JAXRS:JAVADOC:123;
   * JAXRS:JAVADOC:124; JAXRS:JAVADOC:125;
   * 
   * @test_Strategy: Create an instance of Response using
   * Response.fromResponse(Response).build() verify that correct status code is
   * returned
   */
  @Test
  public void fromResponseTest() throws Fault {
    VerificationResult result = new VerificationResult();

    int status = 200;
    String content = "Test Only";
    List<String> type = Arrays.asList("text/plain", "text/html");
    List<String> encoding = Arrays.asList("gzip", "compress");

    MediaType mt1 = new MediaType("text", "plain");
    MediaType mt2 = new MediaType("text", "html");
    List<Variant> vts = getVariantList(encoding, mt1, mt2);

    for (int i = 0; i < vts.size(); i++) {
      Variant vt = vts.get(i);
      Response resp1 = Response.ok(content, vt).build();
      Response resp = Response.fromResponse(resp1).build();
      result.append(verifyContent(resp, content));
      result.append(verifyStatus(resp, status));
      result.append(verifyEncoding(resp, encoding));
      result.append(verifyLanguage(resp, getLangList()));
      result.append(verifyContentType(resp, type));
    }
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: entityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:139; JAXRS:JAVADOC:141; JAXRS:JAVADOC:146;
   * JAXRS:JAVADOC:123; JAXRS:JAVADOC:124; JAXRS:JAVADOC:125;
   * 
   * @test_Strategy: Create an instance of Response using
   * Response.ResponseBuilder.entity(String).build() verify that correct status
   * code is returned
   */
  @Test
  public void entityTest() throws Fault {
    VerificationResult result = new VerificationResult();

    int status = 200;
    String content = "Test Only";

    Response resp = Response.status(status).entity(content).build();
    result.append(verifyContent(resp, content));
    result.append(verifyStatus(resp, status));
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: languageTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:139; JAXRS:JAVADOC:141; JAXRS:JAVADOC:149;
   * JAXRS:JAVADOC:123; JAXRS:JAVADOC:124; JAXRS:JAVADOC:125;
   * 
   * @test_Strategy: Create an instance of Response using
   * Response.ResponseBuilder.language(String).build() verify that correct
   * status code is returned
   */
  @Test
  public void languageTest() throws Fault {
    VerificationResult result = new VerificationResult();

    int status = 200;
    List<String> lang = getLangList();

    for (String language : lang) {
      Response resp = Response.status(status).language(language).build();
      result.append(verifyStatus(resp, status));
      result.append(verifyLanguage(resp, lang));
    }
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: languageTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:139; JAXRS:JAVADOC:141; JAXRS:JAVADOC:150;
   * JAXRS:JAVADOC:123; JAXRS:JAVADOC:124; JAXRS:JAVADOC:125;
   * 
   * @test_Strategy: Create an instance of Response using
   * Response.ResponseBuilder.language(Locale).build() verify that correct
   * status code is returned
   */
  @Test
  public void languageTest1() throws Fault {
    VerificationResult result = new VerificationResult();
    int status = 200;

    for (String language : getLangList()) {
      Response resp = Response.status(status).language(language).build();
      result.append(verifyStatus(resp, status));
      result.append(verifyLanguage(resp, Arrays.asList(language)));
    }
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: typeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:139; JAXRS:JAVADOC:141; JAXRS:JAVADOC:158;
   * JAXRS:JAVADOC:123; JAXRS:JAVADOC:124; JAXRS:JAVADOC:125;
   * 
   * @test_Strategy: Create an instance of Response using
   * Response.ResponseBuilder.type(String).build() verify that correct status
   * code is returned
   */
  @Test
  public void typeTest() throws Fault {
    VerificationResult result = new VerificationResult();
    int status = 200;
    String type = MediaType.TEXT_PLAIN;

    Response resp = Response.status(status).type(type).build();
    result.append(verifyStatus(resp, status));
    result.append(verifyContentType(resp, Arrays.asList(type)));
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: typeTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:139; JAXRS:JAVADOC:141; JAXRS:JAVADOC:157;
   * JAXRS:JAVADOC:123; JAXRS:JAVADOC:124; JAXRS:JAVADOC:125;
   * 
   * @test_Strategy: Create an instance of Response using
   * Response.ResponseBuilder.type(MediaType).build() verify that correct status
   * code is returned
   */
  @Test
  public void typeTest1() throws Fault {
    VerificationResult result;

    int status = 200;
    List<String> types = Arrays.asList("text/plain", "text/html");

    MediaType mt1 = new MediaType("text", "plain");
    MediaType mt2 = new MediaType("text", "html");

    Response resp = Response.status(status).type(mt1).type(mt2).build();
    result = verifyStatus(resp, status);
    result.append(verifyContentType(resp, types));
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: tagTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:139; JAXRS:JAVADOC:141; JAXRS:JAVADOC:156;
   * JAXRS:JAVADOC:123; JAXRS:JAVADOC:124; JAXRS:JAVADOC:125;
   * 
   * @test_Strategy: Create an instance of Response using
   * Response.ResponseBuilder.tag(String).build() verify that correct status
   * code is returned
   */
  @Test
  public void tagTest1() throws Fault {
    VerificationResult result;

    int status = 200;
    String tag = "TestOnly";
    HashMap<String, String> expected_map = new HashMap<String, String>();
    expected_map.put("ETAG", tag);

    Response resp = Response.status(status).tag(tag).build();
    result = verifyStatus(resp, status);
    result.append(verifyHeaders(resp, expected_map));
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: tagTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:139; JAXRS:JAVADOC:141; JAXRS:JAVADOC:155;
   * JAXRS:JAVADOC:123; JAXRS:JAVADOC:124; JAXRS:JAVADOC:125;
   * 
   * @test_Strategy: Create an instance of Response using
   * Response.ResponseBuilder.tag(EntityTag).build() verify that correct status
   * code is returned
   */
  @Test
  public void tagTest2() throws Fault {
    VerificationResult result;
    int status = 200;
    EntityTag et1 = new EntityTag("StrongEntityTagTest", true);
    EntityTag et2 = new EntityTag("TestOnly", false);

    HashMap<String, String> expected_map = new HashMap<String, String>();
    expected_map.put("ETAG", "TestOnly");

    Response resp = Response.status(status).tag(et1).tag(et2).build();
    result = verifyStatus(resp, status);
    result.append(verifyHeaders(resp, expected_map));
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: variantTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:139; JAXRS:JAVADOC:141; JAXRS:JAVADOC:159;
   * JAXRS:JAVADOC:123; JAXRS:JAVADOC:124; JAXRS:JAVADOC:125;
   * 
   * @test_Strategy: Create an instance of Response using
   * Response.status(int).variant(Variant).build() verify that correct status
   * code is returned
   */
  @Test
  public void variantTest() throws Fault {
    VerificationResult result = new VerificationResult();
    Response resp = null;
    int status = 200;
    List<String> encoding = Arrays.asList("gzip", "compress");

    MediaType mt = new MediaType("text", "plain");
    List<Variant> vts = getVariantList(encoding, mt);

    for (int i = 0; i < vts.size(); i++) {
      Variant vt = vts.get(i);
      resp = Response.status(status).variant(vt).build();
      verifyStatus(resp, status);
      verifyEncoding(resp, encoding);
      verifyLanguage(resp, getLangList());
    }
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: variantsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:139; JAXRS:JAVADOC:141; JAXRS:JAVADOC:160;
   * JAXRS:JAVADOC:123; JAXRS:JAVADOC:124; JAXRS:JAVADOC:125;
   * 
   * @test_Strategy: Create an instance of Response using
   * Response.status(int).variants(List<Variant>).build() verify that correct
   * status code is returned
   */
  @Test
  public void variantsTest() throws Fault {
    VerificationResult result;
    Response resp = null;
    int status = 200;
    // String type = "text/plain";
    List<String> encoding = Arrays.asList("gzip", "compress");
    List<String> vars = Arrays.asList("accept-language", "accept-encoding");

    MediaType mt = new MediaType("text", "plain");
    List<Variant> vts = getVariantList(encoding, mt);
    resp = Response.status(status).variants(vts).build();
    result = verifyStatus(resp, status);
    result.append(verifyVary(resp, vars));
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: locationTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:139; JAXRS:JAVADOC:141; JAXRS:JAVADOC:152;
   * JAXRS:JAVADOC:123; JAXRS:JAVADOC:124; JAXRS:JAVADOC:125;
   * 
   * @test_Strategy: Create an instance of Response using
   * Response.status(status).location(URI).build() verify that correct status
   * code is returned
   */
  @Test
  public void locationTest() throws Fault {
    VerificationResult result = new VerificationResult();
    int status = 200;
    List<String> uri_expected = Arrays.asList("mailto:java-net@java.sun.com",
        "news:comp.lang.java", "urn:isbn:096139210x",
        "http://java.sun.com/j2se/1.3/",
        "docs/guide/collections/designfaq.html#28",
        "../../../demo/jfc/SwingSet2/src/SwingSet2.java", "file:///~/calendar");

    URI test_uri = null;
    for (String uri_string : uri_expected) {
      try {
        test_uri = new URI(uri_string);
      } catch (URISyntaxException ex) {
        result.message.append("Unexpected exception thrown:")
            .append(ex.getMessage());
        result.pass = false;
      }
      Response resp = Response.status(status).location(test_uri).build();

      HashMap<String, String> expected_map = new HashMap<String, String>();
      expected_map.put("Location", uri_string);
      result.append(verifyStatus(resp, status));
      result.append(verifyHeaders(resp, expected_map));
    }
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: contentLocationTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:139; JAXRS:JAVADOC:141; JAXRS:JAVADOC:144;
   * JAXRS:JAVADOC:123; JAXRS:JAVADOC:124; JAXRS:JAVADOC:125;
   * 
   * @test_Strategy: Create an instance of Response using
   * Response.status(status).contentLocation(URI).build() verify that correct
   * status code is returned
   */
  @Test
  public void contentLocationTest() throws Fault {
    VerificationResult result = new VerificationResult();
    int status = 200;
    List<String> uri_expected = Arrays.asList("mailto:java-net@java.sun.com",
        "news:comp.lang.java", "urn:isbn:096139210x",
        "http://java.sun.com/j2se/1.3/",
        "docs/guide/collections/designfaq.html#28",
        "../../../demo/jfc/SwingSet2/src/SwingSet2.java", "file:///~/calendar");

    URI test_uri = null;
    for (String uri_string : uri_expected) {
      try {
        test_uri = new URI(uri_string);
      } catch (URISyntaxException ex) {
        result.message.append("Unexpected exception thrown:")
            .append(ex.getMessage());
        result.pass = false;
      }
      Response resp = Response.status(status).contentLocation(test_uri).build();

      HashMap<String, String> expected_map = new HashMap<String, String>();
      expected_map.put("Content-Location", uri_string);
      result.append(verifyStatus(resp, 200));
      result.append(verifyHeaders(resp, expected_map));
    }
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: cacheControlTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:139; JAXRS:JAVADOC:141; JAXRS:JAVADOC:142;
   * JAXRS:JAVADOC:123; JAXRS:JAVADOC:124; JAXRS:JAVADOC:125;
   * 
   * @test_Strategy: Create an instance of Response using
   * Response.ResponseBuilder.cacheControl(String).build() verify that correct
   * status code is returned
   */
  @Test
  public void cacheControlTest() throws Fault {
    VerificationResult result;
    int status = 200;
    boolean nostore = true;

    CacheControl ccl4 = new CacheControl();
    ccl4.setNoStore(nostore);

    List<String> ccl = Arrays.asList("no-store", "no-transform");

    Response resp = Response.status(status).cacheControl(ccl4).build();
    result = verifyStatus(resp, status);
    result.append(verifyCacheControl(resp, ccl));
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: cookieTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:139; JAXRS:JAVADOC:141; JAXRS:JAVADOC:145;
   * JAXRS:JAVADOC:123; JAXRS:JAVADOC:124; JAXRS:JAVADOC:125;
   * 
   * @test_Strategy: Create an instance of Response using
   * Response.ResponseBuilder.cookie(NewCookie).build() verify that correct
   * status code is returned
   */
  @Test
  public void cookieTest() throws Fault {
    VerificationResult result;
    int status = 200;

    String name = "name_1";
    String value = "value_1";
    // int maxage = jakarta.ws.rs.core.NewCookie.DEFAULT_MAX_AGE;

    Cookie ck1 = new Cookie(name, value);
    NewCookie nck1 = new NewCookie(ck1);

    name = "name_2";
    value = "value_2";
    String path = "/acme";
    String domain = "";

    Cookie ck2 = new Cookie(name, value, path, domain);
    NewCookie nck2 = new NewCookie(ck2);

    name = "name_3";
    value = "value_3";
    path = "";
    domain = "y.x.foo.com";

    Cookie ck3 = new Cookie(name, value, path, domain);
    NewCookie nck3 = new NewCookie(ck3);

    List<String> cookies = Arrays.asList(nck1.toString().toLowerCase(),
        nck2.toString().toLowerCase(), nck3.toString().toLowerCase());

    Response resp = Response.status(status).cookie(nck1, nck2, nck3).build();
    result = verifyStatus(resp, status);
    result.append(verifyCookies(resp, cookies));
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: lastModifiedTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:139; JAXRS:JAVADOC:141; JAXRS:JAVADOC:151;
   * JAXRS:JAVADOC:123; JAXRS:JAVADOC:124; JAXRS:JAVADOC:125; JAXRS:JAVADOC:97;
   * 
   * @test_Strategy: Create an instance of Response using
   * Response.ResponseBuilder.lastModified(Date).build() verify that correct
   * status code is returned
   */
  @Test
  public void lastModifiedTest() throws Fault {
    VerificationResult result;
    int status = 200;
    long dt = 123456789;
    Date date = new Date(dt);
    HashMap<String, String> expected_map = new HashMap<String, String>();
    expected_map.put("Last-Modified", "123456789");

    Response resp = Response.status(status).lastModified(date).build();
    result = verifyStatus(resp, status);
    result.append(verifyHeaders(resp, expected_map));
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: headerTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:139; JAXRS:JAVADOC:141; JAXRS:JAVADOC:148;
   * JAXRS:JAVADOC:123; JAXRS:JAVADOC:124; JAXRS:JAVADOC:125; JAXRS:JAVADOC:97;
   * 
   * @test_Strategy: Create an instance of Response using
   * Response.ResponseBuilder.header(String, Object).build() verify that correct
   * status code is returned
   */
  @Test
  public void headerTest() throws Fault {
    VerificationResult result;
    int status = 200;
    List<String> type = Arrays.asList("text/plain", "text/html");
    List<String> encoding = Arrays.asList("gzip", "compress");

    String name = "name_1";
    String value = "value_1";
    Cookie ck1 = new Cookie(name, value);
    NewCookie nck1 = new NewCookie(ck1);

    List<String> cookies = Arrays.asList(nck1.toString().toLowerCase());

    Response resp = Response.status(status)
        .header(HttpHeaders.CONTENT_ENCODING, encoding.get(0))
        .header(HttpHeaders.CONTENT_ENCODING, encoding.get(1))
        .header("Content-Language", "en-US").header("Content-Language", "en-GB")
        .header("Content-Language", "zh-CN")
        .header("Cache-Control", "no-transform")
        .header("Set-Cookie", "name_1=value_1;version=1")
        .header(HttpHeaders.CONTENT_TYPE, type.get(0))
        .header(HttpHeaders.CONTENT_TYPE, type.get(1)).build();
    result = verifyStatus(resp, status);
    result.append(verifyEncoding(resp, encoding));
    result.append(verifyLanguage(resp, getLangList()));
    result.append(verifyContentType(resp, type));
    result.append(verifyCookies(resp, cookies));
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: cloneTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:139; JAXRS:JAVADOC:141; JAXRS:JAVADOC:143;
   * JAXRS:JAVADOC:123; JAXRS:JAVADOC:124; JAXRS:JAVADOC:125;
   * 
   * @test_Strategy: Create an instance of Response using
   * Response.ResponseBuilder.clone() verify that correct status code is
   * returned
   */
  @Test
  public void cloneTest() throws Fault {
    VerificationResult result;
    int status = 200;
    List<String> type = Arrays.asList("text/plain", "text/html");
    List<String> encoding = Arrays.asList("gzip");
    List<String> lang = getLangList();

    String name = "name_1";
    String value = "value_1";
    Cookie ck1 = new Cookie(name, value);
    NewCookie nck1 = new NewCookie(ck1);

    List<String> cookies = Arrays.asList(nck1.toString().toLowerCase());

    Response.ResponseBuilder respb1 = Response.status(status)
        .header("Content-type", "text/plain")
        .header("Content-type", "text/html").header("Content-Language", "en-US")
        .header("Content-Language", "en-GB").header("Content-Language", "zh-CN")
        .header("Cache-Control", "no-transform")
        .header("Set-Cookie", "name_1=value_1;version=1")
        .header(HttpHeaders.CONTENT_ENCODING, "gzip");
    Response.ResponseBuilder respb2;
    respb2 = respb1.clone();

    Response resp2 = respb2.build();
    result = verifyStatus(resp2, status);
    result.append(verifyEncoding(resp2, encoding));
    result.append(verifyLanguage(resp2, lang));
    result.append(verifyContentType(resp2, type));
    result.append(verifyCookies(resp2, cookies));

    String content = "TestOnly";
    Response resp1 = respb1.entity(content).cookie((NewCookie[]) null).build();
    result.append(verifyContent(resp1, content));
    result.append(verifyStatus(resp1, status));
    result.append(verifyEncoding(resp1, encoding));
    result.append(verifyLanguage(resp1, lang));
    result.append(verifyContentType(resp1, type));

    MultivaluedMap<java.lang.String, java.lang.Object> mvp = resp1
        .getMetadata();
    if (mvp.containsKey("Set-Cookie")) {
      result.pass = false;
      result.message.append("Response contains unexpected Set-Cookie: ")
          .append(mvp.getFirst("Set-Cookie").toString()).append(newline);
    }
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: statusTest5
   * 
   * @assertion_ids: JAXRS:JAVADOC:161;
   * 
   * @test_Strategy: Call Response.Status.fromStatusCode(int) verify that
   * correct Response.Status is returned
   */
  @Test
  public void statusTest5() throws Fault {
    VerificationResult result = new VerificationResult();
    Response.Status tmp = null;

    for (int i = 0; i < status_codes.length; i++) {
      tmp = Response.Status.fromStatusCode(status_codes[i]);

      if (tmp != resp_status[i]) {
        result.pass = false;
        result.message.append("fromStatusCode[").append(status_codes[i])
            .append("] failed with ").append(tmp).append(newline);
      }
    }
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: getFamilyTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:162;
   * 
   * @test_Strategy: Call Response.Status.getFamily() verify that correct
   * Response.Status.Family is returned
   */
  @Test
  public void getFamilyTest() throws Fault {
    VerificationResult result = new VerificationResult();
    Response.Status.Family tmp = null;

    assertTrue(status_family.length == Response.Status.values().length,
        "Response.Status.values() are unexpected");

    for (int i = 0; i < status_family.length; i++) {
      tmp = resp_status[i].getFamily();

      if (tmp != status_family[i]) {
        result.pass = false;
        result.message.append("getFamily failed with ").append(resp_status[i])
            .append(" ").append(tmp).append(newline);
      }
    }
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: statusTest7
   * 
   * @assertion_ids: JAXRS:JAVADOC:163;
   * 
   * @test_Strategy: Call Response.Status.getStatusCode() verify that correct
   * status code is returned
   */
  @Test
  public void statusTest7() throws Fault {
    VerificationResult result = new VerificationResult();
    int tmp = 0;

    for (int i = 0; i < status_codes.length; i++) {
      tmp = resp_status[i].getStatusCode();

      if (tmp != status_codes[i]) {
        result.pass = false;
        result.message.append("getStatusCode() failed with ")
            .append(resp_status[i]).append(newline);
        result.message.append("expecting ").append(status_codes[i])
            .append(", got ").append(tmp).append(newline);
      }
    }
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: toStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:164;
   * 
   * @test_Strategy: Call Response.Status.toString() verify that correct reason
   * phase is returned
   */
  @Test
  public void toStringTest() throws Fault {
    VerificationResult result = new VerificationResult();
    String tmp = null;

    for (int i = 0; i < resp_status.length; i++) {
      tmp = resp_status[i].toString();

      if (!tmp.equals(status[i])) {
        result.pass = false;
        result.message.append("Status.toString() failed with ")
            .append(resp_status[i]).append(newline);
        result.message.append("expecting ").append(status[i]).append(", got ")
            .append(tmp).append(newline);
      }
    }
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: getReasonPhraseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:301; JAXRS:JAVADOC:166;
   * 
   * @test_Strategy: Call Response.Status.getReasonPhrase() verify that correct
   * reason phase is returned
   */
  @Test
  public void getReasonPhraseTest() throws Fault {
    VerificationResult result = new VerificationResult();
    String tmp = null;

    assertTrue(status.length == Response.Status.values().length,
        "Response.Status.values() are unexpected");

    for (int i = 0; i < resp_status.length; i++) {
      tmp = resp_status[i].getReasonPhrase();

      if (!tmp.equals(status[i])) {
        result.pass = false;
        result.message.append("Status.toString() failed with ")
            .append(resp_status[i]).append(newline);
        result.message.append("expecting ").append(status[i]).append(", got ")
            .append(tmp).append(newline);
      }
    }
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: statusValueOfTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:165; JAXRS:JAVADOC:166;
   * 
   * @test_Strategy: Call Response.Status.valueOf(String) verify that correct
   * Status is returned
   */
  @Test
  public void statusValueOfTest() throws Fault {
    VerificationResult result = new VerificationResult();
    Response.Status tmp = null;
    for (int i = 0; i < resp_status.length; i++) {
      try {
        tmp = Response.Status.valueOf(
            status[i].replace(" ", "_").replace("-", "_").toUpperCase());
      } catch (Exception ex) {
        result.message.append("Exception thrown with status name ")
            .append(status[i]).append(newline);
        result.message.append(ex.getMessage());
        result.pass = false;
      }
      if (!tmp.equals(resp_status[i])) {
        result.pass = false;
        result.message.append("Status.toString() failed with ")
            .append(resp_status[i]).append(newline);
        result.message.append("expecting ").append(resp_status[i])
            .append(", got ").append(tmp).append(newline);
      }
    }
    logMsg(result);
    assertResultTrue(result);
  }

  /*
   * @testName: statusFamilyValueOfTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:167; JAXRS:JAVADOC:168;
   * 
   * @test_Strategy: Call Response.Status.Family.valueOf() verify that correct
   * Family is returned
   */
  @Test
  public void statusFamilyValueOfTest() throws Fault {
    Response.Status.Family[] families = Response.Status.Family.values();
    assertTrue(families.length == status_family_list.length,
        "Response.Status.Family.values() are unexpected");
    Arrays.sort(status_family_list);
    for (int i = 0; i != families.length; i++) {
      int match = Arrays.binarySearch(status_family_list,
          Response.Status.Family.valueOf(families[i].name()));
      assertTrue(match != -1, "Unknown Response Status Family"+ families[i]);
    }
  }

  /*
   * @testName: statusFamilyValuesTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:168;
   * 
   * @test_Strategy: Call Response.Status.Family.values() verify that correct
   * Family is returned
   */
  @Test
  public void statusFamilyValuesTest() throws Fault {
    Response.Status.Family[] families = Response.Status.Family.values();
    assertTrue(families.length == status_family_list.length,
        "Response.Status.Family.values() are unexpected");
    Arrays.sort(status_family_list);
    for (int i = 0; i != families.length; i++) {
      int match = Arrays.binarySearch(status_family_list, families[i]);
      assertTrue(match != -1, "Unknown Resposne Status Family"+ families[i]);
    }
  }

  /*
   * @testName: acceptedNoArgTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:836;
   * 
   * @test_Strategy: Create a new ResponseBuilder with an ACCEPTED status.
   */
  @Test
  public void acceptedNoArgTest() throws Fault {
    VerificationResult result;
    Response response = null;
    response = Response.accepted().build();
    result = verifyStatus(response, Status.ACCEPTED.getStatusCode());
    logMsg(result.message);
    assertResultTrue(result);
  }

  /*
   * @testName: acceptedStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:837;
   * 
   * @test_Strategy: Create a new ResponseBuilder with an ACCEPTED status that
   * contains a representation. It is the callers responsibility to wrap the
   * actual entity with GenericEntity if preservation of its generic type is
   * required.
   */
  @Test
  public void acceptedStringTest() throws Fault {
    VerificationResult result;
    String entity = "ENtiTy";
    Response response = null;
    response = Response.accepted(entity).build();
    result = verifyStatus(response, Status.ACCEPTED.getStatusCode());
    result.append(verifyContent(response, entity));
    logMsg(result.message);
    assertResultTrue(result);
  }

  /*
   * @testName: acceptedGenericEntityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:837;
   * 
   * @test_Strategy: Create a new ResponseBuilder with an ACCEPTED status that
   * contains a representation. It is the callers responsibility to wrap the
   * actual entity with GenericEntity if preservation of its generic type is
   * required.
   */
  @Test
  public void acceptedGenericEntityTest() throws Fault {
    VerificationResult result;
    String entity = "ENtiTy";
    GenericEntity<String> generic = new GenericEntity<String>(entity,
        String.class);
    Response response = Response.accepted(generic).build();
    result = verifyStatus(response, Status.ACCEPTED.getStatusCode());
    result.append(verifyContent(response, entity));
    logMsg(result.message);
    assertResultTrue(result);
  }

  /*
   * @testName: bufferEntityIgnoreNoBackingStreamTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:838;
   * 
   * @test_Strategy: In case the response entity instance is not backed by an
   * unconsumed input stream an invocation of bufferEntity method is ignored and
   * the method returns false.
   */
  @Test
  public void bufferEntityIgnoreNoBackingStreamTest() throws Fault {
    Response response = Response.ok().build();
    boolean result = response.bufferEntity();
    assertTrue(!result, "#bufferEntity() did not ignore no backing stream");
    logMsg("#bufferEntity did ignore no backing stream as expected");
  }

  /*
   * @testName: bufferEntityThrowsIllegalStateExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:838;
   * 
   * @test_Strategy: throws IllegalStateException - in case the response has
   * been #close() closed.
   */
  @Test
  public void bufferEntityThrowsIllegalStateExceptionTest() throws Fault {
    Response response = Response.ok().build();
    response.close();
    try {
      response.bufferEntity();
      fault("buffer entity did not throw IllegalStateException when closed");
    } catch (IllegalStateException e) {
      logMsg("#bufferEntity throws IllegalStateException as expected");
    }
  }

  /*
   * @testName: getAllowedMethodsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:843;
   * 
   * @test_Strategy: Get the allowed HTTP methods from the Allow HTTP header.
   */
  @Test
  public void getAllowedMethodsTest() throws Fault {
    Response response = Response.ok()
        .header(HttpHeaders.ALLOW, Request.POST.name())
        .header(HttpHeaders.ALLOW, Request.TRACE.name()).build();
    Set<String> set = response.getAllowedMethods();
    String methods = JaxrsUtil.iterableToString(";", set);
    assertContainsIgnoreCase(methods, Request.POST.name(), Request.POST.name(),
        "method has not been found");
    assertContainsIgnoreCase(methods, Request.TRACE.name(),
        Request.TRACE.name(), "method has not been found");
    assertTrue(
        methods.length() < Request.TRACE.name().length()
            + Request.POST.name().length() + 3,
        "Request contains some additional methods then expected"+ methods);
    logMsg("#getAllowedMethods returned expected methods", methods);
  }

  /*
   * @testName: getCookiesTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:844;
   * 
   * @test_Strategy: Get any new cookies set on the response message.
   */
  @Test
  public void getCookiesTest() throws Fault {
    NewCookie cookie1 = new NewCookie("c1", "v1");
    NewCookie cookie2 = new NewCookie("c2", "v2");
    List<String> cookies = Arrays.asList(cookie1.toString().toLowerCase(),
        cookie2.toString().toLowerCase());
    Response response = Response.ok().cookie(cookie1).cookie(cookie2).build();
    // verifyCookies style test
    VerificationResult result = verifyCookies(response, cookies);
    logMsg(result);
    assertResultTrue(result);
    // getCookies test
    Map<String, NewCookie> map = response.getCookies();
    for (Entry<String, NewCookie> entry : map.entrySet())
      if (entry.getKey().equals("c1"))
        assertTrue(entry.getValue().equals(cookie1), cookie1.toString()+
            "not match"+ entry.getValue());
      else if (entry.getKey().equals("c2"))
        assertTrue(entry.getValue().equals(cookie2), cookie2.toString()+
            "not match"+ entry.getValue());
      else
        assertTrue(false, "Got unknown cookie"+ entry.getKey());

    logMsg("#getCookies returned expected cookies");
  }

  /*
   * @testName: getCookiesIsImmutableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:844;
   * 
   * @test_Strategy: returns a read-only map of cookie name (String) to Cookie.
   */
  @Test
  public void getCookiesIsImmutableTest() throws Fault {
    NewCookie cookie1 = new NewCookie("c1", "v1");
    NewCookie cookie2 = new NewCookie("c2", "v2");
    Response response = Response.ok().cookie(cookie1).build();
    // getCookies test
    Map<String, NewCookie> map;
    try {
      map = response.getCookies();
      map.put("c2", cookie2);
    } catch (Exception e) {
      // can throw an exception or nothing or return a copy map
    }
    map = response.getCookies();
    assertTrue(!map.containsKey("c2"), "getCookies is not read-only returned"+
        map.get("c2"));
    logMsg("#getCookies is read-only as expected");
  }

  /*
   * @testName: getDateTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:845;
   * 
   * @test_Strategy: Get message date.
   */
  @Test
  public void getDateTest() throws Fault {
    Date date = Calendar.getInstance().getTime();
    Response response = Response.ok().header("Date", date).build();

    Date responseDate = response.getDate();
    assertTrue(date.equals(responseDate), "Original date"+ date+
        "and response#getDate()"+ responseDate+ "differs");
    logMsg("#getDate matches the Date HTTP header");
  }

  /*
   * @testName: getDateNotPresentTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:845;
   * 
   * @test_Strategy: Get null if not present.
   */
  @Test
  public void getDateNotPresentTest() throws Fault {
    Response response = Response.ok().build();
    Date responseDate = response.getDate();
    assertTrue(responseDate == null, "response#getDate() should be null, was"+
        responseDate);
    logMsg("#getDate is null as expected");
  }

  /*
   * @testName: getEntityThrowsIllegalStateExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:123;
   * 
   * @test_Strategy: if the entity was previously fully consumed as an
   * InputStream input stream, or if the response has been #close() closed.
   */
  @Test
  public void getEntityThrowsIllegalStateExceptionTest() throws Fault {
    Response response = Response.ok("entity").build();
    response.close();
    try {
      response.getEntity();
      fault("No exception has been thrown");
    } catch (IllegalStateException e) {
      logMsg("#getEntity throws IllegalStateException as expected", e);
    }
  }

  /*
   * @testName: getEntityTagTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:847;
   * 
   * @test_Strategy: Get the entity tag.
   */
  @Test
  public void getEntityTagTest() throws Fault {
    EntityTag tag = new EntityTag("getEntityTag");
    Response response = Response.notModified(tag).build();
    EntityTag responseTag = response.getEntityTag();
    assertTrue(tag.equals(responseTag), "response#getEntityTag()"+ responseTag+
        "is unequal to expected EntityTag"+ tag);
    logMsg("#getEntityTag is", responseTag, "as expected");
  }

  /*
   * @testName: getEntityTagNotPresentTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:847;
   * 
   * @test_Strategy: Get null if not present.
   */
  @Test
  public void getEntityTagNotPresentTest() throws Fault {
    Response response = Response.ok().build();
    EntityTag responseTag = response.getEntityTag();
    assertTrue(responseTag == null,
        "response#getEntityTag() should be null, was"+ responseTag);
    logMsg("#getEntityTag() is null as expected");
  }

  /*
   * @testName: getHeadersTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:848;
   * 
   * @test_Strategy: Get view of the response headers and their object values.
   */
  @Test
  public void getHeadersTest() throws Fault {
    CacheControl ccl = new CacheControl();
    NewCookie cookie = new NewCookie("cookie", "eikooc");
    String encoding = "gzip";
    Date date = Calendar.getInstance().getTime();

    Response response = Response.ok().cacheControl(ccl).cookie(cookie)
        .encoding(encoding).expires(date).language(Locale.CANADA_FRENCH)
        .build();
    logMsg("Found following objects:");
    logMsg((Object[]) JaxrsCommonClient.getMetadata(response.getHeaders()));

    MultivaluedMap<String, Object> headers = response.getHeaders();
    String header = null;

    header = headers.getFirst(HttpHeaders.CACHE_CONTROL).toString();
    assertContainsIgnoreCase(header, "no-transform",
        "Cache-Control:no-transform has not been found");

    header = headers.getFirst(HttpHeaders.SET_COOKIE).toString();
    assertContainsIgnoreCase(header, "cookie=eikooc",
        "Set-Cookie:cookie=eikooc has not been found");

    header = headers.getFirst(HttpHeaders.CONTENT_ENCODING).toString();
    assertContainsIgnoreCase(header, "gzip",
        "Content-Encoding:gzip has not been found");

    header = headers.getFirst(HttpHeaders.EXPIRES).toString();
    assertNotNull(header, "Expires has not been found");

    header = headers.getFirst(HttpHeaders.CONTENT_LANGUAGE).toString();
    assertContainsIgnoreCase(langToString(header),
        langToString(Locale.CANADA_FRENCH), "Content-Language:",
        langToString(Locale.CANADA_FRENCH), "has not been found");

    Object noHeader = headers.getFirst("unknown");
    assertNull(noHeader, "Unknown header has been found", header);
  }

  /*
   * @testName: getHeadersIsMutableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:848;
   * 
   * @test_Strategy: Get view of the response headers and their object values.
   * Changes in the underlying header data are reflected in this view.
   */
  @Test
  public void getHeadersIsMutableTest() throws Fault {
    String header = "header";
    Response response = Response.ok().build();
    MultivaluedMap<String, Object> headers = response.getHeaders();
    Object value = headers.getFirst(header);
    assertNull(value, "Unexpected header", header, ":", value);
    headers.add(header, header);
    headers = response.getHeaders();
    value = headers.getFirst(header);
    assertContainsIgnoreCase(value, header, "Unexpected header value", header,
        ":", value);
    logMsg("getHeaders is mutable");
  }

  /*
   * @testName: getHeaderStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:849;
   * 
   * @test_Strategy: Get a message header as a single string value.
   */
  @Test
  public void getHeaderStringTest() throws Fault {
    CacheControl ccl = new CacheControl();
    ccl.setNoStore(true);
    NewCookie cookie = new NewCookie("cookie", "eikooc");
    String encoding = "gzip";
    Date date = Calendar.getInstance().getTime();

    Response response = Response.ok().cacheControl(ccl).cookie(cookie)
        .encoding(encoding).expires(date).language(Locale.CANADA_FRENCH)
        .build();
    logMsg("Found following objects:");
    logMsg((Object[]) JaxrsCommonClient.getMetadata(response.getHeaders()));
    assertContainsIgnoreCase(
        response.getHeaderString(HttpHeaders.CACHE_CONTROL), "no-store",
        "Cache-Control:no-store has not been found");
    assertContainsIgnoreCase(
        response.getHeaderString(HttpHeaders.CACHE_CONTROL), "no-transform",
        "Cache-Control:no-transform has not been found");
    assertContainsIgnoreCase(response.getHeaderString(HttpHeaders.SET_COOKIE),
        "cookie=eikooc", "Set-Cookie:cookie=eikooc has not been found");
    assertContainsIgnoreCase(
        response.getHeaderString(HttpHeaders.CONTENT_ENCODING), "gzip",
        "Content-Encoding:gzip has not been found");
    assertNotNull(response.getHeaderString(HttpHeaders.EXPIRES),
        "Expires has not been found");
    assertContainsIgnoreCase(
        langToString(response.getHeaderString("Content-Language")),
        langToString(Locale.CANADA_FRENCH), "Content-Language:",
        langToString(Locale.CANADA_FRENCH), "has not been found");
    assertNull(response.getHeaderString("unknown"),
        "Unknown header has been found", response.getHeaderString("unknown"));
  }

  /*
   * @testName: getHeaderStringUsingHeaderDelegateTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:849;
   * 
   * @test_Strategy: Get a message header as a single string value. Each single
   * header value is converted to String using a RuntimeDelegate.HeaderDelegate
   * or using its toString
   */
  @Test
  public void getHeaderStringUsingHeaderDelegateTest() throws Fault {
    StringBean bean = new StringBean("s3");
    RuntimeDelegate original = RuntimeDelegate.getInstance();
    RuntimeDelegate.setInstance(new StringBeanRuntimeDelegate(original));
    try {
      Response response = Response.ok().header(bean.get(), bean).build();
      String header = response.getHeaderString(bean.get());
      assertContainsIgnoreCase(header, bean.get(), "Header", bean.get(),
          "has unexpected value", header);
      logMsg("HeaderDelegate is used for header", bean.get());
    } finally {
      RuntimeDelegate.setInstance(original);
      StringBeanRuntimeDelegate.assertNotStringBeanRuntimeDelegate();
    }
  }

  /*
   * @testName: getHeaderStringUsingToStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:849;
   * 
   * @test_Strategy: Get a message header as a single string value. Each single
   * header value is converted to String using a RuntimeDelegate.HeaderDelegate
   * or using its toString
   */
  @Test
  public void getHeaderStringUsingToStringTest() throws Fault {
    StringBuilder builder = new StringBuilder("s1");
    StringBuffer buffer = new StringBuffer("s2");
    Response response = Response.ok().header(builder.toString(), builder)
        .header(buffer.toString(), buffer).build();
    String header = response.getHeaderString(builder.toString());
    assertContainsIgnoreCase(header, builder.toString(), "Header", builder,
        "has unexpected value", header);

    header = response.getHeaderString(buffer.toString());
    assertContainsIgnoreCase(header, buffer.toString(), "Header", builder,
        "has unexpected value", header);

    logMsg("toString method is used as expected");
  }

  /*
   * @testName: getLanguageTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:850;
   * 
   * @test_Strategy: Get the language of the message entity.
   */
  @Test
  public void getLanguageTest() throws Fault {
    Response response = Response.ok().language(Locale.CANADA_FRENCH).build();
    Locale locale = response.getLanguage();
    assertTrue(Locale.CANADA_FRENCH.equals(locale), "Locale"+
        Locale.CANADA_FRENCH+ "does NOT match response#getLocale()"+ locale);
    logMsg("#getLocale matches the Content-Language HTTP header");
  }

  /*
   * @testName: getLanguageNotPresentTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:850;
   * 
   * @test_Strategy: Get null if not present.
   */
  @Test
  public void getLanguageNotPresentTest() throws Fault {
    Response response = Response.ok().build();
    Locale locale = response.getLanguage();
    assertTrue(locale == null, "response#getLanguage() should be null, was"+
        locale);
    logMsg("#getLanguage() is null as expected");
  }

  /*
   * @testName: getLastModifiedTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:851;
   * 
   * @test_Strategy: Get the last modified date.
   */
  @Test
  public void getLastModifiedTest() throws Fault {
    Date date = Calendar.getInstance().getTime();
    Response response = Response.ok().lastModified(date).build();
    Date responseDate = response.getLastModified();
    assertTrue(date.equals(responseDate), "Last Modified date"+ date+
        "does NOT match response#getLastModified()"+ responseDate);
    logMsg("#getLastModified matches the Last-Modified HTTP header");
  }

  /*
   * @testName: getLastModifiedNotPresentTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:851;
   * 
   * @test_Strategy: Get null if not present.
   */
  @Test
  public void getLastModifiedNotPresentTest() throws Fault {
    Response response = Response.ok().build();
    Date responseDate = response.getLastModified();
    assertTrue(responseDate == null,
        "response#getLastModified() should be null, was"+ responseDate);
    logMsg("#getLastModified() is null as expected");
  }

  /*
   * @testName: getLengthTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:852;
   * 
   * @test_Strategy: Get Content-Length value.
   */
  @Test
  public void getLengthTest() throws Fault {
    Response response = Response.ok("1234567890")
        .header(HttpHeaders.CONTENT_LENGTH, "10").build();
    int len = response.getLength();
    assertTrue(len > 9, "Expected Content-Length > 9"+
        "does NOT match response#getLength()"+ len);
    logMsg("#getLength matches expected Content-Length", len);
  }

  /*
   * @testName: getLengthNotPresentTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:852;
   * 
   * @test_Strategy: In other cases returns -1.
   */
  @Test
  public void getLengthNotPresentTest() throws Fault {
    Response response = Response.ok().build();
    int len = response.getLength();
    assertTrue(len == -1, "Expected Content-Length = -1"+
        "does NOT match response#getLength()"+ len);
    logMsg("#getLength matches expected Content-Length", len);
  }

  /*
   * @testName: getLinkTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:853;
   * 
   * @test_Strategy: Get the link for the relation.
   */
  @Test
  public void getLinkTest() throws Fault {
    Link link = createLink("path", "getLinkTest");
    Response response = Response.ok().links(link).build();
    Link responseLink = response.getLink("getLinkTest");
    assertTrue(link.equals(responseLink),
        "#getLink() returned unexpected Link"+ responseLink);
    logMsg("#getLink matches expected Link");
  }

  /*
   * @testName: getLinkNotPresentTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:853;
   * 
   * @test_Strategy: returns null if not present.
   */
  @Test
  public void getLinkNotPresentTest() throws Fault {
    Response response = Response.ok().build();
    Link responseLink = response.getLink("getLinkTest");
    assertTrue(responseLink == null, "#getLink() returned unexpected Link"+
        responseLink);
    logMsg("#getLink return null as expected");
  }

  /*
   * @testName: getLinkBuilderForTheRelationTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:854;
   * 
   * @test_Strategy: Convenience method that returns a Link.Builder for the
   * relation.
   */
  @Test
  public void getLinkBuilderForTheRelationTest() throws Fault {
    String rel = "anyrelation";
    Response response = Response.ok().link("http://abc.com/b/", rel).build();
    Link builderLink = response.getLinkBuilder("anyrelation").build();
    response = Response.ok().links(builderLink).build();
    Link responseLink = response.getLink("anyrelation");
    assertNotNull(responseLink, "#getLinkBuilder('relation') returned null");
    logMsg("#getLinkBuilder creates correct Link for given relation");
  }

  /*
   * @testName: getLinkBuilderForTheNotPresentRelationTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:854;
   * 
   * @test_Strategy: returns null if not present.
   */
  @Test
  public void getLinkBuilderForTheNotPresentRelationTest() throws Fault {
    Response response = Response.ok().build();
    Builder builder = response.getLinkBuilder("anyrelation");
    assertTrue(builder == null,
        "#getLinkBuilder('relation') returned unexpected builder"+ builder);
    logMsg("#getLinkBuilder returned null as expected");
  }

  /*
   * @testName: getLinksTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:855;
   * 
   * @test_Strategy: Get the links attached to the message as header.
   */
  @Test
  public void getLinksTest() throws Fault {
    Link link1 = createLink("path1", "rel1");
    Link link2 = createLink("path2", "rel2");
    Response response = Response.ok().links(link1, link2).build();
    Set<Link> responseLinks = response.getLinks();
    assertEqualsInt(responseLinks.size(), 2,
        "#getLinks() returned set of unexpected size", responseLinks.size());
    assertTrue(responseLinks.contains(link1), "#getLinks does not contain"+
        link1);
    assertTrue(responseLinks.contains(link2), "#getLinks does not contain"+
        link2);
    logMsg("#getLinks contains expected links");
  }

  /*
   * @testName: getLinksIsNotNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:855;
   * 
   * @test_Strategy: Does not return null.
   */
  @Test
  public void getLinksIsNotNullTest() throws Fault {
    Response response = Response.ok().build();
    Set<Link> responseLinks = response.getLinks();
    assertTrue(responseLinks != null, "#getLinks() returned null!");
    assertTrue(responseLinks.size() == 0,
        "#getLinks() returned non-empty map!");
    logMsg("#getLinks contains no links as expected");
  }

  /*
   * @testName: getLocationTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:856;
   * 
   * @test_Strategy: Get the location.
   */
  @Test
  public void getLocationTest() throws Fault {
    URI location = createUri("path");
    Response response = Response.ok().location(location).build();
    URI responseLocation = response.getLocation();
    assertTrue(responseLocation.equals(location), "#getLocation()"+
        responseLocation+ "differs from expected"+ location);
    logMsg("#getLocation contains expected location");
  }

  /*
   * @testName: getLocationNotPresentTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:856;
   * 
   * @test_Strategy: Get null when no present.
   */
  @Test
  public void getLocationNotPresentTest() throws Fault {
    Response response = Response.ok().build();
    URI responseLocation = response.getLocation();
    assertTrue(responseLocation == null, "#getLocation()"+ responseLocation+
        "should be null");
    logMsg("#getLocation returns null as expected");
  }

  /*
   * @testName: getMediaTypeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:857;
   * 
   * @test_Strategy: Get the media type of the message entity.
   */
  @Test
  public void getMediaTypeTest() throws Fault {
    Response response = Response.ok().type(MediaType.APPLICATION_ATOM_XML)
        .build();
    MediaType responseMedia = response.getMediaType();
    assertTrue(MediaType.APPLICATION_ATOM_XML_TYPE.equals(responseMedia),
        "#getMediaType()"+ responseMedia+ "differs from expected"+
        MediaType.APPLICATION_ATOM_XML);
    logMsg("#getMediaType returned expected MediaType");
  }

  /*
   * @testName: getMediaTypeNoMediaTypeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:857;
   * 
   * @test_Strategy: null if there is no response entity.
   */
  @Test
  public void getMediaTypeNoMediaTypeTest() throws Fault {
    Response response = Response.ok().build();
    MediaType responseMedia = response.getMediaType();
    assertTrue(responseMedia == null, "#getMediaType()"+ responseMedia+
        "should be null");
    logMsg("#getMediaType returned null as expected");
  }

  /*
   * @testName: getStatusInfoTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:858;
   * 
   * @test_Strategy: Get the complete status information associated with the
   * response.
   */
  @Test
  public void getStatusInfoTest() throws Fault {
    for (Status status : Status.values()) {
      Response response = Response.status(status).build();
      StatusType info = response.getStatusInfo();
      assertTrue(info.getStatusCode() == status.getStatusCode(),
          "#getStatusInfo returned unexpected value"+ info);
    }
    logMsg("#getStatusInfo returned expected StatusTypes");
  }

  /*
   * @testName: getStringHeadersUsingToStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:859;
   * 
   * @test_Strategy: Get view of the response headers and their string values.
   * Each single header value is converted to String using a
   * RuntimeDelegate.HeaderDelegate or using its toString
   */
  @Test
  public void getStringHeadersUsingToStringTest() throws Fault {
    RuntimeDelegate original = RuntimeDelegate.getInstance();
    RuntimeDelegate.setInstance(new StringBeanRuntimeDelegate(original));
    try {
      StringBuilder builder = new StringBuilder("s1");
      StringBuffer buffer = new StringBuffer("s2");
      StringBean bean = new StringBean("s3");
      Response response = Response.ok().header(builder.toString(), builder)
          .header(buffer.toString(), buffer).header(bean.get(), bean).build();
      MultivaluedMap<String, String> headers = response.getStringHeaders();
      String header = headers.getFirst(builder.toString());
      assertContainsIgnoreCase(header, builder.toString(), "Header", builder,
          "has unexpected value", header);

      header = headers.getFirst(buffer.toString());
      assertContainsIgnoreCase(header, buffer.toString(), "Header", builder,
          "has unexpected value", header);

      logMsg("#getStringHeaders contains expected values",
          JaxrsUtil.iterableToString(",", headers.entrySet()));
    } finally {
      RuntimeDelegate.setInstance(original);
      StringBeanRuntimeDelegate.assertNotStringBeanRuntimeDelegate();
    }
  }

  /*
   * @testName: getStringHeadersUsingHeaderDelegateTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:859;
   * 
   * @test_Strategy: Get view of the response headers and their string values.
   * Each single header value is converted to String using a
   * RuntimeDelegate.HeaderDelegate or using its toString
   */
  @Test
  public void getStringHeadersUsingHeaderDelegateTest() throws Fault {
    RuntimeDelegate original = RuntimeDelegate.getInstance();
    RuntimeDelegate.setInstance(new StringBeanRuntimeDelegate(original));
    try {
      StringBuilder builder = new StringBuilder("s1");
      StringBuffer buffer = new StringBuffer("s2");
      StringBean bean = new StringBean("s3");
      Response response = Response.ok().header(builder.toString(), builder)
          .header(buffer.toString(), buffer).header(bean.get(), bean).build();
      MultivaluedMap<String, String> headers = response.getStringHeaders();
      String header = headers.getFirst(bean.get());
      assertContainsIgnoreCase(bean.get(), header, "Header", bean.get(),
          "has unexpected value", header);

      logMsg("#getStringHeaders contains expected values",
          JaxrsUtil.iterableToString(",", headers.entrySet()));
    } finally {
      RuntimeDelegate.setInstance(original);
      StringBeanRuntimeDelegate.assertNotStringBeanRuntimeDelegate();
    }
  }

  /*
   * @testName: hasEntityWhenEntityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:860;
   * 
   * @test_Strategy: Check if there is an entity available in the response.
   */
  @Test
  public void hasEntityWhenEntityTest() throws Fault {
    Response response = Response.ok("entity").build();
    assertTrue(response.hasEntity(), "#hasEntity did not found the entity");
    logMsg("#hasEntity found the entity as expected");
  }

  /*
   * @testName: hasEntityWhenNoEntityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:860;
   * 
   * @test_Strategy: Check if there is an entity available in the response.
   */
  @Test
  public void hasEntityWhenNoEntityTest() throws Fault {
    Response response = Response.ok().build();
    assertTrue(!response.hasEntity(), "#hasEntity did found the entity");
    logMsg("#hasEntity has not found any entity as expected");
  }

  /*
   * @testName: hasEntityThrowsIllegalStateExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:860;
   * 
   * @test_Strategy: throws java.lang.IllegalStateException - in case the
   * response has been closed.
   */
  @Test
  public void hasEntityThrowsIllegalStateExceptionTest() throws Fault {
    Response response = Response.ok().build();
    response.close();
    try {
      response.hasEntity();
      fault("No exception has been thrown");
    } catch (IllegalStateException e) {
      logMsg("IllegalStateException has been thrown as expected");
    }

  }

  /*
   * @testName: hasLinkWhenLinkTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:862;
   * 
   * @test_Strategy: Check if link for relation exists.
   */
  @Test
  public void hasLinkWhenLinkTest() throws Fault {
    Link link = createLink("path", "rel");
    Response response = Response.ok().links(link).build();
    assertTrue(response.hasLink("rel"), "#hasLink did not found a Link");
    logMsg("#hasEntity found the Link as expected");
  }

  /*
   * @testName: hasLinkWhenNoLinkTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:862;
   * 
   * @test_Strategy: Check if link for relation exists.
   */
  @Test
  public void hasLinkWhenNoLinkTest() throws Fault {
    Response response = Response.ok().build();
    assertTrue(!response.hasLink("rel"), "#has Link did found some Link");
    logMsg("#hasLink has not found any Link as expected");
  }

  // /////////////////////////////////////////////////////////////////////////

  protected VerificationResult verifyContent(Response response,
      String content) {
    VerificationResult result = new VerificationResult();
    if ((content == null) || (content == "")) {
      if (!(response.getEntity() == null)
          || (response.getEntity().equals(""))) {
        result.pass = false;
        result.message.append(indent)
            .append("Entity verification failed: expecting no content, got ")
            .append(response.getEntity()).append(newline);
      }
    } else if (!content.equals(response.getEntity())) {
      result.pass = false;
      result.message.append(indent)
          .append("Entity verification failed: expecting ").append(content)
          .append(", got ").append(response.getEntity()).append(newline);
    } else {
      result.message.append(indent)
          .append("Correct content found in Response: ")
          .append(response.getEntity()).append(newline);
    }
    result.message.append(newline);
    return result;
  }

  protected VerificationResult verifyStatus(Response response, int status) {
    VerificationResult result = new VerificationResult();
    if (response.getStatus() != status) {
      result.pass = false;
      result.message.append(indent)
          .append("Status code verification failed: expecting ").append(status)
          .append(", got ").append(response.getStatus()).append(newline);
    } else {
      result.message.append(indent).append("Correct status found in Response: ")
          .append(status).append(newline);
    }
    result.message.append(newline);
    return result;
  }

  protected VerificationResult verifyHeaders(Response response,
      HashMap<String, String> expected) {
    MultivaluedMap<java.lang.String, java.lang.Object> headers = response
        .getMetadata();
    VerificationResult result = new VerificationResult();
    result.message.append("========== Verifying a Response with Map: ")
        .append(newline);
    for (String key_actual : headers.keySet()) {
      result.message.append(indent).append("Response contains key: ")
          .append(key_actual).append(newline);
    }
    result.message.append(indent)
        .append("Verifying the following keys in Response:").append(newline);
    String actual;

    for (Entry<String, String> entry : expected.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();

      if (!headers.containsKey(key)) {
        result.pass = false;
        result.message.append(indent).append(indent).append("Key: ").append(key)
            .append(" is not found in Response;").append(newline);
      } else if (key.equalsIgnoreCase("last-modified")) {
        result.message.append(indent).append(indent)
            .append("Key Last-Modified is found in response").append(newline);
      } else {
        actual = headers.getFirst(key).toString().toLowerCase();

        if (actual.startsWith("\"") && actual.endsWith("\""))
          actual = actual.substring(1, actual.length() - 1);

        if (!actual.equalsIgnoreCase(value)) {
          result.pass = false;
          result.message.append(indent).append(indent);
          result.message.append("Key: ").append(key);
          result.message.append(" found in Response, but with different value;")
              .append(newline);
          result.message.append(indent).append(indent).append("Expecting ")
              .append(value).append("; got ").append(headers.getFirst(key))
              .append(newline);
        }
        result.message.append(indent).append(indent).append("Processed key ")
            .append(key).append(" with expected value ").append(value)
            .append(newline);
      }
    }
    result.message.append(newline);
    return result;
  }

  protected VerificationResult verifyEncoding(Response response,
      List<String> encoding) {
    VerificationResult result = new VerificationResult();
    List<Object> enc = response.getHeaders().get(HttpHeaders.CONTENT_ENCODING);
    if (enc == null) {
      result.pass = false;
      result.message.append(HttpHeaders.CONTENT_ENCODING)
          .append(" headers is null").append(newline);
      result.message.append("Headers :").append(response.getHeaders())
          .append(newline);
    } else
      for (Object e : enc)
        if (!encoding.contains(e.toString().toLowerCase())) {
          result.pass = false;
          result.message.append(indent).append(indent).append("Encoding ")
              .append(e).append(" was not found in headers")
              .append(response.getHeaders()).append(newline);
        } else
          result.message.append(indent).append("Encoding ").append(e)
              .append(" was found").append(newline);
    result.message.append(newline);
    return result;
  }

  protected VerificationResult verifyLanguage(Response response,
      List<String> languages) {
    VerificationResult result = new VerificationResult();
    List<Object> responseLangs = response.getHeaders()
        .get(HttpHeaders.CONTENT_LANGUAGE); // one only
    if (responseLangs == null) {
      result.pass = false;
      result.message.append(HttpHeaders.CONTENT_LANGUAGE)
          .append(" headers is null").append(newline);
      result.message.append("Headers :").append(response.getHeaders())
          .append(newline);
    } else {
      String lang = langToString(JaxrsUtil.iterableToString(" ", languages))
          .toLowerCase();
      for (Object responseLang : responseLangs)
        if (!lang.contains(langToString(responseLang).toLowerCase())) {
          result.pass = false;
          result.message.append(indent).append(indent)
              .append("language test failed: ").append(responseLang)
              .append(" is not expected in Response")
              .append(response.getHeaders()).append(newline);
          for (String tt : languages) {
            result.message.append(indent).append(indent)
                .append("Expecting Content-Language ").append(tt)
                .append(newline);
          }
        } else
          result.message.append(indent).append("Content Language ").append(lang)
              .append(" was found").append(newline);
    }
    result.message.append(newline);
    return result;
  }

  protected VerificationResult verifyContentType(Response response,
      List<String> type) {
    VerificationResult result = new VerificationResult();
    List<Object> enc = response.getHeaders().get(HttpHeaders.CONTENT_TYPE);
    if (enc == null) {
      result.pass = false;
      result.message.append(HttpHeaders.CONTENT_TYPE).append(" headers is null")
          .append(newline);
      result.message.append("Headers :").append(response.getHeaders())
          .append(newline);
    } else
      for (Object e : enc)
        if (!type.contains(e.toString().toLowerCase())) {
          result.pass = false;
          result.message.append(indent).append(indent).append("Content-Type ")
              .append(e).append(" was not found in headers")
              .append(response.getHeaders()).append(newline);
        } else
          result.message.append(indent).append("Content Type ").append(e)
              .append(" was found").append(newline);
    result.message.append(newline);
    return result;
  }

  protected VerificationResult verifyVary(Response response, List<String> var) {
    VerificationResult result = new VerificationResult();
    if (var != null)
      for (Entry<String, List<Object>> entry : response.getMetadata()
          .entrySet())
        if (entry.getKey().equalsIgnoreCase("Vary"))
          for (String value : var) {
            String actual = entry.getValue().toString().toLowerCase();
            if (actual.indexOf(value.toLowerCase()) < 0) {
              result.pass = false;
              result.message.append(indent).append(indent)
                  .append("Expected header ").append(value)
                  .append(" not set in Vary.").append(newline);
            } else {
              result.message.append(indent).append(indent)
                  .append("Found expected header ").append(value).append(".")
                  .append(newline);
            }
          }
    result.message.append(newline);
    return result;
  }

  protected VerificationResult verifyCacheControl(Response response,
      List<String> ccl) {
    VerificationResult result = new VerificationResult();
    if (ccl != null) {
      for (String tt : ccl)
        result.message.append("Expecting Cache-Control ").append(tt)
            .append(newline);
      for (Entry<String, List<Object>> entry : response.getMetadata()
          .entrySet())
        if (entry.getKey().equalsIgnoreCase("Cache-Control"))
          for (Object all_ccl : entry.getValue())
            for (String cc : ccl)
              if (!(all_ccl.toString().toLowerCase()
                  .indexOf(cc.toLowerCase()) > -1)) {
                result.pass = false;
                result.message.append(indent).append(indent)
                    .append("Cache-Control test failed: ").append(cc)
                    .append(" is not found in Response.").append(newline);
              }
    }
    result.message.append(newline);
    return result;
  }

  protected VerificationResult verifyCookies(Response response,
      List<String> cookies) {
    VerificationResult result = new VerificationResult();
    if (cookies != null) {
      for (String tt : cookies)
        result.message.append(indent).append(indent)
            .append("Expecting Set-Cookie").append(tt).append(newline);
      for (Entry<String, List<Object>> entry : response.getMetadata()
          .entrySet()) {
        if (entry.getKey().equalsIgnoreCase("Set-Cookie"))
          for (Object nck_actual : entry.getValue()) {
            result.message.append(indent).append(indent).append("Processing ")
                .append(nck_actual.toString()).append(newline);
            if (!cookies.contains(
                nck_actual.toString().toLowerCase().replace(" ", ""))) {
              result.pass = false;
              result.message.append(indent).append(indent)
                  .append("Set-Cookie test failed: ").append(nck_actual)
                  .append(" is not expected in Response.").append(newline);
            } else {
              result.message.append(indent).append(indent)
                  .append("Expected Set-Cookie: ").append(nck_actual)
                  .append(" is found in Response.").append(newline);
            }
          }
      }
    }
    result.message.append(newline);
    return result;
  }

   protected static void assertResultTrue(VerificationResult result)   {
    assertTrue(result.pass, "At least one assertion failed");
  }

  protected static List<String> getLangList() {
    return Arrays.asList("en-US", "en-GB", "zh-CN");
  }

  protected static List<Variant> getVariantList(List<String> encoding,
      MediaType... mt) {
    return Variant.VariantListBuilder.newInstance().mediaTypes(mt)
        .languages(new Locale("en", "US"), new Locale("en", "GB"),
            new Locale("zh", "CN"))
        .encodings(encoding.toArray(new String[0])).add().build();
  }

  protected static String langToString(Object object) {
    Locale locale = null;
    if (object instanceof List)
      object = ((List<?>) object).iterator().next();
    if (object instanceof Locale)
      locale = (Locale) object;
    String value = locale == null ? object.toString() : locale.toString();
    return value.replace("_", "-");
  }

  protected static Link createLink(String path, String rel) throws Fault {
    return Link.fromUri(createUri(path)).rel(rel).build();
  }

  protected static URI createUri(String path) throws Fault {
    URI uri;
    try {
      uri = new URI("http://localhost.tck:888/url404/" + path);
    } catch (URISyntaxException e) {
      throw new Fault(e);
    }
    return uri;
  }
}
