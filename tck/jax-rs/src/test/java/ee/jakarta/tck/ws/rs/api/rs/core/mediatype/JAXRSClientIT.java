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

package ee.jakarta.tck.ws.rs.api.rs.core.mediatype;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.core.MediaType;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/*
 * @class.setup_props: webServerHost; webServerPort; ts_home;
 */
public class JAXRSClientIT extends JAXRSCommonClient {

  private static final long serialVersionUID = 3136740904552966415L;

  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : " + testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : " + testInfo.getDisplayName());
  }
  
  /* Run test */

  /*
   * @testName: constructorTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:92; JAXRS:JAVADOC:84; JAXRS:JAVADOC:85;
   * 
   * @test_Strategy: Create an MediaType instance using MediaType()
   */
  @Test
  public void constructorTest1() throws Fault {
    String type = MediaType.MEDIA_TYPE_WILDCARD;
    String subtype = MediaType.MEDIA_TYPE_WILDCARD;

    MediaType mt1 = new MediaType();
    verifyMediaType(mt1, type, subtype);
  }

  /*
   * @testName: constructorTest2
   * 
   * @assertion_ids: JAXRS:SPEC:33.8; JAXRS:SPEC:33.7; JAXRS:JAVADOC:91;
   * JAXRS:JAVADOC:84; JAXRS:JAVADOC:85;
   * 
   * @test_Strategy: Create an MediaType instance using MediaType(String, String)
   */
  @Test
  public void constructorTest2() throws Fault {
    String type = "application";
    String subtype = "atom+xml";

    MediaType mt2 = new MediaType(type, subtype);

    verifyMediaType(mt2, type, subtype);
  }

  /*
   * @testName: constructorTest3
   * 
   * @assertion_ids: JAXRS:JAVADOC:90; JAXRS:JAVADOC:83; JAXRS:JAVADOC:84;
   * JAXRS:JAVADOC:85;
   * 
   * @test_Strategy: Create an MediaType instance using MediaType(String, String,
   * Map)
   */
  @Test
  public void constructorTest3() throws Fault {
    String type = "application";
    String subtype = "x-www-form-urlencoded";
    Map<String, String> params = new HashMap<String, String>();

    MediaType mt3 = new MediaType(type, subtype, params);

    verifyMediaType(mt3, type, subtype, params, new String[0]);
  }

  /*
   * @testName: constructorStringStringStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:825; JAXRS:JAVADOC:84; JAXRS:JAVADOC:85;
   * JAXRS:JAVADOC:93;
   * 
   * @test_Strategy: Creates a new instance of MediaType with the supplied type,
   * subtype and ""charset"" parameter.
   * 
   * getSubtype, getType, toString
   */
  @Test
  public void constructorStringStringStringTest() throws Fault {
    String type = MediaType.APPLICATION_FORM_URLENCODED_TYPE.getType();
    String subtype = MediaType.APPLICATION_FORM_URLENCODED_TYPE.getSubtype();
    String[] charsets = { "UTF-8", "ISO-8859-2", "UTF-16", null };
    for (String charset : charsets) {
      MediaType mt = new MediaType(type, subtype, charset);
      verifyMediaType(mt, type, subtype);
      if (charset != null)
        assertCharset(mt, charset);
    }
  }

  /*
   * @testName: isCompatibleTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:91; JAXRS:JAVADOC:87;
   * 
   * @test_Strategy: Create an MediaType instance using MediaType(String, String),
   * verify method isCompatible works.
   */
  @Test
  public void isCompatibleTest1() throws Fault {
    String type = "text";
    String subtype = "plain";

    MediaType mt3 = new MediaType(type, subtype);

    if (!mt3.isCompatible(jakarta.ws.rs.core.MediaType.TEXT_PLAIN_TYPE)) {
      throw new Fault("isCompatible test failed.");
    }
  }

  /*
   * @testName: isCompatibleTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:90; JAXRS:JAVADOC:91; JAXRS:JAVADOC:87;
   * 
   * @test_Strategy: Create two MediaType instances using MediaType(String,
   * String), and MediaType(String, String, Map<String, String>) verify method
   * isCompatible works.
   */
  @Test
  public void isCompatibleTest2() throws Fault {
    String type = "text";
    String subtype = "plain";
    Map<String, String> params = new HashMap<String, String>();

    params.put("charset", "iso-8859-1");

    MediaType mt5 = new MediaType(type, subtype);
    MediaType mt6 = new MediaType(type, subtype, params);

    if (!mt5.isCompatible(mt6)) {
      throw new Fault("isCompatible test failed: " + "Expecting " + mt5.toString() + ", got " + mt6.toString());
    }
  }

  /*
   * @testName: isCompatibleTest3
   * 
   * @assertion_ids: JAXRS:JAVADOC:91; JAXRS:JAVADOC:87;
   * 
   * @test_Strategy: Create two MediaType instances using MediaType(String,
   * String), and MediaType(String, String, Map<String, String>) verify method
   * isCompatible works.
   */
  @Test
  public void isCompatibleTest3() throws Fault {
    String type = "text";
    String subtype = "plain";
    String subtype1 = "html";

    MediaType mt5 = new MediaType(type, subtype);
    MediaType mt6 = new MediaType(type, subtype1);

    if (mt5.isCompatible(mt6)) {
      throw new Fault("isCompatible test failed: " + "Expecting " + mt5.toString() + ", got " + mt6.toString());
    }
  }

  /*
   * @testName: hashCodeTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:91; JAXRS:JAVADOC:86;
   * 
   * @test_Strategy: Create an MediaType instance using MediaType(String, String),
   * verify method hashCode works.
   */
  @Test
  public void hashCodeTest1() throws Fault {
    String type = "text";
    String subtype = "plain";

    MediaType mt3 = new MediaType(type, subtype);

    if (mt3.hashCode() != jakarta.ws.rs.core.MediaType.TEXT_PLAIN_TYPE.hashCode()) {
      throw new Fault("hashCode test failed." + " Expecting " + mt3.hashCode() + ", got "
          + jakarta.ws.rs.core.MediaType.TEXT_PLAIN_TYPE.hashCode());
    }
  }

  /*
   * @testName: hashCodeTest2
   * 
   * @assertion_ids: JAXRS:JAVADOC:90; JAXRS:JAVADOC:91; JAXRS:JAVADOC:86;
   * 
   * @test_Strategy: Create two MediaType instances using MediaType(String,
   * String), and MediaType(String, String, Map<String, String>) verify method
   * hashCode works.
   */
  @Test
  public void hashCodeTest2() throws Fault {
    String type = "text";
    String subtype = "plain";
    Map<String, String> params = new HashMap<String, String>();

    params.put("charset", "iso-8859-1");

    MediaType mt5 = new MediaType(type, subtype);
    MediaType mt6 = new MediaType(type, subtype, params);

    if (mt5.hashCode() == mt6.hashCode()) {
      throw new Fault(
          "hashCode test failed." + " Expecting different hashCode than " + mt5.hashCode() + ", got the same");
    }
  }

  /*
   * @testName: hashCodeTest3
   * 
   * @assertion_ids: JAXRS:JAVADOC:91; JAXRS:JAVADOC:86;
   * 
   * @test_Strategy: Create two MediaType instances using MediaType(String,
   * String), and MediaType(String, String, Map<String, String>) verify method
   * hashCode works.
   */
  @Test
  public void hashCodeTest3() throws Fault {
    String type = "text";
    String subtype = "plain";
    String subtype1 = "html";

    MediaType mt5 = new MediaType(type, subtype);
    MediaType mt6 = new MediaType(type, subtype1);

    int hc = mt5.hashCode();
    int hc1 = mt6.hashCode();
    if (hc == hc1) {
      throw new Fault("hashCode test failed: " + "Expecting " + mt5.toString() + ", got " + mt6.toString());
    }
  }

  /*
   * @testName: equalsTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:91; JAXRS:JAVADOC:82;
   * 
   * @test_Strategy: Create an MediaType instance using MediaType(String, String)
   * Verify method equals works
   */
  @Test
  public void equalsTest1() throws Fault {
    String type = "text";
    String subtype = "html";

    MediaType mt4 = new MediaType(type, subtype);

    if (!mt4.equals(jakarta.ws.rs.core.MediaType.TEXT_HTML_TYPE)) {
      throw new Fault(
          "Equals test failed" + "Expecting " + MediaType.TEXT_PLAIN_TYPE.toString() + ", got " + mt4.toString());
    }
  }

  /*
   * @testName: equalsTest2
   * 
   * @assertion_ids: JAXRS:SPEC:33.8; JAXRS:SPEC:33.7; JAXRS:JAVADOC:91;
   * JAXRS:JAVADOC:82;
   * 
   * @test_Strategy: Create two MediaType instances using MediaType(String,
   * String) Verify method equals works
   */
  @Test
  public void equalsTest2() throws Fault {
    String type = "application";
    String subtype = "xml";
    String type1 = "Application";
    String subtype1 = "XML";

    MediaType mt7 = new MediaType(type, subtype);
    MediaType mt8 = new MediaType(type1, subtype1);

    if (!mt7.equals(mt8)) {
      throw new Fault("Equals test failed" + "Expecting " + mt7.toString() + ", got " + mt8.toString());
    }
  }

  /*
   * @testName: equalsTest3
   * 
   * @assertion_ids: JAXRS:SPEC:33.8; JAXRS:SPEC:33.7; JAXRS:JAVADOC:90;
   * JAXRS:JAVADOC:91; JAXRS:JAVADOC:82;
   * 
   * @test_Strategy: Create two MediaType instances using MediaType(String,
   * String) MediaType(String, String, Map<String, String>) Verify method equals
   * works
   */
  @Test
  public void equalsTest3() throws Fault {
    String type = "application";
    String subtype = "svg+xml";
    Map<String, String> params = new HashMap<String, String>();

    MediaType mt5 = new MediaType(type, subtype);
    MediaType mt6 = new MediaType(type, subtype, params);

    if (!mt5.equals(mt6)) {
      throw new Fault("Equals test failed" + "Expecting " + mt5.toString() + ", got " + mt6.toString());
    }
  }

  /*
   * @testName: equalsTest4
   * 
   * @assertion_ids: JAXRS:JAVADOC:90; JAXRS:JAVADOC:82;
   * 
   * @test_Strategy: Create two MediaType instances using MediaType(String,
   * String, Map<String, String>) MediaType(String, String, Map<String, String>)
   * Verify method equals works
   */
  @Test
  public void equalsTest4() throws Fault {
    String type = "text";
    String subtype = "plain";
    Map<String, String> params = new HashMap<String, String>();
    Map<String, String> params1 = new HashMap<String, String>();
    params1.put("charset", "iso-8859-1");

    MediaType mt5 = new MediaType(type, subtype, params);
    MediaType mt6 = new MediaType(type, subtype, params1);

    if (mt5.equals(mt6)) {
      throw new Fault("Equals test failed" + "Expecting " + mt5.toString() + ", got " + mt6.toString());
    }
  }

  /*
   * @testName: toStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:90; JAXRS:JAVADOC:93;
   * 
   * @test_Strategy: Create a MediaType instances using MediaType(String, String,
   * Map<String, String>) Verify method toString works
   */
  @Test
  public void toStringTest() throws Fault {
    boolean pass = true;
    StringBuffer sb = new StringBuffer();

    String type = "text";
    String subtype = "plain";
    Map<String, String> params = new HashMap<String, String>();
    params.put("charset", "iso-8859-1");

    MediaType mt6 = new MediaType(type, subtype, params);

    String to_verify = mt6.toString();

    if (!to_verify.toLowerCase().contains(type)) {
      pass = false;
      sb.append("Type is missing");
    }

    if (!to_verify.toLowerCase().contains(subtype)) {
      pass = false;
      sb.append("Subtype is missing");
    }

    if (!to_verify.toLowerCase().contains("char")) {
      pass = false;
      sb.append("Parameter's name is missing");
    }

    if (!to_verify.toLowerCase().contains("iso-8859-1")) {
      pass = false;
      sb.append("Parameter's value is missing");
    }

    assertTrue(pass, "At least one assertion failed: " + sb.toString());
  }

  /*
   * @testName: wildcardTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:92; JAXRS:JAVADOC:88; JAXRS:JAVADOC:89;
   * 
   * @test_Strategy: Create an MediaType instance using MediaType() Verify that
   * method isWildCardType() works Verify that method isWildCardSubtype() works
   */
  @Test
  public void wildcardTest() throws Fault {
    boolean pass = true;
    StringBuffer sb = new StringBuffer();

    MediaType mt2 = new MediaType();

    if (!mt2.isWildcardType()) {
      pass = false;
      sb.append("Failed WildcardType test");
    }

    if (!mt2.isWildcardSubtype()) {
      pass = false;
      sb.append("Failed WildcardSubtype test");
    }

    assertTrue(pass, "At least one assertion failed: " + sb.toString());
  }

  /*
   * @testName: valueOfTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:94;
   * 
   * @test_Strategy: Create an MediaType instance using method
   * MediaType.valueOf(String) Verify that all type, subtype and parameters are
   * preserved.
   */
  @Test
  public void valueOfTest() throws Fault {
    String type = "text";
    String subtype = "plain";
    String toParse = "text/plain; charset=us-ascii";

    MediaType mt10 = MediaType.valueOf(toParse);

    Map<String, String> params = new HashMap<String, String>();
    params.put("charset", "us-ascii");
    String[] pname = { "charset" };

    verifyMediaType(mt10, type, subtype, params, pname);
  }

  /*
   * @testName: valueOfTest1
   * 
   * @assertion_ids: JAXRS:JAVADOC:94;
   * 
   * @test_Strategy: Create an MediaType instance using method
   * MediaType.valueOf(null) Verify that IllegalArgumentException is thrown.
   */
  @Test
  public void valueOfTest1() throws Fault {
    try {
      MediaType.valueOf(null);
      throw new Fault("Expected IllegalArgumentException now thrown.  Test Failed");
    } catch (IllegalArgumentException ilex) {
      logMsg("IllegalArgumentException has been thrown as expected");
    }
  }

  /*
   * @testName: withCharsetTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:826; JAXRS:JAVADOC:84; JAXRS:JAVADOC:85;
   * JAXRS:JAVADOC:93;
   * 
   * @test_Strategy: Create a new MediaType instance with the same type, subtype
   * and parameters copied from the original instance and the supplied ""charset""
   * parameter.
   * 
   * getSubtype, getType, toString
   */
  @Test
  public void withCharsetTest() throws Fault {
    String type = MediaType.APPLICATION_FORM_URLENCODED_TYPE.getType();
    String subtype = MediaType.APPLICATION_FORM_URLENCODED_TYPE.getSubtype();
    MediaType original = new MediaType(type, subtype);
    String[] charsets = { "UTF-8", "ISO-8859-2", "UTF-16", null };
    // test the original does not have the charset
    for (int i = 0; i != charsets.length - 1; i++)
      assertTrue(!original.toString().toLowerCase().contains(charsets[i].toLowerCase()));
    // create new MediaType
    for (String charset : charsets) {
      MediaType created = original.withCharset(charset);
      verifyMediaType(created, type, subtype);
      if (charset != null) {
        // check the original is not changed
        assertTrue(!original.toString().toLowerCase().contains(charset.toLowerCase()));
        // check the charset in new mediaType
        assertContainsIgnoreCase(created.toString(), charset, "MediaType", created,
            "does not contain expected character set", charset);
        logMsg("MediaType", created, "contains expected character set", charset);
      }
    }
  }

  /*
   * @testName: withCharsetNullOrEmptyCharsetTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:826;
   * 
   * @test_Strategy: Create a new MediaType instance with the same type, subtype
   * and parameters copied from the original instance and the supplied ""charset""
   * parameter.
   * 
   * If null or empty the ""charset"" parameter will not be set or updated.
   */
  @Test
  public void withCharsetNullOrEmptyCharsetTest() throws Fault {
    String charset = "UTF-8";
    String type = MediaType.APPLICATION_FORM_URLENCODED_TYPE.getType();
    String subtype = MediaType.APPLICATION_FORM_URLENCODED_TYPE.getSubtype();
    MediaType original = new MediaType(type, subtype, charset);
    for (String newCharset : new String[] { null, "" }) {
      MediaType created = original.withCharset(newCharset);
      verifyMediaType(created, type, subtype);
      assertTrue(created != original, "withCharset(null) did NOT create a new instance");
      assertCharset(created, charset);
      assertCharset(original, charset);
    }
  }

  // ////////////////////////////////////////////////////////////////////////

  private static//
  void assertCharset(MediaType mediaType, String charset) throws Fault {
    String characterSet = MediaType.CHARSET_PARAMETER + "=" + charset;
    assertContainsIgnoreCase(mediaType.toString(), characterSet, "MediaType", mediaType, "does not contain expected",
        characterSet);
    logMsg("MediaType", mediaType, "contains character set", characterSet, "as expected");
  }

  private static void verifyMediaType(MediaType mt, String type, String subtype) throws Fault {
    StringBuffer sb = new StringBuffer();
    boolean pass = true;

    if (!mt.getType().equals(type)) {
      pass = false;
      sb.append("Failed type test.  Expect " + type + " got " + mt.getType());
    }

    if (!mt.getSubtype().equals(subtype)) {
      pass = false;
      sb.append("Failed subtype test.  Expect " + type + " got " + mt.getSubtype());
    }

    assertTrue(pass, "at least one assertion failed: " + sb.toString());
  }

  private static void verifyMediaType(MediaType mt, String type, String subtype, Map<String, String> params,
      String[] pname) throws Fault {
    StringBuffer sb = new StringBuffer();
    boolean pass = true;

    if (!mt.getType().equals(type)) {
      pass = false;
      sb.append("Failed type test.  Expect " + type + " got " + mt.getType());
    }

    if (!mt.getSubtype().equals(subtype)) {
      pass = false;
      sb.append("Failed subtype test.  Expect " + type + " got " + mt.getSubtype());
    }

    Map<String, String> param_actual = mt.getParameters();
    if (params.size() != param_actual.size()) {
      pass = false;
      sb.append(
          "Parameters size are different. " + " Expecting " + params.size() + ", got " + param_actual.size() + ".");
    }

    int i = pname.length;
    int j = 0;
    while (j < i) {
      sb.append("Processing Parameter " + j);
      if (!param_actual.containsKey(pname[j])) {
        pass = false;
        sb.append("Parameter Key " + pname[j] + " not found.");
      } else {
        String tmp = param_actual.get(pname[j]);

        if (tmp.equals((String) params.get(pname[j]))) {
          break;
        } else if (tmp.startsWith("\"") || tmp.equals("\"" + params.get(pname[j]) + "\"")) {
          break;
        } else {
          pass = false;
          sb.append("Parameter Key " + pname[j] + " returned different value than expected.");
          sb.append("expecting " + (String) params.get(pname[j]) + ", got " + (String) param_actual.get(pname[j]));
        }
        j++;
      }

      assertTrue(pass, "at least one assertion failed:" + sb.toString());
    }
  }
}