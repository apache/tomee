/*
 * Copyright (c) 2012, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.spec.context.client;

import static ee.jakarta.tck.ws.rs.spec.context.server.StringBeanEntityProviderWithInjectables.notInjected;
import java.io.InputStream;
import java.io.IOException;

import ee.jakarta.tck.ws.rs.common.provider.PrintingErrorHandler;
import ee.jakarta.tck.ws.rs.common.client.JaxrsCommonClient;
import ee.jakarta.tck.ws.rs.common.provider.StringBean;
import ee.jakarta.tck.ws.rs.spec.context.server.StringBeanEntityProviderWithInjectables;
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
@ExtendWith(ArquillianExtension.class)
public class JAXRSClientIT extends JaxrsCommonClient {

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_spec_context_client_web/resource");
  }

 
  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException{
    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/spec/context/client/web.xml.template");
    String webXml = editWebXmlString(inStream);
    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_spec_context_client_web.war");
    archive.addClasses(TSAppConfig.class, Resource.class, PrintingErrorHandler.class, StringBean.class);
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
   * @testName: clientWriterTest
   * 
   * @assertion_ids: JAXRS:SPEC:93; JAXRS:SPEC:93.3; JAXRS:SPEC:94;
   * JAXRS:SPEC:95; JAXRS:SPEC:96; JAXRS:SPEC:97; JAXRS:SPEC:98; JAXRS:SPEC:99;
   * JAXRS:SPEC:100; JAXRS:JAVADOC:754;
   * 
   * @test_Strategy: @Context available to providers
   * 
   * An instance can be injected into a class field or method parameter using
   * the @Context annotation.
   * 
   * The lifecycle of components registered using this class-based register(...)
   * method is fully managed by the JAX-RS implementation or any underlying IoC
   * container supported by the implementation.
   */
  @Test
  public void clientWriterTest() throws Fault {
    addProvider(StringBeanEntityProviderWithInjectables.class);
    setRequestContentEntity(new StringBean("stringbean"));
    setProperty(Property.REQUEST, buildRequest(Request.POST, "echo"));
    invoke();
    assertInjection("@Context injection did not work properly:");
  }

  /*
   * @testName: clientReaderTest
   * 
   * @assertion_ids: JAXRS:SPEC:93; JAXRS:SPEC:93.3; JAXRS:SPEC:94;
   * JAXRS:SPEC:95; JAXRS:SPEC:96; JAXRS:SPEC:97; JAXRS:SPEC:98; JAXRS:SPEC:99;
   * JAXRS:SPEC:100; JAXRS:JAVADOC:754;
   * 
   * @test_Strategy: @Context available to providers
   * 
   * An instance can be injected into a class field or method parameter using
   * the @Context annotation.
   * 
   * The lifecycle of components registered using this class-based register(...)
   * method is fully managed by the JAX-RS implementation or any underlying IoC
   * container supported by the implementation.
   */
  @Test
  public void clientReaderTest() throws Fault {
    addProvider(StringBeanEntityProviderWithInjectables.class);
    setRequestContentEntity("stringbean");
    setProperty(Property.REQUEST, buildRequest(Request.POST, "echo"));
    invoke();
    StringBean content = getResponseBody(StringBean.class);
    assertNotNull(content, "response body is null");
    logMsg("Injectables are", content.get());
    assertInjection(content.get(), "@Context injection did not work properly:");
  }

  // ////////////////////////////////////////////////////////////////////
  private void assertInjection(String body, Object failMessage) throws Fault {
    assertEquals('1', body.charAt(6), failMessage, notInjected(body, 6),
        "has not been injected"); // Providers
    assertEquals('1', body.charAt(8), failMessage, notInjected(body, 8),
        "has not been injected"); // Configuration
    logMsg("@Context injected as expected");
  }

  private void assertInjection(Object failMessage) throws Fault {
    String body = getResponseBody();
    assertInjection(body, failMessage);
  }
}
