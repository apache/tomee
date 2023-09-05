/*
 * Copyright (c) 2007, 2022 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.api.rs.core.uribuilder;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;

import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriBuilderException;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 */
public class JAXRSClientIT extends JAXRSCommonClient {

  private static final long serialVersionUID = 1814450070599603168L;

  private static final String LOCALHOST = "http://localhost:8080";

  private static final String EXPECTED_PATH = "path-rootless/test2/x%25yz//path-absolute/test1/fred@example.com/x%25yz";

  private static final String ENCODED_EXPECTED_PATH = "path-rootless%2Ftest2/x%25yz/%2Fpath-absolute%2F%2525test1/fred@example.com/x%25yz";

  StringBuilder sb;

  boolean pass = true;

  URI uri;

  public JAXRSClientIT() {
    setContextRoot("/jaxrs_rs.core_uribuilder_web");
    pass = true;
    sb = new StringBuilder();
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
   * @testName: buildTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:176; JAXRS:JAVADOC:190;
   * 
   * @test_Strategy: Create an Uri instance using
   * UriBuilder.fromPath(String).build(String)
   */
  @Test
  public void buildTest1() throws Fault {
    String value = "test1#test2";
    String expected_value = "test1%23test2";

    try {
      uri = UriBuilder.fromPath("{arg1}").build(value);
      gotExpectedPass(uri.toString(), expected_value);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected exception thrown: " + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: buildTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:177;
   * 
   * @test_Strategy: Create an Uri instance using
   * UriBuilder.fromPath(String).build(null); Verify that
   * IllegalArgumentException is thrown.
   */
  @Test
  public void buildTest2() throws Fault {
    String value = null;

    try {
      UriBuilder.fromPath("{arg1}").build(value);
      pass = false;
      sb.append("Expected IllegalArgumentException not thrown" + newline);

    } catch (IllegalArgumentException ilex) {
      sb.append("Expected IllegalArgumentException thrown");
    }

    assertPassAndLog();
  }

  /*
   * @testName: fragmentTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:176; JAXRS:JAVADOC:189; JAXRS:JAVADOC:190;
   * 
   * @test_Strategy: Create an Uri instance using
   * UriBuilder.fromPath(String).fragment(String).build()
   */
  @Test
  public void fragmentTest1() throws Fault {
    String expected_value = "test#xyz";

    try {
      uri = UriBuilder.fromPath("test").fragment("xyz").build();
      gotExpectedPass(uri.toString(), expected_value);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected exception thrown: " + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: buildFromMapTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:185;
   * 
   * @test_Strategy: Create an URI instance using UriBuilder.buildFromMap(Map);
   * Verify % is encoded; and all parameter are replaced by values supplied in
   * Map.
   */
  @Test
  public void buildFromMapTest1() throws Fault {
    Map<String, String> maps = new HashMap<String, String>();
    maps.put("x", "x%yz");
    maps.put("y", "/path-absolute/%25test1");
    maps.put("z", "fred@example.com");
    maps.put("w", "path-rootless/test2");

    try {
      uri = UriBuilder.fromPath("").path("{w}/{x}/{y}/{z}/{x}")
          .buildFromMap(maps);
      gotExpectedPass(uri.getRawPath(), ENCODED_EXPECTED_PATH);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected exception thrown: " + ex.getMessage() + newline);
    }

    assertPassAndLog();
  }

  /*
   * @testName: buildFromMapTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:185;
   * 
   * @test_Strategy: Create an URI instance using UriBuilder.buildFromMap(Map);
   * Verify % is encoded; and all parameter are replaced by values supplied in
   * Map.
   */
  @Test
  public void buildFromMapTest2() throws Fault {
    Map<String, String> maps = new HashMap<String, String>();
    maps.put("x", "x%yz");
    maps.put("y", "/path-absolute/%25test1");
    maps.put("z", "fred@example.com");
    maps.put("w", "path-rootless/test2");
    maps.put("u", "extra");

    try {
      uri = UriBuilder.fromPath("").path("{w}/{x}/{y}/{z}/{x}")
          .buildFromMap(maps);
      gotExpectedPass(uri.getRawPath(), ENCODED_EXPECTED_PATH);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected exception thrown: " + ex.getMessage() + newline);
    }

    assertPassAndLog();
  }


  /*
   * @testName: buildFromMapTest3
   * 
   * @assertion_ids: JAXRS:JAVADOC:185;
   * 
   * @test_Strategy: Create an URI instance using UriBuilder.buildFromMap(Map);
   * Verify IllegalArgumentException is thrown when one parameter value is null.
   */
  @Test
  public void buildFromMapTest3() throws Fault {
    Map<String, String> maps = new HashMap<String, String>();
    maps.put("x", null);
    maps.put("y", "/path-absolute/test1");
    maps.put("z", "fred@example.com");
    maps.put("w", "path-rootless/test2");
    maps.put("u", "extra");

    try {
      UriBuilder.fromPath("").path("{w}/{x}/{y}/{z}/{x}").buildFromMap(maps);
      throw new Fault(
          "Test Failed: expected IllegalArgumentException not thrown");
    } catch (IllegalArgumentException ex) {
      TestUtil.logTrace("Test passed: expected TestUtil.logTrace( thrown");
    }
  }

  /*
   * @testName: buildFromMapTest4
   * 
   * @assertion_ids: JAXRS:JAVADOC:185;
   * 
   * @test_Strategy: Create an URI instance using UriBuilder.buildFromMap(Map);
   * Verify IllegalArgumentException is thrown when one parameter value is null.
   */
  @Test
  public void buildFromMapTest4() throws Fault {
    Map<String, String> maps = new HashMap<String, String>();
    maps.put("x", "x%yz");
    maps.put("y", "/path-absolute/test1");
    maps.put("z", "fred@example.com");
    maps.put("w", "path-rootless/test2");
    maps.put("u", "extra");

    try {
      UriBuilder.fromPath("").path("{w}/{v}/{x}/{y}/{z}/{x}")
          .buildFromMap(maps);
      throw new Fault(
          "Test Failed: expected IllegalArgumentException not thrown");
    } catch (IllegalArgumentException ex) {
      TestUtil.logTrace("Test passed: expected TestUtil.logTrace( thrown");
    }
  }

  /*
   * @testName: buildFromMapTest5
   * 
   * @assertion_ids: JAXRS:JAVADOC:185;
   * 
   * @test_Strategy: Create multiple URI instances from the same UriBuilder
   * instance using the UriBuilder.buildFromMap(Map); Verify that the builder is
   * not affected.
   */
  @Test
  public void buildFromMapTest5() throws Fault {
    UriBuilder ub;

    Map<String, String> maps = new HashMap<String, String>();
    maps.put("x", "x%yz");
    maps.put("y", "/path-absolute/%25test1");
    maps.put("z", "fred@example.com");
    maps.put("w", "path-rootless/test2");

    Map<String, String> maps1 = new HashMap<String, String>();
    maps1.put("x", "x%20yz");
    maps1.put("y", "/path-absolute/test1");
    maps1.put("z", "fred@example.com");
    maps1.put("w", "path-rootless/test2");

    Map<String, String> maps2 = new HashMap<String, String>();
    maps2.put("x", "x%yz");
    maps2.put("y", "/path-absolute/%25test1");
    maps2.put("z", "fred@example.com");
    maps2.put("w", "path-rootless/test2");
    maps2.put("v", "xyz");

    String expected_path_1 = "path-rootless%2Ftest2/x%2520yz/%2Fpath-absolute%2Ftest1/fred@example.com/x%2520yz";

    try {
      ub = UriBuilder.fromPath("").path("{w}/{x}/{y}/{z}/{x}");

      uri = ub.buildFromMap(maps);
      gotExpectedPass(uri.getRawPath(), ENCODED_EXPECTED_PATH);

      uri = ub.buildFromMap(maps1);
      gotExpectedPass(uri.getRawPath(), expected_path_1);

      uri = ub.buildFromMap(maps2);
      gotExpectedPass(uri.getRawPath(), ENCODED_EXPECTED_PATH);
    } catch (Throwable ex) {
      pass = false;
      sb.append("Unexpected exception thrown: " + ex.getMessage() + newline);
    }

    assertPassAndLog();
  }

  /*
   * @testName: buildFromEncodedMapTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:182;
   * 
   * @test_Strategy: Create an URI instance using
   * UriBuilder.buildFromEncodedMap(Map); Verify % is encoded when needed; and
   * all parameter are replaced by values supplied in Map.
   */
  @Test
  public void buildFromEncodedMapTest1() throws Fault {
    Map<String, String> maps = new HashMap<String, String>();
    maps.put("x", "x%20yz");
    maps.put("y", "/path-absolute/%test1");
    maps.put("z", "fred@example.com");
    maps.put("w", "path-rootless/test2");

    String expected_path = "path-rootless/test2/x%20yz//path-absolute/%25test1/fred@example.com/x%20yz";

    try {
      uri = UriBuilder.fromPath("").path("{w}/{x}/{y}/{z}/{x}")
          .buildFromEncodedMap(maps);
      gotExpectedPass(uri.getRawPath(), expected_path);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected exception thrown: " + ex.getMessage() + newline);
    }

    assertPassAndLog();
  }

  /*
   * @testName: buildFromEncodedMapTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:182;
   * 
   * @test_Strategy: Create an URI instance using
   * UriBuilder.buildFromEncodedMap(Map); Verify % is encoded; and all parameter
   * are replaced by values supplied in Map.
   */
  @Test
  public void buildFromEncodedMapTest2() throws Fault {
    Map<String, String> maps = new HashMap<String, String>();
    maps.put("x", "x%yz");
    maps.put("y", "/path-absolute/test1");
    maps.put("z", "fred@example.com");
    maps.put("w", "path-rootless/test2");
    maps.put("u", "extra");

    try {
      uri = UriBuilder.fromPath("").path("{w}/{x}/{y}/{z}/{x}")
          .buildFromEncodedMap(maps);
      gotExpectedPass(uri.getRawPath(), EXPECTED_PATH);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected exception thrown: " + ex.getMessage() + newline);
    }

    assertPassAndLog();
  }

  /*
   * @testName: buildFromEncodedMapTest3
   * 
   * @assertion_ids: JAXRS:JAVADOC:183;
   * 
   * @test_Strategy: Create an URI instance using
   * UriBuilder.buildFromEncodedMap(Map); Verify IllegalArgumentException is
   * thrown when one parameter value is null.
   */
  @Test
  public void buildFromEncodedMapTest3() throws Fault {
    Map<String, String> maps = new HashMap<String, String>();
    maps.put("x", null);
    maps.put("y", "/path-absolute/test1");
    maps.put("z", "fred@example.com");
    maps.put("w", "path-rootless/test2");
    maps.put("u", "extra");

    try {
      UriBuilder.fromPath("").path("{w}/{x}/{y}/{z}/{x}")
          .buildFromEncodedMap(maps);
      throw new Fault(
          "Test Failed: expected IllegalArgumentException not thrown");
    } catch (IllegalArgumentException ex) {
      TestUtil.logTrace("Test passed: expected TestUtil.logTrace( thrown");
    }
  }

  /*
   * @testName: buildFromEncodedMapTest4
   * 
   * @assertion_ids: JAXRS:JAVADOC:183;
   * 
   * @test_Strategy: Create an URI instance using
   * UriBuilder.buildFromEncodedMap(Map); Verify IllegalArgumentException is
   * thrown when one parameter value is null.
   */
  @Test
  public void buildFromEncodedMapTest4() throws Fault {
    Map<String, String> maps = new HashMap<String, String>();
    maps.put("x", "x%yz");
    maps.put("y", "/path-absolute/test1");
    maps.put("z", "fred@example.com");
    maps.put("w", "path-rootless/test2");
    maps.put("u", "extra");

    try {
      UriBuilder.fromPath("").path("{w}/{v}/{x}/{y}/{z}/{x}")
          .buildFromEncodedMap(maps);
      throw new Fault(
          "Test Failed: expected IllegalArgumentException not thrown");
    } catch (IllegalArgumentException ex) {
      TestUtil.logTrace("Test passed: expected TestUtil.logTrace( thrown");
    }
  }

  /*
   * @testName: buildFromEncodedMapTest5
   * 
   * @assertion_ids: JAXRS:JAVADOC:182;
   * 
   * @test_Strategy: Create multiple URI instances from the same UriBuilder
   * instance using the UriBuilder.buildFromEncodedMap(Map); Verify that the
   * builder is not affected.
   */
  @Test
  public void buildFromEncodedMapTest5() throws Fault {
    UriBuilder ub;

    Map<String, String> maps = new HashMap<String, String>();
    maps.put("x", "x%yz");
    maps.put("y", "/path-absolute/test1");
    maps.put("z", "fred@example.com");
    maps.put("w", "path-rootless/test2");

    Map<String, String> maps1 = new HashMap<String, String>();
    maps1.put("x", "x%20yz");
    maps1.put("y", "/path-absolute/test1");
    maps1.put("z", "fred@example.com");
    maps1.put("w", "path-rootless/test2");

    Map<String, String> maps2 = new HashMap<String, String>();
    maps2.put("x", "x%yz");
    maps2.put("y", "/path-absolute/test1");
    maps2.put("z", "fred@example.com");
    maps2.put("w", "path-rootless/test2");
    maps2.put("v", "xyz");

    String expected_path_1 = "path-rootless/test2/x%20yz//path-absolute/test1/fred@example.com/x%20yz";

    try {
      ub = UriBuilder.fromPath("").path("{w}/{x}/{y}/{z}/{x}");

      uri = ub.buildFromEncodedMap(maps);
      gotExpectedPass(uri.getRawPath(), EXPECTED_PATH);

      uri = ub.buildFromEncodedMap(maps1);
      gotExpectedPass(uri.getRawPath(), expected_path_1);

      uri = ub.buildFromEncodedMap(maps2);
      gotExpectedPass(uri.getRawPath(), EXPECTED_PATH);
    } catch (Throwable ex) {
      pass = false;
      sb.append("Unexpected exception thrown: " + ex.getMessage() + newline);
    }

    assertPassAndLog();
  }

  /*
   * @testName: fromPathTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:176; JAXRS:JAVADOC:190;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromPath(String)
   */
  @Test
  public void fromPathTest1() throws Fault {
    String[] paths = { "/", "", "/path-absolute/test1", "fred@example.com",
        "path-rootless/test2" };

    int j = 0;
    while (j < paths.length) {
      try {
        uri = UriBuilder.fromPath(paths[j]).build();
        gotExpectedPass(uri.getPath(), paths[j]);
      } catch (Exception ex) {
        pass = false;
        sb.append("Unexpected exception thrown: " + ex.getMessage());
      }
      j++;
    }

    assertPassAndLog();
  }

  /*
   * @testName: fromPathTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:191;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromPath(null) Verify that IllegalArgumentException is thrown.
   */
  @Test
  public void fromPathTest2() throws Fault {
    String path = null;

    try {
      UriBuilder.fromPath(path);
      throw new Fault(
          "Test Failed: expected IllegalArgumentException not thrown");
    } catch (IllegalArgumentException ilex) {
    }
  }

  /*
   * @testName: fromResourceTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:192;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromResource(null) Verify that IllegalArgumentException is
   * thrown.
   */
  @Test
  public void fromResourceTest1() throws Fault {
    Class<?> res = null;

    try {
      UriBuilder.fromResource(res);
      throw new Fault(
          "Test Failed: expected IllegalArgumentException not thrown");
    } catch (IllegalArgumentException ilex) {
    }
  }

  /*
   * @testName: fromResourceTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:192;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromResource(Class) Verify that Uri can be built from it with
   * correct path.
   */
  @Test
  public void fromResourceTest2() throws Fault {
    Class<?> res = TestPath.class;
    String expected_path = "/TestPath";

    try {
      uri = UriBuilder.fromResource(res).build();
      gotExpectedPass(uri.toString(), expected_path);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected exception thrown: " + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: fromUriTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:194;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromUri((URI)null) Verify that IllegalArgumentException is
   * thrown.
   */
  @Test
  public void fromUriTest1() throws Fault {
    try {
      UriBuilder.fromUri(uri);

      throw new Fault(
          "Test Failed: expected IllegalArgumentException not thrown");
    } catch (IllegalArgumentException ilex) {
    }
  }

  /*
   * @testName: fromUriTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:196;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromUri((String)null) Verify that IllegalArgumentException is
   * thrown.
   */
  @Test
  public void fromUriTest2() throws Fault {
    String uri = null;

    try {
      UriBuilder.fromUri(uri);
      throw new Fault(
          "Test Failed: expected IllegalArgumentException not thrown");
    } catch (IllegalArgumentException ilex) {
    }
  }

  /*
   * @testName: fromUriTest3
   * 
   * @assertion_ids: JAXRS:JAVADOC:176; JAXRS:JAVADOC:196;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromUri(String)
   */
  @Test
  public void fromUriTest3() throws Fault {
    String[] uris = { "ftp://ftp.is.co.za/rfc/rfc1808.txt",
        "mailto:java-net@java.sun.com", "news:comp.lang.java",
        "urn:isbn:096139210x", "http://www.ietf.org/rfc/rfc2396.txt",
        "ldap://[2001:db8::7]/c=GB?objectClass?one", "tel:+1-816-555-1212",
        "telnet://192.0.2.16:80/",
        "foo://example.com:8042/over/there?name=ferret#nose" };

    int j = 0;
    while (j < 9) {
      try {
        uri = UriBuilder.fromUri(uris[j]).build();
        gotExpectedPass(uri.toString().trim(), uris[j]);
      } catch (Exception ex) {
        pass = false;
        sb.append("Unexpected exception thrown: " + ex.getMessage());
      }
      j++;
    }

    assertPassAndLog();
  }

  /*
   * @testName: fromUriTest4
   * 
   * @assertion_ids: JAXRS:JAVADOC:176; JAXRS:JAVADOC:194;
   * 
   * @test_Strategy: Create an UriBuilder instance using UriBuilder.fromUri(URI)
   */
  @Test
  public void fromUriTest4() throws Fault {
    String[] uris = { "ftp://ftp.is.co.za/rfc/rfc1808.txt",
        "mailto:java-net@java.sun.com", "news:comp.lang.java",
        "urn:isbn:096139210x", "http://www.ietf.org/rfc/rfc2396.txt",
        "ldap://[2001:db8::7]/c=GB?objectClass?one", "tel:+1-816-555-1212",
        "telnet://192.0.2.16:80/",
        "foo://example.com:8042/over/there?name=ferret#nose" };

    int j = 0;
    while (j < 9) {
      try {
        uri = UriBuilder.fromUri(new URI(uris[j])).build();
        gotExpectedPass(uri.toString().trim(), uris[j]);
      } catch (Exception ex) {
        pass = false;
        sb.append("Unexpected exception thrown: " + ex.getMessage());
      }
      j++;
    }

    assertPassAndLog();
  }

  /*
   * @testName: pathTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:202;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromPath(String).path("/TestPath"), Verify that "/TestPath" is
   * appended as specified
   */
  @Test
  public void pathTest() throws Fault {
    String path = "test1#test2";
    String path1 = "/TestPath";
    String expected_path = "test1%23test2/TestPath";

    try {
      URI uri = UriBuilder.fromPath(path).path(path1).build();
      gotExpectedPass(uri.toString(), expected_path);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected exception thrown: " + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: pathTest0
   * 
   * @assertion_ids: JAXRS:JAVADOC:202;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromPath(String).path("TestPath"), Verify that "TestPath" is
   * appended as specified
   */
  @Test
  public void pathTest0() throws Fault {
    String path = "test1#test2";
    String path1 = "TestPath";
    String expected_path = "test1%23test2/TestPath";

    try {
      URI uri = UriBuilder.fromPath(path).path(path1).build();
      gotExpectedPass(uri.toString(), expected_path);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected exception thrown: " + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: pathTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:202;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromPath(String).path(String null), Verify that
   * IllegalArgumentException is thrown
   */
  @Test
  public void pathTest1() throws Fault {
    String path = "test1#test2";
    String path1 = null;

    try {
      UriBuilder.fromPath(path).path(path1);
      throw new Fault(
          "Test Failed.  Expected IllegalArgumentException not thrown.");
    } catch (IllegalArgumentException ilex) {
      TestUtil.logTrace(
          "Expected IllegalArgumentException thrown: " + ilex.getMessage());
    }
  }

  /*
   * @testName: pathTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:204;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromPath(String).path(Class TestPath), TestPath is annonated
   * with @Path, Verify that TestPath is appended as specified
   */
  @Test
  public void pathTest2() throws Fault {
    String path = "test1#test2";
    Class<?> path1 = TestPath.class;
    String expected_path = "test1%23test2/TestPath";

    try {
      uri = UriBuilder.fromPath(path).path(path1).build();
      gotExpectedPass(uri.toString(), expected_path);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected exception thrown: " + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: pathTest3
   * 
   * @assertion_ids: JAXRS:JAVADOC:204;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromPath(String).path(Class null), Verify that
   * IllegalArgumentException is thrown
   */
  @Test
  public void pathTest3() throws Fault {
    String path = "test1#test2";
    Class<?> path1 = null;

    try {
      UriBuilder.fromPath(path).path(path1);
      throw new Fault(
          "Test Failed.  Expected IllegalArgumentException not thrown.");

    } catch (IllegalArgumentException ilex) {
      TestUtil.logTrace(
          "Expected IllegalArgumentException thrown: " + ilex.getMessage());
    }
  }

  /*
   * @testName: pathTest4
   * 
   * @assertion_ids: JAXRS:JAVADOC:204;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromPath(String).path(Class TestPathBad), TestPathBad is not
   * annonated with @Path, Verify that IllegalArgumentException is thrown
   */
  @Test
  public void pathTest4() throws Fault {
    String path = "test1#test2";
    Class<?> path1 = TestPathBad.class;

    try {
      UriBuilder.fromPath(path).path(path1);
      throw new Fault(
          "Test Failed.  Expected IllegalArgumentException not thrown.");
    } catch (IllegalArgumentException ilex) {
      TestUtil.logTrace(
          "Expected IllegalArgumentException thrown: " + ilex.getMessage());
    }
  }

  /*
   * @testName: pathTest5
   * 
   * @assertion_ids: JAXRS:JAVADOC:206;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromPath("/TestPath").path(Class TestPath, String "headSub"),
   * Verify that "/sub" is appended as specified
   */
  @Test
  public void pathTest5() throws Fault {
    String path = "/TestPath";
    Class<?> path1 = TestPath.class;
    String md = "headSub";
    String expected_path = "/TestPath/sub";

    try {
      uri = UriBuilder.fromPath(path).path(path1, md).build();
      gotExpectedPass(uri.toString(), expected_path);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected exception thrown: " + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: pathTest6
   * 
   * @assertion_ids: JAXRS:JAVADOC:206;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromPath("/TestPath").path(Class TestPath, String "getPlain"),
   * Verify that IllegalArgumentException is thrown since Method getPlain is not
   * annonated with @Path
   */
  @Test
  public void pathTest6() throws Fault {
    String path = "/TestPath";
    Class<?> path1 = TestPath.class;
    String md = "getPlain";

    try {
      UriBuilder.fromPath(path).path(path1, md).build();
      throw new Fault(
          "Test failed, expected IllegalArgumentException not thrown."
              + newline);
    } catch (IllegalArgumentException ilex) {
      TestUtil.logTrace(
          "Expected IllegalArgumentException thrown: " + ilex.getMessage());
    }
  }

  /*
   * @testName: pathTest7
   * 
   * @assertion_ids: JAXRS:JAVADOC:206;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromPath("/TestPath").path(Class TestPath, null), Verify that
   * IllegalArgumentException is thrown
   */
  @Test
  public void pathTest7() throws Fault {
    String path = "/TestPath";
    Class<?> path1 = TestPath.class;
    String md = null;

    try {
      UriBuilder.fromPath(path).path(path1, md).build();
      throw new Fault(
          "Test failed, expected IllegalArgumentException not thrown."
              + newline);
    } catch (IllegalArgumentException ilex) {
      TestUtil.logTrace(
          "Expected IllegalArgumentException thrown: " + ilex.getMessage());
    }
  }

  /*
   * @testName: pathTest8
   * 
   * @assertion_ids: JAXRS:JAVADOC:206;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromPath("/TestPath").path(Class null, "headSub"), Verify that
   * IllegalArgumentException is thrown since Method getPlan is not annonated
   * with @Path
   */
  @Test
  public void pathTest8() throws Fault {
    String path = "/TestPath";
    Class<?> path1 = null;
    String md = "headSub";

    try {
      UriBuilder.fromPath(path).path(path1, md).build();
      throw new Fault(
          "Test failed, expected IllegalArgumentException not thrown."
              + newline);
    } catch (IllegalArgumentException ilex) {
      TestUtil.logTrace(
          "Expected IllegalArgumentException thrown: " + ilex.getMessage());
    }
  }

  /*
   * @testName: pathTest9
   * 
   * @assertion_ids: JAXRS:JAVADOC:206;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromPath(String).path(TestPath,"test1"), Verify that
   * IllegalArgumentException is thrown, since there are two methods annotated
   * with @Path in TestPath
   */
  @Test
  public void pathTest9() throws Fault {
    String path = "/TestPath";
    Class<?> path1 = TestPath.class;
    String md = "test1";

    try {
      UriBuilder.fromPath(path).path(path1, md).build();
      throw new Fault(
          "Test Failed.  Expected IllegalArgumentException not thrown.");
    } catch (IllegalArgumentException ilex) {
      TestUtil.logTrace(
          "Expected IllegalArgumentException thrown: " + ilex.getMessage());
    }
  }

  /*
   * @testName: pathTest10
   *
   * @assertion_ids: JAXRS:JAVADOC:208;
   *
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromPath("/TestPath").path(Method headSub), Verify that "/sub"
   * is appended as specified
   */
  @Test
  public void pathTest10() throws Fault {
    String path = "/TestPath";
    Class<?> path1 = TestPath.class;
    String expected_path = "/TestPath/sub";

    try {
      Method md = path1.getMethod("headSub", new Class[] {});
      URI uri = UriBuilder.fromPath(path).path(md).build();
      gotExpectedPass(uri.toString(), expected_path);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected exception thrown: " + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: pathTest11
   * 
   * @assertion_ids: JAXRS:JAVADOC:208;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromPath("/TestPath").path(Method headSub), Verify that "/sub1"
   * is appended as specified
   */
  @Test
  public void pathTest11() throws Fault {
    String path = "/TestPath";
    Class<?> path1 = TestPath.class;
    String expected_path = "/TestPath/sub1";

    try {
      Method md = path1.getMethod("test1", new Class[] {});
      URI uri = UriBuilder.fromPath(path).path(md).build();
      gotExpectedPass(uri.toString(), expected_path);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected exception thrown: " + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: pathTest12
   * 
   * @assertion_ids: JAXRS:JAVADOC:208;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromPath(String).path(Method getPlain), Verify that
   * IllegalArgumentException is thrown
   */
  @Test
  public void pathTest12() throws Fault {
    String path = "/TestPath";
    Class<?> path1 = TestPath.class;

    try {
      Method md = path1.getMethod("getPlain", new Class[] {});
      UriBuilder.fromPath(path).path(md).build();

      throw new Fault(
          "Test Failed.  Expected IllegalArgumentException not thrown.");

    } catch (IllegalArgumentException ilex) {
      TestUtil.logTrace(
          "Expected IllegalArgumentException thrown: " + ilex.getMessage());
    } catch (NoSuchMethodException ex) {
      throw new Fault(
          "Test Failed.  Unexpected exception thrown: " + ex.getMessage());
    }
  }

  /*
   * @testName: pathTest13
   * 
   * @assertion_ids: JAXRS:JAVADOC:208;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromPath(String).path(Method null), Verify that
   * IllegalArgumentException is thrown
   */
  @Test
  public void pathTest13() throws Fault {
    String path = "test1#test2";
    java.lang.reflect.Method path1 = null;

    try {
      UriBuilder.fromPath(path).path(path1);
      throw new Fault(
          "Test Failed.  Expected IllegalArgumentException not thrown.");
    } catch (IllegalArgumentException ilex) {
      TestUtil.logTrace(
          "Expected IllegalArgumentException thrown: " + ilex.getMessage());
    }
  }

  /*
   * @testName: replacePathTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:218;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromPath("/TestPath").replacePath(String newPath), Verify that
   * path is updated as specified
   */
  @Test
  public void replacePathTest1() throws Fault {
    String path = "/TestPath";
    String new_path = "/TestPathAgain";

    try {
      URI uri = UriBuilder.fromPath(path).replacePath(new_path).build();
      gotExpectedPass(uri.toString(), new_path);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected exception thrown: " + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: replacePathTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:218;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromPath("/TestPath1/TestPath2").replacePath(String null),
   * Verify that all existing path cleared as specified
   */
  @Test
  public void replacePathTest2() throws Fault {
    String path = "/TestPath1/TestPath2";
    String new_path = null;

    try {
      URI uri = UriBuilder.fromPath(path).replacePath(new_path).build();
      boolean conditionTrue = uri.toString() == null
          || uri.toString().trim().compareTo("") == 0;
      gotExpectedPass(!conditionTrue, uri.toString(), new_path);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected exception thrown: " + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: replacePathTest3
   * 
   * @assertion_ids: JAXRS:JAVADOC:218;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromPath("TestPath").replacePath(String newPath), Verify that
   * all existing path cleared as specified
   */
  @Test
  public void replacePathTest3() throws Fault {
    String path = "TestPath";
    String new_path = null;

    try {
      uri = UriBuilder.fromPath(path).replacePath(new_path).build();
      boolean conditionTrue = uri.toString() == null
          || uri.toString().trim().compareTo("") == 0;
      gotExpectedPass(!conditionTrue, uri.toString(), new_path);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected exception thrown: " + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: portTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:210;
   * 
   * @test_Strategy: Call UriBuilder.port(int), Veify that port number updated
   * accordingly.
   */
  @Test
  public void portTest1() throws Fault {
    String uri_string = "foo://example.com:8042/over/there?name=ferret#nose";
    String uri_string_expected = "foo://example.com:2008/over/there?name=ferret#nose";
    int port = 2008;

    try {
      uri = UriBuilder.fromUri(uri_string).port(port).build();
      gotExpectedPass(uri.toString().trim(), uri_string_expected);
    } catch (Exception ex) {
      throw new Fault("Test Failed: unexpected Exception thrown", ex);
    }
  }

  /*
   * @testName: portTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:210;
   * 
   * @test_Strategy: Call UriBuilder.port(int) with an invalid port number;
   * Verify that IllegalArgumentException is thrown.
   */
  @Test
  public void portTest2() throws Fault {
    String uri_string = "foo://example.com:8042/over/there?name=ferret#nose";
    int port = -10;

    try {
      uri = UriBuilder.fromUri(uri_string).port(port).build();
      throw new Fault(
          "Test failed, expected IllegalArgumentException not thrown."
              + "Got uri=" + uri.toString() + " instead.");
    } catch (IllegalArgumentException ex) {
      TestUtil
          .logTrace("Test Passed: expected IllegalArgumentException thrown");
    }
  }

  /*
   * @testName: hostTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:198;
   * 
   * @test_Strategy: Call UriBuilder.host(String), verify that hostame updated
   * accordingly
   */
  @Test
  public void hostTest1() throws Fault {
    String uri_string = "foo://example.com:8042/over/there?name=ferret#nose";
    String uri_string_1 = "foo://java.net:8042/over/there?name=ferret#nose";
    String host = "java.net";

    try {
      uri = UriBuilder.fromUri(uri_string).host(host).build();
      gotExpectedPass(uri.toString().trim(), uri_string_1);
    } catch (Exception ex) {
      throw new Fault("Test Failed: unexpected Exception thrown", ex);
    }
  }

  /*
   * @testName: hostTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:198;
   * 
   * @test_Strategy: Call UriBuilder.host(hostname) with incorrect hstname,
   * verify that IllegalArgumentException is thrown.
   */
  @Test
  public void hostTest2() throws Fault {
    String uri_string = "foo://example.com:8042/over/there?name=ferret#nose";
    String host = "";

    try {
      uri = UriBuilder.fromUri(uri_string).host(host).build();
      throw new Fault(
          "Test failed, expected IllegalArgumentException not thrown."
              + "Got uri=" + uri.toString() + " instead.");
    } catch (IllegalArgumentException ex) {
      TestUtil
          .logTrace("Test Passed: expected IllegalArgumentException thrown");
    }
  }

  /*
   * @testName: schemeTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:196; JAXRS:JAVADOC:223;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromUri(String)
   */
  @Test
  public void schemeTest1() throws Fault {
    String uri_string = "foo://example.com:8042/over/there?name=ferret#nose";
    String uri_string_1 = "http://example.com:8042/over/there?name=ferret#nose";
    String scheme = "http";

    try {
      uri = UriBuilder.fromUri(uri_string).scheme(scheme).build();
      gotExpectedPass(uri.toString().trim(), uri_string_1);
    } catch (Exception ex) {
      throw new Fault("Test Failed: unexpected Exception thrown", ex);
    }
  }

  /*
   * @testName: schemeTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:223;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromUri(null); Verify that scheme is cleared.
   */
  @Test
  public void schemeTest2() throws Fault {
    String uri_string = "http://example.com:8042/over/there?name=ferret#nose";
    String scheme = null;

    try {
      uri = UriBuilder.fromUri(uri_string).scheme(scheme).build();
      assertTrue(uri.getRawSchemeSpecificPart() == null,
          "scheme not cleared as expected, got"+ uri.getSchemeSpecificPart());
    } catch (Throwable ex) {
      TestUtil.logTrace(
          "Unexpected Exception thrown. Test Failed." + ex.getMessage());
    }
  }

  /*
   * @testName: schemeSpecificPartTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:225;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromUri(String); Call method schemeSpecificPart(String) Verify
   * the method works.
   */
  @Test
  public void schemeSpecificPartTest() throws Fault {
    String uri_1 = "http://example.com:8042/uber/here?name=ferret#nose";
    String uri_2 = "//example1.com:8041/over/there?name=monkey";
    String uri_3 = "http://example1.com:8041/over/there?name=monkey#nose";

    try {
      uri = UriBuilder.fromUri(uri_1).schemeSpecificPart(uri_2).build();
      gotExpectedPass(uri.toString().trim(), uri_3);
    } catch (Exception ex) {
      throw new Fault("Test Failed: unexpected Exception thrown", ex);
    }
  }

  /*
   * @testName: schemeSpecificPartTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:225;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromUri(String); Call method schemeSpecificPart(null) Verify
   * that IllegalArgumentException is thrown.
   */
  @Test
  public void schemeSpecificPartTest1() throws Fault {
    String uri_1 = "http://example.com:8042/uber/here?name=ferret#nose";
    String uri_2 = null;

    try {
      UriBuilder.fromUri(uri_1).schemeSpecificPart(uri_2).build();
      sb.append(
          "Test Failed: expected IllegalArgumentException Exception not thrown.");
      pass = false;
    } catch (IllegalArgumentException ex) {
      sb.append("Expected IllegalArgumentException thrown. Test PASSED.");
    } catch (Exception ex) {
      sb.append("Test Failed: wrong Exception thrown" + ex.getMessage());
      pass = false;
    }

    assertPassAndLog();
  }

  /*
   * @testName: templateTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:176; JAXRS:JAVADOC:189; JAXRS:JAVADOC:190;
   * 
   * @test_Strategy: Create an Uri instance using
   * UriBuilder.fromPath(String).fragment(String).build(String)
   */
  @Test
  public void templateTest1() throws Fault {
    String expected_value = "test#xyz";

    try {
      uri = UriBuilder.fromPath("{arg1}").fragment("{arg2}").build("test",
          "xyz");
      gotExpectedPass(uri.toString(), expected_value);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected exception thrown: " + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: templateTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:176; JAXRS:JAVADOC:190;
   * 
   * @test_Strategy: Create an Uri instance using
   * UriBuilder.fromPath(String).build(String)
   */
  @Test
  public void templateTest2() throws Fault {
    String expected_value = "test1/test2/test1";

    try {
      uri = UriBuilder.fromPath("{arg1}/{arg2}/{arg1}").build("test1", "test2",
          "test3");
      gotExpectedPass(uri.toString(), expected_value);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected exception thrown: " + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: uriTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:176; JAXRS:JAVADOC:194; JAXRS:JAVADOC:229;
   * 
   * @test_Strategy: Create a set of UriBuilder instances with diffeent schemes;
   * using UriBuilder instances using UriBuilder.fromUri(URI) Call uri(URI)
   * method on all instances; verify it works correctly on all of the instances.
   */
  @Test
  public void uriTest() throws Fault {
    String[] uris_orig = getOrigUris();
    URI uris_replace[] = getReplacementUris();
    String[] uris_expect = getExpectedUris();

    int j = 0;
    int i = 0;
    int k = 0;
    while (j < 17) {
      try {
        sb.append("Replace uri with ").append(uris_replace[j]).append(newline);
        uri = UriBuilder.fromUri(new URI(uris_orig[j])).uri(uris_replace[j])
            .build();
        if (gotExpectedPass(uri.toString().trim(), uris_expect[j]) != 0)
          i++;
        else
          k++;
      } catch (Exception ex) {
        pass = false;
        sb.append("Unexpected exception thrown: ").append(ex.getMessage())
            .append(newline);
        sb.append("Test failed with exception for expected uri: ")
            .append(uris_expect[j]).append(newline);
      }
      j++;
    }

    assertTrue(pass, k+ "assertion passed."+ newline+ i+ "assertion(s) failed:"+
        sb.toString()+ newline);
    logMsg(sb);
  }

  /*
   * @testName: uriTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:229;
   * 
   * @test_Strategy: Calling UriBuilder.uri(URI null) verify the
   * IllegalStateException thrown..
   */
  @Test
  public void uriTest1() throws Fault {
    try {
      UriBuilder.fromPath("/").uri(uri);
      throw new Fault("Expected IllegalArgumentException not thrown" + newline);
    } catch (IllegalArgumentException ilex) {
      TestUtil.logTrace("Expected IllegalArgumentException thrown");
    }
  }

  /*
   * @testName: fromEncodedTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:179;
   * 
   * @test_Strategy: Calling UriBuilder.buildFromEncoded(Object...). verify
   * Object.toString() is encoded.
   */
  @Test
  public void fromEncodedTest1() throws Fault {
    String expected_value_1 = "http://localhost:8080/a/%25/=/%25G0/%25/=";
    String expected_value_2 = "http://localhost:8080/xy/%20/%25/xy";

    try {
      uri = UriBuilder.fromPath(LOCALHOST).path("/{v}/{w}/{x}/{y}/{z}/{x}")
          .buildFromEncoded("a", "%25", "=", "%G0", "%", "23");
      gotExpectedPass(uri.toString(), expected_value_1);

      uri = UriBuilder.fromPath(LOCALHOST).path("/{x}/{y}/{z}/{x}")
          .buildFromEncoded("xy", " ", "%");
      gotExpectedPass(uri.toString(), expected_value_2);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected Exception thrown" + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: fromEncodedTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:180;
   * 
   * @test_Strategy: Calling UriBuilder.buildFromEncoded(Object...). verify
   * IllegalArgumentException is thrown when one template is missing its value
   */
  @Test
  public void fromEncodedTest2() throws Fault {
    try {
      uri = UriBuilder.fromPath(LOCALHOST).path("/{v}/{w}/{x}/{y}/{z}/{x}")
          .buildFromEncoded("a", "%25", "=", "%G0");
      throw new Fault("Expected IllegalArgumentException Not thrown. uri ="
          + uri.toString());
    } catch (IllegalArgumentException ilex) {
      TestUtil.logTrace("Expected IllegalArgumentException thrown");
    }
  }

  /*
   * @testName: fromEncodedTest3
   * 
   * @assertion_ids: JAXRS:JAVADOC:180;
   * 
   * @test_Strategy: Calling UriBuilder.buildFromEncoded(Object...). verify
   * IllegalArgumentException is thrown when one value is null.
   */
  @Test
  public void fromEncodedTest3() throws Fault {
    try {
      UriBuilder.fromPath(LOCALHOST).path("/{v}/{w}/{x}/{y}/{z}/{x}")
          .buildFromEncoded("a", "%25", null, "%G0");
      throw new Fault("Expected IllegalArgumentException Not thrown");
    } catch (IllegalArgumentException ilex) {
      TestUtil.logTrace("Expected IllegalArgumentException thrown");
    }
  }

  /*
   * @testName: queryParamTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:212;
   * 
   * @test_Strategy: Calling UriBuilder.queryParam(String name, Object...
   * value). verify IllegalArgumentException is thrown when name is null.
   */
  @Test
  public void queryParamTest1() throws Fault {
    String name = null;

    try {
      UriBuilder.fromPath(LOCALHOST).queryParam(name, "x", "y");
      throw new Fault("Expected IllegalArgumentException Not thrown");
    } catch (IllegalArgumentException ilex) {
      TestUtil.logTrace("Expected IllegalArgumentException thrown");
    }
  }

  /*
   * @testName: queryParamTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:212;
   * 
   * @test_Strategy: Calling UriBuilder.queryParam(String name, Object...
   * value). verify IllegalArgumentException is thrown when values is null.
   */
  @Test
  public void queryParamTest2() throws Fault {
    String name = "name";

    try {
      UriBuilder.fromPath(LOCALHOST).queryParam(name, (Object) null);
      throw new Fault("Expected IllegalArgumentException Not thrown");
    } catch (IllegalArgumentException ilex) {
      TestUtil.logTrace("Expected IllegalArgumentException thrown");
    }
  }

  /*
   * @testName: queryParamTest3
   * 
   * @assertion_ids: JAXRS:JAVADOC:212;
   * 
   * @test_Strategy: Calling UriBuilder.queryParam(String name, Object...
   * value). verify IllegalArgumentException is thrown when one of values is
   * null.
   */
  @Test
  public void queryParamTest3() throws Fault {
    String name = "name";

    try {
      UriBuilder.fromPath(LOCALHOST).queryParam(name, "x", null);
      throw new Fault("Expected IllegalArgumentException Not thrown");
    } catch (IllegalArgumentException ilex) {
      TestUtil.logTrace("Expected IllegalArgumentException thrown");
    }
  }

  /*
   * @testName: queryParamTest4
   * 
   * @assertion_ids: JAXRS:JAVADOC:212;
   * 
   * @test_Strategy: Calling UriBuilder.queryParam(String name, Object...
   * value). verify that when two values are supplied, both are present in final
   * URI.
   */
  @Test
  public void queryParamTest4() throws Fault {
    String name = "name";
    String expected_value = "http://localhost:8080?name=x&name=y";

    try {
      uri = UriBuilder.fromPath(LOCALHOST).queryParam(name, "x", "y").build();
      gotExpectedPass(uri.toString(), expected_value);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected Exception thrown" + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: queryParamTest5
   * 
   * @assertion_ids: JAXRS:JAVADOC:212;
   * 
   * @test_Strategy: Calling UriBuilder.queryParam(String name, Object...
   * value). verify that values are encoded properly in final URI.
   */
  @Test
  public void queryParamTest5() throws Fault {
    String name = "name";
    String actual = null;
    String expected_value = "http://localhost:8080?name=x%3D&name=y?&name=x+y&name=%26";

    try {
      uri = UriBuilder.fromPath(LOCALHOST)
          .queryParam(name, "x=", "y?", "x y", "&").build();
      actual = uri.toString().replace("%3F", "?").replace("%3f", "?");
      gotExpectedPass(actual, expected_value);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected Exception thrown" + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: replaceQueryTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:219;
   * 
   * @test_Strategy: Calling UriBuilder.replaceQuery(String name). verify that
   * query is replaced properly in final URI.
   */
  @Test
  public void replaceQueryTest1() throws Fault {
    String name = "name";
    String expected_value = "http://localhost:8080?name1=xyz";

    try {
      uri = UriBuilder.fromPath(LOCALHOST)
          .queryParam(name, "x=", "y?", "x y", "&").replaceQuery("name1=xyz")
          .build();
      gotExpectedPass(uri.toString(), expected_value);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected Exception thrown" + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: replaceQueryTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:219;
   * 
   * @test_Strategy: Calling UriBuilder.replaceQuery(String null). verify that
   * query is cleared properly in final URI.
   */
  @Test
  public void replaceQueryTest2() throws Fault {
    String name = "name";

    try {
      uri = UriBuilder.fromPath(LOCALHOST)
          .queryParam(name, "x=", "y?", "x y", "&").replaceQuery(null).build();
      gotExpectedPass(uri.toString(), LOCALHOST);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected Exception thrown" + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: replaceQueryTest3
   * 
   * @assertion_ids: JAXRS:JAVADOC:219;
   * 
   * @test_Strategy: Calling UriBuilder.replaceQuery(String query). verify that
   * query is replaced properly in final URI.
   */
  @Test
  public void replaceQueryTest3() throws Fault {
    String name = "name";
    String expected_value = "http://localhost:8080?name1=x&name2=%20&name3=x+y&name4=23&name5=x%20y";

    try {
      uri = UriBuilder.fromPath(LOCALHOST)
          .queryParam(name, "x=", "y?", "x y", "&")
          .replaceQuery("name1=x&name2=%20&name3=x+y&name4=23&name5=x y")
          .build();
      gotExpectedPass(uri.toString(), expected_value);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected Exception thrown" + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * testName: replaceQueryTest4
   * 
   * @assertion_ids: JAXRS:JAVADOC:219;
   * 
   * @test_Strategy: Calling UriBuilder.replaceQuery(String query). verify that
   * IllegalArgumentException is thrown when query cannot be parsed.
   */
  @Test
  @Disabled
  public void replaceQueryTest4() throws Fault {
    String value = "http://localhost:8080?name1=x&name2=%20&name3=x+y&name4=x%20y";

    try {
      uri = UriBuilder.fromPath(value)
          .replaceQuery("name$*()^@!+-]}[{|<>,./:;'#1==x?&name2=%20y").build();
      pass = false;
      sb.append("Expected IllegalArgumentException not thrown");
      sb.append("uri=" + uri.getQuery());
    } catch (IllegalArgumentException ex) {
      sb.append("Expected IllegalArgumentException thrown");
    } catch (Exception ex1) {
      pass = false;
      sb.append("Wrong type Exception thrown" + ex1.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: replaceQueryParamTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:221;
   * 
   * @test_Strategy: Calling UriBuilder.replaceQueryParam(String null, Object...
   * value). verify IllegalArgumentException is thrown
   */
  @Test
  public void replaceQueryParamTest1() throws Fault {
    String name = null;

    try {
      UriBuilder.fromPath(LOCALHOST).queryParam(name, "x", null);
      throw new Fault("Expected IllegalArgumentException Not thrown");
    } catch (IllegalArgumentException ilex) {
      TestUtil.logTrace("Expected IllegalArgumentException thrown");
    }
  }

  /*
   * @testName: replaceQueryParamTest2
   *
   * @assertion_ids: JAXRS:JAVADOC:221;
   *
   * @test_Strategy: Calling UriBuilder.replaceQueryParam(String name, Object...
   * value). verify all query params are cleared when values is null.
   */
  @Test
  public void replaceQueryParamTest2() throws Fault {
    String name = "name";

    try {
      uri = UriBuilder.fromPath(LOCALHOST)
          .queryParam(name, "x=", "y?", "x y", "&")
          .replaceQueryParam(name, (Object[]) null).build();
      gotExpectedPass(uri.toString(), LOCALHOST);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected Exception thrown" + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: replaceQueryParamTest3
   * 
   * @assertion_ids: JAXRS:JAVADOC:221;
   * 
   * @test_Strategy: Calling UriBuilder.replaceQueryParam(String name, Object...
   * value). verify that query parameter is updated accordingly
   */
  @Test
  public void replaceQueryParamTest3() throws Fault {
    String name = "name";
    String expected_value = "http://localhost:8080?name=x&name=y&name=y+x&name=x%25y&name=%20";
    try {
      uri = UriBuilder.fromPath(LOCALHOST)
          .queryParam(name, "x=", "y?", "x y", "&")
          .replaceQueryParam(name, "x", "y", "y x", "x%y", "%20").build();
      gotExpectedPass(uri.toString(), expected_value);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected Exception thrown" + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: segmentTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:227;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromPath("/TestPath").segment(String[] paths), Verify
   * IllegalArgumentException if any element of paths is null
   */
  @Test
  public void segmentTest1() throws Fault {
    String path = null;

    try {
      UriBuilder.fromPath("/").segment(path).build();
      throw new Fault(
          "TestFailed: expected IllegalArgumentException not thrown.");

    } catch (IllegalArgumentException ex) {
      TestUtil
          .logTrace("Test Passed: Expected IllegalArgumentException thrown.");
    }
  }

  /*
   * @testName: segmentTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:227;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromPath("/TestPath").segment(String[] paths), Verify "/" is
   * added when needed and characters are encoded as needed.
   */
  @Test
  public void segmentTest2() throws Fault {
    String path1 = "";
    String[] path2 = { "a1", "/", "3b " };
    String expected_path = "a1/%2F/3b%20";

    try {
      uri = UriBuilder.fromPath(path1).segment(path2).build();
      gotExpectedPass(uri.toString(), expected_path);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected Exception thrown" + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: segmentTest3
   * 
   * @assertion_ids: JAXRS:JAVADOC:227;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromPath("/TestPath").segment(String[] paths), Verify that a
   * single value is only a single URI path segment. and characters are encoded
   * as needed.
   */
  @Test
  public void segmentTest3() throws Fault {
    String path1 = "ab";
    String[] path2 = { "a1", "x/y", "3b " };
    String expected_path = "ab/a1/x%2Fy/3b%20";

    try {
      uri = UriBuilder.fromPath(path1).segment(path2).build();
      gotExpectedPass(uri.toString(), expected_path);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected Exception thrown" + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: uriBuilderExceptionTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:232;
   * 
   * @test_Strategy: Create an UriBuilderException instance using
   * UriBuilderException(). Verify that empty message is associated with the
   * exception.
   */
  @Test
  public void uriBuilderExceptionTest1() throws Fault {
    try {
      throw new UriBuilderException();
    } catch (UriBuilderException ube) {
      if (ube.getMessage() == "" || ube.getMessage() == null) {
        TestUtil.logTrace(
            "Test Passed with empty message: " + ube.getMessage() + ".");
      } else {
        throw new Fault("Test Failed.  Expecting empty message," + " Got "
            + ube.getMessage());
      }
    }
  }

  /*
   * @testName: uriBuilderExceptionTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:233;
   * 
   * @test_Strategy: Create an UriBuilderException instance using
   * UriBuilderException(String msg). Verify that message msg is associated with
   * the exception.
   */
  @Test
  public void uriBuilderExceptionTest2() throws Fault {
    String msg = "JAX-RS Test Message: xyz";

    try {
      throw new UriBuilderException(msg);
    } catch (UriBuilderException ube) {
      if (msg.equals(ube.getMessage())) {
        TestUtil.logTrace(
            "Test Passed with correct message: " + ube.getMessage() + ".");
      } else {
        throw new Fault("Test Failed.  Expecting message," + msg + ", got "
            + ube.getMessage() + ".");
      }
    }
  }

  /*
   * @testName: uriBuilderExceptionTest3
   * 
   * @assertion_ids: JAXRS:JAVADOC:234;
   * 
   * @test_Strategy: Create an UriBuilderException instance using
   * UriBuilderException(String msg, throwable ex). Verify that message msg is
   * associated with the exception.
   */
  @Test
  public void uriBuilderExceptionTest3() throws Fault {
    String msg = "JAX-RS Test Message: xyz";
    String msg1 = "JAX-RS Test Message Again: xyz";

    try {
      throw new UriBuilderException(msg, new Exception(msg1));
    } catch (UriBuilderException ube) {
      if (ube.getMessage().contains(msg)) {
        TestUtil.logTrace(
            "Test Passed with correct message: " + ube.getMessage() + ".");
      } else {
        throw new Fault("Test Failed.  Expecting message," + msg + ", got "
            + ube.getMessage() + ".");
      }
    }
  }

  /*
   * @testName: uriBuilderExceptionTest4
   * 
   * @assertion_ids: JAXRS:JAVADOC:235;
   * 
   * @test_Strategy: Create an UriBuilderException instance using
   * UriBuilderException(String msg, throwable ex). Verify that message msg is
   * associated with the exception.
   */
  @Test
  public void uriBuilderExceptionTest4() throws Fault {
    String msg = "JAX-RS Test Message Again: xyz";

    try {
      throw new UriBuilderException(new Exception(msg));
    } catch (UriBuilderException ube) {
      if (ube.getMessage().contains(msg)) {
        TestUtil.logTrace(
            "Test Passed with correct message: " + ube.getMessage() + ".");
      } else {
        throw new Fault("Test Failed.  Expecting message," + msg + ", got "
            + ube.getMessage() + ".");
      }
    }
  }

  /*
   * @testName: cloneTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:188;
   * 
   * @test_Strategy: Create an UriBuilder instance using
   * UriBuilder.fromPath(String); Create another UriBuilder instance using
   * UriBuilder.clone(); Verify that both are created correctly.
   */
  @Test
  public void cloneTest1() throws Fault {
    UriBuilder ub, ub1;
    URI uri1;
    String path1 = "test";
    String frag = "xyz";
    String expected_path_1 = "test#xyz";

    Map<String, String> maps = new HashMap<String, String>();
    maps.put("x", "x%yz");
    maps.put("y", "/path-absolute/%25test1");
    maps.put("z", "fred@example.com");
    maps.put("w", "path-rootless/test2");

    String expected_path_2 = "test/" + ENCODED_EXPECTED_PATH + "#xyz";

    try {
      ub = UriBuilder.fromPath(path1).fragment(frag);
      ub1 = ub.clone();
      uri = ub.build();
      gotExpectedPass(uri.toString(), expected_path_1);

      uri1 = ub1.path("{w}/{x}/{y}/{z}/{x}").buildFromMap(maps);
      gotExpectedPass(uri1.toString(), expected_path_2);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected exception thrown: " + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: matrixParamTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:200;
   * 
   * @test_Strategy: Calling UriBuilder.matrixParam(String name, Object...
   * value). verify IllegalArgumentException is thrown when name is null.
   */
  @Test
  public void matrixParamTest1() throws Fault {
    String name = null;

    try {
      UriBuilder.fromPath(LOCALHOST).matrixParam(name, "x", "y");
      sb.append("Expected IllegalArgumentException Not thrown");
      pass = false;
    } catch (IllegalArgumentException ilex) {
      sb.append("Expected IllegalArgumentException thrown");
    } catch (Throwable th) {
      pass = false;
      sb.append("Incorrect Exception thrown: " + th.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: matrixParamTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:200;
   * 
   * @test_Strategy: Calling UriBuilder.matrixParam(String name, Object...
   * value). verify IllegalArgumentException is thrown when values is null.
   */
  @Test
  public void matrixParamTest2() throws Fault {
    String name = "name";

    try {
      UriBuilder.fromPath(LOCALHOST).matrixParam(name, (Object) null);
      sb.append("Expected IllegalArgumentException Not thrown");
      pass = false;
    } catch (IllegalArgumentException ilex) {
      sb.append("Expected IllegalArgumentException thrown");
    } catch (Throwable th) {
      pass = false;
      sb.append("Incorrect Exception thrown: " + th.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: matrixParamTest3
   * 
   * @assertion_ids: JAXRS:JAVADOC:200;
   * 
   * @test_Strategy: Calling UriBuilder.matrixParam(String name, Object...
   * value). verify that when two values are supplied, both are present in final
   * URI.
   */
  @Test
  public void matrixParamTest3() throws Fault {
    String name = "name";
    String expected_value = "http://localhost:8080;name=x;name=y";

    try {
      uri = UriBuilder.fromPath(LOCALHOST).matrixParam(name, "x", "y").build();
      gotExpectedPass(uri.toString(), expected_value);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected Exception thrown" + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: replaceMatrixParamTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:216;
   * 
   * @test_Strategy: Calling UriBuilder.replaceMatrixParam(String name,
   * Object... value). verify IllegalArgumentException when name is null.
   */
  @Test
  public void replaceMatrixParamTest1() throws Fault {
    String name = "name";

    try {
      UriBuilder.fromPath(LOCALHOST).matrixParam(name, "x=", "y?", "x y", "&")
          .replaceMatrixParam(null, "x", "y").build();
      pass = false;
      sb.append("Expected exception not thrown.");
    } catch (IllegalArgumentException ex) {
      sb.append("Expected exception thrown.");
    } catch (Throwable th) {
      pass = false;
      sb.append("Wrong type of  Exception thrown" + th.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: replaceMatrixParamTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:216;
   * 
   * @test_Strategy: Calling UriBuilder.replaceMatrixParam(String name,
   * Object... value). verify all values are cleared when value is null.
   */
  @Test
  public void replaceMatrixParamTest2() throws Fault {
    String name = "name";

    try {
      uri = UriBuilder.fromPath(LOCALHOST)
          .matrixParam(name, "x=", "y?", "x y", "&").replaceMatrixParam(name)
          .build();
      gotExpectedPass(uri.toString(), LOCALHOST);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected Exception thrown" + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: replaceMatrixParamTest3
   * 
   * @assertion_ids: JAXRS:JAVADOC:216;
   * 
   * @test_Strategy: Calling UriBuilder.replaceMatrixParam(String name,
   * Object... value). verify that matrix parameter is updated accordingly
   */
  @Test
  public void replaceMatrixParamTest3() throws Fault {
    String name = "name";
    String expected = "http://localhost:8080;name=x;name=y;name=y%20x;name=x%25y;name=%20";

    try {
      uri = UriBuilder
          .fromPath("http://localhost:8080;name=x=;name=y?;name=x y;name=&")
          .replaceMatrixParam(name, "x", "y", "y x", "x%y", "%20").build();
      gotExpectedPass(uri.toString(), expected);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected Exception thrown" + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: replaceMatrixParamTest4
   * 
   * @assertion_ids: JAXRS:JAVADOC:216;
   * 
   * @test_Strategy: Calling UriBuilder.replaceMatrixParam(String name,
   * Object... value). verify that matrix parameter is updated accordingly
   */
  @Test
  public void replaceMatrixParamTest4() throws Fault {
    String name = "name1";
    String expected = "http://localhost:8080;name=x=;name=y%3F;name=x%20y;name=&;name1=x;name1=y;name1=y%20x;name1=x%25y;name1=%20";

    try {
      uri = UriBuilder
          .fromPath("http://localhost:8080;name=x=;name=y?;name=x y;name=&")
          .replaceMatrixParam(name, "x", "y", "y x", "x%y", "%20").build();
      gotExpectedPass(uri.toString(), expected);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected Exception thrown" + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: replaceMatrixTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:214;
   * 
   * @test_Strategy: Calling UriBuilder.replaceMatrix(String value). verify all
   * values are cleared when value is null.
   */
  @Test
  public void replaceMatrixTest1() throws Fault {
    String name = "name";
    String expected1 = "http://localhost:8080;";

    try {
      uri = UriBuilder.fromPath(LOCALHOST)
          .matrixParam(name, "x=", "y?", "x y", "&").replaceMatrix(null)
          .build();
      String sUri = uri.toString();
      boolean condition = sUri.compareToIgnoreCase(LOCALHOST) == 0
          || sUri.compareToIgnoreCase(expected1) == 0;
      gotExpectedPass(!condition, sUri, LOCALHOST);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected Exception thrown" + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: replaceMatrixTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:214;
   * 
   * @test_Strategy: Calling UriBuilder.replaceMatrix(String value). verify that
   * Matrix parameter is updated accordingly
   */
  @Test
  public void replaceMatrixTest2() throws Fault {
    String expected = "http://localhost:8080;name=x;name=y;name=y%20x;name=x%25y;name=%20";
    String value = "name=x;name=y;name=y x;name=x%y;name= ";

    try {
      uri = UriBuilder
          .fromPath("http://localhost:8080;name=x=;name=y?;name=x y;name=&")
          .replaceMatrix(value).build();
      gotExpectedPass(uri.toString(), expected);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected Exception thrown" + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: replaceMatrixTest3
   * 
   * @assertion_ids: JAXRS:JAVADOC:214;
   * 
   * @test_Strategy: Calling UriBuilder.replaceMatrix(String value). verify that
   * matrix parameter is updated accordingly
   */
  @Test
  public void replaceMatrixTest3() throws Fault {
    String expected = "http://localhost:8080;name1=x;name1=y;name1=y%20x;name1=x%25y;name1=%20";
    String value = "name1=x;name1=y;name1=y x;name1=x%y;name1= ";

    try {
      uri = UriBuilder
          .fromPath("http://localhost:8080;name=x=;name=y?;name=x y;name=&")
          .replaceMatrix(value).build();
      gotExpectedPass(uri.toString(), expected);
    } catch (Exception ex) {
      pass = false;
      sb.append("Unexpected Exception thrown" + ex.getMessage());
    }

    assertPassAndLog();
  }

  /*
   * @testName: userInfoTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:231;
   * 
   * @test_Strategy: call UriBuilder.userInfo in different states of UriBuilder
   * Check the userInfo on built java.net.URI
   */
  @Test
  public void userInfoTest() throws Fault {
    String unexpected = "Unexpected user info:";
    String userInfo = "foo:foo";

    uri = UriBuilder.fromUri(LOCALHOST).build();
    assertTrue(uri.getUserInfo() == null, unexpected+ uri.getUserInfo());

    uri = UriBuilder.fromUri(LOCALHOST).userInfo(userInfo).build();
    assertTrue(uri.getRawUserInfo().equals(userInfo), unexpected+
        uri.getRawUserInfo());
    System.out.println(uri.getRawUserInfo());

    uri = UriBuilder.fromUri("http://foo2:foo2@localhost:8080")
        .userInfo(userInfo).build();
    System.out.println(uri.getRawUserInfo());
    assertTrue(userInfo.equals(uri.getRawUserInfo()), unexpected+
        uri.getRawUserInfo());

    uri = UriBuilder.fromPath("").scheme("http").userInfo(userInfo)
        .host("localhost").port(8080).build();
    assertTrue(uri.getRawUserInfo().equals(userInfo), unexpected+
        uri.getRawUserInfo());
  }

  /*
   * @testName: buildObjectsBooleanEncodedTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:886;
   * 
   * @test_Strategy: Build a URI, using the supplied values in order to replace
   * any URI template parameters. Values are converted to String using their
   * toString() method and are then encoded to match the rules of the URI
   * component to which they pertain. The slash ('/') characters in parameter
   * values will be encoded if the template is placed in the URI path component.
   */
  @Test
  public void buildObjectsBooleanEncodedTest() throws Fault {
    Object s[] = { "path-rootless/test2", new StringBuilder("x%yz"),
        "/path-absolute/%25test1", "fred@example.com" };
    uri = UriBuilder.fromPath("").path("{v}/{w}/{x}/{y}/{w}")
        .build(new Object[] { s[0], s[1], s[2], s[3], s[1] }, true);
    gotExpectedPass(uri.getRawPath(), ENCODED_EXPECTED_PATH);
    assertPassAndLog();
  }

  /*
   * @testName: buildObjectsBooleanNotEncodedTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:886;
   * 
   * @test_Strategy: Build a URI, using the supplied values in order to replace
   * any URI template parameters. Values are converted to String using their
   * toString() method and are then encoded to match the rules of the URI
   * component to which they pertain.
   */
  @Test
  public void buildObjectsBooleanNotEncodedTest() throws Fault {
    Object s[] = { new StringBuffer("path-rootless/test2"), "x%yz",
        "/path-absolute", "test1", "fred@example.com" };
    uri = UriBuilder.fromPath("").path("{v}/{w}/{x}/{y}/{z}/{w}")
        .build(new Object[] { s[0], s[1], s[2], s[3], s[4], s[1] }, false);
    gotExpectedPass(uri.getRawPath(), EXPECTED_PATH);
    assertPassAndLog();
  }

  /*
   * @testName: buildObjectsBooleanThrowsIAEWhenNoValueSuppliedTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:887;
   * 
   * @test_Strategy: java.lang.IllegalArgumentException - if there are any URI
   * template parameters without a supplied value
   */
  @Test
  public void buildObjectsBooleanThrowsIAEWhenNoValueSuppliedTest()
      throws Fault {
    try {
      uri = UriBuilder.fromPath("").path("{v}/{w}")
          .build(new Object[] { "first" }, false);
      fault("IllegalArgumentException has not been thrown, uri:", uri);
    } catch (IllegalArgumentException e) {
      logMsg("IllegalArgumentException has been thrown as expected", e);
    }
  }

  /*
   * @testName: buildObjectsBooleanThrowsIAEWhenValueIsNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:887;
   * 
   * @test_Strategy: java.lang.IllegalArgumentException - if a value is null.
   */
  @Test
  public void buildObjectsBooleanThrowsIAEWhenValueIsNullTest() throws Fault {
    try {
      uri = UriBuilder.fromPath("").path("{v}/{w}")
          .build(new Object[] { "first", null }, false);
      fault("IllegalArgumentException has not been thrown");
    } catch (IllegalArgumentException e) {
      logMsg("IllegalArgumentException has been thrown as expected", e);
    }
  }

  /*
   * @testName: buildFromMapWithBooleanSlashEncodedTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:889;
   * 
   * @test_Strategy: Build a URI. Any URI template parameters will be replaced
   * by the value in the supplied map. Values are converted to String using
   * their toString() method and are then encoded to match the rules of the URI
   * component to which they pertain. All '%' characters in the stringified
   * values will be encoded. The state of the builder is unaffected; this method
   * may be called multiple times on the same builder instance. The slash ('/')
   * characters in parameter values will be encoded if the template is placed in
   * the URI path component.
   */
  @Test
  public void buildFromMapWithBooleanSlashEncodedTest() throws Fault {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("x", new StringBuilder("x%yz"));
    map.put("y", new StringBuffer("/path-absolute/%25test1"));
    map.put("z", new Object() {
      public String toString() {
        return "fred@example.com";
      }
    });
    map.put("w", "path-rootless/test2");
    UriBuilder builder = UriBuilder.fromPath("").path("{w}/{x}/{y}/{z}/{x}");
    builder.buildFromMap(map, false); // can be called multiple times
    uri = builder.buildFromMap(map, true);
    gotExpectedPass(uri.getRawPath(), ENCODED_EXPECTED_PATH);
    assertPassAndLog();
  }

  /*
   * @testName: buildFromMapWithBooleanSlashNotEncodedTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:889;
   * 
   * @test_Strategy: Build a URI. Any URI template parameters will be replaced
   * by the value in the supplied map. Values are converted to String using
   * their toString() method and are then encoded to match the rules of the URI
   * component to which they pertain. All '%' characters in the stringified
   * values will be encoded. The state of the builder is unaffected; this method
   * may be called multiple times on the same builder instance.
   */
  @Test
  public void buildFromMapWithBooleanSlashNotEncodedTest() throws Fault {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("x", new StringBuilder("x%yz"));
    map.put("y", new StringBuffer("/path-absolute/test1"));
    map.put("z", new Object() {
      public String toString() {
        return "fred@example.com";
      }
    });
    map.put("w", "path-rootless/test2");
    UriBuilder builder = UriBuilder.fromPath("").path("{w}/{x}/{y}/{z}/{x}");
    uri = builder.buildFromMap(map, false);
    gotExpectedPass(uri.getRawPath(), EXPECTED_PATH);
    assertPassAndLog();
  }

  /*
   * @testName: buildFromMapWithBooleanThrowsIAEWhenNoSuppliedValueTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:890;
   * 
   * @test_Strategy: java.lang.IllegalArgumentException - if there are any URI
   * template parameters without a supplied value
   */
  @Test
  public void buildFromMapWithBooleanThrowsIAEWhenNoSuppliedValueTest()
      throws Fault {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("x", new StringBuilder("x%yz"));
    map.put("y", new StringBuffer("/path-absolute/test1"));
    map.put("w", "path-rootless/test2");
    UriBuilder builder = UriBuilder.fromPath("").path("{w}/{x}/{y}/{z}/{x}");
    try {
      uri = builder.buildFromMap(map, false);
      fault("No exception has been thrown");
    } catch (IllegalArgumentException e) {
      logMsg("IllegalArgumentException has been thrown as expected");
    }
  }

  /*
   * @testName: buildFromMapWithBooleanThrowsIAEWhenNullValueTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:890;
   * 
   * @test_Strategy: java.lang.IllegalArgumentException - if a template
   * parameter value is null.
   */
  @Test
  public void buildFromMapWithBooleanThrowsIAEWhenNullValueTest() throws Fault {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("x", new StringBuilder("x%yz"));
    map.put("y", new StringBuffer("/path-absolute/test1"));
    map.put("z", null);
    map.put("w", "path-rootless/test2");
    UriBuilder builder = UriBuilder.fromPath("").path("{w}/{x}/{y}/{z}/{x}");
    try {
      uri = builder.buildFromMap(map, false);
      fault("No exception has been thrown");
    } catch (IllegalArgumentException e) {
      logMsg("IllegalArgumentException has been thrown as expected");
    }
  }

  /*
   * @testName: fromLinkTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:892;
   * 
   * @test_Strategy: Create a new instance initialized from a Link.
   */
  @Test
  public void fromLinkTest() throws Fault {
    URI uri = UriBuilder.fromUri(LOCALHOST).build();
    Link link = Link.fromUri(uri).build();
    URI uri2 = UriBuilder.fromLink(link).build();
    assertTrue(uri.equals(uri2), "URI"+ uri+ "and"+ uri2+ "are not equal");
    logMsg("URI fromLink is equal to the expected URI");
  }

  /*
   * @testName: fromLinkThrowsIllegalArgumentExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:892;
   * 
   * @test_Strategy: throws IllegalArgumentException - if link is {@code null}
   */
  @Test
  public void fromLinkThrowsIllegalArgumentExceptionTest() throws Fault {
    try {
      UriBuilder.fromLink((Link) null);
      throw new Fault("IllegalArgumentException has not been thrown");
    } catch (IllegalArgumentException e) {
      logMsg("IllegalArgumentException has been thrown as expected");
    }
  }

  /*
   * @testName: fromMethodTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:894;
   * 
   * @test_Strategy: Create a new instance representing a relative URI
   * initialized from a Path-annotated method. This method can only be used in
   * cases where there is a single method with the specified name that is
   * annotated with Path.
   */
  @Test
  public void fromMethodTest() throws Fault {
    URI uri = UriBuilder.fromMethod(TestPath.class, "headSub").build();
    assertTrue(uri.toASCIIString().equals("/sub"),
        "There is no /sub in the URI");
    logMsg("URI fromMethod is equal to the expected URI", uri);
  }

  /*
   * @testName: fromMethodThrowsIllegalArgumentExceptionWhenMorePathsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:894;
   * 
   * @test_Strategy: Throws: IllegalArgumentException - if resource or method is
   * null, or there is more than or less than one variant of the method
   * annotated with Path.
   */
  @Test
  public void fromMethodThrowsIllegalArgumentExceptionWhenMorePathsTest()
      throws Fault {
    try {
      UriBuilder.fromMethod(TestPath.class, "test1");
      throw new Fault("IllegalArgumentException has not been thrown");
    } catch (IllegalArgumentException e) {
      logMsg("IllegalArgumentException has been sucessfully thrown", e);
    }
  }

  /*
   * @testName: fromMethodThrowsIllegalArgumentExceptionWhenNoPathTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:894;
   * 
   * @test_Strategy: Throws: IllegalArgumentException - if resource or method is
   * null, or there is more than or less than one variant of the method
   * annotated with Path.
   */
  @Test
  public void fromMethodThrowsIllegalArgumentExceptionWhenNoPathTest()
      throws Fault {
    try {
      UriBuilder.fromMethod(TestPath.class, "getPlain");
      throw new Fault("IllegalArgumentException has not been thrown");
    } catch (IllegalArgumentException e) {
      logMsg("IllegalArgumentException has been sucessfully thrown", e);
    }
  }

  /*
   * @testName: toTemplateTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:896;
   * 
   * @test_Strategy: Get the URI template string represented by this URI
   * builder.
   */
  @Test
  public void toTemplateTest() throws Fault {
    String template = "{v}/{w}/{x}/{y}/{w}";
    UriBuilder builder = UriBuilder.fromPath("").path(template);
    assertEquals(template, builder.toTemplate(), "Given template", template,
        "differs from obtain", builder.toTemplate());
    logMsg("Got expected template", template);
  }

  /*
   * @testName: uriStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:897;
   * 
   * @test_Strategy: Parses the string and copies the parsed components of the
   * supplied URI to the UriBuilder replacing any existing values for those
   * components.
   */
  @Test
  public void uriStringTest() throws Fault {
    String origUris[] = getOrigUris();
    URI[] replaceUris = getReplacementUris();
    String[] expectUris = getExpectedUris();

    int cnt = 0;
    int failed = 0;
    int passed = 0;
    while (cnt < 17) {
      try {
        sb.append("Replace uri ").append(origUris[cnt]).append(" with ")
            .append(replaceUris[cnt].toASCIIString()).append(newline);
        uri = UriBuilder.fromUri(new URI(origUris[cnt]))
            .uri(replaceUris[cnt].toASCIIString()).build();
        if (gotExpectedPass(uri.toString().trim(), expectUris[cnt]) != 0)
          failed++;
        else
          passed++;
      } catch (Exception ex) {
        pass = false;
        sb.append("Unexpected exception thrown: ").append(ex.getMessage())
            .append(newline);
        sb.append("Test failed with exception for expected uri: ")
            .append(expectUris[cnt]).append(newline);
        failed++;
      }
      cnt++;
    }

    assertTrue(pass, passed+ "assertion passed."+ newline+ failed+
        "assertion(s) failed:"+ sb.toString()+ newline);
  }

  /*
   * @testName: uriStringThrowsIAEWhenNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:896;
   * 
   * @test_Strategy: throws IllegalArgumentException - if URI template or is
   * {@code null}.
   */
  @Test
  public void uriStringThrowsIAEWhenNullTest() throws Fault {
    try {
      UriBuilder.fromMethod(TestPath.class, "headSub").uri((String) null);
      throw new Fault("No Exception has been thrown for #uri(null)");
    } catch (IllegalArgumentException e) {
      logMsg(
          "#IllegalArgumentException has been thrown as expected for #uri(null)");
    }
  }

  /*
   * @testName: uriStringThrowsIAEWhenNoUriTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:896;
   * 
   * @test_Strategy: throws IllegalArgumentException - if {@code uriTemplate} is
   * not a valid URI template
   */
  @Test
  public void uriStringThrowsIAEWhenNoUriTest() throws Fault {
    String sUri = "://";
    try {
      uri = UriBuilder.fromUri(new URI("news//:comp.lang.java")).uri(sUri)
          .build();
      fault("No Exception has been thrown for #uri(noURI)", uri);
    } catch (IllegalArgumentException e) {
      logMsg(
          "#IllegalArgumentException has been thrown as expected for #uri(noURI)");
    } catch (URISyntaxException e) {
      throw new Fault(e);
    }
  }

  /*
   * @testName: resolveTemplateStringObjectTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:957;
   * 
   * @test_Strategy: Resolve a URI template with a given name in this UriBuilder
   * instance using a supplied value
   */
  @Test
  public void resolveTemplateStringObjectTest() throws Fault {
    String template = "{v}/{w}/{x}/{y}/{w}";
    UriBuilder builder = UriBuilder.fromPath("").path(template)
        .resolveTemplate("v", new StringBuilder("aaa"));
    String resolvedTemplate = template.replace("{v}", "aaa");
    String builderTemplate = builder.toTemplate();
    assertEquals(resolvedTemplate, builderTemplate, "Given template", template,
        "was not resolved correctly, remains", builderTemplate);
    logMsg("Got expected template", template);
  }

  /*
   * @testName: resolveTemplateStringObjectThrowsIAEOnNullNameTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:957;
   * 
   * @test_Strategy: if the resolved template name or value is null.
   */
  @Test
  public void resolveTemplateStringObjectThrowsIAEOnNullNameTest()
      throws Fault {
    String template = "{v}/{w}/{x}/{y}/{w}";
    UriBuilder builder = UriBuilder.fromPath("").path(template);
    try {
      builder.resolveTemplate(null, "aaa");
      fault("No exception has been thrown");
    } catch (IllegalArgumentException e) {
      logMsg("IllegalArgumentException has been thrown as expected", e);
    }
  }

  /*
   * @testName: resolveTemplateStringObjectThrowsIAEOnNullValueTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:957;
   * 
   * @test_Strategy: if the resolved template name or value is null.
   */
  @Test
  public void resolveTemplateStringObjectThrowsIAEOnNullValueTest()
      throws Fault {
    String template = "{v}/{w}/{x}/{y}/{w}";
    UriBuilder builder = UriBuilder.fromPath("").path(template);
    try {
      builder.resolveTemplate("v", null);
      fault("No exception has been thrown");
    } catch (IllegalArgumentException e) {
      logMsg("IllegalArgumentException has been thrown as expected", e);
    }
  }

  /*
   * @testName: resolveTemplateStringObjectBooleanSlashEncodedTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:959;
   * 
   * @test_Strategy: Resolve a URI template with a given name in this UriBuilder
   * instance using a supplied value. The slash ('/') characters in template
   * values will be encoded.
   */
  @Test
  public void resolveTemplateStringObjectBooleanSlashEncodedTest()
      throws Fault {
    String template = "{v}/{w}/{x}/{y}/{w}";
    UriBuilder builder = UriBuilder.fromPath("").path(template)
        .resolveTemplate("v", new StringBuilder("a/a/a"), true);
    String resolvedTemplate = template.replace("{v}", "a%2Fa%2Fa");
    String builderTemplate = builder.toTemplate().replace("%2f", "%2F");
    assertEquals(resolvedTemplate, builderTemplate, "Given template", template,
        "was not resolved correctly, remains", builderTemplate);
    logMsg("Got expected template", template);
  }

  /*
   * @testName: resolveTemplateStringObjectBooleanSlashNotEncodedTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:959;
   * 
   * @test_Strategy: Resolve a URI template with a given name in this UriBuilder
   * instance using a supplied value.
   */
  @Test
  public void resolveTemplateStringObjectBooleanSlashNotEncodedTest()
      throws Fault {
    String template = "{v}/{w}/{x}/{y}/{w}";
    UriBuilder builder = UriBuilder.fromPath("").path(template)
        .resolveTemplate("v", new StringBuilder("a/a/a"), false);
    String resolvedTemplate = template.replace("{v}", "a/a/a");
    String builderTemplate = builder.toTemplate();
    assertEquals(resolvedTemplate, builderTemplate, "Given template", template,
        "was not resolved correctly, remains", builderTemplate);
    logMsg("Got expected template", template);
  }

  /*
   * @testName: resolveTemplateStringObjectBooleanThrowsIAEOnNullNameTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:959;
   * 
   * @test_Strategy: if the resolved template name or value is null.
   */
  @Test
  public void resolveTemplateStringObjectBooleanThrowsIAEOnNullNameTest()
      throws Fault {
    String template = "{v}/{w}/{x}/{y}/{w}";
    UriBuilder builder = UriBuilder.fromPath("").path(template);
    try {
      builder.resolveTemplate(null, "aaa", false);
      fault("No exception has been thrown");
    } catch (IllegalArgumentException e) {
      logMsg("IllegalArgumentException has been thrown as expected", e);
    }
  }

  /*
   * @testName: resolveTemplateStringObjectBooleanThrowsIAEOnNullValueTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:959;
   * 
   * @test_Strategy: if the resolved template name or value is null.
   */
  @Test
  public void resolveTemplateStringObjectBooleanThrowsIAEOnNullValueTest()
      throws Fault {
    String template = "{v}/{w}/{x}/{y}/{w}";
    UriBuilder builder = UriBuilder.fromPath("").path(template);
    try {
      builder.resolveTemplate("v", null, false);
      fault("No exception has been thrown");
    } catch (IllegalArgumentException e) {
      logMsg("IllegalArgumentException has been thrown as expected", e);
    }
  }

  /*
   * @testName: resolveTemplateFromEncodedTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:961;
   * 
   * @test_Strategy: Resolve a URI template with a given name in this UriBuilder
   * instance using a supplied encoded value.
   */
  @Test
  public void resolveTemplateFromEncodedTest() throws Fault {
    Object s[] = { "path-rootless%2Ftest2", new StringBuilder("x%25yz"),
        "%2Fpath-absolute%2F%2525test1", "fred@example.com" };
    UriBuilder builder = UriBuilder.fromPath("").path("{v}/{w}/{x}/{y}/{w}");
    builder = builder.resolveTemplateFromEncoded("v", s[0]);
    builder = builder.resolveTemplateFromEncoded("w", s[1]);
    builder = builder.resolveTemplateFromEncoded("x", s[2]);
    builder = builder.resolveTemplateFromEncoded("y", s[3]);
    uri = builder.build();
    gotExpectedPass(uri.getRawPath(), ENCODED_EXPECTED_PATH);
    assertPassAndLog();
  }

  /*
   * @testName: resolveTemplateFromEncodedPercentEncodedTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:961;
   * 
   * @test_Strategy: Resolve a URI template with a given name in this UriBuilder
   * instance using a supplied encoded value. All % characters in the
   * stringified values that are not followed by two hexadecimal numbers will be
   * encoded.
   */
  @Test
  public void resolveTemplateFromEncodedPercentEncodedTest() throws Fault {
    Object s[] = { "path-rootless%2Ftest2", new StringBuilder("x%yz"),
        "%2Fpath-absolute%2F%2525test1", "fred@example.com" };
    UriBuilder builder = UriBuilder.fromPath("").path("{v}/{w}/{x}/{y}/{w}");
    builder = builder.resolveTemplateFromEncoded("v", s[0]);
    builder = builder.resolveTemplateFromEncoded("w", s[1]);
    builder = builder.resolveTemplateFromEncoded("x", s[2]);
    builder = builder.resolveTemplateFromEncoded("y", s[3]);
    uri = builder.build();
    gotExpectedPass(uri.getRawPath(), ENCODED_EXPECTED_PATH);
    assertPassAndLog();
  }

  /*
   * @testName: resolveTemplateFromEncodedThrowsNullOnNullNameTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:961;
   * 
   * @test_Strategy: java.lang.IllegalArgumentException - if the resolved
   * template name or encoded value is null.
   */
  @Test
  public void resolveTemplateFromEncodedThrowsNullOnNullNameTest()
      throws Fault {
    Object s[] = { "path-rootless%2Ftest2", new StringBuilder("x%25yz"),
        "%2Fpath-absolute%2F%2525test1", "fred@example.com" };
    UriBuilder builder = UriBuilder.fromPath("").path("{v}/{w}/{x}/{y}/{w}");
    try {
      builder.resolveTemplateFromEncoded(null, s[0]);
      fault("No exception has been thrown");
    } catch (IllegalArgumentException e) {
      logMsg("IllegalArgumentException thrown as expected", e);
    }
  }

  /*
   * @testName: resolveTemplateFromEncodedThrowsNullOnNullValueTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:961;
   * 
   * @test_Strategy: java.lang.IllegalArgumentException - if the resolved
   * template name or encoded value is null.
   */
  @Test
  public void resolveTemplateFromEncodedThrowsNullOnNullValueTest()
      throws Fault {
    UriBuilder builder = UriBuilder.fromPath("").path("{v}/{w}/{x}/{y}/{w}");
    try {
      builder.resolveTemplateFromEncoded("v", (Object) null);
      fault("No exception has been thrown");
    } catch (IllegalArgumentException e) {
      logMsg("IllegalArgumentException thrown as expected", e);
    }
  }

  /*
   * @testName: resolveTemplatesMapTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:963;
   * 
   * @test_Strategy: Resolve one or more URI templates in this UriBuilder
   * instance using supplied name-value pairs.
   */
  @Test
  public void resolveTemplatesMapTest() throws Fault {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("x", new StringBuilder("x%yz"));
    map.put("y", new StringBuffer("/path-absolute/%25test1"));
    map.put("z", new Object() {
      public String toString() {
        return "fred@example.com";
      }
    });
    map.put("w", "path-rootless/test2");
    UriBuilder builder = UriBuilder.fromPath("").path("{w}/{x}/{y}/{z}/{x}");
    uri = builder.resolveTemplates(map).build();
    gotExpectedPass(uri.getRawPath(), ENCODED_EXPECTED_PATH);
    assertPassAndLog();
  }

  /*
   * @testName: resolveTemplatesMapThrowsIAEOnNullNameTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:963;
   * 
   * @test_Strategy:java.lang.IllegalArgumentException - if the name-value map
   * or any of the names or values in the map is null.
   */
  @Test
  public void resolveTemplatesMapThrowsIAEOnNullNameTest() throws Fault {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("a", new StringBuilder("x%yz"));
    map.put(null, "path-rootless/test2");
    UriBuilder builder = UriBuilder.fromPath("").path("{a}/{b}");
    try {
      builder.resolveTemplates(map);
      fault("IllegalArgumentException has not been thrown");
    } catch (IllegalArgumentException e) {
      logMsg("IllegalArgumentException has been thrown as expected", e);
    }
  }

  /*
   * @testName: resolveTemplatesMapThrowsIAEOnNullValueTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:963;
   * 
   * @test_Strategy:java.lang.IllegalArgumentException - if the name-value map
   * or any of the names or values in the map is null.
   */
  @Test
  public void resolveTemplatesMapThrowsIAEOnNullValueTest() throws Fault {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("a", null);
    map.put("b", "path-rootless/test2");
    UriBuilder builder = UriBuilder.fromPath("").path("{a}/{b}");
    try {
      builder.resolveTemplates(map);
      fault("IllegalArgumentException has not been thrown");
    } catch (IllegalArgumentException e) {
      logMsg("IllegalArgumentException has been thrown as expected", e);
    }
  }

  /*
   * @testName: resolveTemplatesMapBooleanSlashEncodedTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:965;
   * 
   * @test_Strategy: Resolve one or more URI templates in this UriBuilder
   * instance using supplied name-value pairs.
   */
  @Test
  public void resolveTemplatesMapBooleanSlashEncodedTest() throws Fault {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("x", new StringBuilder("x%yz"));
    map.put("y", new StringBuffer("/path-absolute/%25test1"));
    map.put("z", new Object() {
      public String toString() {
        return "fred@example.com";
      }
    });
    map.put("w", "path-rootless/test2");
    UriBuilder builder = UriBuilder.fromPath("").path("{w}/{x}/{y}/{z}/{x}");
    uri = builder.resolveTemplates(map, true).build();
    gotExpectedPass(uri.getRawPath(), ENCODED_EXPECTED_PATH);
    assertPassAndLog();
  }

  /*
   * @testName: resolveTemplatesMapBooleanSlashNotEncodedTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:965;
   * 
   * @test_Strategy: Resolve one or more URI templates in this UriBuilder
   * instance using supplied name-value pairs.
   */
  @Test
  public void resolveTemplatesMapBooleanSlashNotEncodedTest() throws Fault {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("x", new StringBuilder("x%yz"));
    map.put("y", new StringBuffer("/path-absolute/test1"));
    map.put("z", new Object() {
      public String toString() {
        return "fred@example.com";
      }
    });
    map.put("w", "path-rootless/test2");
    UriBuilder builder = UriBuilder.fromPath("").path("{w}/{x}/{y}/{z}/{x}");
    uri = builder.resolveTemplates(map, false).build();
    gotExpectedPass(uri.getRawPath(), EXPECTED_PATH);
    assertPassAndLog();
  }

  /*
   * @testName: resolveTemplatesMapBooleanThrowsIAEOnNullNameTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:966;
   * 
   * @test_Strategy:java.lang.IllegalArgumentException - if the name-value map
   * or any of the names or values in the map is null.
   */
  @Test
  public void resolveTemplatesMapBooleanThrowsIAEOnNullNameTest() throws Fault {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("a", new StringBuilder("x%yz"));
    map.put(null, "path-rootless/test2");
    UriBuilder builder = UriBuilder.fromPath("").path("{a}/{b}");
    try {
      builder.resolveTemplates(map, true);
      fault("IllegalArgumentException has not been thrown");
    } catch (IllegalArgumentException e) {
      logMsg("IllegalArgumentException has been thrown as expected", e);
    }
  }

  /*
   * @testName: resolveTemplatesMapBooleanThrowsIAEOnNullValueTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:966;
   * 
   * @test_Strategy:java.lang.IllegalArgumentException - if the name-value map
   * or any of the names or values in the map is null.
   */
  @Test
  public void resolveTemplatesMapBooleanThrowsIAEOnNullValueTest()
      throws Fault {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("a", null);
    map.put("b", "path-rootless/test2");
    UriBuilder builder = UriBuilder.fromPath("").path("{a}/{b}");
    try {
      builder.resolveTemplates(map, false);
      fault("IllegalArgumentException has not been thrown");
    } catch (IllegalArgumentException e) {
      logMsg("IllegalArgumentException has been thrown as expected", e);
    }
  }

  /*
   * @testName: resolveTemplatesMapBooleanThrowsIAEOnNullMapTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:966;
   * 
   * @test_Strategy:java.lang.IllegalArgumentException - if the name-value map
   * or any of the names or values in the map is null.
   */
  @Test
  public void resolveTemplatesMapBooleanThrowsIAEOnNullMapTest() throws Fault {
    UriBuilder builder = UriBuilder.fromPath("").path("{a}/{b}");
    try {
      builder.resolveTemplates((Map<String, Object>) null, false);
      fault("IllegalArgumentException has not been thrown");
    } catch (IllegalArgumentException e) {
      logMsg("IllegalArgumentException has been thrown as expected", e);
    }
  }

  /*
   * @testName: resolveTemplatesFromEncodedTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:967;
   * 
   * @test_Strategy: Resolve one or more URI templates in this instance using
   * supplied name-value pairs.
   */
  @Test
  public void resolveTemplatesFromEncodedTest() throws Fault {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("v", new StringBuilder("path-rootless%2Ftest2"));
    map.put("w", new StringBuilder("x%25yz"));
    map.put("x", new Object() {
      public String toString() {
        return "%2Fpath-absolute%2F%2525test1";
      }
    });
    map.put("y", "fred@example.com");
    UriBuilder builder = UriBuilder.fromPath("").path("{v}/{w}/{x}/{y}/{w}");
    builder = builder.resolveTemplatesFromEncoded(map);
    uri = builder.build();
    gotExpectedPass(uri.getRawPath(), ENCODED_EXPECTED_PATH);
    assertPassAndLog();
  }

  /*
   * @testName: resolveTemplatesFromEncodedPercentEncodedTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:967;
   * 
   * @test_Strategy: Resolve one or more URI templates in this instance using
   * supplied name-value pairs. All % characters in the stringified values that
   * are not followed by two hexadecimal numbers will be encoded.
   */
  @Test
  public void resolveTemplatesFromEncodedPercentEncodedTest() throws Fault {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("v", new StringBuilder("path-rootless%2Ftest2"));
    map.put("w", new StringBuilder("x%yz"));
    map.put("x", new Object() {
      public String toString() {
        return "%2Fpath-absolute%2F%2525test1";
      }
    });
    map.put("y", "fred@example.com");
    UriBuilder builder = UriBuilder.fromPath("").path("{v}/{w}/{x}/{y}/{w}");
    builder = builder.resolveTemplatesFromEncoded(map);
    uri = builder.build();
    gotExpectedPass(uri.getRawPath(), ENCODED_EXPECTED_PATH);
    assertPassAndLog();
  }

  /*
   * @testName: resolveTemplatesFromEncodedThrowsNullOnNullNameTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:967;
   * 
   * @test_Strategy: java.lang.IllegalArgumentException - if the name-value map
   * or any of the names or values in the map is null.
   */
  @Test
  public void resolveTemplatesFromEncodedThrowsNullOnNullNameTest()
      throws Fault {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put(null, "aa");
    UriBuilder builder = UriBuilder.fromPath("").path("{v}/{w}/{x}/{y}/{w}");
    try {
      builder.resolveTemplatesFromEncoded(map);
      fault("No exception has been thrown");
    } catch (IllegalArgumentException e) {
      logMsg("IllegalArgumentException thrown as expected", e);
    }
  }

  /*
   * @testName: resolveTemplatesFromEncodedThrowsNullOnNullValueTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:967;
   * 
   * @test_Strategy: java.lang.IllegalArgumentException - if the name-value map
   * or any of the names or values in the map is null.
   */
  @Test
  public void resolveTemplatesFromEncodedThrowsNullOnNullValueTest()
      throws Fault {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("v", null);
    UriBuilder builder = UriBuilder.fromPath("").path("{v}/{w}/{x}/{y}/{w}");
    try {
      builder.resolveTemplatesFromEncoded(map);
      fault("No exception has been thrown");
    } catch (IllegalArgumentException e) {
      logMsg("IllegalArgumentException thrown as expected", e);
    }
  }

  /*
   * @testName: resolveTemplatesFromEncodedThrowsNullOnNullMapTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:967;
   * 
   * @test_Strategy: java.lang.IllegalArgumentException - if the name-value map
   * or any of the names or values in the map is null.
   */
  @Test
  public void resolveTemplatesFromEncodedThrowsNullOnNullMapTest()
      throws Fault {
    UriBuilder builder = UriBuilder.fromPath("").path("{v}/{w}/{x}/{y}/{w}");
    try {
      builder.resolveTemplatesFromEncoded((Map<String, Object>) null);
      fault("No exception has been thrown");
    } catch (IllegalArgumentException e) {
      logMsg("IllegalArgumentException thrown as expected", e);
    }
  }


  // ////////////////////////////////////////////////////////////////////////
  /**
   * This is the pattern used in all over the tests. Add to stringBuilder what's
   * expected, what's got and set pass value.
   * 
   * @param got
   * @param expected
   * @return got.compareToIgnoreCase(expected)
   */
  private int gotExpectedPass(String got, String expected) {
    got = got.replace("%2f", "%2F");
    int compare = got.compareToIgnoreCase(expected);
    gotExpectedPass(compare != 0, got, expected);
    return compare;
  }

  private boolean gotExpectedPass(boolean conditionFalse, String got,
      String expected) {
    if (conditionFalse) {
      pass = false;
      sb.append("Incorrect URI returned: ").append(got);
      sb.append(", expecting ").append(expected);
    } else
      sb.append("Got expected return: ").append(expected);
    sb.append(newline);
    return conditionFalse;

  }

  private void assertPassAndLog() throws Fault {
    assertTrue(pass, "At least one assertion failed:"+ sb.toString());    
    TestUtil.logTrace(sb.toString());
  }

  private static String[] getOrigUris() {
    String[] uris_orig = { "ftp://ftp.is.co.za/rfc/rfc1808.txt",
        "ftp://ftp.is.co.za/rfc/rfc1808.txt", "mailto:java-net@java.sun.com",
        "mailto:java-net@java.sun.com", "news:comp.lang.java",
        "news:comp.lang.java", "urn:isbn:096139210x",
        "http://www.ietf.org/rfc/rfc2396.txt",
        "http://www.ietf.org/rfc/rfc2396.txt",
        "ldap://[2001:db8::7]/c=GB?objectClass?one",
        "ldap://[2001:db8::7]/c=GB?objectClass?one", "tel:+1-816-555-1212",
        "tel:+1-816-555-1212", "telnet://192.0.2.16:80/",
        "telnet://192.0.2.16:80/",
        "foo://example.com:8042/over/there?name=ferret#nose",
        "foo://example.com:8042/over/there?name=ferret#nose" };
    return uris_orig;
  }

  private static URI[] getReplacementUris() throws Fault {
    URI uris_replace[] = { null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null };

    try {
      uris_replace[0] = new URI("http", "//ftp.is.co.za/rfc/rfc1808.txt", null);
      uris_replace[1] = new URI(null, "ftp.is.co.za", "/test/rfc1808.txt", null,
          null);
      uris_replace[2] = new URI("mailto", "java-net@java.sun.com", null);
      uris_replace[3] = new URI(null, "testuser@sun.com", null);
      uris_replace[4] = new URI("http", "//comp.lang.java", null);
      uris_replace[5] = new URI(null, "news.lang.java", null);
      uris_replace[6] = new URI("urn:isbn:096139210x");
      uris_replace[7] = new URI(null, "//www.ietf.org/rfc/rfc2396.txt", null);
      uris_replace[8] = new URI(null, "www.ietf.org", "/rfc/rfc2396.txt", null,
          null);
      uris_replace[9] = new URI("ldap", "//[2001:db8::7]/c=GB?objectClass?one",
          null);
      uris_replace[10] = new URI(null, "//[2001:db8::7]/c=GB?objectClass?one",
          null);
      uris_replace[11] = new URI("tel", "+1-816-555-1212", null);
      uris_replace[12] = new URI(null, "+1-866-555-1212", null);
      uris_replace[13] = new URI("telnet", "//192.0.2.16:80/", null);
      uris_replace[14] = new URI(null, "//192.0.2.16:81/", null);
      uris_replace[15] = new URI("http",
          "//example.com:8042/over/there?name=ferret", null);
      uris_replace[16] = new URI(null,
          "//example.com:8042/over/there?name=ferret", "mouth");
    } catch (Exception ex) {
      throw new Fault(
          "========== Exception thrown constructing replacing URIs: "
              + ex.getMessage() + newline);
    }
    return uris_replace;
  }

  private static String[] getExpectedUris() {
    String[] uris_expect = { "http://ftp.is.co.za/rfc/rfc1808.txt",
        "ftp://ftp.is.co.za/test/rfc1808.txt", "mailto:java-net@java.sun.com",
        "mailto:testuser@sun.com", "http://comp.lang.java",
        "news:news.lang.java", "urn:isbn:096139210x",
        "http://www.ietf.org/rfc/rfc2396.txt",
        "http://www.ietf.org/rfc/rfc2396.txt",
        "ldap://[2001:db8::7]/c=GB?objectClass?one",
        "ldap://[2001:db8::7]/c=GB?objectClass?one", "tel:+1-816-555-1212",
        "tel:+1-866-555-1212", "telnet://192.0.2.16:80/",
        "telnet://192.0.2.16:81/",
        "http://example.com:8042/over/there?name=ferret#nose",
        "foo://example.com:8042/over/there?name=ferret#mouth" };
    return uris_expect;
  }

}
