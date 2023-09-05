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

package ee.jakarta.tck.ws.rs.spec.resource.valueofandfromstring;

import java.io.InputStream;
import java.io.IOException;

import ee.jakarta.tck.ws.rs.common.client.JaxrsCommonClient;
import ee.jakarta.tck.ws.rs.ee.rs.ParamEntityPrototype;
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

  private static final String DATA = "ASDFGHJKLQWERTYUIOPPPPPPP";

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_spec_resource_valueofandfromstring_web");
  }

 
  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException{
    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/spec/resource/valueofandfromstring/web.xml.template");
    String webXml = editWebXmlString(inStream);
    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_spec_resource_valueofandfromstring_web.war");
    archive.addClasses(TSAppConfig.class, Resource.class, EnumWithFromStringAndValueOf.class, ParamEntityWithFromStringAndValueOf.class, ParamEntityPrototype.class);
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
   * @testName: enumHeaderTest
   * 
   * @assertion_ids: JAXRS:SPEC:5; JAXRS:SPEC:5.5;
   * 
   * @test_Strategy: If both methods are present then valueOf MUST be used
   * unless the type is an enum in which case fromString MUST be used.
   */
  @Test
  public void enumHeaderTest() throws Fault {
    setProperty(Property.REQUEST_HEADERS, "param:" + DATA);
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "resource/enumheader"));
    setProperty(Property.SEARCH_STRING,
        EnumWithFromStringAndValueOf.FROMSTRING.name());
    invoke();
  }

  /*
   * @testName: enumCookieTest
   * 
   * @assertion_ids: JAXRS:SPEC:5; JAXRS:SPEC:5.5;
   * 
   * @test_Strategy: If both methods are present then valueOf MUST be used
   * unless the type is an enum in which case fromString MUST be used.
   */
  @Test
  public void enumCookieTest() throws Fault {
    setProperty(Property.REQUEST_HEADERS, "Cookie: param=" + DATA);
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "resource/enumcookie"));
    setProperty(Property.SEARCH_STRING,
        EnumWithFromStringAndValueOf.FROMSTRING.name());
    invoke();
  }

  /*
   * @testName: enumMaxtrixTest
   * 
   * @assertion_ids: JAXRS:SPEC:5; JAXRS:SPEC:5.5;
   * 
   * @test_Strategy: If both methods are present then valueOf MUST be used
   * unless the type is an enum in which case fromString MUST be used.
   */
  @Test
  public void enumMaxtrixTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "resource/enummatrix;param=" + DATA));
    setProperty(Property.SEARCH_STRING,
        EnumWithFromStringAndValueOf.FROMSTRING.name());
    invoke();
  }

  /*
   * @testName: enumQueryTest
   * 
   * @assertion_ids: JAXRS:SPEC:5; JAXRS:SPEC:5.5;
   * 
   * @test_Strategy: If both methods are present then valueOf MUST be used
   * unless the type is an enum in which case fromString MUST be used.
   */
  @Test
  public void enumQueryTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "resource/enumquery?param=" + DATA));
    setProperty(Property.SEARCH_STRING,
        EnumWithFromStringAndValueOf.FROMSTRING.name());
    invoke();
  }

  /*
   * @testName: enumPathTest
   * 
   * @assertion_ids: JAXRS:SPEC:5; JAXRS:SPEC:5.5;
   * 
   * @test_Strategy: If both methods are present then valueOf MUST be used
   * unless the type is an enum in which case fromString MUST be used.
   */
  @Test
  public void enumPathTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "resource/enumpath/" + DATA));
    setProperty(Property.SEARCH_STRING,
        EnumWithFromStringAndValueOf.FROMSTRING.name());
    invoke();
  }

  /*
   * @testName: entityHeaderTest
   * 
   * @assertion_ids: JAXRS:SPEC:5; JAXRS:SPEC:5.5;
   * 
   * @test_Strategy: If both methods are present then valueOf MUST be used
   * unless the type is an entity in which case fromString MUST be used.
   */
  @Test
  public void entityHeaderTest() throws Fault {
    setProperty(Property.REQUEST_HEADERS, "param:" + DATA);
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "resource/entityheader"));
    setProperty(Property.SEARCH_STRING,
        EnumWithFromStringAndValueOf.VALUEOF.name());
    invoke();
  }

  /*
   * @testName: entityCookieTest
   * 
   * @assertion_ids: JAXRS:SPEC:5; JAXRS:SPEC:5.5;
   * 
   * @test_Strategy: If both methods are present then valueOf MUST be used
   * unless the type is an entity in which case fromString MUST be used.
   */
  @Test
  public void entityCookieTest() throws Fault {
    setProperty(Property.REQUEST_HEADERS, "Cookie: param=" + DATA);
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "resource/entitycookie"));
    setProperty(Property.SEARCH_STRING,
        EnumWithFromStringAndValueOf.VALUEOF.name());
    invoke();
  }

  /*
   * @testName: entityMaxtrixTest
   * 
   * @assertion_ids: JAXRS:SPEC:5; JAXRS:SPEC:5.5;
   * 
   * @test_Strategy: If both methods are present then valueOf MUST be used
   * unless the type is an entity in which case fromString MUST be used.
   */
  @Test
  public void entityMaxtrixTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "resource/entitymatrix;param=" + DATA));
    setProperty(Property.SEARCH_STRING,
        EnumWithFromStringAndValueOf.VALUEOF.name());
    invoke();
  }

  /*
   * @testName: entityQueryTest
   * 
   * @assertion_ids: JAXRS:SPEC:5; JAXRS:SPEC:5.5;
   * 
   * @test_Strategy: If both methods are present then valueOf MUST be used
   * unless the type is an entity in which case fromString MUST be used.
   */
  @Test
  public void entityQueryTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "resource/entityquery?param=" + DATA));
    setProperty(Property.SEARCH_STRING,
        EnumWithFromStringAndValueOf.VALUEOF.name());
    invoke();
  }

  /*
   * @testName: entityPathTest
   * 
   * @assertion_ids: JAXRS:SPEC:5; JAXRS:SPEC:5.5;
   * 
   * @test_Strategy: If both methods are present then valueOf MUST be used
   * unless the type is an entity in which case fromString MUST be used.
   */
  @Test
  public void entityPathTest() throws Fault {
    setProperty(Property.REQUEST,
        buildRequest(Request.GET, "resource/entitypath/" + DATA));
    setProperty(Property.SEARCH_STRING,
        EnumWithFromStringAndValueOf.VALUEOF.name());
    invoke();
  }
}
