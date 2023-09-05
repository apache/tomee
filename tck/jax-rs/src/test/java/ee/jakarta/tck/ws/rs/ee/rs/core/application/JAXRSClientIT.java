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

package ee.jakarta.tck.ws.rs.ee.rs.core.application;

import java.io.IOException;
import java.util.Map;
import java.io.InputStream;

import ee.jakarta.tck.ws.rs.common.webclient.http.HttpResponse;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

import jakarta.ws.rs.core.Application;
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
public class JAXRSClientIT extends JAXRSCommonClient {
  
  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_ee_core_application_web/ApplicationTest");
  }

  private static final long serialVersionUID = 1L;

  protected int expectedSingletons = 1;

  protected int expectedClasses = 1;

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

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/core/application/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_core_application_web.war");
    archive.addClasses(TSAppConfig.class, ApplicationServlet.class, ApplicationHolderSingleton.class, JaxrsUtil.class);
    archive.setWebXML(new StringAsset(webXml));
    return archive;

  }


  /*
   * @testName: getSingletonsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:23
   * 
   * @test_Strategy: Check that vi does not modify the getSingletons()
   */
  @Test
  public void getSingletonsTest() throws Fault {
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
   * @assertion_ids: JAXRS:JAVADOC:22; JAXRS:SPEC:40;
   * 
   * @test_Strategy: Check the implementation injects TSAppConfig
   */
  @Test
  public void getClassesTest() throws Fault {
    setProperty(REQUEST, buildRequest(GET, "GetClasses"));
    setProperty(STATUS_CODE, getStatusCode(Status.OK));
    invoke();
    assertTrue(getReturnedNumber() == expectedClasses,
        "Application.getClasses() return incorrect value:"+
        getReturnedNumber());
  }

  /*
   * @testName: getPropertiesTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1035; JAXRS:SPEC:40;
   * 
   * @test_Strategy: The returned properties are reflected in the application
   * configuration passed to the server-side features or injected into
   * server-side JAX-RS components.
   */
  @Test
  public void getPropertiesTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(GET, "properties"));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.OK));
    setProperty(Property.SEARCH_STRING, TSAppConfig.KEYS[0]);
    invoke();
  }

  /*
   * @testName: defaultGetPropertiesIsEmptyTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1035;
   * 
   * @test_Strategy: The default implementation returns an empty set.
   */
  @Test
  public void defaultGetPropertiesIsEmptyTest() throws Fault {
    Application application = new Application();
    Map<String, Object> properties = application.getProperties();
    assertNotNull(properties,
        "Default implementation is not empty map, but null");
    assertTrue(properties.isEmpty(), "Default implementation is not empty, but"+
        JaxrsUtil.mapToString(properties));
    logMsg("Default implementation gets empty map as expected");
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
