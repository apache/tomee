/*
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.rs.put;

import ee.jakarta.tck.ws.rs.common.client.JaxrsCommonClient;

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

@ExtendWith(ArquillianExtension.class)
public class JAXRSClientIT extends JaxrsCommonClient {

  private static final long serialVersionUID = -71817508809693264L;

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_ee_rs_put_web");
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

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/put/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_rs_put_web.war");
    archive.addClasses(TSAppConfig.class, HttpMethodPutTest.class);
    archive.setWebXML(new StringAsset(webXml));
    return archive;

  }



  /*
   * @class.setup_props: webServerHost; webServerPort; ts_home;
   */
  /* Run test */
  /*
   * @testName: putTest1
   * 
   * @assertion_ids: JAXRS:SPEC:20.1; JAXRS:JAVADOC:6; JAXRS:JAVADOC:8;
   * JAXRS:JAVADOC:10;
   * 
   * @test_Strategy: Client invokes PUT on root resource at /PutTest; Verify
   * that right Method is invoked.
   */
  @Test
  public void putTest1() throws Fault {
    setProperty(Property.REQUEST_HEADERS, "Accept:text/plain");
    setProperty(Property.CONTENT, "dummy");
    setProperty(Property.REQUEST, buildRequest(Request.PUT, "PutTest"));
    setProperty(Property.SEARCH_STRING, "CTS-put text/plain");
    invoke();
  }

  /*
   * @testName: putTest2
   * 
   * @assertion_ids: JAXRS:SPEC:20.1; JAXRS:JAVADOC:6; JAXRS:JAVADOC:8;
   * JAXRS:JAVADOC:10;
   * 
   * @test_Strategy: Client invokes PUT on root resource at /PutTest; Verify
   * that right Method is invoked.
   */
  @Test
  public void putTest2() throws Fault {
    setProperty(Property.CONTENT, "dummy");
    setProperty(Property.REQUEST_HEADERS, "Accept:text/html");
    setProperty(Property.REQUEST, buildRequest(Request.PUT, "PutTest"));
    setProperty(Property.SEARCH_STRING, "CTS-put text/html");
    invoke();
  }

  /*
   * @testName: putSubTest
   * 
   * @assertion_ids: JAXRS:SPEC:20.1; JAXRS:JAVADOC:6; JAXRS:JAVADOC:8;
   * JAXRS:JAVADOC:10;
   * 
   * @test_Strategy: Client invokes PUT on a sub resource at /PutTest/sub;
   * Verify that right Method is invoked.
   */
  @Test
  public void putSubTest() throws Fault {
    setProperty(Property.CONTENT, "dummy");
    setProperty(Property.REQUEST, buildRequest(Request.PUT, "PutTest/sub"));
    setProperty(Property.SEARCH_STRING, "CTS-put text/html");
    invoke();
  }
}
