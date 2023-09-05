/*
 * Copyright (c) 2007, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.api.rs.core.newcookie;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.NewCookie;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
public class JAXRSClientIT extends JAXRSCommonClient {

  private static final long serialVersionUID = 295711558453778471L;

  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : "+testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : "+testInfo.getDisplayName());
  }

  /*
   * @testName: constructorTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:107; JAXRS:JAVADOC:100; JAXRS:JAVADOC:101;
   * JAXRS:JAVADOC:103; JAXRS:JAVADOC:51; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54;
   * JAXRS:JAVADOC:55; JAXRS:JAVADOC:56;
   * 
   * @test_Strategy: Create a NewCookie instance using constructor
   * Cookie(String, String) NewCookie(Cookie)
   */
  @Test
  public void constructorTest1() throws Fault {
    String name = "name_1";
    String value = "value_1";
    String comment = "";
    String domain = "";
    String path = "";
    int maxage = jakarta.ws.rs.core.NewCookie.DEFAULT_MAX_AGE;
    int version = 1;
    boolean secure = false;

    Cookie ck1 = new Cookie(name, value);
    NewCookie nck1 = new NewCookie(ck1);

    verifyNewCookie(nck1, name, value, path, domain, version, comment, maxage,
        secure);
  }

  /*
   * @testName: constructorTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:107; JAXRS:JAVADOC:100; JAXRS:JAVADOC:101;
   * JAXRS:JAVADOC:103; JAXRS:JAVADOC:50; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54;
   * JAXRS:JAVADOC:55; JAXRS:JAVADOC:56;
   * 
   * @test_Strategy: Create a NewCookie instance using constructor
   * Cookie(String, String, String, String) NewCookie(Cookie)
   */
  @Test
  public void constructorTest2() throws Fault {
    String name = "name_1";
    String value = "value_1";
    String path = "/acme";
    String domain = "";
    String comment = "";
    int maxage = jakarta.ws.rs.core.NewCookie.DEFAULT_MAX_AGE;
    int version = 1;
    boolean secure = false;

    Cookie ck2 = new Cookie(name, value, path, domain);
    NewCookie nck2 = new NewCookie(ck2);

    verifyNewCookie(nck2, name, value, path, domain, version, comment, maxage,
        secure);
  }

  /*
   * @testName: constructorTest3
   * 
   * @assertion_ids: JAXRS:JAVADOC:107; JAXRS:JAVADOC:100; JAXRS:JAVADOC:101;
   * JAXRS:JAVADOC:103; JAXRS:JAVADOC:50; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54;
   * JAXRS:JAVADOC:55; JAXRS:JAVADOC:56;
   * 
   * @test_Strategy: Create a NewCookie instance using constructor
   * Cookie(String, String, String, String) NewCookie(Cookie)
   */
  @Test
  public void constructorTest3() throws Fault {
    String name = "name_1";
    String value = "value_1";
    String path = "";
    String domain = "y.x.foo.com";
    String comment = "";
    int maxage = jakarta.ws.rs.core.NewCookie.DEFAULT_MAX_AGE;
    int version = 1;
    boolean secure = false;

    Cookie ck3 = new Cookie(name, value, path, domain);
    NewCookie nck3 = new NewCookie(ck3);

    verifyNewCookie(nck3, name, value, path, domain, version, comment, maxage,
        secure);
  }

  /*
   * @testName: constructorTest4
   * 
   * @assertion_ids: JAXRS:JAVADOC:107; JAXRS:JAVADOC:100; JAXRS:JAVADOC:101;
   * JAXRS:JAVADOC:103; JAXRS:JAVADOC:49; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54;
   * JAXRS:JAVADOC:55; JAXRS:JAVADOC:56;
   * 
   * @test_Strategy: Create a version 1 NewCookie instance using constructor
   * Cookie(String, String, String, String, int) NewCookie(Cookie)
   */
  @Test
  public void constructorTest4() throws Fault {
    String name = "name_1";
    String value = "value_1";
    String path = "/acme";
    String domain = "y.x.foo.com";
    String comment = "";
    int maxage = jakarta.ws.rs.core.NewCookie.DEFAULT_MAX_AGE;
    int version = 1;
    boolean secure = false;

    Cookie ck4 = new Cookie(name, value, path, domain, version);
    NewCookie nck4 = new NewCookie(ck4);

    verifyNewCookie(nck4, name, value, path, domain, version, comment, maxage,
        secure);
  }

  /*
   * @testName: constructorTest5
   * 
   * @assertion_ids: JAXRS:JAVADOC:107; JAXRS:JAVADOC:100; JAXRS:JAVADOC:101;
   * JAXRS:JAVADOC:103; JAXRS:JAVADOC:49; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54;
   * JAXRS:JAVADOC:55; JAXRS:JAVADOC:56;
   * 
   * @test_Strategy: Create a version 0 NewCookie instance using constructor
   * Cookie(String, String, String, String, int) NewCookie(Cookie)
   */
  @Test
  public void constructorTest5() throws Fault {
    String name = "name_1";
    String value = "value_1";
    String path = "/acme";
    String domain = "y.x.foo.com";
    int version = 0;
    String comment = "";
    int maxage = jakarta.ws.rs.core.NewCookie.DEFAULT_MAX_AGE;
    boolean secure = false;

    Cookie ck5 = new Cookie(name, value, path, domain, version);
    NewCookie nck5 = new NewCookie(ck5);

    verifyNewCookie(nck5, name, value, path, domain, version, comment, maxage,
        secure);
  }

  /*
   * @testName: constructorTest6
   * 
   * @assertion_ids: JAXRS:JAVADOC:104; JAXRS:JAVADOC:100; JAXRS:JAVADOC:101;
   * JAXRS:JAVADOC:103; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54; JAXRS:JAVADOC:55;
   * JAXRS:JAVADOC:56;
   * 
   * @test_Strategy: Create a version 0 NewCookie instance using constructor
   * NewCookie(String, String)
   */
  @Test
  public void constructorTest6() throws Fault {
    String name = "name_1";
    String value = "value_1";
    String comment = "";
    String domain = "";
    String path = "";
    int maxage = jakarta.ws.rs.core.NewCookie.DEFAULT_MAX_AGE;
    int version = 1;
    boolean secure = false;

    NewCookie nck6 = new NewCookie(name, value);

    verifyNewCookie(nck6, name, value, path, domain, version, comment, maxage,
        secure);
  }

  /*
   * @testName: constructorTest7
   * 
   * @assertion_ids: JAXRS:JAVADOC:105; JAXRS:JAVADOC:100; JAXRS:JAVADOC:101;
   * JAXRS:JAVADOC:103; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54; JAXRS:JAVADOC:55;
   * JAXRS:JAVADOC:56;
   * 
   * @test_Strategy: Create a NewCookie instance using constructor
   * NewCookie(java.lang.String name, java.lang.String value, java.lang.String
   * path, java.lang.String domain, java.lang.String comment, int maxAge,
   * boolean secure)
   */
  @Test
  public void constructorTest7() throws Fault {
    String name = "name_1";
    String value = "value_1";
    String path = "/acme";
    String domain = "";
    String comment = "";
    int maxage = jakarta.ws.rs.core.NewCookie.DEFAULT_MAX_AGE;
    int version = 1;
    boolean secure = false;

    NewCookie nck7 = new NewCookie(name, value, path, domain, comment, maxage,
        secure);

    verifyNewCookie(nck7, name, value, path, domain, version, comment, maxage,
        secure);
  }

  /*
   * @testName: constructorTest8
   * 
   * @assertion_ids: JAXRS:JAVADOC:105; JAXRS:JAVADOC:100; JAXRS:JAVADOC:101;
   * JAXRS:JAVADOC:103; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54; JAXRS:JAVADOC:55;
   * JAXRS:JAVADOC:56;
   * 
   * @test_Strategy: Create a NewCookie instance using constructor
   * NewCookie(java.lang.String name, java.lang.String value, java.lang.String
   * path, java.lang.String domain, java.lang.String comment, int maxAge,
   * boolean secure)
   */
  @Test
  public void constructorTest8() throws Fault {
    String name = "name_1";
    String value = "value_1";
    String path = "";
    String domain = "y.x.foo.com";
    String comment = "";
    int maxage = jakarta.ws.rs.core.NewCookie.DEFAULT_MAX_AGE;
    int version = 1;
    boolean secure = false;

    NewCookie nck8 = new NewCookie(name, value, path, domain, comment, maxage,
        secure);

    verifyNewCookie(nck8, name, value, path, domain, version, comment, maxage,
        secure);
  }

  /*
   * @testName: constructorTest9
   * 
   * @assertion_ids: JAXRS:JAVADOC:105; JAXRS:JAVADOC:100; JAXRS:JAVADOC:101;
   * JAXRS:JAVADOC:103; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54; JAXRS:JAVADOC:55;
   * JAXRS:JAVADOC:56;
   * 
   * @test_Strategy: Create a version 1 NewCookie instance using constructor
   * NewCookie(java.lang.String name, java.lang.String value, java.lang.String
   * path, java.lang.String domain, java.lang.String comment, int maxAge,
   * boolean secure)
   */
  @Test
  public void constructorTest9() throws Fault {
    String name = "name_1";
    String value = "value_1";
    String path = "/acme";
    String domain = "y.x.foo.com";
    String comment = "cts test comment";
    int maxage = jakarta.ws.rs.core.NewCookie.DEFAULT_MAX_AGE;
    int version = 1;
    boolean secure = false;

    NewCookie nck9 = new NewCookie(name, value, path, domain, comment, maxage,
        secure);

    verifyNewCookie(nck9, name, value, path, domain, version, comment, maxage,
        secure);
  }

  /*
   * @testName: constructorTest10
   * 
   * @assertion_ids: JAXRS:JAVADOC:105; JAXRS:JAVADOC:100; JAXRS:JAVADOC:101;
   * JAXRS:JAVADOC:103; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54; JAXRS:JAVADOC:55;
   * JAXRS:JAVADOC:56;
   * 
   * @test_Strategy: Create a version 1 NewCookie instance using constructor
   * NewCookie(java.lang.String name, java.lang.String value, java.lang.String
   * path, java.lang.String domain, java.lang.String comment, int maxAge,
   * boolean secure)
   */
  @Test
  public void constructorTest10() throws Fault {
    String name = "name_1";
    String value = "value_1";
    String path = "/acme";
    String domain = "y.x.foo.com";
    int version = 1;
    String comment = "cts test comment";
    int maxage = jakarta.ws.rs.core.NewCookie.DEFAULT_MAX_AGE;
    boolean secure = true;

    NewCookie nck10 = new NewCookie(name, value, path, domain, comment, maxage,
        secure);

    verifyNewCookie(nck10, name, value, path, domain, version, comment, maxage,
        secure);
  }

  /*
   * @testName: constructorTest11
   * 
   * @assertion_ids: JAXRS:JAVADOC:106; JAXRS:JAVADOC:100; JAXRS:JAVADOC:101;
   * JAXRS:JAVADOC:103; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54; JAXRS:JAVADOC:55;
   * JAXRS:JAVADOC:56;
   * 
   * @test_Strategy: Create a NewCookie instance using constructor
   * NewCookie(java.lang.String name, java.lang.String value, java.lang.String
   * path, java.lang.String domain, java.lang.String comment, int maxAge,
   * boolean secure)
   */
  @Test
  public void constructorTest11() throws Fault {
    String name = "name_1";
    String value = "value_1";
    String path = "/acme";
    String domain = "y.x.foo.com";
    int version = 1;
    String comment = "cts test comment";
    int maxage = 12345;
    boolean secure = false;

    NewCookie nck11 = new NewCookie(name, value, path, domain, version, comment,
        maxage, secure);

    verifyNewCookie(nck11, name, value, path, domain, version, comment, maxage,
        secure);
  }

  /*
   * @testName: constructorTest12
   * 
   * @assertion_ids: JAXRS:JAVADOC:106; JAXRS:JAVADOC:100; JAXRS:JAVADOC:101;
   * JAXRS:JAVADOC:103; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54; JAXRS:JAVADOC:55;
   * JAXRS:JAVADOC:56;
   * 
   * @test_Strategy: Create a NewCookie instance using constructor
   * NewCookie(java.lang.String name, java.lang.String value, java.lang.String
   * path, java.lang.String domain, int version, java.lang.String comment, int
   * maxAge, boolean secure)
   */
  @Test
  public void constructorTest12() throws Fault {
    String name = "name_1";
    String value = "value_1";
    String path = "/acme";
    String domain = "";
    String comment = "";
    int maxage = jakarta.ws.rs.core.NewCookie.DEFAULT_MAX_AGE;
    int version = 1;
    boolean secure = false;

    NewCookie nck12 = new NewCookie(name, value, path, domain, version, comment,
        maxage, secure);

    verifyNewCookie(nck12, name, value, path, domain, version, comment, maxage,
        secure);
  }

  /*
   * @testName: constructorTest13
   * 
   * @assertion_ids: JAXRS:JAVADOC:106; JAXRS:JAVADOC:100; JAXRS:JAVADOC:101;
   * JAXRS:JAVADOC:103; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54; JAXRS:JAVADOC:55;
   * JAXRS:JAVADOC:56;
   * 
   * @test_Strategy: Create a NewCookie instance using constructor
   * NewCookie(java.lang.String name, java.lang.String value, java.lang.String
   * path, java.lang.String domain, int version, java.lang.String comment, int
   * maxAge, boolean secure)
   */
  @Test
  public void constructorTest13() throws Fault {
    String name = "name_1";
    String value = "value_1";
    String path = "";
    String domain = "y.x.foo.com";
    String comment = "";
    int maxage = jakarta.ws.rs.core.NewCookie.DEFAULT_MAX_AGE;
    int version = 1;
    boolean secure = false;

    NewCookie nck13 = new NewCookie(name, value, path, domain, version, comment,
        maxage, secure);

    verifyNewCookie(nck13, name, value, path, domain, version, comment, maxage,
        secure);
  }

  /*
   * @testName: constructorTest14
   * 
   * @assertion_ids: JAXRS:JAVADOC:106; JAXRS:JAVADOC:100; JAXRS:JAVADOC:101;
   * JAXRS:JAVADOC:103; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54; JAXRS:JAVADOC:55;
   * JAXRS:JAVADOC:56;
   * 
   * @test_Strategy: Create a version 1 NewCookie instance using constructor
   * NewCookie(java.lang.String name, java.lang.String value, java.lang.String
   * path, java.lang.String domain, int version, java.lang.String comment, int
   * maxAge, boolean secure)
   */
  @Test
  public void constructorTest14() throws Fault {
    String name = "name_1";
    String value = "value_1";
    String path = "/acme";
    String domain = "y.x.foo.com";
    String comment = "cts test comment";
    int maxage = jakarta.ws.rs.core.NewCookie.DEFAULT_MAX_AGE;
    int version = 1;
    boolean secure = false;

    NewCookie nck14 = new NewCookie(name, value, path, domain, version, comment,
        maxage, secure);

    verifyNewCookie(nck14, name, value, path, domain, version, comment, maxage,
        secure);
  }

  /*
   * @testName: constructorTest15
   * 
   * @assertion_ids: JAXRS:JAVADOC:106; JAXRS:JAVADOC:100; JAXRS:JAVADOC:101;
   * JAXRS:JAVADOC:103; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54; JAXRS:JAVADOC:55;
   * JAXRS:JAVADOC:56;
   *
   * @test_Strategy: Create a version 1 NewCookie instance using constructor
   * NewCookie(java.lang.String name, java.lang.String value, java.lang.String
   * path, java.lang.String domain, int version, java.lang.String comment, int
   * maxAge, boolean secure)
   */
  @Test
  public void constructorTest15() throws Fault {
    String name = "name_1";
    String value = "value_1";
    String path = "/acme";
    String domain = "y.x.foo.com";
    int version = 0;
    String comment = "cts test comment";
    int maxage = jakarta.ws.rs.core.NewCookie.DEFAULT_MAX_AGE;
    boolean secure = true;

    NewCookie nck15 = new NewCookie(name, value, path, domain, version, comment,
        maxage, secure);

    verifyNewCookie(nck15, name, value, path, domain, version, comment, maxage,
        secure);
  }

  /*
   * @testName: constructorTest16
   * 
   * @assertion_ids: JAXRS:JAVADOC:106; JAXRS:JAVADOC:100; JAXRS:JAVADOC:101;
   * JAXRS:JAVADOC:103; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54; JAXRS:JAVADOC:55;
   * JAXRS:JAVADOC:56;
   * 
   * @test_Strategy: Create a version 1 NewCookie instance using constructor
   * NewCookie(java.lang.String name, java.lang.String value, java.lang.String
   * path, java.lang.String domain, int version, java.lang.String comment, int
   * maxAge, boolean secure)
   */
  @Test
  public void constructorTest16() throws Fault {
    // ToDo: Create a list of name, value, path, domain
    String name = "name_1";
    String value = "value_1";
    String path = "/acme";
    String domain = "y.x.foo.com";
    int version = 0;
    String comment = "cts test comment";
    int maxage = jakarta.ws.rs.core.NewCookie.DEFAULT_MAX_AGE;
    boolean secure = true;

    NewCookie nck16 = new NewCookie(name, value, path, domain, version, comment,
        maxage, secure);

    verifyNewCookie(nck16, name, value, path, domain, version, comment, maxage,
        secure);
  }

  /*
   * @testName: constructorTest17
   * 
   * @assertion_ids: JAXRS:JAVADOC:106; JAXRS:JAVADOC:100; JAXRS:JAVADOC:101;
   * JAXRS:JAVADOC:103; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54; JAXRS:JAVADOC:55;
   * JAXRS:JAVADOC:56;
   * 
   * @test_Strategy: Create a version 1 NewCookie instance using constructor
   * NewCookie(java.lang.String name, java.lang.String value, java.lang.String
   * path, java.lang.String domain, int version, java.lang.String comment, int
   * maxAge, boolean secure)
   */
  @Test
  public void constructorTest17() throws Fault {
    // ToDo: Create a list of name, value, path, domain
    String name = "name_1";
    String value = "value_1";
    String path = "/acme";
    String domain = "y.x.foo.com";
    int version = 1;
    String comment = "cts test comment";
    int maxage = 123456;
    boolean secure = true;

    NewCookie nck17 = new NewCookie(name, value, path, domain, version, comment,
        maxage, secure);

    verifyNewCookie(nck17, name, value, path, domain, version, comment, maxage,
        secure);
  }

  /*
   * @testName: constructorTest18
   * 
   * @assertion_ids: JAXRS:JAVADOC:108; JAXRS:JAVADOC:100; JAXRS:JAVADOC:101;
   * JAXRS:JAVADOC:103; JAXRS:JAVADOC:51; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54;
   * JAXRS:JAVADOC:55; JAXRS:JAVADOC:56;
   * 
   * @test_Strategy: Create a NewCookie instance using constructor
   * Cookie(String, String) NewCookie(Cookie, String comment, int maxAge,
   * boolean secure)
   */
  @Test
  public void constructorTest18() throws Fault {
    String name = "name_1";
    String value = "value_1";
    String comment = "";
    String domain = "";
    String path = "";
    int maxage = jakarta.ws.rs.core.NewCookie.DEFAULT_MAX_AGE;
    int version = 1;
    boolean secure = false;

    Cookie ck18 = new Cookie(name, value);
    NewCookie nck18 = new NewCookie(ck18, comment, maxage, secure);

    verifyNewCookie(nck18, name, value, path, domain, version, comment, maxage,
        secure);
  }

  /*
   * @testName: constructorTest19
   *
   * @assertion_ids: JAXRS:JAVADOC:108; JAXRS:JAVADOC:100; JAXRS:JAVADOC:101;
   * JAXRS:JAVADOC:103; JAXRS:JAVADOC:50; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54;
   * JAXRS:JAVADOC:55; JAXRS:JAVADOC:56;
   *
   * @test_Strategy: Create a NewCookie instance using constructor
   * Cookie(String, String, String, String) NewCookie(Cookie, String comment,
   * int maxAge, boolean secure)
   */
  @Test
  public void constructorTest19() throws Fault {

    String name = "name_1";
    String value = "value_1";
    String path = "/acme";
    String domain = "";
    String comment = "";
    int maxage = jakarta.ws.rs.core.NewCookie.DEFAULT_MAX_AGE;
    int version = 1;
    boolean secure = false;

    Cookie ck2 = new Cookie(name, value, path, domain);
    NewCookie nck2 = new NewCookie(ck2, comment, maxage, secure);

    verifyNewCookie(nck2, name, value, path, domain, version, comment, maxage,
        secure);
  }

  /*
   * @testName: constructorTest20
   *
   * @assertion_ids: JAXRS:JAVADOC:108; JAXRS:JAVADOC:100; JAXRS:JAVADOC:101;
   * JAXRS:JAVADOC:103; JAXRS:JAVADOC:50; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54;
   * JAXRS:JAVADOC:55; JAXRS:JAVADOC:56;
   *
   * @test_Strategy: Create a NewCookie instance using constructor
   * Cookie(String, String, String, String) NewCookie(Cookie, String comment,
   * int maxAge, boolean secure)
   */
  @Test
  public void constructorTest20() throws Fault {
    String name = "name_1";
    String value = "value_1";
    String path = "";
    String domain = "y.x.foo.com";
    String comment = "";
    int maxage = jakarta.ws.rs.core.NewCookie.DEFAULT_MAX_AGE;
    int version = 1;
    boolean secure = false;

    Cookie ck20 = new Cookie(name, value, path, domain);
    NewCookie nck20 = new NewCookie(ck20, comment, maxage, secure);

    verifyNewCookie(nck20, name, value, path, domain, version, comment, maxage,
        secure);
  }

  /*
   * @testName: constructorTest21
   *
   * @assertion_ids: JAXRS:JAVADOC:108; JAXRS:JAVADOC:100; JAXRS:JAVADOC:101;
   * JAXRS:JAVADOC:103; JAXRS:JAVADOC:49; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54;
   * JAXRS:JAVADOC:55; JAXRS:JAVADOC:56;
   *
   * @test_Strategy: Create a version 1 NewCookie instance using constructor
   * Cookie(String, String, String, String, int) NewCookie(Cookie, String
   * comment, int maxAge, boolean secure)
   */
  @Test
  public void constructorTest21() throws Fault {
    String name = "name_1";
    String value = "value_1";
    String path = "/acme";
    String domain = "y.x.foo.com";
    String comment = "";
    int maxage = jakarta.ws.rs.core.NewCookie.DEFAULT_MAX_AGE;
    int version = 1;
    boolean secure = false;

    Cookie ck21 = new Cookie(name, value, path, domain, version);
    NewCookie nck21 = new NewCookie(ck21, comment, maxage, secure);

    verifyNewCookie(nck21, name, value, path, domain, version, comment, maxage,
        secure);
  }

  /*
   * @testName: constructorTest22
   * 
   * @assertion_ids: JAXRS:JAVADOC:108; JAXRS:JAVADOC:100; JAXRS:JAVADOC:101;
   * JAXRS:JAVADOC:103; JAXRS:JAVADOC:49; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54;
   * JAXRS:JAVADOC:55; JAXRS:JAVADOC:56;
   * 
   * @test_Strategy: Create a version 1 NewCookie instance using constructor
   * Cookie(String, String, String, String, int) NewCookie(Cookie, String
   * comment, int maxAge, boolean secure)
   */
  @Test
  public void constructorTest22() throws Fault {
    String name = "name_1";
    String value = "value_1";
    String path = "/acme";
    String domain = "y.x.foo.com";
    int version = 1;
    String comment = "cts test comment";
    int maxage = jakarta.ws.rs.core.NewCookie.DEFAULT_MAX_AGE;
    boolean secure = false;

    Cookie ck22 = new Cookie(name, value, path, domain, version);
    NewCookie nck22 = new NewCookie(ck22, comment, maxage, secure);

    verifyNewCookie(nck22, name, value, path, domain, version, comment, maxage,
        secure);
  }

  /*
   * @testName: constructorTest23
   * 
   * @assertion_ids: JAXRS:JAVADOC:108; JAXRS:JAVADOC:100; JAXRS:JAVADOC:101;
   * JAXRS:JAVADOC:103; JAXRS:JAVADOC:49; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54;
   * JAXRS:JAVADOC:55; JAXRS:JAVADOC:56;
   * 
   * @test_Strategy: Create a version 1 NewCookie instance using constructor
   * Cookie(String, String, String, String, int) NewCookie(Cookie, String
   * comment, int maxAge, boolean secure)
   */
  @Test
  public void constructorTest23() throws Fault {
    String name = "name_1";
    String value = "value_1";
    String path = "/acme";
    String domain = "y.x.foo.com";
    int version = 0;
    String comment = "cts test comment";
    int maxage = jakarta.ws.rs.core.NewCookie.DEFAULT_MAX_AGE;
    boolean secure = false;

    Cookie ck22 = new Cookie(name, value, path, domain, version);
    NewCookie nck22 = new NewCookie(ck22, comment, maxage, secure);

    verifyNewCookie(nck22, name, value, path, domain, version, comment, maxage,
        secure);
  }

  /*
   * @testName: constructorTest24
   * 
   * @assertion_ids: JAXRS:JAVADOC:108; JAXRS:JAVADOC:100; JAXRS:JAVADOC:101;
   * JAXRS:JAVADOC:103; JAXRS:JAVADOC:49; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54;
   * JAXRS:JAVADOC:55; JAXRS:JAVADOC:56;
   * 
   * @test_Strategy: Create a version 1 NewCookie instance using constructor
   * Cookie(String, String, String, String, int) NewCookie(Cookie, String
   * comment, int maxAge, boolean secure)
   */
  @Test
  public void constructorTest24() throws Fault {
    String name = "name_1";
    String value = "value_1";
    String path = "/acme";
    String domain = "y.x.foo.com";
    int version = 1;
    String comment = "cts test comment";
    int maxage = jakarta.ws.rs.core.NewCookie.DEFAULT_MAX_AGE;
    boolean secure = true;

    Cookie ck24 = new Cookie(name, value, path, domain, version);
    NewCookie nck24 = new NewCookie(ck24, comment, maxage, secure);

    verifyNewCookie(nck24, name, value, path, domain, version, comment, maxage,
        secure);
  }

  /*
   * @testName: constructorTest25
   * 
   * @assertion_ids: JAXRS:JAVADOC:108; JAXRS:JAVADOC:100; JAXRS:JAVADOC:101;
   * JAXRS:JAVADOC:103; JAXRS:JAVADOC:49; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54;
   * JAXRS:JAVADOC:55; JAXRS:JAVADOC:56;
   * 
   * @test_Strategy: Create a version 1 NewCookie instance using constructor
   * Cookie(String, String, String, String, int) NewCookie(Cookie, String
   * comment, int maxAge, boolean secure)
   */
  @Test
  public void constructorTest25() throws Fault {
    String name = "name_1";
    String value = "value_1";
    String path = "/acme";
    String domain = "y.x.foo.com";
    int version = 1;
    String comment = "cts test comment";
    int maxage = 12345;
    boolean secure = false;

    Cookie ck25 = new Cookie(name, value, path, domain, version);
    NewCookie nck25 = new NewCookie(ck25, comment, maxage, secure);

    verifyNewCookie(nck25, name, value, path, domain, version, comment, maxage,
        secure);
  }

  /*
   * @testName: parseTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:111; JAXRS:JAVADOC:100; JAXRS:JAVADOC:101;
   * JAXRS:JAVADOC:103; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54; JAXRS:JAVADOC:55;
   * JAXRS:JAVADOC:56;
   * 
   * @test_Strategy: Create a version 0 Cookie instance by Parsing a String
   */
  @Test
  public void parseTest1() throws Fault {
    String NewCookie_toParse = "NAME_1=Value_1;";
    String name = "name_1";
    String value = "value_1";
    String path = "";
    String domain = "";
    int version = 1;

    NewCookie nck26 = jakarta.ws.rs.core.NewCookie.valueOf(NewCookie_toParse);

    verifyNewCookie(nck26, name, value, path, domain, version, "", -1, false);
  }

  /*
   * @testName: parseTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:111; JAXRS:JAVADOC:100; JAXRS:JAVADOC:101;
   * JAXRS:JAVADOC:103; JAXRS:JAVADOC:53; JAXRS:JAVADOC:54; JAXRS:JAVADOC:55;
   * JAXRS:JAVADOC:56;
   * 
   * @test_Strategy: Create a version 0 NewCookie instance by Parsing a String
   */
  @Test
  public void parseTest2() throws Fault {
    String newCookie_toParse = "Customer=WILE_E_COYOTE; Path=/acme; Version=1";

    String name = "customer";
    String value = "wile_e_coyote";
    String path = "/acme";
    String domain = "";
    int version = 1;

    NewCookie nck27 = jakarta.ws.rs.core.NewCookie.valueOf(newCookie_toParse);

    verifyNewCookie(nck27, name, value, path, domain, version, "", -1, false);
  }

  /*
   * @testName: parseTest3
   * 
   * @assertion_ids: JAXRS:JAVADOC:111;
   * 
   * @test_Strategy: Create a NewCookie instance by Parsing a null String.
   * Verify that IllegalArgumentException is thrown
   */
  @Test
  public void parseTest3() throws Fault {
    try {
      jakarta.ws.rs.core.NewCookie.valueOf(null);
      throw new Fault(
          "Expected IllegalArgumentException not thrown. Test Failed.");
    } catch (IllegalArgumentException ilex) {
      logMsg("IllegalArgumentException has been thrown as expected");
    }
  }

  /*
   * @testName: equalsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:99; JAXRS:JAVADOC:102;
   * 
   * @test_Strategy: Create two NewCookie instances using constructor
   * Cookie(String, String, String, String, int) NewCookie(Cookie, String
   * comment, int maxAge, boolean secure) and NewCokie(String name, String
   * value, String path, String domain, int version, String comment, int maxAge,
   * boolean secure) Verify that equals and hashCode methods work.
   */
  @Test
  public void equalsTest() throws Fault {
    boolean pass = true;
    StringBuffer sb = new StringBuffer();

    String name = "name_1";
    String value = "value_1";
    String path = "/acme";
    String domain = "y.x.foo.com";
    int version = 1;
    String comment = "cts test comment";
    int maxage = 12345;
    boolean secure = false;

    Cookie ck28 = new Cookie(name, value, path, domain, version);
    NewCookie nck28 = new NewCookie(ck28, comment, maxage, secure);
    NewCookie nck29 = new NewCookie(name, value, path, domain, version, comment,
        maxage, secure);

    if (!nck28.equals(nck29)) {
      pass = false;
      sb.append("Equal test failed.").append(newline);
      sb.append("First  :").append(nck28.toString()).append(newline);
      sb.append("Second :").append(nck29.toString()).append(newline);
    }

    if (nck28.hashCode() != nck29.hashCode()) {
      pass = false;
      sb.append("HashCode equal test failed.").append(newline);
    }

    name = "name1";
    nck29 = new NewCookie(name, value, path, domain, version, comment, maxage,
        secure);
    if (nck28.equals(nck29)) {
      pass = false;
      sb.append("UnEqual test failed at name.").append(newline);
      sb.append("First  :").append(nck28.toString()).append(newline);
      sb.append("Second :").append(nck29.toString()).append(newline);
    }

    if (nck28.hashCode() == nck29.hashCode()) {
      pass = false;
      sb.append("HashCode unequal test failed at name.").append(newline);
    }

    assertTrue(pass, "At least one assertion failed: " + sb.toString());
  }

  /*
   * @testName: toCookieTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:109; JAXRS:JAVADOC:52; JAXRS:JAVADOC:58;
   * 
   * @test_Strategy: Create a Cookie instance using constructor Cookie(String,
   * String, String, String, int) and a NewCookie instance NewCokie(String name,
   * String value, String path, String domain, int version, String comment, int
   * maxAge, boolean secure) NewCookie(Cookie, String comment, int maxAge,
   * boolean secure) Verify that toCookie method works by using .equals and
   * hashCode method.
   */
  @Test
  public void toCookieTest() throws Fault {
    boolean pass = true;
    StringBuffer sb = new StringBuffer();

    String name = "name_1";
    String value = "value_1";
    String path = "/acme";
    String domain = "y.x.foo.com";
    int version = 1;
    String comment = "cts test comment";
    int maxage = 12345;
    boolean secure = false;

    Cookie ck30 = new Cookie(name, value, path, domain, version);
    NewCookie nck30 = new NewCookie(name, value, path, domain, version, comment,
        maxage, secure);
    Cookie ck31 = nck30.toCookie();

    if (!ck30.equals(ck31)) {
      pass = false;
      sb.append("Equal test failed.").append(newline);
      sb.append("First  :").append(ck30.toString()).append(newline);
      sb.append("Second :").append(ck31.toString()).append(newline);
    }

    if (ck30.hashCode() != ck31.hashCode()) {
      pass = false;
      sb.append("HashCode equal test failed.").append(newline);
    }

    assertTrue(pass, "At least one assertion failed: " + sb.toString());
  }

  /*
   * @testName: toStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:110; JAXRS:JAVADOC:52; JAXRS:JAVADOC:58;
   * 
   * @test_Strategy: Create a NewCookie instance NewCokie(String name, String
   * value, String path, String domain, int version, String comment, int maxAge,
   * boolean secure) Verify that toString method works.
   */
  @Test
  public void toStringTest() throws Fault {
    boolean pass = true;
    StringBuffer sb = new StringBuffer();

    String name = "name_1";
    String value = "value_1";
    String path = "/acme";
    String domain = "y.x.foo.com";
    int version = 1;
    String comment = "cts test comment";
    int maxage = 12345;
    boolean secure = false;

    List<String> tobeVerified = Arrays.asList(name, value, path, domain,
        Integer.valueOf(version).toString(), comment,
        Integer.valueOf(maxage).toString(),
        secure ? Boolean.valueOf(secure).toString() : null);

    NewCookie nck31 = new NewCookie(name, value, path, domain, version, comment,
        maxage, secure);

    String nk_String = nck31.toString();

    for (String nk_part : tobeVerified) {
      if (nk_part != null) {
        if (!nk_String.contains(nk_part)) {
          sb.append("Test failed.  Expected ").append(nk_part + " not faound.")
              .append(newline);
          pass = false;
        } else {
          sb.append("Expected ").append(nk_part).append(" faound.")
              .append(newline);
        }
      }
    }

    sb.append("Expected NewCookie: ").append(nk_String).append(newline);

    assertTrue(pass, "At least one assertion failed: " + sb.toString());
    TestUtil.logTrace(sb.toString());
  }

  private static boolean verifyNewCookie(NewCookie nck, String name,
      String value, String path, String domain, int version, String comment,
      int maxage, boolean secure) throws Fault {

    StringBuffer sb = new StringBuffer();
    boolean pass = true;

    if (name == "" || name == null) {
      pass = false;
      sb.append("NewCookie's name is empty");
    } else if (!nck.getName().toLowerCase().equals(name)) {
      pass = false;
      sb.append("Failed name test.  Expect ").append(name)
          .append(" got " + nck.getName());
    }

    if (value == "" || value == null) {
      pass = false;
      sb.append("NewCookie's value is empty");
    } else if (!nck.getValue().toLowerCase().equals(value)) {
      pass = false;
      sb.append("Failed value test.  Expect ").append(value)
          .append(" got " + nck.getValue());
    }

    if (nck.getVersion() != version) {
      pass = false;
      sb.append("Failed version test.  Expect ").append(version)
          .append(" got " + nck.getVersion());
    }

    if (comment == "" || comment == null) {
      if (nck.getComment() != "" && nck.getComment() != null) {
        pass = false;
        sb.append("Failed comment test.  Expect null String, got <"
            + nck.getComment()).append(">");
      }
    } else if (!nck.getComment().toLowerCase().equals(comment)) {
      pass = false;
      sb.append("Failed comment test.  Expect ").append(comment)
          .append(" got " + nck.getComment());
    }

    if (path == "" || path == null) {
      if (nck.getPath() != "" && nck.getPath() != null) {
        pass = false;
        sb.append(
            "Failed path test.  Expect null String, got " + nck.getPath());
      }
    } else if (nck.getPath() == null || nck.getPath() == "") {
      pass = false;
      sb.append("Failed path test.  Got null, expecting ").append(path);
    } else if (!nck.getPath().toLowerCase().equals(path)) {
      pass = false;
      sb.append("Failed path test.  Expect ").append(path)
          .append(" got " + nck.getPath());
    }

    if (domain == "" || domain == null) {
      if (nck.getDomain() != "" && nck.getDomain() != null) {
        pass = false;
        sb.append("Failed path test.  Expect ").append(domain)
            .append(" got " + nck.getDomain());
      }
    } else if (!nck.getDomain().toLowerCase().equals(domain)) {
      pass = false;
      sb.append("Failed domain test.  Expect ").append(domain)
          .append(" got " + nck.getDomain());
    }

    if (nck.getMaxAge() != maxage) {
      pass = false;
      sb.append("Failed maxage test.  Expect ").append(maxage)
          .append(" got " + nck.getMaxAge());
    }

    if (nck.isSecure() != secure) {
      pass = false;
      sb.append("Failed secure test.  Expect ").append(secure)
          .append(" got " + nck.isSecure());
    }

    assertTrue(pass, "At least one assertion falied: " + sb.toString());
    return pass;
  }
}
