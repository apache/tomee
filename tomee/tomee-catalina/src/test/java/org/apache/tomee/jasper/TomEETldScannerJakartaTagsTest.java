/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.jasper;

import org.apache.tomcat.util.descriptor.tld.TaglibXml;
import org.apache.tomcat.util.descriptor.tld.TldResourcePath;
import org.apache.tomcat.util.descriptor.tld.ValidatorXml;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * TOMEE-4651: Jakarta Tags 3.0 renamed the JSTL taglib URIs to the {@code jakarta.tags.*} form.
 * The shaded taglibs-standard TLDs only declare the legacy {@code http://java.sun.com/jsp/jstl/*}
 * spellings, so {@link TomEETldScanner} registers the new URIs as aliases of the same resources.
 * <p>
 * The shaded taglibs jar is not on this module's test classpath, so the scanner's own static data is
 * empty here. These tests therefore drive the aliasing against a synthetic resource map instead.
 */
public class TomEETldScannerJakartaTagsTest {

    private static final String PERMITTED_TAGLIBS_URI = "http://jakarta.apache.org/taglibs/standard/permittedTaglibs";

    /**
     * Mirrors what a Jakarta-native JSTL implementation ships, where both spellings resolve to the
     * same TLD. Kept independent of the production map on purpose so a typo there fails the test.
     */
    private static final String[][] ALIASES = {
        {"jakarta.tags.core", "http://java.sun.com/jsp/jstl/core"},
        {"jakarta.tags.fmt", "http://java.sun.com/jsp/jstl/fmt"},
        {"jakarta.tags.sql", "http://java.sun.com/jsp/jstl/sql"},
        {"jakarta.tags.xml", "http://java.sun.com/jsp/jstl/xml"},
        {"jakarta.tags.functions", "http://java.sun.com/jsp/jstl/functions"},
        {"jakarta.tags.permittedTaglibs", PERMITTED_TAGLIBS_URI},
        {"jakarta.tags.scriptfree", "http://jakarta.apache.org/taglibs/standard/scriptfree"},
    };

    private Map<String, TldResourcePath> uris;
    private Map<TldResourcePath, TaglibXml> taglibs;
    private Map<String, TldResourcePath> uriBackup;
    private Map<TldResourcePath, TaglibXml> taglibBackup;

    @Before
    public void backupStaticState() throws Exception {
        uris = staticMap("URI_TLD_RESOURCE");
        taglibs = staticMap("TLD_RESOURCE_TAG_LIB");
        uriBackup = new HashMap<>(uris);
        taglibBackup = new HashMap<>(taglibs);
        uris.clear();
        taglibs.clear();
    }

    @After
    public void restoreStaticState() {
        uris.clear();
        uris.putAll(uriBackup);
        taglibs.clear();
        taglibs.putAll(taglibBackup);
    }

    @Test
    public void jakartaTagsUrisAliasTheLegacyJstlResources() throws Exception {
        givenJstlIsPopulated();

        aliasJakartaTagsUris();

        for (final String[] alias : ALIASES) {
            assertTrue(alias[0] + " was not registered", uris.containsKey(alias[0]));
            // must be the very same instance: scanPlatform() tells myfaces and jstl entries apart by
            // reference identity on the resource path's URL, and TLD_RESOURCE_TAG_LIB is keyed on it
            assertSame(alias[0] + " must reuse the legacy resource path", uris.get(alias[1]), uris.get(alias[0]));
        }
    }

    @Test
    public void legacyUrisKeepWorking() throws Exception {
        givenJstlIsPopulated();

        aliasJakartaTagsUris();

        for (final String[] alias : ALIASES) {
            assertTrue("legacy URI " + alias[1] + " was dropped", uris.containsKey(alias[1]));
        }
    }

    @Test
    public void aliasingIsANoopWithoutJstl() throws Exception {
        // no shaded taglibs in lib/ -> nothing was pre-populated, so nothing may be aliased
        aliasJakartaTagsUris();

        assertEquals(0, uris.size());
    }

    @Test
    public void permittedTaglibsAcceptsBothSpellings() throws Exception {
        givenJstlIsPopulated();

        aliasJakartaTagsUris();

        final List<String> permitted = permittedTaglibs();
        // the four URIs the shaded TLD restricts pages to, plus their Jakarta Tags equivalents
        assertEquals(8, permitted.size());
        for (final String uri : new String[]{"core", "fmt", "sql", "xml"}) {
            assertTrue("legacy " + uri + " missing", permitted.contains("http://java.sun.com/jsp/jstl/" + uri));
            assertTrue("jakarta.tags." + uri + " missing", permitted.contains("jakarta.tags." + uri));
        }
    }

    /**
     * The static initialiser runs once, but guard against the widening being applied twice: doing so
     * would keep appending the same URIs to the validator's init param.
     */
    @Test
    public void aliasingTwiceDoesNotDuplicatePermittedTaglibs() throws Exception {
        givenJstlIsPopulated();

        aliasJakartaTagsUris();
        final List<String> once = permittedTaglibs();
        aliasJakartaTagsUris();

        assertEquals(once, permittedTaglibs());
    }

    @Test
    public void permittedTaglibsIsLeftAloneWithoutJstl() throws Exception {
        aliasJakartaTagsUris();

        assertEquals(0, taglibs.size());
    }

    /**
     * Simulates {@code populateMyfacesAndJstlData()} having run against the shaded taglibs jar:
     * every legacy URI mapped to a resource path, and the permittedTaglibs validator configured as
     * that jar's TLD configures it.
     */
    private void givenJstlIsPopulated() throws Exception {
        final URL jar = new URL("file:/fake/taglibs-shade.jar");
        for (final String[] alias : ALIASES) {
            uris.put(alias[1], new TldResourcePath(jar, null, "META-INF/fake.tld"));
        }

        final ValidatorXml validator = new ValidatorXml();
        validator.setValidatorClass("jakarta.servlet.jsp.jstl.tlv.PermittedTaglibsTLV");
        validator.addInitParam("permittedTaglibs", "http://java.sun.com/jsp/jstl/core\n"
            + "http://java.sun.com/jsp/jstl/fmt\n"
            + "http://java.sun.com/jsp/jstl/sql\n"
            + "http://java.sun.com/jsp/jstl/xml");
        final TaglibXml taglibXml = new TaglibXml();
        taglibXml.setValidator(validator);
        taglibs.put(uris.get(PERMITTED_TAGLIBS_URI), taglibXml);
    }

    private List<String> permittedTaglibs() {
        final TaglibXml taglibXml = taglibs.get(uris.get(PERMITTED_TAGLIBS_URI));
        return Arrays.asList(taglibXml.getValidator().getInitParams().get("permittedTaglibs").split("\n"));
    }

    @SuppressWarnings("unchecked")
    private static <K, V> Map<K, V> staticMap(final String name) throws Exception {
        final Field field = TomEETldScanner.class.getDeclaredField(name);
        field.setAccessible(true);
        return (Map<K, V>) field.get(null);
    }

    private static void aliasJakartaTagsUris() throws Exception {
        final Method method = TomEETldScanner.class.getDeclaredMethod("aliasJakartaTagsUris");
        method.setAccessible(true);
        method.invoke(null);
    }
}
