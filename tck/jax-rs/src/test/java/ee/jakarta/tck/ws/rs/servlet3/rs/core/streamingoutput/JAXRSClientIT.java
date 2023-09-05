/*
 * Copyright (c) 2011, 2018, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.servlet3.rs.core.streamingoutput;

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

  public static final String _root = "/jaxrs_ee_core_streamoutput/StreamOutputTest";

  public JAXRSClientIT() {
    setup();
    setContextRoot(_root);
  }

  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException {
    
    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/servlet3/rs/core/streamingoutput/web.xml.template");
    // Replace the servlet_adaptor in web.xml.template with the System variable set as servlet adaptor
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_core_streamoutput.war");
    archive.addClasses(TSAppConfig.class, StreamOutputTest.class);
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
   * @testName: writeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:173;
   * 
   * @test_Strategy: Client send a request. Verify that
   * StreamingOutput.write(OutputStream) works.
   */
  @Test
  public void writeTest() throws Fault {
    setProperty(REQUEST, buildRequest(GET, "Test1"));
    setProperty(SEARCH_STRING, "StreamingOutputTest1");
    invoke();
  }

  /*
   * @testName: writeIOExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:174; JAXRS:JAVADOC:132;
   * 
   * @test_Strategy: Client send a request. Verify that
   * StreamingOutput.write(OutputStream) throws IOException (Servlet container
   * shall return 500 - ResponseBuilder responsibility).
   */
  @Test
  public void writeIOExceptionTest() throws Fault {
    setProperty(REQUEST, buildRequest(GET, "IOExceptionTest"));
    setProperty(STATUS_CODE, getStatusCode(Status.INTERNAL_SERVER_ERROR));
    invoke();
  }

  /*
   * @testName: writeWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:175;
   * 
   * @test_Strategy: Client send a request. Verify that
   * StreamingOutput.write(OutputStream) throws WebApplicationException works.
   */
  @Test
  public void writeWebApplicationExceptionTest() throws Fault {
    setProperty(REQUEST, buildRequest(GET, "Test2"));
    setProperty(STATUS_CODE, getStatusCode(Status.NOT_FOUND));
    invoke();
  }
}