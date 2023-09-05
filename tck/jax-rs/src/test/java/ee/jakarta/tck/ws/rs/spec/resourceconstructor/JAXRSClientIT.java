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

package ee.jakarta.tck.ws.rs.spec.resourceconstructor;

import java.io.InputStream;
import java.io.IOException;

import ee.jakarta.tck.ws.rs.common.client.JaxrsCommonClient;
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
public class JAXRSClientIT extends JaxrsCommonClient {

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_spec_resourceconstructor_web/resource");
  }

 
  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException{
    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/spec/resourceconstructor/web.xml.template");
    String webXml = editWebXmlString(inStream);
    inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/spec/resourceconstructor/beans.xml");
    String beansXml = toString(inStream);
    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_spec_resourceconstructor_web.war");
    archive.addClasses(TSAppConfig.class, Resource.class, CookieResource.class, HeaderResource.class, MatrixResource.class, PathResource.class, QueryResource.class);
    archive.addAsWebInfResource(new StringAsset(beansXml), "beans.xml");
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
   * @testName: visibleTest
   * 
   * @assertion_ids: JAXRS:SPEC:1; JAXRS:SPEC:2; JAXRS:SPEC:77; JAXRS:SPEC:77.1;
   * 
   * @test_Strategy: verify the PUBLIC constructor of a resource with most
   * attributes is used
   * 
   * A public constructor MAY include parameters annotated with one of the
   * following: @Context
   */
  @Test
  public void visibleTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "mostAttributes"));
    invoke();
  }

  /*
   * @testName: packageVisibilityTest
   * 
   * @assertion_ids: JAXRS:SPEC:11;
   * 
   * @test_Strategy: Only public methods may be exposed as resource methods.
   */
  @Test
  public void packageVisibilityTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "packageVisibility"));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.NOT_FOUND));
    invoke();
  }

  /*
   * @testName: protectedVisibilityTest
   * 
   * @assertion_ids: JAXRS:SPEC:11;
   * 
   * @test_Strategy: Only public methods may be exposed as resource methods.
   */
  @Test
  public void protectedVisibilityTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "protectedVisibility"));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.NOT_FOUND));
    invoke();
  }

  /*
   * @testName: privateVisibilityTest
   * 
   * @assertion_ids: JAXRS:SPEC:11;
   * 
   * @test_Strategy: Only public methods may be exposed as resource methods.
   */
  @Test
  public void privateVisibilityTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "privateVisibility"));
    setProperty(Property.STATUS_CODE, getStatusCode(Status.NOT_FOUND));
    invoke();
  }

  /*
   * @testName: constructorWithHeaderParamUsedTest
   * 
   * @assertion_ids: JAXRS:SPEC:77; JAXRS:SPEC:77.2;
   * 
   * @test_Strategy: A public constructor MAY include parameters annotated with
   * one of the following: @HeaderParam
   */
  @Test
  public void constructorWithHeaderParamUsedTest() throws Fault {
    String param = "ABCDEFGH";
    setProperty(Property.REQUEST_HEADERS, "param:" + param);
    setProperty(Property.REQUEST, buildRequest(Request.GET, "header"));
    setProperty(Property.SEARCH_STRING, param);
    invoke();
  }

  /*
   * @testName: constructorWithCookieParamUsedTest
   * 
   * @assertion_ids: JAXRS:SPEC:77; JAXRS:SPEC:77.3;
   * 
   * @test_Strategy: A public constructor MAY include parameters annotated with
   * one of the following: @HeaderParam
   */
  @Test
  public void constructorWithCookieParamUsedTest() throws Fault {
    String param = "ABCDEFGH";
    setProperty(Property.REQUEST_HEADERS, "Cookie: param=" + param);
    setProperty(Property.REQUEST, buildRequest(Request.GET, "cookie"));
    setProperty(Property.SEARCH_STRING, param);
    invoke();
  }

  /*
   * @testName: constructorWithMatrixParamUsedTest
   * 
   * @assertion_ids: JAXRS:SPEC:77; JAXRS:SPEC:77.4;
   * 
   * @test_Strategy: A public constructor MAY include parameters annotated with
   * one of the following: @MatrixParam
   */
  @Test
  public void constructorWithMatrixParamUsedTest() throws Fault {
    String param = "ABCDEFGH";
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "matrix;param=" + param));
    setProperty(Property.SEARCH_STRING, param);
    invoke();
  }

  /*
   * @testName: constructorWithQueryParamUsedTest
   * 
   * @assertion_ids: JAXRS:SPEC:77; JAXRS:SPEC:77.5;
   * 
   * @test_Strategy: A public constructor MAY include parameters annotated with
   * one of the following: @QueryParam
   */
  @Test
  public void constructorWithQueryParamUsedTest() throws Fault {
    String param = "ABCDEFGH";
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "query?param=" + param));
    setProperty(Property.SEARCH_STRING, param);
    invoke();
  }

  /*
   * @testName: constructorWithPathParamUsedTest
   * 
   * @assertion_ids: JAXRS:SPEC:77; JAXRS:SPEC:77.6;
   * 
   * @test_Strategy: A public constructor MAY include parameters annotated with
   * one of the following: @PathParam
   */
  @Test
  public void constructorWithPathParamUsedTest() throws Fault {
    String param = "ABCDEFGH";
    setProperty(Property.REQUEST, buildRequest(Request.GET, "path/" + param));
    setProperty(Property.SEARCH_STRING, param);
    invoke();
  }
}
