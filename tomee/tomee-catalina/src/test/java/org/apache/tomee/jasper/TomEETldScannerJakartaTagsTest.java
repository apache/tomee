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

import org.apache.jasper.servlet.TldScanner;
import org.apache.tomcat.util.descriptor.tld.TaglibXml;
import org.apache.xbean.asm9.ClassReader;
import org.apache.xbean.asm9.ClassVisitor;
import org.apache.xbean.asm9.MethodVisitor;
import org.apache.xbean.asm9.Opcodes;
import org.apache.tomcat.util.descriptor.tld.TldResourcePath;
import org.apache.tomcat.util.descriptor.tld.ValidatorXml;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import jakarta.servlet.ServletContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

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
    private static final Logger LOGGER = Logger.getLogger(TomEETldScanner.class.getName());

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
    };

    /**
     * The TLV taglibs the spec did not rename -- they must stay reachable under their legacy URIs and
     * must not gain a {@code jakarta.tags.*} spelling.
     */
    private static final String[] NOT_RENAMED = {
        PERMITTED_TAGLIBS_URI,
        "http://jakarta.apache.org/taglibs/standard/scriptfree",
    };

    private Map<String, TldResourcePath> uris;
    private Map<TldResourcePath, TaglibXml> taglibs;
    private Map<String, TldResourcePath> uriBackup;
    private Map<TldResourcePath, TaglibXml> taglibBackup;
    /** The JSTL jar {@link #givenJstlIsPopulated()} pretended to find, i.e. a non-null JSTL_URL. */
    private URL populatedJstlUrl;
    private Handler logHandler;

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
        if (logHandler != null) {
            LOGGER.removeHandler(logHandler);
            logHandler = null;
        }
        uris.clear();
        uris.putAll(uriBackup);
        taglibs.clear();
        taglibs.putAll(taglibBackup);
    }

    @Test
    public void jakartaTagsUrisAliasTheLegacyJstlResources() throws Exception {
        givenJstlIsPopulated();

        aliasJakartaTagsUris(populatedJstlUrl);

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

        aliasJakartaTagsUris(populatedJstlUrl);

        for (final String[] alias : ALIASES) {
            assertTrue("legacy URI " + alias[1] + " was dropped", uris.containsKey(alias[1]));
        }
        for (final String uri : NOT_RENAMED) {
            assertTrue("legacy URI " + uri + " was dropped", uris.containsKey(uri));
        }
    }

    /**
     * Jakarta Tags 3.0 renamed only the five functional taglibs. permittedTaglibs/scriptfree are
     * Apache-Standard-Taglibs TLVs that kept their URIs, so we must not advertise keys the spec never
     * defines.
     */
    @Test
    public void tlvTaglibsGetNoJakartaTagsSpelling() throws Exception {
        givenJstlIsPopulated();

        aliasJakartaTagsUris(populatedJstlUrl);

        assertFalse(uris.containsKey("jakarta.tags.permittedTaglibs"));
        assertFalse(uris.containsKey("jakarta.tags.scriptfree"));
    }

    /**
     * The guard has to key off JSTL specifically, not off the map being empty: myfaces is
     * pre-populated independently, so on a distribution carrying myfaces but no JSTL an emptiness
     * check would fall through and log a warning for every alias.
     */
    @Test
    public void aliasingIsANoopWithoutJstl() throws Exception {
        givenMyfacesIsPopulatedWithoutJstl();

        final List<String> warnings = captureWarnings();
        aliasJakartaTagsUris(null);

        for (final String[] alias : ALIASES) {
            assertFalse(alias[0] + " must not be registered without JSTL", uris.containsKey(alias[0]));
        }
        // an emptiness check instead of a JSTL check would fall through to the loop and warn per alias
        assertEquals("absent JSTL is normal and must stay quiet", 0, warnings.size());
    }

    @Test
    public void permittedTaglibsAcceptsBothSpellings() throws Exception {
        givenJstlIsPopulated();

        aliasJakartaTagsUris(populatedJstlUrl);

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

        aliasJakartaTagsUris(populatedJstlUrl);
        final List<String> once = permittedTaglibs();
        aliasJakartaTagsUris(populatedJstlUrl);

        assertEquals(once, permittedTaglibs());
    }

    @Test
    public void permittedTaglibsIsLeftAloneWithoutJstl() throws Exception {
        givenMyfacesIsPopulatedWithoutJstl();

        aliasJakartaTagsUris(null);

        assertEquals(0, taglibs.size());
    }

    /**
     * The TLV tokenizes {@code permittedTaglibs} with a plain {@code StringTokenizer}, i.e. on any
     * whitespace. A TLD writing the list space-separated must still be widened.
     */
    @Test
    public void permittedTaglibsHandlesWhitespaceSeparatedLists() throws Exception {
        givenJstlIsPopulated();
        validator().addInitParam("permittedTaglibs",
            "http://java.sun.com/jsp/jstl/core http://java.sun.com/jsp/jstl/fmt");

        aliasJakartaTagsUris(populatedJstlUrl);

        final List<String> permitted = permittedTaglibs();
        assertTrue("jakarta.tags.core missing", permitted.contains("jakarta.tags.core"));
        assertTrue("jakarta.tags.fmt missing", permitted.contains("jakarta.tags.fmt"));
        assertFalse("sql was never permitted", permitted.contains("jakarta.tags.sql"));
    }

    /**
     * Pins that the aliasing is wired into the static initialiser -- every other test drives
     * {@code aliasJakartaTagsUris()} by hand and would stay green if that call were dropped.
     * <p>
     * The shaded taglibs jar is not on this module's test classpath, so the initialiser cannot be
     * observed through its effect on the maps. Assert the call is present in the initialiser's
     * bytecode instead, which holds regardless of what is resolvable at test time.
     */
    @Test
    public void staticInitialiserRegistersTheAliases() throws Exception {
        final ClassReader reader = new ClassReader(TomEETldScanner.class.getName());
        final List<String> calls = new ArrayList<>();
        reader.accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(final int access, final String name, final String descriptor,
                                             final String signature, final String[] exceptions) {
                if (!"<clinit>".equals(name)) {
                    return null;
                }
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public void visitMethodInsn(final int opcode, final String owner, final String method,
                                                final String desc, final boolean isInterface) {
                        calls.add(method);
                    }
                };
            }
        }, ClassReader.SKIP_FRAMES);

        assertTrue("aliasJakartaTagsUris() is not called from the static initialiser",
            calls.contains("aliasJakartaTagsUris"));
        assertTrue("populateMyfacesAndJstlData() must run before the aliasing",
            calls.indexOf("populateMyfacesAndJstlData") < calls.indexOf("aliasJakartaTagsUris"));
    }

    /**
     * Pins the precedence this change establishes: because {@code TldScanner.scan()} runs
     * {@code scanPlatform()} first and {@code parseTld()} only registers a URI it does not already
     * know, the container's alias wins over one an application bundles in {@code WEB-INF/lib}.
     * <p>
     * This is a deliberate trade-off rather than an accident -- it matches how the legacy
     * {@code http://java.sun.com/jsp/jstl/*} URIs have always behaved -- but it does mean bundling a
     * Jakarta-native JSTL is no longer a way to override the container. Driven against the real
     * {@code parseTld()} so the assertion tracks Tomcat rather than a restatement of it.
     */
    @Test
    public void containerAliasWinsOverAnApplicationBundledTld() throws Exception {
        givenJstlIsPopulated();
        aliasJakartaTagsUris(populatedJstlUrl);

        final TomEETldScanner scanner = new TomEETldScanner(mock(ServletContext.class), true, false, true);
        final Map<String, TldResourcePath> deployment = scanner.getUriTldResourcePathMap();
        deployment.putAll(uris);
        final TldResourcePath containerPath = deployment.get("jakarta.tags.core");

        // an application jar declaring the same URI, parsed the way scanJars() would
        final File appTld = new File("target/test-classes/jakarta-tags-core.tld");
        appTld.getParentFile().mkdirs();
        Files.write(appTld.toPath(), ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<taglib xmlns=\"https://jakarta.ee/xml/ns/jakartaee\" version=\"3.0\">"
            + "<tlib-version>3.0</tlib-version><short-name>c</short-name>"
            + "<uri>jakarta.tags.core</uri></taglib>").getBytes(StandardCharsets.UTF_8));
        parseTld(scanner, new TldResourcePath(appTld.toURI().toURL(), null));

        assertSame("an application TLD must not displace the container alias",
            containerPath, deployment.get("jakarta.tags.core"));
    }

    /**
     * The aliases are only useful if {@code scanPlatform()} copies them into the per-deployment map
     * Jasper actually resolves {@code <%@ taglib uri="..." %>} against. Asserts each alias arrives
     * there pointing at the same TLD resource as its legacy spelling.
     * <p>
     * Note this only covers the {@code shouldSkipJsf()} branch: that method returns true for every
     * {@code jakarta.faces.*} name unconditionally, so the myfaces-exclude branch below it is
     * currently unreachable and cannot be driven from a test.
     */
    @Test
    public void scanPlatformForwardsAliasesToTheDeploymentMap() throws Exception {
        givenJstlIsPopulated();

        aliasJakartaTagsUris(populatedJstlUrl);

        final Map<String, TldResourcePath> deployment = scanPlatform();

        for (final String[] alias : ALIASES) {
            assertTrue(alias[0] + " never reached the deployment map", deployment.containsKey(alias[0]));
            assertSame(alias[0] + " must resolve to the legacy TLD resource",
                uris.get(alias[1]), deployment.get(alias[0]));
        }
    }

    /**
     * Invokes the protected {@code scanPlatform()} and returns the per-deployment URI map it
     * populated -- that map is the one Jasper resolves {@code <%@ taglib uri="..." %>} against.
     */
    private Map<String, TldResourcePath> scanPlatform() throws Exception {
        final TomEETldScanner scanner = new TomEETldScanner(mock(ServletContext.class), false, false, false);

        final Method scanPlatform = TomEETldScanner.class.getDeclaredMethod("scanPlatform");
        scanPlatform.setAccessible(true);
        scanPlatform.invoke(scanner);

        return scanner.getUriTldResourcePathMap();
    }

    /**
     * Simulates {@code populateMyfacesAndJstlData()} having run against the shaded taglibs jar:
     * every legacy URI mapped to a resource path, and the permittedTaglibs validator configured as
     * that jar's TLD configures it.
     */
    private void givenJstlIsPopulated() throws Exception {
        final URL jar = jstlUrl();
        populatedJstlUrl = jar;
        for (final String[] alias : ALIASES) {
            uris.put(alias[1], new TldResourcePath(jar, null, "META-INF/fake.tld"));
        }
        for (final String uri : NOT_RENAMED) {
            uris.put(uri, new TldResourcePath(jar, null, "META-INF/fake.tld"));
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

    /**
     * A distribution carrying myfaces but no JSTL: the pre-populated map is non-empty, but none of the
     * legacy JSTL URIs are in it.
     */
    private void givenMyfacesIsPopulatedWithoutJstl() throws Exception {
        uris.put("http://java.sun.com/jsf/html",
            new TldResourcePath(new URL("file:/fake/myfaces-impl.jar"), null, "META-INF/myfaces_html.tld"));
    }

    /**
     * Collects warnings the scanner emits for the rest of the test; the handler is detached in
     * {@link #restoreStaticState()}.
     */
    private List<String> captureWarnings() {
        final List<String> warnings = new ArrayList<>();
        logHandler = new Handler() {
            @Override
            public void publish(final LogRecord record) {
                if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                    warnings.add(record.getMessage());
                }
            }

            @Override
            public void flush() {
                // no-op
            }

            @Override
            public void close() {
                // no-op
            }
        };
        LOGGER.addHandler(logHandler);
        return warnings;
    }

    private ValidatorXml validator() {
        return taglibs.get(uris.get(PERMITTED_TAGLIBS_URI)).getValidator();
    }

    private List<String> permittedTaglibs() {
        // read back the way PermittedTaglibsTLV does, i.e. on any whitespace
        return Arrays.asList(validator().getInitParams().get("permittedTaglibs").trim().split("\\s+"));
    }

    /**
     * The URL the pre-populated JSTL entries carry. Uses the real {@code JSTL_URL} when the shaded
     * taglibs jar happens to be resolvable, so the identity comparison in {@code scanPlatform()} is
     * exercised against the production value rather than a stand-in.
     */
    private URL jstlUrl() throws Exception {
        final URL real = staticUrl("JSTL_URL");
        return real != null ? real : new URL("file:/fake/taglibs-shade.jar");
    }

    private static URL staticUrl(final String name) throws Exception {
        final Field field = TomEETldScanner.class.getDeclaredField(name);
        field.setAccessible(true);
        return (URL) field.get(null);
    }

    @SuppressWarnings("unchecked")
    private static <K, V> Map<K, V> staticMap(final String name) throws Exception {
        final Field field = TomEETldScanner.class.getDeclaredField(name);
        field.setAccessible(true);
        return (Map<K, V>) field.get(null);
    }

    private static void parseTld(final TomEETldScanner scanner, final TldResourcePath path) throws Exception {
        final Method method = TldScanner.class.getDeclaredMethod("parseTld", TldResourcePath.class);
        method.setAccessible(true);
        method.invoke(scanner, path);
    }

    private static void aliasJakartaTagsUris() throws Exception {
        final Method method = TomEETldScanner.class.getDeclaredMethod("aliasJakartaTagsUris");
        method.setAccessible(true);
        method.invoke(null);
    }

    /**
     * Drives the JSTL-presence guard, which cannot be exercised through the no-arg overload:
     * {@code JSTL_URL} is {@code static final} and modern JDKs reject reflective writes to it.
     */
    private static void aliasJakartaTagsUris(final URL jstlUrl) throws Exception {
        final Method method = TomEETldScanner.class.getDeclaredMethod("aliasJakartaTagsUris", URL.class);
        method.setAccessible(true);
        method.invoke(null, jstlUrl);
    }
}
