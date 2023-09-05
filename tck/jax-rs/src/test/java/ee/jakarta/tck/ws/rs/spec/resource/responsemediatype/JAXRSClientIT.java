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

package ee.jakarta.tck.ws.rs.spec.resource.responsemediatype;

import java.util.List;
import java.io.InputStream;
import java.io.IOException;

import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

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
    setContextRoot("/jaxrs_spec_resource_responsemediatype_web");
  }

 
  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException{
    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/spec/resource/responsemediatype/web.xml.template");
    String webXml = editWebXmlString(inStream);
    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_spec_resource_responsemediatype_web.war");
    archive.addClasses(TSAppConfig.class, MediaResource.class, ErrorResource.class, MediaWriter.class, NoMediaResource.class, StringWriter.class, WeightResource.class);
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
   * @testName: responseOverrideTest
   * 
   * @assertion_ids: JAXRS:SPEC:26; JAXRS:SPEC:26.1;
   * 
   * @test_Strategy: If the method returns an instance of Response whose
   * metadata includes the response media type (Mspecified) then set Mselected =
   * Mspecified, finish.
   */
  @Test
  public void responseOverrideTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "resource/responseoverride"));
    setProperty(Property.EXPECTED_HEADERS,
        buildContentType(MediaType.APPLICATION_XML_TYPE));
    invoke();
  }

  /*
   * @testName: responseNotAllowedToOverrideTest
   * 
   * @assertion_ids: JAXRS:SPEC:26; JAXRS:SPEC:26.1; JAXRS:SPEC:25.6;
   * 
   * @test_Strategy: If the method returns an instance of Response whose
   * metadata includes the response media type (Mspecified) then set Mselected =
   * Mspecified, finish.
   * 
   * There is no way to get method body through Accept/Produces
   */
  @Test
  public void responseNotAllowedToOverrideTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "resource/responseoverride"));
    setProperty(Property.REQUEST_HEADERS,
        buildAccept(MediaType.APPLICATION_JSON_TYPE));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.NOT_ACCEPTABLE));
    invoke();
  }

  /*
   * @testName: responseOverrideNoProducesTest
   * 
   * @assertion_ids: JAXRS:SPEC:26; JAXRS:SPEC:26.1;
   * 
   * @test_Strategy: If the method returns an instance of Response whose
   * metadata includes the response media type (Mspecified) then set Mselected =
   * Mspecified, finish.
   */
  @Test
  public void responseOverrideNoProducesTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "nomedia/responseoverride"));
    setProperty(Property.REQUEST_HEADERS,
        buildAccept(MediaType.APPLICATION_JSON_TYPE));
    setProperty(Property.EXPECTED_HEADERS,
        buildContentType(MediaType.APPLICATION_XML_TYPE));
    invoke();
  }

  /*
   * @testName: methodProducesTest
   * 
   * @assertion_ids: JAXRS:SPEC:26; JAXRS:SPEC:26.2;
   * 
   * @test_Strategy: If the method is annotated with @Produces, set P = V
   * (method) where V (t) represents the values of @Produces on the specified
   * target t.
   */
  @Test
  public void methodProducesTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "resource/method"));
    setProperty(Property.EXPECTED_HEADERS,
        buildContentType(MediaType.APPLICATION_ATOM_XML_TYPE));
    invoke();
  }

  /*
   * @testName: classProducesTest
   * 
   * @assertion_ids: JAXRS:SPEC:26; JAXRS:SPEC:26.3;
   * 
   * @test_Strategy: Else if the class is annotated with @Produces, set P = V
   * (class)..
   */
  @Test
  public void classProducesTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.HEAD, "resource/class"));
    setProperty(Property.EXPECTED_HEADERS,
        buildContentType(MediaType.TEXT_HTML_TYPE));
    invoke();
  }

  /*
   * @testName: mesageBodyWriterProducesTest
   * 
   * @assertion_ids: JAXRS:SPEC:26; JAXRS:SPEC:26.4;
   * 
   * @test_Strategy: Else set P = V (writers) where writers is the set of
   * MessageBodyWriter that support the class of the returned entity object.
   */
  @Test
  public void mesageBodyWriterProducesTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "nomedia/list"));
    setProperty(Property.EXPECTED_HEADERS,
        buildContentType(MediaType.APPLICATION_SVG_XML_TYPE));
    setProperty(Property.SEARCH_STRING, List.class.getSimpleName());
    invoke();
  }

  /*
   * @assertion_ids: JAXRS:SPEC:26; JAXRS:SPEC:26.5; JAXRS:SPEC:26.9;
   * 
   * @test_Strategy: If P = {}, set P = {*\*}. untestable
   */
  public void noProducesTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "nomedia/nothing"));
    setProperty(Property.SEARCH_STRING, "nothing");
    setProperty(Property.EXPECTED_HEADERS,
        buildContentType(MediaType.APPLICATION_OCTET_STREAM_TYPE));
    invoke();
  }

  /*
   * @assertion_ids: JAXRS:SPEC:26; JAXRS:SPEC:26.5; JAXRS:SPEC:26.9;
   * 
   * @test_Strategy: If P = {}, set P = {*\*}. untestable
   */
  public void noProducesResponseReturnTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "nomedia/response"));
    setProperty(Property.SEARCH_STRING, "nothing");
    setProperty(Property.EXPECTED_HEADERS,
        buildContentType(MediaType.APPLICATION_OCTET_STREAM_TYPE));
    invoke();
  }

  // ----------------------------- 26.8 -------------------------------

  /*
   * @testName: noPreferenceTest
   * 
   * @assertion_ids: JAXRS:SPEC:26; JAXRS:SPEC:26.8;
   * 
   * @test_Strategy: Sort M in descending order, with a primary key of
   * specificity (n/m > n\* > *\*), a secondary key of q-value and a tertiary
   * key of qs-value.
   */
  @Test
  public void noPreferenceTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "weight"));
    setProperty(Property.SEARCH_STRING, MediaType.TEXT_PLAIN);
    setProperty(Property.EXPECTED_HEADERS,
        buildContentType(MediaType.TEXT_PLAIN_TYPE));
    invoke();
  }

  /*
   * @testName: textPreferenceTest
   * 
   * @assertion_ids: JAXRS:SPEC:26; JAXRS:SPEC:26.8;
   * 
   * @test_Strategy: Sort M in descending order, with a primary key of
   * specificity (n/m > n\* > *\*), a secondary key of q-value and a tertiary
   * key of qs-value.
   */
  @Test
  public void textPreferenceTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "weight"));
    setProperty(Property.REQUEST_HEADERS, "Accept: text/*");
    setProperty(Property.SEARCH_STRING, MediaType.TEXT_PLAIN);
    setProperty(Property.EXPECTED_HEADERS,
        buildContentType(MediaType.TEXT_PLAIN_TYPE));
    invoke();
  }

  /*
   * @testName: appPreferenceTest
   * 
   * @assertion_ids: JAXRS:SPEC:26; JAXRS:SPEC:26.8;
   * 
   * @test_Strategy: Sort M in descending order, with a primary key of
   * specificity (n/m > n\* > *\*), a secondary key of q-value and a tertiary
   * key of qs-value.
   */
  @Test
  public void appPreferenceTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "weight"));
    setProperty(Property.REQUEST_HEADERS,
        "Accept: application/*;q=0.9, application/xml;q=0.1");
    setProperty(Property.SEARCH_STRING, MediaType.APPLICATION_XML);
    setProperty(Property.EXPECTED_HEADERS,
        buildContentType(MediaType.APPLICATION_XML_TYPE));
    invoke();
  }

  /*
   * @testName: imagePreferenceTest
   * 
   * @assertion_ids: JAXRS:SPEC:26; JAXRS:SPEC:26.8;
   * 
   * @test_Strategy: Sort M in descending order, with a primary key of
   * specificity (n/m > n\* > *\*), a secondary key of q-value and a tertiary
   * key of qs-value.
   */
  @Test
  public void imagePreferenceTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "weight"));
    setProperty(Property.REQUEST_HEADERS, "Accept: image/*");
    setProperty(Property.SEARCH_STRING, "image/png");
    setProperty(Property.EXPECTED_HEADERS, "Content-Type: image/png");
    invoke();
  }

  /*
   * @testName: clientImagePreferenceTest
   * 
   * @assertion_ids: JAXRS:SPEC:26; JAXRS:SPEC:26.8;
   * 
   * @test_Strategy: Sort M in descending order, with a primary key of
   * specificity (n/m > n\* > *\*), a secondary key of q-value and a tertiary
   * key of qs-value.
   */
  @Test
  public void clientImagePreferenceTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "weight"));
    setProperty(Property.REQUEST_HEADERS,
        "Accept: image/something;q=0.1, image/*;q=0.9");
    setProperty(Property.SEARCH_STRING, "image/png");
    setProperty(Property.EXPECTED_HEADERS, "Content-Type: image/png");
    invoke();
  }

  /*
   * @testName: clientXmlHtmlPreferenceTest
   * 
   * @assertion_ids: JAXRS:SPEC:26; JAXRS:SPEC:26.8;
   * 
   * @test_Strategy: Sort M in descending order, with a primary key of
   * specificity (n/m > n\* > *\*), a secondary key of q-value and a tertiary
   * key of qs-value.
   */
  @Test
  public void clientXmlHtmlPreferenceTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "weight"));
    setProperty(Property.REQUEST_HEADERS,
        "Accept: text/html;q=0.2,text/xml;q=0.9");
    setProperty(Property.SEARCH_STRING, MediaType.TEXT_XML);
    setProperty(Property.EXPECTED_HEADERS,
        buildContentType(MediaType.TEXT_XML_TYPE));
    invoke();
  }

  /*
   * @testName: clientXmlHtmlPreferenceNoWeightOnServerTest
   * 
   * @assertion_ids: JAXRS:SPEC:26; JAXRS:SPEC:26.8;
   * 
   * @test_Strategy: Sort M in descending order, with a primary key of
   * specificity (n/m > n\* > *\*), a secondary key of q-value and a tertiary
   * key of qs-value.
   */
  @Test
  public void clientXmlHtmlPreferenceNoWeightOnServerTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "resource"));
    setProperty(Property.REQUEST_HEADERS,
        "Accept: text/html;q=0.2,text/xml;q=0.9");
    setProperty(Property.SEARCH_STRING, MediaType.TEXT_XML);
    setProperty(Property.EXPECTED_HEADERS,
        buildContentType(MediaType.TEXT_XML_TYPE));
    invoke();
  }

  /*
   * @testName: clientHtmlXmlPreferenceTest
   * 
   * @assertion_ids: JAXRS:SPEC:26; JAXRS:SPEC:26.8;
   * 
   * @test_Strategy: Sort M in descending order, with a primary key of
   * specificity (n/m > n\* > *\*), a secondary key of q-value and a tertiary
   * key of qs-value.
   */
  @Test
  public void clientHtmlXmlPreferenceTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "weight"));
    setProperty(Property.REQUEST_HEADERS,
        "Accept: text/xml;q=0.3, text/html;q=0.9");
    setProperty(Property.SEARCH_STRING, MediaType.TEXT_HTML);
    setProperty(Property.EXPECTED_HEADERS,
        buildContentType(MediaType.TEXT_HTML_TYPE));
    invoke();
  }

  /*
   * @testName: clientAnyPreferenceTest
   * 
   * @assertion_ids: JAXRS:SPEC:26; JAXRS:SPEC:26.8;
   * 
   * @test_Strategy: Sort M in descending order, with a primary key of
   * specificity (n/m > n\* > *\*), a secondary key of q-value and a tertiary
   * key of qs-value.
   */
  @Test
  public void clientAnyPreferenceTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "weight"));
    setProperty(Property.REQUEST_HEADERS, "Accept: */*;q=0.8, text/xml;q=0.3");
    setProperty(Property.SEARCH_STRING, MediaType.TEXT_PLAIN);
    setProperty(Property.EXPECTED_HEADERS,
        buildContentType(MediaType.TEXT_PLAIN_TYPE));
    invoke();
  }

  // ----------------------------- 26.10 -------------------------------
  // JIRA: JERSEY-1054
  /*
   * @testName: defaultErrorTest
   * 
   * @assertion_ids: JAXRS:SPEC:26; JAXRS:SPEC:26.10;
   * 
   * @test_Strategy: Generate a WebApplicationException with a not acceptable
   * response (HTTP 406 status) and no entity. The exception MUST be processed
   * as described in section 3.3.4. Finish.
   */
  @Test
  public void defaultErrorTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "error"));
    setProperty(Property.REQUEST_HEADERS, "Accept: text/*");
    setProperty(Property.STATUS_CODE, getStatusCode(Status.NOT_ACCEPTABLE));
    invoke();
  }

  /*
   * @testName: defaultResponseErrorTest
   * 
   * @assertion_ids: JAXRS:SPEC:26; JAXRS:SPEC:26.10;
   * 
   * @test_Strategy: Generate a WebApplicationException with a not acceptable
   * response (HTTP 406 status) and no entity. The exception MUST be processed
   * as described in section 3.3.4. Finish.
   */
  @Test
  public void defaultResponseErrorTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "error"));
    setProperty(Property.CONTENT, "anything");
    setProperty(Property.REQUEST_HEADERS, "Accept: text/*");
    setProperty(Property.STATUS_CODE, getStatusCode(Status.NOT_ACCEPTABLE));
    invoke();
  }
}
