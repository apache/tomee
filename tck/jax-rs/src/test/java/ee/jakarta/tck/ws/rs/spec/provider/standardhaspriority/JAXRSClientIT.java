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

package ee.jakarta.tck.ws.rs.spec.provider.standardhaspriority;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.io.IOException;

import ee.jakarta.tck.ws.rs.common.client.JaxrsCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.xml.bind.JAXBElement;

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
public class JAXRSClientIT extends JaxrsCommonClient {

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_spec_provider_standardhaspriority_web/resource");
  }

  private void setPropertyAndInvoke(String resourceMethod, MediaType md)
      throws Fault {
    String ct = buildContentType(md);
    setProperty(Property.REQUEST, buildRequest(Request.POST, resourceMethod));
    setProperty(Property.REQUEST_HEADERS, ct);
    setProperty(Property.REQUEST_HEADERS, buildAccept(md));
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH,
        "Tck" + resourceMethod + "Reader");
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH,
        "Tck" + resourceMethod + "Writer");
    setProperty(Property.SEARCH_STRING, resourceMethod);
    invoke();
  }

 
  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException{
    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/spec/provider/standardhaspriority/web.xml.template");
    String webXml = editWebXmlString(inStream);
    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_spec_provider_standardhaspriority_web.war");
    archive.addClasses(TSAppConfig.class, Resource.class, AbstractProvider.class, ProviderWalker.class, TckBooleanProvider.class, TckCharacterProvider.class, TckJaxbProvider.class, TckMapProvider.class, TckNumberProvider.class, TckUniversalProvider.class);
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
   * @testName: readWriteJaxbProviderTest
   * 
   * @assertion_ids: JAXRS:SPEC:35
   * 
   * @test_Strategy: An implementation MUST support application-provided entity
   * providers and MUST use those in preference to its own pre-packaged
   * providers when either could handle the same request. More precisely, step 4
   * in Section 4.2.1 and step 5 in Section 4.2.2 MUST prefer
   * application-provided over pre-packaged entity providers. i.e. When have the
   * same mediaType
   */
  @Test
  @Tag("xml_binding")
  public void readWriteJaxbProviderTest() throws Fault {
    JAXBElement<String> element = new JAXBElement<String>(new QName("jaxb"),
        String.class, "jaxb");
    setRequestContentEntity(element);
    setPropertyAndInvoke("jaxb", MediaType.APPLICATION_XML_TYPE);
  }

  /*
   * @testName: readWriteMapProviderTest
   * 
   * @assertion_ids: JAXRS:SPEC:35
   * 
   * @test_Strategy: An implementation MUST support application-provided entity
   * providers and MUST use those in preference to its own pre-packaged
   * providers when either could handle the same request. More precisely, step 4
   * in Section 4.2.1 and step 5 in Section 4.2.2 MUST prefer
   * application-provided over pre-packaged entity providers. i.e. When have the
   * same mediaType
   */
  @Test
  @Tag("xml_binding")
  public void readWriteMapProviderTest() throws Fault {
    MultivaluedMap<String, String> map = new MultivaluedHashMap<String, String>();
    map.add("map", "map");
    setRequestContentEntity(map);
    setPropertyAndInvoke("map", MediaType.APPLICATION_FORM_URLENCODED_TYPE);
  }

  /*
   * @testName: readWriteBooleanProviderTest
   * 
   * @assertion_ids: JAXRS:SPEC:35
   * 
   * @test_Strategy: An implementation MUST support application-provided entity
   * providers and MUST use those in preference to its own pre-packaged
   * providers when either could handle the same request. More precisely, step 4
   * in Section 4.2.1 and step 5 in Section 4.2.2 MUST prefer
   * application-provided over pre-packaged entity providers. i.e. When have the
   * same mediaType
   */
  @Test
  @Tag("xml_binding")
  public void readWriteBooleanProviderTest() throws Fault {
    MediaType mt = MediaType.TEXT_PLAIN_TYPE;
    setProperty(Property.REQUEST, buildRequest(Request.POST, "boolean"));
    setProperty(Property.REQUEST_HEADERS, buildContentType(mt));
    setProperty(Property.REQUEST_HEADERS, buildAccept(mt));
    setProperty(Property.CONTENT, "false");
    setProperty(Property.SEARCH_STRING, "false");
    invoke();
  }

  /*
   * @testName: readWriteCharacterProviderTest
   * 
   * @assertion_ids: JAXRS:SPEC:35
   * 
   * @test_Strategy: An implementation MUST support application-provided entity
   * providers and MUST use those in preference to its own pre-packaged
   * providers when either could handle the same request. More precisely, step 4
   * in Section 4.2.1 and step 5 in Section 4.2.2 MUST prefer
   * application-provided over pre-packaged entity providers. i.e. When have the
   * same mediaType
   */
  @Test
  @Tag("xml_binding")
  public void readWriteCharacterProviderTest() throws Fault {
    MediaType mt = MediaType.TEXT_PLAIN_TYPE;
    setProperty(Property.REQUEST, buildRequest(Request.POST, "character"));
    setProperty(Property.REQUEST_HEADERS, buildContentType(mt));
    setProperty(Property.REQUEST_HEADERS, buildAccept(mt));
    setProperty(Property.CONTENT, "a");
    setProperty(Property.SEARCH_STRING, "a");
    invoke();
  }

  /*
   * @testName: readWriteIntegerProviderTest
   * 
   * @assertion_ids: JAXRS:SPEC:35
   * 
   * @test_Strategy: An implementation MUST support application-provided entity
   * providers and MUST use those in preference to its own pre-packaged
   * providers when either could handle the same request. More precisely, step 4
   * in Section 4.2.1 and step 5 in Section 4.2.2 MUST prefer
   * application-provided over pre-packaged entity providers. i.e. When have the
   * same mediaType
   */
  @Test
  @Tag("xml_binding")
  public void readWriteIntegerProviderTest() throws Fault {
    MediaType mt = MediaType.TEXT_PLAIN_TYPE;
    setProperty(Property.REQUEST, buildRequest(Request.POST, "number"));
    setProperty(Property.REQUEST_HEADERS, buildContentType(mt));
    setProperty(Property.REQUEST_HEADERS, buildAccept(mt));
    setProperty(Property.CONTENT, "0");
    setProperty(Property.SEARCH_STRING, "0");
    invoke();
  }

}
