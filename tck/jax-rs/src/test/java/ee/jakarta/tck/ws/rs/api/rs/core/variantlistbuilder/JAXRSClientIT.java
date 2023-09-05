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

package ee.jakarta.tck.ws.rs.api.rs.core.variantlistbuilder;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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

  private static final long serialVersionUID = 1L;

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
   * @testName: newInstanceTest
   *
   * @assertion_ids: JAXRS:JAVADOC:268; JAXRS:JAVADOC:264;
   *
   * @test_Strategy: Create a VariantListBuilder instance using
   * Variant.VariantListBuilder.newInstance() Verify that no Variant in the
   * returned List
   */
  @Test
  public void newInstanceTest() throws Fault {
    List<Variant> vt = Variant.VariantListBuilder.newInstance().build();
    if (!vt.isEmpty()) {
      throw new Fault("List<Variant> returned is not empty.  It includes " + vt.size() + " Variant.");
    }
  }

  /*
   * @testName: mediaTypesTest1
   * 
   * @assertion_ids: JAXRS:SPEC:33.7; JAXRS:SPEC:33.8; JAXRS:JAVADOC:268;
   * JAXRS:JAVADOC:264; JAXRS:JAVADOC:267; JAXRS:JAVADOC:263; JAXRS:JAVADOC:255;
   * JAXRS:JAVADOC:256; JAXRS:JAVADOC:257;
   * 
   * @test_Strategy: Create a VariantListBuilder instance using
   * Variant.VariantListBuilder.newInstance(); Add two MediaType to it; Verify
   * that two Variant in the returned List
   */
  @Test
  public void mediaTypesTest1() throws Fault {
    boolean pass = true;
    StringBuffer sb = new StringBuffer();

    List<String> encoding = new ArrayList<String>();
    List<String> lang = new ArrayList<String>();

    MediaType mt1 = new MediaType();
    MediaType mt2 = new MediaType("application", "atom+xml");

    List<MediaType> types = Arrays.asList(mt1, mt2);

    List<Variant> vts = Variant.VariantListBuilder.newInstance().mediaTypes(mt1, mt2).add().build();

    String status = verifyVariants(vts, types, encoding, lang, 2);
    if (status.endsWith("false")) {
      pass = false;
    }
    sb.append(status);

    assertTrue(pass, "At least one assertion faled: " + sb.toString());
    TestUtil.logTrace(sb.toString());
  }

  /*
   * @testName: mediaTypesTest2
   * 
   * @assertion_ids: JAXRS:SPEC:33.7; JAXRS:SPEC:33.8; JAXRS:SPEC:33.9;
   * JAXRS:JAVADOC:268; JAXRS:JAVADOC:264; JAXRS:JAVADOC:267; JAXRS:JAVADOC:263;
   * JAXRS:JAVADOC:255; JAXRS:JAVADOC:256; JAXRS:JAVADOC:257;
   * 
   * @test_Strategy: Create a VariantListBuilder instance using
   * Variant.VariantListBuilder.newInstance(); Add three MediaType to it; Verify
   * that two Variant in the returned List
   */
  @Test
  public void mediaTypesTest2() throws Fault {
    boolean pass = true;
    StringBuffer sb = new StringBuffer();

    List<String> encoding = new ArrayList<String>();
    List<String> lang = new ArrayList<String>();

    Map<String, String> params = new HashMap<String, String>();

    MediaType mt1 = new MediaType();
    MediaType mt2 = new MediaType("application", "atom+xml");
    MediaType mt3 = new MediaType("application", "x-www-form-urlencoded", params);
    List<MediaType> types = Arrays.asList(mt1, mt2, mt3);

    List<Variant> vts = Variant.VariantListBuilder.newInstance().mediaTypes(mt1, mt2, mt3).add().build();
    String status = verifyVariants(vts, types, encoding, lang, 3);
    if (status.endsWith("false")) {
      pass = false;
    }
    sb.append(status);

    assertTrue(pass, "At least one assertion faled: " + sb.toString());
    TestUtil.logTrace(sb.toString());
  }

  /*
   * @testName: languageTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:268; JAXRS:JAVADOC:264; JAXRS:JAVADOC:267;
   * JAXRS:JAVADOC:263; JAXRS:JAVADOC:266; JAXRS:JAVADOC:255; JAXRS:JAVADOC:256;
   * JAXRS:JAVADOC:257;
   * 
   * @test_Strategy: Create a VariantListBuilder instance using
   * Variant.VariantListBuilder.newInstance(); Add one MediaType to it with three
   * languages; Verify that three Variant in the returned List
   */
  @Test
  public void languageTest() throws Fault {
    boolean pass = true;
    StringBuffer sb = new StringBuffer();

    List<String> encoding = new ArrayList<String>();

    MediaType mt1 = new MediaType();
    List<MediaType> types = Arrays.asList(mt1);

    List<Variant> vts = Variant.VariantListBuilder.newInstance().mediaTypes(mt1)
        .languages(new Locale("en", "US"), new Locale("en", "GB"), new Locale("zh", "CN")).add().build();
    int size = vts.size();
    sb.append("size================== " + size + newline);
    String status = verifyVariants(vts, types, encoding, getLangList(), 3);
    if (status.endsWith("false")) {
      pass = false;
    }
    sb.append(status);

    assertTrue(pass, "At least one assertion faled: " + sb.toString());
    TestUtil.logTrace(sb.toString());
  }

  /*
   * @testName: encodingTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:268; JAXRS:JAVADOC:264; JAXRS:JAVADOC:267;
   * JAXRS:JAVADOC:263; JAXRS:JAVADOC:265; JAXRS:JAVADOC:255; JAXRS:JAVADOC:256;
   * JAXRS:JAVADOC:257;
   * 
   * @test_Strategy: Create a VariantListBuilder instance using
   * Variant.VariantListBuilder.newInstance(); Add one MediaType to it with two
   * encodings; Verify that two variants in the returned List
   */
  @Test
  public void encodingTest() throws Fault {
    boolean pass = true;
    StringBuffer sb = new StringBuffer();

    List<String> encoding = Arrays.asList("gzip", "compress");
    List<String> lang = new ArrayList<String>();

    MediaType mt1 = new MediaType();
    List<MediaType> types = Arrays.asList(mt1);

    List<Variant> vts = Variant.VariantListBuilder.newInstance().mediaTypes(mt1).encodings("gzip", "compress").add()
        .build();

    String status = verifyVariants(vts, types, encoding, lang, 2);
    if (status.endsWith("false")) {
      pass = false;
    }
    sb.append(status);

    TestUtil.logTrace(sb.toString());
    assertTrue(pass, "At least one assertion faled: " + sb.toString());
  }

  /*
   * @testName: complexTest
   * 
   * @assertion_ids: JAXRS:SPEC:33.7; JAXRS:SPEC:33.8; JAXRS:JAVADOC:268;
   * JAXRS:JAVADOC:264; JAXRS:JAVADOC:267; JAXRS:JAVADOC:263; JAXRS:JAVADOC:265;
   * JAXRS:JAVADOC:266; JAXRS:JAVADOC:255; JAXRS:JAVADOC:256; JAXRS:JAVADOC:257;
   * 
   * @test_Strategy: Create a VariantListBuilder instance using
   * Variant.VariantListBuilder.newInstance(); Add two MediaType to it with three
   * language and two encodings; Verify that 12 variants in the returned List
   */
  @Test
  public void complexTest() throws Fault {
    boolean pass = true;
    StringBuffer sb = new StringBuffer();

    List<String> encoding = Arrays.asList("gzip", "compress");

    MediaType mt1 = new MediaType();
    MediaType mt2 = new MediaType("application", "atom+xml");
    List<MediaType> types = Arrays.asList(mt1, mt2);

    List<Variant> vts = getVariantList(encoding, mt1, mt2);

    String status = verifyVariants(vts, types, encoding, getLangList(), 12);
    if (status.endsWith("false")) {
      pass = false;
    }
    sb.append(status);

    TestUtil.logTrace(sb.toString());
    assertTrue(pass, "At least one assertion faled: " + sb.toString());
  }

  private static String verifyVariants(List<Variant> vts, List<MediaType> types, List<String> encoding,
      List<String> lang, int size_expected) {
    StringBuffer sb = new StringBuffer();
    boolean pass = true;
    List<MediaType> actual_types = new ArrayList<MediaType>();

    sb.append(newline + "========== Verifying Variants" + newline);

    int size_vt = vts.size();

    if (size_vt != size_expected) {
      sb.append(indent + "Test Failed: List<Variant> returned is not right.  It includes " + size_vt
          + " Variant Objects. Expecting " + size_expected + newline);

      pass = false;

      for (int j = 0; j < vts.size(); j++) {
        Variant vt = vts.get(j);
        sb.append(indent + "Variant " + j + ": MediaType: " + vt.getMediaType().toString() + "; " + "Langauge: "
            + vt.getLanguage() + "; Encoding: " + vt.getEncoding() + newline);
      }
    } else {
      sb.append(indent + "Correct number of Variant returned: " + size_expected + newline);
    }

    for (Variant vt : vts) {

      sb.append(indent + "===== Verifying Variant" + newline);
      MediaType mt = vt.getMediaType();

      if (!types.contains(mt)) {
        pass = false;
        sb.append(indent + "Unexpected MediaType found in variant: type=" + mt.getType() + " subtype = "
            + mt.getSubtype() + newline);
        sb.append(indent + "Expecting the following MediaType:" + newline);
        for (MediaType tmp : types) {
          sb.append(indent + indent + tmp.getType() + "/" + tmp.getSubtype() + newline);
        }
      } else {
        sb.append(
            indent + "Expected MediaType found Variant: " + mt.getType() + " subtype = " + mt.getSubtype() + newline);
        actual_types.add(mt);
      }

      if (encoding == null || encoding.isEmpty()) {
        if (vt.getEncoding() != null) {
          pass = false;
          sb.append(indent + "Unexpected encoding found: " + vt.getEncoding().toString());
        }
      } else {
        if (vt.getEncoding() == null) {
          pass = false;
          sb.append(indent + "No encoding found: " + vt.getEncoding() + newline);
          sb.append(indent + "Expecting the following encodings: " + newline);

          for (String tmp : encoding) {
            sb.append(indent + indent + tmp + newline);
          }
        } else if (!encoding.contains(vt.getEncoding().toString().toLowerCase())) {
          sb.append(indent + "Unexpected encoding found: " + vt.getEncoding() + newline);
          sb.append(indent + "Expecting the following encodings: " + newline);
          for (String tmp : lang) {
            sb.append(indent + indent + tmp + newline);
          }
          pass = false;
        } else {
          sb.append(indent + "Found expected encoding " + vt.getEncoding() + newline);
        }

        if (lang == null || lang.isEmpty()) {
          if (vt.getLanguage() != null) {
            pass = false;
            sb.append(indent + "Unexpected language found: " + vt.getLanguage().toString());
          }
        } else {
          if (vt.getLanguage() == null) {
            pass = false;
            sb.append(indent + "No language found: " + vt.getLanguage() + newline);
            sb.append(indent + "Expecting the following languages: " + newline);

            for (String tmp : lang) {
              sb.append(indent + indent + tmp + newline);
            }
          } else if (!lang.contains(langToString(vt.getLanguage()))) {
            sb.append(indent + "Unexpected language found: " + vt.getLanguage() + newline);
            sb.append(indent + "Expecting the following languages: " + newline);
            for (String tmp : lang) {
              sb.append(indent + indent + tmp + newline);
            }
            pass = false;
          } else {
            sb.append(indent + "Found expected language " + vt.getLanguage() + newline);
          }
        }
      }
    }

    Set<MediaType> set = new LinkedHashSet<MediaType>();
    set.addAll(actual_types);
    List<MediaType> final_types = new ArrayList<MediaType>(set);

    if (types.size() != final_types.size()) {
      pass = false;
      sb.append("Some missing MediaType: expecting " + types.size() + ", only got " + final_types.size() + newline);

      for (int i = 0; i < final_types.size(); i++) {
        sb.append(final_types.get(i).toString() + newline);
      }
    }
    sb.append(pass);
    return sb.toString();
  }

  protected static List<String> getLangList() {
    return Arrays.asList("en-US", "en-GB", "zh-CN");
  }

  protected static List<Variant> getVariantList(List<String> encoding, MediaType... mt) {
    return Variant.VariantListBuilder.newInstance().mediaTypes(mt)
        .languages(new Locale("en", "US"), new Locale("en", "GB"), new Locale("zh", "CN"))
        .encodings(encoding.toArray(new String[0])).add().build();
  }

  protected static String langToString(Object object) {
    Locale locale = null;
    if (object instanceof List)
      object = ((List<?>) object).iterator().next();
    if (object instanceof Locale)
      locale = (Locale) object;
    return locale == null ? object.toString() : locale.toString().replace("_", "-");
  }

}
