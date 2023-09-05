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

package ee.jakarta.tck.ws.rs.spec.provider.exceptionmapper;

import java.io.InputStream;
import java.io.IOException;

import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

import jakarta.ws.rs.core.Response.Status;

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
public class JAXRSClientIT extends JAXRSCommonClient {

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_spec_provider_exceptionmapper_web/resource");
  }

 
  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException{
    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/spec/provider/exceptionmapper/web.xml.template");
    String webXml = editWebXmlString(inStream);
    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_spec_provider_exceptionmapper_web.war");
    archive.addClasses(TSAppConfig.class, Resource.class, ClientErrorExceptionMapper.class, ExceptionFromMappedException.class, ExceptionFromMappedExceptionMapper.class, FilterChainTestException.class, FilterChainTestExceptionMapper.class, PlainExceptionMapper.class, ResponseFilter.class, RuntimeExceptionMapper.class, ThrowableMapper.class, WebAppExceptionMapper.class);
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
   * @testName: throwableTest
   * 
   * @assertion_ids: JAXRS:SPEC:39;
   * 
   * @test_Strategy: When choosing an exception mapping provider to map an
   * exception, an implementation MUST use the provider whose generic type is
   * the nearest superclass of the exception.
   */
  @Test
  public void throwableTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "throwable"));
    setProperty(Property.SEARCH_STRING, ThrowableMapper.class.getName());
    invoke();
  }

  /*
   * @testName: exceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:39;
   * 
   * @test_Strategy: When choosing an exception mapping provider to map an
   * exception, an implementation MUST use the provider whose generic type is
   * the nearest superclass of the exception.
   */
  @Test
  public void exceptionTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "exception"));
    setProperty(Property.SEARCH_STRING, PlainExceptionMapper.class.getName());
    invoke();
  }

  /*
   * @testName: runtimeExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:39;
   * 
   * @test_Strategy: When choosing an exception mapping provider to map an
   * exception, an implementation MUST use the provider whose generic type is
   * the nearest superclass of the exception.
   */
  @Test
  public void runtimeExceptionTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "runtime"));
    setProperty(Property.SEARCH_STRING, RuntimeExceptionMapper.class.getName());
    invoke();
  }

  /*
   * @testName: webapplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:39;
   * 
   * @test_Strategy: When choosing an exception mapping provider to map an
   * exception, an implementation MUST use the provider whose generic type is
   * the nearest superclass of the exception.
   */
  @Test
  public void webapplicationExceptionTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "webapp"));
    setProperty(Property.SEARCH_STRING, WebAppExceptionMapper.class.getName());
    invoke();
  }

  /*
   * @testName: clientErrorExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:39;
   * 
   * @test_Strategy: When choosing an exception mapping provider to map an
   * exception, an implementation MUST use the provider whose generic type is
   * the nearest superclass of the exception.
   */
  @Test
  public void clientErrorExceptionTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "clienterror"));
    setProperty(Property.SEARCH_STRING,
        ClientErrorExceptionMapper.class.getName());
    invoke();
  }

  /*
   * @testName: filterChainTest
   * 
   * @assertion_ids: JAXRS:SPEC:82;
   * 
   * @test_Strategy: When a resource class or provider method throws an
   * exception for which there is an exception mapping provider, the matching
   * provider is used to obtain a Response instance. The resulting Response is
   * processed as if a web resource method had returned the Response, see
   * Section 3.3.3. In particular, a mapped Response MUST be processed using the
   * ContainerResponse filter chain defined in Chapter 6.
   */
  @Test
  public void filterChainTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "chain"));
    setProperty(Property.SEARCH_STRING, ResponseFilter.class.getName());
    invoke();
  }

  /*
   * @testName: mappedExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:83;
   * 
   * @test_Strategy: To avoid a potentially infinite loop, a single exception
   * mapper must be used during the processing of a request and its
   * corresponding response. JAX-RS implementations MUST NOT attempt to map
   * exceptions thrown while processing a response previously mapped from an
   * exception. Instead, this exception MUST be processed as described in steps
   * 3 and 4 in Section 3.3.4.
   */
  // TODO : Use a servlet filter to verify the exception has been passed
  // to underlying container, JIRA 1613
  @Test
  public void mappedExceptionTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "mapped"));
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH,
        WebAppExceptionMapper.class.getName());
    setProperty(Property.STATUS_CODE,
        getStatusCode(Status.INTERNAL_SERVER_ERROR));
    invoke();
  }

}
