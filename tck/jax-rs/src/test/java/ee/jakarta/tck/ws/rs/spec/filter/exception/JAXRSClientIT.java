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

package ee.jakarta.tck.ws.rs.spec.filter.exception;

import ee.jakarta.tck.ws.rs.common.client.JaxrsCommonClient;
import java.io.InputStream;
import java.io.IOException;

import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;
import jakarta.ws.rs.core.Response.Status;
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
    setContextRoot("/jaxrs_spec_filter_exception_web/resource");
  }

 
  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException{
    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/spec/filter/exception/web.xml.template");
    String webXml = editWebXmlString(inStream);
    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_spec_filter_exception_web.war");
    archive.addClasses(TSAppConfig.class, Resource.class, AbstractAddFilter.class, AbstractAddInterceptor.class, AddOneFilter.class, AddOneInterceptor.class, AddTenGlobalFilter.class, AddTenGlobalInterceptor.class, ExceptionNameBinding.class, NeverUsedExceptionMapper.class, PostMatchingThrowingFilter.class, PreMatchingThrowingFilter.class, RuntimeExceptionMapper.class, JaxrsUtil.class);
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
   * @testName: throwExceptionOnPostMatchingFilterTest
   * 
   * @assertion_ids: JAXRS:SPEC:90; JAXRS:SPEC:91; JAXRS:SPEC:91.1;
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
  public void throwExceptionOnPostMatchingFilterTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "echo"));
    setProperty(Property.REQUEST_HEADERS,
        PostMatchingThrowingFilter.EXCEPTION_FIRING_HEADER + ":100");
    setProperty(Property.CONTENT, "0");
    setProperty(Property.SEARCH_STRING, "11111");
    invoke();
    logMsg("Exception has been handled as expected");
  }

  /*
   * @testName: throwExceptionOnPreMatchingFilterTest
   * 
   * @assertion_ids: JAXRS:SPEC:90; JAXRS:SPEC:91; JAXRS:SPEC:91.2;
   * 
   * @test_Strategy: When a filter or interceptor method throws an exception,
   * the JAX-RS runtime will attempt to map the exception
   * 
   * Otherwise, only globally bound filters in the Container Response chain MUST
   * be invoked
   */
  @Test
  public void throwExceptionOnPreMatchingFilterTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "echo"));
    setProperty(Property.REQUEST_HEADERS,
        PreMatchingThrowingFilter.EXCEPTION_FIRING_HEADER + ":100");
    setProperty(Property.CONTENT, "0");
    setProperty(Property.SEARCH_STRING, "10110");
    invoke();
    logMsg("Exception has been handled as expected");
  }

  /*
   * @testName: throwExceptionOnInterceptorTest
   * 
   * @assertion_ids: JAXRS:SPEC:90;
   * 
   * @test_Strategy: When a filter or interceptor method throws an exception,
   * the JAX-RS runtime will attempt to map the exception
   */
  @Test
  public void throwExceptionOnInterceptorTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "echo"));
    setProperty(Property.REQUEST_HEADERS,
        AddTenGlobalInterceptor.EXCEPTION_FIRING_HEADER + ":100");
    setProperty(Property.CONTENT, "0");
    setProperty(Property.SEARCH_STRING, "11111");
    invoke();
    logMsg("Exception has been handled as expected");
  }

  /*
   * @testName: noNameBoundInterceptorTest
   * 
   * @assertion_ids: JAXRS:SPEC:90;
   * 
   * @test_Strategy: Just to be sure we have only one global binding interceptor
   */
  @Test
  public void noNameBoundInterceptorTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "nobinding"));
    setProperty(Property.CONTENT, "0");
    setProperty(Property.SEARCH_STRING, "10020");
    invoke();
    logMsg("Only Globally bound interceptor has been invoked as expected");
  }

  /*
   * @testName: throwSecondExceptionFromMapperFirstFromInterceptorTest
   * 
   * @assertion_ids: JAXRS:SPEC:90; JAXRS:SPEC:91;
   * 
   * @test_Strategy: When a filter or interceptor method throws an exception,
   * the JAX-RS runtime will attempt to map the exception
   * 
   * At most one exception mapper will be used in a single request processing
   * cycle to avoid potentially infinite loops.
   */
  @Test
  public void throwSecondExceptionFromMapperFirstFromInterceptorTest()
      throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "echo"));
    setProperty(Property.REQUEST_HEADERS,
        AddTenGlobalInterceptor.EXCEPTION_FIRING_HEADER + ":"
            + RuntimeExceptionMapper.THROW_AGAIN);
    setProperty(Property.CONTENT, "0");
    setProperty(Property.STATUS_CODE,
        getStatusCode(Status.INTERNAL_SERVER_ERROR));
    invoke();
    logMsg("Exception has not been handled second time as expected");
  }

  /*
   * @testName: throwSecondExceptionFromMapperFirstFromPreMatchingFilterTest
   * 
   * @assertion_ids: JAXRS:SPEC:90; JAXRS:SPEC:91;
   * 
   * @test_Strategy: When a filter or interceptor method throws an exception,
   * the JAX-RS runtime will attempt to map the exception
   * 
   * At most one exception mapper will be used in a single request processing
   * cycle to avoid potentially infinite loops.
   */
  @Test
  public void throwSecondExceptionFromMapperFirstFromPreMatchingFilterTest()
      throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "echo"));
    setProperty(Property.REQUEST_HEADERS,
        PreMatchingThrowingFilter.EXCEPTION_FIRING_HEADER + ":"
            + RuntimeExceptionMapper.THROW_AGAIN);
    setProperty(Property.CONTENT, "0");
    setProperty(Property.STATUS_CODE,
        getStatusCode(Status.INTERNAL_SERVER_ERROR));
    invoke();
    logMsg("Exception has not been handled second time as expected");
  }

  /*
   * @testName: throwSecondExceptionFromMapperFirstFromPostMatchingFilterTest
   * 
   * @assertion_ids: JAXRS:SPEC:90; JAXRS:SPEC:91;
   * 
   * @test_Strategy: When a filter or interceptor method throws an exception,
   * the JAX-RS runtime will attempt to map the exception
   * 
   * At most one exception mapper will be used in a single request processing
   * cycle to avoid potentially infinite loops.
   */
  @Test
  public void throwSecondExceptionFromMapperFirstFromPostMatchingFilterTest()
      throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "echo"));
    setProperty(Property.REQUEST_HEADERS,
        PostMatchingThrowingFilter.EXCEPTION_FIRING_HEADER + ":"
            + RuntimeExceptionMapper.THROW_AGAIN);
    setProperty(Property.CONTENT, "0");
    setProperty(Property.STATUS_CODE,
        getStatusCode(Status.INTERNAL_SERVER_ERROR));
    invoke();
    logMsg("Exception has not been handled second time as expected");
  }

  /*
   * @testName: throwSecondExceptionFromInterceptorFirstFromInterceptorTest
   * 
   * @assertion_ids: JAXRS:SPEC:90; JAXRS:SPEC:91;
   * 
   * @test_Strategy: When a filter or interceptor method throws an exception,
   * the JAX-RS runtime will attempt to map the exception
   * 
   * At most one exception mapper will be used in a single request processing
   * cycle to avoid potentially infinite loops.
   */
  @Test
  public void throwSecondExceptionFromInterceptorFirstFromInterceptorTest()
      throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "echo"));
    setProperty(Property.REQUEST_HEADERS,
        AddTenGlobalInterceptor.EXCEPTION_FIRING_HEADER + ":"
            + AddTenGlobalInterceptor.EXCEPTION_FIRING_HEADER);
    setProperty(Property.CONTENT, "0");
    setProperty(Property.STATUS_CODE,
        getStatusCode(Status.INTERNAL_SERVER_ERROR));
    invoke();
    logMsg("Exception has not been handled second time as expected");
  }

  /*
   * @testName:
   * throwSecondExceptionFromInterceptorFirstFromPreMatchingFilterTest
   * 
   * @assertion_ids: JAXRS:SPEC:90; JAXRS:SPEC:91;
   * 
   * @test_Strategy: When a filter or interceptor method throws an exception,
   * the JAX-RS runtime will attempt to map the exception
   * 
   * At most one exception mapper will be used in a single request processing
   * cycle to avoid potentially infinite loops.
   */
  @Test
  public void throwSecondExceptionFromInterceptorFirstFromPreMatchingFilterTest()
      throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "echo"));
    setProperty(Property.REQUEST_HEADERS,
        PreMatchingThrowingFilter.EXCEPTION_FIRING_HEADER + ":"
            + AddTenGlobalInterceptor.EXCEPTION_FIRING_HEADER);
    setProperty(Property.CONTENT, "0");
    setProperty(Property.STATUS_CODE,
        getStatusCode(Status.INTERNAL_SERVER_ERROR));
    invoke();
    logMsg("Exception has not been handled second time as expected");
  }

  /*
   * @testName:
   * throwSecondExceptionFromInterceptorFirstFromPostMatchingFilterTest
   * 
   * @assertion_ids: JAXRS:SPEC:90; JAXRS:SPEC:91;
   * 
   * @test_Strategy: When a filter or interceptor method throws an exception,
   * the JAX-RS runtime will attempt to map the exception
   * 
   * At most one exception mapper will be used in a single request processing
   * cycle to avoid potentially infinite loops.
   */
  @Test
  public void throwSecondExceptionFromInterceptorFirstFromPostMatchingFilterTest()
      throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "echo"));
    setProperty(Property.REQUEST_HEADERS,
        PostMatchingThrowingFilter.EXCEPTION_FIRING_HEADER + ":"
            + AddTenGlobalInterceptor.EXCEPTION_FIRING_HEADER);
    setProperty(Property.CONTENT, "0");
    setProperty(Property.STATUS_CODE,
        getStatusCode(Status.INTERNAL_SERVER_ERROR));
    invoke();
    logMsg("Exception has not been handled second time as expected");
  }

  /*
   * @testName: throwNoExceptionFromPostMatchingFilterFirstFromInterceptorTest
   * 
   * @assertion_ids: JAXRS:SPEC:90; JAXRS:SPEC:91;
   * 
   * @test_Strategy: When a filter or interceptor method throws an exception,
   * the JAX-RS runtime will attempt to map the exception
   * 
   * At most one exception mapper will be used in a single request processing
   * cycle to avoid potentially infinite loops.
   */
  @Test
  public void throwNoExceptionFromPostMatchingFilterFirstFromInterceptorTest()
      throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "echo"));
    setProperty(Property.REQUEST_HEADERS,
        AddTenGlobalInterceptor.EXCEPTION_FIRING_HEADER + ":"
            + PostMatchingThrowingFilter.EXCEPTION_FIRING_HEADER);
    setProperty(Property.CONTENT, "0");
    setProperty(Property.SEARCH_STRING, "111011");
    invoke();
    logMsg("Exception has not been handled second time as expected");
  }

  /*
   * @testName:
   * throwNoExceptionFromPostMatchingFilterFirstFromPostMatchingFilterTest
   * 
   * @assertion_ids: JAXRS:SPEC:90; JAXRS:SPEC:91;
   * 
   * @test_Strategy: When a filter or interceptor method throws an exception,
   * the JAX-RS runtime will attempt to map the exception
   * 
   * At most one exception mapper will be used in a single request processing
   * cycle to avoid potentially infinite loops.
   * 
   * The request filter is not used on response from mapper
   */
  @Test
  public void throwNoExceptionFromPostMatchingFilterFirstFromPostMatchingFilterTest()
      throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "echo"));
    setProperty(Property.REQUEST_HEADERS,
        PostMatchingThrowingFilter.EXCEPTION_FIRING_HEADER + ":"
            + PostMatchingThrowingFilter.EXCEPTION_FIRING_HEADER);
    setProperty(Property.CONTENT, "0");
    setProperty(Property.SEARCH_STRING, "111011");
    invoke();
    logMsg("Exception has not been handled second time as expected");
  }

  /*
   * @testName:
   * throwNoExceptionFromPostMatchingFilterFirstFromPreMatchingFilterTest
   * 
   * @assertion_ids: JAXRS:SPEC:90; JAXRS:SPEC:91;
   * 
   * @test_Strategy: When a filter or interceptor method throws an exception,
   * the JAX-RS runtime will attempt to map the exception
   * 
   * At most one exception mapper will be used in a single request processing
   * cycle to avoid potentially infinite loops.
   * 
   * The request filter is not used on response from mapper
   */
  @Test
  public void throwNoExceptionFromPostMatchingFilterFirstFromPreMatchingFilterTest()
      throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "echo"));
    setProperty(Property.REQUEST_HEADERS,
        PreMatchingThrowingFilter.EXCEPTION_FIRING_HEADER + ":"
            + PostMatchingThrowingFilter.EXCEPTION_FIRING_HEADER);
    setProperty(Property.CONTENT, "0");
    setProperty(Property.SEARCH_STRING, "110010");
    invoke();
    logMsg("Exception has not been handled second time as expected");
  }
}
