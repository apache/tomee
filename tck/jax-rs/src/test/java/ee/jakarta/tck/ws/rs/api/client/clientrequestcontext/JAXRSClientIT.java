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

package ee.jakarta.tck.ws.rs.api.client.clientrequestcontext;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.Variant;
import jakarta.ws.rs.ext.RuntimeDelegate;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.common.provider.StringBean;
import ee.jakarta.tck.ws.rs.common.provider.StringBeanEntityProvider;
import ee.jakarta.tck.ws.rs.common.provider.StringBeanRuntimeDelegate;
import ee.jakarta.tck.ws.rs.common.provider.StringBeanWithAnnotation;
import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
public class JAXRSClientIT extends JAXRSCommonClient {

  private static final long serialVersionUID = 8883841555516513076L;

  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : "+testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : "+testInfo.getDisplayName());
  }

  /*
   * @testName: abortWithTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:427; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85;
   * 
   * @test_Strategy: Abort the filter chain with a response. This method breaks
   * the filter chain processing and returns the provided response back to the
   * client. The provided response goes through the chain of applicable response
   * filters.
   * 
   * ClientRequestFilter.filter ClientRequestFilter.abortWith
   */
  @Test
  public void abortWithTest() throws Fault {
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        Response r = Response.status(Status.CREATED).build();
        context.abortWith(r);
      }
    };
    Invocation i = buildInvocation(provider);
    Response r = invoke(i);
    assertStatus(r, Status.CREATED);
  }

  /*
   * @testName: getAcceptableLanguagesTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:428; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Get a list of languages that are acceptable for the
   * response. Returns: a read-only list of acceptable languages sorted
   * according to their q-value, with highest preference first.
   * 
   * ClientRequestFilter.filter ClientRequestFilter.abortWith
   */
  @Test
  public void getAcceptableLanguagesTest() throws Fault {
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        List<Locale> locales = context.getAcceptableLanguages();
        String languages = JaxrsUtil.iterableToString(";", locales);
        Response r = Response.ok(languages).build();
        context.abortWith(r);
      }
    };
    Invocation.Builder builder = buildBuilder(provider);
    Invocation invocation;
    invocation = builder.acceptLanguage(Locale.CANADA_FRENCH)
        .acceptLanguage(Locale.PRC).buildGet();
    Response response = invoke(invocation);
    String entity = response.readEntity(String.class);
    assertContains(entity, Locale.CANADA_FRENCH.toString());
    assertContains(entity, Locale.PRC.toString());
  }

  /*
   * @testName: getAcceptableLanguagesByWeightsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:428; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: a read-only list of requested response media types sorted
   * according to their q-value, with highest preference first.
   * 
   * ClientRequestFilter.filter ClientRequestFilter.abortWith
   */
  @Test
  public void getAcceptableLanguagesByWeightsTest() throws Fault {
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        List<Locale> locales = context.getAcceptableLanguages();
        String languages = JaxrsUtil.iterableToString(";", locales);
        Response r = Response.ok(languages).build();
        context.abortWith(r);
      }
    };
    Invocation.Builder builder = buildBuilder(provider);
    Invocation invocation;
    invocation = builder.acceptLanguage("da, en-gb;q=0.6, en-us;q=0.7")
        .buildGet();
    Response response = invoke(invocation);
    String entity = response.readEntity(String.class).toLowerCase();
    assertContains(entity, "da");
    assertContains(entity, "gb");
    assertContains(entity, "us");
    int indexDa = entity.indexOf("da");
    int indexUs = entity.indexOf("us");
    int indexGb = entity.indexOf("gb");

    assertTrue(indexDa < indexUs && indexUs < indexGb,
        "List of acceptable languages " + entity + " is not sorted by q values");
  }

  /*
   * @testName: getAcceptableLanguagesIsImmutableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:428; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: a read-only list of requested response media types sorted
   * according to their q-value, with highest preference first.
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getAcceptableLanguagesIsImmutableTest() throws Fault {
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        List<Locale> locales = context.getAcceptableLanguages();
        try {
          locales.add(Locale.JAPAN);
        } catch (Exception e) {
          // either exception is thrown, or add does nothing
        }
        locales = context.getAcceptableLanguages();
        boolean b = locales.contains(Locale.JAPAN);
        assertTrue(!b, "getAcceptableLanguages is not read-only");
        Response r = Response.ok().build();
        context.abortWith(r);
      }
    };
    WebTarget target = buildTarget(provider);
    Invocation.Builder builder = target.request();
    Invocation invocation;
    invocation = builder
        .header("Accept-Language", "da, en-gb;q=0.6, en-us;q=0.7").buildGet();
    Response response = invoke(invocation);
    assertStatus(response, Status.OK);
  }

  /*
   * @testName: getAcceptableMediaTypesTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:429; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Get a list of media types that are acceptable for the
   * response. Returns a read-only list of requested response media types sorted
   * according to their q-value, with highest preference first.
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getAcceptableMediaTypesTest() throws Fault {
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        List<MediaType> types = context.getAcceptableMediaTypes();
        String medias = JaxrsUtil.iterableToString(";", types);
        Response r = Response.ok(medias).build();
        context.abortWith(r);
      }
    };
    String media = "text/*;q=0.3, text/html;q=0.7, text/html;level=1, text/html;level=2;q=0.4, */*;q=0.5";
    Invocation.Builder builder = buildBuilder(provider);
    Invocation invocation = builder.header("Accept", media).buildGet();
    Response response = invoke(invocation);

    String entity = response.readEntity(String.class);
    assertContains(entity, "text/*");
    assertContains(entity, "text/html");
    assertContains(entity, "*/*");
  }

  /*
   * @testName: getAcceptableMediaTypesIsSortedTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:429; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Get a list of media types that are acceptable for the
   * response. Returns a read-only list of requested response media types sorted
   * according to their q-value, with highest preference first.
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getAcceptableMediaTypesIsSortedTest() throws Fault {
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        List<MediaType> types = context.getAcceptableMediaTypes();
        String medias = JaxrsUtil.iterableToString(";", types);
        Response r = Response.ok(medias).build();
        context.abortWith(r);
      }
    };
    String media = "text/plain;q=0.3, text/html;q=0.7, text/xml;level=1, text/java;level=2;q=0.4, */*;q=0.5";
    Invocation.Builder builder = buildBuilder(provider);
    Invocation invocation = builder.header("Accept", media).buildGet();
    Response response = invoke(invocation);

    String entity = response.readEntity(String.class).toLowerCase();
    int indexXml = entity.indexOf(MediaType.TEXT_XML);
    int indexHtml = entity.indexOf(MediaType.TEXT_HTML);
    int indexAny = entity.indexOf(MediaType.WILDCARD);
    int indexJava = entity.indexOf("text/java");
    int indexPlain = entity.indexOf(MediaType.TEXT_PLAIN);

    assertTrue(indexXml < indexHtml && indexHtml < indexAny
        && indexAny < indexJava && indexJava < indexPlain, "Media Types " +
        entity + " are not sorted");
  }

  /*
   * @testName: getAcceptableMediaTypesIsImmutableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:429; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Get a list of media types that are acceptable for the
   * response. Returns a read-only list of requested response media types sorted
   * according to their q-value, with highest preference first.
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getAcceptableMediaTypesIsImmutableTest() throws Fault {
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        List<MediaType> types = context.getAcceptableMediaTypes();
        try {
          types.add(MediaType.APPLICATION_JSON_TYPE);
        } catch (Exception e) {
          // either exception is thrown or add does nothing
        }
        types = context.getAcceptableMediaTypes();
        boolean b = types.contains(MediaType.APPLICATION_JSON_TYPE);
        assertTrue(!b, "getAcceptableMediaTypes is not read only");
        Response r = Response.ok().build();
        context.abortWith(r);
      }
    };
    String media = "text/*;q=0.3, text/html;q=0.7, text/html;level=1, text/html;level=2;q=0.4, */*;q=0.5";
    Invocation.Builder builder = buildBuilder(provider);
    Invocation invocation = builder.header("Accept", media).buildGet();
    Response response = invoke(invocation);
    assertStatus(response, Status.OK);
  }

  /*
   * @testName: getClientTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:430; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Get the client instance associated with the request.
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getClientTest() throws Fault {
    final Client client = ClientBuilder.newClient();

    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        Client contextClient = context.getClient();
        assertTrue(client == contextClient,
            "the client instance is different from the context one");
        Response r = Response.ok().build();
        context.abortWith(r);
      }
    };
    client.register(provider);
    WebTarget target = client.target(getUrl());
    Invocation invocation = target.request().buildGet();
    Response response = invoke(invocation);
    assertStatus(response, Status.OK);
  }

  /*
   * @testName: getConfigurationTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:977; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Get the immutable configuration of the request.
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getConfigurationTest() throws Fault {
    final Client client = ClientBuilder.newClient();
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        Client contextClient = context.getClient();
        assertEquals(contextClient, client,
            "the client instance is different from the context one");
        Configuration contextConfig = context.getConfiguration();
        assertNotNull(contextConfig,
            "context.getConfiguration() returned null");
        Response r = Response.ok().build();
        context.abortWith(r);
      }
    };
    client.register(provider);
    WebTarget target = client.target(getUrl());
    Invocation invocation = target.request().buildGet();
    Response response = invoke(invocation);
    assertStatus(response, Status.OK);
  }

  /*
   * @testName: getCookiesTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:432; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Get any cookies that accompanied the request. Returns a
   * read-only map of cookie name (String) to Cookie.
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getCookiesTest() throws Fault {
    Cookie cts = new Cookie("cts", "cts");
    Cookie tck = new Cookie("tck", "tck");
    Cookie jee = new Cookie("jee", "jee");

    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        String cookies = JaxrsUtil.iterableToString(";",
            context.getCookies().values());
        Response r = Response.ok(cookies).build();
        context.abortWith(r);
      }
    };
    Invocation invocation = buildBuilder(provider).cookie(cts).cookie(tck)
        .cookie(jee).buildGet();
    Response response = invoke(invocation);

    String entity = response.readEntity(String.class);
    assertContains(entity, "cts");
    assertContains(entity, "tck");
    assertContains(entity, "jee");
  }

  /*
   * @testName: getCookiesIsImmutableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:432; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Get any cookies that accompanied the request. Returns a
   * read-only map of cookie name (String) to Cookie.
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getCookiesIsImmutableTest() throws Fault {
    final Cookie cts = new Cookie("cts", "cts");

    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        Map<String, Cookie> cookies = context.getCookies();
        try {
          cookies.put("test", cts);
        } catch (Exception e) {
          // either exception is thrown or put does nothing
        }
        cookies = context.getCookies();
        Cookie cookie = cookies.get("test");
        assertTrue(cookie == null, "getCookies is not read-only");
        Response r = Response.ok().build();
        context.abortWith(r);
      }
    };
    Invocation invocation = buildBuilder(provider).cookie(cts).buildGet();
    Response response = invoke(invocation);
    assertStatus(response, Status.OK);
  }

  /*
   * @testName: getDateNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:433; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Get message date. Returns: the message date, otherwise null
   * if not present.
   *
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getDateNullTest() throws Fault {
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        Date date = context.getDate();
        Response r = Response.ok(date == null ? "NULL" : date.toString())
            .build();
        context.abortWith(r);
      }
    };
    Invocation invocation = buildInvocation(provider);
    Response response = invoke(invocation);

    String entity = response.readEntity(String.class);
    assertContains(entity, "NULL");
  }

  /*
   * @testName: getDateTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:433; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Get message date. Returns: the message date, otherwise null
   * if not present.
   *
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getDateTest() throws Fault {
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        Date date = context.getDate();
        Response r = Response.ok(date.toString()).build();
        context.abortWith(r);
      }
    };
    Invocation invocation = buildBuilder(provider)
        .header("Date", "Tue, 15 Nov 1994 08:12:31 GMT").buildGet();
    Response response = invoke(invocation);

    String entity = response.readEntity(String.class);
    assertContains(entity, "Nov");
    assertContains(entity, "1994");
    assertContains(entity, "31");
  }

  /*
   * @testName: getEntityNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:434; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Get the message entity Java instance. Returns null if the
   * message does not contain an entity.
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getEntityNullTest() throws Fault {
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        Object entity = context.getEntity();
        Response r = Response.ok(entity == null ? "NULL" : entity.toString())
            .build();
        context.abortWith(r);
      }
    };
    Invocation invocation = buildInvocation(provider);
    Response response = invoke(invocation);

    String entity = response.readEntity(String.class);
    assertContains(entity, "NULL");
  }

  /*
   * @testName: getEntityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:434; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85;
   * 
   * @test_Strategy: Get the message entity Java instance. Returns null if the
   * message does not contain an entity.
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getEntityTest() throws Fault {
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        Object entity = context.getEntity();
        Response r = Response.ok(entity.toString()).build();
        context.abortWith(r);
      }
    };
    Entity<String> post = createEntity("test");
    Invocation invocation = buildBuilder(provider).buildPost(post);
    Response response = invoke(invocation);

    String entity = response.readEntity(String.class);
    assertContains(entity, "test");
  }

  /*
   * @testName: getEntityAnnotationsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:435; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Get the annotations attached to the entity. Note that the
   * returned annotations array contains only those annotations explicitly
   * attached to entity instance (such as the ones attached using
   * Entity.Entity(Object, jakarta.ws.rs.core.MediaType,
   * java.lang.annotation.Annotation[]) method).
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getEntityAnnotationsTest() throws Fault {
    Annotation[] annotations = ContextProvider.class.getAnnotations();
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        Annotation[] annotations = context.getEntityAnnotations();
        String first = annotations == null ? "NULL"
            : annotations.length == 0 ? "0"
                : annotations[0].annotationType().getName();
        Response r = Response.ok(first).build();
        context.abortWith(r);
      }
    };
    Entity<String> post = Entity.entity("test", MediaType.WILDCARD_TYPE,
        annotations);
    Invocation invocation = buildBuilder(provider).buildPost(post);
    Response response = invoke(invocation);

    String entity = response.readEntity(String.class);
    assertContains(entity, annotations[0].annotationType().getName());
  }

  /*
   * @testName: getEntityAnnotationsIsNotTakenFromEntityClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:435; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Get the annotations attached to the entity. Note that the
   * returned annotations array contains only those annotations explicitly
   * attached to entity instance (such as the ones attached using
   * Entity.Entity(Object, jakarta.ws.rs.core.MediaType,
   * java.lang.annotation.Annotation[]) method). The entity instance annotations
   * array does not include annotations declared on the entity implementation
   * class or its ancestors.
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getEntityAnnotationsIsNotTakenFromEntityClassTest() throws Fault {
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        Annotation[] annotations = context.getEntityAnnotations();
        String first = annotations == null ? "0"
            : String.valueOf(annotations.length);
        Response r = Response.ok(first).build();
        context.abortWith(r);
      }
    };
    Entity<StringBeanWithAnnotation> post = createEntity(
        new StringBeanWithAnnotation("test"));
    Invocation invocation = buildTarget(provider)
        .register(StringBeanEntityProvider.class).request().buildPost(post);
    Response response = invoke(invocation);

    String entity = response.readEntity(String.class);
    assertContains(entity, "0");
  }

  /*
   * @testName: getEntityAnnotationsNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:435; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Get the annotations attached to the entity.
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getEntityAnnotationsNullTest() throws Fault {
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        Annotation[] annotations = context.getEntityAnnotations();
        String len = annotations == null ? "0"
            : String.valueOf(annotations.length);
        Response r = Response.ok(len).build();
        context.abortWith(r);
      }
    };
    Entity<String> post = createEntity("test");
    Invocation invocation = buildBuilder(provider).buildPost(post);
    Response response = invoke(invocation);

    String entity = response.readEntity(String.class);
    assertContains(entity, "0");
  }

  /*
   * @testName: getEntityClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:436; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Get the raw entity type information.
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getEntityClassTest() throws Fault {
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        Class<?> clazz = context.getEntityClass();
        Response r = Response.ok(clazz.getName()).build();
        context.abortWith(r);
      }
    };
    Entity<ByteArrayInputStream> post = createEntity(
        new ByteArrayInputStream("test".getBytes()));
    Invocation invocation = buildBuilder(provider).buildPost(post);
    Response response = invoke(invocation);

    String entity = response.readEntity(String.class);
    assertContains(entity, ByteArrayInputStream.class.getName());
  }

  /*
   * @testName: getEntityClassListStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:436; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Get the raw entity type information.
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getEntityClassListStringTest() throws Fault {
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        Class<?> clazz = context.getEntityClass();
        Response r = Response.ok(clazz.getName()).build();
        context.abortWith(r);
      }
    };
    List<String> list = new ArrayList<String>();
    Entity<List<String>> post = createEntity(list);
    Invocation invocation = buildBuilder(provider).buildPost(post);
    Response response = invoke(invocation);

    String entity = response.readEntity(String.class);
    assertContains(entity, ArrayList.class.getName());
  }

  /*
   * @testName: getEntityTypeListStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:438; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Get the generic entity type information.
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getEntityTypeListStringTest() throws Fault {
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        Type type = context.getEntityType();
        String entity = type.toString();
        Response r = Response.ok(entity).build();
        context.abortWith(r);
      }
    };
    List<String> list = new ArrayList<String>();
    GenericEntity<List<String>> generic = new GenericEntity<List<String>>(
        list) {
    };
    Entity<GenericEntity<List<String>>> post = createEntity(generic);
    Invocation invocation = buildBuilder(provider).buildPost(post);
    Response response = invoke(invocation);

    String entity = response.readEntity(String.class);
    assertContains(entity, String.class.getName());
  }

  /*
   * @testName: getHeadersTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:439; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Get the mutable request headers multivalued map.
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getHeadersTest() throws Fault {
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        MultivaluedMap<String, Object> headers = context.getHeaders();
        String entity = JaxrsUtil.iterableToString(";", headers.keySet());
        Response r = Response.ok(entity).build();
        context.abortWith(r);
      }
    };
    Invocation invocation = buildBuilder(provider)
        .header("Accept", MediaType.TEXT_HTML).header("tck", "cts")
        .header("Date", "Tue, 15 Nov 1994 08:12:31 GMT").buildGet();
    Response response = invoke(invocation);

    String entity = response.readEntity(String.class);
    assertContains(entity, "Accept");
    assertContains(entity, "Date");
    assertContains(entity, "tck");
  }

  /*
   * @testName: getHeadersIsMutableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:440; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Get the mutable request headers multivalued map.
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getHeadersIsMutableTest() throws Fault {
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        MultivaluedMap<String, Object> headers = context.getHeaders();
        headers.add("Accept", MediaType.APPLICATION_JSON);

        headers = context.getHeaders();
        String entity = JaxrsUtil.iterableToString(";", headers.keySet());
        Response r = Response.ok(entity).build();
        context.abortWith(r);
      }
    };
    Invocation invocation = buildBuilder(provider).buildGet();
    Response response = invoke(invocation);

    String entity = response.readEntity(String.class);
    assertContains(entity, "Accept");
  }

  /*
   * @testName: getHeaderStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:440; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Get a message header as a single string value.
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getHeaderStringTest() throws Fault {
    final String TCK = "cts";
    final String DATE = "Tue, 15 Nov 1994 08:12:31 GMT";
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        String value;
        value = context.getHeaderString("tck");
        assertContainsIgnoreCase(value, TCK, "The expected value", TCK,
            "was not found, found", value, "instead");
        value = context.getHeaderString("accept");
        assertContainsIgnoreCase(value, MediaType.TEXT_HTML,
            "The expected value", MediaType.TEXT_HTML, "was not found, found",
            value, "instead");
        value = context.getHeaderString("date");
        assertContainsIgnoreCase(value, DATE, "The expected value", DATE,
            "was not found, found", value, "instead");
        Response r = Response.ok().build();
        context.abortWith(r);
      }
    };
    Invocation invocation = buildBuilder(provider)
        .header("Accept", MediaType.TEXT_HTML)
        .header("tck", new StringBuffer().append(TCK)) // toString()
        .header("Date", DATE).buildGet();
    Response response = invoke(invocation);
    assertStatus(response, Status.OK);
  }

  /*
   * @testName: getHeaderStringUsingHeaderDelegateTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:440; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Get a message header as a single string value. Each single
   * header value is converted to String using a RuntimeDelegate.HeaderDelegate.
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getHeaderStringUsingHeaderDelegateTest() throws Fault {
    final String name = "BEAN";
    final StringBean bean = new StringBean(name);
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        String value = context.getHeaderString(name);
        Response r = Response.ok(value).build();
        context.abortWith(r);
      }
    };
    RuntimeDelegate original = RuntimeDelegate.getInstance();
    RuntimeDelegate.setInstance(new StringBeanRuntimeDelegate(original));
    try {
      Invocation invocation = buildBuilder(provider).header(name, bean)
          .buildGet();
      Response response = invoke(invocation);
      String body = response.readEntity(String.class);
      assertContains(name.toLowerCase(), body.toLowerCase());
    } finally {
      RuntimeDelegate.setInstance(original);
      StringBeanRuntimeDelegate.assertNotStringBeanRuntimeDelegate();
    }
  }

  /*
   * @testName: getLanguageIsNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:441; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Get the language of the entity. Returns: the language of
   * the entity or null if not specified
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getLanguageIsNullTest() throws Fault {
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        Locale lang = context.getLanguage();
        String entity = lang == null ? "NULL" : lang.toString();
        Response r = Response.ok(entity).build();
        context.abortWith(r);
      }
    };
    Entity<String> entity = createEntity("TEST");
    Invocation invocation = buildBuilder(provider).buildPost(entity);
    Response response = invoke(invocation);

    String body = response.readEntity(String.class);
    assertContains(body, "NULL");
  }

  /*
   * @testName: getLanguageTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:441; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Get the language of the entity. Returns: the language of
   * the entity or null if not specified
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getLanguageTest() throws Fault {
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        Locale lang = context.getLanguage();
        String entity = lang == null ? "NULL" : lang.toString();
        Response r = Response.ok(entity).build();
        context.abortWith(r);
      }
    };
    Locale locale = Locale.TRADITIONAL_CHINESE;
    Variant variant = new Variant(MediaType.TEXT_XML_TYPE, locale, null);
    Entity<String> entity = Entity.entity("TEST", variant);
    Invocation invocation = buildBuilder(provider).buildPost(entity);
    Response response = invoke(invocation);

    String body = response.readEntity(String.class).toLowerCase().replace('-',
        '_');
    assertContains(body, locale.toString().toLowerCase());
  }

  /*
   * @testName: getMediaTypeIsNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:442; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Get the media type of the entity. Returns: the media type
   * or null if not specified (e.g. there's no request entity).
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getMediaTypeIsNullTest() throws Fault {
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        MediaType media = context.getMediaType();
        String entity = media == null ? "NULL" : media.toString();
        Response r = Response.ok(entity).build();
        context.abortWith(r);
      }
    };
    Invocation invocation = buildBuilder(provider).buildGet();
    Response response = invoke(invocation);

    String body = response.readEntity(String.class);
    assertContains(body, "NULL");
  }

  /*
   * @testName: getMediaTypeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:442; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Get the media type of the entity. Returns: the media type
   * or null if not specified (e.g. there's no request entity).
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getMediaTypeTest() throws Fault {
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        MediaType media = context.getMediaType();
        String entity = media == null ? "NULL" : media.toString();
        Response r = Response.ok(entity).build();
        context.abortWith(r);
      }
    };
    Entity<String> entity = Entity.entity("TEST",
        MediaType.APPLICATION_FORM_URLENCODED);
    Invocation invocation = buildBuilder(provider).buildPost(entity);
    Response response = invoke(invocation);

    String body = response.readEntity(String.class);
    assertContains(body, MediaType.APPLICATION_FORM_URLENCODED);
  }

  /*
   * @testName: getMethodTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:443; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Get the request method.
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getMethodTest() throws Fault {
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        String method = context.getMethod();
        Response r = Response.ok(method).build();
        context.abortWith(r);
      }
    };
    Entity<String> entity = createEntity("TEST");
    Invocation invocation;
    Response response;

    for (String method : new String[] { "OPTIONS", "DELETE", "GET", "TRACE" }) {
      invocation = buildBuilder(provider).build(method);
      response = invoke(invocation);
      String body = response.readEntity(String.class).toUpperCase();
      assertContains(body, method);
    }

    for (String method : new String[] { "PUT", "POST" }) {
      invocation = buildBuilder(provider).build(method, entity);
      response = invoke(invocation);
      String body = response.readEntity(String.class).toUpperCase();
      assertContains(body, method);
    }
  }

  /*
   * @testName: getPropertyIsNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:444; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Returns the property with the given name registered in the
   * current request/response exchange context, or null if there is no property
   * by that name.
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getPropertyIsNullTest() throws Fault {
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        Object property = context.getProperty("PROPERTY");
        String entity = property == null ? "NULL" : property.toString();
        Response r = Response.ok(entity).build();
        context.abortWith(r);
      }
    };
    Invocation invocation = buildBuilder(provider).buildGet();
    Response response = invoke(invocation);

    String body = response.readEntity(String.class);
    assertContains(body, "NULL");
  }

  /*
   * @testName: getSetPropertyTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:444; JAXRS:JAVADOC:453; JAXRS:JAVADOC:455;
   * JAXRS:JAVADOC:456; JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Returns the property with the given name registered in the
   * current request/response exchange context, or null if there is no property
   * by that name.
   * 
   * Binds an object to a given property name in the current request/response
   * exchange context.
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getSetPropertyTest() throws Fault {
    final AtomicInteger counter = new AtomicInteger(0);
    ContextProvider provider = new GetSetPropertyProvider(counter);
    ContextProvider provider2 = new GetSetPropertyProvider(counter) {
    };

    Invocation invocation = buildInvocation(provider, provider2);
    Response response = invoke(invocation);

    String body = response.readEntity(String.class);
    assertContains(body, "value");
  }

  /*
   * @testName: getPropertyNamesTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:978; JAXRS:JAVADOC:453; JAXRS:JAVADOC:455;
   * JAXRS:JAVADOC:456; JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Returns an immutable collection containing the property
   * names available within the context of the current request/response exchange
   * context.
   * 
   * Binds an object to a given property name in the current request/response
   * exchange context.
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getPropertyNamesTest() throws Fault {
    final AtomicInteger counter = new AtomicInteger(0);
    ContextProvider provider = new GetPropertyNamesProvider(counter);
    ContextProvider provider2 = new GetPropertyNamesProvider(counter) {
    };

    Invocation invocation = buildInvocation(provider, provider2);
    Response response = invoke(invocation);

    String body = response.readEntity(String.class);
    assertContains(body, "PROPERTY1");
    assertContains(body, "PROPERTY2");
  }

  /*
   * @testName: getPropertyNamesIsImmutableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:978; JAXRS:JAVADOC:453; JAXRS:JAVADOC:455;
   * JAXRS:JAVADOC:456; JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Returns an immutable collection containing the property
   * names available within the context of the current request/response exchange
   * context.
   * 
   * Binds an object to a given property name in the current request/response
   * exchange context.
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getPropertyNamesIsImmutableTest() throws Fault {
    final AtomicInteger counter = new AtomicInteger(0);
    ContextProvider provider = new GetPropertyNamesIsImmutableProvider(counter);

    Invocation invocation = buildInvocation(provider);
    Response response = invoke(invocation);
    String body = response.readEntity(String.class);
    assertEqualsInt(0, counter.get(),
        "getPropertyNames collection is not immutable");
    assertEquals("0", body, "getPropertyNames collection is not immutable");
    logMsg("getPropertyNames is immutable as expected");
  }

  /*
   * @testName: getStringHeadersTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:446; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Get a string view of header values associated with the
   * message.
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getStringHeadersTest() throws Fault {
    final String TCK = "cts";
    final String DATE = "Tue, 15 Nov 1994 08:12:31 GMT";
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        MultivaluedMap<String, String> map;
        map = context.getStringHeaders();
        StringBuilder value = new StringBuilder();
        value.append(map.getFirst("Accept")).append(" ");
        value.append(map.getFirst("tck")).append(" ");
        value.append(map.getFirst("Date"));
        Response r = Response.ok(value.toString()).build();
        context.abortWith(r);
      }
    };
    Invocation invocation = buildBuilder(provider)
        .header("Accept", MediaType.TEXT_HTML).header("tck", TCK)
        .header("Date", DATE).buildGet();
    Response response = invoke(invocation);
    String body = response.readEntity(String.class);
    assertContains(body, MediaType.TEXT_HTML);
    assertContains(body, TCK);
    assertContains(body, DATE);
  }

  /*
   * @testName: getStringHeadersReflectsTheUnderlayingMapTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:446; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Get a string view of header values associated with the
   * message. Changes in the underlying headers map are reflected in this view.
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getStringHeadersReflectsTheUnderlayingMapTest() throws Fault {
    final String TCK = "cts";
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        context.getHeaders().add(TCK, TCK);
        MultivaluedMap<String, String> map;
        map = context.getStringHeaders();
        String value = map.getFirst(TCK);
        Response r = Response.ok(value).build();
        context.abortWith(r);
      }
    };
    Invocation invocation = buildBuilder(provider).buildGet();
    Response response = invoke(invocation);
    String body = response.readEntity(String.class);
    assertContains(body, TCK);
  }

  /*
   * @testName: getStringHeadersUsingHeaderDelegateTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:446; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Get a string view of header values associated with the
   * message. The method converts the non-string header values to strings using
   * a RuntimeDelegate.HeaderDelegate
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getStringHeadersUsingHeaderDelegateTest() throws Fault {
    final String TCK = "cts";
    final StringBean bean = new StringBean(TCK);
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        MultivaluedMap<String, String> map;
        map = context.getStringHeaders();
        StringBuilder value = new StringBuilder();
        value.append(map.getFirst(TCK));
        Response r = Response.ok(value.toString()).build();
        context.abortWith(r);
      }
    };
    RuntimeDelegate delegate = RuntimeDelegate.getInstance();
    RuntimeDelegate.setInstance(new StringBeanRuntimeDelegate(delegate));
    try {
      Invocation invocation = buildBuilder(provider).header(TCK, bean)
          .buildGet();
      Response response = invoke(invocation);
      String body = response.readEntity(String.class);
      assertContains(body, TCK);
    } finally {
      RuntimeDelegate.setInstance(delegate);
      StringBeanRuntimeDelegate.assertNotStringBeanRuntimeDelegate();
    }
  }

  /*
   * @testName: getUriTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:447; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Get the request URI.
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void getUriTest() throws Fault {
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        URI uri = context.getUri();
        String entity = uri.toASCIIString();
        Response r = Response.ok(entity).build();
        context.abortWith(r);
      }
    };

    Invocation invocation = buildInvocation(provider);
    Response response = invoke(invocation);

    String body = response.readEntity(String.class);
    assertContains(body, getUrl());
  }

  /*
   * @testName: hasEntityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:448; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy: Check if there is an entity available in the request. The
   * method returns true if the entity is present, returns false otherwise.
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void hasEntityTest() throws Fault {
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext context)
          throws Fault {
        boolean has = context.hasEntity();
        String entity = String.valueOf(has);
        Response r = Response.ok(entity).build();
        context.abortWith(r);
      }
    };

    Invocation invocation = buildInvocation(provider);
    Response response = invoke(invocation);
    String body = response.readEntity(String.class);
    assertContains(body, "false");

    Entity<String> entity = createEntity("TEST");
    WebTarget target = buildTarget(provider);
    invocation = target.request().buildPost(entity);
    response = invoke(invocation);
    body = response.readEntity(String.class);
    assertContains(body, "true");
  }

  /*
   * @testName: removePropertyTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:449; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456;
   * JAXRS:SPEC:85; JAXRS:JAVADOC:427;
   * 
   * @test_Strategy:Removes a property with the given name from the current
   * request/response exchange context. After removal, subsequent calls to
   * getProperty(java.lang.String) to retrieve the property value will return
   * null.
   *
   * ClientRequestFilter.abortWith
   */
  @Test
  public void removePropertyTest() throws Fault {
    final AtomicInteger counter = new AtomicInteger(0);
    ContextProvider provider = new RemovePropertyProvider(counter);
    ContextProvider provider2 = new RemovePropertyProvider(counter) {
    };
    ContextProvider provider3 = new RemovePropertyProvider(counter) {
    };

    Invocation invocation = buildInvocation(provider, provider2, provider3);
    Response response = invoke(invocation);

    String body = response.readEntity(String.class);
    assertContains(body, "NULL");
  }

  /*
   * @testName: setEntityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:450; JAXRS:JAVADOC:434; JAXRS:JAVADOC:435;
   * JAXRS:JAVADOC:438; JAXRS:JAVADOC:455; JAXRS:JAVADOC:456; JAXRS:SPEC:85;
   * 
   * @test_Strategy: Set a new response message entity. It is the callers
   * responsibility to wrap the actual entity with
   * jakarta.ws.rs.core.GenericEntity if preservation of its generic type is
   * required.
   * 
   * ClientRequestFilter.abortWith
   */
  @Test
  public void setEntityTest() throws Fault {
    final AtomicInteger counter = new AtomicInteger(0);

    ContextProvider provider = new SetEntityProvider(counter);
    ContextProvider provider2 = new SetEntityProvider(counter) {
    };

    Entity<ByteArrayInputStream> entity = createEntity(
        new ByteArrayInputStream("test".getBytes()));

    WebTarget target = buildTarget(provider, provider2);
    Invocation invocation = target.request().buildPost(entity);
    Response response = invoke(invocation);

    assertStatus(response, Status.OK);
  }

  // ///////////////////////////////////////////////////////////////////////
  /**
   * Call given provider CheckContextFilter method
   */
  protected static Response invoke(Invocation i) throws Fault {
    Response r = null;
    try {
      r = i.invoke();
    } catch (Exception e) {
      Object cause = e.getCause();
      if (cause instanceof Fault)
        throw (Fault) cause;
      else
        throw new Fault(e);
    }
    return r;
  }

  protected static Invocation buildInvocation(ContextProvider... provider) {
    WebTarget target = buildTarget(provider);
    Invocation i = target.request().buildGet();
    return i;
  }

  protected static WebTarget buildTarget(ContextProvider... providers) {
    Client client = ClientBuilder.newClient();
    for (ContextProvider provider : providers)
      client.register(provider);
    WebTarget target = client.target(getUrl());
    return target;
  }

  protected static Invocation.Builder buildBuilder(
      ContextProvider... provider) {
    Invocation.Builder builder = buildTarget(provider).request();
    return builder;
  }

  protected static void assertStatus(Response r, Status status) throws Fault {
    assertTrue(r.getStatus() == status.getStatusCode(), "Expected " +
        status.getStatusCode() + " got " + r.getStatus());
    TestUtil.logMsg("Found expected status: " + status.getStatusCode());
  }

  protected static void assertContains(String string, String substring)
      throws Fault {
    assertTrue(string.contains(substring), string + " does NOT contain " +
        substring + ", it is: " + string);
    TestUtil.logMsg("Found expected substring: " + substring);
  }

  /**
   * @return any possible url
   */
  protected static String getUrl() {
    return "http://localhost:8080/404URL/";
  }

  protected <T> Entity<T> createEntity(T entity) {
    return Entity.entity(entity, MediaType.WILDCARD_TYPE);
  }
}
