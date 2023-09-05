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

package ee.jakarta.tck.ws.rs.spec.resource.annotationprecedence;

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
    setContextRoot("/jaxrs_spec_resource_annotationprecedence_web/resource");
  }

 
  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException{
    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/spec/resource/annotationprecedence/web.xml.template");
    String webXml = editWebXmlString(inStream);
    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_spec_resource_annotationprecedence_web.war");
    archive.addClasses(TSAppConfig.class, Resource.class, ResourceInterface.class, SuperClass.class);
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
   * @testName: correctTest
   * 
   * @assertion_ids: JAXRS:SPEC:23;
   * 
   * @test_Strategy: Annotations on a super-class take precedence over those on
   * an implemented interface.
   */
  @Test
  public void correctTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "post"));
    invoke();
  }

  /*
   * @testName: incorrectPathOnClassTest
   * 
   * @assertion_ids: JAXRS:SPEC:23;
   * 
   * @test_Strategy: Annotations on a super-class take precedence over those on
   * an implemented interface. (@Path)
   */
  @Test
  public void incorrectPathOnClassTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "post")
        .replace("/resource", "/interfaceresource"));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.NOT_FOUND));
    invoke();
  }

  /*
   * @testName: incorrectPathOnMethodTest
   * 
   * @assertion_ids: JAXRS:SPEC:23;
   * 
   * @test_Strategy: Annotations on a super-class take precedence over those on
   * an implemented interface. (@Path)
   */
  @Test
  public void incorrectPathOnMethodTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "get"));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.NOT_FOUND));
    invoke();
  }

  /*
   * @testName: correctRequestTest
   * 
   * @assertion_ids: JAXRS:SPEC:23;
   * 
   * @test_Strategy: Annotations on a super-class take precedence over those on
   * an implemented interface. (@GET)
   */
  @Test
  public void correctRequestTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "post"));
    setProperty(Property.STATUS_CODE, "!" + getStatusCode(Status.OK));
    invoke();
  }

  /*
   * @testName: incorrectConsumesTest
   * 
   * @assertion_ids: JAXRS:SPEC:23;
   * 
   * @test_Strategy: Annotations on a super-class take precedence over those on
   * an implemented interface. (Content-Type)
   */
  @Test
  public void incorrectConsumesTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "post"));
    setProperty(Property.REQUEST_HEADERS,
        buildContentType(MediaType.TEXT_PLAIN_TYPE));
    setProperty(Property.STATUS_CODE,
        getStatusCode(Status.UNSUPPORTED_MEDIA_TYPE));
    invoke();
  }

  /*
   * @testName: incorrectProdecesTest
   * 
   * @assertion_ids: JAXRS:SPEC:23;
   * 
   * @test_Strategy: Annotations on a super-class take precedence over those on
   * an implemented interface. (Accept)
   */
  @Test
  public void incorrectProdecesTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "post"));
    setProperty(Property.REQUEST_HEADERS,
        buildAccept(MediaType.TEXT_PLAIN_TYPE));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.NOT_ACCEPTABLE));
    invoke();
  }

  /*
   * @testName: incorrectProducesConsumesTest
   * 
   * @assertion_ids: JAXRS:SPEC:23;
   * 
   * @test_Strategy: Annotations on a super-class take precedence over those on
   * an implemented interface. (Accept, Content-type)
   */
  @Test
  public void incorrectProducesConsumesTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "post"));
    setProperty(Property.REQUEST_HEADERS, buildAccept(MediaType.TEXT_XML_TYPE));
    setProperty(Property.REQUEST_HEADERS,
        buildContentType(MediaType.TEXT_XML_TYPE));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.OK));
    invoke();
  }

  /*
   * @testName: formParamTest
   * 
   * @assertion_ids: JAXRS:SPEC:23;
   * 
   * @test_Strategy: Annotations on a super-class take precedence over those on
   * an implemented interface. (formparam=pqr)
   */
  @Test
  public void formParamTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "post"));
    setProperty(Property.CONTENT, "pqr=hello");
    setProperty(Property.SEARCH_STRING, "default");
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH, "hello");
    invoke();
  }

  /*
   * @testName: queryParamXyzTest
   * 
   * @assertion_ids: JAXRS:SPEC:23;
   * 
   * @test_Strategy: Annotations on a super-class take precedence over those on
   * an implemented interface. (queryParam=xyz)
   */
  @Test
  public void queryParamXyzTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "post?xyz=hello"));
    setProperty(Property.SEARCH_STRING, "default");
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH, "hello");
    invoke();
  }

  /*
   * @testName: queryParamPqrTest
   * 
   * @assertion_ids: JAXRS:SPEC:23;
   * 
   * @test_Strategy: Annotations on a super-class take precedence over those on
   * an implemented interface. (queryParam=pqr)
   */
  @Test
  public void queryParamPqrTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "post?pqr=hello"));
    setProperty(Property.SEARCH_STRING, "hello");
    invoke();
  }
}
