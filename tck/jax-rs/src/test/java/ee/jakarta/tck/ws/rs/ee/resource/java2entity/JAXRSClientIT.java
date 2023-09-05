/*
 * Copyright (c) 2012, 2020, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.resource.java2entity;

import java.io.IOException;
import java.io.InputStream;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.ws.rs.core.Response;
import ee.jakarta.tck.ws.rs.common.AbstractMessageBodyRW;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
@ExtendWith(ArquillianExtension.class)
public class JAXRSClientIT extends JAXRSCommonClient {

  private static final long serialVersionUID = 1L;

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_resource_java2entity_web/resource");
  }

  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException {

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/resource/java2entity/web.xml.template");
    // Replace the servlet_adaptor in web.xml.template with the System variable set as servlet adaptor
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_resource_java2entity_web.war");
    archive.addClasses(TSAppConfig.class, AbstractMessageBodyRW.class, Resource.class, CollectionWriter.class, IncorrectCollectionWriter.class);
    archive.setWebXML(new StringAsset(webXml));
    //archive.addAsWebInfResource(JAXRSClientIT.class.getPackage(), "web.xml.template", "web.xml"); //can use if the web.xml.template doesn't need to be modified.    
    
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

  /*
   * @testName: directClassTypeTest
   * 
   * @assertion_ids: JAXRS:SPEC:15; JAXRS:SPEC:15.1; JAXRS:SPEC:15.2;
   * JAXRS:SPEC:15.3; JAXRS:SPEC:15.4;
   * 
   * @test_Strategy: Other | Return type or subclass | Class of instance |
   * Generic type of return type
   */
  @Test
  public void directClassTypeTest() throws Fault {
    setProperty(REQUEST, buildRequest(GET, "linkedlist"));
    setProperty(SEARCH_STRING, Response.Status.OK.name());
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH,
        IncorrectCollectionWriter.ERROR);
    invoke();
  }

  /*
   * @testName: responseDirectClassTypeTest
   * 
   * @assertion_ids: JAXRS:SPEC:15; JAXRS:SPEC:15.1; JAXRS:SPEC:15.2;
   * JAXRS:SPEC:15.3; JAXRS:SPEC:15.4;
   * 
   * @test_Strategy: Response | Object or subclass | Class of instance | Class
   * of instance
   */
  @Test
  public void responseDirectClassTypeTest() throws Fault {
    setProperty(REQUEST, buildRequest(GET, "response/linkedlist"));
    setProperty(SEARCH_STRING, Response.Status.OK.name());
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH,
        IncorrectCollectionWriter.ERROR);
    invoke();
  }

  /*
   * @testName: responseGenericEntityTest
   * 
   * @assertion_ids: JAXRS:SPEC:15; JAXRS:SPEC:15.1; JAXRS:SPEC:15.2;
   * JAXRS:SPEC:15.3; JAXRS:SPEC:15.4;
   * 
   * @test_Strategy: Response | GenericEntity or subclass | RawType property |
   * Type property
   */
  @Test
  public void responseGenericEntityTest() throws Fault {
    setProperty(REQUEST,
        buildRequest(GET, "response/genericentity/linkedlist"));
    setProperty(SEARCH_STRING, Response.Status.OK.name());
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH,
        IncorrectCollectionWriter.ERROR);
    invoke();
  }

  /*
   * @testName: genericEntityTest
   * 
   * @assertion_ids: JAXRS:SPEC:15; JAXRS:SPEC:15.1; JAXRS:SPEC:15.2;
   * JAXRS:SPEC:15.3; JAXRS:SPEC:15.4;
   * 
   * @test_Strategy: GenericEntity | GenericEntity or subclass | RawType
   * property | Type property
   */
  @Test
  public void genericEntityTest() throws Fault {
    setProperty(REQUEST, buildRequest(GET, "genericentity/linkedlist"));
    setProperty(SEARCH_STRING, Response.Status.OK.name());
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH,
        IncorrectCollectionWriter.ERROR);
    invoke();
  }
}