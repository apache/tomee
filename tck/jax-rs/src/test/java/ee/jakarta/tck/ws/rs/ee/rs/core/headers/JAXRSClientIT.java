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

package ee.jakarta.tck.ws.rs.ee.rs.core.headers;

import java.io.IOException;
import java.text.DateFormat;
import java.util.TimeZone;
import java.io.InputStream;

import ee.jakarta.tck.ws.rs.common.client.JaxrsCommonClient;
import ee.jakarta.tck.ws.rs.common.provider.StringBean;
import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Variant;

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

  private static final long serialVersionUID = -5727774504018187299L;

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_ee_core_headers_web/HeadersTest");
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

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/core/headers/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_core_headers_web.war");
    archive.addClasses(TSAppConfig.class, HttpHeadersTest.class);
    archive.setWebXML(new StringAsset(webXml));
    return archive;

  }


  /* Run test */
  /*
   * @testName: cookieTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.6; JAXRS:JAVADOC:77;
   * 
   * @test_Strategy: Client invokes GET request on root resource at
   * /HeadersTest/cookie with Cookie set; Verify that all Cookies properties are
   * set by the request
   */
  @Test
  public void cookieTest() throws Fault {
    setProperty(Property.REQUEST_HEADERS,
        "Cookie: $Version=1; name1=value1; $Domain=" + _hostname
            + "; $Path=/jaxrs_ee_core_headers_web");
    setProperty(Property.REQUEST, buildRequest(Request.GET, "cookie"));
    setProperty(Property.SEARCH_STRING,
        "getCookie|Cookie Name=name1|Cookie Value=value1"
            + "|Cookie Path=/jaxrs_ee_core_headers_web" + "|Cookie Domain="
            + "|Cookie Version=1");
    invoke();
  }

  /*
   * @testName: acceptLanguageTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.6; JAXRS:JAVADOC:75;
   * 
   * @test_Strategy: Client invokes GET request on root resource at
   * /HeadersTes/acl with Language Header set; Verify that HttpHeaders got the
   * property set by the request
   */
  @Test
  public void acceptLanguageTest() throws Fault {
    setProperty(Property.REQUEST_HEADERS,
        buildAccept(MediaType.TEXT_PLAIN_TYPE));
    setProperty(Property.REQUEST_HEADERS, "Accept-Language:en-US");
    setProperty(Property.REQUEST, buildRequest(Request.GET, "acl"));
    setProperty(Property.SEARCH_STRING, "Accept-Language|en-US");
    invoke();
  }

  /*
   * @testName: contentLanguageTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.6; JAXRS:JAVADOC:78;
   * 
   * @test_Strategy: Client invokes PUT request on root resource at /HeadersTest
   * with Language Header set; Verify that HttpHeaders got the property set by
   * the request
   */
  @Test
  public void contentLanguageTest() throws Fault {
    setProperty(Property.REQUEST_HEADERS,
        buildAccept(MediaType.TEXT_PLAIN_TYPE));
    Variant variant = new Variant(MediaType.WILDCARD_TYPE, "en-US", null);
    Entity<String> entity = Entity.entity("anything", variant);
    setRequestContentEntity(entity);
    setProperty(Property.REQUEST, buildRequest(Request.PUT, ""));
    setProperty(Property.SEARCH_STRING_IGNORE_CASE, "Content-Language|en-US");
    invoke();
  }

  /*
   * @testName: mediaTypeTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.6; JAXRS:SPEC:33.8; JAXRS:JAVADOC:79;
   * 
   * @test_Strategy: Client invokes GET request on a sub resource at
   * /HeadersTest/mt with Content-Type Header set; Verify that HttpHeaders got
   * the property set by the request
   */
  @Test
  public void mediaTypeTest() throws Fault {
    setProperty(Property.REQUEST_HEADERS,
        "Content-Type:application/xml;charset=utf8");
    setProperty(Property.REQUEST, buildRequest(Request.GET, "mt"));
    setProperty(Property.SEARCH_STRING, "getMediaType|application/xml"
        + "|MediaType size=1|Key charset|Value utf8");
    invoke();
  }

  /*
   * @testName: mediaTypeAcceptableTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.6; JAXRS:JAVADOC:76;
   * 
   * @test_Strategy: Client invokes GET request on a sub resource at
   * /HeadersTest/amt with Accept MediaType Header set; Verify that HttpHeaders
   * got the property set by the request
   */
  @Test
  public void mediaTypeAcceptableTest() throws Fault {
    setProperty(Property.REQUEST_HEADERS,
        "Accept:text/*, text/html, text/html;level=1, */*");
    setProperty(Property.REQUEST, buildRequest(Request.GET, "amt"));
    setProperty(Property.UNORDERED_SEARCH_STRING,
        "getAcceptableMediaTypes|text/*|text/html|text/html|*/*");
    invoke();
  }

  /*
   * @testName: requestHeadersTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.6; JAXRS:SPEC:33.8; JAXRS:SPEC:40;
   * JAXRS:JAVADOC:80; JAXRS:JAVADOC:81;
   * 
   * @test_Strategy: Client invokes GET request on a sub resource at
   * /HeadersTest/sub2 with Accept MediaType and Content-Type Headers set;
   * Verify that HttpHeaders got the property set by the request
   */
  @Test
  public void requestHeadersTest() throws Fault {
    setProperty(Property.REQUEST_HEADERS,
        "Accept:text/*, text/html, text/html;level=1, */*");
    setProperty(Property.REQUEST_HEADERS,
        "Content-Type:application/xml;charset=utf8");
    setProperty(Property.REQUEST, buildRequest(Request.GET, "headers"));
    setProperty(Property.SEARCH_STRING, "getRequestHeaders=|Accept:|text/*");
    setProperty(Property.SEARCH_STRING, "text/html|text/html|*/*");
    setProperty(Property.SEARCH_STRING, "Content-Type:|application/xml");
    setProperty(Property.SEARCH_STRING, "charset=utf8");
    invoke();
  }

  /*
   * @testName: getDateTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:779;
   * 
   * @test_Strategy: Get message date
   */
  @Test
  public void getDateTest() throws Fault {
    long currentTime = System.currentTimeMillis();
    DateFormat format = JaxrsUtil.createDateFormat(TimeZone.getTimeZone("GMT"));
    String gmt = format.format(currentTime);

    setProperty(Property.REQUEST, buildRequest(Request.POST, "date"));
    setProperty(Property.CONTENT, "getDate");
    setProperty(Property.REQUEST_HEADERS, "Date:" + gmt);
    invoke();
    long responseTime = Long.parseLong(getResponseBody());
    boolean check = Math.abs(currentTime - responseTime) < 1001L;
    assertTrue(check, "HttpHeaders.getDate()="+ responseTime+
        "differs from expected"+ currentTime+ "by more than 1000 ms.");
    logMsg("#getDate() returned expected Date instance");
  }

  /*
   * @testName: getHeaderStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:780;
   * 
   * @test_Strategy: Get a HTTP header as a single string value.
   */
  @Test
  public void getHeaderStringTest() throws Fault {
    String[] headers = { "askdjb", "ksadbva", "klwiaslkfn", "klwvasbk" };
    for (String header : headers) {
      setProperty(Property.REQUEST, buildRequest(Request.POST, "headerstring"));
      setProperty(Property.CONTENT, header);
      setProperty(Property.REQUEST_HEADERS, header + ":" + header + header);
      setProperty(Property.SEARCH_STRING, header + header);
      invoke();
    }
    logMsg("#getHeaderString() returned expected header values");
  }

  /*
   * @testName: getHeaderStringUsesToStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:780;
   * 
   * @test_Strategy: Get a HTTP header as a single string value. Each single
   * header value is converted to String using its toString method if a header
   * delegate is not available.
   */
  @Test
  public void getHeaderStringUsesToStringTest() throws Fault {
    final StringBean bean = new StringBean("bean");
    ClientRequestFilter filter = new ClientRequestFilter() {
      @Override
      public void filter(ClientRequestContext ctx) throws IOException {
        ctx.getHeaders().add(bean.get(), bean);
      }
    };
    setProperty(Property.REQUEST, buildRequest(Request.POST, "headerstring"));
    setProperty(Property.CONTENT, bean.get());
    addProvider(filter);
    setProperty(Property.SEARCH_STRING, bean.toString());
    invoke();
    logMsg(
        "#getHeaderString() returned expected header converted by toString() method");
  }

  /*
   * @testName: getLengthTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:781;
   * 
   * @test_Strategy: Get Content-Length value
   */
  @Test
  public void getLengthTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "length"));
    invoke();
    String body = getResponseBody();
    assertTrue(body != null && body.length() > 0, "Nothing returned");
    long length = Long.parseLong(body);
    assertTrue(length != 0, "Nothing returned");
    logMsg("#getLength() returned expected length", body);
  }
}
