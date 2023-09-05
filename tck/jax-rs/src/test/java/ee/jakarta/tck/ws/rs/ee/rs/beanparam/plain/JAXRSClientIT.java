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

package ee.jakarta.tck.ws.rs.ee.rs.beanparam.plain;
import java.io.InputStream;
import java.io.IOException;

import ee.jakarta.tck.ws.rs.ee.rs.beanparam.bean.BeanParamEntity;
import ee.jakarta.tck.ws.rs.ee.rs.beanparam.bean.InnerBeanParamEntity;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

import ee.jakarta.tck.ws.rs.common.client.JaxrsCommonClient;

import jakarta.ws.rs.core.MediaType;

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
/**
 * Plain String to String parameter conversion into @BeanParam annotated bean
 * 
 * @since 2.0.1
 */
@ExtendWith(ArquillianExtension.class)
public class JAXRSClientIT extends JaxrsCommonClient {

  private static final long serialVersionUID = 201;

  private static final String CONTENT = "Anything";

  private static final String FIRST = "FIRST";

  private static final String SECOND = "SECOND";

  private static final String THIRD = "Third";

  private static final String FOURTH = "Fourth";

  private static final String FIFTH = "Fifth";

  private static final String SIXTH = "Sixth";

  private static final String SEVENTH = "Seventh";

  private static final String EIGHTH = "Eighth";

  private static final String NINETH = "Nineth";

  private static final String TENTH = "Tenth";

  private static final String ELEVENTH = "Eleventh";

