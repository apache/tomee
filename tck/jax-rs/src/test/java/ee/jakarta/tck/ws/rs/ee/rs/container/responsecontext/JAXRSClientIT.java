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

package ee.jakarta.tck.ws.rs.ee.rs.container.responsecontext;

import java.io.ByteArrayInputStream;
import java.util.Locale;
import java.io.InputStream;
import java.io.IOException;

import ee.jakarta.tck.ws.rs.common.provider.PrintingErrorHandler;
import ee.jakarta.tck.ws.rs.common.provider.StringBean;
import ee.jakarta.tck.ws.rs.common.provider.StringBeanEntityProvider;
import ee.jakarta.tck.ws.rs.common.provider.StringBeanHeaderDelegate;
import ee.jakarta.tck.ws.rs.common.provider.StringBeanRuntimeDelegate;
import ee.jakarta.tck.ws.rs.common.provider.StringBeanWithAnnotation;
import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

import ee.jakarta.tck.ws.rs.common.client.JaxrsCommonClient;
import ee.jakarta.tck.ws.rs.common.client.TextCaser;

import jakarta.annotation.Priority;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

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
 *                     ts_home;
 *
 */
@ExtendWith(ArquillianExtension.class)
public class JAXRSClientIT extends JaxrsCommonClient {

