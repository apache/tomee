/*
 * Copyright (c) 2014, 2020 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.rs.beanparam.header.plain;

import java.io.InputStream;
import java.io.IOException;

import ee.jakarta.tck.ws.rs.ee.rs.JaxrsParamClient;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityPrototype;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityThrowingExceptionGivenByName;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityThrowingWebApplicationException;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithConstructor;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithFromString;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityWithValueOf;
import ee.jakarta.tck.ws.rs.ee.rs.ParamTest;
import ee.jakarta.tck.ws.rs.ee.rs.RuntimeExceptionMapper;
import ee.jakarta.tck.ws.rs.ee.rs.WebApplicationExceptionMapper;
import ee.jakarta.tck.ws.rs.ee.rs.beanparam.header.bean.HeaderBeanParamEntity;
import ee.jakarta.tck.ws.rs.ee.rs.beanparam.header.bean.InnerHeaderBeanParamEntity;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

import ee.jakarta.tck.ws.rs.ee.rs.Constants;
import ee.jakarta.tck.ws.rs.ee.rs.beanparam.BeanParamCommonClient;

import jakarta.ws.rs.core.Response.Status;

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
 * @since 2.0.1                     
 */
@ExtendWith(ArquillianExtension.class)
public class JAXRSClientIT extends BeanParamCommonClient {
  private static final long serialVersionUID = 201L;

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_ee_rs_beanparam_header_plain_web/resource");
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

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/beanparam/header/plain/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_rs_beanparam_header_plain_web.war");
    archive.addClasses(AppConfig.class, Resource.class,
      HeaderBeanParamEntity.class,
      InnerHeaderBeanParamEntity.class,
      Constants.class,
      ParamEntityPrototype.class,
      ParamEntityWithConstructor.class,
      ParamEntityWithValueOf.class,
      ParamEntityWithFromString.class,
      ParamTest.class,
      JaxrsParamClient.CollectionName.class,
      ParamEntityThrowingWebApplicationException.class,
      ParamEntityThrowingExceptionGivenByName.class,
      RuntimeExceptionMapper.class,
      WebApplicationExceptionMapper.class
    );
    archive.setWebXML(new StringAsset(webXml));
    return archive;

  }

  /* Run test */

  /*
   * @testName: headerParamEntityWithConstructorTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.5; JAXRS:SPEC:5.2; JAXRS:JAVADOC:6;
   * JAXRS:JAVADOC:12; JAXRS:JAVADOC:12.1;
   * 
   * @test_Strategy: Verify that named HeaderParam is handled properly
   */
  @Test
  public void headerParamEntityWithConstructorTest() throws Fault {
    super.paramEntityWithConstructorTest();
  }

  /*
   * @testName: headerParamEntityWithValueOfTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.5; JAXRS:SPEC:5.3; JAXRS:JAVADOC:6;
   * JAXRS:JAVADOC:12; JAXRS:JAVADOC:12.1;
   * 
   * @test_Strategy: Verify that named HeaderParam is handled properly
   */
  @Test
  public void headerParamEntityWithValueOfTest() throws Fault {
    super.paramEntityWithValueOfTest();
  }

  /*
   * @testName: headerParamEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.5; JAXRS:SPEC:5.3; JAXRS:JAVADOC:6;
   * JAXRS:JAVADOC:12; JAXRS:JAVADOC:12.1;
   * 
   * @test_Strategy: Verify that named HeaderParam is handled properly
   */
  @Test
  public void headerParamEntityWithFromStringTest() throws Fault {
    super.paramEntityWithFromStringTest();
  }

  /*
   * @testName: headerParamSetEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.5; JAXRS:SPEC:5.4; JAXRS:JAVADOC:6;
   * JAXRS:JAVADOC:12; JAXRS:JAVADOC:12.1;
   * 
   * @test_Strategy: Verify that named HeaderParam is handled properly
   */
  @Test
  public void headerParamSetEntityWithFromStringTest() throws Fault {
    super.paramCollectionEntityWithFromStringTest(CollectionName.SET);
  }

  /*
   * @testName: headerParamSortedSetEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.5; JAXRS:SPEC:5.4; JAXRS:JAVADOC:6;
   * JAXRS:JAVADOC:12; JAXRS:JAVADOC:12.1;
   * 
   * @test_Strategy: Verify that named HeaderParam is handled properly
   */
  @Test
  public void headerParamSortedSetEntityWithFromStringTest() throws Fault {
    super.paramCollectionEntityWithFromStringTest(CollectionName.SORTED_SET);
  }

  /*
   * @testName: headerParamListEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.5; JAXRS:SPEC:5.4; JAXRS:JAVADOC:6;
   * JAXRS:JAVADOC:12; JAXRS:JAVADOC:12.1;
   * 
   * @test_Strategy: Verify that named HeaderParam is handled properly
   */
  @Test
  public void headerParamListEntityWithFromStringTest() throws Fault {
    super.paramCollectionEntityWithFromStringTest(CollectionName.LIST);
  }

  /*
   * @testName: headerFieldParamEntityWithConstructorTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.5; JAXRS:SPEC:5.2; JAXRS:JAVADOC:6;
   * 
   * @test_Strategy: Verify that named HeaderParam is handled properly
   */
  @Test
  public void headerFieldParamEntityWithConstructorTest() throws Fault {
    super.fieldEntityWithConstructorTest();
  }

  /*
   * @testName: headerFieldParamEntityWithValueOfTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.5; JAXRS:SPEC:5.3; JAXRS:JAVADOC:6;
   * 
   * @test_Strategy: Verify that named HeaderParam is handled properly
   */
  @Test
  public void headerFieldParamEntityWithValueOfTest() throws Fault {
    super.fieldEntityWithValueOfTest();
  }

  /*
   * @testName: headerFieldParamEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.5; JAXRS:SPEC:5.3; JAXRS:JAVADOC:6;
   * 
   * @test_Strategy: Verify that named HeaderParam is handled properly
   */
  @Test
  public void headerFieldParamEntityWithFromStringTest() throws Fault {
    super.fieldEntityWithFromStringTest();
  }

  /*
   * @testName: headerFieldParamSetEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.5; JAXRS:SPEC:5.4; JAXRS:JAVADOC:6;
   * 
   * @test_Strategy: Verify that named HeaderParam is handled properly
   */
  @Test
  public void headerFieldParamSetEntityWithFromStringTest() throws Fault {
    super.fieldCollectionEntityWithFromStringTest(CollectionName.SET);
  }

  /*
   * @testName: headerFieldParamSortedSetEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.5; JAXRS:SPEC:5.4; JAXRS:JAVADOC:6;
   * 
   * @test_Strategy: Verify that named HeaderParam is handled properly
   */
  @Test
  public void headerFieldParamSortedSetEntityWithFromStringTest() throws Fault {
    super.fieldCollectionEntityWithFromStringTest(CollectionName.SORTED_SET);
  }

  /*
   * @testName: headerFieldParamListEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.5; JAXRS:SPEC:5.4; JAXRS:JAVADOC:6;
   * 
   * @test_Strategy: Verify that named HeaderParam is handled properly
   */
  @Test
  public void headerFieldParamListEntityWithFromStringTest() throws Fault {
    super.fieldCollectionEntityWithFromStringTest(CollectionName.LIST);
  }

  /*
   * @testName: headerParamThrowingWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:12.3;
   * 
   * @test_Strategy: Exceptions thrown during construction of parameter values
   * are treated the same as exceptions thrown during construction of field or
   * bean property values, see Section 3.2.
   */
  @Test
  public void headerParamThrowingWebApplicationExceptionTest() throws Fault {
    super.paramThrowingWebApplicationExceptionTest();
    super.paramThrowingWebApplicationExceptionTest();
  }

  /*
   * @testName: headerFieldThrowingWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.1; JAXRS:SPEC:8;
   * 
   * @test_Strategy: A WebApplicationException thrown during construction of
   * field or property values using 2 or 3 above is processed directly as
   * described in section 3.3.4.
   */
  @Test
  public void headerFieldThrowingWebApplicationExceptionTest() throws Fault {
    super.fieldThrowingWebApplicationExceptionTest();
    super.fieldThrowingWebApplicationExceptionTest();
  }

  /*
   * @testName: headerParamThrowingIllegalArgumentExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:12.3;
   * 
   * @test_Strategy: Exceptions thrown during construction of parameter values
   * are treated the same as exceptions thrown during construction of field or
   * bean property values, see section 3.2.
   */
  @Test
  public void headerParamThrowingIllegalArgumentExceptionTest() throws Fault {
    setProperty(Property.UNORDERED_SEARCH_STRING, Status.BAD_REQUEST.name());
    super.paramThrowingIllegalArgumentExceptionTest();
    setProperty(Property.UNORDERED_SEARCH_STRING, Status.BAD_REQUEST.name());
    super.paramThrowingIllegalArgumentExceptionTest();
  }

  /*
   * @testName: headerFieldThrowingIllegalArgumentExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:9; JAXRS:SPEC:9.2; JAXRS:SPEC:10;
   * 
   * @test_Strategy: Other exceptions thrown during construction of field or
   * property values using 2 or 3 above are treated as client errors:
   *
   * if the field or property is annotated with @HeaderParam or @CookieParam
   * then an implementation MUST generate a WebApplicationException that wraps
   * the thrown exception with a client error response (400 status) and no
   * entity.
   */
  @Test
  public void headerFieldThrowingIllegalArgumentExceptionTest() throws Fault {
    setProperty(Property.UNORDERED_SEARCH_STRING, Status.BAD_REQUEST.name());
    super.fieldThrowingIllegalArgumentExceptionTest();
    setProperty(Property.UNORDERED_SEARCH_STRING, Status.BAD_REQUEST.name());
    super.fieldThrowingIllegalArgumentExceptionTest();
  }

  @Override
  protected String buildRequest(String param) {
    if (!"".equals(param)) {
      String par = param.replace("=", ":");
      setProperty(Property.REQUEST_HEADERS, par);
      setProperty(Property.REQUEST_HEADERS, Constants.INNER, par);
    }
    return buildRequest(Request.GET, fieldBeanParam);
  }

  @Override
  protected//
  String buildRequestForException(String param, int entity) throws Fault {
    String par = param.replace("=", ":");
    if (entity == 1) {
      setProperty(Property.REQUEST_HEADERS, par);
    } else {
      setProperty(Property.REQUEST_HEADERS, Constants.INNER, par);
    }
    return buildRequest(Request.GET, fieldBeanParam);
  }
}
