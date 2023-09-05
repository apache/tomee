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

package ee.jakarta.tck.ws.rs.spec.provider.visibility;

import java.io.InputStream;
import java.io.IOException;

import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

import jakarta.ws.rs.core.MediaType;

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
    setContextRoot("/jaxrs_spec_provider_visibility_web/resource");
  }

 
  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException{
    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/spec/provider/visibility/web.xml.template");
    String webXml = editWebXmlString(inStream);
    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_spec_provider_visibility_web.war");
    archive.addClasses(TSAppConfig.class, Resource.class, DummyClass.class, DummyWriter.class, HolderClass.class, HolderResolver.class, StringReader.class, VisibilityException.class, VisibilityExceptionMapper.class);
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
   * @testName: contextResolverTest
   * 
   * @assertion_ids: JAXRS:SPEC:27; JAXRS:SPEC:28;
   * 
   * @test_Strategy: Provider classes are instantiated by the JAX-RS runtime and
   * MUST have a public constructor for which the JAX-RS runtime can provide all
   * parameter values.
   * 
   * If more than one public constructor can be used then an implementation MUST
   * use the one with the most parameters
   */
  @Test
  public void contextResolverTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "contextresolver"));
    setProperty(Property.SEARCH_STRING, HolderClass.OK);
    invoke();
  }

  /*
   * @testName: exceptionMapperTest
   * 
   * @assertion_ids: JAXRS:SPEC:27; JAXRS:SPEC:28;
   * 
   * @test_Strategy: Provider classes are instantiated by the JAX-RS runtime and
   * MUST have a public constructor for which the JAX-RS runtime can provide all
   * parameter values.
   * 
   * If more than one public constructor can be used then an implementation MUST
   * use the one with the most parameters
   */
  @Test
  public void exceptionMapperTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "exceptionmapper"));
    setProperty(Property.SEARCH_STRING, HolderClass.OK);
    invoke();
  }

  /*
   * @testName: bodyWriterTest
   * 
   * @assertion_ids: JAXRS:SPEC:27; JAXRS:SPEC:28;
   * 
   * @test_Strategy: Provider classes are instantiated by the JAX-RS runtime and
   * MUST have a public constructor for which the JAX-RS runtime can provide all
   * parameter values.
   * 
   * If more than one public constructor can be used then an implementation MUST
   * use the one with the most parameters
   */
  @Test
  public void bodyWriterTest() throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "bodywriter"));
    setProperty(Property.SEARCH_STRING, HolderClass.OK);
    invoke();
  }

  /*
   * @testName: bodyReaderTest
   * 
   * @assertion_ids: JAXRS:SPEC:27; JAXRS:SPEC:28;
   * 
   * @test_Strategy: Provider classes are instantiated by the JAX-RS runtime and
   * MUST have a public constructor for which the JAX-RS runtime can provide all
   * parameter values.
   * 
   * If more than one public constructor can be used then an implementation MUST
   * use the one with the most parameters
   */
  @Test
  public void bodyReaderTest() throws Fault {
    MediaType type = new MediaType("text", "tck");
    setProperty(Property.REQUEST, buildRequest(Request.POST, "bodyreader"));
    setProperty(Property.CONTENT, "text");
    setProperty(Property.REQUEST_HEADERS, buildContentType(type));
    setProperty(Property.SEARCH_STRING, HolderClass.OK);
    invoke();
  }

}
