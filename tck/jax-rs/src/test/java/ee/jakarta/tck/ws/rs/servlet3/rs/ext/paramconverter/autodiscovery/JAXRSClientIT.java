/*
 * Copyright (c) 2012, 2018, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.servlet3.rs.ext.paramconverter.autodiscovery;

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

import ee.jakarta.tck.ws.rs.common.client.JaxrsCommonClient;
import ee.jakarta.tck.ws.rs.common.provider.PrintingErrorHandler;
import ee.jakarta.tck.ws.rs.common.provider.StringBean;
import ee.jakarta.tck.ws.rs.common.provider.StringBeanParamConverter;
import ee.jakarta.tck.ws.rs.common.provider.StringBeanParamConverterProvider;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
@ExtendWith(ArquillianExtension.class)
public class JAXRSClientIT extends JaxrsCommonClient {

  /**
   * 
   */
  private static final long serialVersionUID = 8764917394183731977L;

  public JAXRSClientIT() {
    setup();
    setContextRoot(
        "/jaxrs_servlet3_rs_ext_paramconverter_autodiscovery/resource");
  }

  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException {

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/servlet3/rs/ext/paramconverter/autodiscovery/web.xml.template");
    // Replace the servlet_adaptor in web.xml.template with the System variable set as servlet adaptor
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_servlet3_rs_ext_paramconverter_autodiscovery.war");
    archive.addClasses(
      TSAppConfig.class, 
      Resource.class,
      StringBeanParamConverter.class,
      StringBeanParamConverterProvider.class,
      PrintingErrorHandler.class,
      StringBean.class
    );
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
   * @testName: isParamCoverterFoundByAutodiscoveryUsedTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:919; JAXRS:SPEC:59;
   * 
   * @test_Strategy: Providers implementing ParamConverterProvider contract must
   * be annotated with @Provider annotation to be automatically discovered by
   * the JAX-RS runtime during a provider scanning phase.
   * 
   * 2.3.2 When an Application subclass is present in the archive, if both
   * Application.getClasses and Application.getSingletons return an empty list
   * then all root resource classes and providers packaged in the web
   * application MUST be included and the JAX-RS implementation is REQUIRED to
   * discover them automatically.
   */
  @Test
  public void isParamCoverterFoundByAutodiscoveryUsedTest() throws Fault {
    String query = "ABCDEFGH";
    setPropertyRequest(Request.GET, "sbquery?query=", query);
    setProperty(Property.SEARCH_STRING, StringBeanParamConverter.VALUE);
    setProperty(Property.SEARCH_STRING, query);
    invoke();
  }

  /*
   * @assertion_ids: JAXRS:JAVADOC:919; JAXRS:SPEC:59;
   * 
   * @test_Strategy: Providers implementing ParamConverterProvider contract must
   * be annotated with @Provider annotation to be automatically discovered by
   * the JAX-RS runtime during a provider scanning phase.
   * 
   * 2.3.2 When an Application subclass is present in the archive, if both
   * Application.getClasses and Application.getSingletons return an empty list
   * then all root resource classes and providers packaged in the web
   * application MUST be included and the JAX-RS implementation is REQUIRED to
   * discover them automatically.
   * 
   * check whether it pass in a case of writer only TODO: IN MR public void
   * isParamCoverterUsedForWritingTest() throws Fault { String query = "OK";
   * setPropertyRequest(Request.GET, ""); setProperty(Property.EXPECTED_HEADERS,
   * Resource.HEADER_NAME + ":" + query); invoke(); }
   */

  // ////////////////////////////////////////////////////////////////////
  private void setPropertyRequest(Request request, String... resource) {
    StringBuilder sb = new StringBuilder();
    for (String r : resource)
      sb.append(r);
    setProperty(Property.REQUEST, buildRequest(request, sb.toString()));
  }

}