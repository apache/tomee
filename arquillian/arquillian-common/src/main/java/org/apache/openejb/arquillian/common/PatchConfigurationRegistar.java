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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.arquillian.common;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.impl.extension.ConfigurationRegistrar;
import org.jboss.arquillian.config.impl.extension.PropertiesParser;
import org.jboss.arquillian.config.impl.extension.StringPropertyReplacer;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.api.event.ManagerStarted;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

// see https://issues.jboss.org/browse/ARQ-700
// this class should disappear when ARQ-700 will be fixed (see comments)
public class PatchConfigurationRegistar {
    @Inject
    @ApplicationScoped
    private InstanceProducer<ArquillianDescriptor> descriptorInst;

    static ArquillianDescriptor resolveSystemProperties(final ArquillianDescriptor descriptor) throws IllegalArgumentException {
        final String descrStr = descriptor.exportAsString();
        return Descriptors.importAs(ArquillianDescriptor.class).fromString(StringPropertyReplacer.replaceProperties(descrStr));
    }

    static Properties loadArquillianProperties(String propertyName, String defaultName) {
        final Properties props = new Properties();
        props.putAll(System.getProperties());

        FileName resourceName = getConfigFileName(propertyName, defaultName);
        InputStream input = loadResource(resourceName);
        if (input != null) {
            try {
                props.load(input);
            } catch (final IOException e) {
                throw new RuntimeException("Could not load Arquillian properties file, " + resourceName.getName(), e);
            }
        }
        return props;
    }

    static InputStream loadArquillianXml(String propertyName, String defaultName) {
        FileName resourceName = getConfigFileName(propertyName, defaultName);
        return loadResource(resourceName);
    }

    static InputStream loadResource(FileName resourceName) {
        InputStream stream = loadClassPathResource(resourceName.getName());
        if (stream == null) {
            stream = loadFileResource(resourceName.getName());
        }
        // only throw Exception if configured (non default) could not be found
        if (stream == null && !resourceName.isDefault()) {
            throw new IllegalArgumentException("Could not find configured filename as either classpath resource nor file resource: " + resourceName.getName());
        }
        return stream;
    }

    static InputStream loadFileResource(String resourceName) {
        final File file = new File(resourceName);
        if (file.exists()) {
            try {
                return new FileInputStream(file);
            } catch (final FileNotFoundException e) {
                // should not happen unless file has been deleted since we did file.exists call
                throw new IllegalArgumentException("Configuration file could not be found, " + resourceName);
            }
        }
        return null;
    }

    static InputStream loadClassPathResource(String resourceName) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader.getResourceAsStream(resourceName);
    }

    static FileName getConfigFileName(String propertyName, String defaultName) {
        String name = System.getProperty(propertyName);
        if (name == null) {
            return new FileName(defaultName, true);
        }
        return new FileName(name, false);
    }

    public void loadConfigurationWithoutTrimmingProperties(final @Observes(precedence = -1) ManagerStarted event) {
        final ArquillianDescriptor descriptor;

        final InputStream input = loadArquillianXml(ConfigurationRegistrar.ARQUILLIAN_XML_PROPERTY, ConfigurationRegistrar.ARQUILLIAN_XML_DEFAULT);
        if (input != null) {
            descriptor = Descriptors.importAs(ArquillianDescriptor.class).fromStream(input);
        } else {
            descriptor = Descriptors.create(ArquillianDescriptor.class);
        }

        final ArquillianDescriptor resolvedDesc = resolveSystemProperties(descriptor);
        new PropertiesParser().addProperties(resolvedDesc, loadArquillianProperties(ConfigurationRegistrar.ARQUILLIAN_PROP_PROPERTY, ConfigurationRegistrar.ARQUILLIAN_PROP_DEFAULT));

        descriptorInst.set(resolvedDesc);
    }

    static class FileName {
        private String name;
        private boolean isDefault;

        public FileName(String name, boolean isDefault) {
            this.name = name;
            this.isDefault = isDefault;
        }

        public String getName() {
            return name;
        }

        public boolean isDefault() {
            return isDefault;
        }
    }
}
