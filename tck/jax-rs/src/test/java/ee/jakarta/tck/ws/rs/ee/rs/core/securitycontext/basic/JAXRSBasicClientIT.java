/*
 * Copyright (c) 2012, 2022 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.rs.core.securitycontext.basic;

import java.io.InputStream;
import java.io.IOException;

import ee.jakarta.tck.ws.rs.ee.rs.core.securitycontext.JAXRSClient;
import ee.jakarta.tck.ws.rs.ee.rs.core.securitycontext.TSAppConfig;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;
import ee.jakarta.tck.ws.rs.ee.rs.core.securitycontext.TestServlet;
import ee.jakarta.tck.ws.rs.ee.rs.core.securitycontext.TestServlet.Scheme;

import jakarta.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.AfterEach;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     user;
 *                     password;
 *                     authuser;
 *                     authpassword;
 */
@ExtendWith(ArquillianExtension.class)
public class JAXRSBasicClientIT
    extends JAXRSClient {

  private static final long serialVersionUID = 340277879725875946L;

  public JAXRSBasicClientIT() {
    setup();
    setContextRoot("/jaxrs_ee_core_securitycontext_basic_web/Servlet");
  }

  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : "+testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : "+testInfo.getDisplayName());
  }

  @Deployment(testable = true)
  public static WebArchive createDeployment() throws IOException {

    InputStream inStream = JAXRSBasicClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/core/securitycontext/basic/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_core_securitycontext_basic_web.war");
    archive.addClasses(TSAppConfig.class,
      TestServlet.class,
      TestServlet.Security.class,
      TestServlet.Scheme.class,
      TestServlet.Role.class);

//  This TCK test needs additional information about roles and principals (DIRECTOR:j2ee, OTHERROLE:javajoe).
//  In GlassFish, the following sun-web.xml descriptor can be added:
//  archive.addAsWebInfResource("ee/jakarta/tck/ws/rs/ee/rs/core/securitycontext/basic/jaxrs_ee_core_securitycontext_basic_web.war.sun-web.xml", "sun-web.xml");

//  Vendor implementations are encouraged to utilize Arqullian SPI (LoadableExtension, ApplicationArchiveProcessor)
//  to extend the archive with vendor deployment descriptors as needed.
//  For Jersey in GlassFish, this is demonstrated in the jersey-tck module of the Jakarta RESTful Web Services GitHub repository.

    archive.setWebXML(new StringAsset(webXml));
    return archive;
  }

  /* Run test */

  /*
   * @testName: noAuthorizationTest
   * 
   * @assertion_ids:
   * 
   * @test_Strategy: Send no authorization, make sure of 401 response
   */
  @Test
  @Tag("security")
  @RunAsClient
  public void noAuthorizationTest() throws Fault {
    super.noAuthorizationTest();
  }

  /*
   * @testName: basicAuthorizationAdminTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:169; JAXRS:JAVADOC:170; JAXRS:JAVADOC:171;
   * JAXRS:JAVADOC:172; JAXRS:SPEC:40;
   * 
   * @test_Strategy: Send basic authorization, check security context
   */
  @Test
  @Tag("security")
  @RunAsClient
  public void basicAuthorizationAdminTest() throws Fault {
    setProperty(Property.STATUS_CODE, getStatusCode(Response.Status.OK));
    setProperty(Property.BASIC_AUTH_USER, user);
    setProperty(Property.BASIC_AUTH_PASSWD, password);

    setProperty(Property.SEARCH_STRING, TestServlet.Security.UNSECURED.name());
    setProperty(Property.SEARCH_STRING, TestServlet.Role.DIRECTOR.name());
    setProperty(Property.SEARCH_STRING, user);
    setProperty(Property.SEARCH_STRING, TestServlet.Scheme.BASIC.name());
    invokeRequest();
  }

  /*
   * @testName: basicAuthorizationIncorrectUserTest
   * 
   * @assertion_ids:
   * 
   * @test_Strategy: Send basic authorization, check security context
   */
  @Test
  @Tag("security")
  @RunAsClient
  public void basicAuthorizationIncorrectUserTest() throws Fault {
    setProperty(Property.STATUS_CODE,
        getStatusCode(Response.Status.UNAUTHORIZED));
    setProperty(Property.BASIC_AUTH_USER, Scheme.NOSCHEME.name());
    setProperty(Property.BASIC_AUTH_PASSWD, password);
    invokeRequest();
  }

  /*
   * @testName: basicAuthorizationIncorrectPasswordTest
   * 
   * @assertion_ids:
   * 
   * @test_Strategy: Send basic authorization, check security context
   */
  @Test
  @Tag("security")
  @RunAsClient
  public void basicAuthorizationIncorrectPasswordTest() throws Fault {
    setProperty(Property.STATUS_CODE,
        getStatusCode(Response.Status.UNAUTHORIZED));
    setProperty(Property.BASIC_AUTH_USER, authuser);
    setProperty(Property.BASIC_AUTH_PASSWD, password);
    invokeRequest();
  }

  /*
   * @testName: basicAuthorizationStandardUserTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:169; JAXRS:JAVADOC:170; JAXRS:JAVADOC:171;
   * JAXRS:JAVADOC:172; JAXRS:SPEC:40;
   * 
   * @test_Strategy: Send basic authorization with made up Realm, check security
   * context
   */
  @Test
  @Tag("security")
  @RunAsClient
  public void basicAuthorizationStandardUserTest() throws Fault {
    setProperty(Property.STATUS_CODE, getStatusCode(Response.Status.OK));
    setProperty(Property.BASIC_AUTH_USER, authuser);
    setProperty(Property.BASIC_AUTH_PASSWD, authpassword);

    setProperty(Property.SEARCH_STRING, TestServlet.Security.UNSECURED.name());
    setProperty(Property.SEARCH_STRING, TestServlet.Role.OTHERROLE.name());
    setProperty(Property.SEARCH_STRING, authuser);
    setProperty(Property.SEARCH_STRING, TestServlet.Scheme.BASIC.name());
    invokeRequest();
  }
}
