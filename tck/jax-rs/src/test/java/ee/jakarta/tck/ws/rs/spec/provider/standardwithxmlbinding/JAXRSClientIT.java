/*
 * Copyright (c) 2020, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.spec.provider.standardwithxmlbinding;

import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;
import java.io.InputStream;
import java.io.IOException;

import jakarta.ws.rs.core.MediaType;

import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
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
    setContextRoot("/jaxrs_spec_provider_standardwithxmlbinding_web/resource");
  }

  private void setPropertyAndInvoke(String resourceMethod, MediaType md)
      throws Fault {
    String ct = buildContentType(md);
    setProperty(Property.REQUEST, buildRequest(Request.POST, resourceMethod));
    if (!MediaType.WILDCARD_TYPE.equals(md))
      setProperty(Property.EXPECTED_HEADERS, ct);
    setProperty(Property.REQUEST_HEADERS, ct);
    setProperty(Property.REQUEST_HEADERS, buildAccept(md));
    invoke();
  }

  private void setPropertyAndInvokeXml(String resourceMethod, MediaType md)
      throws Fault {
    setProperty(Property.CONTENT, "<tag>" + resourceMethod + "</tag>");
    setProperty(Property.SEARCH_STRING, resourceMethod);
    setPropertyAndInvoke(resourceMethod, md);
  }

  private void setPropertyAndInvokeXml(String method) throws Fault {
    setPropertyAndInvokeXml(method, MediaType.TEXT_XML_TYPE);
    setPropertyAndInvokeXml(method, MediaType.APPLICATION_XML_TYPE);
    setPropertyAndInvokeXml(method, MediaType.APPLICATION_ATOM_XML_TYPE);
    setPropertyAndInvokeXml(method, MediaType.APPLICATION_SVG_XML_TYPE);
    setPropertyAndInvokeXml(method, new MediaType("application", "*+xml"));
  }

  protected String getAbsoluteUrl() {
    StringBuilder sb = new StringBuilder();
    sb.append("http://").append(_hostname).append(":").append(_port)
        .append(getContextRoot());
    return sb.toString();
  }

 
  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException{
    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/spec/provider/standardwithxmlbinding/web.xml.template");
    String webXml = editWebXmlString(inStream);
    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_spec_provider_standardwithxmlbinding_web.war");
    archive.addClasses(TSAppConfig.class, Resource.class);
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
   * @testName: jaxbElementProviderTest
   * 
   * @assertion_ids: JAXRS:SPEC:33; JAXRS:SPEC:33.8;
   * 
   * @test_Strategy: An implementation MUST include pre-packaged
   * MessageBodyReader and MessageBodyWriter implementations for the following
   * Java and media type combinations
   * 
   * jakarta.xml.bind.JAXBElement and application-supplied JAXB classes XML media
   * types (text/xml, application/xml and application/*+xml)
   */
  @Test
  @Tag("xml_binding")
  public void jaxbElementProviderTest() throws Fault {
    setPropertyAndInvokeXml("jaxb");
  }

}
