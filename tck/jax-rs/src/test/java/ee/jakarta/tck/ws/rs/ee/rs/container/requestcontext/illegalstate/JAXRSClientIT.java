/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.rs.container.requestcontext.illegalstate;

import java.io.InputStream;
import java.io.IOException;

import ee.jakarta.tck.ws.rs.common.impl.SecurityContextImpl;
import ee.jakarta.tck.ws.rs.common.provider.PrintingErrorHandler;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;
import ee.jakarta.tck.ws.rs.common.client.JaxrsCommonClient;

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

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
@ExtendWith(ArquillianExtension.class)
public class JAXRSClientIT extends JaxrsCommonClient {

  private static final long serialVersionUID = -8112756483664393579L;

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_ee_rs_container_requestcontext_illegalstate_web/resource");
    setPrintEntity(true);
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

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/container/requestcontext/illegalstate/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_rs_container_requestcontext_illegalstate_web.war");
    archive.addClasses(TSAppConfig.class, Resource.class, RequestFilter.class, ContextOperation.class, 
      RequestTemplateFilter.class, ResponseFilter.class, ResponseTemplateFilter.class, TemplateFilter.class, 
      PrintingErrorHandler.class,
      SecurityContextImpl.class);
    archive.setWebXML(new StringAsset(webXml));
    return archive;

  }

  /*
   * @testName: setMethodTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:669; JAXRS:SPEC:85;
   * 
   * @test_Strategy: Throws IllegalStateException - in case the method is not
   * invoked from a pre-matching request filter.
   */
  @Test
  public void setMethodTest() throws Fault {
    setProperty(Property.SEARCH_STRING, RequestFilter.ISEXCEPTION);
    invokeRequestAndCheckResponse(ContextOperation.SETMETHOD);
  }

  /*
   * @testName: setRequestUriOneUriTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:672; JAXRS:SPEC:85;
   * 
   * @test_Strategy: Trying to invoke the method in a filter bound to a resource
   * method results in an IllegalStateException being thrown.
   * 
   * ContainerRequestContext.abortWith
   */
  @Test
  public void setRequestUriOneUriTest() throws Fault {
    setProperty(Property.SEARCH_STRING, RequestFilter.ISEXCEPTION);
    invokeRequestAndCheckResponse(ContextOperation.SETREQUESTURI1);
  }

  /*
   * @testName: setRequestUriTwoUrisTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:674; JAXRS:SPEC:85;
   * 
   * @test_Strategy: Trying to invoke the method in a filter bound to a resource
   * method results in an IllegalStateException being thrown.
   * 
   * ContainerRequestContext.abortWith
   */
  @Test
  public void setRequestUriTwoUrisTest() throws Fault {
    setProperty(Property.SEARCH_STRING, RequestFilter.ISEXCEPTION);
    invokeRequestAndCheckResponse(ContextOperation.SETREQUESTURI2);
  }

  /*
   * @testName: abortWithTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:649;
   * 
   * @test_Strategy: throws IllegalStateException in case the method is invoked
   * from a response filter.
   */
  @Test
  public void abortWithTest() throws Fault {
    setProperty(Property.SEARCH_STRING, RequestFilter.ISEXCEPTION);
    invokeRequestAndCheckResponse(ContextOperation.ABORTWITH);
  }

  /*
   * @testName: setEntityStreamTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:668;
   * 
   * @test_Strategy: throws IllegalStateException in case the method is invoked
   * from a response filter.
   */
  @Test
  public void setEntityStreamTest() throws Fault {
    setProperty(Property.SEARCH_STRING, RequestFilter.ISEXCEPTION);
    invokeRequestAndCheckResponse(ContextOperation.SETENTITYSTREAM);
  }

  /*
   * @testName: setSecurityContextTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:676;
   * 
   * @test_Strategy: throws IllegalStateException in case the method is invoked
   * from a response filter.
   */
  @Test
  public void setSecurityContextTest() throws Fault {
    setProperty(Property.SEARCH_STRING, RequestFilter.ISEXCEPTION);
    invokeRequestAndCheckResponse(ContextOperation.SETSECURITYCONTEXT);
  }

  // ////////////////////////////////////////////////////////////////////////////

  protected void invokeRequestAndCheckResponse(ContextOperation operation)
      throws Fault {
    String op = operation.name();
    String request = buildRequest(Request.GET, op.toLowerCase());
    String header = RequestFilter.OPERATION + ":" + op;
    setProperty(Property.REQUEST, request);
    setProperty(Property.REQUEST_HEADERS, header);
    invoke();
  }
}
