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

package ee.jakarta.tck.ws.rs.spec.filter.globalbinding;

import java.io.InputStream;
import java.io.IOException;

import ee.jakarta.tck.ws.rs.common.client.JaxrsCommonClient;
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
    setContextRoot("/jaxrs_spec_filter_globalbinding_web/resource");
  }

 
  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException{
    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/spec/filter/globalbinding/web.xml.template");
    String webXml = editWebXmlString(inStream);
    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_spec_filter_globalbinding_web.war");
    archive.addClasses(TSAppConfig.class, Resource.class, AbstractAddFilter.class, AbstractAddInterceptor.class, AddOneInterceptor.class, AddTenFilter.class, GlobalNameBinding.class, JaxrsUtil.class);
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
   * @testName: nameBoundResourceTest
   * 
   * @assertion_ids: JAXRS:SPEC:89;
   * 
   * @test_Strategy: If providers are decorated with at least one name binding
   * annotation, the application subclass must be annotated as shown above in
   * order for those filters or interceptors to be globally bound
   */
  @Test
  public void nameBoundResourceTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "bind"));
    setProperty(Property.CONTENT, "0");
    setProperty(Property.SEARCH_STRING, "12");
    invoke();
    logMsg("Bound as expected");
  }

  /*
   * @testName: globalBoundResourceTest
   * 
   * @assertion_ids: JAXRS:SPEC:89;
   * 
   * @test_Strategy: If providers are decorated with at least one name binding
   * annotation, the application subclass must be annotated as shown above in
   * order for those filters or interceptors to be globally bound
   */
  @Test
  public void globalBoundResourceTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "nobind"));
    setProperty(Property.CONTENT, "0");
    setProperty(Property.SEARCH_STRING, "12");
    invoke();
    logMsg("Bound as expected");
  }

}
