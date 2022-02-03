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
import org.apache.openejb.cipher.SafePasswordCipher;
import org.apache.openejb.loader.SystemInstance;

import java.util.Map;
import java.util.Properties;

public final class PropertyPlaceHolderHelper {
    private static final String PREFIX = "${";
    private static final String SUFFIX = "}";
    private static final String VALUE_DELIMITER;

    private static final Properties CACHE = new Properties();

    private static final PropertiesLookup RESOLVER_TO_NULL_IF_MISSING = new PropertiesLookup(false);
    private static final PropertiesLookup RESOLVER_TO_KEY_IF_MISSING = new PropertiesLookup(true);

    private static final StrSubstitutor DEFAULT_SUBSTITUTOR = new StrSubstitutor(RESOLVER_TO_KEY_IF_MISSING);

    private static final StrSubstitutor VALUE_DELIMITER_SUBSTITUTOR = new StrSubstitutor(RESOLVER_TO_NULL_IF_MISSING);

    static {
        VALUE_DELIMITER = JavaSecurityManagers.getSystemProperty("openejb.placehodler.delimiter", ":-"); // default one of [lang3]

        DEFAULT_SUBSTITUTOR.setEnableSubstitutionInVariables(true);
        DEFAULT_SUBSTITUTOR.setValueDelimiter(VALUE_DELIMITER);

        VALUE_DELIMITER_SUBSTITUTOR.setPreserveEscapes(true);
        VALUE_DELIMITER_SUBSTITUTOR.setEnableSubstitutionInVariables(true);
        VALUE_DELIMITER_SUBSTITUTOR.setValueDelimiter(VALUE_DELIMITER);
    }

    private static final String CIPHER_PREFIX = "cipher:";
    private static final String JAVA_PREFIX = "java:";

    private PropertyPlaceHolderHelper() {
        // no-op
    }

    public static void reset() {
        CACHE.clear();
        RESOLVER_TO_NULL_IF_MISSING.reload();
        RESOLVER_TO_KEY_IF_MISSING.reload();
    }

    public static String simpleValue(final String raw) {
        return String.class.cast(simpleValueAsStringOrCharArray(raw, false));
    }

    public static Object simpleValueAsStringOrCharArray(final String raw) {
        return simpleValueAsStringOrCharArray(raw, true);
    }

    private static Object simpleValueAsStringOrCharArray(final String raw, boolean acceptCharArray) {
        if (raw == null) {
            return null;
        }
        if (!raw.contains(PREFIX) || !raw.contains(SUFFIX)) {
            return decryptIfNeeded(raw, acceptCharArray);
        }

        String value = replace(raw);

        if (!value.equals(raw) && value.startsWith(JAVA_PREFIX)) {
            value = value.substring(5);
        }

        return decryptIfNeeded(value, acceptCharArray);
    }

    private static Object decryptIfNeeded(final String replace, final boolean acceptCharArray) {
        if (replace.startsWith(CIPHER_PREFIX)) {
            final String algo = replace.substring(CIPHER_PREFIX.length(), replace.indexOf(':', CIPHER_PREFIX.length() + 1));
            PasswordCipher cipher;
            try {
                cipher = PasswordCipherFactory.getPasswordCipher(algo);
            } catch (final PasswordCipherException ex) {
                try {
                    cipher = PasswordCipher.class.cast(Thread.currentThread().getContextClassLoader().loadClass(algo).newInstance());
                } catch (final Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }

            final char[] input = replace.substring(CIPHER_PREFIX.length() + algo.length() + 1).toCharArray();
            return acceptCharArray && SafePasswordCipher.class.isInstance(cipher) ?
                SafePasswordCipher.class.cast(cipher).decryptAsCharArray(input) :
                cipher.decrypt(input);
        }
        return replace;
    }

    public static String value(final String raw) {
        if (raw == null) {
            return null;
        }
        if (!raw.contains(PREFIX) || !raw.contains(SUFFIX)) {
            return String.class.cast(decryptIfNeeded(raw, false));
        }

        String value = CACHE.getProperty(raw);
        if (value != null) {
            return value;
        }

        value = simpleValue(raw);
        CACHE.setProperty(raw, value);
        return value;
    }

    public static Properties simpleHolds(final Properties properties) {
        return holds(properties, false);
    }

    public static Properties holds(final Properties properties) {
        return holds(properties, true);
    }

    private static Properties holds(final Properties properties, final boolean cache) {
        // we can put null values in SuperProperties, since properties is often of this type we need to tolerate it
        final Properties updated = new SuperProperties();
        if (properties == null) {
            return updated;
        }

        for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
            final Object rawValue = entry.getValue();
            if (rawValue instanceof String) {
                final String value = (String) rawValue;
                updated.put(entry.getKey(), cache ? value(value) : simpleValueAsStringOrCharArray(value));
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

    public static String replace(final String raw) {
        if(raw == null) {
            return null;
        }

        if(raw.contains(VALUE_DELIMITER)) {
            return DEFAULT_SUBSTITUTOR.replace(VALUE_DELIMITER_SUBSTITUTOR.replace(raw));
        } else {
            return DEFAULT_SUBSTITUTOR.replace(raw);
        }
    }

    private static class PropertiesLookup extends StrLookup<Object> {
        private static final Map<String, String> ENV = System.getenv();

        private final boolean resolveToKey;

        public PropertiesLookup(boolean resolveToKey) {
            this.resolveToKey = resolveToKey;
        }

        @Override
        public synchronized String lookup(final String key) {
            String value = SystemInstance.get().getProperties().getProperty(key);
            if (value != null) {
                return value;
            }

            value = ENV.get(key);
            if (value != null) {
                return value;
            }

            return this.resolveToKey ? key : null;
        }

        public synchronized void reload() {
            //no-op
        }
    }

}
