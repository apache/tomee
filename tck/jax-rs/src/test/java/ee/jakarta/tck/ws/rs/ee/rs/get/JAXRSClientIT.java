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

package ee.jakarta.tck.ws.rs.ee.rs.get;

import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;

import jakarta.ws.rs.core.MediaType;

import java.io.InputStream;
import java.io.IOException;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

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
 *                     ts_home;
 */
@ExtendWith(ArquillianExtension.class)
public class JAXRSClientIT extends JAXRSCommonClient {
  private static final long serialVersionUID = 1L;

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_ee_rs_get_web/GetTest");
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

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/get/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_rs_get_web.war");
    archive.addClasses(TSAppConfig.class, HttpMethodGetTest.class, RecursiveLocator.class, SubResource.class);
    archive.setWebXML(new StringAsset(webXml));
    return archive;

  }


  /* Run test */
  /*
   * @testName: getTest1
   * 
   * @assertion_ids: JAXRS:SPEC:20.1; JAXRS:JAVADOC:6; JAXRS:JAVADOC:8;
   * JAXRS:JAVADOC:10;
   * 
   * @test_Strategy: Client invokes GET on root resource at /GetTest; Verify
   * that right Method is invoked.
   */
  @Test
  public void getTest1() throws Fault {
    setProperty(Property.REQUEST_HEADERS,
        buildAccept(MediaType.TEXT_PLAIN_TYPE));
    setProperty(Property.REQUEST, buildRequest(GET, ""));
    setProperty(Property.SEARCH_STRING, "CTS-get text/plain");
    invoke();
  }

  /*
   * @testName: getTest2
   * 
   * @assertion_ids: JAXRS:SPEC:20.1; JAXRS:JAVADOC:6; JAXRS:JAVADOC:8;
   * JAXRS:JAVADOC:10;
   * 
   * @test_Strategy: Client invokes GET on root resource at /GetTest; Verify
   * that right Method is invoked.
   */
  @Test
  public void getTest2() throws Fault {
    setProperty(Property.REQUEST_HEADERS,
        buildAccept(MediaType.TEXT_HTML_TYPE));
    setProperty(Property.REQUEST, buildRequest(GET, ""));
    setProperty(Property.SEARCH_STRING, "CTS-get text/html");
    invoke();
  }

  /*
   * @testName: getSubTest
   * 
   * @assertion_ids: JAXRS:SPEC:20.1; JAXRS:JAVADOC:6; JAXRS:JAVADOC:8;
   * JAXRS:JAVADOC:10;
   * 
   * @test_Strategy: Client invokes GET on a sub resource at /GetTest/sub;
   * Verify that right Method is invoked.
   */
  @Test
  public void getSubTest() throws Fault {
    setProperty(Property.REQUEST_HEADERS,
        buildAccept(MediaType.TEXT_PLAIN_TYPE));
    setProperty(Property.REQUEST, buildRequest(GET, "sub"));
    setProperty(Property.SEARCH_STRING, "CTS-get text/plain");
    invoke();
  }

  /*
   * @testName: headTest1
   * 
   * @assertion_ids: JAXRS:SPEC:17.2; JAXRS:JAVADOC:6; JAXRS:JAVADOC:8;
   * JAXRS:JAVADOC:10;
   * 
   * @test_Strategy: Client invokes Head on root resource at /GetTest; which no
   * request method designated for HEAD; Verify that corresponding GET Method is
   * invoked.
   */
  @Test
  public void headTest1() throws Fault {
    setProperty(Property.REQUEST_HEADERS,
        buildAccept(MediaType.TEXT_PLAIN_TYPE));
    setProperty(Property.REQUEST, buildRequest("HEAD", ""));
    setProperty(Property.EXPECTED_HEADERS, "CTS-HEAD: text-plain");
    invoke();
  }

  /*
   * @testName: headTest2
   * 
   * @assertion_ids: JAXRS:SPEC:17.2; JAXRS:JAVADOC:6; JAXRS:JAVADOC:8;
   * JAXRS:JAVADOC:10;
   * 
   * @test_Strategy: Client invokes HEAD on root resource at /GetTest; which no
   * request method designated for HEAD; Verify that corresponding GET Method is
   * invoked.
   */
  @Test
  public void headTest2() throws Fault {
    setProperty(Property.REQUEST_HEADERS,
        buildAccept(MediaType.TEXT_HTML_TYPE));
    setProperty(Property.REQUEST, buildRequest("HEAD", ""));
    setProperty(Property.EXPECTED_HEADERS, "CTS-HEAD: text-html");
    invoke();
  }

  /*
   * @testName: headSubTest
   * 
   * @assertion_ids: JAXRS:SPEC:17.2; JAXRS:JAVADOC:6; JAXRS:JAVADOC:8;
   * JAXRS:JAVADOC:10;
   * 
   * @test_Strategy: Client invokes HEAD on sub resource at /GetTest/sub; which
   * no request method designated for HEAD; Verify that corresponding GET Method
   * is invoked instead.
   */
  @Test
  public void headSubTest() throws Fault {
    setProperty(Property.REQUEST_HEADERS,
        buildAccept(MediaType.TEXT_PLAIN_TYPE));
    setProperty(Property.REQUEST, buildRequest("HEAD", "sub"));
    setProperty(Property.EXPECTED_HEADERS, "CTS-HEAD: sub-text-plain");
    invoke();
  }

  /*
   * @testName: optionSubTest
   * 
   * @assertion_ids: JAXRS:SPEC:18.2; JAXRS:JAVADOC:6; JAXRS:JAVADOC:8;
   * JAXRS:JAVADOC:10;
   * 
   * @test_Strategy: Client invokes OPTIONS on a sub resource at /GetTest/sub;
   * which no request method designated for OPTIONS. Verify that an automatic
   * response is generated.
   */
  @Test
  public void optionSubTest() throws Fault {
    setProperty(Property.REQUEST_HEADERS,
        buildAccept(MediaType.TEXT_HTML_TYPE));
    setProperty(Property.REQUEST, buildRequest("OPTIONS", "sub"));
    setProperty(Property.EXPECTED_HEADERS, "ALLOW:GET,OPTIONS,HEAD");
    invoke();
  }

  /*
   * @testName: dynamicGetTest
   * 
   * @assertion_ids: JAXRS:SPEC:20; JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: The presence or absence of a request method designator
   * (e.g. @GET) differentiates between the two: o Absent
   */
  @Test
  public void dynamicGetTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(GET, "123"));
    setProperty(Property.SEARCH_STRING, SubResource.ID);
    invoke();
  }

  /*
   * @testName: recursiveResourceLocatorTest
   * 
   * @assertion_ids: JAXRS:SPEC:20; JAXRS:SPEC:20.2;
   * 
   * @test_Strategy: The presence or absence of a request method designator
   * (e.g. @GET) differentiates between the two: o Absent
   *
   * Any returned object is treated as a resource class instance and used to
   * either handle the request or to further resolve the object that will handle
   * the request
   * 
   */
  @Test
  public void recursiveResourceLocatorTest() throws Fault {
    StringBuilder sb = new StringBuilder();
    sb.append("recursive");
    for (int i = 0; i != 10; i++)
      sb.append("/lvl");
    setProperty(Property.REQUEST, buildRequest(GET, sb.toString()));
    setProperty(Property.SEARCH_STRING, "10");
    invoke();
  }

}
