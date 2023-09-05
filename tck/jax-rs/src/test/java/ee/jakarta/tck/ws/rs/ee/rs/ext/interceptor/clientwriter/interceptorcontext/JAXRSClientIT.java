/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.rs.ext.interceptor.clientwriter.interceptorcontext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.io.InputStream;

import ee.jakarta.tck.ws.rs.api.rs.ext.interceptor.ContextOperation;
import ee.jakarta.tck.ws.rs.api.rs.ext.interceptor.InputStreamReaderProvider;
import ee.jakarta.tck.ws.rs.api.rs.ext.interceptor.TemplateInterceptorBody;
import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;
import ee.jakarta.tck.ws.rs.ee.rs.ext.interceptor.clientwriter.WriterClient;
import ee.jakarta.tck.ws.rs.ee.rs.ext.interceptor.writer.interceptorcontext.WriterInterceptorOne;
import ee.jakarta.tck.ws.rs.ee.rs.ext.interceptor.writer.interceptorcontext.WriterInterceptorTwo;

import jakarta.ws.rs.client.Entity;
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
 */
@ExtendWith(ArquillianExtension.class)
public class JAXRSClientIT extends WriterClient<ContextOperation> {

  private static final long serialVersionUID = -5479399808367387477L;

  public JAXRSClientIT() {
    setup();
    setContextRoot(
        "/jaxrs_ee_rs_ext_interceptor_clientwriter_interceptorcontext_web/resource");
    addProviders();
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

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/ext/interceptor/clientwriter/interceptorcontext/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_rs_ext_interceptor_clientwriter_interceptorcontext_web.war");
    archive.addClasses(TSAppConfig.class, Resource.class);
    archive.setWebXML(new StringAsset(webXml));
    return archive;

  }


  /* Run test */
  /*
   * @testName: getAnnotationsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:903; JAXRS:JAVADOC:930;
   * 
   * @test_Strategy: Get an array of the annotations formally declared on the
   * artifact that initiated the intercepted entity provider invocation.
   *
   * WriterInterceptor.aroundWriteTo
   */
  @Test
  public void getAnnotationsTest() throws Fault {
    Annotation[] annotations = ContextOperation.class.getAnnotations();
    Entity<String> entity = Entity.entity(TemplateInterceptorBody.ENTITY,
        MediaType.WILDCARD_TYPE, annotations);
    setOperationAndEntity(ContextOperation.GETANNOTATIONS);
    setRequestContentEntity(entity);
    for (Annotation a : annotations)
      setProperty(Property.UNORDERED_SEARCH_STRING,
          a.annotationType().getName());
    invoke();
  }

  /*
   * @testName: getGenericTypeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:904; JAXRS:JAVADOC:930;
   * 
   * @test_Strategy: Get an array of the annotations formally declared on the
   * artifact that initiated the intercepted entity provider invocation.
   *
   * WriterInterceptor.aroundWriteTo
   */
  @Test
  public void getGenericTypeTest() throws Fault {
    setOperationAndEntity(ContextOperation.GETGENERICTYPE);
    setProperty(Property.SEARCH_STRING, String.class.getName());
    invoke();
  }

  /*
   * @testName: getMediaTypeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:905; JAXRS:JAVADOC:930;
   * 
   * @test_Strategy: Get media type of HTTP entity.
   *
   * WriterInterceptor.aroundWriteTo
   */
  @Test
  public void getMediaTypeTest() throws Fault {
    Entity<String> entity = Entity.entity(TemplateInterceptorBody.ENTITY,
        MediaType.APPLICATION_JSON_TYPE);
    setOperationAndEntity(ContextOperation.GETMEDIATYPE);
    setRequestContentEntity(entity);
    setProperty(Property.SEARCH_STRING, MediaType.APPLICATION_JSON);
    invoke();
  }

  /*
   * @testName: getPropertyIsNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:906; JAXRS:JAVADOC:930;
   * 
   * @test_Strategy: Returns null if there is no property by that name.
   *
   * WriterInterceptor.aroundWriteTo
   */
  @Test
  public void getPropertyIsNullTest() throws Fault {
    setOperationAndEntity(ContextOperation.GETPROPERTY);
    setProperty(Property.SEARCH_STRING, TemplateInterceptorBody.NULL);
    invoke();
  }

  /*
   * @testName: getPropertyNamesTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1007; JAXRS:JAVADOC:930;
   * 
   * @test_Strategy: Returns an enumeration containing the property names
   * available within the context of the current request/response exchange
   * context.
   *
   * WriterInterceptor.aroundWriteTo
   */
  @Test
  public void getPropertyNamesTest() throws Fault {
    setOperationAndEntity(ContextOperation.GETPROPERTYNAMES);
    for (int i = 0; i != 5; i++)
      setProperty(Property.UNORDERED_SEARCH_STRING,
          TemplateInterceptorBody.PROPERTY + i);
    invoke();
  }

  /*
   * @testName: getPropertyNamesIsReadOnlyTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1007; JAXRS:JAVADOC:930;
   * 
   * @test_Strategy: Returns immutable java.util.Collection containing the
   * property names available within the context of the current request/response
   * exchange context.
   * 
   * WriterInterceptor.aroundWriteTo
   */
  @Test
  public void getPropertyNamesIsReadOnlyTest() throws Fault {
    setOperationAndEntity(ContextOperation.GETPROPERTYNAMESISREADONLY);
    setProperty(Property.UNORDERED_SEARCH_STRING, TemplateInterceptorBody.NULL);
    invoke();
  }