  private static final long serialVersionUID = 7090474648496503290L;

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_ee_rs_container_responsecontext_web/resource");
    setPrintEntity(true);
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

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/container/responsecontext/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_rs_container_responsecontext_web.war");
    archive.addClasses(TSAppConfig.class, Resource.class,
      ContextOperation.class, ResponseFilter.class, SecondResponseFilter.class, TemplateFilter.class, 
      PrintingErrorHandler.class,
      StringBean.class,
      StringBeanEntityProvider.class,
      StringBeanHeaderDelegate.class,
      StringBeanRuntimeDelegate.class,
      StringBeanWithAnnotation.class,
      PrintingErrorHandler.class,
      JaxrsUtil.class
    );
    archive.setWebXML(new StringAsset(webXml));
    return archive;

  }

  /*
   * @testName: getAllowedMethodsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:679; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Get the allowed HTTP methods, all methods will returned as
   * upper case strings.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getAllowedMethodsTest() throws Fault {
    setProperty(Property.UNORDERED_SEARCH_STRING, "options");
    setProperty(Property.UNORDERED_SEARCH_STRING, "trace");
    invokeRequestAndCheckResponse(Request.GET,
        ContextOperation.GETALLOWEDMETHODS);
  }

  /*
   * @testName: getCookiesTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:680; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Get a read-only map of cookie name (String) to a new
   * cookie.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getCookiesTest() throws Fault {
    setProperty(Property.SEARCH_STRING_IGNORE_CASE, ResponseFilter.COOKIENAME);
    invokeRequestAndCheckResponse(Request.GET, ContextOperation.GETCOOKIES);
  }

  /*
   * @testName: getCookiesIsReadOnlyTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:680; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Get a read-only map of cookie name (String) to a new
   * cookie.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getCookiesIsReadOnlyTest() throws Fault {
    invokeRequestAndCheckResponse(Request.GET,
        ContextOperation.GETCOOKIESISREADONLY);
  }

  /*
   * @testName: getDateTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:681; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Get the message date, otherwise null if not present.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getDateTest() throws Fault {
    long date = System.currentTimeMillis();
    date = (date / 1000) * 1000;
    String value = String.valueOf(date);
    setProperty(Property.CONTENT, value);
    setProperty(Property.SEARCH_STRING, value);
    invokeRequestAndCheckResponse(ContextOperation.GETDATE);
  }

  /*
   * @testName: getDateIsNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:681; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Get the message date, otherwise null if not present.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getDateIsNullTest() throws Fault {
    setProperty(Property.SEARCH_STRING, "0");
    invokeRequestAndCheckResponse(ContextOperation.GETDATE);
  }

  /*
   * @testName: getEntityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:682; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Get the message entity Java instance. Returns null if the
   * message does not contain an entity.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getEntityTest() throws Fault {
    String entity = getClass().getName() + "entity";
    setProperty(Property.CONTENT, entity);
    setProperty(Property.SEARCH_STRING, entity + entity);
    invokeRequestAndCheckResponse(ContextOperation.GETENTITY);
  }

  /*
   * @testName: getEntityIsNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:682; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Get the message entity Java instance. Returns null if the
   * message does not contain an entity.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getEntityIsNullTest() throws Fault {
    setProperty(Property.SEARCH_STRING, ResponseFilter.NULL);
    invokeRequestAndCheckResponse(ContextOperation.GETENTITY);
  }

  /*
   * @testName: getEntityAnnotationsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:683; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Get the annotations attached to the entity.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getEntityAnnotationsTest() throws Fault {
    setTextCaser(TextCaser.LOWER);
    setProperty(Property.UNORDERED_SEARCH_STRING, Provider.class.getName());
    setProperty(Property.UNORDERED_SEARCH_STRING, Priority.class.getName());
    setProperty(Property.UNORDERED_SEARCH_STRING, Path.class.getName());
    setProperty(Property.UNORDERED_SEARCH_STRING, POST.class.getName());
    setProperty(Property.CONTENT, "true");
    invokeRequestAndCheckResponse(ContextOperation.GETENTITYANNOTATIONS);
  }

  /*
   * @testName: getEntityAnnotationsWhenNoAnnotationsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:683; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Get the annotations attached to the entity.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getEntityAnnotationsWhenNoAnnotationsTest() throws Fault {
    setTextCaser(TextCaser.UPPER);
    setProperty(Property.UNORDERED_SEARCH_STRING, Path.class.getName());
    setProperty(Property.UNORDERED_SEARCH_STRING, POST.class.getName());
    setProperty(Property.CONTENT, "false");
    invokeRequestAndCheckResponse(ContextOperation.GETENTITYANNOTATIONS);
  }

  /*
   * @testName: getEntityAnnotationsWhenAnnotationsOnEntityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:683; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Get the annotations attached to the entity. The entity
   * instance annotations array does not include annotations declared on the
   * entity implementation class or its ancestors.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getEntityAnnotationsWhenAnnotationsOnEntityTest() throws Fault {
    String entity = "getEntityAnnotationsWhenAnnotationsOnEntityTest";
    setTextCaser(TextCaser.UPPER);
    setProperty(Property.CONTENT, entity);
    setProperty(Property.UNORDERED_SEARCH_STRING, entity);
    setProperty(Property.UNORDERED_SEARCH_STRING, "2");
    invokeRequestAndCheckResponse(
        ContextOperation.GETENTITYANNOTATIONSONENTITY);
  }

  /*
   * @testName: getEntityClassStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:684; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Get the raw entity type information.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getEntityClassStringTest() throws Fault {
    setProperty(Property.CONTENT, "string");
    setProperty(Property.SEARCH_STRING, String.class.getName());
    invokeRequestAndCheckResponse(ContextOperation.GETENTITYCLASS);
  }

  /*
   * @testName: getEntityClassByteArrayTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:684; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Get the raw entity type information.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getEntityClassByteArrayTest() throws Fault {
    setProperty(Property.CONTENT, "bytearray");
    setProperty(Property.SEARCH_STRING, "[B");
    invokeRequestAndCheckResponse(ContextOperation.GETENTITYCLASS);
  }

  /*
   * @testName: getEntityClassInputStreamTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:684; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Get the raw entity type information.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getEntityClassInputStreamTest() throws Fault {
    setProperty(Property.CONTENT, "inputstream");
    setProperty(Property.SEARCH_STRING, ByteArrayInputStream.class.getName());
    invokeRequestAndCheckResponse(ContextOperation.GETENTITYCLASS);
  }

  /*
   * @testName: getEntityStreamTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:685; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Get the entity output stream.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getEntityStreamTest() throws Fault {
    setProperty(Property.SEARCH_STRING, ResponseFilter.ENTITY);
    setProperty(Property.UNEXPECTED_RESPONSE_MATCH, ResponseFilter.NULL);
    invokeRequestAndCheckResponse(Request.GET,
        ContextOperation.GETENTITYSTREAM);
  }

  /*
   * @testName: getEntityTagTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:686; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: the entity tag, otherwise null if not present.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getEntityTagTest() throws Fault {
    setProperty(Property.CONTENT, ResponseFilter.ENTITY);
    setProperty(Property.SEARCH_STRING, ResponseFilter.ENTITY);
    invokeRequestAndCheckResponse(ContextOperation.GETENTITYTAG);
  }

  /*
   * @testName: getEntityTagIsNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:686; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: the entity tag, otherwise null if not present.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getEntityTagIsNullTest() throws Fault {
    setProperty(Property.SEARCH_STRING, ResponseFilter.NULL);
    invokeRequestAndCheckResponse(ContextOperation.GETENTITYTAG);
  }

  /*
   * @testName: getEntityTypeStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:687; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Get the raw entity type information.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getEntityTypeStringTest() throws Fault {
    setProperty(Property.CONTENT, "string");
    setProperty(Property.SEARCH_STRING, String.class.getName());
    invokeRequestAndCheckResponse(ContextOperation.GETENTITYTYPE);
  }

  /*
   * @testName: getEntityTypeByteArrayTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:687; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Get the raw entity type information.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getEntityTypeByteArrayTest() throws Fault {
    setProperty(Property.CONTENT, "bytearray");
    setProperty(Property.SEARCH_STRING, "[B");
    invokeRequestAndCheckResponse(ContextOperation.GETENTITYTYPE);
  }

  /*
   * @testName: getEntityTypeInputStreamTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:687; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Get the raw entity type information.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getEntityTypeInputStreamTest() throws Fault {
    setProperty(Property.CONTENT, "inputstream");
    setProperty(Property.SEARCH_STRING, ByteArrayInputStream.class.getName());
    invokeRequestAndCheckResponse(ContextOperation.GETENTITYTYPE);
  }

  /*
   * @testName: getHeadersTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:688; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Get the mutable response headers multivalued map.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getHeadersTest() throws Fault {
    String header = "header";
    for (int i = 0; i != 5; i++)
      setProperty(Property.UNORDERED_SEARCH_STRING, header + i);
    setProperty(Property.CONTENT, header);
    invokeRequestAndCheckResponse(ContextOperation.GETHEADERS);
  }

  /*
   * @testName: getHeadersIsMutableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:688; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Get the mutable response headers multivalued map.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getHeadersIsMutableTest() throws Fault {
    setProperty(Property.SEARCH_STRING, ResponseFilter.HEADER);
    invokeRequestAndCheckResponse(Request.GET,
        ContextOperation.GETHEADERSISMUTABLE);
  }

  /*
   * @testName: getHeaderStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:689; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Get a message header as a single string value.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getHeaderStringTest() throws Fault {
    setProperty(Property.SEARCH_STRING,
        ContextOperation.GETHEADERSTRINGOPERATION.name());
    invokeRequestAndCheckResponse(Request.GET,
        ContextOperation.GETHEADERSTRINGOPERATION);
  }

  /*
   * @testName: getHeaderStringHeaderIsNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:689; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Get a message header as a single string value. If the
   * message header is not present then null is returned.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getHeaderStringHeaderIsNullTest() throws Fault {
    setProperty(Property.SEARCH_STRING, ResponseFilter.NULL);
    setProperty(Property.CONTENT, "null");
    invokeRequestAndCheckResponse(ContextOperation.GETHEADERSTRINGHEADER);
  }

  /*
   * @testName: getHeaderStringHeaderUsesToStringMethodTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:689; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Get a message header as a single string value. Each single
   * header value is converted to String using its toString method if a header
   * delegate is not available.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getHeaderStringHeaderUsesToStringMethodTest() throws Fault {
    setProperty(Property.SEARCH_STRING, ResponseFilter.ENTITY);
    setProperty(Property.CONTENT, "toString");
    invokeRequestAndCheckResponse(ContextOperation.GETHEADERSTRINGHEADER);
  }

  /*
   * @testName: getHeaderStringHeaderUsesHeaderDelegateTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:689; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Get a message header as a single string value. Each single
   * header value is converted to String using a RuntimeDelegate.HeaderDelegate
   * if one is available via
   * RuntimeDelegate.createHeaderDelegate(java.lang.Class) for the header value
   * class
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getHeaderStringHeaderUsesHeaderDelegateTest() throws Fault {
    invokeRequestAndCheckResponse(Request.GET,
        ContextOperation.SETSTRINGBEANRUNTIME);

    setProperty(Property.SEARCH_STRING, ResponseFilter.ENTITY);
    setProperty(Property.CONTENT, "headerDelegate");
    invokeRequestAndCheckResponse(ContextOperation.GETHEADERSTRINGHEADER);

    invokeRequestAndCheckResponse(Request.GET,
        ContextOperation.SETORIGINALRUNTIME);
  }

  /*
   * @testName: getHeaderStringHeaderIsCommaSepearatedTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:689; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Get a message header as a single string value. If the
   * message header is present more than once then the values of joined together
   * and separated by a ',' character.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getHeaderStringHeaderIsCommaSepearatedTest() throws Fault {
    String entity = ResponseFilter.ENTITY + "," + ResponseFilter.ENTITY;
    setProperty(Property.SEARCH_STRING, entity);
    setProperty(Property.CONTENT, "commaSeparated");
    invokeRequestAndCheckResponse(ContextOperation.GETHEADERSTRINGHEADER);
  }

  /*
   * @testName: getLanguageTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:690; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: the language of the entity or null if not specified
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getLanguageTest() throws Fault {
    setProperty(Property.CONTENT, "fr-ca");
    setProperty(Property.SEARCH_STRING, Locale.CANADA_FRENCH.toString());
    invokeRequestAndCheckResponse(ContextOperation.GETLANGUAGE);
  }

  /*
   * @testName: getLanguageIsNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:690; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: the language of the entity or null if not specified
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getLanguageIsNullTest() throws Fault {
    setProperty(Property.SEARCH_STRING, ResponseFilter.NULL);
    invokeRequestAndCheckResponse(ContextOperation.GETLANGUAGE);
  }

  /*
   * @testName: getLastModifiedTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:691; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: the last modified date, otherwise null if not present.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getLastModifiedTest() throws Fault {
    long milis = System.currentTimeMillis();
    milis = (milis / 1000) * 1000;
    String lastModified = String.valueOf(milis);
    setProperty(Property.CONTENT, lastModified);
    setProperty(Property.SEARCH_STRING, lastModified);
    invokeRequestAndCheckResponse(ContextOperation.GETLASTMODIFIED);
  }

  /*
   * @testName: getLastModifiedIsNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:691; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: the last modified date, otherwise null if not present.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getLastModifiedIsNullTest() throws Fault {
    setProperty(Property.SEARCH_STRING, ResponseFilter.NULL);
    invokeRequestAndCheckResponse(ContextOperation.GETLASTMODIFIED);
  }

  /*
   * @testName: getLengthTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:692; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Content-Length as integer if present and valid number. In
   * other cases returns -1.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getLengthTest() throws Fault {
    String entity = ResponseFilter.ENTITY;
    setProperty(Property.CONTENT, entity);
    String search = ResponseFilter.replaceStart(entity, entity.length());
    setProperty(Property.SEARCH_STRING, search);
    invokeRequestAndCheckResponse(ContextOperation.GETLENGTH);
  }

  /*
   * @testName: getLengthWhenNoEntityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:692; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Content-Length as integer if present and valid number. In
   * other cases returns -1.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getLengthWhenNoEntityTest() throws Fault {
    setProperty(Property.SEARCH_STRING, "-1");
    invokeRequestAndCheckResponse(ContextOperation.GETLENGTH);
  }

  /*
   * @testName: getLinkTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:693; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: the link for the relation, otherwise null if not present.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getLinkTest() throws Fault {
    String url = getAbsoluteUrl();
    setProperty(Property.CONTENT, url);
    setProperty(Property.SEARCH_STRING, url);
    invokeRequestAndCheckResponse(ContextOperation.GETLINK);
  }

  /*
   * @testName: getLinkWhenNoLinkTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:693; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: the link for the relation, otherwise null if not present.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getLinkWhenNoLinkTest() throws Fault {
    setProperty(Property.SEARCH_STRING, ResponseFilter.NULL);
    invokeRequestAndCheckResponse(ContextOperation.GETLINK);
  }

  /*
   * @testName: getLinkBuilderTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:694; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: the link for the relation, otherwise null if not present.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getLinkBuilderTest() throws Fault {
    String url = getAbsoluteUrl();
    setProperty(Property.CONTENT, url);
    setProperty(Property.SEARCH_STRING, url);
    invokeRequestAndCheckResponse(ContextOperation.GETLINKBUILDER);
  }

  /*
   * @testName: getLinkBuilderWhenNoLinkTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:694; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: the link for the relation, otherwise null if not present.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getLinkBuilderWhenNoLinkTest() throws Fault {
    setProperty(Property.SEARCH_STRING, ResponseFilter.NULL);
    invokeRequestAndCheckResponse(ContextOperation.GETLINKBUILDER);
  }

  /*
   * @testName: getLinksTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:695; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: links, may return empty Set if no links are present. Never
   * returns null.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getLinksTest() throws Fault {
    String uri1 = getAbsoluteUrl();
    String uri2 = "html://localhost:8080/nohttp";
    setProperty(Property.CONTENT, uri1 + ";" + uri2);
    setProperty(Property.UNORDERED_SEARCH_STRING, uri1);
    setProperty(Property.UNORDERED_SEARCH_STRING, uri2);
    invokeRequestAndCheckResponse(ContextOperation.GETLINKS);
  }

  /*
   * @testName: getLinksWhenNoLinkTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:695; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: links, may return empty Set if no links are present. Never
   * returns null.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getLinksWhenNoLinkTest() throws Fault {
    setProperty(Property.SEARCH_STRING, ResponseFilter.NULL);
    invokeRequestAndCheckResponse(ContextOperation.GETLINKS);
  }

  /*
   * @testName: getLocationTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:696; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: the location URI, otherwise null if not present.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getLocationTest() throws Fault {
    String uri = getAbsoluteUrl();
    setProperty(Property.CONTENT, uri);
    setProperty(Property.SEARCH_STRING, uri);
    invokeRequestAndCheckResponse(ContextOperation.GETLOCATION);
  }

  /*
   * @testName: getLocationWhenNoLocationTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:696; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: the location URI, otherwise null if not present.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getLocationWhenNoLocationTest() throws Fault {
    setProperty(Property.SEARCH_STRING, ResponseFilter.NULL);
    invokeRequestAndCheckResponse(ContextOperation.GETLOCATION);
  }

  /*
   * @testName: getMediaTypeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:697; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: the media type or null if not specified.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getMediaTypeTest() throws Fault {
    String uri = MediaType.APPLICATION_SVG_XML;
    setProperty(Property.CONTENT, uri);
    setProperty(Property.SEARCH_STRING, uri);
    invokeRequestAndCheckResponse(ContextOperation.GETMEDIATYPE);
  }

  /*
   * @testName: getMediaTypeWhenNoMediaTypeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:697; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: the media type or null if not specified.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getMediaTypeWhenNoMediaTypeTest() throws Fault {
    setProperty(Property.SEARCH_STRING, ResponseFilter.NULL);
    invokeRequestAndCheckResponse(ContextOperation.GETMEDIATYPE);
  }

  /*
   * @testName: getStatusTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:698; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Get the status code associated with the response.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getStatusTest() throws Fault {
    for (Response.Status status : Response.Status.values()) {
      String content = String.valueOf(status.getStatusCode());
      setProperty(Property.CONTENT, content);
      setProperty(Property.SEARCH_STRING, content);
      invokeRequestAndCheckResponse(ContextOperation.GETSTATUS);
    }
  }

  /*
   * @testName: getStatusInfoTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:699; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: the response status information or null if the status was
   * not set.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getStatusInfoTest() throws Fault {
    for (Response.Status status : Response.Status.values()) {
      String content = String.valueOf(status.getStatusCode());
      setProperty(Property.CONTENT, content);
      setProperty(Property.SEARCH_STRING, content);
      invokeRequestAndCheckResponse(ContextOperation.GETSTATUSINFO);
    }
  }

  /*
   * @testName: getStringHeadersTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:700; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Get a string view of header values associated with the
   * message.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getStringHeadersTest() throws Fault {
    String entity = "EnTITY";
    setProperty(Property.SEARCH_STRING, entity);
    setProperty(Property.CONTENT, entity);
    invokeRequestAndCheckResponse(ContextOperation.GETSTRINGHEADERS);
  }

  /*
   * @testName: getStringHeadersHeaderIsNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:700; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Get a string view of header values associated with the
   * message. If the message header is not present then null is returned.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getStringHeadersHeaderIsNullTest() throws Fault {
    setProperty(Property.SEARCH_STRING, ResponseFilter.NULL);
    setProperty(Property.CONTENT, "null");
    invokeRequestAndCheckResponse(ContextOperation.GETSTRINGHEADERS);
  }

  /*
   * @testName: getStringHeadersUsesToStringMethodTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:700; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Get a string view of header values associated with the
   * message. Each single header value is converted to String using its toString
   * method if a header delegate is not available.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getStringHeadersUsesToStringMethodTest() throws Fault {
    setProperty(Property.SEARCH_STRING, ResponseFilter.ENTITY);
    setProperty(Property.CONTENT, "toString");
    invokeRequestAndCheckResponse(ContextOperation.GETSTRINGHEADERS);
  }

  /*
   * @testName: getStringHeadersUsesHeaderDelegateTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:700; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Get a string view of header values associated with the
   * message. Each single header value is converted to String using a
   * RuntimeDelegate.HeaderDelegate if one is available via
   * RuntimeDelegate.createHeaderDelegate(java.lang.Class) for the header value
   * class
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getStringHeadersUsesHeaderDelegateTest() throws Fault {
    invokeRequestAndCheckResponse(Request.GET,
        ContextOperation.SETSTRINGBEANRUNTIME);

    setProperty(Property.SEARCH_STRING, ResponseFilter.ENTITY);
    setProperty(Property.CONTENT, "headerDelegate");
    invokeRequestAndCheckResponse(ContextOperation.GETSTRINGHEADERS);

    invokeRequestAndCheckResponse(Request.GET,
        ContextOperation.SETORIGINALRUNTIME);
  }

  /*
   * @testName: getStringsHeaderMoreItemsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:700; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Get a string view of header values associated with the
   * message.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void getStringsHeaderMoreItemsTest() throws Fault {
    // its twice there
    setProperty(Property.SEARCH_STRING_IGNORE_CASE, ResponseFilter.ENTITY);
    setProperty(Property.SEARCH_STRING_IGNORE_CASE, ResponseFilter.ENTITY);
    setProperty(Property.CONTENT, "commaSeparated");
    invokeRequestAndCheckResponse(ContextOperation.GETSTRINGHEADERS);
  }

  /*
   * @testName: hasEntityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:701; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: The method returns true if the entity is present, returns
   * false otherwise.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void hasEntityTest() throws Fault {
    setProperty(Property.CONTENT, ResponseFilter.ENTITY);
    setProperty(Property.SEARCH_STRING, "true");
    invokeRequestAndCheckResponse(ContextOperation.HASENTITY);
  }

  /*
   * @testName: hasEntityWhenNoEntityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:701; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: The method returns true if the entity is present, returns
   * false otherwise.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void hasEntityWhenNoEntityTest() throws Fault {
    setProperty(Property.SEARCH_STRING, "false");
    invokeRequestAndCheckResponse(ContextOperation.HASENTITY);
  }

  /*
   * @testName: hasLinkTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:702; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Check if link for relation exists.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void hasLinkTest() throws Fault {
    String url = getAbsoluteUrl();
    setProperty(Property.CONTENT, url);
    setProperty(Property.SEARCH_STRING, "true");
    invokeRequestAndCheckResponse(ContextOperation.HASLINK);
  }

  /*
   * @testName: hasLinkWhenNoLinkTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:702; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Check if link for relation exists.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void hasLinkWhenNoLinkTest() throws Fault {
    setProperty(Property.SEARCH_STRING, "false");
    invokeRequestAndCheckResponse(ContextOperation.HASLINK);
  }

  /*
   * @testName: setEntityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:703; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Set a new response message entity.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void setEntityTest() throws Fault {
    setProperty(Property.SEARCH_STRING, ResponseFilter.ENTITY);
    invokeRequestAndCheckResponse(Request.GET, ContextOperation.SETENTITY);
  }

  /*
   * @testName: setEntityStreamTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:704; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Set a new entity output stream.
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void setEntityStreamTest() throws Fault {
    setProperty(Property.SEARCH_STRING, ResponseFilter.ENTITY);
    setProperty(Property.SEARCH_STRING, "OK");
    invokeRequestAndCheckResponse(Request.GET,
        ContextOperation.SETENTITYSTREAM);
  }

  /*
   * @testName: setStatusTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:705; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Set a new response status code
   *
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void setStatusTest() throws Fault {
    for (Response.Status status : Response.Status.values()) {
      String content = String.valueOf(status.getStatusCode());
      setProperty(Property.CONTENT, content);
      setProperty(Property.STATUS_CODE, content);
      invokeRequestAndCheckResponse(ContextOperation.SETSTATUS);
    }
  }

  /*
   * @testName: setStatusInfoTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:706; JAXRS:JAVADOC:707; JAXRS:JAVADOC:708;
   * 
   * @test_Strategy: Set the complete status information associated with the
   * response.
   * 
   * Filter method called after a response has been provided for a request.
   * Throws IOException.
   */
  @Test
  public void setStatusInfoTest() throws Fault {
    for (Response.Status status : Response.Status.values()) {
      String content = String.valueOf(status.getStatusCode());
      setProperty(Property.CONTENT, content);
      setProperty(Property.STATUS_CODE, content);
      invokeRequestAndCheckResponse(ContextOperation.SETSTATUSINFO);
    }
  }

  // ////////////////////////////////////////////////////////////////////////////

  protected void invokeRequestAndCheckResponse(ContextOperation operation)
      throws Fault {
    String request;
    request = buildRequest(Request.POST, operation.name().toLowerCase());
    setProperty(Property.REQUEST, request);
    invoke();
  }

  protected void invokeRequestAndCheckResponse(Request method,
      ContextOperation operation) throws Fault {
    String request;
    request = buildRequest(method, operation.name().toLowerCase());
    setProperty(Property.REQUEST, request);
    invoke();
  }
}
