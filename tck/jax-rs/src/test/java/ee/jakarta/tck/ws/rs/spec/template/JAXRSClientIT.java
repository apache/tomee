/*
 * Copyright (c) 2007, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.spec.template;

import java.io.InputStream;
import java.io.IOException;

import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

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

@ExtendWith(ArquillianExtension.class)
public class JAXRSClientIT extends JAXRSCommonClient {

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_spec_templateTest_web");
  }


  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException{
    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/spec/template/web.xml.template");
    String webXml = editWebXmlString(inStream);
    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_spec_templateTest_web.war");
    archive.addClasses(TSAppConfig.class, TemplateTest.class);
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

  /*
   * @class.setup_props: webServerHost; webServerPort; ts_home;
   */
  /* Run test */
  /*
   * @testName: Test1
   *
   * @assertion_ids: JAXRS:SPEC:3.3; JAXRS:SPEC:20.1; JAXRS:SPEC:60;
   *
   * @test_Strategy: Client sends a request on a resource at /TemplateTest/{id},
   * Verify that correct resource method invoked through use of URI Template
   */
  @Test
  public void Test1() throws Fault {
    setProperty(REQUEST,
        "GET " + "/jaxrs_spec_templateTest_web/TemplateTest/xyz HTTP/1.1");
    setProperty(SEARCH_STRING, "id1=xyz");
    invoke();
  }

  /*
   * @testName: Test2
   *
   * @assertion_ids: JAXRS:SPEC:3.3; JAXRS:SPEC:20.1; JAXRS:SPEC:57;
   * JAXRS:SPEC:60;
   *
   * @test_Strategy:Client sends a request on a resource at /TemplateTest/{id],
   * Verify that correct resource method invoked through use of URI Template
   */
  @Test
  public void Test2() throws Fault {
    setProperty(REQUEST,
        "GET " + "/jaxrs_spec_templateTest_web/TemplateTest/xyz/abc HTTP/1.1");
    setProperty(SEARCH_STRING, "id3=abc");
    invoke();
  }

  /*
   * @testName: Test3
   *
   * @assertion_ids: JAXRS:SPEC:3.3; JAXRS:SPEC:20.1; JAXRS:SPEC:57;
   * JAXRS:SPEC:60;
   *
   * @test_Strategy:Client sends a request on a resource at /TemplateTest/{id],
   * Verify that correct resource method invoked through use of URI Template
   */
  @Test
  public void Test3() throws Fault {
    setProperty(REQUEST, "GET "
        + "/jaxrs_spec_templateTest_web/TemplateTest/xyz/abc/def HTTP/1.1");
    setProperty(SEARCH_STRING, "id3=abc/def");
    invoke();
  }

  /*
   * @testName: Test4
   *
   * @assertion_ids: JAXRS:SPEC:3.3; JAXRS:SPEC:20.1; JAXRS:SPEC:57;
   * JAXRS:SPEC:60;
   *
   * @test_Strategy:Client sends a request on a resource at /TemplateTest/{id],
   * Verify that correct resource method invoked through use of URI Template
   */
  @Test
  public void Test4() throws Fault {
    setProperty(REQUEST, "GET "
        + "/jaxrs_spec_templateTest_web/TemplateTest/xy/abc/def HTTP/1.1");
    setProperty(SEARCH_STRING, "id1=xy/abc/def");
    invoke();
  }

  /*
   * @testName: Test5
   *
   * @assertion_ids: JAXRS:SPEC:3.3; JAXRS:SPEC:20.1; JAXRS:SPEC:57;
   * JAXRS:SPEC:60;
   *
   * @test_Strategy:Client sends a request on a resource at /TemplateTest/{id],
   * Verify that correct resource method invoked through use of URI Template
   */
  @Test
  public void Test5() throws Fault {
    setProperty(REQUEST, "GET "
        + "/jaxrs_spec_templateTest_web/TemplateTest/abc/test.html HTTP/1.1");
    setProperty(SEARCH_STRING, "id4=abc|name=test");
    invoke();
  }

  /*
   * @testName: Test6
   *
   * @assertion_ids: JAXRS:SPEC:3.3; JAXRS:SPEC:20.1; JAXRS:SPEC:57;
   * JAXRS:SPEC:60;
   *
   * @test_Strategy:Client sends a request on a resource at /TemplateTest/{id],
   * Verify that correct resource method invoked through use of URI Template
   */
  @Test
  public void Test6() throws Fault {
    setProperty(REQUEST, "GET "
        + "/jaxrs_spec_templateTest_web/TemplateTest/abc/def/test.xml HTTP/1.1");
    setProperty(SEARCH_STRING, "id5=abc/def|name=test");
    invoke();
  }
}
