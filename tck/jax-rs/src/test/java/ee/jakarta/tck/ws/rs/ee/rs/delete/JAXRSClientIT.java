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

/*
 * $Id$
 */
package ee.jakarta.tck.ws.rs.ee.rs.delete;

import java.io.InputStream;
import java.io.IOException;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;

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

@ExtendWith(ArquillianExtension.class)
public class JAXRSClientIT extends JAXRSCommonClient {

  private static final long serialVersionUID = 204493956987397506L;

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_ee_rs_delete_web");
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

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/delete/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_rs_delete_web.war");
    archive.addClasses(TSAppConfig.class, HttpMethodDeleteTest.class);
    archive.setWebXML(new StringAsset(webXml));
    return archive;

  }

  /* Run test */
  /*
   * @testName: deleteTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:6; JAXRS:JAVADOC:10;
   * 
   * @test_Strategy: Client invokes Delete on root resource at /DeleteTest;
   * Verify that right Method is invoked.
   */
  @Test
  public void deleteTest1() throws Fault {
    setProperty(REQUEST_HEADERS, "Accept:text/plain");
    setProperty(REQUEST, "DELETE " + getContextRoot() + "/DeleteTest HTTP/1.1");
    setProperty(SEARCH_STRING, "CTS-Delete text/plain");
    invoke();
  }

  /*
   * @testName: deleteTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:6; JAXRS:JAVADOC:10;
   * 
   * @test_Strategy: Client invokes Delete on root resource at /DeleteTest;
   * Verify that right Method is invoked.
   */
  @Test
  public void deleteTest2() throws Fault {
    setProperty(REQUEST_HEADERS, "Accept:text/html");
    setProperty(REQUEST,
        "DELETE " + getContextRoot() + "/DeleteTest  HTTP/1.1");
    setProperty(SEARCH_STRING, "CTS-Delete text/html");
    invoke();
  }

  /*
   * @testName: deleteSubTest
   * 
   * @assertion_ids: JAXRS:SPEC:20.1; JAXRS:JAVADOC:6; JAXRS:JAVADOC:10;
   * JAXRS:JAVADOC:8;
   * 
   * @test_Strategy: Client invokes Delete on a sub resource at /DeleteTest/sub;
   * Verify that right Method is invoked.
   */
  @Test
  public void deleteSubTest() throws Fault {
    setProperty(REQUEST_HEADERS, "Accept:text/html");
    setProperty(REQUEST,
        "DELETE " + getContextRoot() + "/DeleteTest/sub HTTP/1.1");
    setProperty(SEARCH_STRING, "CTS-Delete text/html");
    invoke();
  }

  /*
   * @testName: deleteSubTest1
   * 
   * @assertion_ids: JAXRS:SPEC:20.1; JAXRS:SPEC:21; JAXRS:SPEC:25.6;
   * JAXRS:JAVADOC:6; JAXRS:JAVADOC:10; JAXRS:JAVADOC:8;
   * 
   * @test_Strategy: Client invokes Delete on a sub resource at /DeleteTest/sub;
   * Verify that no Method is invoked.
   */
  @Test
  public void deleteSubTest1() throws Fault {
    setProperty(REQUEST_HEADERS, "Accept:text/plain");
    setProperty(REQUEST,
        "DELETE " + getContextRoot() + "/DeleteTest/sub HTTP/1.1");
    setProperty(STATUS_CODE, getStatusCode(Status.NOT_ACCEPTABLE));
    invoke();
  }
}
