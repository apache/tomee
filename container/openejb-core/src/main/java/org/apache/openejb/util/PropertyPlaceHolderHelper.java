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

package org.apache.openejb.util;

import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.openejb.cipher.PasswordCipher;
import org.apache.openejb.cipher.PasswordCipherException;
import org.apache.openejb.cipher.PasswordCipherFactory;
import org.apache.openejb.loader.SystemInstance;

import java.util.Map;
import java.util.Properties;

public final class PropertyPlaceHolderHelper {
    private static final String PREFIX = "${";
    private static final String SUFFIX = "}";
    private static final Properties CACHE = new Properties();

    private static final PropertiesLookup RESOLVER = new PropertiesLookup();
    public static final StrSubstitutor SUBSTITUTOR = new StrSubstitutor(RESOLVER);
    static {
        SUBSTITUTOR.setEnableSubstitutionInVariables(true);
        SUBSTITUTOR.setValueDelimiter(System.getProperty("openejb.placehodler.delimiter", ":-")); // default one of [lang3]
    }

    public static final String CIPHER_PREFIX = "cipher:";

    private PropertyPlaceHolderHelper() {
        // no-op
    }

    public static void reset() {
        CACHE.clear();
        RESOLVER.reload();
    }

    public static String simpleValue(final String raw) {
        if (raw == null || !raw.contains(PREFIX) || !raw.contains(SUFFIX)) {
            return raw;
        }

        String value = SUBSTITUTOR.replace(raw);
        if (!value.equals(raw) && value.startsWith("java:")) {
            value = value.substring(5);
        }

        final String replace = value.replace(PREFIX, "").replace(SUFFIX, "");
        if (replace.startsWith(CIPHER_PREFIX)) {
            final String algo = replace.substring(CIPHER_PREFIX.length(), replace.indexOf(':', CIPHER_PREFIX.length() + 1));
            PasswordCipher cipher = null;
            try {
                cipher = PasswordCipherFactory.getPasswordCipher(algo);
            } catch (final PasswordCipherException ex) {
                try {
                    cipher = PasswordCipher.class.cast(Thread.currentThread().getContextClassLoader().loadClass(algo).newInstance());
                } catch (final Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }
            return cipher.decrypt(replace.substring(CIPHER_PREFIX.length() + algo.length()).toCharArray());
        }
        return replace;
    }

    public static String value(final String aw) {
        if (aw == null || !aw.contains(PREFIX) || !aw.contains(SUFFIX)) {
            return aw;
        }

        String value = CACHE.getProperty(aw);
        if (value != null) {
            return value;
        }

        value = simpleValue(aw);
        CACHE.setProperty(aw, value);
        return value;
    }

    public static Properties holds(final Properties properties) {
        // we can put null values in SuperProperties, since properties is often of this type we need to tolerate it
        final Properties updated = new SuperProperties();
        if (properties == null) {
            return updated;
        }

        for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
            final Object rawValue = entry.getValue();
            if (rawValue instanceof String) {
                updated.put(entry.getKey(), value((String) rawValue));
            } else {
                updated.put(entry.getKey(), rawValue);
            }
        }
        return updated;
    }

    public static void holdsWithUpdate(final Properties props) {
        final Properties toUpdate = holds(props);
        props.putAll(toUpdate);
    }

    private static class PropertiesLookup extends StrLookup<Object> {
        private static Properties PROPERTIES = SystemInstance.get().getProperties();
        private static final Map<String, String> ENV = System.getenv();

        @Override
        public synchronized String lookup(final String key) {
            String value = PROPERTIES.getProperty(key);
            if (value != null) {
                return value;
            }

            value = ENV.get(key);
            if (value != null) {
                return value;
            }

            return null;
        }

        public synchronized void reload() {
            PROPERTIES = SystemInstance.get().getProperties();
        }
    }
}
