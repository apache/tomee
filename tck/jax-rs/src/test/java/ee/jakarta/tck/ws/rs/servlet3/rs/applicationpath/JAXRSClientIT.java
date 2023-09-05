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

package ee.jakarta.tck.ws.rs.servlet3.rs.applicationpath;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
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
    setContextRoot("/jaxrs_ee_applicationpath");
  }

  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException {
    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_applicationpath.war");
    archive.addClasses(TSAppConfig.class, Resource.class); 
    
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
   * @testName: applicationPathAnnotationEncodedTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:297
   * 
   * @test_Strategy: Check the ApplicationPath annotation on Application works
   * 
   * Note that percent encoded values are allowed in the value, an
   * implementation will recognize such values and will not double encode the
   * '%' character.
   */
  @Test
  public void applicationPathAnnotationEncodedTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "ApplicationPath!/Resource"));
    invoke();
  }

  /*
   * @testName: applicationPathAnnotationNotUsedTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:297
   * 
   * @test_Strategy: Check the ApplicationPath is used properly
   */
  @Test
  public void applicationPathAnnotationNotUsedTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "Resource"));
    setProperty(Property.STATUS_CODE, "-1");
    invoke();
    Status status = getResponseStatusCode();
    assertTrue(status != Status.OK && status != Status.NO_CONTENT,
        "unexpected status code received " + status);
    logMsg("Received expected status code", status);
  }

}