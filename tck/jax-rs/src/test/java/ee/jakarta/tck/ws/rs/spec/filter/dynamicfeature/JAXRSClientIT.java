/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.spec.filter.dynamicfeature;

import ee.jakarta.tck.ws.rs.common.client.JaxrsCommonClient;
import java.io.InputStream;
import java.io.IOException;

import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;
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
/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 */
/**
 * Test the interceptor is called when any entity provider is called
 */
@ExtendWith(ArquillianExtension.class)
public class JAXRSClientIT extends JaxrsCommonClient {

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_spec_filter_dynamicfeature_web/resource");
  }

 
  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException{
    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/spec/filter/dynamicfeature/web.xml.template");
    String webXml = editWebXmlString(inStream);
    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_spec_filter_dynamicfeature_web.war");
    archive.addClasses(TSAppConfig.class, Resource.class, AddTenFilter.class, AddOneInterceptor.class, AddDynamicFeature.class, AbstractAddInterceptor.class, AbstractAddFilter.class, JaxrsUtil.class);
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
   * @testName: noBindingTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:987;
   * 
   * @test_Strategy: When a filter or interceptor method throws an exception,
   * the JAX-RS runtime will attempt to map the exception
   * 
   * If a web resource had been matched before the exception was thrown, then
   * all the filters in the ContainerResponse chain for that resource MUST be
   * invoked;
   * 
   */
  @Test
  public void noBindingTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "nobinding"));
    setProperty(Property.CONTENT, "0");
    setProperty(Property.SEARCH_STRING, "0");
    invoke();
    logMsg(
        "Dynamic Bynding did not bind any filter or interceptor as expected");
  }

  /*
   * @testName: dynamicBindingTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:987;
   * 
   * @test_Strategy: When a filter or interceptor method throws an exception,
   * the JAX-RS runtime will attempt to map the exception
   * 
   * If a web resource had been matched before the exception was thrown, then
   * all the filters in the ContainerResponse chain for that resource MUST be
   * invoked;
   * 
   */
  @Test
  public void dynamicBindingTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "dynamic"));
    setProperty(Property.CONTENT, "0");
    setProperty(Property.SEARCH_STRING, "12");
    invoke();
    logMsg("Dynamic feature bound filter and interceptor as expected");
  }
}
