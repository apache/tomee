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

package ee.jakarta.tck.ws.rs.spec.resource.requestmatching;

import java.io.InputStream;
import java.io.IOException;

import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

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
public class JAXRSClientIT extends JAXRSCommonClient {

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_spec_resource_requestmatching_web");
  }

 
  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException{
    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/spec/resource/requestmatching/web.xml.template");
    String webXml = editWebXmlString(inStream);
    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_spec_resource_requestmatching_web.war");
    archive.addClasses(TSAppConfig.class, MainResource.class, EmptyResource.class, AnotherResourceLocator.class, AnotherSubResource.class, ExceptionMatcher.class, LocatorResource.class, MainResourceLocator.class, MainSubResource.class, YetAnotherSubresource.class);
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
   * @testName: emptyUriTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.1;
   * 
   * @test_Strategy: If E is empty then no matching resource can be found, the
   * algorithm terminates and an implementation MUST generate a
   * WebApplicationException with a not found response (HTTP 404 status) and no
   * entity. The exception MUST be processed as described in section 3.3.4.
   * 
   * Make sure the server does not return 404 every time
   */
  @SuppressWarnings("incomplete-switch")
  public void emptyUriTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "").replace("/ ", " "));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.OK));

    setProperty(Property.STATUS_CODE, getStatusCode(Status.MOVED_PERMANENTLY));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.FOUND));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.SEE_OTHER));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.TEMPORARY_REDIRECT));
    invoke();

    switch (getResponseStatusCode()) {
    case OK:
      String search = EmptyResource.class.getSimpleName();
      assertResponseBodyContain(search);
      break;
    case MOVED_PERMANENTLY:
    case FOUND:
    case SEE_OTHER:
    case TEMPORARY_REDIRECT:
      search = getContextRoot() + "/";
      assertResponseHeadersContain(search);
      break;
    }
  }

  /*
   * @testName: slashUriTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.1;
   * 
   * @test_Strategy: If E is empty then no matching resource can be found, the
   * algorithm terminates and an implementation MUST generate a
   * WebApplicationException with a not found response (HTTP 404 status) and no
   * entity. The exception MUST be processed as described in section 3.3.4.
   * 
   * Make sure the server does not return 404 every time
   */
  @Test
  public void slashUriTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, ""));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.OK));
    setProperty(Property.SEARCH_STRING, EmptyResource.class.getSimpleName());
    invoke();
  }

  /*
   * @testName: slashWrongUriTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.1;
   * 
   * @test_Strategy: If E is empty then no matching resource can be found, the
   * algorithm terminates and an implementation MUST generate a
   * WebApplicationException with a not found response (HTTP 404 status) and no
   * entity. The exception MUST be processed as described in section 3.3.4.
   */
  @Test
  public void slashWrongUriTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "wrong"));
    setProperty(Property.SEARCH_STRING, getStatusCode(Status.NOT_FOUND));
    invoke();
  }

  /*
   * @testName: wrongAppNameTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.1;
   * 
   * @test_Strategy: If E is empty then no matching resource can be found, the
   * algorithm terminates and an implementation MUST generate a
   * WebApplicationException with a not found response (HTTP 404 status) and no
   * entity. The exception MUST be processed as described in section 3.3.4.
   */
  @Test
  public void wrongAppNameTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "").replace("web", "wrong"));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.NOT_FOUND));
    invoke();
  }

  /*
   * @testName: slashAppNameTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.1;
   * 
   * @test_Strategy: If E is empty then no matching resource can be found, the
   * algorithm terminates and an implementation MUST generate a
   * WebApplicationException with a not found response (HTTP 404 status) and no
   * entity. The exception MUST be processed as described in section 3.3.4.
   */
  @Test
  public void slashAppNameTest() throws Fault {
    setProperty(Property.REQUEST,
        Request.GET.name() + " /" + MainResource.ID + HTTP11);
    setProperty(Property.STATUS_CODE, getStatusCode(Status.NOT_FOUND));
    invoke();
  }

  /*
   * @testName: descendantResourcePathValueTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.2;
   * 
   * @test_Strategy: Sort E using the number of literal characters in each
   * member as the primary key (descending order), the number of capturing
   * groups as a secondary key (descending order) and the number of capturing
   * groups with non-default regular expressions as the tertiary key (descending
   * order).
   */
  @Test
  public void descendantResourcePathValueTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "resource/subresource"));
    setProperty(Property.SEARCH_STRING, MainSubResource.class.getSimpleName());
    invoke();
  }

  /*
   * @testName: descendantSubResourcePathValueTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.2;
   * 
   * @test_Strategy: Sort E using the number of literal characters5 in each
   * member as the primary key (descending order), the number of capturing
   * groups as a secondary key (descending order) and the number of capturing
   * groups with non-default regular expressions as the tertiary key (descending
   * order).
   */
  @Test
  public void descendantSubResourcePathValueTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.POST, "resource/subresource/sub"));
    setProperty(Property.SEARCH_STRING,
        AnotherSubResource.class.getSimpleName());
    invoke();
  }

  /*
   * @testName: resourceLocatorTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.3;
   * 
   * @test_Strategy: Sort E using the number of literal characters in each
   * member as the primary key (descending order), the number of capturing
   * groups as a secondary key (descending order), the number of capturing
   * groups with non-default regular expressions as the tertiary key (descending
   * order), and the source of each member as quaternary key sorting those
   * derived from method ahead of those derived from locator.
   * 
   * @Path on method has precedence over on resource locator
   */
  @Test
  public void resourceLocatorTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "resource/locator/locator"));
    setProperty(Property.SEARCH_STRING, LocatorResource.class.getSimpleName());
    invoke();
  }

  /*
   * @testName: foundAnotherResourceLocatorByPathTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.3;
   * 
   * @test_Strategy: Sort E using the number of literal characters in each
   * member as the primary key (descending order), the number of capturing
   * groups as a secondary key (descending order), the number of capturing
   * groups with non-default regular expressions as the tertiary key (descending
   * order), and the source of each member as quaternary key sorting those
   * derived from method ahead of those derived from locator.
   * 
   * Check Resource locator finds subresource
   */
  @Test
  public void foundAnotherResourceLocatorByPathTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "resource/locator/sub"));
    setProperty(Property.SEARCH_STRING,
        AnotherResourceLocator.class.getSimpleName());
    invoke();
  }

  /*
   * @testName: locatorNameTooLongTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.1;
   * 
   * @test_Strategy: If E is empty then no matching resource can be found, the
   * algorithm terminates and an implementation MUST generate a
   * WebApplicationException with a not found response (HTTP 404 status) and no
   * entity. The exception MUST be processed as described in section 3.3.4.
   * 
   * From Alg.2
   */
  @Test
  public void locatorNameTooLongTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "resource/locator/sub/locator"));
    setProperty(Property.SEARCH_STRING, getStatusCode(Status.NOT_FOUND));
    invoke();
  }

  /*
   * @testName: locatorNameTooLongAgainTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.1;
   * 
   * @test_Strategy: If E is empty then no matching resource can be found, the
   * algorithm terminates and an implementation MUST generate a
   * WebApplicationException with a not found response (HTTP 404 status) and no
   * entity. The exception MUST be processed as described in section 3.3.4.
   * 
   * From Alg.2
   */
  @Test
  public void locatorNameTooLongAgainTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "resource/locator/locator/locator"));
    setProperty(Property.SEARCH_STRING, getStatusCode(Status.NOT_FOUND));
    invoke();
  }

  /*
   * @testName: methodNotFoundTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.1;
   * 
   * @test_Strategy: If E is empty then no matching resource can be found, the
   * algorithm terminates and an implementation MUST generate a
   * WebApplicationException with a not found response (HTTP 404 status) and no
   * entity. The exception MUST be processed as described in section 3.3.4.
   * 
   * From Alg.2
   */
  @Test
  public void methodNotFoundTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "resource/locator/test"));
    setProperty(Property.SEARCH_STRING, getStatusCode(Status.NOT_FOUND));
    invoke();
  }

  /*
   * @testName: requestNotSupportedOnResourceTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.4;
   * 
   * @test_Strategy: The request method is supported. If no methods support the
   * request method an implementation MUST generate a WebApplicationException
   * with a method not allowed response (HTTP 405 status) and no entity. The
   * exception MUST be processed as described in section 3.3.4. Note the
   * additional support for HEAD and OPTIONS described in section 3.3.5.
   */
  @Test
  public void requestNotSupportedOnResourceTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.PUT, "resource/something"));
    setProperty(Property.SEARCH_STRING,
        getStatusCode(Status.METHOD_NOT_ALLOWED));
    invoke();
  }

  /*
   * @testName: requestNotSupportedOnSubResourceTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.4;
   * 
   * @test_Strategy: The request method is supported. If no methods support the
   * request method an implementation MUST generate a WebApplicationException
   * with a method not allowed response (HTTP 405 status) and no entity. The
   * exception MUST be processed as described in section 3.3.4. Note the
   * additional support for HEAD and OPTIONS described in section 3.3.5.
   */
  @Test
  public void requestNotSupportedOnSubResourceTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.PUT, "resource/subresource/something"));
    setProperty(Property.SEARCH_STRING,
        getStatusCode(Status.METHOD_NOT_ALLOWED));
    invoke();
  }

  /*
   * @testName: requestNotSupportedOnResourceLocatorTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.4;
   * 
   * @test_Strategy: The request method is supported. If no methods support the
   * request method an implementation MUST generate a WebApplicationException
   * with a method not allowed response (HTTP 405 status) and no entity. The
   * exception MUST be processed as described in section 3.3.4. Note the
   * additional support for HEAD and OPTIONS described in section 3.3.5.
   */
  @Test
  public void requestNotSupportedOnResourceLocatorTest() throws Fault {
    String request = buildRequest(Request.PUT,
        "resource/subresource/consumeslocator");
    setProperty(Property.REQUEST, request);
    setProperty(Property.SEARCH_STRING,
        getStatusCode(Status.METHOD_NOT_ALLOWED));
    invoke();
  }

  /*
   * @testName: requestNotSupportedOnSubResourceLocatorTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.4;
   * 
   * @test_Strategy: The request method is supported. If no methods support the
   * request method an implementation MUST generate a WebApplicationException
   * with a method not allowed response (HTTP 405 status) and no entity. The
   * exception MUST be processed as described in section 3.3.4. Note the
   * additional support for HEAD and OPTIONS described in section 3.3.5.
   */
  @Test
  public void requestNotSupportedOnSubResourceLocatorTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.PUT, "resource/consumeslocator"));
    setProperty(Property.SEARCH_STRING,
        getStatusCode(Status.METHOD_NOT_ALLOWED));
    invoke();
  }

  /*
   * @testName: optionsOnSubResourceTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.4;
   * 
   * @test_Strategy: The request method is supported. If no methods support the
   * request method an implementation MUST generate a WebApplicationException
   * with a method not allowed response (HTTP 405 status) and no entity. The
   * exception MUST be processed as described in section 3.3.4. Note the
   * additional support for HEAD and OPTIONS described in section 3.3.5.
   */
  @Test
  public void optionsOnSubResourceTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.OPTIONS, "resource/subresource/something"));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.OK));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.NO_CONTENT));
    invoke();
    boolean foundGet = false;
    for (String header : getResponseHeaders())
      if (header.startsWith(HttpHeaders.ALLOW))
        foundGet |= header.contains(Request.GET.name());
    assertTrue(foundGet, "Header Allow: GET was not found");
    logMsg("Header Allow: GET found as expected");
  }

  /*
   * @testName: headOnSubResourceTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.4;
   * 
   * @test_Strategy: The request method is supported. If no methods support the
   * request method an implementation MUST generate a WebApplicationException
   * with a method not allowed response (HTTP 405 status) and no entity. The
   * exception MUST be processed as described in section 3.3.4. Note the
   * additional support for HEAD and OPTIONS described in section 3.3.5.
   */
  @Test
  public void headOnSubResourceTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.HEAD, "resource/subresource/something"));
    invoke();
    String body = getResponseBody();
    assertTrue(body == null, "Unexpected response body"+ body);
  }

  /*
   * @testName: consumesOnResourceTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.5;
   * 
   * @test_Strategy: The media type of the request entity body (if any) is a
   * supported input data format (see Section 3.5). If no methods support the
   * media type of the request entity body an implementation MUST generate a
   * WebApplicationException with an unsupported media type response (HTTP 415
   * status) and no entity. The exception MUST be processed as described in
   * Section 3.3.4.
   */
  @Test
  public void consumesOnResourceTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.POST, "resource/consumes"));
    setProperty(Property.REQUEST_HEADERS,
        buildContentType(MediaType.APPLICATION_ATOM_XML_TYPE));
    setProperty(Property.SEARCH_STRING,
        getStatusCode(Status.UNSUPPORTED_MEDIA_TYPE));
    invoke();
  }

  /*
   * @testName: consumesCorrectContentTypeOnResourceTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.5;
   * 
   * @test_Strategy: The media type of the request entity body (if any) is a
   * supported input data format (see Section 3.5). If no methods support the
   * media type of the request entity body an implementation MUST generate a
   * WebApplicationException with an unsupported media type response (HTTP 415
   * status) and no entity. The exception MUST be processed as described in
   * Section 3.3.4.
   */
  @Test
  public void consumesCorrectContentTypeOnResourceTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.POST, "resource/consumes"));
    setProperty(Property.REQUEST_HEADERS,
        buildContentType(MediaType.TEXT_PLAIN_TYPE));
    setProperty(Property.SEARCH_STRING, MainResource.class.getSimpleName());
    invoke();
  }

  /*
   * @testName: consumesOnResourceLocatorTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.5;
   * 
   * @test_Strategy: The media type of the request entity body (if any) is a
   * supported input data format (see Section 3.5). If no methods support the
   * media type of the request entity body an implementation MUST generate a
   * WebApplicationException with an unsupported media type response (HTTP 415
   * status) and no entity. The exception MUST be processed as described in
   * Section 3.3.4.
   */
  @Test
  public void consumesOnResourceLocatorTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.POST, "resource/consumeslocator"));
    setProperty(Property.REQUEST_HEADERS,
        buildContentType(MediaType.APPLICATION_ATOM_XML_TYPE));
    setProperty(Property.SEARCH_STRING,
        getStatusCode(Status.UNSUPPORTED_MEDIA_TYPE));
    invoke();
  }

  /*
   * @testName: consumesCorrectContentTypeOnResourceLocatorTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.5;
   * 
   * @test_Strategy: The media type of the request entity body (if any) is a
   * supported input data format (see Section 3.5). If no methods support the
   * media type of the request entity body an implementation MUST generate a
   * WebApplicationException with an unsupported media type response (HTTP 415
   * status) and no entity. The exception MUST be processed as described in
   * Section 3.3.4.
   */
  @Test
  public void consumesCorrectContentTypeOnResourceLocatorTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.POST, "resource/consumeslocator"));
    setProperty(Property.REQUEST_HEADERS,
        buildContentType(MediaType.TEXT_PLAIN_TYPE));
    setProperty(Property.SEARCH_STRING,
        MainResourceLocator.class.getSimpleName());
    invoke();
  }

  /*
   * @testName: consumesOnSubResourceLocatorTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.5;
   * 
   * @test_Strategy: The media type of the request entity body (if any) is a
   * supported input data format (see Section 3.5). If no methods support the
   * media type of the request entity body an implementation MUST generate a
   * WebApplicationException with an unsupported media type response (HTTP 415
   * status) and no entity. The exception MUST be processed as described in
   * Section 3.3.4.
   */
  @Test
  public void consumesOnSubResourceLocatorTest() throws Fault {
    String request = buildRequest(Request.POST,
        "resource/subresource/consumeslocator");
    setProperty(Property.REQUEST, request);
    setProperty(Property.REQUEST_HEADERS,
        buildContentType(MediaType.APPLICATION_ATOM_XML_TYPE));
    setProperty(Property.SEARCH_STRING,
        getStatusCode(Status.UNSUPPORTED_MEDIA_TYPE));
    invoke();
  }

  /*
   * @testName: consumesCorrectContentTypeOnSubResourceLocatorTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.5;
   * 
   * @test_Strategy: The media type of the request entity body (if any) is a
   * supported input data format (see Section 3.5). If no methods support the
   * media type of the request entity body an implementation MUST generate a
   * WebApplicationException with an unsupported media type response (HTTP 415
   * status) and no entity. The exception MUST be processed as described in
   * Section 3.3.4.
   */
  @Test
  public void consumesCorrectContentTypeOnSubResourceLocatorTest()
      throws Fault {
    String request = buildRequest(Request.POST,
        "resource/subresource/consumeslocator");
    setProperty(Property.REQUEST, request);
    setProperty(Property.REQUEST_HEADERS,
        buildContentType(MediaType.TEXT_PLAIN_TYPE));
    setProperty(Property.SEARCH_STRING,
        AnotherResourceLocator.class.getSimpleName());
    invoke();
  }

  // ----------------------------------25.6----------------------------------

  /*
   * @testName: producesOnResourceTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.6;
   * 
   * @test_Strategy: At least one of the acceptable response entity body media
   * types is a supported output data format (see Section 3.5). If no methods
   * support one of the acceptable response entity body media types an
   * implementation MUST generate a WebApplicationException with a not
   * acceptable response (HTTP 406 status) and no entity. The exception MUST be
   * processed as described in Section 3.3.4
   */
  @Test
  public void producesOnResourceTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.POST, "resource/produces"));
    setProperty(Property.REQUEST_HEADERS,
        buildAccept(MediaType.APPLICATION_ATOM_XML_TYPE));
    setProperty(Property.SEARCH_STRING, getStatusCode(Status.NOT_ACCEPTABLE));
    invoke();
  }

  /*
   * @testName: producesCorrectContentTypeOnResourceTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.6;
   * 
   * @test_Strategy: At least one of the acceptable response entity body media
   * types is a supported output data format (see Section 3.5). If no methods
   * support one of the acceptable response entity body media types an
   * implementation MUST generate a WebApplicationException with a not
   * acceptable response (HTTP 406 status) and no entity. The exception MUST be
   * processed as described in Section 3.3.4
   */
  @Test
  public void producesCorrectContentTypeOnResourceTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.POST, "resource/produces"));
    setProperty(Property.REQUEST_HEADERS,
        buildAccept(MediaType.TEXT_PLAIN_TYPE));
    setProperty(Property.SEARCH_STRING, MainResource.class.getSimpleName());
    invoke();
  }

  /*
   * @testName: producesOnResourceLocatorTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.6;
   * 
   * @test_Strategy: At least one of the acceptable response entity body media
   * types is a supported output data format (see Section 3.5). If no methods
   * support one of the acceptable response entity body media types an
   * implementation MUST generate a WebApplicationException with a not
   * acceptable response (HTTP 406 status) and no entity. The exception MUST be
   * processed as described in Section 3.3.4
   */
  @Test
  public void producesOnResourceLocatorTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.POST, "resource/produceslocator"));
    setProperty(Property.REQUEST_HEADERS,
        buildAccept(MediaType.APPLICATION_ATOM_XML_TYPE));
    setProperty(Property.SEARCH_STRING, getStatusCode(Status.NOT_ACCEPTABLE));
    invoke();
  }

  /*
   * @testName: producesCorrectContentTypeOnResourceLocatorTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.6;
   * 
   * @test_Strategy: At least one of the acceptable response entity body media
   * types is a supported output data format (see Section 3.5). If no methods
   * support one of the acceptable response entity body media types an
   * implementation MUST generate a WebApplicationException with a not
   * acceptable response (HTTP 406 status) and no entity. The exception MUST be
   * processed as described in Section 3.3.4
   */
  @Test
  public void producesCorrectContentTypeOnResourceLocatorTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.POST, "resource/produceslocator"));
    setProperty(Property.REQUEST_HEADERS,
        buildAccept(MediaType.TEXT_PLAIN_TYPE));
    setProperty(Property.SEARCH_STRING,
        MainResourceLocator.class.getSimpleName());
    invoke();
  }

  /*
   * @testName: producesOnSubResourceLocatorTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.6;
   * 
   * @test_Strategy: At least one of the acceptable response entity body media
   * types is a supported output data format (see Section 3.5). If no methods
   * support one of the acceptable response entity body media types an
   * implementation MUST generate a WebApplicationException with a not
   * acceptable response (HTTP 406 status) and no entity. The exception MUST be
   * processed as described in Section 3.3.4
   */
  @Test
  public void producesOnSubResourceLocatorTest() throws Fault {
    String request = buildRequest(Request.POST,
        "resource/subresource/produceslocator");
    setProperty(Property.REQUEST, request);
    setProperty(Property.REQUEST_HEADERS,
        buildAccept(MediaType.APPLICATION_ATOM_XML_TYPE));
    setProperty(Property.SEARCH_STRING, getStatusCode(Status.NOT_ACCEPTABLE));
    invoke();
  }

  /*
   * @testName: producesCorrectContentTypeOnSubResourceLocatorTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.6;
   * 
   * @test_Strategy: At least one of the acceptable response entity body media
   * types is a supported output data format (see Section 3.5). If no methods
   * support one of the acceptable response entity body media types an
   * implementation MUST generate a WebApplicationException with a not
   * acceptable response (HTTP 406 status) and no entity. The exception MUST be
   * processed as described in Section 3.3.4
   */
  @Test
  public void producesCorrectContentTypeOnSubResourceLocatorTest()
      throws Fault {
    String request = buildRequest(Request.POST,
        "resource/subresource/produceslocator");
    setProperty(Property.REQUEST, request);
    setProperty(Property.REQUEST_HEADERS,
        buildAccept(MediaType.TEXT_PLAIN_TYPE));
    setProperty(Property.SEARCH_STRING,
        AnotherResourceLocator.class.getSimpleName());
    invoke();
  }

  /*
   * @testName: l2SubResourceLocatorTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.3;
   * 
   * @test_Strategy: Check sub-resource locator from sub-resource locator
   */
  @Test
  public void l2SubResourceLocatorTest() throws Fault {
    String request = buildRequest(Request.DELETE,
        "resource/l2locator/l2locator");
    setProperty(Property.REQUEST, request);
    setProperty(Property.SEARCH_STRING,
        AnotherResourceLocator.class.getSimpleName());
    invoke();
  }

  // ----------------------------------25.7----------------------------------
  /*
   * @testName: consumesOverridesDescendantSubResourcePathValueTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.7;
   * 
   * @test_Strategy: The primary key is the media type of input data. Methods
   * whose @Consumes value is the best match for the media type of the request
   * are sorted first.
   * 
   * Like in descendantSubResourcePathValueTest, AnotherSubResource method is
   * used, because MainSubResource is another object
   */
  @Test
  public void consumesOverridesDescendantSubResourcePathValueTest()
      throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.POST, "resource/subresource/sub"));
    setProperty(Property.REQUEST_HEADERS,
        buildContentType(MediaType.TEXT_PLAIN_TYPE));
    String clazz = AnotherSubResource.class.getSimpleName();
    setProperty(Property.SEARCH_STRING, clazz);
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH, clazz + clazz);
    invoke();
  }

  // ----------------------------------25.8----------------------------------
  /*
   * @testName: producesOverridesDescendantSubResourcePathValueTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.8;
   * 
   * @test_Strategy: The secondary key is the @Produces value. Methods whose
   * value of @Produces best matches the value of the request accept header are
   * sorted first.
   * 
   * Like in descendantSubResourcePathValueTest, AnotherSubResource method is
   * used, because MainSubResource is another object
   */
  @Test
  public void producesOverridesDescendantSubResourcePathValueTest()
      throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "resource/subresource/sub"));
    setProperty(Property.REQUEST_HEADERS,
        buildAccept(MediaType.TEXT_PLAIN_TYPE));
    String clazz = AnotherSubResource.class.getSimpleName();
    setProperty(Property.SEARCH_STRING, clazz + clazz);
    invoke();
  }

  /*
   * @testName: producesOverridesDescendantSubResourcePathValuePostTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.7; JAXRS:SPEC:25.8;
   * 
   * @test_Strategy: The secondary key is the @Produces value. Methods whose
   * value of @Produces best matches the value of the request accept header are
   * sorted first.
   * 
   * Like in descendantSubResourcePathValueTest, AnotherSubResource method is
   * used, because MainSubResource is another object
   * 
   * By Post, it is first matched by content-type, which is
   *//*
      * and a better match for AnotherSubResource#sub
      */
  @Test
  public void producesOverridesDescendantSubResourcePathValuePostTest()
      throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.POST, "resource/subresource/sub"));
    setProperty(Property.REQUEST_HEADERS,
        buildAccept(MediaType.TEXT_PLAIN_TYPE));
    String clazz = AnotherSubResource.class.getSimpleName();
    setProperty(Property.SEARCH_STRING, clazz);
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH, clazz + clazz);
    setProperty(Property.EXPECTED_HEADERS,
        buildContentType(MediaType.TEXT_PLAIN_TYPE));
    invoke();
  }

  // ----------------------------------25.9----------------------------------
  /*
   * @testName: concreteOverStarWhenAcceptStarTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.9;
   * 
   * @test_Strategy: n1/m1 > n2/m2 where the partial order > is defined as n/m >
   * n/* >
   *//*
     */
  @Test
  public void concreteOverStarWhenAcceptStarTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "yas"));
    setProperty(Property.REQUEST_HEADERS, "Accept: testi/*");
    setProperty(Property.SEARCH_STRING, "test/text");
    invoke();
  }

  // ----------------------------------25.10----------------------------------

  /*
   * @testName: qualityDeterminesTextATest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.10; JAXRS:SPEC:78;
   * 
   * @test_Strategy: n2/m2 /> n1/m1 and v1 > v2
   * 
   * When accepting multiple media types, clients may indicate preferences by
   * using a relative quality factor known as the q parameter. The value of the
   * q parameter, or q-value, is used to sort the set of accepted
   */
  @Test
  public void qualityDeterminesTextATest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "yas"));
    setProperty(Property.REQUEST_HEADERS,
        "Accept: testii/texta;q=0.5, testii/textb;q=0.4");
    setProperty(Property.SEARCH_STRING, "textA");
    invoke();
  }

  /*
   * @testName: qualityDeterminesTextBTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.10; JAXRS:SPEC:78;
   * 
   * @test_Strategy: n2/m2 /> n1/m1 and v1 > v2
   * 
   * When accepting multiple media types, clients may indicate preferences by
   * using a relative quality factor known as the q parameter. The value of the
   * q parameter, or q-value, is used to sort the set of accepted
   */
  @Test
  public void qualityDeterminesTextBTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "yas"));
    setProperty(Property.REQUEST_HEADERS,
        "Accept: testii/texta;q=0.4, testii/textb;q=0.5");
    setProperty(Property.SEARCH_STRING, "textB");
    invoke();
  }

  // ----------------------------------25.11----------------------------------

  /*
   * @testName: producesOverridesDescendantSubResourcePathValueWeightTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.11; JAXRS:SPEC:79;
   * 
   * @test_Strategy: n2/m2 /> n1/m1 and v1 = v2 and v1' > v2'
   * 
   * A server can also indicate media type preference using the qs parameter;
   * server preference is only examined when multiple media types are accepted
   * by a client with the same q-value.
   */
  @Test
  public void producesOverridesDescendantSubResourcePathValueWeightTest()
      throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "yas"));
    setProperty(Property.REQUEST_HEADERS, buildAccept(MediaType.TEXT_XML_TYPE));
    setProperty(Property.SEARCH_STRING, "text/*");
    invoke();
  }

  /*
   * @testName: qualityOfSourceOnDifferentMediaTypesTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.11; JAXRS:SPEC:79;
   * 
   * @test_Strategy: n2/m2 /> n1/m1 and v1 = v2 and v1' > v2'
   * 
   * A server can also indicate media type preference using the qs parameter;
   * server preference is only examined when multiple media types are accepted
   * by a client with the same q-value.
   */
  @Test
  public void qualityOfSourceOnDifferentMediaTypesTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "yas"));
    setProperty(Property.REQUEST_HEADERS,
        "Accept: testiii/textiii, application/xml");
    setProperty(Property.SEARCH_STRING, MediaType.APPLICATION_XML);
    invoke();
  }

  // ----------------------------------25.12----------------------------------

  /*
   * @testName: concreteOverStarTest
   * 
   * @assertion_ids: JAXRS:SPEC:25; JAXRS:SPEC:25.12;
   * 
   * @test_Strategy: n2/m2 /> n1/m1 and v1 = v2 and v1' = v2' and v1'' <= v2''
   */
  @Test
  public void concreteOverStarTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "yas"));
    setProperty(Property.REQUEST_HEADERS, "Accept: testi/text");
    setProperty(Property.SEARCH_STRING, "test/text");
    invoke();
  }

}
