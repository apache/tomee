/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.properties.impl;

import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.openejb.api.resource.PropertiesResourceProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

public class CustomPropertiesProvider implements PropertiesResourceProvider {
    private static final Logger LOGGER = Logger.getLogger(CustomPropertiesProvider.class.getName());

    private Properties properties;

    /**
     * This method is called by TomEE to provide the properties already set on the resource.
     * This is optional, but allows us to do things like placeholder expansion.
     *
     * In this example, we'll substitute any properties in placeholders like $[property] with
     * properties from the custom properties file.
     *
     * @param properties properties from the resource as defined in tomee.xml
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * This demonstrates a custom properties provider for TomEE resources by reading properties from a different
     * properties file. However, the logic here can be customized to suit your needs.
     * @return The properties for the resource
     */
    @Override
    public Properties provides() {
        // read from a different file in the conf/ folder

        final String catalinaHome = System.getProperty("catalina.home"); // this is the TomEE root directory
        final File dbConfigFile = new File(catalinaHome, "conf/db-properties.conf"); // let's get these properties from conf/db-properties.conf

        try {
            final Properties customProperties = new Properties();
            customProperties.load(new FileInputStream(dbConfigFile));

            final StrSubstitutor substitutor = new StrSubstitutor(new Lookup(customProperties), "$[", "]", StrSubstitutor.DEFAULT_ESCAPE);
            final Properties result = new Properties();

            // replace any placeholders in the resource properties
            final Enumeration<?> enumeration = properties.propertyNames();
            while (enumeration.hasMoreElements()) {
                final String key = (String) enumeration.nextElement();
                final String value = properties.getProperty(key);
                final String newValue = (value == null ? null : substitutor.replace(value));

                result.setProperty(key, newValue);
            }

            return result;
        } catch (IOException e) {
            LOGGER.severe("Unable to read properties from " + dbConfigFile.getAbsolutePath() + ", continuing without placeholder substitution");
            e.printStackTrace();
        }

        return properties;
    }

    private static class Lookup extends StrLookup<Object> {
        private final Properties properties;

        public Lookup(Properties properties) {
            this.properties = properties;
        }


        @Override
        public String lookup(final String key) {
            return properties.getProperty(key);
        }
    }
}
