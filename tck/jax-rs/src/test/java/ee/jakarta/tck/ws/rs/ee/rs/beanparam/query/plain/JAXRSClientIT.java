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

package ee.jakarta.tck.ws.rs.ee.rs.beanparam.query.plain;

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
import ee.jakarta.tck.ws.rs.ee.rs.beanparam.query.bean.InnerQueryBeanParamEntity;
import ee.jakarta.tck.ws.rs.ee.rs.beanparam.query.bean.QueryBeanParamEntity;
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
    setContextRoot("/jaxrs_ee_rs_beanparam_query_plain_web/resource");
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

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/beanparam/query/plain/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_rs_beanparam_query_plain_web.war");
    archive.addClasses(AppConfig.class, Resource.class,
      QueryBeanParamEntity.class,
      InnerQueryBeanParamEntity.class,
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
   * @testName: queryParamEntityWithConstructorTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.2; JAXRS:SPEC:12;
   * JAXRS:SPEC:12.1;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void queryParamEntityWithConstructorTest() throws Fault {
    super.paramEntityWithConstructorTest();
  }

  /*
   * @testName: queryParamEntityWithValueOfTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.3; JAXRS:SPEC:12;
   * JAXRS:SPEC:12.1;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void queryParamEntityWithValueOfTest() throws Fault {
    super.paramEntityWithValueOfTest();
  }

  /*
   * @testName: queryParamEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.3; JAXRS:SPEC:12;
   * JAXRS:SPEC:12.1;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void queryParamEntityWithFromStringTest() throws Fault {
    super.paramEntityWithFromStringTest();
  }

  /*
   * @testName: queryParamSetEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.4; JAXRS:SPEC:12;
   * JAXRS:SPEC:12.1;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void queryParamSetEntityWithFromStringTest() throws Fault {
    super.paramCollectionEntityWithFromStringTest(CollectionName.SET);
  }

  /*
   * @testName: queryParamSortedSetEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.4; JAXRS:SPEC:12;
   * JAXRS:SPEC:12.1;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void queryParamSortedSetEntityWithFromStringTest() throws Fault {
    super.paramCollectionEntityWithFromStringTest(CollectionName.SORTED_SET);
  }

  /*
   * @testName: queryParamListEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.4; JAXRS:SPEC:12;
   * JAXRS:SPEC:12.1;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void queryParamListEntityWithFromStringTest() throws Fault {
    super.paramCollectionEntityWithFromStringTest(CollectionName.LIST);
  }

  /*
   * @testName: queryFieldParamEntityWithConstructorTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.2; JAXRS:SPEC:6;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void queryFieldParamEntityWithConstructorTest() throws Fault {
    super.fieldEntityWithConstructorTest();
  }

  /*
   * @testName: queryFieldParamEntityWithValueOfTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.3; JAXRS:SPEC:6;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void queryFieldParamEntityWithValueOfTest() throws Fault {
    super.fieldEntityWithValueOfTest();
  }

  /*
   * @testName: queryFieldParamEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.3; JAXRS:SPEC:6;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void queryFieldParamEntityWithFromStringTest() throws Fault {
    super.fieldEntityWithFromStringTest();
  }

  /*
   * @testName: queryFieldParamSetEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.4; JAXRS:SPEC:6;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void queryFieldParamSetEntityWithFromStringTest() throws Fault {
    super.fieldCollectionEntityWithFromStringTest(CollectionName.SET);
  }

  /*
   * @testName: queryFieldParamSortedSetEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.4; JAXRS:SPEC:6;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void queryFieldParamSortedSetEntityWithFromStringTest() throws Fault {
    super.fieldCollectionEntityWithFromStringTest(CollectionName.SORTED_SET);
  }

  /*
   * @testName: queryFieldParamListEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:5.4; JAXRS:SPEC:6;
   * 
   * @test_Strategy: Verify that named QueryParam is handled properly
   */
  @Test
  public void queryFieldParamListEntityWithFromStringTest() throws Fault {
    super.fieldCollectionEntityWithFromStringTest(CollectionName.LIST);
  }

  /*
   * @testName: queryParamEntityWithEncodedTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:7; JAXRS:SPEC:12;
   * JAXRS:SPEC:12.2;
   * 
   * @test_Strategy: Verify that named QueryParam @Encoded is handled
   */
  @Test
  public void queryParamEntityWithEncodedTest() throws Fault {
    super.paramEntityWithEncodedTest();
  }

  /*
   * @testName: queryFieldParamEntityWithEncodedTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:7;
   * 
   * @test_Strategy: Verify that named QueryParam @Encoded is handled
   */
  @Test
  public void queryFieldParamEntityWithEncodedTest() throws Fault {
    super.fieldEntityWithEncodedTest();
  }

  /*
   * @testName: queryParamThrowingWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:12.3;
   * 
   * @test_Strategy: Exceptions thrown during construction of parameter values
   * are treated the same as exceptions thrown during construction of field or
   * bean property values, see Section 3.2.
   */
  @Test
  public void queryParamThrowingWebApplicationExceptionTest() throws Fault {
    super.paramThrowingWebApplicationExceptionTest();
    super.paramThrowingWebApplicationExceptionTest();
  }

  /*
   * @testName: queryFieldThrowingWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2; JAXRS:SPEC:8;
   * 
   * @test_Strategy: A WebApplicationException thrown during construction of
   * field or property values using 2 or 3 above is processed directly as
   * described in section 3.3.4.
   */
  @Test
  public void queryFieldThrowingWebApplicationExceptionTest() throws Fault {
    super.fieldThrowingWebApplicationExceptionTest();
    super.fieldThrowingWebApplicationExceptionTest();
  }

  /*
   * @testName: queryParamThrowingIllegalArgumentExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:12.3;
   * 
   * @test_Strategy: Exceptions thrown during construction of parameter values
   * are treated the same as exceptions thrown during construction of field or
   * bean property values, see section 3.2.
   */
  @Test
  public void queryParamThrowingIllegalArgumentExceptionTest() throws Fault {
    setProperty(Property.UNORDERED_SEARCH_STRING, Status.NOT_FOUND.name());
    super.paramThrowingIllegalArgumentExceptionTest();
    setProperty(Property.UNORDERED_SEARCH_STRING, Status.NOT_FOUND.name());
    super.paramThrowingIllegalArgumentExceptionTest();
  }

  /*
   * @testName: queryFieldThrowingIllegalArgumentExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:9; JAXRS:SPEC:9.1; JAXRS:SPEC:10;
   * 
   * @test_Strategy: Other exceptions thrown during construction of field or
   * property values using 2 or 3 above are treated as client errors:
   * 
   * if the field or property is annotated with @MatrixParam,
   * 
   * @QueryParam or @PathParam then an implementation MUST generate a
   * WebApplicationException that wraps the thrown exception with a not found
   * response (404 status) and no entity;
   */
  @Test
  public void queryFieldThrowingIllegalArgumentExceptionTest() throws Fault {
    setProperty(Property.UNORDERED_SEARCH_STRING, Status.NOT_FOUND.name());
    super.fieldThrowingIllegalArgumentExceptionTest();
    setProperty(Property.UNORDERED_SEARCH_STRING, Status.NOT_FOUND.name());
    super.fieldThrowingIllegalArgumentExceptionTest();
  }

  @Override
  protected String buildRequest(String param) {
    return buildRequest(Request.GET, fieldBeanParam, "?", param, "&",
        Constants.INNER, param);
  }

  @Override
  protected//
  String buildRequestForException(String param, int entity) throws Fault {
    if (entity == 1)
      return buildRequest(Request.GET, fieldBeanParam, "?", param);
    else
      return buildRequest(Request.GET, fieldBeanParam, "?", Constants.INNER,
          param);
  }

}
