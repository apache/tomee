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

package ee.jakarta.tck.ws.rs.ee.rs.ext.providers;

import ee.jakarta.tck.ws.rs.common.AbstractMessageBodyRW;
import ee.jakarta.tck.ws.rs.ee.rs.core.application.ApplicationHolderSingleton;
import ee.jakarta.tck.ws.rs.ee.rs.core.application.ApplicationServlet;
import ee.jakarta.tck.ws.rs.ee.rs.ext.contextresolver.EnumContextResolver;
import ee.jakarta.tck.ws.rs.ee.rs.ext.contextresolver.EnumProvider;
import ee.jakarta.tck.ws.rs.ee.rs.ext.contextresolver.TextPlainEnumContextResolver;
import ee.jakarta.tck.ws.rs.ee.rs.ext.exceptionmapper.AnyExceptionExceptionMapper;
import ee.jakarta.tck.ws.rs.ee.rs.ext.exceptionmapper.IOExceptionExceptionMapper;
import ee.jakarta.tck.ws.rs.ee.rs.ext.messagebodyreaderwriter.EntityAnnotation;
import ee.jakarta.tck.ws.rs.ee.rs.ext.messagebodyreaderwriter.EntityMessageReader;
import ee.jakarta.tck.ws.rs.ee.rs.ext.messagebodyreaderwriter.EntityMessageWriter;
import ee.jakarta.tck.ws.rs.ee.rs.ext.messagebodyreaderwriter.ReadableWritableEntity;

import java.io.InputStream;
import java.io.IOException;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;

