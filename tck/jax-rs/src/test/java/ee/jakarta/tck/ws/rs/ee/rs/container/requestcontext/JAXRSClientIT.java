/*
 * Copyright (c) 2012, 2022 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.rs.container.requestcontext;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.io.InputStream;
import java.io.IOException;

import ee.jakarta.tck.ws.rs.common.client.JaxrsCommonClient;
import ee.jakarta.tck.ws.rs.common.impl.SecurityContextImpl;
import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.AfterEach;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
@ExtendWith(ArquillianExtension.class)
public class JAXRSClientIT extends JaxrsCommonClient {

  private static final long serialVersionUID = 111355567568365703L;

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_ee_rs_container_requestcontext_web/resource");
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
  public static WebArchive createDeployment() throws IOException {

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/container/requestcontext/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_rs_container_requestcontext_web.war");
    archive.addClasses(TSAppConfig.class, Resource.class,
    ContextOperation.class, RequestFilter.class, SecondRequestFilter.class,
    TemplateFilter.class, SecurityContextImpl.class);
    archive.setWebXML(new StringAsset(webXml));
    return archive;

  }

  /*
   * @testName: abortWithTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:649; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Invoke HTTP DELETE method for the current request
   * asynchronously.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void abortWithTest() throws Fault {
    invokeRequestAndCheckResponse(ContextOperation.ABORTWITH);
  }

  /*
   * @testName: getAcceptableLanguagesTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:650; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Get a read-only list of acceptable languages sorted
   * according to their q-value, with highest preference first.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void getAcceptableLanguagesTest() throws Fault {
    setProperty(Property.REQUEST_HEADERS, "Accept-Language:en-us");
    invokeRequestAndCheckResponse(ContextOperation.GETACCEPTABLELANGUAGES);
  }

  /*
   * @testName: getAcceptableLanguagesIsSortedTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:650; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Get a read-only list of acceptable languages sorted
   * according to their q-value, with highest preference first.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void getAcceptableLanguagesIsSortedTest() throws Fault {
    logMsg(
        "Check the #getAcceptableLanguages is sorted according to their q-value");
    setProperty(Property.REQUEST_HEADERS,
        "Accept-Language: da, en-gb;q=0.6, en-us;q=0.7");
    setProperty(Property.SEARCH_STRING, "da");
    setProperty(Property.SEARCH_STRING, "en-us");
    setProperty(Property.SEARCH_STRING, "en-gb");
    invokeRequestAndCheckResponse(ContextOperation.GETACCEPTABLELANGUAGES);
  }

  /*
   * @testName: getAcceptableLanguagesIsReadOnlyTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:650; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Get a read-only list of acceptable languages sorted
   * according to their q-value, with highest preference first.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void getAcceptableLanguagesIsReadOnlyTest() throws Fault {
    setProperty(Property.REQUEST_HEADERS,
        "Accept-Language: da, en-gb;q=0.6, en-us;q=0.7");
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH, "ca-fr");
    invokeRequestAndCheckResponse(
        ContextOperation.GETACCEPTABLELANGUAGESISREADONLY);
  }

  /*
   * @testName: getAcceptableMediaTypesTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:651; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Get a read-only list of acceptable languages sorted
   * according to their q-value.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void getAcceptableMediaTypesTest() throws Fault {
    setProperty(Property.REQUEST_HEADERS,
        buildAccept(MediaType.APPLICATION_JSON_TYPE));
    setProperty(Property.SEARCH_STRING, MediaType.APPLICATION_JSON);
    invokeRequestAndCheckResponse(ContextOperation.GETACCEPTABLEMEDIATYPES);
  }

  /*
   * @testName: getAcceptableMediaTypesIsSortedTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:651; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Get a read-only list of acceptable languages sorted
   * according to their q-value.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void getAcceptableMediaTypesIsSortedTest() throws Fault {
    logMsg(
        "Check the #getAcceptableMediaTypes is sorted according to their q-value");
    setProperty(Property.REQUEST_HEADERS,
        "Accept: text/*;q=0.3, text/html;q=0.7, text/html;level=1,text/html;level=2;q=0.4, */*;q=0.5");
    setProperty(Property.SEARCH_STRING, "text/html;level=1");
    setProperty(Property.SEARCH_STRING, "text/html;q=0.7");
    setProperty(Property.SEARCH_STRING, "*/*;q=0.5");
    setProperty(Property.SEARCH_STRING, "text/html;level=2;q=0.4");
    setProperty(Property.SEARCH_STRING, "text/*;q=0.3");
    invokeRequestAndCheckResponse(ContextOperation.GETACCEPTABLEMEDIATYPES);
  }

  /*
   * @testName: getAcceptableMediaTypesIsReadOnlyTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:651; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Get a read-only list of acceptable languages sorted
   * according to their q-value.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void getAcceptableMediaTypesIsReadOnlyTest() throws Fault {
    setProperty(Property.REQUEST_HEADERS, buildAccept(MediaType.TEXT_XML_TYPE));
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH, MediaType.APPLICATION_JSON);
    invokeRequestAndCheckResponse(
        ContextOperation.GETACCEPTABLEMEDIATYPESISREADONLY);
  }

  /*
   * @testName: getCookiesTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:652; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Get any cookies that accompanied the request.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void getCookiesTest() throws Fault {
    String[] cookies = { "cookie1", "coookkkie99", "cookiiieee999" };
    for (String cookie : cookies) {
      String sCookie = new StringBuilder().append("Cookie: $Version=1; ")
          .append(cookie).append("=").append(cookie).append(";").toString();
      setProperty(Property.REQUEST_HEADERS, sCookie);
      setProperty(Property.SEARCH_STRING, cookie);
      invokeRequestAndCheckResponse(ContextOperation.GETCOOKIES);
    }
  }

  /*
   * @testName: getCookiesIsReadonlyTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:652; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Get any cookies that accompanied the request. Returns : a
   * read-only map of cookie name (String) to Cookie.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void getCookiesIsReadonlyTest() throws Fault {
    setPrintEntity(true);
    invokeRequestAndCheckResponse(ContextOperation.GETCOOKIESISREADONLY);
  }

  /*
   * @testName: getDateTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:653; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: the message date, otherwise null if not present.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void getDateTest() throws Fault {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.MILLISECOND, 0);
    Date date = calendar.getTime();
    String gmt = JaxrsUtil.createDateFormat(TimeZone.getTimeZone("GMT"))
        .format(date);
    setProperty(Property.REQUEST_HEADERS, "Date:" + gmt);
    setProperty(Property.SEARCH_STRING, String.valueOf(date.getTime()));
    invokeRequestAndCheckResponse(ContextOperation.GETDATE);
  }

  /*
   * @testName: getDateIsNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:653; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: the message date, otherwise null if not present.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void getDateIsNullTest() throws Fault {
    setProperty(Property.SEARCH_STRING, "NULL");
    invokeRequestAndCheckResponse(ContextOperation.GETDATE);
  }

  /*
   * @testName: getEntityStreamTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:654; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Get the entity input stream.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void getEntityStreamTest() throws Fault {
    String entity = "EnTiTyStReAmTeSt";
    setProperty(Property.CONTENT, entity);
    setProperty(Property.SEARCH_STRING, entity);
    invokeRequestAndCheckResponse(Request.POST,
        ContextOperation.GETENTITYSTREAM);
  }

  /*
   * @testName: getHeadersTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:655; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Get mutable multivalued map of request headers.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void getHeadersTest() throws Fault {
    for (int i = 1; i != 5; i++) {
      String header = "header" + i + ":" + "header" + i;
      setProperty(Property.REQUEST_HEADERS, header);
      setProperty(Property.UNORDERED_SEARCH_STRING, header);
    }
    invokeRequestAndCheckResponse(ContextOperation.GETHEADERS);
  }

  /*
   * @testName: getHeadersIsMutableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:655; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Get mutable multivalued map of request headers.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void getHeadersIsMutableTest() throws Fault {
    setPrintEntity(true);
    invokeRequestAndCheckResponse(ContextOperation.GETHEADERSISMUTABLE);
  }

  /*
   * @testName: getHeaderStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:656; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Get a message header as a single string value.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void getHeaderStringTest() throws Fault {
    setProperty(Property.SEARCH_STRING,
        ContextOperation.GETHEADERSTRING2.name());
    invokeRequestAndCheckResponse(ContextOperation.GETHEADERSTRING2);
  }

  /*
   * @testName: getLanguageTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:657; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Get the language of the entity or null if not specified
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void getLanguageTest() throws Fault {
    setProperty(Property.REQUEST_HEADERS,
        HttpHeaders.CONTENT_LANGUAGE + ":en-gb");
    setProperty(Property.SEARCH_STRING, "en-gb");
    invokeRequestAndCheckResponse(ContextOperation.GETLANGUAGE);
  }

  /*
   * @testName: getLanguageIsNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:657; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Get the language of the entity or null if not specified
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void getLanguageIsNullTest() throws Fault {
    setProperty(Property.SEARCH_STRING, "NULL");
    invokeRequestAndCheckResponse(ContextOperation.GETLANGUAGE);
  }

  /*
   * @testName: getLengthTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:658; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Get Content-Length as integer if present and valid number.
   * In other cases returns -1.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void getLengthTest() throws Fault {
    setProperty(Property.CONTENT, "12345678901234567890");
    setProperty(Property.SEARCH_STRING, "20");
    invokeRequestAndCheckResponse(Request.POST, ContextOperation.GETLENGTH);
  }

  /*
   * @testName: getLengthWhenNoEntityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:658; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Get Content-Length as integer if present and valid number.
   * In other cases returns -1.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void getLengthWhenNoEntityTest() throws Fault {
    setProperty(Property.SEARCH_STRING, "-1");
    invokeRequestAndCheckResponse(ContextOperation.GETLENGTH);
  }

  /*
   * @testName: getMediaTypeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:659; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: the media type or null if not specified (e.g. there's no
   * request entity).
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void getMediaTypeTest() throws Fault {
    addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_SVG_XML);
    setProperty(Property.SEARCH_STRING, MediaType.APPLICATION_SVG_XML);
    invokeRequestAndCheckResponse(ContextOperation.GETMEDIATYPE);
  }

  /*
   * @testName: getMediaTypeIsNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:659; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: the media type or null if not specified (e.g. there's no
   * request entity).
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void getMediaTypeIsNullTest() throws Fault {
    setProperty(Property.SEARCH_STRING, "NULL");
    invokeRequestAndCheckResponse(ContextOperation.GETMEDIATYPE);
  }

  /*
   * @testName: getMethodTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:660; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Get the request method.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void getMethodTest() throws Fault {
    String method = Request.OPTIONS.name();
    String header = RequestFilter.OPERATION + ":"
        + ContextOperation.GETMETHOD.name();
    getTestCase().setRequestType(method);
    getTestCase().setUrlRequest(getContextRoot());
    setProperty(Property.SEARCH_STRING_IGNORE_CASE, method);
    setProperty(Property.REQUEST_HEADERS, header);
    invoke();
  }

  /*
   * @testName: getPropertyIsNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:661; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Returns the property with the given name registered in the
   * current request/response exchange context, or null if there is no property
   * by that name.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void getPropertyIsNullTest() throws Fault {
    setProperty(Property.SEARCH_STRING, "NULL");
    invokeRequestAndCheckResponse(ContextOperation.GETPROPERTY);
  }

  /*
   * @testName: getPropertyNamesTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:986; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Returns immutable java.util.Collection collection
   * containing the property names available within the context of the current
   * request/response exchange context.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void getPropertyNamesTest() throws Fault {
    for (int i = 0; i != 5; i++)
      setProperty(Property.UNORDERED_SEARCH_STRING,
          TemplateFilter.PROPERTYNAME.toLowerCase() + i);
    invokeRequestAndCheckResponse(ContextOperation.GETPROPERTYNAMES);
  }

  /*
   * @testName: getPropertyNamesIsReadOnlyTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:986;
   * 
   * @test_Strategy: Returns immutable java.util.Collection collection
   * containing the property names available within the context of the current
   * request/response exchange context.
   */
  @Test
  @Tag("servlet")
  public void getPropertyNamesIsReadOnlyTest() throws Fault {
    setProperty(Property.UNORDERED_SEARCH_STRING, "0");
    invokeRequestAndCheckResponse(ContextOperation.GETPROPERTYNAMESISREADONLY);
  }

  /*
   * @testName: getRequestTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:663; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Get the injectable request information.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void getRequestTest() throws Fault {
    String method = Request.OPTIONS.name();
    String header = RequestFilter.OPERATION + ":"
        + ContextOperation.GETREQUEST.name();
    getTestCase().setRequestType(method);
    getTestCase().setUrlRequest(getContextRoot());
    setProperty(Property.SEARCH_STRING_IGNORE_CASE, method);
    setProperty(Property.REQUEST_HEADERS, header);
    invoke();
  }

  /*
   * @testName: getSecurityContextPrincipalIsNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:664; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Get the injectable security context information for the
   * current request. The SecurityContext.getUserPrincipal() must return null if
   * the current request has not been authenticated.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void getSecurityContextPrincipalIsNullTest() throws Fault {
    setProperty(Property.SEARCH_STRING, "NULL");
    invokeRequestAndCheckResponse(ContextOperation.GETSECURITYCONTEXT);
  }

  /*
   * @testName: getUriInfoTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:665; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Get request URI information.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void getUriInfoTest() throws Fault {
    setProperty(Property.SEARCH_STRING, getAbsoluteUrl());
    invokeRequestAndCheckResponse(ContextOperation.GETURIINFO);
  }

  /*
   * @testName: hasEntityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:666; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Check if there is a non-empty entity input stream available
   * in the request message.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void hasEntityTest() throws Fault {
    setRequestContentEntity("entity");
    setProperty(Property.SEARCH_STRING, "true");
    invokeRequestAndCheckResponse(Request.POST, ContextOperation.HASENTITY);
  }

  /*
   * @testName: hasEntityWhenNoEntityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:666; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Check if there is a non-empty entity input stream available
   * in the request message.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void hasEntityWhenNoEntityTest() throws Fault {
    setProperty(Property.SEARCH_STRING, "false");
    invokeRequestAndCheckResponse(ContextOperation.HASENTITY);
  }

  /*
   * @testName: removePropertyTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:667; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Removes a property with the given name from the current
   * request/response exchange context. After removal, subsequent calls to
   * getProperty(java.lang.String) to retrieve the property value will return
   * null.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void removePropertyTest() throws Fault {
    // getProperty returns null after the property has been set and removed
    setProperty(Property.SEARCH_STRING, "NULL");
    invokeRequestAndCheckResponse(ContextOperation.REMOVEPROPERTY);
  }

  /*
   * @testName: setEntityStreamTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:668; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Set a new entity input stream.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void setEntityStreamTest() throws Fault {
    setProperty(Property.SEARCH_STRING, RequestFilter.SETENTITYSTREAMENTITY);
    invokeRequestAndCheckResponse(ContextOperation.SETENTITYSTREAM);
  }

  /*
   * @testName: setMethodTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:669; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Set the request method.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void setMethodTest() throws Fault {
    setProperty(Property.SEARCH_STRING, Request.OPTIONS.name());
    invokeRequestAndCheckResponse(ContextOperation.SETMETHOD);
  }

  /*
   * @testName: setPropertyTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:671; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Binds an object to a given property name in the current
   * request/response exchange context. If the name specified is already used
   * for a property, this method will replace the value of the property with the
   * new value.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void setPropertyTest() throws Fault {
    setProperty(Property.SEARCH_STRING, TemplateFilter.PROPERTYNAME);
    invokeRequestAndCheckResponse(ContextOperation.SETPROPERTY);
  }

  /*
   * @testName: setPropertyNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:671; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: If a null value is passed, the effect is the same as
   * calling the removeProperty(String) method. Will replace the value of the
   * property with the new value.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException. *
   */
  @Test
  @Tag("servlet")
  public void setPropertyNullTest() throws Fault {
    setProperty(Property.SEARCH_STRING, "NULL");
    invokeRequestAndCheckResponse(ContextOperation.SETPROPERTYNULL);
  }

  /*
   * @testName: setPropertyIsReflectedInServletRequestTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:671; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: In a Servlet container, the properties are synchronized
   * with the ServletRequest and expose all the attributes available in the
   * ServletRequest. Any modifications of the properties are also reflected in
   * the set of properties of the associated ServletRequest.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException. *
   */
  @Test
  @Tag("servlet")
  public void setPropertyIsReflectedInServletRequestTest() throws Fault {
    setProperty(Property.SEARCH_STRING, RequestFilter.PROPERTYNAME);
    invokeRequestAndCheckResponse(ContextOperation.SETPROPERTYCONTEXT);
  }

  /*
   * @testName: setRequestUriOneUriTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:672; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Set a new request URI using the current base URI of the
   * application to resolve the application-specific request URI part.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void setRequestUriOneUriTest() throws Fault {
    setProperty(Property.SEARCH_STRING, RequestFilter.URI);
    invokeRequestAndCheckResponse(ContextOperation.SETREQUESTURI1);
  }

  /*
   * @testName: setRequestUriTwoUrisTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:674; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Set a new request URI using a new base URI to resolve the
   * application-specific request URI part.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void setRequestUriTwoUrisTest() throws Fault {
    setProperty(Property.SEARCH_STRING, RequestFilter.URI);
    invokeRequestAndCheckResponse(ContextOperation.SETREQUESTURI2);
  }

  /*
   * @testName: setSecurityContextTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:676; JAXRS:JAVADOC:677; JAXRS:JAVADOC:678;
   * 
   * @test_Strategy: Set a new injectable security context information for the
   * current request.
   * 
   * Filter method called before a request has been dispatched to a resource.
   * Throws IOException.
   */
  @Test
  @Tag("servlet")
  public void setSecurityContextTest() throws Fault {
    setProperty(Property.SEARCH_STRING, RequestFilter.PRINCIPAL);
    invokeRequestAndCheckResponse(ContextOperation.SETSECURITYCONTEXT);
  }

  // ////////////////////////////////////////////////////////////////////////////

  protected void invokeRequestAndCheckResponse(
      ContextOperation contextOperation) throws Fault {
    invokeRequestAndCheckResponse(Request.GET, contextOperation);
  }

  protected void invokeRequestAndCheckResponse(Request req,
      ContextOperation contextOperation) throws Fault {
    String operation = contextOperation.name();
    String request = buildRequest(req, operation.toLowerCase());
    String header = RequestFilter.OPERATION + ":" + operation;
    setProperty(Property.REQUEST, request);
    setProperty(Property.SEARCH_STRING, operation);
    setProperty(Property.REQUEST_HEADERS, header);
    invoke();
  }
}
