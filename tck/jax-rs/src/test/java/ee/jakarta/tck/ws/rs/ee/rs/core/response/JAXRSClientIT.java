/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.rs.core.response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import ee.jakarta.tck.ws.rs.common.provider.PrintingErrorHandler;
import ee.jakarta.tck.ws.rs.common.provider.StringBean;
import ee.jakarta.tck.ws.rs.common.provider.StringBeanEntityProvider;
import ee.jakarta.tck.ws.rs.common.provider.StringBeanHeaderDelegate;
import ee.jakarta.tck.ws.rs.common.provider.StringBeanRuntimeDelegate;
import ee.jakarta.tck.ws.rs.common.client.JaxrsCommonClient;
import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.Link.Builder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.Response.StatusType;

import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

/**
 * Some tests are the same as the tests in api.rs.core package except how the
 * Response is created. This is because implementation of inbound and outbound
 * Response differ.
 */
@ExtendWith(ArquillianExtension.class)
public class JAXRSClientIT extends JaxrsCommonClient {

  private static final long serialVersionUID = 4182256439207983256L;

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_ee_core_response_web/resource");
    setPrintEntity(true);
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

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/core/response/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_core_response_web.war");
    archive.addClasses(TSAppConfig.class, JaxrsUtil.class, ResponseTest.class, CorruptedInputStream.class, StringBean.class, StringBeanHeaderDelegate.class, StringBeanEntityProvider.class, StringBeanRuntimeDelegate.class, PrintingErrorHandler.class);
    archive.setWebXML(new StringAsset(webXml));
    return archive;

  }


  /*
   * @class.setup_props: webServerHost; webServerPort; ts_home;
   */
  /* Run test */

  /*
   * @testName: statusTest
   * 
   * @assertion_ids: JAXRS:SPEC:14.2; JAXRS:JAVADOC:131; JAXRS:JAVADOC:139;
   * 
   * @test_Strategy: Client send request to a resource, verify that correct
   * status code returned
   *
   */
  @Test
  public void statusTest() throws Fault {
    for (Response.Status status : Response.Status.values()) {
      if (status == Status.RESET_CONTENT)
        continue; // it takes some time
      setProperty(Property.STATUS_CODE, getStatusCode(status));
      invokeGet("status?status=" + getStatusCode(status));
    }
  }

  /*
   * @testName: bufferEntityBuffersDataTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:838;
   * 
   * @test_Strategy: Buffer the message entity. In case the message entity is
   * backed by an unconsumed entity input stream, all the bytes of the original
   * entity input stream are read and stored locally.
   * 
   * This operation is idempotent, i.e. it can be invoked multiple times with
   * the same effect which also means that calling the bufferEntity() method on
   * an already buffered (and thus closed) message instance is legal and has no
   * further effect.
   */
  @Test
  public void bufferEntityBuffersDataTest() throws Fault {
    Response response = invokeGet("entity");
    boolean buffer = response.bufferEntity();
    assertTrue(buffer, "#bufferEntity() did not buffer opened stream");
    buffer = response.bufferEntity();
    assertTrue(buffer, "#bufferEntity() is not idempotent");

    String read = response.readEntity(String.class);
    assertTrue(read.equals(ResponseTest.ENTITY), "Read entity"+ read+
        "instead of"+ ResponseTest.ENTITY);
    read = response.readEntity(String.class);
    assertTrue(read.equals(ResponseTest.ENTITY), "Read entity"+ read+
        "instead of"+ ResponseTest.ENTITY);
    logMsg("#bufferEntity did buffer opened stream as expected");
  }

  /*
   * @testName: bufferEntityThrowsExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:838;
   * 
   * @test_Strategy: Throws: ProcessingException - if there was an error while
   * buffering the entity input stream.
   */
  @Test
  public void bufferEntityThrowsExceptionTest() throws Fault {
    setCorruptedStream(); // the error is on buffering, which calls close()
    Response response = invokeGet("corrupted");
    catchCorruptedStreamExceptionOnBufferEntity(response);
    logMsg("ProcessingException has been thrown as expected");
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
    Response response = invokeGet("entity");
    response.close();
    try {
      response.bufferEntity();
      fault("buffer entity did not throw IllegalStateException when closed");
    } catch (IllegalStateException e) {
      logMsg("#bufferEntity throws IllegalStateException as expected");
    }
  }

  /*
   * @testName: closeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:841;
   * 
   * @test_Strategy: Close the message entity input stream (if available and
   * open). This operation is idempotent, i.e. it can be invoked multiple times
   * with the same effect which also means that calling the method on an already
   * closed message instance is legal and has no further effect
   */
  @Test
  public void closeTest() throws Fault {
    // make new Client, the one from super class calls bufferEntity()
    Client client = ClientBuilder.newClient();
    WebTarget target = client
        .target("http://" + _hostname + ":" + _port + getContextRoot())
        .path("entity");
    Response response = target.request(MediaType.TEXT_PLAIN_TYPE).buildGet()
        .invoke();
    response.close(); // calling the method on an already closed message
    response.close(); // instance is legal

    // Any attempts to manipulate (read, get, buffer) a message entity on a
    // closed response will result in an IllegalStateException being thrown
    try {
      String entity = response.readEntity(String.class);
      assertTrue(false, "IllegalStateException has not been thrown when"+
          "#close() and #readEntity() but entity"+ entity+ "has been read");
    } catch (IllegalStateException e) {
      logMsg(
          "#close() closed the stream, and consecutive reading threw IllegalStateException as expected");
    }
  }

  /*
   * @testName: closeThrowsExceptionWhenErrorTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:841;
   * 
   * @test_Strategy: throws ProcessingException - if there is an error closing
   * the response.
   */
  @Test
  public void closeThrowsExceptionWhenErrorTest() throws Fault {
    setCorruptedStream();
    Response response = invokeGet("corrupted");
    try {
      response.close(); // response.close should call stream.close()
      assertTrue(false, "ProcessingException has not been thrown when"+
          "CorruptedInputStream#close()");
    } catch (ProcessingException e) {
      // it is corrupted, #close throws IOException
      assertNotNull(e.getCause(), "unknown exception thrown", e);
      assertEquals(e.getCause().getMessage(), CorruptedInputStream.IOETEXT,
          "unknown exception thrown", e);
      logMsg("#close() threw ProcessingException as expected");
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
    String allowed = Request.POST.name() + " " + Request.TRACE.name();
    Response response = invokePost("allowedmethods", allowed);
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
    Response response = invokeGet("cookies");
    // getCookies test
    Map<String, NewCookie> map = response.getCookies();
    for (Entry<String, NewCookie> entry : map.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue().getValue();
      if (key.equals("c1"))
        assertEquals(value, "v1", value, "does not match expected v1");
      else if (key.equals("c2"))
        assertEquals(value, "v2", value, "does not match expected v2");
      else
        fault("Got unknown cookie", entry.getKey());
    }

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
    NewCookie cookie3 = new NewCookie("c3", "v3");
    Response response = invokeGet("cookies");
    // getCookies test
    Map<String, NewCookie> map;
    try {
      map = response.getCookies();
      map.put("c3", cookie3);
    } catch (Exception e) {
      // can throw an exception or nothing or return a copy map
    }
    map = response.getCookies();
    assertFalse(map.containsKey("c3"), "getCookies is not read-only returned"+
        map.get("c3"));
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
    long date = getCurrentTimeMillis();
    Response response = invokePost("date", String.valueOf(date));
    long responseDate = response.getDate().getTime();
    // assertEqualsLong(date, responseDate, "Original date", date,
    // "and response#getDate()", responseDate, "differs");
    // int TC, the Date settings is overriden by underlaying containers
    // But getDate is to be tested, not setDate, hence
    assertTrue(Math.abs(responseDate - date) < (10 * 60 * 1000),
        "#getDate() returned time that differes by more than 10 minutes");
    logMsg("#getDate matches the Date HTTP header");
  }

  /*
   * @testName: getEntityThrowsIllegalStateExceptionTestWhenClosed
   * 
   * @assertion_ids: JAXRS:JAVADOC:123;
   * 
   * @test_Strategy: if the entity was previously fully consumed as an
   * InputStream input stream, or if the response has been #close() closed.
   */
  @Test
  public void getEntityThrowsIllegalStateExceptionTestWhenClosed()
      throws Fault {
    Response response = invokeGet("entity");
    response.close();
    try {
      Object entity = response.getEntity();
      fault("No exception has been thrown, entity=", entity);
    } catch (IllegalStateException e) {
      logMsg("#getEntity throws IllegalStateException as expected", e);
    }
  }

  /*
   * @testName: getEntityThrowsIllegalStateExceptionWhenConsumedTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:123;
   * 
   * @test_Strategy: if the entity was previously fully consumed as an
   * InputStream input stream, or if the response has been #close() closed.
   */
  @Test
  public void getEntityThrowsIllegalStateExceptionWhenConsumedTest()
      throws Fault {
    Response response = invokeGet("entity");
    response.readEntity(String.class);
    try {
      Object entity = response.getEntity();
      fault("No exception has been thrown entity=", entity);
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
    String tag = "ABCDEF0123456789";
    setProperty(Property.STATUS_CODE, getStatusCode(Status.NOT_MODIFIED));
    Response response = invokePost("entitytag", tag);
    EntityTag responseTag = response.getEntityTag();
    assertEquals(tag, responseTag.getValue(), "response#getEntityTag()",
        responseTag.getValue(), "is unequal to expected EntityTag", tag);
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
    ResponseHeaderValue<EntityTag> value = new ResponseHeaderValue<>();
    addProvider(new HeaderNotPresent<EntityTag>(value) {
      @Override
      protected void setHeader(ClientResponseContext responseContext,
          ResponseHeaderValue<EntityTag> header) {
        header.value = responseContext.getEntityTag();
      }
    });

    Response response = invokePost("entitytag", null);
    EntityTag responseTag = response.getEntityTag();
    assertHeaderNull(responseTag, value, "getEntityTag");
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
    Response response = invokePost("headers", "notnull");
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
   * @testName: getHeadersUsingHeaderDelegateTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:848;
   * 
   * @test_Strategy: Get view of the response headers and their object values.
   */
  @Test
  public void getHeadersUsingHeaderDelegateTest() throws Fault {
    invokeGet("setstringbeanruntime");

    Response response = invokePost("headerstring", "notnull");
    String[] metadata = JaxrsCommonClient.getMetadata(response.getHeaders());
    logMsg("Received:");
    logMsg((Object[]) metadata);
    String headers = JaxrsUtil.iterableToString(";", (Object[]) metadata);
    for (int i = 1; i != 4; i++) {
      String header = "s" + i + ":s" + i;
      assertContainsIgnoreCase(headers, header, "Expected header", header,
          "was not found in received headers", headers);
    }
    logMsg("Received expected headers", headers);

    invokeGet("setoriginalruntime");
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
    Response response = invokePost("headers", null);
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
    Response response = invokePost("headers", "headerstring");
    logMsg("Found following objects:");
    logMsg((Object[]) JaxrsCommonClient.getMetadata(response.getHeaders()));
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
    invokeGet("setstringbeanruntime");

    Response response = invokePost("headerstring", "delegate");
    String header = response.getHeaderString("s3");
    assertContainsIgnoreCase(header, "s3", "Header", "s3",
        "has unexpected value", header);
    logMsg("HeaderDelegate is used for header as expected");

    invokeGet("setoriginalruntime");
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
    Response response = invokePost("headerstring", "toString");
    String header = response.getHeaderString("s1");
    assertContainsIgnoreCase(header, "s1", "Header s1 has unexpected value",
        header);

    header = response.getHeaderString("s2");
    assertContainsIgnoreCase(header, "s2", "Header s2 has unexpected value",
        header);

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
    Response response = invokePost("language",
        Locale.CANADA_FRENCH.getCountry());
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
    ResponseHeaderValue<Locale> value = new ResponseHeaderValue<>();
    addProvider(new HeaderNotPresent<Locale>(value) {
      @Override
      protected void setHeader(ClientResponseContext responseContext,
          ResponseHeaderValue<Locale> header) {
        header.value = responseContext.getLanguage();
      }
    });

    Response response = invokePost("language", null);
    Locale locale = response.getLanguage();
    assertHeaderNull(locale, value, "getLanguage");
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
    long time = getCurrentTimeMillis();
    Response response = invokePost("lastmodified", String.valueOf(time));
    long responseDate = response.getLastModified().getTime();
    assertEqualsLong(time, responseDate, "Last Modified date", time,
        "does NOT match response#getLastModified()", responseDate);
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
    ResponseHeaderValue<Date> containerValue = new ResponseHeaderValue<>();
    addProvider(new HeaderNotPresent<Date>(containerValue) {
      @Override
      protected void setHeader(ClientResponseContext responseContext,
          ResponseHeaderValue<Date> header) {
        header.value = responseContext.getLastModified();
      }
    });

    Response response = invokePost("lastmodified", null);
    Date responseDate = response.getLastModified();
    assertHeaderNull(responseDate, containerValue, "getLastModified");
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
    Response response = invokePost("length", "1234567890");
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
    Response response = invokePost("length", null);
    int len = response.getLength();
    String headerLen = response.getHeaderString(HttpHeaders.CONTENT_LENGTH);
    if (headerLen == null)
      assertEqualsInt(len, -1, "Expected Content-Length = -1",
          "does NOT match response#getLength()", len);
    else
      assertEqualsInt(len, Integer.parseInt(headerLen),
          "Expected Content-Length =", headerLen,
          "does NOT match response#getLength()=", len);
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
    String rel = "getLinkTest";
    Response response = invokePost("link", rel);
    Link responseLink = response.getLink(rel);
    assertNotNull(responseLink, "#getLink is null");
    assertContains(rel, responseLink.getRel(),
        "#getLink() returned unexpected Link", responseLink);
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
    ResponseHeaderValue<Link> containerValue = new ResponseHeaderValue<>();
    addProvider(new HeaderNotPresent<Link>(containerValue) {
      @Override
      protected void setHeader(ClientResponseContext responseContext,
          ResponseHeaderValue<Link> header) {
        header.value = responseContext.getLink("getLinkTest");
      }
    });

    Response response = invokePost("link", null);
    Link responseLink = response.getLink("getLinkTest");
    assertHeaderNull(responseLink, containerValue, "getLink");
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
    Response response = invokePost("linkbuilder", rel);
    Link responseLink = response.getLink(rel);
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
    Response response = invokeGet("entity");
    Builder builder = response.getLinkBuilder("anyrelation");
    assertNull(builder,
        "#getLinkBuilder('relation') returned unexpected builder", builder);
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
    Response response = invokeGet("links");
    Set<Link> responseLinks = response.getLinks();
    assertEqualsInt(responseLinks.size(), 2,
        "#getLinks() returned set of unexpected size", responseLinks.size());
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
    Response response = invokeGet("entity");
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
    String path = "path";
    URI serverUri = ResponseTest.createUri(path);
    Response response = invokePost("location", path);
    URI responseLocation = response.getLocation();
    assertEquals(responseLocation, serverUri, "#getLocation()",
        responseLocation, "differs from expected", serverUri);
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
    ResponseHeaderValue<URI> containerValue = new ResponseHeaderValue<>();
    addProvider(new HeaderNotPresent<URI>(containerValue) {
      @Override
      protected void setHeader(ClientResponseContext responseContext,
          ResponseHeaderValue<URI> header) {
        header.value = responseContext.getLocation();
      }
    });

    Response response = invokeGet("entity");
    URI responseLocation = response.getLocation();
    assertHeaderNull(responseLocation, containerValue, "getLocation");
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
    MediaType mediaType = MediaType.APPLICATION_ATOM_XML_TYPE;
    Response response = invokePost("mediatype", mediaType.toString());
    MediaType responseMedia = response.getMediaType();
    assertEquals(mediaType, responseMedia, "#getMediaType()", responseMedia,
        "differs from expected", MediaType.APPLICATION_ATOM_XML);
    logMsg("#getMediaType returned expected MediaType");
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
      setProperty(Property.STATUS_CODE, getStatusCode(status));
      Response response = invokePost("statusinfo", status.name());
      StatusType info = response.getStatusInfo();
      assertEqualsInt(info.getStatusCode(), status.getStatusCode(),
          "#getStatusInfo returned unexpected value", info);
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
    Response response = invokePost("headerstring", "stringheaders");
    MultivaluedMap<String, String> headers = response.getStringHeaders();
    String header = headers.getFirst("s1");
    assertContainsIgnoreCase(header, "s1", "Header", "s1",
        "has unexpected value", header);

    header = headers.getFirst("s2");
    assertContainsIgnoreCase(header, "s2", "Header", "s2",
        "has unexpected value", header);

    logMsg("#getStringHeaders contains expected values",
        JaxrsUtil.iterableToString(",", headers.entrySet()));
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
    invokeGet("setstringbeanruntime");

    Response response = invokePost("headerstring", "stringheaders");
    MultivaluedMap<String, String> headers = response.getStringHeaders();
    String header = headers.getFirst("s3");
    assertContainsIgnoreCase("s3", header, "Header", "s3",
        "has unexpected value", header);

    logMsg("#getStringHeaders contains expected values",
        JaxrsUtil.iterableToString(",", headers.entrySet()));

    invokeGet("setoriginalruntime");
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
    Response response = invokeGet("entity");
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
    Response response = invokePost("headerstring", null);
    assertFalse(response.hasEntity(), "#hasEntity did found the entity");
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
    Response response = invokeGet("entity");
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
    Response response = invokePost("link", "path");
    assertTrue(response.hasLink("path"), "#hasLink did not found a Link");
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
    Response response = invokeGet("entity");
    assertFalse(response.hasLink("rel"), "#has Link did found some Link");
    logMsg("#hasLink has not found any Link as expected");
  }

  /*
   * @testName: readEntityClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:863;
   * 
   * @test_Strategy: Read the message entity input stream as an instance of
   * specified Java type using a MessageBodyReader that supports mapping the
   * message entity stream onto the requested type
   */
  @Test
  public void readEntityClassTest() throws Fault {
    Response response = invokeGet("entity");
    response.bufferEntity();
    String line;

    Reader reader = response.readEntity(Reader.class);
    line = readLine(reader);
    assertTrue(ResponseTest.ENTITY.equals(line), "#readEntity(Reader)={"+ line+
        "} differs from expected"+ ResponseTest.ENTITY);

    byte[] buffer = new byte[0];
    buffer = response.readEntity(buffer.getClass());
    line = new String(buffer);
    assertTrue(ResponseTest.ENTITY.equals(line), "#readEntity(byte[].class)={"+
        line+ "} differs from expected"+ ResponseTest.ENTITY);

    logMsg("Got expected", line);
  }

  /*
   * @testName: readEntityClassIsNullWhenNoEntityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:863;
   * 
   * @test_Strategy: If the message does not contain an entity body null is
   * returned.
   */
  @Test
  public void readEntityClassIsNullWhenNoEntityTest() throws Fault {
    Response response = invokeGet("status?status=200");
    String entity = response.readEntity(String.class);
    assertTrue(entity == null || "".equals(entity),
        "entity is not null or zero length"+ entity);
    logMsg("Null or zero length entity returned when no entity as expected");
  }

  /*
   * @testName: readEntityClassCloseIsCalledTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:863;
   * 
   * @test_Strategy: Unless the supplied entity type is an input stream, this
   * method automatically closes the consumed response entity stream if it is
   * not buffered.
   */
  @Test
  public void readEntityClassCloseIsCalledTest() throws Fault {
    AtomicInteger ai = setCorruptedStream();
    final Response response = invokeGet("corrupted");
    catchCorruptedStreamException(new Runnable() {
      @Override
      public void run() {
        response.readEntity(String.class);
      }
    });
    assertEquals(ai.get(), CorruptedInputStream.CLOSEVALUE,
        "Close has not been called");
    logMsg("Close() has been called on an entity stream as expected");
  }

  /*
   * @testName: readEntityClassCloseIsNotCalledOnInputStreamTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:863;
   * 
   * @test_Strategy: Unless the supplied entity type is an input stream, this
   * method automatically closes the consumed response entity stream if it is
   * not buffered.
   */
  @Test
  public void readEntityClassCloseIsNotCalledOnInputStreamTest() throws Fault {
    AtomicInteger ai = setCorruptedStream();
    Response response = invokeGet("corrupted");
    try {
      response.readEntity(InputStream.class);
      logMsg("Close() has not been called on entity stream as expected");
    } catch (Exception e) {
      throw new Fault("Close was called", e);
    }
    assertTrue(ai.get() == 0, "Close was called");
  }

  /*
   * @testName: readEntityClassThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:863;
   * 
   * @test_Strategy: Method throws an ProcessingException if the content of the
   * message cannot be mapped to an entity of the requested type
   */
  @Test
  public void readEntityClassThrowsProcessingExceptionTest() throws Fault {
    Response response = invokeGet("entity");
    try {
      response.readEntity(Void.class);
      throw new Fault(
          "No exception has been thrown when reader for entity class is not known");
    } catch (ProcessingException e) {
      logMsg("ProcessingException has been thrown as expected");
    }
  }

  /*
   * @testName: readEntityClassThrowsIllegalStateExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:863;
   * 
   * @test_Strategy: if the entity is not backed by an input stream, or if the
   * entity input stream has been fully consumed already and has not been
   * buffered prior consuming.
   */
  @Test
  public void readEntityClassThrowsIllegalStateExceptionTest() throws Fault {
    Client client = ClientBuilder.newClient(); // create a new client
    WebTarget target = client.target( // with no bufferEntity called
        "http://" + _hostname + ":" + _port + getContextRoot()).path("entity");
    Response response = target.request(MediaType.TEXT_PLAIN_TYPE).buildGet()
        .invoke();
    String entity = response.readEntity(String.class);
    assertTrue(ResponseTest.ENTITY.equals(entity),
        "#readEntity(String.class)={"+ entity+ "} differs from expected"+
        ResponseTest.ENTITY);
    try {
      response.readEntity(Reader.class);
      throw new Fault(
          "No exception has been thrown when reader for entity is not buffered");
    } catch (IllegalStateException e) {
      logMsg("IllegalStateException has been thrown as expected");
    }
  }

  /*
   * @testName: readEntityGenericTypeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:866;
   * 
   * @test_Strategy: Read the message entity input stream as an instance of
   * specified Java type using a MessageBodyReader that supports mapping the
   * message entity stream onto the requested type
   */
  @Test
  public void readEntityGenericTypeTest() throws Fault {
    Response response = invokeGet("entity");
    response.bufferEntity();
    String line;

    Reader reader = response.readEntity(generic(Reader.class));
    line = readLine(reader);
    assertTrue(ResponseTest.ENTITY.equals(line),
        "#readEntity(GenericType<Reader>)={"+ line+ "} differs from expected"+
        ResponseTest.ENTITY);

    byte[] buffer = new byte[0];
    buffer = response.readEntity(generic(buffer.getClass()));
    assertTrue(buffer != null,
        "response.readEntity(GenericType<byte[]>) is null");
    line = new String(buffer);
    assertTrue(ResponseTest.ENTITY.equals(line),
        "#readEntity(GenericType<byte[]>)={"+ line+ "} differs from expected"+
        ResponseTest.ENTITY);

    logMsg("Got expected", line);
  }

  /*
   * @testName: readEntityGenericIsNullWhenNoEntityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:866;
   * 
   * @test_Strategy: If the message does not contain an entity body null is
   * returned.
   */
  @Test
  public void readEntityGenericIsNullWhenNoEntityTest() throws Fault {
    String request = buildRequest(Request.GET, "status?status=200");
    setProperty(Property.REQUEST, request);
    invoke();
    Response response = getResponse();
    String entity = response.readEntity(generic(String.class));
    assertTrue(entity == null || "".equals(entity),
        "entity is not null or zero length"+ entity);
    logMsg("Null or zero length entity returned when no entity as expected");
  }

  /*
   * @testName: readEntityGenericCloseIsCalledTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:866;
   * 
   * @test_Strategy: Unless the supplied entity type is an input stream, this
   * method automatically closes the consumed response entity stream if it is
   * not buffered.
   */
  @Test
  public void readEntityGenericCloseIsCalledTest() throws Fault {
    AtomicInteger ai = setCorruptedStream();
    final Response response = invokeGet("corrupted");
    catchCorruptedStreamException(new Runnable() {
      @Override
      public void run() {
        response.readEntity(generic(String.class));
      }
    });
    assertTrue(ai.get() == CorruptedInputStream.CLOSEVALUE,
        "Close has not been called");
    logMsg("Close() has been called on an entity stream as expected");
  }

  /*
   * @testName: readEntityGenericTypeCloseIsNotCalledOnInputStreamTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:866;
   * 
   * @test_Strategy: Unless the supplied entity type is an input stream, this
   * method automatically closes the consumed response entity stream if it is
   * not buffered.
   */
  @Test
  public void readEntityGenericTypeCloseIsNotCalledOnInputStreamTest()
      throws Fault {
    AtomicInteger ai = setCorruptedStream();
    Response response = invokeGet("corrupted");
    try {
      response.readEntity(generic(InputStream.class));
      logMsg("Close() has not been called on entity stream as expected");
    } catch (Exception e) {
      throw new Fault("Close was called", e);
    }
    assertTrue(ai.get() == 0, "Close was called");
  }

  /*
   * @testName: readEntityGenericTypeThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:866;
   * 
   * @test_Strategy: Method throws an ProcessingException if the content of the
   * message cannot be mapped to an entity of the requested type
   */
  @Test
  public void readEntityGenericTypeThrowsProcessingExceptionTest()
      throws Fault {
    Response response = invokeGet("entity");
    try {
      response.readEntity(generic(Void.class));
      throw new Fault(
          "No exception has been thrown when reader for entity class is not known");
    } catch (ProcessingException e) {
      logMsg("ProcessingException has been thrown as expected");
    }
  }

  /*
   * @testName: readEntityGenericTypeThrowsIllegalStateExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:866;
   * 
   * @test_Strategy: if the entity is not backed by an input stream, or if the
   * entity input stream has been fully consumed already and has not been
   * buffered prior consuming.
   */
  @Test
  public void readEntityGenericTypeThrowsIllegalStateExceptionTest()
      throws Fault {
    Client client = ClientBuilder.newClient(); // create a new client
    WebTarget target = client.target( // with no bufferEntity called
        "http://" + _hostname + ":" + _port + getContextRoot()).path("entity");
    Response response = target.request(MediaType.TEXT_PLAIN_TYPE).buildGet()
        .invoke();
    String entity = response.readEntity(generic(String.class));
    assertTrue(ResponseTest.ENTITY.equals(entity),
        "#readEntity(GenericType<byte[]>)={"+ entity+ "} differs from expected"+
        ResponseTest.ENTITY);
    try {
      response.readEntity(generic(Reader.class));
      throw new Fault(
          "No exception has been thrown when reader for entity is not buffered");
    } catch (IllegalStateException e) {
      logMsg("IllegalStateException has been thrown as expected");
    }
  }

  /*
   * @testName: readEntityClassAnnotationTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:869;
   * 
   * @test_Strategy: Read the message entity input stream as an instance of
   * specified Java type using a MessageBodyReader that supports mapping the
   * message entity stream onto the requested type. annotations - annotations
   * that will be passed to the MessageBodyReader.
   */
  @Test
  public void readEntityClassAnnotationTest() throws Fault {
    Date date = Calendar.getInstance().getTime();
    String sDate = String.valueOf(date.getTime());
    Annotation[] annotations = AnnotatedClass.class.getAnnotations();
    int expected = DateReaderWriter.ANNOTATION_CONSUMES
        | DateReaderWriter.ANNOTATION_PROVIDER;

    AtomicInteger ai = new AtomicInteger();
    DateReaderWriter drw = new DateReaderWriter(ai);
    addProvider(drw);

    Response response = invokeGet("date?date=" + sDate);
    response.bufferEntity();

    Date entity = response.readEntity(Date.class, annotations);
    assertTrue(date.equals(entity), "#readEntity(Date, annotations)={"+ entity+
        "} differs from expected"+ date);

    assertTrue(ai.get() == expected, ai.get()+ "differes from expected"+
        expected+ "which suggest a problem with annotation passing");

    String responseDate = response.readEntity(String.class, annotations);
    assertTrue(sDate.equals(responseDate),
        "#readEntity(String.class, annotations)={"+ responseDate+
        "} differs from expected"+ sDate);

    assertTrue(ai.get() == expected, ai.get()+ "differes from expected"+
        expected+ "which suggest a problem with annotation passing");

    logMsg("Got expected date", date);
  }

  /*
   * @testName: readEntityClassAnnotationIsNullWhenNoEntityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:869;
   * 
   * @test_Strategy: If the message does not contain an entity body null is
   * returned.
   */
  @Test
  public void readEntityClassAnnotationIsNullWhenNoEntityTest() throws Fault {
    Annotation[] annotations = AnnotatedClass.class.getAnnotations();
    Response response = invokeGet("status?status=200");
    String entity = response.readEntity(String.class, annotations);
    assertTrue(entity == null || "".equals(entity),
        "entity is not null or zero length"+ entity);
    logMsg("Null or zero length entity returned when no entity as expected");
  }

  /*
   * @testName: readEntityClassAnnotationCloseIsCalledTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:869;
   * 
   * @test_Strategy: Unless the supplied entity type is an input stream, this
   * method automatically closes the consumed response entity stream if it is
   * not buffered.
   */
  @Test
  public void readEntityClassAnnotationCloseIsCalledTest() throws Fault {
    final Annotation[] annotations = AnnotatedClass.class.getAnnotations();
    AtomicInteger ai = setCorruptedStream();
    final Response response = invokeGet("corrupted");
    catchCorruptedStreamException(new Runnable() {
      @Override
      public void run() {
        response.readEntity(String.class, annotations);
      }
    });
    assertEqualsInt(ai.get(), CorruptedInputStream.CLOSEVALUE,
        "Close has not been called");
    logMsg("Close() has been called on an entity stream as expected");
  }

  /*
   * @testName: readEntityClassAnnotationCloseIsNotCalledOnInputStreamTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:869;
   * 
   * @test_Strategy: Unless the supplied entity type is an input stream, this
   * method automatically closes the consumed response entity stream if it is
   * not buffered.
   */
  @Test
  public void readEntityClassAnnotationCloseIsNotCalledOnInputStreamTest()
      throws Fault {
    Annotation[] annotations = AnnotatedClass.class.getAnnotations();
    AtomicInteger ai = setCorruptedStream();
    Response response = invokeGet("corrupted");
    try {
      response.readEntity(InputStream.class, annotations);
      logMsg("Close() has not been called on entity stream as expected");
    } catch (ProcessingException e) {
      throw new Fault("Close was called", e);
    }
    assertTrue(ai.get() == 0, "Close was called");
  }

  /*
   * @testName: readEntityClassAnnotationThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:869;
   * 
   * @test_Strategy: Method throws an ProcessingException if the content of the
   * message cannot be mapped to an entity of the requested type
   */
  @Test
  public void readEntityClassAnnotationThrowsProcessingExceptionTest()
      throws Fault {
    Annotation[] annotations = AnnotatedClass.class.getAnnotations();
    Response response = invokeGet("entity");
    try {
      response.readEntity(Void.class, annotations);
      throw new Fault(
          "No exception has been thrown when reader for entity class is not known");
    } catch (ProcessingException e) {
      logMsg("ProcessingException has been thrown as expected");
    }
  }

  /*
   * @testName: readEntityClassAnnotationThrowsIllegalStateExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:869;
   * 
   * @test_Strategy: if the entity is not backed by an input stream, or if the
   * entity input stream has been fully consumed already and has not been
   * buffered prior consuming.
   */
  @Test
  public void readEntityClassAnnotationThrowsIllegalStateExceptionTest()
      throws Fault {
    Annotation[] annotations = AnnotatedClass.class.getAnnotations();
    Client client = ClientBuilder.newClient(); // create a new client
    WebTarget target = client.target( // with no bufferEntity called
        "http://" + _hostname + ":" + _port + getContextRoot()).path("entity");
    Response response = target.request(MediaType.TEXT_PLAIN_TYPE).buildGet()
        .invoke();
    String entity = response.readEntity(String.class, annotations);
    assertTrue(ResponseTest.ENTITY.equals(entity),
        "#readEntity(String.class, annotations)={"+ entity+
        "} differs from expected"+ ResponseTest.ENTITY);
    try {
      response.readEntity(Reader.class, annotations);
      throw new Fault(
          "No exception has been thrown when reader for entity is not buffered");
    } catch (IllegalStateException e) {
      logMsg("IllegalStateException has been thrown as expected");
    }
  }

  /*
   * @testName: readEntityGenericTypeAnnotationTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:872;
   * 
   * @test_Strategy: Read the message entity input stream as an instance of
   * specified Java type using a MessageBodyReader that supports mapping the
   * message entity stream onto the requested type. annotations - annotations
   * that will be passed to the MessageBodyReader.
   */
  @Test
  public void readEntityGenericTypeAnnotationTest() throws Fault {
    Date date = Calendar.getInstance().getTime();
    String sDate = String.valueOf(date.getTime());
    Annotation[] annotations = AnnotatedClass.class.getAnnotations();
    int expected = DateReaderWriter.ANNOTATION_CONSUMES
        | DateReaderWriter.ANNOTATION_PROVIDER;

    AtomicInteger ai = new AtomicInteger();
    DateReaderWriter drw = new DateReaderWriter(ai);
    addProvider(drw);

    Response response = invokeGet("date?date=" + sDate);
    response.bufferEntity();

    Date entity = response.readEntity(generic(Date.class), annotations);
    assertTrue(date.equals(entity), "#readEntity(Date, annotations)={"+ entity+
        "} differs from expected"+ date);

    assertTrue(ai.get() == expected, ai.get()+ "differes from expected"+
        expected+ "which suggest a problem with annotation passing");

    String responseDate = response.readEntity(generic(String.class),
        annotations);
    assertTrue(sDate.equals(responseDate),
        "#readEntity(String.class, annotations)={"+ responseDate+
        "} differs from expected"+ sDate);

    assertTrue(ai.get() == expected, ai.get()+ "differes from expected"+
        expected+ "which suggest a problem with annotation passing");

    logMsg("Got expected date", date);
  }

  /*
   * @testName: readEntityGenericTypeAnnotationIsNullWhenNoEntityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:872;
   * 
   * @test_Strategy: If the message does not contain an entity body null is
   * returned.
   */
  @Test
  public void readEntityGenericTypeAnnotationIsNullWhenNoEntityTest()
      throws Fault {
    Annotation[] annotations = AnnotatedClass.class.getAnnotations();
    Response response = invokeGet("status?status=200");
    String entity = response.readEntity(generic(String.class), annotations);
    assertTrue(entity == null || "".equals(entity),
        "entity is not null or zero length"+ entity);
    logMsg("Null or zero length entity returned when no entity as expected");
  }

  /*
   * @testName: readEntityGenericTypeAnnotationCloseIsCalledTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:872;
   * 
   * @test_Strategy: Unless the supplied entity type is an input stream, this
   * method automatically closes the consumed response entity stream if it is
   * not buffered.
   */
  @Test
  public void readEntityGenericTypeAnnotationCloseIsCalledTest() throws Fault {
    final Annotation[] annotations = AnnotatedClass.class.getAnnotations();
    AtomicInteger ai = setCorruptedStream();
    final Response response = invokeGet("corrupted");
    catchCorruptedStreamException(new Runnable() {
      @Override
      public void run() {
        response.readEntity(generic(String.class), annotations);
      }
    });
    assertEqualsInt(ai.get(), CorruptedInputStream.CLOSEVALUE,
        "Close has not been called");
    logMsg("Close() has been called on an entity stream as expected");
  }

  /*
   * @testName: readEntityGenericTypeAnnotationCloseIsNotCalledOnInputStreamTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:872;
   * 
   * @test_Strategy: Unless the supplied entity type is an input stream, this
   * method automatically closes the consumed response entity stream if it is
   * not buffered.
   */
  @Test
  public void readEntityGenericTypeAnnotationCloseIsNotCalledOnInputStreamTest()
      throws Fault {
    Annotation[] annotations = AnnotatedClass.class.getAnnotations();
    AtomicInteger ai = setCorruptedStream();
    Response response = invokeGet("corrupted");
    try {
      response.readEntity(generic(InputStream.class), annotations);
    } catch (ProcessingException e) {
      fault("Close was called", e);
    }
    assertEqualsInt(ai.get(), 0, "Close was called");
    logMsg("Close() has not been called on entity stream as expected");
  }

  /*
   * @testName: readEntityGenericTypeAnnotationThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:872;
   * 
   * @test_Strategy: Method throws an ProcessingException if the content of the
   * message cannot be mapped to an entity of the requested type
   */
  @Test
  public void readEntityGenericTypeAnnotationThrowsProcessingExceptionTest()
      throws Fault {
    Annotation[] annotations = AnnotatedClass.class.getAnnotations();
    Response response = invokeGet("entity");
    try {
      response.readEntity(generic(Void.class), annotations);
      throw new Fault(
          "No exception has been thrown when reader for entity class is not known");
    } catch (ProcessingException e) {
      logMsg("ProcessingException has been thrown as expected");
    }
  }

  /*
   * @testName: readEntityGenericTypeAnnotationThrowsIllegalStateExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:872;
   * 
   * @test_Strategy: if the entity is not backed by an input stream, or if the
   * entity input stream has been fully consumed already and has not been
   * buffered prior consuming.
   */
  @Test
  public void readEntityGenericTypeAnnotationThrowsIllegalStateExceptionTest()
      throws Fault {
    Annotation[] annotations = AnnotatedClass.class.getAnnotations();
    Client client = ClientBuilder.newClient(); // create a new client
    WebTarget target = client.target( // with no bufferEntity called
        "http://" + _hostname + ":" + _port + getContextRoot()).path("entity");
    Response response = target.request(MediaType.TEXT_PLAIN_TYPE).buildGet()
        .invoke();
    String entity = response.readEntity(generic(String.class), annotations);
    assertTrue(ResponseTest.ENTITY.equals(entity),
        "#readEntity(GenericType<String>, annotations)={"+ entity+
        "} differs from expected"+ ResponseTest.ENTITY);
    try {
      response.readEntity(generic(Reader.class), annotations);
      throw new Fault(
          "No exception has been thrown when reader for entity is not buffered");
    } catch (IllegalStateException e) {
      logMsg("IllegalStateException has been thrown as expected");
    }
  }

    /*
   * @testName: responseCreatedRelativeURITest
   *
   * @assertion_ids: JAXRS:JAVADOC:121;
   *
   * @test_Strategy: The resource calls Response.created() to set the Location header with a
   * relative URI. The relative URI should be converted into an absolute URI by resolving it
   * relative to the base URI.
   */
  @Test
  public void responseCreatedRelativeURITest()
      throws Fault {
    String resourceUrl = getAbsoluteUrl();
    String expected = resourceUrl.substring(0, resourceUrl.length() - "resource".length()) + "created";
    Response response = invokeGet("created");
    try {
      assertTrue(expected.equals(response.getHeaderString("location")),
        "#response.getHeaderString(\"location\") [" +
        response.getHeaderString("location") + "] differs from "+ expected);
    } finally {
      response.close();
    }
  }

  // ////////////////////////////////////////////////////////////////////
  protected <T> GenericType<T> generic(Class<T> clazz) {
    return new GenericType<T>(clazz);
  }

  protected Response invokeGet(String method) throws Fault {
    String request = buildRequest(Request.GET, method);
    setProperty(Property.REQUEST_HEADERS,
        buildAccept(MediaType.TEXT_PLAIN_TYPE));
    setProperty(Property.REQUEST, request);
    invoke();
    return getResponse();
  }

  protected Response invokePost(String method, Object entity) throws Fault {
    String request = buildRequest(Request.POST, method);
    setProperty(Property.REQUEST, request);
    setRequestContentEntity(entity);
    invoke();
    return getResponse();
  }

  protected String readLine(Reader reader) throws Fault {
    String line = null;
    BufferedReader buffered = new BufferedReader(reader);
    try {
      line = buffered.readLine();
    } catch (IOException e) {
      try {
        buffered.close();
      } catch (IOException ie) {
      }
      throw new Fault(e);
    }
    return line;
  }

  protected AtomicInteger setCorruptedStream() {
    final AtomicInteger ai = new AtomicInteger(0);
    ClientResponseFilter filter = new ClientResponseFilter() {
      @Override
      public void filter(ClientRequestContext arg0,
          ClientResponseContext response) throws IOException {
        CorruptedInputStream cis = new CorruptedInputStream(
            ResponseTest.ENTITY.getBytes(), ai);
        cis.setCorrupted(true);
        response.setEntityStream(cis);
      }
    };
    addProvider(filter);

    // do not use new entity stream in logging filter for the case of priority
    // disfunction, the CorruptedInputStream would be then replaced, wrongly
    // informing about not closing the stream
    super.setPrintEntity(false);

    return ai;
  }

  protected long getCurrentTimeMillis() {
    long millis = System.currentTimeMillis() / 1000;
    millis *= 1000;
    return millis;
  }

  protected String langToString(Object object) {
    Locale locale = null;
    if (object instanceof List)
      object = ((List<?>) object).iterator().next();
    if (object instanceof Locale)
      locale = (Locale) object;
    String value = locale == null ? object.toString() : locale.toString();
    return value.replace("_", "-");
  }

  protected void catchCorruptedStreamException(Runnable runnable) throws Fault {
    try {
      runnable.run();
    } catch (ProcessingException e) {
      // it is corrupted, #close throws IOException
      assertNotNull(e.getCause(), "unknown exception thrown", e);
      assertEquals(e.getCause().getMessage(), CorruptedInputStream.IOETEXT,
          "unknown exception thrown", e);
    }
  }

  protected void catchCorruptedStreamExceptionOnBufferEntity(
      final Response response) throws Fault {
    catchCorruptedStreamException(new Runnable() {
      @Override
      public void run() {
        // The original entity input stream is
        // consumed and automatically closed
        response.bufferEntity();
      }
    });
  }

  //////////////////////////////////////////////////////////////////////////////////////////////
  // Even though the test itself do not set the headers, possibly the vendor's
  ////////////////////////////////////////////////////////////////////////////////////////////// container
  ////////////////////////////////////////////////////////////////////////////////////////////// does.
  // In that case, the ResponseFilter checks the value; if it is not expected
  ////////////////////////////////////////////////////////////////////////////////////////////// null,
  ////////////////////////////////////////////////////////////////////////////////////////////// the
  ////////////////////////////////////////////////////////////////////////////////////////////// response
  // should return the same value the response context does.

  private <T> void assertHeaderNull(T actualHeader,
      ResponseHeaderValue<T> filterValue, String method) throws Fault {
    if (filterValue.value == null) {
      assertNull(actualHeader, "response#" + method + "() should be null, was ",
          actualHeader);
      logMsg("response#" + method + "() was null as expected");
    } else {
      assertEquals(filterValue.value, actualHeader,
          "response#" + method + "() was set to " + filterValue.value,
          "by container but was ", actualHeader);
      logMsg("response#" + method + "() was set to " + actualHeader
          + " as preset by container");
    }
  }

  class ResponseHeaderValue<T> {
    private T value;
  }

  abstract class HeaderNotPresent<T> implements ClientResponseFilter {
    private ResponseHeaderValue<T> headerValue;

    public HeaderNotPresent(ResponseHeaderValue<T> headerValue) {
      this.headerValue = headerValue;
    }

    @Override
    public void filter(ClientRequestContext requestContext,
        ClientResponseContext responseContext) throws IOException {
      setHeader(responseContext, headerValue);
    }

    protected abstract void setHeader(ClientResponseContext responseContext,
        ResponseHeaderValue<T> header);
  }

}
