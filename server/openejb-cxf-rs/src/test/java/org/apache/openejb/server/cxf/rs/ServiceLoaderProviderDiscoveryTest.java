/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.openejb.server.cxf.rs;

import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * REST 3.1 requires {@link Feature} and {@link DynamicFeature} implementations declared through
 * META-INF/services to be discovered with the JDK ServiceLoader at deploy time. CXF does not do
 * this on its own, so TomEE performs the lookup while assembling the provider list.
 *
 * The services descriptors are written to a throwaway directory and read through a dedicated
 * classloader, so the discovery is exercised without registering these features into the other
 * tests of this module.
 *
 * @see <a href="https://issues.apache.org/jira/browse/TOMEE-4643">TOMEE-4643</a>
 */
public class ServiceLoaderProviderDiscoveryTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private ClassLoader classLoaderDeclaring(final Class<?>... services) throws Exception {
        final Map<Class<?>, List<String>> perSpi = new LinkedHashMap<>();
        for (final Class<?> service : services) {
            final Class<?> spi = Feature.class.isAssignableFrom(service) ? Feature.class : DynamicFeature.class;
            perSpi.computeIfAbsent(spi, k -> new ArrayList<>()).add(service.getName());
        }
        return classLoaderDeclaringNames(perSpi);
    }

    /**
     * Writes one descriptor per SPI, listing every entry given for it. Entries do not have to
     * resolve to a real class - that is how the malformed-descriptor cases are set up.
     */
    private ClassLoader classLoaderDeclaringNames(final Map<Class<?>, List<String>> perSpi) throws Exception {
        final File classes = temporaryFolder.newFolder();
        final File descriptors = new File(classes, "META-INF/services");
        assertTrue(descriptors.mkdirs());

        for (final Map.Entry<Class<?>, List<String>> entry : perSpi.entrySet()) {
            Files.write(new File(descriptors, entry.getKey().getName()).toPath(),
                    String.join("\n", entry.getValue()).getBytes(StandardCharsets.UTF_8));
        }

        return new URLClassLoader(new URL[]{classes.toURI().toURL()},
                ServiceLoaderProviderDiscoveryTest.class.getClassLoader());
    }

    @Test
    public void featureIsDiscovered() throws Exception {
        final List<Object> providers = new ArrayList<>();
        CxfRsHttpListener.addServiceLoaderProviders(providers, classLoaderDeclaring(AFeature.class));

        assertEquals(1, providers.size());
        assertTrue(providers.get(0) instanceof AFeature);
    }

    @Test
    public void dynamicFeatureIsDiscovered() throws Exception {
        final List<Object> providers = new ArrayList<>();
        CxfRsHttpListener.addServiceLoaderProviders(providers, classLoaderDeclaring(ADynamicFeature.class));

        assertEquals(1, providers.size());
        assertTrue(providers.get(0) instanceof ADynamicFeature);
    }

    @Test
    public void bothKindsAreDiscovered() throws Exception {
        final List<Object> providers = new ArrayList<>();
        CxfRsHttpListener.addServiceLoaderProviders(providers,
                classLoaderDeclaring(AFeature.class, ADynamicFeature.class));

        assertEquals(2, providers.size());
    }

    @Test
    public void alreadyRegisteredProvidersAreNotDuplicated() throws Exception {
        final List<Object> providers = new ArrayList<>();
        providers.add(new AFeature());
        CxfRsHttpListener.addServiceLoaderProviders(providers, classLoaderDeclaring(AFeature.class));

        assertEquals(1, providers.size());
    }

    @Test
    public void noDescriptorAddsNothing() throws Exception {
        final List<Object> providers = new ArrayList<>();
        CxfRsHttpListener.addServiceLoaderProviders(providers, classLoaderDeclaring());

        assertTrue(providers.isEmpty());
    }

    /**
     * A single unusable entry must not discard the remaining providers declared for the same SPI.
     */
    @Test
    public void brokenEntryDoesNotDiscardTheOthers() throws Exception {
        final List<Object> providers = new ArrayList<>();
        CxfRsHttpListener.addServiceLoaderProviders(providers, classLoaderDeclaringNames(
                singletonMap(Feature.class, asList(
                        "does.not.Exist",
                        ThrowingFeature.class.getName(),
                        AFeature.class.getName()))));

        assertEquals(1, providers.size());
        assertTrue(providers.get(0) instanceof AFeature);
    }

    /**
     * The spec disables service loading when the Application maps jakarta.ws.rs.loadServices
     * to Boolean.FALSE.
     */
    @Test
    public void serviceLoadingCanBeDisabledByTheApplication() {
        assertFalse(CxfRsHttpListener.isServiceLoadingEnabled(
                new ConfigurableApplication(singletonMap("jakarta.ws.rs.loadServices", Boolean.FALSE))));
    }

    @Test
    public void serviceLoadingIsEnabledByDefault() {
        assertTrue(CxfRsHttpListener.isServiceLoadingEnabled(new Application()));
        assertTrue(CxfRsHttpListener.isServiceLoadingEnabled(null));
        assertTrue(CxfRsHttpListener.isServiceLoadingEnabled(
                new ConfigurableApplication(singletonMap("jakarta.ws.rs.loadServices", Boolean.TRUE))));
        assertTrue(CxfRsHttpListener.isServiceLoadingEnabled(
                new ConfigurableApplication(emptyMap())));
    }

    public static class ConfigurableApplication extends Application {
        private final Map<String, Object> properties;

        public ConfigurableApplication(final Map<String, Object> properties) {
            this.properties = properties;
        }

        @Override
        public Map<String, Object> getProperties() {
            return properties;
        }
    }

    public static class ThrowingFeature implements Feature {
        public ThrowingFeature() {
            throw new IllegalStateException("should be skipped, not fatal");
        }

        @Override
        public boolean configure(final FeatureContext context) {
            return true;
        }
    }

    public static class AFeature implements Feature {
        @Override
        public boolean configure(final FeatureContext context) {
            return true;
        }
    }

    public static class ADynamicFeature implements DynamicFeature {
        @Override
        public void configure(final ResourceInfo resourceInfo, final FeatureContext context) {
            // no-op
        }
    }
}