  /*
   * @testName: getTypeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:908; JAXRS:JAVADOC:930;
   * 
   * @test_Strategy: Get Java type supported by corresponding message body
   * provider.
   *
   * WriterInterceptor.aroundWriteTo
   */
  @Test
  public void getTypeTest() throws Fault {
    setOperationAndEntity(ContextOperation.GETTYPE);
    setProperty(Property.SEARCH_STRING, String.class.getName());
    invoke();
  }

  /*
   * @testName: removePropertyTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:909; JAXRS:JAVADOC:930;
   * 
   * @test_Strategy: Removes a property with the given name from the current
   * request/response exchange context. After removal, subsequent calls to
   * getProperty(java.lang.String) to retrieve the property value will return
   * null.
   *
   * WriterInterceptor.aroundWriteTo
   */
  @Test
  public void removePropertyTest() throws Fault {
    setOperationAndEntity(ContextOperation.REMOVEPROPERTY);
    setProperty(Property.SEARCH_STRING, TemplateInterceptorBody.NULL);
    invoke();
  }

  /*
   * @testName: setAnnotationsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:910; JAXRS:JAVADOC:930;
   * 
   * @test_Strategy: Update annotations on the formal declaration of the
   * artifact that initiated the intercepted entity provider invocation.
   *
   * WriterInterceptor.aroundWriteTo
   */
  @Test
  public void setAnnotationsTest() throws Fault {
    Annotation[] annotations = WriterInterceptorOne.class.getAnnotations();
    setOperationAndEntity(ContextOperation.SETANNOTATIONS);
    for (Annotation a : annotations)
      setProperty(Property.UNORDERED_SEARCH_STRING,
          a.annotationType().getName());
    invoke();
  }

  /*
   * @testName: setAnnotationsNullThrowsNPETest
   * 
   * @assertion_ids: JAXRS:JAVADOC:910; JAXRS:JAVADOC:930;
   * 
   * @test_Strategy: Throws NullPointerException - in case the input parameter
   * is null.
   *
   * WriterInterceptor.aroundWriteTo
   */
  @Test
  public void setAnnotationsNullThrowsNPETest() throws Fault {
    setOperationAndEntity(ContextOperation.SETANNOTATIONSNULL);
    setProperty(Property.SEARCH_STRING, TemplateInterceptorBody.NPE);
    invoke();
  }

  /*
   * @testName: setGenericTypeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:911; JAXRS:JAVADOC:930;
   * 
   * @test_Strategy: Update type of the object to be produced or written.
   *
   * WriterInterceptor.aroundWriteTo
   */
  @Test
  public void setGenericTypeTest() throws Fault {
    setOperationAndEntity(ContextOperation.SETGENERICTYPE);
    setProperty(Property.SEARCH_STRING, "[B");
    invoke();
  }

  /*
   * @testName: setMediaTypeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:912; JAXRS:JAVADOC:930;
   * 
   * @test_Strategy: Update media type of HTTP entity.
   *
   * WriterInterceptor.aroundWriteTo
   */
  @Test
  public void setMediaTypeTest() throws Fault {
    setOperationAndEntity(ContextOperation.SETMEDIATYPE);
    setProperty(Property.SEARCH_STRING, MediaType.APPLICATION_FORM_URLENCODED);
    invoke();
  }

  /*
   * @testName: setPropertyTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:913; JAXRS:JAVADOC:930;
   * 
   * @test_Strategy: Binds an object to a given property name in the current
   * request/response exchange context. If the name specified is already used
   * for a property, this method will replace the value of the property with the
   * new value.
   *
   * WriterInterceptor.aroundWriteTo
   */
  @Test
  public void setPropertyTest() throws Fault {
    setOperationAndEntity(ContextOperation.SETPROPERTY);
    setProperty(Property.SEARCH_STRING, TemplateInterceptorBody.ENTITY2);
    invoke();
  }

  /*
   * @testName: setPropertyNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:913; JAXRS:JAVADOC:930;
   * 
   * @test_Strategy: If a null value is passed, the effect is the same as
   * calling the removeProperty(String) method.
   *
   * WriterInterceptor.aroundWriteTo
   */
  @Test
  public void setPropertyNullTest() throws Fault {
    setOperationAndEntity(ContextOperation.SETPROPERTYNULL);
    setProperty(Property.SEARCH_STRING, TemplateInterceptorBody.NULL);
    invoke();
  }

  /*
   * @testName: setTypeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:914; JAXRS:JAVADOC:930;
   * 
   * @test_Strategy: Update Java type before calling message body provider.
   *
   * WriterInterceptor.aroundWriteTo
   */
  @Test
  public void setTypeTest() throws Fault {
    ByteArrayInputStream bais = new ByteArrayInputStream(
        TemplateInterceptorBody.ENTITY.getBytes());
    Reader reader = new InputStreamReader(bais);
    setOperationAndEntity(ContextOperation.SETTYPE);
    setRequestContentEntity(reader);
    addProvider(InputStreamReaderProvider.class);
    invoke();
    InputStreamReader isr = getResponseBody(InputStreamReader.class);
    try {
      String entity = JaxrsUtil.readFromReader(isr);
      assertTrue(entity.contains(InputStreamReader.class.getName()),
          "Expected"+ InputStreamReader.class.getName()+ "not found");
      logMsg("#setType set correct type", entity);
    } catch (IOException e) {
      throw new Fault(e);
    }
  }

  // /////////////////////////////////////////////////////////////////////
  @Override
  protected void addProviders() {
    addProvider(WriterInterceptorTwo.class);
    addProvider(new WriterInterceptorOne());
  }
}
