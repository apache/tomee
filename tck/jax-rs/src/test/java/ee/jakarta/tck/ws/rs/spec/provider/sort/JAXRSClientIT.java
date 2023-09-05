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

package ee.jakarta.tck.ws.rs.spec.provider.sort;

import java.io.InputStream;
import java.io.IOException;

import ee.jakarta.tck.ws.rs.common.provider.StringBean;
import ee.jakarta.tck.ws.rs.common.provider.StringBeanEntityProvider;
import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;
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
    setContextRoot("/jaxrs_spec_provider_sort_web/resource");
  }

 
  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException{
    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/spec/provider/sort/web.xml.template");
    String webXml = editWebXmlString(inStream);
    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_spec_provider_sort_web.war");
    archive.addClasses(TSAppConfig.class, Resource.class, TextPlainStringBeanEntityProvider.class, TextWildCardStringBeanEntityProvider.class, StringBeanEntityProvider.class, StringBean.class, JaxrsUtil.class);
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


  /*
   * @testName: contentTypeApplicationGotWildCardTest
   * 
   * @assertion_ids: JAXRS:SPEC:30;
   * 
   * @test_Strategy: The absence of these annotations is equivalent to their
   * inclusion with media type (
   *//*
   * ), i.e. absence implies that any media type is supported.
   *
   * Unexpected providers add "text/" to content
   */
  @Test
  public void contentTypeApplicationGotWildCardTest() throws Fault {
    MediaType type = new MediaType("application", "plain");
    setProperty(Property.REQUEST, buildRequest(Request.POST, ""));
    setProperty(Property.REQUEST_HEADERS, buildContentType(type));
    setProperty(Property.REQUEST_HEADERS, buildAccept(type));
    setProperty(Property.CONTENT, "test");
    setProperty(Property.SEARCH_STRING, "test");
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH, "text");
    invoke();
  }

  /*
   * @testName: contentTypeTextHmtlGotTextWildCardTest
   * 
   * @assertion_ids: JAXRS:SPEC:32;
   * 
   * @test_Strategy: When choosing an entity provider an implementation sorts
   * the available providers according to the media types they declare support
   * for. Sorting of media types follows the general rule: x/y < x/* <
   *//*
   * , i.e. a provider that explicitly lists a media types is sorted before a
   * provider that lists
   *//*
   * .
   */
  @Test
  public void contentTypeTextHmtlGotTextWildCardTest() throws Fault {
    MediaType type = MediaType.TEXT_HTML_TYPE;
    setProperty(Property.REQUEST, buildRequest(Request.POST, ""));
    setProperty(Property.REQUEST_HEADERS, buildContentType(type));
    setProperty(Property.REQUEST_HEADERS, buildAccept(type));
    setProperty(Property.CONTENT, "test");
    setProperty(Property.SEARCH_STRING, "test");
    setProperty(Property.SEARCH_STRING, "text/*");
    invoke();
  }

  /*
   * @testName: contentTypeTextXmlGotTextWildCardTest
   * 
   * @assertion_ids: JAXRS:SPEC:32; JAXRS:SPEC:38;
   * 
   * @test_Strategy: When choosing an entity provider an implementation sorts
   * the available providers according to the media types they declare support
   * for. Sorting of media types follows the general rule: x/y < x/* <
   *//*
   * , i.e. a provider that explicitly lists a media types is sorted before a
   * provider that lists
   *//*
   * .
   */
  @Test
  public void contentTypeTextXmlGotTextWildCardTest() throws Fault {
    MediaType type = MediaType.TEXT_XML_TYPE;
    setProperty(Property.REQUEST, buildRequest(Request.POST, ""));
    setProperty(Property.REQUEST_HEADERS, buildContentType(type));
    setProperty(Property.REQUEST_HEADERS, buildAccept(type));
    setProperty(Property.CONTENT, "test");
    setProperty(Property.SEARCH_STRING, "test");
    setProperty(Property.SEARCH_STRING, "text/*");
    invoke();
  }

  /*
   * @testName: contentTypeTextPlainGotTextPlainTest
   * 
   * @assertion_ids: JAXRS:SPEC:32; JAXRS:SPEC:38;
   * 
   * @test_Strategy: When choosing an entity provider an implementation sorts
   * the available providers according to the media types they declare support
   * for. Sorting of media types follows the general rule: x/y < x/* <
   *//*
   * , i.e. a provider that explicitly lists a media types is sorted before a
   * provider that lists
   *//*
   * .
   */
  @Test
  public void contentTypeTextPlainGotTextPlainTest() throws Fault {
    MediaType type = MediaType.TEXT_PLAIN_TYPE;
    setProperty(Property.REQUEST, buildRequest(Request.POST, ""));
    setProperty(Property.REQUEST_HEADERS, buildContentType(type));
    setProperty(Property.REQUEST_HEADERS, buildAccept(type));
    setProperty(Property.CONTENT, "test");
    setProperty(Property.SEARCH_STRING, "test");
    setProperty(Property.SEARCH_STRING, "text/plain");
    invoke();
  }
}
