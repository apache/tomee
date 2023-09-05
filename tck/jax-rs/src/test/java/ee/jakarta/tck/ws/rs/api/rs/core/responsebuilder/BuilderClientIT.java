/*
 * Copyright (c) 2011, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.api.rs.core.responsebuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import ee.jakarta.tck.ws.rs.api.rs.core.responseclient.JAXRSClientIT;
import ee.jakarta.tck.ws.rs.api.rs.core.responseclient.VerificationResult;
import ee.jakarta.tck.ws.rs.common.impl.SinglevaluedMap;
import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Variant;
import jakarta.ws.rs.ext.RuntimeDelegate;

public class BuilderClientIT
    extends JAXRSClientIT {

  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : "+testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : "+testInfo.getDisplayName());
  }
  
  /*
   * @testName: statusTest1
   *
   * @assertion_ids: JAXRS:JAVADOC:131; JAXRS:JAVADOC:153; JAXRS:JAVADOC:141;
   * JAXRS:JAVADOC:123; JAXRS:JAVADOC:125; JAXRS:JAVADOC:124;
   *
   * @test_Strategy: Create an instance of ResponseBuilder Response.ok();
   * Setting status using ResponseBuilder.status(int); verify that correct
   * status code is returned
   */
  @Test
  public void statusTest1() throws Fault {
    VerificationResult result = new VerificationResult();
    Response resp = null;
    ResponseBuilder respb = null;
    for (int status : status_codes) {
      respb = Response.ok();
      respb = respb.status(status);
      resp = respb.build();
      result.append(verifyStatus(resp, status));
    }
    logMsg(result);
    assertTrue(result.pass);
  }

  /*
   * @testName: statusTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:131; JAXRS:JAVADOC:154; JAXRS:JAVADOC:141;
   * JAXRS:JAVADOC:123; JAXRS:JAVADOC:124; JAXRS:JAVADOC:125;
   * 
   * @test_Strategy: Create an instance of ResponseBuilder Response.ok();
   * Setting status using ResponseBuilder.status(Status); verify that correct
   * status code is returned
   */
  @Test
  public void statusTest2() throws Fault {
    VerificationResult result = new VerificationResult();
    Response resp = null;
    ResponseBuilder respb = null;
    for (int i = 0; i < status_codes.length - 1; i++) {
      respb = Response.ok();
      respb = respb.status(resp_status[i]);
      resp = respb.build();
      result.append(verifyStatus(resp, status_codes[i]));
    }
    logMsg(result);
    assertTrue(result.pass);
  }

  /*
   * @testName: expiresTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:147;
   * 
   * @test_Strategy: Set Expires to ResponseBuilder, build a response and check
   * expires timestamp.
   */
  @Test
  public void expiresTest() throws Fault {
    Date now = Calendar.getInstance().getTime();
    ResponseBuilder rs = Response.ok();
    rs.expires(now);
    Response response = rs.build();
    MultivaluedMap<String, Object> metadata = response.getMetadata();
    if (metadata == null)
      fail("No metadata in response");
    List<Object> expires = response.getMetadata().get("Expires");
    if (expires == null || expires.isEmpty())
      fail("No Expires property in metadata");
    boolean condition = false;
    Object fetched = expires.iterator().next();
    if (Date.class.isInstance(fetched))
      condition = ((Date) fetched).compareTo(now) == 0;
    else if (String.class.isInstance(fetched))
      condition = formats(now).contains(fetched.toString());
    else
      fail("Fetched object not recognised");

    assertTrue(condition, "Expires value not matched, set: " + now.toString()+
        "fetched:"+ fetched.toString());
    logMsg("Set and fetched expire dates matched");
  }

  /*
   * @testName: allowStringArrayTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:875;
   * 
   * @test_Strategy: Set the list of allowed methods for the resource.
   */
  @Test
  public void allowStringArrayTest() throws Fault {
    String[] methods = { Request.OPTIONS.name(), Request.TRACE.name() };
    ResponseBuilder rb = RuntimeDelegate.getInstance().createResponseBuilder();
    Response response = rb.allow(methods).build();
    Set<String> set = response.getAllowedMethods();
    String responseMethods = JaxrsUtil.iterableToString(" ", set);

    for (String method : methods) {
      assertContains(responseMethods, method, "Expected allow method", method,
          "was not found in response allowed methods", responseMethods);
      logMsg("Found expected allowed method", method);
    }
  }

  /*
   * @testName: allowStringArrayTruncateDuplicatesTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:875;
   * 
   * @test_Strategy: Set the list of allowed methods for the resource.
   */
  @Test
  public void allowStringArrayTruncateDuplicatesTest() throws Fault {
    String[] methods = { Request.OPTIONS.name(), Request.OPTIONS.name() };
    ResponseBuilder rb = RuntimeDelegate.getInstance().createResponseBuilder();
    Response response = rb.allow(methods).build();
    Set<String> set = response.getAllowedMethods();
    assertEqualsInt(1, set.size(), "Only one allow method should be present");
    assertEquals(set.iterator().next(), Request.OPTIONS.name(),
        Request.OPTIONS.name(), "has not been found in allowed methods");
    logMsg(Request.OPTIONS.name(), "has been found in allowed methods");
  }

  /*
   * @testName: allowStringArrayNullRemovesAllTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:875;
   * 
   * @test_Strategy: if null any existing allowed method list will be removed.
   */
  @Test
  public void allowStringArrayNullRemovesAllTest() throws Fault {
    String[] methods = { Request.OPTIONS.name(), Request.GET.name() };
    ResponseBuilder rb = RuntimeDelegate.getInstance().createResponseBuilder();
    Response response = rb.allow(methods).allow((String[]) null).build();
    Set<String> set = response.getAllowedMethods();
    assertEqualsInt(0, set.size(), "No one allow method should be present");
    logMsg("Allowed methods has been removed by null value as expected");
  }

  /*
   * @testName: allowStringSetTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:876;
   * 
   * @test_Strategy: Set the list of allowed methods for the resource.
   */
  @Test
  public void allowStringSetTest() throws Fault {
    Set<String> methods = new TreeSet<String>();
    methods.add(Request.OPTIONS.name());
    methods.add(Request.TRACE.name());

    ResponseBuilder rb = RuntimeDelegate.getInstance().createResponseBuilder();
    Response response = rb.allow(methods).build();
    Set<String> set = response.getAllowedMethods();
    String responseMethods = JaxrsUtil.iterableToString(" ", set);

    for (String method : methods) {
      assertContains(responseMethods, method, "Expected allow method", method,
          "was not found in response allowed methods", responseMethods);
      logMsg("Found expected allowed method", method);
    }
  }

  /*
   * @testName: allowStringSetNullRemovesAllTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:876;
   * 
   * @test_Strategy: if null any existing allowed method list will be removed.
   */
  @Test
  public void allowStringSetNullRemovesAllTest() throws Fault {
    Set<String> methods = new TreeSet<String>();
    methods.add(Request.OPTIONS.name());
    methods.add(Request.TRACE.name());

    ResponseBuilder rb = RuntimeDelegate.getInstance().createResponseBuilder();
    Response response = rb.allow(methods).allow((Set<String>) null).build();
    Set<String> set = response.getAllowedMethods();
    assertEqualsInt(0, set.size(), "No one allow method should be present");
    logMsg("Allowed methods has been removed by null value as expected");
  }

  /*
   * @testName: encodingTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:878;
   * 
   * @test_Strategy: Set the message entity content encoding.
   */
  @Test
  public void encodingTest() throws Fault {
    String[] encodings = { "gzip", "ccitt", "pic" };
    VerificationResult vr = new VerificationResult();
    for (String encoding : encodings) {
      Response response = Response.ok().encoding(encoding).build();
      vr.append(verifyEncoding(response, Collections.singletonList(encoding)));
    }
    logMsg(vr.message);
    assertTrue(vr.pass);
    logMsg("Found expected encodings");
  }

  /*
   * @testName: linkUriStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:880;
   * 
   * @test_Strategy: Add a link header.
   */
  @Test
  public void linkUriStringTest() throws Fault {
    URI uri = null;
    try {
      uri = new URI(URL);
    } catch (URISyntaxException e) {
      fail(e.getMessage());
    }
    String rel = "REL";
    Response response = Response.ok().link(uri, rel).build();
    Link link = response.getLink(rel);
    assertTrue(link != null, "link is null");
    assertTrue(link.toString().contains(URL), "link"+ link+
        "does not contain expected"+ URL);
    logMsg("Found expected link", link);
  }

  /*
   * @testName: linkStringStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:881;
   * 
   * @test_Strategy: Add a link header.
   */
  @Test
  public void linkStringStringTest() throws Fault {
    String rel = "REL";
    Response response = Response.ok().link(URL, rel).build();
    Link link = response.getLink(rel);
    assertTrue(link != null, "link is null");
    assertTrue(link.toString().contains(URL), "link"+ link+
        "does not contain expected"+ URL);
    logMsg("Found expected link", link);
  }

  /*
   * @testName: linksTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:882;
   * 
   * @test_Strategy: Add one or more link headers.
   */
  @Test
  public void linksTest() throws Fault {
    String rel = "REL";
    Link link1 = Link.fromUri(URL).rel(rel + "1").build();
    Link link11 = Link.fromUri(URL).rel(rel + "11").build();
    Link link2 = Link.fromUri(URL + "/link2").rel(rel + "2").build();

    Response response = Response.ok().links(link1, link11, link2).build();
    Link link = response.getLink(rel + "1");
    assertTrue(link != null, "link is null");
    assertTrue(link.toString().contains(URL), "link"+ link+
        "does not contain expected"+ URL);
    link = response.getLink(rel + "11");
    assertTrue(link != null, "link is null");
    assertTrue(link.toString().contains(URL), "link"+ link+
        "does not contain expected"+ URL);
    link = response.getLink(rel + "2");
    assertTrue(link != null, "link is null");
    assertTrue(link.toString().contains(URL + "/link2"), "link"+ link+
        "does not contain expected"+ URL + "/link2");

    logMsg("Found expected links");
  }

  /*
   * @testName: replaceAllTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:883;
   * 
   * @test_Strategy: Replaces all existing headers with the newly supplied
   * headers.
   */
  @Test
  public void replaceAllTest() throws Fault {
    String[] headers = { "header1", "header2", "header3" };
    String header99 = "header99";
    MultivaluedMap<String, Object> mv = new SinglevaluedMap<String, Object>();
    mv.add(header99, header99);
    Response response = Response.ok().header(headers[0], headers[0])
        .header(headers[1], headers[1]).header(headers[2], headers[2])
        .replaceAll(mv).build();
    for (String header : headers)
      assertTrue(response.getHeaderString(header) == null,
          "response contains non replaced header" + header);

    assertTrue(response.getHeaderString(header99).equals(header99),
        "response does not contain header from replacedAll map"+ header99);
  }

  /*
   * @testName: replaceAllByNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:883;
   * 
   * @test_Strategy: Replaces all existing headers with the newly supplied
   * headers. if null all existing headers will be removed.
   */
  @Test
  public void replaceAllByNullTest() throws Fault {
    String[] headers = { "header1", "header2", "header3" };
    Response response = Response.ok().header(headers[0], headers[0])
        .header(headers[1], headers[1]).header(headers[2], headers[2])
        .replaceAll(null).build();
    for (String header : headers)
      assertTrue(response.getHeaderString(header) == null,
          "response contains non replaced header"+ header);
  }

  /*
   * @testName: variantsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:884;
   * 
   * @test_Strategy: Add a Vary header that lists the available variants.
   */
  @Test
  public void variantsTest() throws Fault {
    List<String> encoding = Arrays.asList("gzip", "compress");
    List<String> vars = Arrays.asList(HttpHeaders.ACCEPT_LANGUAGE,
        HttpHeaders.ACCEPT_ENCODING);
    MediaType mt = MediaType.APPLICATION_JSON_TYPE;
    ResponseBuilder rb = Response.ok();
    rb = rb.variants(getVariantList(encoding, mt).toArray(new Variant[0]));
    Response response = rb.build();
    VerificationResult result = new VerificationResult();
    result.append(verifyVary(response, vars));
    logMsg(result.message);
    assertTrue(result.pass);
  }

  // //////////////////////////////////////////////////////////////////

  private static String formats(Date date) {
    DateFormat format;
    TestUtil.logMsg("Creating possible string format list");
    StringBuilder sb = new StringBuilder();
    for (String tz : TimeZone.getAvailableIDs()) {
      format = JaxrsUtil.createDateFormat(TimeZone.getTimeZone(tz));
      sb.append(format.format(date));
    }
    return sb.toString();
  }

  protected Invocation.Builder invocation() {
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(URL);
    Builder b = target.request();
    return b;
  }

  static final String URL = "http://localhost:888/noUrl";
}