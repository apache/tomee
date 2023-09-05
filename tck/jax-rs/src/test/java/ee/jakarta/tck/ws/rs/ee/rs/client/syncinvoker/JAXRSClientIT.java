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

package ee.jakarta.tck.ws.rs.ee.rs.client.syncinvoker;

import java.io.InputStream;
import java.io.IOException;

import ee.jakarta.tck.ws.rs.common.impl.TRACE;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;
import ee.jakarta.tck.ws.rs.common.client.JaxrsCommonClient;
import ee.jakarta.tck.ws.rs.common.client.JdkLoggingFilter;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.SyncInvoker;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

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
public class JAXRSClientIT extends JaxrsCommonClient {

  private static final long serialVersionUID = 4942772066511819511L;

  protected long millis;

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_ee_rs_client_syncinvoker_web/resource");
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

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/client/syncinvoker/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_rs_client_syncinvoker_web.war");
    archive.addClasses(TSAppConfig.class, Resource.class, TRACE.class);
    archive.setWebXML(new StringAsset(webXml));
    return archive;

  }


  static final String[] METHODS = { "DELETE", "GET", "OPTIONS" };

  static final String[] ENTITY_METHODS = { "PUT", "POST" };

  /* Run test */
  // --------------------------------------------------------------------
  // ---------------------- DELETE --------------------------------------
  // --------------------------------------------------------------------
  /*
   * @testName: deleteTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:541;
   * 
   * @test_Strategy: Invoke HTTP DELETE method for the current request
   * synchronously.
   */
  @Test
  public void deleteTest() throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("delete");
    Response response = sync.delete();
    assertResponseOk(response);
  }

  /*
   * @testName: deleteThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:541;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.delete throws
   * ProcessingException in case the invocation failed.
   */
  @Test
  public void deleteThrowsProcessingExceptionTest() throws Fault {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        SyncInvoker sync = createSyncInvokerWrongUrl();
        sync.delete();
      }
    };
    assertProcessingException(run);
  }

  /*
   * @testName: deleteWithStringClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:543;
   * 
   * @test_Strategy: Invoke HTTP DELETE method for the current request
   * synchronously.
   */
  @Test
  public void deleteWithStringClassTest() throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("delete");
    String response = sync.delete(String.class);
    assertResponseString(response, "delete");
  }

  /*
   * @testName: deleteWithResponseClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:543;
   * 
   * @test_Strategy: Invoke HTTP DELETE method for the current request
   * synchronously.
   */
  @Test
  public void deleteWithResponseClassTest() throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("delete");
    Response response = sync.delete(Response.class);
    assertResponseOk(response);
  }

  /*
   * @testName: deleteWithStringClassThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:543;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.delete( Class ) throws
   * ProcessingException in case the invocation failed.
   */
  @Test
  public void deleteWithStringClassThrowsProcessingExceptionTest()
      throws Fault {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        SyncInvoker sync = createSyncInvokerWrongUrl();
        sync.delete(String.class);
      }
    };
    assertProcessingException(run);
  }

  /*
   * @testName: deleteWithStringClassThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:543;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.delete( Class ) throws
   * WebApplicationException - in case the response status code of the response
   * returned by the server is not successful and the specified response type is
   * not Response.
   */
  @Test
  public void deleteWithStringClassThrowsWebApplicationExceptionTest()
      throws Fault {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        SyncInvoker sync = createSyncInvokerForMethod("deletenotok");
        sync.delete(String.class);
      }
    };
    assertWebApplicationException(run);
  }

  /*
   * @testName: deleteWithResponseClassThrowsNoWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:543;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.delete( Class ) throws
   * WebApplicationException - in case the response status code of the response
   * returned by the server is not successful and the specified response type is
   * not Response.
   */
  @Test
  public void deleteWithResponseClassThrowsNoWebApplicationExceptionTest()
      throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("deletenotok");
    Response response = sync.delete(Response.class);
    assertStatusAndLog(response, Status.NOT_ACCEPTABLE);
  }

  /*
   * @testName: deleteWithGenericTypeStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:546;
   * 
   * @test_Strategy: Invoke HTTP DELETE method for the current request
   * synchronously.
   */
  @Test
  public void deleteWithGenericTypeStringTest() throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("delete");
    GenericType<String> generic = createGeneric(String.class);
    String response = sync.delete(generic);
    assertResponseString(response, "delete");
  }

  /*
   * @testName: deleteWithGenericTypeResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:546;
   * 
   * @test_Strategy: Invoke HTTP DELETE method for the current request
   * synchronously.
   */
  @Test
  public void deleteWithGenericTypeResponseTest() throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("delete");
    GenericType<Response> generic = createGeneric(Response.class);
    Response response = sync.delete(generic);
    assertResponseOk(response);
  }

  /*
   * @testName: deleteWithGenericTypeStringThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:546;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.delete( Class ) throws
   * ProcessingException in case the invocation failed.
   */
  @Test
  public void deleteWithGenericTypeStringThrowsProcessingExceptionTest()
      throws Fault {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        SyncInvoker sync = createSyncInvokerWrongUrl();
        GenericType<String> generic = createGeneric(String.class);
        sync.delete(generic);
      }
    };
    assertProcessingException(run);
  }

  /*
   * @testName: deleteWithGenericTypeStringThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:546;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.delete( Class ) throws
   * WebApplicationException - in case the response status code of the response
   * returned by the server is not successful and the specified response type is
   * not Response.
   */
  @Test
  public void deleteWithGenericTypeStringThrowsWebApplicationExceptionTest()
      throws Fault {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        SyncInvoker sync = createSyncInvokerForMethod("deletenotok");
        GenericType<String> generic = createGeneric(String.class);
        sync.delete(generic);
      }
    };
    assertWebApplicationException(run);
  }

  /*
   * @testName: deleteWithGenericTypeResponseThrowsNoWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:546;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.delete( Class ) throws
   * WebApplicationException - in case the response status code of the response
   * returned by the server is not successful and the specified response type is
   * not Response.
   */
  @Test
  public void deleteWithGenericTypeResponseThrowsNoWebApplicationExceptionTest()
      throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("deletenotok");
    GenericType<Response> generic = createGeneric(Response.class);
    Response response = sync.delete(generic);
    assertStatusAndLog(response, Status.NOT_ACCEPTABLE);
  }

  // ------------------------------------------------------------------
  // ---------------------------GET------------------------------------
  // ------------------------------------------------------------------
  /*
   * @testName: getTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:549;
   * 
   * @test_Strategy: Invoke HTTP GET method for the current request
   * synchronously.
   */
  @Test
  public void getTest() throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("get");
    Response response = sync.get();
    assertResponseOk(response);
  }

  /*
   * @testName: getThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:549;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.get throws
   * ProcessingException in case the invocation failed.
   */
  @Test
  public void getThrowsProcessingExceptionTest() throws Fault {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        SyncInvoker sync = createSyncInvokerWrongUrl();
        sync.get();
      }
    };
    assertProcessingException(run);
  }

  /*
   * @testName: getWithStringClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:551;
   * 
   * @test_Strategy: Invoke HTTP GET method for the current request
   * synchronously.
   */
  @Test
  public void getWithStringClassTest() throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("get");
    String response = sync.get(String.class);
    assertResponseString(response, "get");
  }

  /*
   * @testName: getWithResponseClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:551;
   * 
   * @test_Strategy: Invoke HTTP GET method for the current request
   * synchronously.
   */
  @Test
  public void getWithResponseClassTest() throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("get");
    Response response = sync.get(Response.class);
    assertResponseOk(response);
  }

  /*
   * @testName: getWithStringClassThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:551;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.get( Class ) throws
   * ProcessingException in case the invocation failed.
   */
  @Test
  public void getWithStringClassThrowsProcessingExceptionTest() throws Fault {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        SyncInvoker sync = createSyncInvokerWrongUrl();
        sync.get(String.class);
      }
    };
    assertProcessingException(run);
  }

  /*
   * @testName: getWithStringClassThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:551;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.get( Class ) throws
   * WebApplicationException - in case the response status code of the response
   * returned by the server is not successful and the specified response type is
   * not Response.
   */
  @Test
  public void getWithStringClassThrowsWebApplicationExceptionTest()
      throws Fault {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        SyncInvoker sync = createSyncInvokerForMethod("getnotok");
        sync.get(String.class);
      }
    };
    assertWebApplicationException(run);
  }

  /*
   * @testName: getWithResponseClassThrowsNoWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:551;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.get( Class ) throws
   * WebApplicationException - in case the response status code of the response
   * returned by the server is not successful and the specified response type is
   * not Response.
   */
  @Test
  public void getWithResponseClassThrowsNoWebApplicationExceptionTest()
      throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("getnotok");
    Response response = sync.get(Response.class);
    assertStatusAndLog(response, Status.NOT_ACCEPTABLE);
  }

  /*
   * @testName: getWithGenericTypeStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:554;
   * 
   * @test_Strategy: Invoke HTTP GET method for the current request
   * synchronously.
   */
  @Test
  public void getWithGenericTypeStringTest() throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("get");
    GenericType<String> generic = createGeneric(String.class);
    String response = sync.get(generic);
    assertResponseString(response, "get");
  }

  /*
   * @testName: getWithGenericTypeResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:554;
   * 
   * @test_Strategy: Invoke HTTP GET method for the current request
   * synchronously.
   */
  @Test
  public void getWithGenericTypeResponseTest() throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("get");
    GenericType<Response> generic = createGeneric(Response.class);
    Response response = sync.get(generic);
    assertResponseOk(response);
  }

  /*
   * @testName: getWithGenericTypeStringThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:554;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.get( GenericType ) throws
   * ProcessingException in case the invocation failed.
   */
  @Test
  public void getWithGenericTypeStringThrowsProcessingExceptionTest()
      throws Fault {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        SyncInvoker sync = createSyncInvokerWrongUrl();
        GenericType<String> generic = createGeneric(String.class);
        sync.get(generic);
      }
    };
    assertProcessingException(run);
  }

  /*
   * @testName: getWithGenericTypeStringThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:554;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.get( GenericType ) throws
   * WebApplicationException - in case the response status code of the response
   * returned by the server is not successful and the specified response type is
   * not Response.
   */
  @Test
  public void getWithGenericTypeStringThrowsWebApplicationExceptionTest()
      throws Fault {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        SyncInvoker sync = createSyncInvokerForMethod("getnotok");
        GenericType<String> generic = createGeneric(String.class);
        sync.get(generic);
      }
    };
    assertWebApplicationException(run);
  }

  /*
   * @testName: getWithGenericTypeResponseThrowsNoWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:554;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.get( GenericType ) throws
   * WebApplicationException - in case the response status code of the response
   * returned by the server is not successful and the specified response type is
   * not Response.
   */
  @Test
  public void getWithGenericTypeResponseThrowsNoWebApplicationExceptionTest()
      throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("getnotok");
    GenericType<Response> generic = createGeneric(Response.class);
    Response response = sync.get(generic);
    assertStatusAndLog(response, Status.NOT_ACCEPTABLE);
  }

  // ------------------------------------------------------------------
  // ---------------------------HEAD-----------------------------------
  // ------------------------------------------------------------------

  /*
   * @testName: headTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:557;
   * 
   * @test_Strategy: Invoke HTTP HEAD method for the current request
   * synchronously.
   */
  @Test
  public void headTest() throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("head");
    Response response = sync.head();
    Status status = Status.fromStatusCode(response.getStatus());
    assertTrue(status == Status.OK || status == Status.NO_CONTENT,
        "Incorrect status for head received");
  }

  /*
   * @testName: headThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:557;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.head throws
   * ProcessingException in case the invocation failed.
   */
  @Test
  public void headThrowsProcessingExceptionTest() throws Fault {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        SyncInvoker sync = createSyncInvokerWrongUrl();
        sync.head();
      }
    };
    assertProcessingException(run);
  }

  // ------------------------------------------------------------------
  // ---------------------------METHOD-----------------------------------
  // ------------------------------------------------------------------

  /*
   * @testName: methodTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:559;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * synchronously.
   */
  @Test
  public void methodTest() throws Fault {
    Response response = null;
    for (String method : METHODS) {
      SyncInvoker sync = createSyncInvokerForMethod(method.toLowerCase());
      response = sync.method(method);
      assertResponseOk(response);
    }
  }

  /*
   * @testName: methodThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:559;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.method(String) throws
   * ProcessingException in case the invocation failed.
   */
  @Test
  public void methodThrowsProcessingExceptionTest() throws Fault {
    for (final String method : METHODS) {
      Runnable run = new Runnable() {
        @Override
        public void run() {
          SyncInvoker sync = createSyncInvokerWrongUrl();
          sync.method(method);
        }
      };
      assertProcessingException(run);
    }
  }

  /*
   * @testName: methodWithStringClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:561;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * synchronously.
   */
  @Test
  public void methodWithStringClassTest() throws Fault {
    String response = null;
    for (String method : METHODS) {
      SyncInvoker sync = createSyncInvokerForMethod(method.toLowerCase());
      response = sync.method(method, String.class);
      assertResponseString(response, method.toLowerCase());
    }
  }

  /*
   * @testName: methodWithResponseClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:561;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * synchronously.
   */
  @Test
  public void methodWithResponseClassTest() throws Fault {
    Response response = null;
    for (String method : METHODS) {
      SyncInvoker sync = createSyncInvokerForMethod(method.toLowerCase());
      response = sync.method(method, Response.class);
      assertResponseOk(response);
    }
  }

  /*
   * @testName: methodWithStringClassThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:561;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.method(String) throws
   * WebApplicationException - in case the response status code of the response
   * returned by the server is not successful and the specified response type is
   * not Response.
   */
  @Test
  public void methodWithStringClassThrowsProcessingExceptionTest()
      throws Fault {
    for (final String method : METHODS) {
      Runnable run = new Runnable() {
        @Override
        public void run() {
          SyncInvoker sync = createSyncInvokerWrongUrl();
          sync.method(method, String.class);
        }
      };
      assertProcessingException(run);
    }
  }

  /*
   * @testName: methodWithStringClassThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:561;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.method(String) throws
   * WebApplicationException - in case the response status code of the response
   * returned by the server is not successful and the specified response type is
   * not Response.
   */
  @Test
  public void methodWithStringClassThrowsWebApplicationExceptionTest()
      throws Fault {
    for (final String method : METHODS) {
      Runnable run = new Runnable() {
        @Override
        public void run() {
          SyncInvoker sync = createSyncInvokerForMethod(
              method.toLowerCase() + "notok");
          sync.method(method, String.class);
        }
      };
      assertWebApplicationException(run);
    }
  }

  /*
   * @testName: methodWithResponseClassThrowsNoWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:561;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.method(String) throws
   * ProcessingException in case the invocation failed.
   */
  @Test
  public void methodWithResponseClassThrowsNoWebApplicationExceptionTest()
      throws Fault {
    for (final String method : METHODS) {
      SyncInvoker sync = createSyncInvokerForMethod(
          method.toLowerCase() + "notok");
      Response response = sync.method(method, Response.class);
      assertStatusAndLog(response, Status.NOT_ACCEPTABLE);
    }
  }

  /*
   * @testName: methodWithGenericTypeStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:564;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * synchronously.
   */
  @Test
  public void methodWithGenericTypeStringTest() throws Fault {
    GenericType<String> generic = createGeneric(String.class);
    String response = null;
    for (String method : METHODS) {
      SyncInvoker sync = createSyncInvokerForMethod(method.toLowerCase());
      response = sync.method(method, generic);
      assertResponseString(response, method.toLowerCase());
    }
  }

  /*
   * @testName: methodWithGenericTypeResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:564;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * synchronously.
   */
  @Test
  public void methodWithGenericTypeResponseTest() throws Fault {
    GenericType<Response> generic = createGeneric(Response.class);
    Response response = null;
    for (String method : METHODS) {
      SyncInvoker sync = createSyncInvokerForMethod(method.toLowerCase());
      response = sync.method(method, generic);
      assertResponseOk(response);
    }
  }

  /*
   * @testName: methodWithGenericTypeStringThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:564;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.method(String) throws
   * ProcessingException in case the invocation failed.
   */
  @Test
  public void methodWithGenericTypeStringThrowsProcessingExceptionTest()
      throws Fault {
    final GenericType<String> generic = createGeneric(String.class);
    for (final String method : METHODS) {
      Runnable run = new Runnable() {
        @Override
        public void run() {
          SyncInvoker sync = createSyncInvokerWrongUrl();
          sync.method(method, generic);
        }
      };
      assertProcessingException(run);
    }
  }

  /*
   * @testName: methodWithGenericTypeStringThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:564;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.method(GenericType<String>)
   * throws WebApplicationException - in case the response status code of the
   * response returned by the server is not successful and the specified
   * response type is not Response.
   */
  @Test
  public void methodWithGenericTypeStringThrowsWebApplicationExceptionTest()
      throws Fault {
    final GenericType<String> generic = createGeneric(String.class);
    for (final String method : METHODS) {
      Runnable run = new Runnable() {
        @Override
        public void run() {
          SyncInvoker sync = createSyncInvokerForMethod(
              method.toLowerCase() + "notok");
          sync.method(method, generic);
        }
      };
      assertWebApplicationException(run);
    }
  }

  /*
   * @testName: methodWithGenericTypeResponseThrowsNoWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:564;
   * 
   * @test_Strategy:
   * jakarta.ws.rs.client.SyncInvoker.method(GenericType<Response>) throws
   * WebApplicationException - in case the response status code of the response
   * returned by the server is not successful and the specified response type is
   * not Response.
   */
  @Test
  public void methodWithGenericTypeResponseThrowsNoWebApplicationExceptionTest()
      throws Fault {
    GenericType<Response> generic = createGeneric(Response.class);
    Response response = null;
    for (final String method : METHODS) {
      SyncInvoker sync = createSyncInvokerForMethod(
          method.toLowerCase() + "notok");
      response = sync.method(method, generic);
      assertStatusAndLog(response, Status.NOT_ACCEPTABLE);
    }
  }

  /*
   * @testName: methodWithEntityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:567;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * synchronously.
   */
  @Test
  public void methodWithEntityTest() throws Fault {
    Response response = null;
    for (String method : ENTITY_METHODS) {
      SyncInvoker sync = createSyncInvokerForMethod(method.toLowerCase());
      Entity<String> entity = createEntity(method.toLowerCase());
      response = sync.method(method, entity);
      assertResponseOk(response);
    }
  }

  /*
   * @testName: methodWithEntityThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:567;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.method(String, Entity)
   * throws ProcessingException in case the invocation failed.
   */
  @Test
  public void methodWithEntityThrowsProcessingExceptionTest() throws Fault {
    final Entity<String> entity = createEntity("entity");
    for (final String method : ENTITY_METHODS) {
      Runnable run = new Runnable() {
        @Override
        public void run() {
          SyncInvoker sync = createSyncInvokerWrongUrl();
          sync.method(method, entity);
        }
      };
      assertProcessingException(run);
    }
  }

  /*
   * @testName: methodWithStringClassWithEntityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:569;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * synchronously.
   */
  @Test
  public void methodWithStringClassWithEntityTest() throws Fault {
    String response = null;
    for (String method : ENTITY_METHODS) {
      SyncInvoker sync = createSyncInvokerForMethod(method.toLowerCase());
      Entity<String> entity = createEntity(method.toLowerCase());
      response = sync.method(method, entity, String.class);
      assertResponseString(response, method.toLowerCase());
    }
  }

  /*
   * @testName: methodWithResponseClassWithEntityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:569;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * synchronously.
   */
  @Test
  public void methodWithResponseClassWithEntityTest() throws Fault {
    String response = null;
    for (String method : ENTITY_METHODS) {
      SyncInvoker sync = createSyncInvokerForMethod(method.toLowerCase());
      Entity<String> entity = createEntity(method.toLowerCase());
      response = sync.method(method, entity, String.class);
      assertResponseString(response, method.toLowerCase());
    }
  }

  /*
   * @testName: methodWithStringClassWithEntityThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:569;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.method(String, Entity,
   * Class) throws ProcessingException in case the invocation failed.
   */
  @Test
  public void methodWithStringClassWithEntityThrowsProcessingExceptionTest()
      throws Fault {
    for (final String method : ENTITY_METHODS) {
      Runnable run = new Runnable() {
        @Override
        public void run() {
          SyncInvoker sync = createSyncInvokerWrongUrl();
          Entity<String> entity = createEntity(method.toLowerCase());
          sync.method(method, entity, String.class);
        }
      };
      assertProcessingException(run);
    }
  }

  /*
   * @testName: methodWithStringClassWithEntityThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:569;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.method(String, Entity,
   * Class) throws WebApplicationException - in case the response status code of
   * the response returned by the server is not successful and the specified
   * response type is not Response.
   */
  @Test
  public void methodWithStringClassWithEntityThrowsWebApplicationExceptionTest()
      throws Fault {
    for (final String method : ENTITY_METHODS) {
      Runnable run = new Runnable() {
        @Override
        public void run() {
          SyncInvoker sync = createSyncInvokerForMethod(
              method.toLowerCase() + "notok");
          Entity<String> entity = createEntity(method.toLowerCase());
          sync.method(method, entity, String.class);
        }
      };
      assertWebApplicationException(run);
    }
  }

  /*
   * @testName:
   * methodWithResponseClassWithEntityThrowsNoWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:569;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.method(String, Entity,
   * Class) throws WebApplicationException - in case the response status code of
   * the response returned by the server is not successful and the specified
   * response type is not Response.
   */
  @Test
  public void methodWithResponseClassWithEntityThrowsNoWebApplicationExceptionTest()
      throws Fault {
    for (final String method : ENTITY_METHODS) {
      SyncInvoker sync = createSyncInvokerForMethod(
          method.toLowerCase() + "notok");
      Entity<String> entity = createEntity(method.toLowerCase());
      Response response = sync.method(method, entity, Response.class);
      assertStatusAndLog(response, Status.NOT_ACCEPTABLE);
    }
  }

  /*
   * @testName: methodWithGenericTypeStringWithEntityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:572;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * synchronously.
   */
  @Test
  public void methodWithGenericTypeStringWithEntityTest() throws Fault {
    String response = null;
    for (String method : ENTITY_METHODS) {
      GenericType<String> generic = createGeneric(String.class);
      SyncInvoker sync = createSyncInvokerForMethod(method.toLowerCase());
      Entity<String> entity = createEntity(method.toLowerCase());
      response = sync.method(method, entity, generic);
      assertResponseString(response, method.toLowerCase());
    }
  }

  /*
   * @testName: methodWithGenericTypeResponseWithEntityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:572;
   * 
   * @test_Strategy: Invoke an arbitrary method for the current request
   * synchronously.
   */
  @Test
  public void methodWithGenericTypeResponseWithEntityTest() throws Fault {
    Response response = null;
    for (String method : ENTITY_METHODS) {
      GenericType<Response> generic = createGeneric(Response.class);
      SyncInvoker sync = createSyncInvokerForMethod(method.toLowerCase());
      Entity<String> entity = createEntity(method.toLowerCase());
      response = sync.method(method, entity, generic);
      assertResponseOk(response);
    }
  }

  /*
   * @testName:
   * methodWithGenericTypeStringWithEntityThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:572;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.method(String, Entity,
   * GenericType) throws ProcessingException in case the invocation failed.
   */
  @Test
  public void methodWithGenericTypeStringWithEntityThrowsProcessingExceptionTest()
      throws Fault {
    for (final String method : ENTITY_METHODS) {
      Runnable run = new Runnable() {
        @Override
        public void run() {
          GenericType<String> generic = createGeneric(String.class);
          SyncInvoker sync = createSyncInvokerWrongUrl();
          Entity<String> entity = createEntity(method);
          sync.method(method, entity, generic);
        }
      };
      assertProcessingException(run);
    }
  }

  /*
   * @testName:
   * methodWithGenericTypeStringWithEntityThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:572;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.method(String, Entity,
   * GenericType) throws WebApplicationException - in case the response status
   * code of the response returned by the server is not successful and the
   * specified response type is not Response.
   */
  @Test
  public void methodWithGenericTypeStringWithEntityThrowsWebApplicationExceptionTest()
      throws Fault {
    for (final String method : ENTITY_METHODS) {
      Runnable run = new Runnable() {
        @Override
        public void run() {
          GenericType<String> generic = createGeneric(String.class);
          SyncInvoker sync = createSyncInvokerForMethod(
              method.toLowerCase() + "notok");
          Entity<String> entity = createEntity(method);
          sync.method(method, entity, generic);
        }
      };
      assertWebApplicationException(run);
    }
  }

  /*
   * @testName:
   * methodWithGenericTypeResponseWithEntityThrowsNoWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:572;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.method(String, Entity,
   * GenericType) throws WebApplicationException - in case the response status
   * code of the response returned by the server is not successful and the
   * specified response type is not Response.
   */
  @Test
  public void methodWithGenericTypeResponseWithEntityThrowsNoWebApplicationExceptionTest()
      throws Fault {
    for (final String method : ENTITY_METHODS) {
      GenericType<Response> generic = createGeneric(Response.class);
      SyncInvoker sync = createSyncInvokerForMethod(
          method.toLowerCase() + "notok");
      Entity<String> entity = createEntity(method);
      Response response = sync.method(method, entity, generic);
      assertStatusAndLog(response, Status.NOT_ACCEPTABLE);
    }
  }

  // ------------------------------------------------------------------
  // ---------------------------OPTIONS--------------------------------
  // ------------------------------------------------------------------

  /*
   * @testName: optionsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:575;
   * 
   * @test_Strategy: Invoke HTTP options method for the current request
   * synchronously.
   */
  @Test
  public void optionsTest() throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("options");
    Response response = sync.options();
    assertResponseOk(response);
  }

  /*
   * @testName: optionsThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:575;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.options throws
   * ProcessingException in case the invocation failed.
   */
  @Test
  public void optionsThrowsProcessingExceptionTest() throws Fault {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        SyncInvoker sync = createSyncInvokerWrongUrl();
        sync.options();
      }
    };
    assertProcessingException(run);
  }

  /*
   * @testName: optionsWithStringClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:577;
   * 
   * @test_Strategy: Invoke HTTP options method for the current request
   * synchronously.
   */
  @Test
  public void optionsWithStringClassTest() throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("options");
    String response = sync.options(String.class);
    assertResponseString(response, "options");
  }

  /*
   * @testName: optionsWithResponseClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:577;
   * 
   * @test_Strategy: Invoke HTTP options method for the current request
   * synchronously.
   */
  @Test
  public void optionsWithResponseClassTest() throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("options");
    Response response = sync.options(Response.class);
    assertResponseOk(response);
  }

  /*
   * @testName: optionsWithStringThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:577;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.options( Class ) throws
   * ProcessingException in case the invocation failed.
   */
  @Test
  public void optionsWithStringThrowsProcessingExceptionTest() throws Fault {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        SyncInvoker sync = createSyncInvokerWrongUrl();
        sync.options(String.class);
      }
    };
    assertProcessingException(run);
  }

  /*
   * @testName: optionsWithStringThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:577;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.options( Class ) throws
   * WebApplicationException - in case the response status code of the response
   * returned by the server is not successful and the specified response type is
   * not Response.
   */
  @Test
  public void optionsWithStringThrowsWebApplicationExceptionTest()
      throws Fault {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        SyncInvoker sync = createSyncInvokerForMethod("optionsnotok");
        sync.options(String.class);
      }
    };
    assertWebApplicationException(run);
  }

  /*
   * @testName: optionsWithResponseThrowsNoWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:577;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.options( Class ) throws
   * WebApplicationException - in case the response status code of the response
   * returned by the server is not successful and the specified response type is
   * not Response.
   */
  @Test
  public void optionsWithResponseThrowsNoWebApplicationExceptionTest()
      throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("optionsnotok");
    Response response = sync.options(Response.class);
    assertStatusAndLog(response, Status.NOT_ACCEPTABLE);
  }

  /*
   * @testName: optionsWithGenericTypeStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:580;
   * 
   * @test_Strategy: Invoke HTTP options method for the current request
   * synchronously.
   */
  @Test
  public void optionsWithGenericTypeStringTest() throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("options");
    GenericType<String> generic = createGeneric(String.class);
    String response = sync.options(generic);
    assertResponseString(response, "options");
  }

  /*
   * @testName: optionsWithGenericTypeResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:580;
   * 
   * @test_Strategy: Invoke HTTP options method for the current request
   * synchronously.
   */
  @Test
  public void optionsWithGenericTypeResponseTest() throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("options");
    GenericType<Response> generic = createGeneric(Response.class);
    Response response = sync.options(generic);
    assertResponseOk(response);
  }

  /*
   * @testName: optionsWithGenericTypeStringThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:580;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.options( GenericType )
   * throws ProcessingException in case the invocation failed.
   */
  @Test
  public void optionsWithGenericTypeStringThrowsProcessingExceptionTest()
      throws Fault {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        SyncInvoker sync = createSyncInvokerWrongUrl();
        GenericType<String> generic = createGeneric(String.class);
        sync.options(generic);
      }
    };
    assertProcessingException(run);
  }

  /*
   * @testName: optionsWithGenericTypeStringThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:580;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.options( GenericType )
   * throws WebApplicationException - in case the response status code of the
   * response returned by the server is not successful and the specified
   * response type is not Response.
   */
  @Test
  public void optionsWithGenericTypeStringThrowsWebApplicationExceptionTest()
      throws Fault {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        SyncInvoker sync = createSyncInvokerForMethod("optionsnotok");
        GenericType<String> generic = createGeneric(String.class);
        sync.options(generic);
      }
    };
    assertWebApplicationException(run);
  }

  /*
   * @testName:
   * optionsWithGenericTypeResponseThrowsNoWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:580;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.options( GenericType )
   * throws WebApplicationException - in case the response status code of the
   * response returned by the server is not successful and the specified
   * response type is not Response.
   */
  @Test
  public void optionsWithGenericTypeResponseThrowsNoWebApplicationExceptionTest()
      throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("optionsnotok");
    GenericType<Response> generic = createGeneric(Response.class);
    Response response = sync.options(generic);
    assertStatusAndLog(response, Status.NOT_ACCEPTABLE);
  }

  // ------------------------------------------------------------------
  // ---------------------------POST-----------------------------------
  // ------------------------------------------------------------------

  /*
   * @testName: postTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:583;
   * 
   * @test_Strategy: Invoke HTTP post method for the current request
   * synchronously.
   */
  @Test
  public void postTest() throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("post");
    Entity<String> entity = createEntity("post");
    Response response = sync.post(entity);
    assertResponseOk(response);
  }

  /*
   * @testName: postThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:583;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.post(Entity) throws
   * ProcessingException in case the invocation failed.
   */
  @Test
  public void postThrowsProcessingExceptionTest() throws Fault {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        SyncInvoker sync = createSyncInvokerWrongUrl();
        Entity<String> entity = createEntity("post");
        sync.post(entity);
      }
    };
    assertProcessingException(run);
  }

  /*
   * @testName: postWithStringClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:585;
   * 
   * @test_Strategy: Invoke HTTP post method for the current request
   * synchronously.
   */
  @Test
  public void postWithStringClassTest() throws Fault {
    Entity<String> entity = createEntity("post");
    SyncInvoker sync = createSyncInvokerForMethod("post");
    String response = sync.post(entity, String.class);
    assertResponseString(response, "post");
  }

  /*
   * @testName: postWithResponseClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:585;
   * 
   * @test_Strategy: Invoke HTTP post method for the current request
   * synchronously.
   */
  @Test
  public void postWithResponseClassTest() throws Fault {
    Entity<String> entity = createEntity("post");
    SyncInvoker sync = createSyncInvokerForMethod("post");
    Response response = sync.post(entity, Response.class);
    assertResponseOk(response);
  }

  /*
   * @testName: postWithStringClassThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:585;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.post( Entity, Class ) throws
   * ProcessingException in case the invocation failed.
   */
  @Test
  public void postWithStringClassThrowsProcessingExceptionTest() throws Fault {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        SyncInvoker sync = createSyncInvokerWrongUrl();
        Entity<String> entity = createEntity("post");
        sync.post(entity, String.class);
      }
    };
    assertProcessingException(run);
  }

  /*
   * @testName: postWithStringClassThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:585;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.post( Entity, Class ) throws
   * WebApplicationException - in case the response status code of the response
   * returned by the server is not successful and the specified response type is
   * not Response.
   */
  @Test
  public void postWithStringClassThrowsWebApplicationExceptionTest()
      throws Fault {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        SyncInvoker sync = createSyncInvokerForMethod("postnotok");
        Entity<String> entity = createEntity("post");
        sync.post(entity, String.class);
      }
    };
    assertWebApplicationException(run);
  }

  /*
   * @testName: postWithResponseClassThrowsNoWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:585;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.post( Entity, Class ) throws
   * WebApplicationException - in case the response status code of the response
   * returned by the server is not successful and the specified response type is
   * not Response.
   */
  @Test
  public void postWithResponseClassThrowsNoWebApplicationExceptionTest()
      throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("postnotok");
    Entity<String> entity = createEntity("post");
    Response response = sync.post(entity, Response.class);
    assertStatusAndLog(response, Status.NOT_ACCEPTABLE);
  }

  /*
   * @testName: postWithGenericTypeStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:588;
   * 
   * @test_Strategy: Invoke HTTP post method for the current request
   * synchronously.
   */
  @Test
  public void postWithGenericTypeStringTest() throws Fault {
    GenericType<String> generic = createGeneric(String.class);
    Entity<String> entity = createEntity("post");
    SyncInvoker sync = createSyncInvokerForMethod("post");
    String response = sync.post(entity, generic);
    assertResponseString(response, "post");
  }

  /*
   * @testName: postWithGenericTypeResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:588;
   * 
   * @test_Strategy: Invoke HTTP post method for the current request
   * synchronously.
   */
  @Test
  public void postWithGenericTypeResponseTest() throws Fault {
    GenericType<Response> generic = createGeneric(Response.class);
    Entity<String> entity = createEntity("post");
    SyncInvoker sync = createSyncInvokerForMethod("post");
    Response response = sync.post(entity, generic);
    assertResponseOk(response);
  }

  /*
   * @testName: postWithGenericTypeStringThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:588;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.post( Entity, GenericType )
   * throws ProcessingException in case the invocation failed.
   */
  @Test
  public void postWithGenericTypeStringThrowsProcessingExceptionTest()
      throws Fault {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        SyncInvoker sync = createSyncInvokerWrongUrl();
        Entity<String> entity = createEntity("post");
        GenericType<String> generic = createGeneric(String.class);
        sync.post(entity, generic);
      }
    };
    assertProcessingException(run);
  }

  /*
   * @testName: postWithGenericTypeStringThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:588;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.post( Entity, GenericType )
   * throws WebApplicationException - in case the response status code of the
   * response returned by the server is not successful and the specified
   * response type is not Response.
   */
  @Test
  public void postWithGenericTypeStringThrowsWebApplicationExceptionTest()
      throws Fault {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        SyncInvoker sync = createSyncInvokerForMethod("postnotok");
        Entity<String> entity = createEntity("post");
        GenericType<String> generic = createGeneric(String.class);
        sync.post(entity, generic);
      }
    };
    assertWebApplicationException(run);
  }

  /*
   * @testName: postWithGenericTypeResponseThrowsNoWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:588;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.post( Entity, GenericType )
   * throws WebApplicationException - in case the response status code of the
   * response returned by the server is not successful and the specified
   * response type is not Response.
   */
  @Test
  public void postWithGenericTypeResponseThrowsNoWebApplicationExceptionTest()
      throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("postnotok");
    Entity<String> entity = createEntity("post");
    GenericType<Response> generic = createGeneric(Response.class);
    Response response = sync.post(entity, generic);
    assertStatusAndLog(response, Status.NOT_ACCEPTABLE);
  }

  // ------------------------------------------------------------------
  // ---------------------------PUT -----------------------------------
  // ------------------------------------------------------------------

  /*
   * @testName: putTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:591;
   * 
   * @test_Strategy: Invoke HTTP PUT method for the current request
   * synchronously.
   */
  @Test
  public void putTest() throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("put");
    Entity<String> entity = createEntity("put");
    Response response = sync.put(entity);
    assertResponseOk(response);
  }

  /*
   * @testName: putThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:591;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.put(Entity) throws
   * ProcessingException in case the invocation failed.
   */
  @Test
  public void putThrowsProcessingExceptionTest() throws Fault {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        SyncInvoker sync = createSyncInvokerWrongUrl();
        Entity<String> entity = createEntity("put");
        sync.put(entity);
      }
    };
    assertProcessingException(run);
  }

  /*
   * @testName: putWithStringClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:593;
   * 
   * @test_Strategy: Invoke HTTP put method for the current request
   * synchronously.
   */
  @Test
  public void putWithStringClassTest() throws Fault {
    Entity<String> entity = createEntity("put");
    SyncInvoker sync = createSyncInvokerForMethod("put");
    String response = sync.put(entity, String.class);
    assertResponseString(response, "put");
  }

  /*
   * @testName: putWithResponseClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:593;
   * 
   * @test_Strategy: Invoke HTTP put method for the current request
   * synchronously.
   */
  @Test
  public void putWithResponseClassTest() throws Fault {
    Entity<String> entity = createEntity("put");
    SyncInvoker sync = createSyncInvokerForMethod("put");
    Response response = sync.put(entity, Response.class);
    assertResponseOk(response);
  }

  /*
   * @testName: putWithStringClassThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:593;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.put( Entity, Class ) throws
   * ProcessingException in case the invocation failed.
   */
  @Test
  public void putWithStringClassThrowsProcessingExceptionTest() throws Fault {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        SyncInvoker sync = createSyncInvokerWrongUrl();
        Entity<String> entity = createEntity("put");
        sync.put(entity, String.class);
      }
    };
    assertProcessingException(run);
  }

  /*
   * @testName: putWithStringClassThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:593;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.put( Entity, Class ) throws
   * WebApplicationException - in case the response status code of the response
   * returned by the server is not successful and the specified response type is
   * not Response.
   */
  @Test
  public void putWithStringClassThrowsWebApplicationExceptionTest()
      throws Fault {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        SyncInvoker sync = createSyncInvokerForMethod("putnotok");
        Entity<String> entity = createEntity("put");
        sync.put(entity, String.class);
      }
    };
    assertWebApplicationException(run);
  }

  /*
   * @testName: putWithResponseClassThrowsNoWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:593;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.put( Entity, Class ) throws
   * WebApplicationException - in case the response status code of the response
   * returned by the server is not successful and the specified response type is
   * not Response.
   */
  @Test
  public void putWithResponseClassThrowsNoWebApplicationExceptionTest()
      throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("putnotok");
    Entity<String> entity = createEntity("put");
    Response response = sync.put(entity, Response.class);
    assertStatusAndLog(response, Status.NOT_ACCEPTABLE);
  }

  /*
   * @testName: putWithGenericTypeStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:596;
   * 
   * @test_Strategy: Invoke HTTP put method for the current request
   * synchronously.
   */
  @Test
  public void putWithGenericTypeStringTest() throws Fault {
    GenericType<String> generic = createGeneric(String.class);
    Entity<String> entity = createEntity("put");
    SyncInvoker sync = createSyncInvokerForMethod("put");
    String response = sync.put(entity, generic);
    assertResponseString(response, "put");
  }

  /*
   * @testName: putWithGenericTypeResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:596;
   * 
   * @test_Strategy: Invoke HTTP put method for the current request
   * synchronously.
   */
  @Test
  public void putWithGenericTypeResponseTest() throws Fault {
    GenericType<Response> generic = createGeneric(Response.class);
    Entity<String> entity = createEntity("put");
    SyncInvoker sync = createSyncInvokerForMethod("put");
    Response response = sync.put(entity, generic);
    assertResponseOk(response);
  }

  /*
   * @testName: putWithGenericTypeStringThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:596;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.put( Entity, GenericType )
   * throws ProcessingException in case the invocation failed.
   */
  @Test
  public void putWithGenericTypeStringThrowsProcessingExceptionTest()
      throws Fault {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        SyncInvoker sync = createSyncInvokerWrongUrl();
        Entity<String> entity = createEntity("put");
        GenericType<String> generic = createGeneric(String.class);
        sync.put(entity, generic);
      }
    };
    assertProcessingException(run);
  }

  /*
   * @testName: putWithGenericTypeStringThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:596;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.put( Entity, GenericType )
   * throws WebApplicationException - in case the response status code of the
   * response returned by the server is not successful and the specified
   * response type is not Response.
   */
  @Test
  public void putWithGenericTypeStringThrowsWebApplicationExceptionTest()
      throws Fault {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        SyncInvoker sync = createSyncInvokerForMethod("putnotok");
        Entity<String> entity = createEntity("put");
        GenericType<String> generic = createGeneric(String.class);
        sync.put(entity, generic);
      }
    };
    assertWebApplicationException(run);
  }

  /*
   * @testName: putWithGenericTypeResponseThrowsNoWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:596;
   * 
   * @test_Strategy: throws WebApplicationException - in case the response
   * status code of the response returned by the server is not successful and
   * the specified response type is not Response.
   */
  @Test
  public void putWithGenericTypeResponseThrowsNoWebApplicationExceptionTest()
      throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("putnotok");
    Entity<String> entity = createEntity("put");
    GenericType<Response> generic = createGeneric(Response.class);
    Response response = sync.put(entity, generic);
    assertStatusAndLog(response, Status.NOT_ACCEPTABLE);
  }

  // ------------------------------------------------------------------
  // ---------------------------TRACE -----------------------------------
  // ------------------------------------------------------------------

  /*
   * @testName: traceTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:599;
   * 
   * @test_Strategy: Invoke HTTP trace method for the current request
   * synchronously.
   */
  @Test
  public void traceTest() throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("trace");
    Response response = sync.trace();
    assertResponseOk(response);
  }

  /*
   * @testName: traceThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:599;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.trace(Entity) throws
   * ProcessingException in case the invocation failed.
   */
  @Test
  public void traceThrowsProcessingExceptionTest() throws Fault {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        SyncInvoker sync = createSyncInvokerWrongUrl();
        sync.trace();
      }
    };
    assertProcessingException(run);
  }

  /*
   * @testName: traceWithStringClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:601;
   * 
   * @test_Strategy: Invoke HTTP trace method for the current request
   * synchronously.
   */
  @Test
  public void traceWithStringClassTest() throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("trace");
    String response = sync.trace(String.class);
    assertResponseString(response, "trace");
  }

  /*
   * @testName: traceWithResponseClassTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:601;
   * 
   * @test_Strategy: Invoke HTTP trace method for the current request
   * synchronously.
   */
  @Test
  public void traceWithResponseClassTest() throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("trace");
    Response response = sync.trace(Response.class);
    assertResponseOk(response);
  }

  /*
   * @testName: traceWithStringClassThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:601;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.trace( Entity, Class )
   * throws ProcessingException in case the invocation failed.
   */
  @Test
  public void traceWithStringClassThrowsProcessingExceptionTest() throws Fault {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        SyncInvoker sync = createSyncInvokerWrongUrl();
        sync.trace(String.class);
      }
    };
    assertProcessingException(run);
  }

  /*
   * @testName: traceWithStringClassThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:601;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.trace( Entity, Class )
   * throws WebApplicationException - in case the response status code of the
   * response returned by the server is not successful and the specified
   * response type is not Response.
   */
  @Test
  public void traceWithStringClassThrowsWebApplicationExceptionTest()
      throws Fault {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        SyncInvoker sync = createSyncInvokerForMethod("tracenotok");
        sync.trace(String.class);
      }
    };
    assertWebApplicationException(run);
  }

  /*
   * @testName: traceWithResponseClassThrowsNoWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:601;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.trace( Entity, Class )
   * throws WebApplicationException - in case the response status code of the
   * response returned by the server is not successful and the specified
   * response type is not Response.
   */
  @Test
  public void traceWithResponseClassThrowsNoWebApplicationExceptionTest()
      throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("tracenotok");
    Response response = sync.trace(Response.class);
    assertStatusAndLog(response, Status.NOT_ACCEPTABLE);
  }

  /*
   * @testName: traceWithGenericTypeStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:604;
   * 
   * @test_Strategy: Invoke HTTP trace method for the current request
   * synchronously.
   */
  @Test
  public void traceWithGenericTypeStringTest() throws Fault {
    GenericType<String> generic = createGeneric(String.class);
    SyncInvoker sync = createSyncInvokerForMethod("trace");
    String response = sync.trace(generic);
    assertResponseString(response, "trace");
  }

  /*
   * @testName: traceWithGenericTypeResponseTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:604;
   * 
   * @test_Strategy: Invoke HTTP trace method for the current request
   * synchronously.
   */
  @Test
  public void traceWithGenericTypeResponseTest() throws Fault {
    GenericType<Response> generic = createGeneric(Response.class);
    SyncInvoker sync = createSyncInvokerForMethod("trace");
    Response response = sync.trace(generic);
    assertResponseOk(response);
  }

  /*
   * @testName: traceWithGenericTypeStringThrowsProcessingExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:604;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.trace( Entity, GenericType )
   * throws ProcessingException in case the invocation failed.
   */
  @Test
  public void traceWithGenericTypeStringThrowsProcessingExceptionTest()
      throws Fault {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        SyncInvoker sync = createSyncInvokerWrongUrl();
        GenericType<String> generic = createGeneric(String.class);
        sync.trace(generic);
      }
    };
    assertProcessingException(run);
  }

  /*
   * @testName: traceWithGenericTypeStringThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:604;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.trace( Entity, GenericType )
   * throws WebApplicationException - in case the response status code of the
   * response returned by the server is not successful and the specified
   * response type is not Response.
   */
  @Test
  public void traceWithGenericTypeStringThrowsWebApplicationExceptionTest()
      throws Fault {
    Runnable run = new Runnable() {
      @Override
      public void run() {
        SyncInvoker sync = createSyncInvokerForMethod("tracenotok");
        GenericType<String> generic = createGeneric(String.class);
        sync.trace(generic);
      }
    };
    assertWebApplicationException(run);
  }

  /*
   * @testName: traceWithGenericTypeResponseThrowsNoWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:604;
   * 
   * @test_Strategy: jakarta.ws.rs.client.SyncInvoker.trace( Entity, GenericType )
   * throws WebApplicationException - in case the response status code of the
   * response returned by the server is not successful and the specified
   * response type is not Response.
   */
  @Test
  public void traceWithGenericTypeResponseThrowsNoWebApplicationExceptionTest()
      throws Fault {
    SyncInvoker sync = createSyncInvokerForMethod("tracenotok");
    GenericType<Response> generic = createGeneric(Response.class);
    Response response = sync.trace(generic);
    assertStatusAndLog(response, Status.NOT_ACCEPTABLE);
  }

  // ///////////////////////////////////////////////////////////////////////
  // utility methods

  protected String getUrl(String method) {
    StringBuilder url = new StringBuilder();
    url.append("http://").append(_hostname).append(":").append(_port);
    url.append(getContextRoot()).append("/").append(method);
    return url.toString();
  }

  /**
   * Create SyncInvoker for given resource method and start time
   */
  protected SyncInvoker createSyncInvokerForMethod(String methodName) {
    Client client = ClientBuilder.newClient();
    client.register(new JdkLoggingFilter(false));
    WebTarget target = client.target(getUrl(methodName));
    SyncInvoker sync = target.request();
    return sync;
  }

  protected SyncInvoker createSyncInvokerWrongUrl() {
    _hostname = "tck.cts";
    _port = 888;
    return createSyncInvokerForMethod("wrongurl");
  }

  protected static void assertStatusAndLog(Response response, Status status)
      throws Fault {
    assertTrue(response.getStatus() == status.getStatusCode(),
        "Returned unexpected status"+ response.getStatus());
    String msg = new StringBuilder().append("Returned status ")
        .append(status.getStatusCode()).append(" (").append(status.name())
        .append(")").toString();
    TestUtil.logMsg(msg);
  }

  protected static void assertResponseOk(Response response) throws Fault {
    assertStatusAndLog(response, Status.OK);
  }

  protected static void assertResponseString(String response,
      String expectedValue) throws Fault {
    assertTrue(expectedValue.equals(response), "expected value"+ expectedValue+
        "differes from acquired value"+ response);
  }

  protected static <T> Entity<T> createEntity(T entity) {
    return Entity.entity(entity, MediaType.WILDCARD_TYPE);
  }

  protected static <T> GenericType<T> createGeneric(Class<T> clazz) {
    return new GenericType<T>(clazz);
  }

  protected static void assertProcessingException(Runnable runnable)
      throws Fault {
    assertException(runnable, ProcessingException.class);
  }

  protected static//
  void assertWebApplicationException(Runnable runnable) throws Fault {
    assertException(runnable, WebApplicationException.class);
  }

  protected static <T extends Exception> void assertException(Runnable runnable,
      Class<T> exception) throws Fault {
    try {
      runnable.run();
    } catch (Exception e) {
      if (exception != null && exception.isInstance(e)) {
        return;
      }
      throw new Fault("unexpected exception", e);
    }
    fault("ProcessingException has not been thrown");
  }

}