import ee.jakarta.tck.ws.rs.common.webclient.http.HttpResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

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
public class JAXRSProvidersClientIT
    //extends ee.jakarta.tck.ws.rs.ee.rs.core.application.JAXRSClientIT {
    extends JAXRSCommonClient {

  private static final long serialVersionUID = -935293219512493643L;

  protected int expectedSingletons = 1;

  protected int expectedClasses = 1;

  public JAXRSProvidersClientIT() {
    setup();
    TSAppConfig cfg = new TSAppConfig();
    setContextRoot("/jaxrs_ee_ext_providers_web/ProvidersServlet");
    expectedClasses = cfg.getClasses().size();
    expectedSingletons = cfg.getSingletons().size();
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

    InputStream inStream = JAXRSProvidersClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/ext/providers/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_ext_providers_web.war");
    archive.addClasses(TSAppConfig.class, ProvidersServlet.class, 
      AbstractMessageBodyRW.class,
      ApplicationServlet.class,
      ApplicationHolderSingleton.class,
      EnumProvider.class,
      EnumContextResolver.class,
      TextPlainEnumContextResolver.class,
      AnyExceptionExceptionMapper.class,
      IOExceptionExceptionMapper.class,
      EntityAnnotation.class,
      EntityMessageReader.class,
      EntityMessageWriter.class,
      ReadableWritableEntity.class
    );
    archive.setWebXML(new StringAsset(webXml));
    return archive;

  }

  /* Run test */

  /*
   * @testName: getSingletonsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:23
   * 
   * @test_Strategy: Check that the implementation returns set of
   * TSAppConfig.CLASSLIST
   */
  @Test
  public void getSingletonsTest() throws Fault {
    //super.getSingletonsTest();
    setProperty(REQUEST, buildRequest(GET, "GetSingletons"));
    setProperty(STATUS_CODE, getStatusCode(Status.OK));
    invoke();
    assertTrue(getReturnedNumber() == expectedSingletons,
        "Application.getSingletons() return incorrect value:"+
        getReturnedNumber());
  }

  /*
   * @testName: getClassesTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:22
   * 
   * @test_Strategy: Check the implementation injects TSAppConfig
   */
  @Test
  public void getClassesTest() throws Fault {
    //super.getClassesTest();
    setProperty(REQUEST, buildRequest(GET, "GetClasses"));
    setProperty(STATUS_CODE, getStatusCode(Status.OK));
    invoke();
    assertTrue(getReturnedNumber() == expectedClasses,
        "Application.getClasses() return incorrect value:"+
        getReturnedNumber());
  }

  /*
   * @testName: isRegisteredTextPlainContextResolverTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:269; JAXRS:JAVADOC:280; JAXRS:JAVADOC:299;
   * JAXRS:SPEC:40; JAXRS:SPEC:80; JAXRS:SPEC:81;
   * 
   * @test_Strategy: Register ContextResolver and try to get proper Provider
   * 
   * When injecting an instance of one of the types listed in section 9.2, the
   * instance supplied MUST be capable of selecting the correct context for a
   * particular request.
   * 
   * Context providers MAY return null from the getContext method if they do not
   * wish to provide their context for a particular Java type.
   * 
   * Context provider implementations MAY restrict the media types they support
   * using the @Produces annotation.
   */
  @Test
  public void isRegisteredTextPlainContextResolverTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(GET, "isRegisteredTextPlainContextResolver"));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.OK));
    invoke();
  }

  /*
   * @testName: isRegisteredAppJsonContextResolverTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:269; JAXRS:JAVADOC:280; JAXRS:JAVADOC:299;
   * JAXRS:SPEC:40; JAXRS:SPEC:80; JAXRS:SPEC:81;
   * 
   * @test_Strategy: Register ContextResolver and try to get proper Provider
   * 
   * When injecting an instance of one of the types listed in section 9.2, the
   * instance supplied MUST be capable of selecting the correct context for a
   * particular request.
   * 
   * Context providers MAY return null from the getContext method if they do not
   * wish to provide their context for a particular Java type.
   * 
   * Context provider implementations MAY restrict the media types they support
   * using the @Produces annotation. Absence implies that any media type is
   * supported.
   */
  @Test
  public void isRegisteredAppJsonContextResolverTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(GET, "isRegisteredAppJsonContextResolver"));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.OK));
    invoke();
  }

  /*
   * @testName: isRegisteredExceptionMapperRuntimeExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:270; JAXRS:JAVADOC:281; JAXRS:SPEC:40;
   * 
   * @test_Strategy: Try to get proper ExceptionMapper
   */
  @Test
  public void isRegisteredExceptionMapperRuntimeExceptionTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(GET, "isRegisteredExceptionMapperRuntimeEx"));
    setProperty(Property.STATUS_CODE,
        getStatusCode(Status.INTERNAL_SERVER_ERROR));
    invoke();
  }

  /*
   * @testName: isRegisteredExceptionMapperNullExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:281;
   * 
   * @test_Strategy: Try to get proper ExceptionMapper
   */
  @Test
  public void isRegisteredExceptionMapperNullExceptionTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(GET, "isRegisteredExceptionMapperNullEx"));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.NO_CONTENT));
    invoke();
  }

  /*
   * @testName: isRegisteredRuntimeExceptionExceptionMapperTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:281; JAXRS:JAVADOC:300; JAXRS:SPEC:40;
   * 
   * @test_Strategy: Try to get RuntimeExceptionExceptionMapper but there is
   * none
   */
  @Test
  public void isRegisteredRuntimeExceptionExceptionMapperTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(GET, "isRegisteredRuntimeExceptionMapper"));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.OK));
    invoke();
  }

  /*
   * @testName: isRegisteredIOExceptionExceptionMapperTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:281;
   * 
   * @test_Strategy: Try to get IOExceptionExceptionMapper
   */
  @Test
  public void isRegisteredIOExceptionExceptionMapperTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(GET, "isRegisteredIOExceptionMapper"));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.ACCEPTED));
    invoke();
  }

  /*
   * @testName: isRegisteredMessageBodyWriterWildcardTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:87; JAXRS:JAVADOC:276; JAXRS:JAVADOC:283;
   * JAXRS:JAVADOC:299; JAXRS:SPEC:40;
   * 
   * @test_Strategy: Check what is returned for wildcard is for text/plain
   */
  @Test
  public void isRegisteredMessageBodyWriterWildcardTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(GET, "isRegisteredWriterWildcard"));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.OK));
    invoke();
  }

  /*
   * @testName: isRegisteredMessageBodyWriterXmlTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:87; JAXRS:JAVADOC:276; JAXRS:JAVADOC:283;
   * JAXRS:JAVADOC:299; JAXRS:SPEC:40;
   * 
   * @test_Strategy: Check BodyWriter is returned for text/xml
   */
  @Test
  public void isRegisteredMessageBodyWriterXmlTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(GET, "isRegisteredMessageWriterXml"));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.OK));
    invoke();
  }

  /*
   * @testName: isRegisteredMessageBodyReaderWildcardTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:87; JAXRS:JAVADOC:276; JAXRS:JAVADOC:282;
   * JAXRS:JAVADOC:299; JAXRS:SPEC:40;
   * 
   * @test_Strategy: Check what is returned for wildcard is for text/plain
   */
  @Test
  public void isRegisteredMessageBodyReaderWildcardTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(GET, "isRegisteredMessageReaderWildCard"));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.OK));
    invoke();
  }

  /*
   * @testName: isRegisteredMessageBodReaderXmlTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:87; JAXRS:JAVADOC:276; JAXRS:JAVADOC:282;
   * JAXRS:JAVADOC:299; JAXRS:SPEC:40;
   * 
   * @test_Strategy: Check BodyReader is returned for text/xml
   */
  @Test
  public void isRegisteredMessageBodReaderXmlTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(GET, "isRegisteredMessageReaderXml"));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.OK));
    invoke();
  }

  /*
   * @testName: writeBodyEntityUsingWriterTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:87; JAXRS:JAVADOC:276; JAXRS:JAVADOC:283;
   * JAXRS:JAVADOC:132; JAXRS:JAVADOC:275; JAXRS:JAVADOC:276; JAXRS:JAVADOC:304;
   * 
   * @test_Strategy: Check BodyWriter is used for text/xml to write entity
   */
  @Test
  public void writeBodyEntityUsingWriterTest() throws Fault {
    String ename = EnumProvider.JAXRS.name();
    String search = new ReadableWritableEntity(ename).toXmlString();
    setProperty(Property.REQUEST_HEADERS, "Accept: " + MediaType.TEXT_XML);
    setProperty(Property.REQUEST,
        buildRequest(GET, "writeBodyEntityUsingWriter"));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.OK));
    setProperty(Property.SEARCH_STRING, search);
    invoke();
  }

  /*
   * @testName: writeHeaderEntityUsingWriterTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:87; JAXRS:JAVADOC:276; JAXRS:JAVADOC:132;
   * JAXRS:JAVADOC:275; JAXRS:JAVADOC:277; JAXRS:JAVADOC:304;
   * 
   * @test_Strategy: Check HeaderWriter is used for text/xml to write entity
   */
  @Test
  public void writeHeaderEntityUsingWriterTest() throws Fault {
    String ename = EnumProvider.JAXRS.name();
    String search = new ReadableWritableEntity(ename).toXmlString();
    setProperty(Property.REQUEST_HEADERS, "Accept: " + MediaType.TEXT_XML);
    setProperty(Property.REQUEST,
        buildRequest(GET, "writeHeaderEntityUsingWriter"));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.OK));
    setProperty(Property.EXPECTED_HEADERS,
        ReadableWritableEntity.NAME + ":" + search);
    invoke();
  }

  /*
   * @testName: writeIOExceptionUsingWriterTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:281; JAXRS:JAVADOC:304; JAXRS:JAVADOC:87;
   * JAXRS:JAVADOC:132; JAXRS:JAVADOC:277; JAXRS:JAVADOC:278;
   * 
   * @test_Strategy: Check EntityWriter is used and IOException is written using
   * mapper
   */
  @Test
  public void writeIOExceptionUsingWriterTest() throws Fault {
    setProperty(Property.REQUEST_HEADERS, "Accept: " + MediaType.TEXT_XML);
    setProperty(Property.REQUEST,
        buildRequest(GET, "writeIOExceptionUsingWriter"));
    // Depending whether the response has been committed
    setProperty(Property.STATUS_CODE, getStatusCode(Status.ACCEPTED));
    setProperty(Property.STATUS_CODE,
        getStatusCode(Status.INTERNAL_SERVER_ERROR));
    invoke();
  }

  /*
   * @testName: writeIOExceptionWithoutWriterTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:304; JAXRS:JAVADOC:281; JAXRS:SPEC:16.2;
   * 
   * @test_Strategy: Check IOExceptionExceptionMapper is chosen
   */
  @Test
  public void writeIOExceptionWithoutWriterTest() throws Fault {
    setProperty(Property.REQUEST_HEADERS, "Accept: " + MediaType.TEXT_XML);
    setProperty(Property.REQUEST,
        buildRequest(GET, "writeIOExceptionWithoutWriter"));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.ACCEPTED));
    invoke();
  }

  /*
   * @testName: readEntityFromHeaderTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:271; JAXRS:JAVADOC:272; JAXRS:JAVADOC:138;
   * JAXRS:JAVADOC:304; JAXRS:JAVADOC:282;
   * 
   * @test_Strategy: Put entity to header and read it using reader
   */
  @Test
  public void readEntityFromHeaderTest() throws Fault {
    ReadableWritableEntity entity;
    entity = new ReadableWritableEntity(EnumProvider.JAXRS.name());
    String header = ReadableWritableEntity.NAME + ":" + entity.toXmlString();
    setProperty(Property.REQUEST_HEADERS,
        "Content-Type: " + MediaType.TEXT_XML);
    setProperty(Property.REQUEST_HEADERS, header);
    setProperty(Property.REQUEST, buildRequest("POST", "readEntityFromHeader"));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.OK));
    invoke();
  }

  /*
   * @testName: readEntityFromBodyTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:271; JAXRS:JAVADOC:272; JAXRS:JAVADOC:138;
   * JAXRS:JAVADOC:304; JAXRS:JAVADOC:282;
   * 
   * @test_Strategy: Put entity to body and read it using reader
   */
  @Test
  public void readEntityFromBodyTest() throws Fault {
    ReadableWritableEntity entity;
    entity = new ReadableWritableEntity(EnumProvider.JAXRS.name());
    setProperty(Property.REQUEST_HEADERS,
        "Content-Type: " + MediaType.TEXT_XML);
    setProperty(Property.REQUEST, buildRequest("POST", "readEntityFromBody"));
    setProperty(Property.CONTENT, entity.toXmlString());
    setProperty(Property.STATUS_CODE, getStatusCode(Status.OK));
    invoke();
  }

  /*
   * @testName: readEntityIOExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:273; JAXRS:JAVADOC:138; JAXRS:JAVADOC:304;
   * JAXRS:JAVADOC:282; JAXRS:JAVADOC:271; JAXRS:JAVADOC:272;
   * 
   * @test_Strategy: Put entity to body and read it using reader
   */
  @Test
  public void readEntityIOExceptionTest() throws Fault {
    setProperty(Property.REQUEST_HEADERS,
        "Content-Type: " + MediaType.TEXT_XML);
    setProperty(Property.REQUEST,
        buildRequest("POST", "readEntityIOException"));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.ACCEPTED));
    invoke();
  }

  /*
   * @testName: readEntityWebException400Test
   * 
   * @assertion_ids: JAXRS:JAVADOC:274; JAXRS:JAVADOC:138; JAXRS:JAVADOC:304;
   * JAXRS:JAVADOC:282; JAXRS:JAVADOC:271; JAXRS:JAVADOC:272; JAXRS:SPEC:16.2;
   * 
   * @test_Strategy: Put entity to body and read it using reader
   */
  @Test
  public void readEntityWebException400Test() throws Fault {
    String code = ReadableWritableEntity.NAME + ":" + Status.BAD_REQUEST.name();
    setProperty(Property.REQUEST_HEADERS,
        "Content-Type: " + MediaType.TEXT_XML);
    setProperty(Property.REQUEST,
        buildRequest("POST", "readEntityWebException"));
    setProperty(Property.REQUEST_HEADERS, code);
    setProperty(Property.STATUS_CODE, getStatusCode(Status.BAD_REQUEST));
    invoke();
  }

  /*
   * @testName: readEntityWebException410Test
   * 
   * @assertion_ids: JAXRS:JAVADOC:274; JAXRS:JAVADOC:138; JAXRS:JAVADOC:304;
   * JAXRS:JAVADOC:282; JAXRS:JAVADOC:271; JAXRS:JAVADOC:272; JAXRS:SPEC:16.2;
   * 
   * @test_Strategy: Put entity to body and read it using reader
   */
  @Test
  public void readEntityWebException410Test() throws Fault {
    String code = ReadableWritableEntity.NAME + ":" + Status.GONE.name();
    setProperty(Property.REQUEST_HEADERS,
        "Content-Type: " + MediaType.TEXT_XML);
    setProperty(Property.REQUEST,
        buildRequest("POST", "readEntityWebException"));
    setProperty(Property.REQUEST_HEADERS, code);
    setProperty(Property.STATUS_CODE, getStatusCode(Status.GONE));
    invoke();
  }
  
  // ///////////////////////////////////////////////////////////////////////

  protected int getReturnedNumber() throws Fault {
    HttpResponse response = _testCase.getResponse();
    String body;
    try {
      body = response.getResponseBodyAsString();
    } catch (IOException e) {
      throw new Fault(e);
    }
    return Integer.parseInt(body);
  }


}
