/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.jaxrs21.ee.priority;

import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;

import java.io.IOException;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
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

  private static final long serialVersionUID = 21L;

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_jaxrs21_ee_priority_web");
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
  public static WebArchive createDeployment() throws IOException {

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_jaxrs21_ee_priority_web.war");
    archive.addClasses(TSAppConfig.class, ExceptionMapperOne.class,
      ExceptionMapperThree.class, ExceptionMapperTwo.class, ExceptionResource.class,
      ParamConverterProviderAnother.class, ParamConverterProviderOne.class, ParamConverterProviderTwo.class,
      ParamConverterResource.class, TckPriorityException.class);
    return archive;

  }


  /* Run test */

  /*
   * @testName: exceptionMapperPriorityTest
   * 
   * @assertion_ids: JAXRS:SPEC:127;
   * 
   * @test_Strategy:
   */
  @Test
  public void exceptionMapperPriorityTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "exception"));
    setProperty(Property.SEARCH_STRING, ExceptionMapperOne.class.getName());
    invoke();
  }

  /*
   * @testName: paramConverterPriorityTest
   * 
   * @assertion_ids: JAXRS:SPEC:127;
   * 
   * @test_Strategy:
   */
  @Test
  public void paramConverterPriorityTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "converter?id=something"));
    setProperty(Property.SEARCH_STRING,
        ParamConverterProviderOne.class.getName());
    invoke();
  }

}
