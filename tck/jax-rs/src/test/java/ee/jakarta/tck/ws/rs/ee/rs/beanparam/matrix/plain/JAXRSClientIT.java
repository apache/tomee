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

package ee.jakarta.tck.ws.rs.ee.rs.beanparam.matrix.plain;

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
import ee.jakarta.tck.ws.rs.ee.rs.beanparam.matrix.bean.InnerMatrixBeanParamEntity;
import ee.jakarta.tck.ws.rs.ee.rs.beanparam.matrix.bean.MatrixBeanParamEntity;
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
    setContextRoot("/jaxrs_ee_rs_beanparam_matrix_plain_web/resource");
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

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/beanparam/matrix/plain/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_rs_beanparam_matrix_plain_web.war");
    archive.addClasses(AppConfig.class, Resource.class,
      MatrixBeanParamEntity.class,
      InnerMatrixBeanParamEntity.class,
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
   * @testName: matrixParamEntityWithConstructorTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.1; JAXRS:SPEC:5.2; JAXRS:JAVADOC:6;
   * JAXRS:JAVADOC:12; JAXRS:JAVADOC:12.1;
   * 
   * @test_Strategy: Verify that named MatrixParam is handled properly
   */
  @Test
  public void matrixParamEntityWithConstructorTest() throws Fault {
    paramEntityWithConstructorTest();
  }

  /*
   * @testName: matrixParamEntityWithValueOfTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.1; JAXRS:SPEC:5.3; JAXRS:JAVADOC:6;
   * JAXRS:JAVADOC:12; JAXRS:JAVADOC:12.1;
   * 
   * @test_Strategy: Verify that named MatrixParam is handled properly
   */
  @Test
  public void matrixParamEntityWithValueOfTest() throws Fault {
    paramEntityWithValueOfTest();
  }

  /*
   * @testName: matrixParamEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.1; JAXRS:SPEC:5.3; JAXRS:JAVADOC:6;
   * JAXRS:JAVADOC:12; JAXRS:JAVADOC:12.1;
   * 
   * @test_Strategy: Verify that named MatrixParam is handled properly
   */
  @Test
  public void matrixParamEntityWithFromStringTest() throws Fault {
    paramEntityWithFromStringTest();
  }

  /*
   * @testName: matrixParamSetEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.1; JAXRS:SPEC:5.4; JAXRS:JAVADOC:6;
   * 
   * @test_Strategy: Verify that named MatrixParam is handled properly
   */
  @Test
  public void matrixParamSetEntityWithFromStringTest() throws Fault {
    paramCollectionEntityWithFromStringTest(CollectionName.SET);
  }

  /*
   * @testName: matrixParamSortedSetEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.1; JAXRS:SPEC:5.4; JAXRS:JAVADOC:6;
   * JAXRS:JAVADOC:12; JAXRS:JAVADOC:12.1;
   * 
   * @test_Strategy: Verify that named MatrixParam is handled properly
   */
  @Test
  public void matrixParamSortedSetEntityWithFromStringTest() throws Fault {
    paramCollectionEntityWithFromStringTest(CollectionName.SORTED_SET);
  }

  /*
   * @testName: matrixParamListEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.1; JAXRS:SPEC:5.4; JAXRS:JAVADOC:6;
   * JAXRS:JAVADOC:12; JAXRS:JAVADOC:12.1;
   * 
   * @test_Strategy: Verify that named MatrixParam is handled properly
   */
  @Test
  public void matrixParamListEntityWithFromStringTest() throws Fault {
    paramCollectionEntityWithFromStringTest(CollectionName.LIST);
  }

  /*
   * @testName: matrixFieldParamEntityWithConstructorTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.1; JAXRS:SPEC:5.2; JAXRS:JAVADOC:6;
   * 
   * @test_Strategy: Verify that named MatrixParam is handled properly
   */
  @Test
  public void matrixFieldParamEntityWithConstructorTest() throws Fault {
    fieldEntityWithConstructorTest();
  }

  /*
   * @testName: matrixFieldParamEntityWithValueOfTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.1; JAXRS:SPEC:5.3; JAXRS:JAVADOC:6;
   * 
   * @test_Strategy: Verify that named MatrixParam is handled properly
   */
  @Test
  public void matrixFieldParamEntityWithValueOfTest() throws Fault {
    fieldEntityWithValueOfTest();
  }

  /*
   * @testName: matrixFieldParamEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.1; JAXRS:SPEC:5.3; JAXRS:JAVADOC:6;
   * 
   * @test_Strategy: Verify that named MatrixParam is handled properly
   */
  @Test
  public void matrixFieldParamEntityWithFromStringTest() throws Fault {
    fieldEntityWithFromStringTest();
  }

  /*
   * @testName: matrixFieldParamSetEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.1; JAXRS:SPEC:5.4; JAXRS:JAVADOC:6;
   * 
   * @test_Strategy: Verify that named MatrixParam is handled properly
   */
  @Test
  public void matrixFieldParamSetEntityWithFromStringTest() throws Fault {
    fieldCollectionEntityWithFromStringTest(CollectionName.SET);
  }

  /*
   * @testName: matrixFieldParamSortedSetEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.1; JAXRS:SPEC:5.4; JAXRS:JAVADOC:6;
   * 
   * @test_Strategy: Verify that named MatrixParam is handled properly
   */
  @Test
  public void matrixFieldParamSortedSetEntityWithFromStringTest() throws Fault {
    fieldCollectionEntityWithFromStringTest(CollectionName.SORTED_SET);
  }

  /*
   * @testName: matrixFieldParamListEntityWithFromStringTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.1; JAXRS:SPEC:5.4; JAXRS:JAVADOC:6;
   * 
   * @test_Strategy: Verify that named MatrixParam is handled properly
   */
  @Test
  public void matrixFieldParamListEntityWithFromStringTest() throws Fault {
    fieldCollectionEntityWithFromStringTest(CollectionName.LIST);
  }

  /*
   * @testName: matrixParamEntityWithEncodedTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.1; JAXRS:SPEC:7; JAXRS:SPEC:12.2;
   * 
   * @test_Strategy: Verify that named MatrixParam @Encoded is handled
   */
  @Test
  public void matrixParamEntityWithEncodedTest() throws Fault {
    super.paramEntityWithEncodedTest();
  }

  /*
   * @testName: matrixFieldParamEntityWithEncodedTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.1; JAXRS:SPEC:7;
   * 
   * @test_Strategy: Verify that named MatrixParam @Encoded is handled
   */
  @Test
  public void matrixFieldParamEntityWithEncodedTest() throws Fault {
    super.fieldEntityWithEncodedTest();
  }

  /*
   * @testName: matrixParamThrowingWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:12.3;
   * 
   * @test_Strategy: Exceptions thrown during construction of parameter values
   * are treated the same as exceptions thrown during construction of field or
   * bean property values, see Section 3.2.
   */
  @Test
  public void matrixParamThrowingWebApplicationExceptionTest() throws Fault {
    super.paramThrowingWebApplicationExceptionTest();
    super.paramThrowingWebApplicationExceptionTest();
  }

  /*
   * @testName: matrixFieldThrowingWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.1; JAXRS:SPEC:8;
   * 
   * @test_Strategy: A WebApplicationException thrown during construction of
   * field or property values using 2 or 3 above is processed directly as
   * described in section 3.3.4.
   */
  @Test
  public void matrixFieldThrowingWebApplicationExceptionTest() throws Fault {
    super.fieldThrowingWebApplicationExceptionTest();
    super.fieldThrowingWebApplicationExceptionTest();
  }

  /*
   * @testName: matrixParamThrowingIllegalArgumentExceptionTest
   * 
   * @assertion_ids: JAXRS:SPEC:12.3;
   * 
   * @test_Strategy: Exceptions thrown during construction of parameter values
   * are treated the same as exceptions thrown during construction of field or
   * bean property values, see section 3.2.
   */
  @Test
  public void matrixParamThrowingIllegalArgumentExceptionTest() throws Fault {
    setProperty(Property.UNORDERED_SEARCH_STRING, Status.NOT_FOUND.name());
    super.paramThrowingIllegalArgumentExceptionTest();
    setProperty(Property.UNORDERED_SEARCH_STRING, Status.NOT_FOUND.name());
    super.paramThrowingIllegalArgumentExceptionTest();
  }

  /*
   * @testName: matrixFieldThrowingIllegalArgumentExceptionTest
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
  public void matrixFieldThrowingIllegalArgumentExceptionTest() throws Fault {
    setProperty(Property.UNORDERED_SEARCH_STRING, Status.NOT_FOUND.name());
    super.fieldThrowingIllegalArgumentExceptionTest();
    setProperty(Property.UNORDERED_SEARCH_STRING, Status.NOT_FOUND.name());
    super.fieldThrowingIllegalArgumentExceptionTest();
  }

  @Override
  protected String buildRequest(String param) {
    return buildRequest(Request.GET, fieldBeanParam, ";", param, ";",
        Constants.INNER, param);
  }

  @Override
  protected//
  String buildRequestForException(String param, int entity) throws Fault {
    if (entity == 1)
      return buildRequest(Request.GET, fieldBeanParam, ";", param);
    else
      return buildRequest(Request.GET, fieldBeanParam, ";", Constants.INNER,
          param);
  }

}