  private static final String TWELVENTH = "Twelveth";

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_ee_rs_beanparam_plain_web/resource");
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

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/beanparam/plain/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_rs_beanparam_plain_web.war");
    archive.addClasses(AppConfig.class, Resource.class,
      BeanParamEntity.class,
      InnerBeanParamEntity.class
    );
    archive.setWebXML(new StringAsset(webXml));
    return archive;

  }

  /*
   * @testName: queryParamInParamTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2;
   * 
   * @test_Strategy: Make sure @QueryParam works in @BeanParam annotated bean
   */
  @Test
  public void queryParamInParamTest() throws Fault {
    String[] reqArgs = getRequestArguments("Query");
    String request = buildRequestQuery(Request.POST, "queryparam", reqArgs[0],
        reqArgs[1]);
    invoke(request);
  }

  /*
   * @testName: queryParamOnFieldTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.2;
   * 
   * @test_Strategy: Make sure @QueryParam works in @BeanParam annotated bean
   */
  @Test
  public void queryParamOnFieldTest() throws Fault {
    String[] reqArgs = getRequestArguments("Query");
    String request = buildRequestQuery(Request.POST, "queryfield", reqArgs[0],
        reqArgs[1]);
    invoke(request);
  }

  /*
   * @testName: formParamInParamTest
   * 
   * @assertion_ids: JAXRS:SPEC:12;
   * 
   * @test_Strategy: Make sure @FormParam works in @BeanParam annotated bean
   */
  @Test
  public void formParamInParamTest() throws Fault {
    String[] reqArgs = getRequestArguments("Form");
    String request = buildRequest(Request.POST, "formparam");
    invoke(request, andize(CONTENT, reqArgs[0], reqArgs[1]));
  }

  /*
   * @testName: formParamOnFieldTest
   * 
   * @assertion_ids: JAXRS:SPEC:12;
   * 
   * @test_Strategy: Make sure @FormParam works in @BeanParam annotated bean
   */
  @Test
  public void formParamOnFieldTest() throws Fault {
    String[] reqArgs = getRequestArguments("Form");
    String request = buildRequest(Request.POST, "formfield");
    invoke(request, andize(CONTENT, reqArgs[0], reqArgs[1]));
  }

  /*
   * @testName: headerParamInParamTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.5;
   * 
   * @test_Strategy: Make sure @HeaderParam works in @BeanParam annotated bean
   */
  @Test
  public void headerParamInParamTest() throws Fault {
    String[] reqArgs = getRequestArguments("Header");
    String request = buildRequest(Request.POST, "headerparam");
    setProperty(Property.REQUEST_HEADERS, reqArgs[0].replace('=', ':'));
    setProperty(Property.REQUEST_HEADERS, reqArgs[1].replace('=', ':'));
    invoke(request);
  }

  /*
   * @testName: headerParamOnFieldTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.5;
   * 
   * @test_Strategy: Make sure @HeaderParam works in @BeanParam annotated bean
   */
  @Test
  public void headerParamOnFieldTest() throws Fault {
    String[] reqArgs = getRequestArguments("Header");
    String request = buildRequest(Request.POST, "headerfield");
    setProperty(Property.REQUEST_HEADERS, reqArgs[0].replace('=', ':'));
    setProperty(Property.REQUEST_HEADERS, reqArgs[1].replace('=', ':'));
    invoke(request);
  }

  /*
   * @testName: pathParamInParamTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.3;
   * 
   * @test_Strategy: Make sure @PathParam works in @BeanParam annotated bean
   */
  @Test
  public void pathParamInParamTest() throws Fault {
    String request = buildRequest(Request.POST, "pathparam/", FIRST, "/",
        SECOND);
    invoke(request);
  }

  /*
   * @testName: pathParamOnFieldTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.3;
   * 
   * @test_Strategy: Make sure @PathParam works in @BeanParam annotated bean
   */
  @Test
  public void pathParamOnFieldTest() throws Fault {
    String request = buildRequest(Request.POST, "pathfield/", FIRST, "/",
        SECOND);
    invoke(request);
  }

  /*
   * @testName: matrixParamInParamTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.1;
   * 
   * @test_Strategy: Make sure @MatrixParam works in @BeanParam annotated bean
   */
  @Test
  public void matrixParamInParamTest() throws Fault {
    String[] reqArgs = getRequestArguments("Matrix");
    String request = buildRequest(Request.POST, "matrixparam;", reqArgs[0], ";",
        reqArgs[1]);
    invoke(request);
  }

  /*
   * @testName: matrixParamOnFieldTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.1;
   * 
   * @test_Strategy: Make sure @MatrixParam works in @BeanParam annotated bean
   */
  @Test
  public void matrixParamOnFieldTest() throws Fault {
    String[] reqArgs = getRequestArguments("Matrix");
    String request = buildRequest(Request.POST, "matrixfield;", reqArgs[0], ";",
        reqArgs[1]);
    invoke(request);
  }

  /*
   * @testName: cookieParamInParamTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.4;
   * 
   * @test_Strategy: Make sure @CookieParam works in @BeanParam annotated bean
   */
  @Test
  public void cookieParamInParamTest() throws Fault {
    String request = buildRequest(Request.POST, "cookieparam");
    buildCookie();
    invoke(request);
  }

  /*
   * @testName: cookieParamOnFieldTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.4;
   * 
   * @test_Strategy: Make sure @CookieParam works in @BeanParam annotated bean
   */
  @Test
  public void cookieParamOnFieldTest() throws Fault {
    String request = buildRequest(Request.POST, "cookiefield");
    buildCookie();
    invoke(request);
  }

  /*
   * @testName: allParamsInParamTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.1; JAXRS:SPEC:3.2; JAXRS:SPEC:3.3;
   * JAXRS:SPEC:3.4; JAXRS:SPEC:3.5; JAXRS:SPEC:12;
   * 
   * @test_Strategy: Make sure all params works in @BeanParam annotated bean
   */
  @Test
  public void allParamsInParamTest() throws Fault {
    allParamsTest("allparam");
  }

  /*
   * @testName: allParamsOnFieldTest
   * 
   * @assertion_ids: JAXRS:SPEC:3.1; JAXRS:SPEC:3.2; JAXRS:SPEC:3.3;
   * JAXRS:SPEC:3.4; JAXRS:SPEC:3.5; JAXRS:SPEC:12;
   * 
   * @test_Strategy: Make sure all params works in @BeanParam annotated bean
   */
  @Test
  public void allParamsOnFieldTest() throws Fault {
    allParamsTest("allfield");
  }

  private void allParamsTest(String path) throws Fault {
    String[] formArgs = getRequestArguments("Form", THIRD, FOURTH);
    String[] headerArgs = getRequestArguments("Header", FIFTH, SIXTH);
    String[] matrixArgs = getRequestArguments("Matrix", SEVENTH, EIGHTH);
    String[] queryArgs = getRequestArguments("Query", ELEVENTH, TWELVENTH);
    String request = buildRequest(Request.POST, path, "/", NINETH, "/", TENTH,
        ";", matrixArgs[0], ";", matrixArgs[1], "?",
        andize(queryArgs[0], queryArgs[1]));

    buildCookie();
    setProperty(Property.REQUEST_HEADERS, headerArgs[0].replace('=', ':'));
    setProperty(Property.REQUEST_HEADERS, headerArgs[1].replace('=', ':'));

    setProperty(Property.SEARCH_STRING, THIRD);
    setProperty(Property.SEARCH_STRING, FOURTH);
    setProperty(Property.SEARCH_STRING, FIFTH);
    setProperty(Property.SEARCH_STRING, SIXTH);
    setProperty(Property.SEARCH_STRING, SEVENTH);
    setProperty(Property.SEARCH_STRING, EIGHTH);
    setProperty(Property.SEARCH_STRING, NINETH);
    setProperty(Property.SEARCH_STRING, TENTH);
    setProperty(Property.SEARCH_STRING, ELEVENTH);
    setProperty(Property.SEARCH_STRING, TWELVENTH);
    invoke(request, andize(CONTENT, formArgs[0], formArgs[1]));
  }

  // ///////////////////////////////////////////////////////////////////////
  private static String[] getRequestArguments(String type) {
    return getRequestArguments(type, FIRST, SECOND);
  }

  private static String[] getRequestArguments(String type, String first,
      String second) {
    String[] args = new String[2];
    args[0] = new StringBuilder().append("bpe").append(type).append("=")
        .append(first).toString();
    args[1] = new StringBuilder().append("inner").append(type).append("=")
        .append(second).toString();
    return args;
  }

  private void invoke(String request, String content) throws Fault {
    setProperty(Property.REQUEST, request);
    setProperty(Property.REQUEST_HEADERS,
        buildContentType(MediaType.APPLICATION_FORM_URLENCODED_TYPE));
    setProperty(Property.CONTENT, content);
    setProperty(Property.SEARCH_STRING, content);
    setProperty(Property.SEARCH_STRING, FIRST);
    setProperty(Property.SEARCH_STRING, SECOND);
    invoke();
  }

  private void invoke(String request) throws Fault {
    invoke(request, CONTENT);
  }

  private String buildRequestQuery(Request type, String path, String... query) {
    StringBuilder sb = new StringBuilder();
    sb.append(buildRequest(type, path));
    sb.append("?").append(andize(query));
    return sb.toString();
  }

  private static String andize(String... args) {
    StringBuilder sb = new StringBuilder();
    if (args != null)
      for (int i = 0; i != args.length; i++) {
        if (i != 0)
          sb.append("&");
        sb.append(args[i]);
      }
    return sb.toString();
  }

  private void buildCookie() {
    String[] reqArgs = getRequestArguments("Cookie");
    StringBuilder sb = new StringBuilder().append("Cookie:").append(reqArgs[0])
        .append(";Version=1;").append(reqArgs[1]).append(";Version=1");

    setProperty(Property.REQUEST_HEADERS, sb.toString());
  }
};
