/*
 * Copyright (c) 2012, 2020, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.resource.webappexception.mapper;

import java.io.IOException;
import java.io.InputStream;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.ws.rs.core.Response.Status;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
@ExtendWith(ArquillianExtension.class)
public class JAXRSClientIT extends JAXRSCommonClient {
  private static final long serialVersionUID = 1L;

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_resource_webappexception_mapper_web/resource");
  }

  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException {

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/resource/webappexception/mapper/web.xml.template");
    // Replace the servlet_adaptor in web.xml.template with the System variable set as servlet adaptor
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_resource_webappexception_mapper_web.war");
    archive.addClasses(
        TSAppConfig.class, 
        Resource.class, 
        RuntimeExceptionMapper.class, 
        WebAppExceptionMapper.class, 
        DirectResponseUsageResource.class, 
        ResponseWithNoEntityUsesMapperResource.class
    );
    archive.setWebXML(new StringAsset(webXml));
    //archive.addAsWebInfResource(JAXRSClientIT.class.getPackage(), "web.xml.template", "web.xml"); //can use if the web.xml.template doesn't need to be modified.    
    
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

  /*
   * @testName: noResponseTest
   * 
   * @assertion_ids: JAXRS:SPEC:16; JAXRS:SPEC:16.1; JAXRS:SPEC:16.2;
   * 
   * @test_Strategy: An implementation MUST catch all exceptions and process
   * them as follows: Instances of WebApplicationException MUST be mapped to a
   * response as follows. If the response property of the exception does not
   * contain an entity and an exception mapping provider (see section 4.4) is
   * available for WebApplicationException an implementation MUST use the
   * provider to create a new Response instance, otherwise the response property
   * is used directly.
   */
  @Test
  public void noResponseTest() throws Fault {
    setProperty(REQUEST, buildRequest(GET, "noresponse"));
    setProperty(STATUS_CODE, getStatusCode(Status.ACCEPTED));
    invoke();
  }

  /*
   * @testName: okResponseTest
   * 
   * @assertion_ids: JAXRS:SPEC:16; JAXRS:SPEC:16.1; JAXRS:SPEC:16.2;
   * 
   * @test_Strategy: An implementation MUST catch all exceptions and process
   * them as follows: Instances of WebApplicationException MUST be mapped to a
   * response as follows. If the response property of the exception does not
   * contain an entity and an exception mapping provider (see section 4.4) is
   * available for WebApplicationException an implementation MUST use the
   * provider to create a new Response instance, otherwise the response property
   * is used directly.
   */
  @Test
  public void okResponseTest() throws Fault {
    setProperty(REQUEST, buildRequest(GET, "responseok"));
    setProperty(STATUS_CODE, getStatusCode(Status.ACCEPTED));
    invoke();
  }

  /*
   * @testName: responseEntityTest
   * 
   * @assertion_ids: JAXRS:SPEC:16; JAXRS:SPEC:16.1; JAXRS:SPEC:16.2;
   * 
   * @test_Strategy: An implementation MUST catch all exceptions and process
   * them as follows: Instances of WebApplicationException MUST be mapped to a
   * response as follows. If the response property of the exception does not
   * contain an entity and an exception mapping provider (see section 4.4) is
   * available for WebApplicationException an implementation MUST use the
   * provider to create a new Response instance, otherwise the response property
   * is used directly.
   * 
   * The ExceptionMapper is omitted
   */
  @Test
  public void responseEntityTest() throws Fault {
    setProperty(REQUEST, buildRequest(GET, "responseentity"));
    setProperty(Property.SEARCH_STRING, Resource.class.getSimpleName());
    invoke();
  }

  /*
   * @testName: statusOkResponseTest
   * 
   * @assertion_ids: JAXRS:SPEC:16; JAXRS:SPEC:16.1; JAXRS:SPEC:16.2;
   * 
   * @test_Strategy: An implementation MUST catch all exceptions and process
   * them as follows: Instances of WebApplicationException MUST be mapped to a
   * response as follows. If the response property of the exception does not
   * contain an entity and an exception mapping provider (see section 4.4) is
   * available for WebApplicationException an implementation MUST use the
   * provider to create a new Response instance, otherwise the response property
   * is used directly.
   */
  @Test
  public void statusOkResponseTest() throws Fault {
    setProperty(REQUEST, buildRequest(GET, "responsestatusok"));
    setProperty(STATUS_CODE, getStatusCode(Status.ACCEPTED));
    invoke();
  }

  /*
   * @testName: statusIntOkResponseTest
   * 
   * @assertion_ids: JAXRS:SPEC:16; JAXRS:SPEC:16.1; JAXRS:SPEC:16.2;
   * 
   * @test_Strategy: An implementation MUST catch all exceptions and process
   * them as follows: Instances of WebApplicationException MUST be mapped to a
   * response as follows. If the response property of the exception does not
   * contain an entity and an exception mapping provider (see section 4.4) is
   * available for WebApplicationException an implementation MUST use the
   * provider to create a new Response instance, otherwise the response property
   * is used directly.
   */
  @Test
  public void statusIntOkResponseTest() throws Fault {
    setProperty(REQUEST, buildRequest(GET, "responsestatusintok"));
    setProperty(STATUS_CODE, getStatusCode(Status.ACCEPTED));
    invoke();
  }

  /*
   * @testName: throwableResponseTest
   * 
   * @assertion_ids: JAXRS:SPEC:16; JAXRS:SPEC:16.1; JAXRS:SPEC:16.2;
   * 
   * @test_Strategy: An implementation MUST catch all exceptions and process
   * them as follows: Instances of WebApplicationException MUST be mapped to a
   * response as follows. If the response property of the exception does not
   * contain an entity and an exception mapping provider (see section 4.4) is
   * available for WebApplicationException an implementation MUST use the
   * provider to create a new Response instance, otherwise the response property
   * is used directly.
   */
  @Test
  public void throwableResponseTest() throws Fault {
    setProperty(REQUEST, buildRequest(GET, "responsethrowable"));
    setProperty(STATUS_CODE, getStatusCode(Status.ACCEPTED));
    invoke();
  }

  /*
   * @testName: throwableOkResponseTest
   * 
   * @assertion_ids: JAXRS:SPEC:16; JAXRS:SPEC:16.1; JAXRS:SPEC:16.2;
   * 
   * @test_Strategy: An implementation MUST catch all exceptions and process
   * them as follows: Instances of WebApplicationException MUST be mapped to a
   * response as follows. If the response property of the exception does not
   * contain an entity and an exception mapping provider (see section 4.4) is
   * available for WebApplicationException an implementation MUST use the
   * provider to create a new Response instance, otherwise the response property
   * is used directly.
   */
  @Test
  public void throwableOkResponseTest() throws Fault {
    setProperty(REQUEST, buildRequest(GET, "responsestatusthrowableok"));
    setProperty(STATUS_CODE, getStatusCode(Status.ACCEPTED));
    invoke();
  }

  /*
   * @testName: throwableIntOkResponseTest
   * 
   * @assertion_ids: JAXRS:SPEC:16; JAXRS:SPEC:16.1; JAXRS:SPEC:16.2;
   * 
   * @test_Strategy: An implementation MUST catch all exceptions and process
   * them as follows: Instances of WebApplicationException MUST be mapped to a
   * response as follows. If the response property of the exception does not
   * contain an entity and an exception mapping provider (see section 4.4) is
   * available for WebApplicationException an implementation MUST use the
   * provider to create a new Response instance, otherwise the response property
   * is used directly.
   */
  @Test
  public void throwableIntOkResponseTest() throws Fault {
    setProperty(REQUEST, buildRequest(GET, "responsestatusthrowableintok"));
    setProperty(STATUS_CODE, getStatusCode(Status.ACCEPTED));
    invoke();
  }

  /*
   * @testName: throwUncheckedExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:16; JAXRS:SPEC:16.3;
   * 
   * @test_Strategy: Unchecked exceptions and errors MUST be re-thrown and
   * allowed to propagate to the underlying container..
   */
  @Test
  public void throwUncheckedExceptionTest() throws Fault {
    setProperty(REQUEST, buildRequest(GET, "uncheckedexception"));
    setProperty(STATUS_CODE, getStatusCode(Status.NOT_ACCEPTABLE));
    invoke();
  }

  /*
   * @testName: webApplicationExceptionHasResponseWithEntityDoesNotUseMapperTest
   * 
   * @assertion_ids: JAXRS:SPEC:16; JAXRS:SPEC:16.1;
   * 
   * @test_Strategy: An implementation MUST catch all exceptions and process
   * them as follows: Instances of WebApplicationException MUST be mapped to a
   * response as follows. If the response property of the exception does not
   * contain an entity and an exception mapping provider (see section 4.4) is
   * available for WebApplicationException an implementation MUST use the
   * provider to create a new Response instance, otherwise the response property
   * is used directly.
   */
  @Test
  public void webApplicationExceptionHasResponseWithEntityDoesNotUseMapperTest()
      throws Fault {
    int[] codes = { 2000, 4000, 400, 401, 403, 404, 405, 406, 415, 3000, 5000,
        500, 503 };
    for (int i = 0; i != codes.length; i++) {
      setProperty(Property.REQUEST,
          buildRequest(Request.GET, "direct/" + codes[i]));
      setProperty(Property.STATUS_CODE,
          String.valueOf(codes[i] > 1000 ? codes[i] / 10 : codes[i]));
      setProperty(Property.SEARCH_STRING,
          codes[i] > 1000 ? DirectResponseUsageResource.ENTITY
              : DirectResponseUsageResource.getReasonPhrase(codes[i]));
      invoke();
    }
  }

  /*
   * @testName: webApplicationExceptionHasResponseWithoutEntityDoesUseMapperTest
   * 
   * @assertion_ids: JAXRS:SPEC:16; JAXRS:SPEC:16.1;
   * 
   * @test_Strategy: An implementation MUST catch all exceptions and process
   * them as follows: Instances of WebApplicationException MUST be mapped to a
   * response as follows. If the response property of the exception does not
   * contain an entity and an exception mapping provider (see section 4.4) is
   * available for WebApplicationException an implementation MUST use the
   * provider to create a new Response instance, otherwise the response property
   * is used directly.
   */
  @Test
  public void webApplicationExceptionHasResponseWithoutEntityDoesUseMapperTest()
      throws Fault {
    int[] codes = { 4000, 400, 401, 403, 404, 405, 406, 415, 3000, 5000, 500,
        503 };
    for (int i = 0; i != codes.length; i++) {
      setProperty(Property.REQUEST,
          buildRequest(Request.GET, "noentity/" + codes[i]));
      setProperty(Property.STATUS_CODE, getStatusCode(Status.FOUND));
      invoke();
    }
  }
}