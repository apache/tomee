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

package ee.jakarta.tck.ws.rs.api.rs.core.variant;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Variant;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/*
 * @class.setup_props: webServerHost; webServerPort; ts_home;
 */
public class JAXRSClientIT extends JAXRSCommonClient {

    private static final long serialVersionUID = 396770878268682189L;

    public JAXRSClientIT() {
      setContextRoot("/jaxrs_rs_core_variantTest_web");
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
     * @testName: constructorTest1
     * 
     * @assertion_ids: JAXRS:JAVADOC:255; JAXRS:JAVADOC:256; JAXRS:JAVADOC:257;
     * JAXRS:JAVADOC:261; JAXRS:JAVADOC:262;
     * 
     * @test_Strategy: Create an MediaType instance using MediaType()
     */
    @Test
    public void constructorTest1() throws Fault {
      String type = jakarta.ws.rs.core.MediaType.MEDIA_TYPE_WILDCARD;
      String subtype = jakarta.ws.rs.core.MediaType.MEDIA_TYPE_WILDCARD;
      String encoding = "ISO8859-15";
      Locale lang = new Locale("en", "US");
  
      MediaType mt1 = new MediaType();
  
      Variant vt = new Variant(mt1, lang, encoding);
      verifyVariant(vt, type, subtype, lang, encoding);
    }
  
    /*
     * @testName: constructorTest2
     * 
     * @assertion_ids: JAXRS:JAVADOC:255; JAXRS:JAVADOC:256; JAXRS:JAVADOC:257;
     * JAXRS:JAVADOC:261; JAXRS:JAVADOC:262;
     * 
     * @test_Strategy: Create an MediaType instance using MediaType(String,
     * String)
     */
    @Test
    public void constructorTest2() throws Fault {
      String type = "application";
      String subtype = "atom+xml";
      String encoding = "";
      Locale lang = null;
  
      MediaType mt2 = new MediaType(type, subtype);
  
      Variant vt = new Variant(mt2, lang, encoding);
      verifyVariant(vt, type, subtype, lang, encoding);
    }
  
    /*
     * @testName: constructorTest3
     * 
     * @assertion_ids: JAXRS:JAVADOC:255; JAXRS:JAVADOC:256; JAXRS:JAVADOC:257;
     * JAXRS:JAVADOC:261; JAXRS:JAVADOC:262;
     * 
     * @test_Strategy: Create an MediaType instance using MediaType(String,
     * String)
     */
    @Test
    public void constructorTest3() throws Fault {
      String type = "application";
      String subtype = "x-www-form-urlencoded";
      Map<String, String> params = new HashMap<String, String>();
      String encoding = null;
      Locale lang = null;
  
      MediaType mt3 = new MediaType(type, subtype, params);
      Variant vt = new Variant(mt3, lang, encoding);
      verifyVariant(vt, type, subtype, params, new String[0], lang, encoding);
    }
  
    /*
     * @testName: constructorMediaStringStringTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:900;
     * 
     * @test_Strategy: Create a Variant(MediaType, String, String)
     */
    @Test
    public void constructorMediaStringStringTest() throws Fault {
      Variant vt = null;
      String[] encodings = { null, "CP1250", "UTF8", "ISO8859-2" };
      Locale[] langs = { null, Locale.CANADA, Locale.FRENCH };
  
      for (String encoding : encodings)
        for (Locale lang : langs) {
          vt = new Variant(MediaType.APPLICATION_XHTML_XML_TYPE,
              lang == null ? null : lang.toString(), encoding);
          verifyVariant(vt, MediaType.APPLICATION_XHTML_XML_TYPE.getType(),
              MediaType.APPLICATION_XHTML_XML_TYPE.getSubtype(), lang, encoding);
        }
    }
  
    /*
     * @testName: constructorMediaStringStringThrowsIllegalArgumentExceptionTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:900;
     * 
     * @test_Strategy: throws IllegalArgumentException - if all the parameters are
     * null.
     */
    @Test
    public void constructorMediaStringStringThrowsIllegalArgumentExceptionTest()
        throws Fault {
      try {
        new Variant((MediaType) null, (String) null, (String) null);
        throw new Fault("Did not throw IllegalArgumentException for all nulls");
      } catch (IllegalArgumentException e) {
        logMsg("Thrown IllegalArgumentException as expected");
      }
    }
  
    /*
     * @testName: constructorMediaStringStringStringTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:901;
     * 
     * @test_Strategy: Create a Variant(MediaType, String, String, String)
     */
    @Test
    public void constructorMediaStringStringStringTest() throws Fault {
      Variant vt = null;
      String[] encodings = { null, "CP1250", "UTF8", "ISO8859-2" };
      Locale[] langs = { null, Locale.CANADA, Locale.FRENCH };
  
      for (String encoding : encodings)
        for (Locale lang : langs) {
          vt = new Variant(MediaType.APPLICATION_XHTML_XML_TYPE,
              lang == null ? null : lang.getLanguage(),
              lang == null ? null : lang.getCountry(), encoding);
          verifyVariant(vt, MediaType.APPLICATION_XHTML_XML_TYPE.getType(),
              MediaType.APPLICATION_XHTML_XML_TYPE.getSubtype(), lang, encoding);
        }
    }
  
    /*
     * @testName:
     * constructorMediaStringStringStringThrowsIllegalArgumentExceptionTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:901;
     * 
     * @test_Strategy: throws IllegalArgumentException - if all the parameters are
     * null.
     */
    @Test
    public void constructorMediaStringStringStringThrowsIllegalArgumentExceptionTest()
        throws Fault {
      try {
        new Variant((MediaType) null, (String) null, (String) null,
            (String) null);
        throw new Fault("Did not throw IllegalArgumentException for all nulls");
      } catch (IllegalArgumentException e) {
        logMsg("Thrown IllegalArgumentException as expected");
      }
    }
  
    /*
     * @testName: constructorMediaStringStringStringStringTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:902;
     * 
     * @test_Strategy: Create a Variant(MediaType, String, String, String, String)
     */
    @Test
    public void constructorMediaStringStringStringStringTest() throws Fault {
      Variant vt = null;
      String[] encodings = { null, "CP1250", "UTF8", "ISO8859-2" };
      Locale[] langs = { null, Locale.CANADA, Locale.FRENCH };
  
      for (String encoding : encodings)
        for (Locale lang : langs) {
          vt = new Variant(MediaType.APPLICATION_XHTML_XML_TYPE,
              lang == null ? null : lang.getLanguage(),
              lang == null ? null : lang.getCountry(),
              lang == null ? null : lang.getVariant(), encoding);
          verifyVariant(vt, MediaType.APPLICATION_XHTML_XML_TYPE.getType(),
              MediaType.APPLICATION_XHTML_XML_TYPE.getSubtype(), lang, encoding);
        }
    }
  
    /*
     * @testName:
     * constructorMediaStringStringStringStringThrowsIllegalArgumentExceptionTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:902;
     * 
     * @test_Strategy: throws IllegalArgumentException - if all the parameters are
     * null.
     */
    @Test
    public void constructorMediaStringStringStringStringThrowsIllegalArgumentExceptionTest()
        throws Fault {
      try {
        new Variant((MediaType) null, (String) null, (String) null, (String) null,
            (String) null);
        throw new Fault("Did not throw IllegalArgumentException for all nulls");
      } catch (IllegalArgumentException e) {
        logMsg("Thrown IllegalArgumentException as expected");
      }
    }
  
    /*
     * @testName: equalTest1
     * 
     * @assertion_ids: JAXRS:JAVADOC:262; JAXRS:JAVADOC:254; JAXRS:JAVADOC:258;
     * 
     * @test_Strategy: Create two Variants with different Locale. Verify
     * Variant.equals and Variant.hasCode methods work properly using those two
     * Variant objects.
     */
    @Test
    public void equalTest1() throws Fault {
      StringBuffer sb = new StringBuffer();
      boolean pass;
  
      String type = jakarta.ws.rs.core.MediaType.MEDIA_TYPE_WILDCARD;
      String subtype = jakarta.ws.rs.core.MediaType.MEDIA_TYPE_WILDCARD;
  
      MediaType mt1 = new MediaType();
      pass = checkMediaTypeForEqualTest1(sb, mt1);
  
      MediaType mt2 = new MediaType(type, subtype);
      pass &= checkMediaTypeForEqualTest1(sb, mt2);
  
      assertTrue(pass, "At least one assertion failed: " + sb.toString());
    }
  
    private static boolean checkMediaTypeForEqualTest1(StringBuffer sb,
        MediaType type) {
      String encoding = "ISO8859-15";
      Locale lang1 = new Locale("en", "US");
      Locale lang2 = null;
      Boolean pass = true;
  
      Variant vt1 = new Variant(type, lang1, encoding);
      Variant vt2 = new Variant(type, lang2, encoding);
  
      if (vt1.equals(vt2)) {
        pass = false;
        sb.append("Equals Test1 Failed" + newline);
      }
  
      if (vt1.hashCode() == vt2.hashCode()) {
        sb.append(
            "hasCode Test1 Failed: vt1.hashCode()=" + vt1.hashCode() + newline);
        sb.append(
            "                      vt2.hashCode()=" + vt2.hashCode() + newline);
        pass = false;
      }
      return pass;
    }
  
    /*
     * @testName: equalsTest2
     * 
     * @assertion_ids: JAXRS:JAVADOC:262; JAXRS:JAVADOC:254; JAXRS:JAVADOC:258;
     * 
     * @test_Strategy: Create two Variants with different MediaType. Verify
     * Variant.equals and Variant.hasCode methods work properly using those two
     * Variant objects.
     */
    @Test
    public void equalsTest2() throws Fault {
      StringBuffer sb = new StringBuffer();
      Boolean pass = true;
  
      String type = "application";
      String subtype = "atom+xml";
      String subtype1 = "xml";
      String encoding = "";
      Locale lang = null;
  
      MediaType mt1 = new MediaType(type, subtype);
      MediaType mt2 = new MediaType(type, subtype1);
  
      Variant vt1 = new Variant(mt1, lang, encoding);
      Variant vt2 = new Variant(mt2, lang, encoding);
  
      if (vt1.equals(vt2)) {
        pass = false;
        sb.append("Equals Test2 Failed" + newline);
      }
  
      if (vt1.hashCode() == vt2.hashCode()) {
        sb.append(
            "hasCode Test2 Failed: vt1.hashCode()=" + vt1.hashCode() + newline);
        sb.append(
            "                      vt2.hashCode()=" + vt2.hashCode() + newline);
        pass = false;
      }
  
      assertTrue(pass, "At least one assertion failed: " + sb.toString());
    }
  
    /*
     * @testName: equalsTest3
     * 
     * @assertion_ids: JAXRS:JAVADOC:262; JAXRS:JAVADOC:254; JAXRS:JAVADOC:258;
     * 
     * @test_Strategy: Create two Variants with the same properties. Verify
     * Variant.equals and Variant.hasCode methods work properly using those two
     * Variant objects.
     */
    @Test
    public void equalsTest3() throws Fault {
      StringBuffer sb = new StringBuffer();
      Boolean pass = true;
  
      String type = "application";
      String subtype = "x-www-form-urlencoded";
      Map<String, String> params = new HashMap<String, String>();
      String encoding = null;
      Locale lang = null;
  
      MediaType mt4 = new MediaType(type, subtype, params);
  
      Variant vt1 = new Variant(mt4, lang, encoding);
      Variant vt2 = new Variant(mt4, lang, encoding);
  
      if (!vt1.equals(vt2)) {
        pass = false;
        sb.append("Equals Test3 Failed" + newline);
      }
  
      if (vt1.hashCode() != vt2.hashCode()) {
        sb.append(
            "hasCode Test3 Failed: vt1.hashCode()=" + vt1.hashCode() + newline);
        sb.append(
            "                      vt2.hashCode()=" + vt2.hashCode() + newline);
        pass = false;
      }
  
      assertTrue(pass, "At least one assertion failed: " + sb.toString());
    }
  
    /*
     * @testName: languagesTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:259;
     * 
     * @test_Strategy: Call Variant.languages(Locale ...) with three Locales,
     * Verify that three Variant instances created properly
     */
    @Test
    public void languagesTest() throws Fault {
      List<String> encodingS = null;
      List<MediaType> mts = null;
  
      List<Variant> vts = Variant.languages(new Locale("en", "US"),
          new Locale("en", "GB"), new Locale("zh", "CN")).add().build();
  
      verifyVariants(vts, mts, getLangList(), encodingS);
    }
  
    /*
     * @testName: getLanguageStringTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:899;
     * 
     * @test_Strategy: Get the string representation of the variant language
     */
    @Test
    public void getLanguageStringTest() throws Fault {
      Locale[] langs = { null, Locale.CHINA, Locale.PRC, Locale.CANADA_FRENCH,
          Locale.GERMAN };
      for (Locale lang : langs) {
        Variant v = new Variant(MediaType.TEXT_PLAIN_TYPE, lang, (String) null);
        if (lang != null)
          assertTrue(v.getLanguageString().contains(lang.getLanguage()),
              "Created variant contained " + v.getLanguageString() +
              "but was expected " + lang.getLanguage());
        else
          assertTrue(v.getLanguageString() == null, "#getLanguageString was " +
              v.getLanguageString() + " expected was null");
      }
      logMsg("#getLanguageString returned exected language string");
    }
  
    /*
     * @testName: encodingsTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:253;
     * 
     * @test_Strategy: Call Variant.encodings(String ...) with three Encodings,
     * Verify that three Variant instances created properly
     */
    @Test
    public void encodingsTest() throws Fault {
      String encoding1 = "ISO8859-15";
      String encoding2 = "GB2312";
      String encoding3 = "UTF-8";
  
      List<String> encodingS = Arrays.asList(encoding1, encoding2, encoding3);
      List<String> langS = null;
      List<MediaType> mts = null;
  
      List<Variant> vts = Variant.encodings(encoding1, encoding2, encoding3).add()
          .build();
  
      verifyVariants(vts, mts, langS, encodingS);
    }
  
    /*
     * @testName: mediaTypesTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:260;
     * 
     * @test_Strategy: Call Variant.mediaTypes(MediaType ...) with three MediaType
     * objects, Verify that three Variant instances created properly
     */
    @Test
    public void mediaTypesTest() throws Fault {
      String type = "application";
      String subtype = "x-www-form-urlencoded";
  
      MediaType mt = new MediaType(type, subtype);
  
      type = "application";
      subtype = "atom+xml";
  
      MediaType mt1 = new MediaType(type, subtype);
  
      type = jakarta.ws.rs.core.MediaType.MEDIA_TYPE_WILDCARD;
      subtype = jakarta.ws.rs.core.MediaType.MEDIA_TYPE_WILDCARD;
  
      MediaType mt2 = new MediaType();
  
      List<String> langS = null;
      List<String> encodingS = null;
      List<MediaType> mts = Arrays.asList(mt, mt1, mt2);
  
      List<Variant> vts = Variant.mediaTypes(mt, mt1, mt2).add().build();
  
      verifyVariants(vts, mts, langS, encodingS);
    }
  
    // ////////////////////////////////////////////////////////////////////////
  
    private static void verifyVariants(List<Variant> vts, List<MediaType> mts,
        List<String> langs, List<String> encodings) throws Fault {
      StringBuffer sb = new StringBuffer();
      boolean pass = true;
  
      for (Variant vt : vts) {
        if (mts != null) {
          MediaType mt = vt.getMediaType();
  
          if (mt != null && !mts.contains(mt)) {
            pass = false;
            sb.append("MediaType is not found: " + mt + newline);
          }
        }
  
        if (langs != null) {
          if (!langs.contains(vt.getLanguage().toString())) {
            sb.append("Language not found: " + vt.getLanguage() + "." + newline);
  
          }
        }
  
        if (encodings != null) {
          if (!encodings.contains(vt.getEncoding())) {
            pass = false;
            sb.append("Encoding not found: " + vt.getEncoding() + newline);
          }
        }
      }
  
      int m = 1;
      int e = 1;
      int l = 1;
      if (mts != null) {
        m = mts.size();
      }
  
      if (encodings != null) {
        e = encodings.size();
      }
  
      if (langs != null) {
        l = langs.size();
      }
  
      if (vts.size() != m * e * l) {
        pass = false;
        sb.append("Number of Variants is incorrect, expecting " + m + "*" + e
            + "*" + l + ", got " + vts.size() + newline);
      }
  
      if (!pass) {
  
        sb.append("Expected language: ");
        if (langs != null) {
          for (String lang : langs) {
            sb.append(lang + ", ");
          }
          sb.append(newline);
        }
  
        sb.append("Expected encodings: ");
        if (encodings != null) {
          for (String encoding : encodings) {
            sb.append(encoding + ", ");
          }
          sb.append(newline);
        }
  
        sb.append("Expected MediaType: ");
        for (MediaType mt : mts) {
          sb.append(mt.toString() + ", ");
        }
        sb.append(newline);
  
        throw new Fault("at least one assertion failed: " + sb.toString());
      }
    }
  
    private static void verifyVariant(Variant vt, String type, String subtype,
        Locale lang, String encoding) throws Fault {
      StringBuffer sb = new StringBuffer();
      boolean pass = true;
  
      MediaType mt = vt.getMediaType();
  
      if (!equals(mt.getType(), type)) {
        pass = false;
        append(sb, "Failed type test.  Expect ", type, mt.getType());
      }
  
      if (!equals(mt.getSubtype(), subtype)) {
        pass = false;
        append(sb, "Failed subtype test.", type, mt.getSubtype());
      }
  
      if (vt.getLanguage() == null && lang != null) {
        pass = false;
        append(sb, "Failed langauge test", lang, vt.getLanguage());
      } else if (vt.getLanguage() != null
          && !vt.getLanguage().toString().equalsIgnoreCase(lang.toString())) {
        pass = false;
        append(sb, "Failed langauge test.", lang, vt.getLanguage());
      }
  
      if (encoding == null || encoding.equals("")) {
        if (!(vt.getEncoding() == null || vt.getEncoding().equals(""))) {
          pass = false;
          append(sb, "Failed encoding test", encoding, vt.getEncoding());
        }
      } else if (!equals(vt.getEncoding(), encoding)) {
        pass = false;
        append(sb, "Failed encoding test.", encoding, vt.getEncoding());
      }
  
      pass &= verifyToString(vt, sb);
      assertTrue(pass, "At least one assertion failed: " + sb.toString());
    }
  
    private static void verifyVariant(Variant vt, String type, String subtype,
        Map<String, String> params, String[] pname, Locale lang, String encoding)
        throws Fault {
      StringBuffer sb = new StringBuffer();
      boolean pass = true;
      MediaType mt = vt.getMediaType();
  
      if (!equals(mt.getType(), type)) {
        pass = false;
        append(sb, "Failed type test.", type, mt.getType());
      }
  
      if (!equals(mt.getSubtype(), subtype)) {
        pass = false;
        append(sb, "Failed subtype test.", subtype, mt.getSubtype());
      }
  
      Map<String, String> param_actual = mt.getParameters();
      if (params.size() != param_actual.size()) {
        pass = false;
        append(sb, "Parameters size are different.", params.size(),
            param_actual.size());
      }
  
      int i = pname.length;
      int j = 0;
      while (j < i) {
        sb.append("Processing Parameter " + j);
        if (!param_actual.containsKey(pname[j])) {
          pass = false;
          sb.append("Parameter Key " + pname[j] + " not found." + newline);
        } else {
          String tmp = param_actual.get(pname[j]);
  
          if (equals(tmp, params.get(pname[j]))) {
            break;
          } else if (tmp.startsWith("\"")
              || tmp.equals("\"" + params.get(pname[j]) + "\"")) {
            break;
          } else {
            pass = false;
            sb.append("Parameter Key ").append(pname[j])
                .append(" returned different value than expected.")
                .append(newline);
            append(sb, "", params.get(pname[j]), param_actual.get(pname[j]));
          }
          j++;
        }
  
        assertTrue(pass, "At least one assertion failed: " + sb.toString());
      }
  
      if (vt.getLanguage() == null) {
        if (lang != null) {
          pass = false;
          append(sb, "Failed language test.", lang, vt.getLanguage());
        }
      } else if (!equals(vt.getLanguage(), lang)) {
        pass = false;
        append(sb, "Failed language test.", lang, vt.getLanguage());
      }
  
      if (encoding == null || encoding == "") {
        if (!(vt.getEncoding() == null || vt.getEncoding().equals(""))) {
          pass = false;
          append(sb, "Failed encoding test.", encoding, vt.getEncoding());
        }
      } else if (!equals(vt.getEncoding(), encoding)) {
        pass = false;
        append(sb, "Failed encoding test.", encoding, vt.getEncoding());
      }
  
      pass &= verifyToString(vt, sb);
    }
  
    /**
     * Check whether the toString() method has been overridden. Compare the result
     * String of toString() with what Object.toString() would return.
     * 
     * @param variant
     * @param sb
     * @return False if toString() has NOT been overridden
     */
    private static boolean verifyToString(Variant variant, StringBuffer sb) {
      String badPrefix = Variant.class.getName() + "@";
      boolean startsWith = variant.toString().startsWith(badPrefix);
      startsWith &= variant.toString().length() <= badPrefix.length() + 8;
      if (startsWith)
        sb.append("Variant.toString() is not overridden");
      return !startsWith;
    }
  
    private static void append(StringBuffer sb, Object... args) {
      sb.append(args[0]);
      sb.append(" Expect ");
      sb.append(args[1]);
      sb.append(" got ");
      sb.append(args[2]);
      sb.append(newline);
    }
  
    private static <T> boolean equals(T first, T second) {
      return first == null ? null == second : first.equals(second);
    }
  
    protected List<String> getLangList() {
      return Arrays.asList("en-US", "en-GB", "zh-CN");
    }
    
}
