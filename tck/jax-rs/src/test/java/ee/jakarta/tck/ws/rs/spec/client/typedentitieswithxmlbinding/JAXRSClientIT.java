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

package ee.jakarta.tck.ws.rs.spec.client.typedentitieswithxmlbinding;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.io.IOException;

import ee.jakarta.tck.ws.rs.ee.rs.ext.messagebodyreaderwriter.ReadableWritableEntity;
import ee.jakarta.tck.ws.rs.common.client.JaxrsCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
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

  private static final String entity = Resource.class.getName();

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_spec_client_typedentitieswithxmlbinding_web/resource");
  }

 
  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException{
    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/spec/client/typedentitieswithxmlbinding/web.xml.template");
    String webXml = editWebXmlString(inStream);
    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_spec_client_typedentitieswithxmlbinding_web.war");
    archive.addClasses(TSAppConfig.class, Resource.class, EntityMessageReader.class, EntityMessageWriter.class, ReadableWritableEntity.class);
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
   * @testName: clientJaxbElementReaderTest
   * 
   * @assertion_ids: JAXRS:SPEC:70;
   * 
   * @test_Strategy: See Section 4.2.4 for a list of entity providers that MUST
   * be supported by all JAX-RS implementations
   */
  @Test
  @Tag("xml_binding")
  public void clientJaxbElementReaderTest() throws Fault {
    GenericType<JAXBElement<String>> type = new GenericType<JAXBElement<String>>() {
    };

    standardReaderInvocation(MediaType.TEXT_XML_TYPE);
    JAXBElement<?> responseEntity = getResponse().readEntity(type);
    assertTrue(responseEntity != null, "Returned Entity is null!");

    standardReaderInvocation(MediaType.APPLICATION_XML_TYPE);
    responseEntity = getResponse().readEntity(type);
    assertTrue(responseEntity != null, "Returned Entity is null!");

    standardReaderInvocation(MediaType.APPLICATION_ATOM_XML_TYPE);
    responseEntity = getResponse().readEntity(type);
    assertTrue(responseEntity != null, "Returned Entity is null!");

    String s = responseEntity.getValue().toString();
    assertTrue(s.equals(entity), "Returned Entity"+ s+ "is unexpected");
  }

  /*
   * @testName: clientJaxbElementWriterTest
   * 
   * @assertion_ids: JAXRS:SPEC:70;
   * 
   * @test_Strategy: See Section 4.2.4 for a list of entity providers that MUST
   * be supported by all JAX-RS implementations
   */
  @Test
  @Tag("xml_binding")
  public void clientJaxbElementWriterTest() throws Fault {
    setProperty(Property.REQUEST_HEADERS,
        buildContentType(MediaType.APPLICATION_XML_TYPE));
    JAXBElement<String> element = new JAXBElement<String>(new QName(""),
        String.class, entity);
    standardWriterInvocation(element);
  }


  // ///////////////////////////////////////////////////////////////////////
  // Helper methods

  protected void standardReaderInvocation(MediaType mediaType) throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.GET, "standardreader"));
    setProperty(Property.SEARCH_STRING, entity);
    setProperty(Property.REQUEST_HEADERS, buildAccept(mediaType));
    bufferEntity(true);
    invoke();
  }

  protected void standardWriterInvocation(Object objectEntity) throws Fault {
    setProperty(Property.REQUEST, buildRequest(Request.POST, "standardwriter"));
    setRequestContentEntity(objectEntity);
    setProperty(Property.SEARCH_STRING, entity);
    invoke();
  }


  protected <T> void toStringTest(Class<T> clazz) throws Fault {
    T responseEntity = getResponse().readEntity(clazz);
    assertTrue(responseEntity != null, "Returned Entity is null!");
    String s = responseEntity.toString();
    if (s.startsWith("[B"))
      s = new String((byte[]) responseEntity);
      assertTrue(s.equals(entity), "Was expected returned entity"+ entity+ "got"+
        s);
  }
}
