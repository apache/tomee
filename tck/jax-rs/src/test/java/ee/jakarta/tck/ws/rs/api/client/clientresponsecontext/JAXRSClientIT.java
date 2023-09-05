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

package ee.jakarta.tck.ws.rs.api.client.clientresponsecontext;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.Link.Builder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.Response.Status.Family;
import jakarta.ws.rs.core.Response.StatusType;
import jakarta.ws.rs.ext.RuntimeDelegate;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.common.provider.StringBean;
import ee.jakarta.tck.ws.rs.common.provider.StringBeanRuntimeDelegate;
import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
public class JAXRSClientIT extends JAXRSCommonClient {

  private static final long serialVersionUID = -9134505693194656037L;

  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : "+testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : "+testInfo.getDisplayName());
  }

  /*
   * @testName: getAllowedMethodsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:457; JAXRS:JAVADOC:479; JAXRS:JAVADOC:480;
   * 
   * @test_Strategy: Get the allowed HTTP methods from the Allow HTTP header.
   * All methods will returned as upper case strings.
   * 
   * ClientResponseFilter.filter
   */
  @Test
  public void getAllowedMethodsTest() throws Fault {
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext requestContext,
          ClientResponseContext responseContext) throws Fault {
        Set<String> map = responseContext.getAllowedMethods();
        logMsg("found methods:", JaxrsUtil.iterableToString(" ", map));
        assertTrue(map.size() == 2, "Allowed mthods were not set");
        assertTrue(map.contains("OPTIONS"),
            "OPTIONS allowed method were not found");
        assertTrue(map.contains("GET"), "GET allowed method was not found");
      }
    };
    Response response = Response.ok().header(HttpHeaders.ALLOW, "get")
        .header(HttpHeaders.ALLOW, "options").build();
    invokeWithResponseAndAssertStatus(response, Status.OK, provider);
  }

  /*
   * @testName: getCookiesTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:458; JAXRS:JAVADOC:479; JAXRS:JAVADOC:480;
   * 
   * @test_Strategy: Get any new cookies set on the response message.
   * 
   * ClientResponseFilter.filter
   */
  @Test
  public void getCookiesTest() throws Fault {
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext requestContext,
          ClientResponseContext responseContext) throws Fault {
        Map<String, NewCookie> map = responseContext.getCookies();
        assertTrue(map.size() == 2, "Cookies were not set");
      }
    };
    NewCookie cookie1 = new NewCookie("cookie1", "cookie1");
    NewCookie cookie2 = new NewCookie("cookie2", "cookie2");
    Response response = Response.ok().cookie(cookie1).cookie(cookie2).build();
    invokeWithResponseAndAssertStatus(response, Status.OK, provider);
  }

  /*
   * @testName: getDateTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:459; JAXRS:JAVADOC:479; JAXRS:JAVADOC:480;
   * 
   * @test_Strategy: Get message date.
   * 
   * ClientResponseFilter.filter
   */
  @Test
  public void getDateTest() throws Fault {
    final Date date = getNewDate();
    ContextProvider in = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext requestContext,
          ClientResponseContext responseContext) throws Fault {
        assertTrue(date.equals(responseContext.getDate()), "The #getDate " +
            responseContext.getDate() +
            " is not equal to what is inserted to the response " + date);
        logMsg("Found #getDate()=", responseContext.getDate());
      }
    };
    Response response = Response.ok().header("Date", date).build();
    invokeWithResponseAndAssertStatus(response, Status.OK, in);
  }

  /*
   * @testName: getEntityStreamTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:460; JAXRS:JAVADOC:479; JAXRS:JAVADOC:480;
   * 
   * @test_Strategy: Get the entity input stream
   * 
   * ClientResponseFilter.filter
   */
  @Test
  public void getEntityStreamTest() throws Fault {
    final String entity = "ENTITY";
    ContextProvider in = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext requestContext,
          ClientResponseContext responseContext) throws Fault {
        InputStream stream = responseContext.getEntityStream();
        assertTrue(stream != null, "the #getEntityStream is null");
        InputStreamReader isr = new InputStreamReader(stream);
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        try {
          line = br.readLine();
        } catch (IOException e) {
          throw new Fault(e);
        } finally {
          try {
            br.close();
          } catch (IOException e) {
          }
        }
        assertTrue(entity.equals(line), "The #getEntityStream " + line +
            " is not equal to what is inserted to the response: " + entity);
        logMsg("Found #getEntityStream()=", line);
        // for next reading
        responseContext
            .setEntityStream(new ByteArrayInputStream(entity.getBytes()));
      }
    };
    Response response = Response.ok(entity).build();
    invokeWithResponseAndAssertStatus(response, Status.OK, in);
  }

  /*
   * @testName: getEntityTagTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:461; JAXRS:JAVADOC:479; JAXRS:JAVADOC:480;
   * 
   * @test_Strategy: Get the entity tag.
   * 
   * ClientResponseFilter.filter
   */
  @Test
  public void getEntityTagTest() throws Fault {
    final String value = "EntityTagValue";
    final EntityTag tag = new EntityTag(value);
    ContextProvider in = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext requestContext,
          ClientResponseContext responseContext) throws Fault {
        EntityTag etag = responseContext.getEntityTag();
        assertTrue(etag != null, "the #getEntityTag is null");
        assertTrue(value.equals(etag.getValue()), "The #getEntityTag " +
            etag.getValue() +
            " is not equal to what is inserted to the response: " + value);
        logMsg("Found #getEntityTag()=", value);
      }
    };
    Response response = Response.ok().tag(tag).build();
    invokeWithResponseAndAssertStatus(response, Status.OK, in);
  }

  /*
   * @testName: getHeadersTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:462; JAXRS:JAVADOC:479; JAXRS:JAVADOC:480;
   * 
   * @test_Strategy: Get the mutable response headers multivalued map.
   *
   * ClientResponseFilter.filter
   */
  @Test
  public void getHeadersTest() throws Fault {
    final String header1 = "header1";
    final String value1 = "value1";
    final String header2 = "header2";
    final String value2 = "value2";
    ContextProvider in = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext requestContext,
          ClientResponseContext responseContext) throws Fault {
        MultivaluedMap<String, String> headers = responseContext.getHeaders();
        assertTrue(headers != null, "the #getHeaders is null");
        assertTrue(headers.size() == 2, "the #getHeaders size is " +
            headers.size() + " expected 2");
        assertTrue(value1.equals(headers.getFirst(header1)),
            "#getHeaders was supposed to contain" + header1 + ":" + value1 +
            " header, but " + header1 + " is " + headers.getFirst(header1));
        logMsg("Found #getHeaders()={", header1, ":", value1, "}");
      }
    };
    Response response = Response.ok().header(header1, value1)
        .header(header2, value2).build();
    invokeWithResponseAndAssertStatus(response, Status.OK, in);
  }

  /*
   * @testName: getHeadersIsMutableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:462; JAXRS:JAVADOC:479; JAXRS:JAVADOC:480;
   * 
   * @test_Strategy: Get the mutable response headers multivalued map.
   * 
   * ClientResponseFilter.filter
   */
  @Test
  public void getHeadersIsMutableTest() throws Fault {
    final String header1 = "header1";
    final String value1 = "value1";
    ContextProvider in = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext requestContext,
          ClientResponseContext responseContext) throws Fault {
        MultivaluedMap<String, String> headers = responseContext.getHeaders();
        headers.add(header1, value1);
        headers = responseContext.getHeaders();
        assertTrue(headers != null, "the #getHeaders is null");
        assertTrue(headers.size() == 1, "the #getHeaders size is " +
            headers.size() + " expected 1");
        assertTrue(value1.equals(headers.getFirst(header1)),
            "#getHeaders was supposed to contain " + header1 + ":" + value1 +
            " header, but " + header1 + " is " + headers.getFirst(header1));
        logMsg("#getHeaders is mutable as expected");
      }
    };
    Response response = Response.ok().build();
    invokeWithResponseAndAssertStatus(response, Status.OK, in);
  }

  /*
   * @testName: getHeaderStringIsNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:463; JAXRS:JAVADOC:479; JAXRS:JAVADOC:480;
   * 
   * @test_Strategy: the message header value. If the message header is not
   * present then null is returned.
   * 
   * ClientResponseFilter.filter
   */
  @Test
  public void getHeaderStringIsNullTest() throws Fault {
    final String header1 = "header1";
    ContextProvider in = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext requestContext,
          ClientResponseContext responseContext) throws Fault {
        String header = responseContext.getHeaderString(header1);
        assertTrue(header == null, "the #getHeaderString is NOT null");
        logMsg("#getHeaderString is null as expected");
      }
    };
    Response response = Response.ok().build();
    invokeWithResponseAndAssertStatus(response, Status.OK, in);
  }

  /*
   * @testName: getHeaderStringIsEmptyTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:463; JAXRS:JAVADOC:479; JAXRS:JAVADOC:480;
   * 
   * @test_Strategy: the message header value. If the message header is present
   * but has no value then the empty string is returned.
   * 
   * ClientResponseFilter.filter
   */
  @Test
  public <T> void getHeaderStringIsEmptyTest() throws Fault {
    final String header1 = "header1";
    RuntimeDelegate original = RuntimeDelegate.getInstance();
    RuntimeDelegate.setInstance(new NullStringBeanRuntimeDelegate(original));
    ContextProvider in = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext requestContext,
          ClientResponseContext responseContext) throws Fault {
        String header = responseContext.getHeaderString(header1);
        assertTrue(header != null, "the #getHeaderString is null");
        assertTrue(header.equals(""), "the #getHeaderString is NOT empty, but " +
            header);
        logMsg("#getHeaderString is empty string as expected");
      }
    };
    Response response = Response.ok().header(header1, new StringBean("aa"))
        .build();
    try {
      invokeWithResponseAndAssertStatus(response, Status.OK, in);
    } finally {
      RuntimeDelegate.setInstance(original);
      StringBeanRuntimeDelegate.assertNotStringBeanRuntimeDelegate();
    }
  }

  /*
   * @testName: getHeaderStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:463; JAXRS:JAVADOC:479; JAXRS:JAVADOC:480;
   * 
   * @test_Strategy: the message header value.
   * 
   * ClientResponseFilter.filter
   */
  @Test
  public void getHeaderStringTest() throws Fault {
    final String header1 = "header1";
    final String value1 = "value1";
    ContextProvider in = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext requestContext,
          ClientResponseContext responseContext) throws Fault {
        String header = responseContext.getHeaderString(header1);
        assertTrue(header != null, "the #getHeaderString is null");
        assertTrue(header.equals(value1), "the #getHeaderString=" + header +
            " differs from expected " + value1);
        logMsg("#getHeaderString is", value1, "as expected");
      }
    };
    Response response = Response.ok().header(header1, value1).build();
    invokeWithResponseAndAssertStatus(response, Status.OK, in);
  }

  /*
   * @testName: getHeaderStringIsCommaSeparatedTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:463; JAXRS:JAVADOC:479; JAXRS:JAVADOC:480;
   * 
   * @test_Strategy: the message header value. If the message header is present
   * more than once then the values of joined together and separated by a ','
   * character.
   * 
   * ClientResponseFilter.filter
   */
  @Test
  public void getHeaderStringIsCommaSeparatedTest() throws Fault {
    final String header1 = "header1";
    final String value1 = "value1";
    final String value2 = "value2";
    ContextProvider in = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext requestContext,
          ClientResponseContext responseContext) throws Fault {
        String header = responseContext.getHeaderString(header1);
        String value3 = value1 + "," + value2;
        String value4 = value2 + "," + value1;
        assertTrue(header != null, "the #getHeaderString is null");
        assertTrue(header.equals(value3) || header.equals(value4),
            "the #getHeaderString=" + header +
            " differs from expected comma separated combination of " + value1 +
            " and " + value2);
        logMsg("#getHeaderString is comma separated combination of", value1,
            "and", value2, "as expected");
      }
    };
    Response response = Response.ok().header(header1, value1)
        .header(header1, value2).build();
    invokeWithResponseAndAssertStatus(response, Status.OK, in);
  }

  /*
   * @testName: getLanguageTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:464; JAXRS:JAVADOC:479; JAXRS:JAVADOC:480;
   * 
   * @test_Strategy: Get the language of the entity.
   * 
   * ClientResponseFilter.filter
   */
  @Test
  public void getLanguageTest() throws Fault {
    final Locale language = Locale.CANADA_FRENCH;
    ContextProvider in = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext requestContext,
          ClientResponseContext responseContext) throws Fault {
        Locale responseLanguage = responseContext.getLanguage();
        assertTrue(responseLanguage != null, "the #getLanguage is null");
        assertTrue(language.equals(responseLanguage),
            "#getLanguage was supposed to be " + language + " but was " +
            responseLanguage);
        logMsg("Found #getLanguage()=", responseLanguage);
      }
    };
    Response response = Response.ok("entity").language(language).build();
    invokeWithResponseAndAssertStatus(response, Status.OK, in);
  }

  /*
   * @testName: getLastModifiedTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:465; JAXRS:JAVADOC:479; JAXRS:JAVADOC:480;
   * 
   * @test_Strategy: Get the last modified date.
   * 
   * ClientResponseFilter.filter
   */
  @Test
  public void getLastModifiedTest() throws Fault {
    final Date lastModified = getNewDate();
    ContextProvider in = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext requestContext,
          ClientResponseContext responseContext) throws Fault {
        Date date = responseContext.getLastModified();
        assertTrue(date != null, "the #getLastModified is null");
        assertTrue(lastModified.equals(date),
            "#getLastModified was supposed to be " + lastModified + " but was " +
            date);
        logMsg("Found #getLastModified()=", date);
      }
    };
    Response response = Response.ok().lastModified(lastModified).build();
    invokeWithResponseAndAssertStatus(response, Status.OK, in);
  }

  /*
   * @testName: getLengthTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:466; JAXRS:JAVADOC:479; JAXRS:JAVADOC:480;
   * 
   * @test_Strategy: Get Content-Length value.
   * 
   * ClientResponseFilter.filter
   */
  @Test
  public void getLengthTest() throws Fault {
    final String entity = "ENTITY";
    ContextProvider in = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext requestContext,
          ClientResponseContext responseContext) throws Fault {
        int len = responseContext.getLength();
        assertTrue(len == entity.length(), "#getLength was supposed to be " +
            entity.length() + " but was " + len);
        logMsg("Found #getLength()=", len);
      }
    };
    Response response = Response.ok()
        .header(HttpHeaders.CONTENT_LENGTH, entity.length()).build();
    invokeWithResponseAndAssertStatus(response, Status.OK, in);
  }

  /*
   * @testName: getLinkTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:467; JAXRS:JAVADOC:479; JAXRS:JAVADOC:480;
   * 
   * @test_Strategy: Get the link for the relation.
   * 
   * ClientResponseFilter.filter
   */
  @Test
  public void getLinkTest() throws Fault {
    final String rel = "RELATION";
    ContextProvider in = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext requestContext,
          ClientResponseContext responseContext) throws Fault {
        Link link = responseContext.getLink(rel);
        assertTrue(link != null, "the #getLink is null");
        assertTrue(link.getUri() != null, "the #getLink.getUri is null");
        assertTrue(link.getUri().toASCIIString().contains(getUrl()),
            "#getLink was supposed to contain " + getUrl() + " but was " +
            link.getUri().toASCIIString());
        logMsg("Found #getLink()=", link.getUri().toASCIIString());
      }
    };
    Response response = Response.ok().link(getUrl(), rel).build();
    invokeWithResponseAndAssertStatus(response, Status.OK, in);
  }

  /*
   * @testName: getLinkBuilderTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:468; JAXRS:JAVADOC:479; JAXRS:JAVADOC:480;
   * 
   * @test_Strategy: Convenience method that returns a
   * jakarta.ws.rs.core.Link.Builder for the relation. ClientResponseFilter.filter
   */
  @Test
  public void getLinkBuilderTest() throws Fault {
    final String rel = "RELATION";
    ContextProvider in = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext requestContext,
          ClientResponseContext responseContext) throws Fault {
        Builder builder = responseContext.getLinkBuilder(rel);
        assertTrue(builder != null, "the #getLinkBuilder is null");
        assertTrue(builder.build().getUri().toASCIIString().contains(getUrl()),
            "#getLinkBuilder.build was supposed to contain " + getUrl() +
            " but was " + builder.build().getUri().toASCIIString());
        logMsg("Found #getLinkBuilder()=",
            builder.build().getUri().toASCIIString());
      }
    };
    Response response = Response.ok().link(getUrl(), rel).build();
    invokeWithResponseAndAssertStatus(response, Status.OK, in);
  }

  /*
   * @testName: getLinksTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:469; JAXRS:JAVADOC:479; JAXRS:JAVADOC:480;
   * 
   * @test_Strategy: Get the links attached to the message as header.
   * 
   * ClientResponseFilter.filter
   */
  @Test
  public void getLinksTest() throws Fault {
    final Link link = Link.fromUri(getUrl()).build();
    ContextProvider in = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext requestContext,
          ClientResponseContext responseContext) throws Fault {
        Set<Link> links = responseContext.getLinks();
        assertTrue(links != null, "the #getLinks is null");
        assertTrue(links.size() == 1,
            "the links was supposed to be of size 1, was " + links.size());
        assertTrue(links.contains(link), "#getLinks was supposed to contain " +
            link.getUri().toASCIIString());
        logMsg("Found #getLinks()={", link.getUri().toASCIIString(), "}");
      }
    };
    Response response = Response.ok().links(link).build();
    invokeWithResponseAndAssertStatus(response, Status.OK, in);
  }

  /*
   * @testName: getLocationTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:470; JAXRS:JAVADOC:479; JAXRS:JAVADOC:480;
   * 
   * @test_Strategy: Get the location. ClientResponseFilter.filter
   */
  @Test
  public void getLocationTest() throws Fault {
    URI uri = null;
    try {
      uri = new URI(getUrl());
    } catch (URISyntaxException e) {
      throw new Fault(e);
    }
    final URI location = uri;
    ContextProvider in = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext requestContext,
          ClientResponseContext responseContext) throws Fault {
        URI responseLocation = responseContext.getLocation();
        assertTrue(responseLocation != null, "the #getLinks is null");
        assertTrue(location.equals(responseLocation),
            "#getLocation was supposed to be " + location + " but was " +
            responseLocation);
        logMsg("Found #getLocation=", location.toASCIIString());
      }
    };
    Response response = Response.ok().location(location).build();
    invokeWithResponseAndAssertStatus(response, Status.OK, in);
  }

  /*
   * @testName: getMediaTypeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:471; JAXRS:JAVADOC:479; JAXRS:JAVADOC:480;
   * 
   * @test_Strategy: Get the media type of the entity.
   * 
   * ClientResponseFilter.filter
   */
  @Test
  public void getMediaTypeTest() throws Fault {
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext requestContext,
          ClientResponseContext responseContext) throws Fault {
        MediaType type = responseContext.getMediaType();
        assertTrue(MediaType.APPLICATION_SVG_XML_TYPE.equals(type),
            "Unexpected mediatype found " + type);
        TestUtil.logMsg("Found expected MediaType.APPLICATION_SVG_XML_TYPE");
      }
    };
    Response response = Response.ok("TEST", MediaType.APPLICATION_SVG_XML)
        .build();
    invokeWithResponseAndAssertStatus(response, Status.OK, provider);
  }

  /*
   * @testName: getStatusTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:472; JAXRS:JAVADOC:479; JAXRS:JAVADOC:480;
   * 
   * @test_Strategy: Get the status code associated with the response.
   * ClientResponseFilter.filter
   */
  @Test
  public void getStatusTest() throws Fault {
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext requestContext,
          ClientResponseContext responseContext) throws Fault {
        assertTrue(responseContext.getStatus() == 222, "unexpected status " +
            responseContext.getStatus());
        TestUtil.logMsg("Found expected response status 222");
      }
    };
    Response response = Response.status(222).build();
    ClientRequestFilter filter = createRequestFilter(response);
    Invocation i = buildInvocation(filter, provider);
    Response r = invoke(i);
    assertTrue(r.getStatus() == 222, "unexpected status " + r.getStatus());
  }

  /*
   * @testName: getStatusInfoTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:473; JAXRS:JAVADOC:479; JAXRS:JAVADOC:480;
   * 
   * @test_Strategy: Get the status code associated with the response.
   * 
   * ClientResponseFilter.filter
   */
  @Test
  public void getStatusInfoTest() throws Fault {
    ContextProvider provider = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext requestContext,
          ClientResponseContext responseContext) throws Fault {
        assertTrue(responseContext.getStatusInfo().getStatusCode() == 222,
            "unexpected status " +
            responseContext.getStatusInfo().getStatusCode());
        TestUtil.logMsg("Found expected response status 222");
      }
    };
    Response response = Response.status(222).build();
    ClientRequestFilter filter = createRequestFilter(response);
    Invocation i = buildInvocation(filter, provider);
    Response r = invoke(i);
    assertTrue(r.getStatus() == 222, "unexpected status " + r.getStatus());
  }

  /*
   * @testName: hasEntityWhenEntityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:474; JAXRS:JAVADOC:479; JAXRS:JAVADOC:480;
   * 
   * @test_Strategy: Check if there is a non-empty entity input stream is
   * available in the response message. ClientResponseFilter.filter
   */
  @Test
  public void hasEntityWhenEntityTest() throws Fault {
    final String entity = "eNtitY";
    ContextProvider in = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext requestContext,
          ClientResponseContext responseContext) throws Fault {
        boolean has = responseContext.hasEntity();
        assertTrue(has, "the #hasEntity did not found the given entity");
        logMsg("Found #hasEntity()=true");
      }
    };
    Response response = Response.ok(entity).build();
    invokeWithResponseAndAssertStatus(response, Status.OK, in);
  }

  /*
   * @testName: hasEntityWhenNoEntityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:474; JAXRS:JAVADOC:479; JAXRS:JAVADOC:480;
   * 
   * @test_Strategy: Check if there is a non-empty entity input stream is
   * available in the response message.
   * 
   * ClientResponseFilter.filter
   */
  @Test
  public void hasEntityWhenNoEntityTest() throws Fault {
    ContextProvider in = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext requestContext,
          ClientResponseContext responseContext) throws Fault {
        boolean has = responseContext.hasEntity();
        assertTrue(!has, "the #hasEntity found some entity");
        logMsg("Found #hasEntity()=false");
      }
    };
    Response response = Response.ok().build();
    invokeWithResponseAndAssertStatus(response, Status.OK, in);
  }

  /*
   * @testName: hasLinkWhenLinkTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:475; JAXRS:JAVADOC:479; JAXRS:JAVADOC:480;
   * 
   * @test_Strategy: Check if link for relation exists.
   * ClientResponseFilter.filter
   */
  @Test
  public void hasLinkWhenLinkTest() throws Fault {
    final String rel = "RelatiOn";
    ContextProvider in = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext requestContext,
          ClientResponseContext responseContext) throws Fault {
        boolean has = responseContext.hasLink(rel);
        assertTrue(has, "the #hasLink did not found the given link");
        logMsg("#hasLink has found the given link");
      }
    };
    Response response = Response.ok().link(getUrl(), rel).build();
    invokeWithResponseAndAssertStatus(response, Status.OK, in);
  }

  /*
   * @testName: hasLinkWhenNoLinkTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:475; JAXRS:JAVADOC:479; JAXRS:JAVADOC:480;
   * 
   * @test_Strategy: Check if link for relation exists.
   * 
   * ClientResponseFilter.filter
   */
  @Test
  public void hasLinkWhenNoLinkTest() throws Fault {
    ContextProvider in = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext requestContext,
          ClientResponseContext responseContext) throws Fault {
        boolean has = responseContext.hasLink("rel");
        assertTrue(!has, "the #hasLink did found some link");
        logMsg("#hasLink has not found any link as expected");
      }
    };
    Response response = Response.ok().link(getUrl(), "ANY").build();
    invokeWithResponseAndAssertStatus(response, Status.OK, in);
  }

  /*
   * @testName: setEntityStreamTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:476; JAXRS:JAVADOC:479; JAXRS:JAVADOC:480;
   * 
   * @test_Strategy: Set a new entity input stream. ClientResponseFilter.filter
   */
  @Test
  public void setEntityStreamTest() throws Fault {
    final String entity = "ENTITY";
    ContextProvider in = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext requestContext,
          ClientResponseContext responseContext) throws Fault {
        responseContext
            .setEntityStream(new ByteArrayInputStream(entity.getBytes()));
        InputStream stream = responseContext.getEntityStream();
        assertTrue(stream != null, "the #getEntityStream is null");
        InputStreamReader isr = new InputStreamReader(stream);
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        try {
          line = br.readLine();
        } catch (IOException e) {
          throw new Fault(e);
        } finally {
          try {
            br.close();
          } catch (IOException e) {
          }
        }
        assertTrue(entity.equals(line), "The #getEntityStream " + line +
            " is not equal to what is inserted to the response: " + entity);
        logMsg("#setEntityStream(", entity, ") set entity", line);
      }
    };
    Response response = Response.ok().build();
    invokeWithResponseAndAssertStatus(response, Status.OK, in);
  }

  /*
   * @testName: setStatusTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:477; JAXRS:JAVADOC:479; JAXRS:JAVADOC:480;
   * 
   * @test_Strategy: Set a new response status code. ClientResponseFilter.filter
   */
  @Test
  public void setStatusTest() throws Fault {
    ContextProvider in = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext requestContext,
          ClientResponseContext responseContext) throws Fault {
        responseContext.setStatus(Status.FORBIDDEN.getStatusCode());
      }
    };
    Response response = Response.ok().build();
    invokeWithResponseAndAssertStatus(response, Status.FORBIDDEN, in);
  }

  /*
   * @testName: setStatusInfoTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:478; JAXRS:JAVADOC:479; JAXRS:JAVADOC:480;
   * 
   * @test_Strategy: Set the complete status information associated with the
   * response.
   * 
   * ClientResponseFilter.filter
   */
  @Test
  public void setStatusInfoTest() throws Fault {
    ContextProvider in = new ContextProvider() {
      @Override
      protected void checkFilterContext(ClientRequestContext requestContext,
          ClientResponseContext responseContext) throws Fault {
        StatusType info = new StatusType() {
          @Override
          public int getStatusCode() {
            return Status.FOUND.getStatusCode();
          }

          @Override
          public String getReasonPhrase() {
            return null;
          }

          @Override
          public Family getFamily() {
            return null;
          }
        };
        responseContext.setStatusInfo(info);
      }
    };
    Response response = Response.ok().build();
    invokeWithResponseAndAssertStatus(response, Status.FOUND, in);
  }

  // ///////////////////////////////////////////////////////////////////////
  protected static ClientRequestFilter createRequestFilter(
      final Response response) {
    ClientRequestFilter outFilter = new ClientRequestFilter() {

      @Override
      public void filter(ClientRequestContext context) throws IOException {
        // logMsg(" -- OUT FILTER --");
        Response r;
        if (response == null)
          r = Response.ok().build();
        else
          r = response;
        context.abortWith(r);
      }
    };
    return outFilter;
  }

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

  protected static Invocation buildInvocation(ClientRequestFilter requestFilter,
      ContextProvider... provider) {
    WebTarget target = buildTarget(requestFilter, provider);
    Invocation i = target.request().buildGet();
    return i;
  }

  protected static WebTarget buildTarget(ClientRequestFilter requestFilter,
      ContextProvider... providers) {
    Client client = ClientBuilder.newClient();
    client.register(requestFilter);
    for (ContextProvider provider : providers)
      client.register(provider);
    WebTarget target = client.target(getUrl());
    return target;
  }

  protected static void assertStatus(Response r, Status status) throws Fault {
    assertTrue(r.getStatus() == status.getStatusCode(), "Expected " +
        status.getStatusCode() + " got " + r.getStatus());
    TestUtil.logMsg("Found expected status: " + status.getStatusCode());
  }

  protected static void invokeWithResponseAndAssertStatus(Response response,
      Status status, ContextProvider provider) throws Fault {
    ClientRequestFilter filter = createRequestFilter(response);
    Invocation i = buildInvocation(filter, provider);
    Response r = invoke(i);
    assertStatus(r, status);
  }

  /**
   * @return any nonexistent URL
   */
  protected static String getUrl() {
    return "http://localhost:8080/404URL/";
  }

  protected static Date getNewDate() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.MILLISECOND, 0);
    Date date = calendar.getTime();
    return date;
  }
}
