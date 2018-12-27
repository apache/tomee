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
package org.apache.openejb.loader;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Collections;
import java.lang.reflect.Constructor;

/**
 * The purpose of this class is to provide a more strongly typed version of a
 * java.util.Properties object. So far it is a read only view of the properties
 * and does not set data into the underlying Properties instance.
 * <p/>
 * Similar to java.util.Properties it will delegate to a "parent" instance when
 * a property is not found.  If a property is found but its value cannot be parsed
 * as the desired data type, the parent's value is used.
 * <p/>
 * By default this object will log nothing, but if a Log implementation is set the
 * Options class will log three kinds of statements:
 * <p/>
 * - When a property is not found: the property name and default value in use along
 * with all possible values (enums only). Debug level.
 * - When a property is found: the property name and value.  Info level.
 * - When a property value cannot be parsed: the property name and invalid value. Warn level.
 * <p/>
 * Logging the user supplied values onto INFO is really nice as it shows up in the standard
 * log output and allows us to easily see which values the user has changed from the default.
 * It's rather impossible to diagnose issues without this information.
 * <p/>
 * ENUM SETS:
 * <p/>
 * Properties that accept a Set of enum values automatically accept ALL and NONE in
 * addition to the explicitly created enum items.
 * <p/>
 * Using ALL. This allows users to have an easy way to imply "all" without having to
 * hardcode an the entire list of enum items and protects against the case where that
 * list may grow in the future.
 * <p/>
 * Using NONE.  This allows users an alternative to using an empty string when explicitly
 * specifying that none of the options should be used.
 * <p/>
 * In the internal code, this allows us to have these concepts in all enum options
 * without us having to add NONE or ALL enum items explicitly which leads to strange code.
 * <p/>
 * Additionally TRUE is an alias for ALL and FALSE an alias for NONE.  This allows options
 * that used to support only true/false values to be further defined in the future without
 * breaking compatibility.
 *
 * @version $Rev$ $Date$
 */
public class Options {

    private final Options parent;
    private final TomEEPropertyAdapter properties;

    public Options(final Properties properties) {
        this(properties, new NullOptions());
    }

    public Options(final Properties properties, final Options parent) {
        this.parent = parent;
        this.properties = new TomEEPropertyAdapter(properties);
    }

    public Properties getProperties() {
        return properties.delegate;
    }

    public void setLogger(final Log logger) {
        parent.setLogger(logger);
    }

    public Log getLogger() {
        return parent.getLogger();
    }

    public boolean has(final String property) {
        return properties.containsKey(property) || parent.has(property);
    }

    public String get(final String property, final String defaultValue) {
        final String value = properties.getProperty(property);

        return value != null ? log(property, value) : parent.get(property, defaultValue);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(final String property, final T defaultValue) {
        if (defaultValue == null) {
            throw new NullPointerException("defaultValue");
        }

        final String value = properties.getProperty(property);

        if (value == null || value.isEmpty()) {
            return parent.get(property, defaultValue);
        }

        try {
            final Class<?> type = defaultValue.getClass();
            final Constructor<?> constructor = type.getConstructor(String.class);
            final T t = (T) constructor.newInstance(value);
            return log(property, t);
        } catch (final Exception e) {
            e.printStackTrace();
            warn(property, value, e);
            return parent.get(property, defaultValue);
        }
    }

    public int get(final String property, final int defaultValue) {
        final String value = properties.getProperty(property);

        if (value == null || value.isEmpty()) {
            return parent.get(property, defaultValue);
        }

        try {
            return log(property, Integer.parseInt(value));
        } catch (final NumberFormatException e) {
            warn(property, value, e);
            return parent.get(property, defaultValue);
        }
    }

    public long get(final String property, final long defaultValue) {
        final String value = properties.getProperty(property);

        if (value == null || value.isEmpty()) {
            return parent.get(property, defaultValue);
        }

        try {
            return log(property, Long.parseLong(value));
        } catch (final NumberFormatException e) {
            warn(property, value, e);
            return parent.get(property, defaultValue);
        }
    }

    public boolean get(final String property, final boolean defaultValue) {
        final String value = properties.getProperty(property);

        if (value == null || value.isEmpty()) {
            return parent.get(property, defaultValue);
        }

        try {
            return log(property, Boolean.parseBoolean(value));
        } catch (final NumberFormatException e) {
            warn(property, value, e);
            return parent.get(property, defaultValue);
        }
    }

    public Class<?> get(final String property, final Class<?> defaultValue) {
        final String className = properties.getProperty(property);

        if (className == null) {
            return parent.get(property, defaultValue);
        }

        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            return log(property, classLoader.loadClass(className));
        } catch (final Exception e) {
            getLogger().warning("Could not load " + property + " : " + className, e);
            return parent.get(property, defaultValue);
        }
    }

    public <T extends Enum<T>> T get(final String property, final T defaultValue) {
        final String value = properties.getProperty(property);

        if (value == null || value.isEmpty()) {
            return parent.get(property, defaultValue);
        }

        if (defaultValue == null) {
            throw new IllegalArgumentException("Must supply a default for property " + property);
        }

        final Class<T> enumType = (Class<T>) defaultValue.getClass();

        try {
            return log(property, valueOf(enumType, value.toUpperCase()));
        } catch (final IllegalArgumentException e) {
            warn(property, value);
            return parent.get(property, defaultValue);
        }
    }

    public <T extends Enum<T>> Set<T> getAll(final String property, final T... defaultValue) {
        final EnumSet<T> defaults = EnumSet.copyOf(Arrays.asList(defaultValue));
        return getAll(property, defaults);
    }

    public <T extends Enum<T>> Set<T> getAll(final String property, final Set<T> defaultValue) {
        final Class<T> enumType;
        try {
            final T t = defaultValue.iterator().next();
            enumType = (Class<T>) t.getClass();
        } catch (final Exception e) {
            throw new IllegalArgumentException("Must supply a default for property " + property);
        }

        return getAll(property, defaultValue, enumType);
    }

    public <T extends Enum<T>> Set<T> getAll(final String property, final Class<T> enumType) {
        return getAll(property, Collections.EMPTY_SET, enumType);
    }

    protected <T extends Enum<T>> Set<T> getAll(final String property, final Set<T> defaultValue, final Class<T> enumType) {
        final String value = properties.getProperty(property);

        if (value == null || value.isEmpty()) {
            return parent.getAll(property, defaultValue, enumType);
        }

        // Shorthand for specifying ALL or NONE for any option
        // that allows for multiple values of the enum
        if ("all".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value)) {
            log(property, value);
            return EnumSet.allOf(enumType);
        } else if ("none".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            log(property, value);
            return EnumSet.noneOf(enumType);
        }

        try {
            final String[] values = value.split(",");
            final EnumSet<T> set = EnumSet.noneOf(enumType);

            for (String s : values) {
                s = s.trim();
                set.add(valueOf(enumType, s.toUpperCase()));
            }
            return logAll(property, set);
        } catch (final IllegalArgumentException e) {
            warn(property, value);
            return parent.getAll(property, defaultValue, enumType);
        }
    }

    /**
     * Use this instead of Enum.valueOf() when you want to ensure that the
     * the enum values are case insensitive.
     *
     * @param enumType
     * @param name
     * @param <T>
     * @return
     */
    public static <T extends Enum<T>> T valueOf(final Class<T> enumType, final String name) {
        final Map<String, T> map = new HashMap<>();
        for (final T t : enumType.getEnumConstants()) {
            map.put(t.name().toUpperCase(), t);
        }

        final T value = map.get(name.toUpperCase());

        // Call Enum.valueOf for the clean exception
        if (value == null || value.equals("")) {
            Enum.valueOf(enumType, name);
        }

        return value;
    }

    private void warn(final String property, final String value) {
        getLogger().warning("Cannot parse supplied value \"" + value + "\" for option \"" + property + "\"");
    }

    private void warn(final String property, final String value, final Exception e) {
        getLogger().warning("Cannot parse supplied value \"" + value + "\" for option \"" + property + "\"", e);
    }

    private <V> V log(final String property, final V value) {
        if (!getLogger().isInfoEnabled()) {
            return value;
        }

        if (value instanceof Class) {
            final Class clazz = (Class) value;
            getLogger().info("Using \'" + property + "=" + clazz.getName() + "\'");
        } else {
            getLogger().info("Using \'" + property + "=" + value + "\'");
        }
        return value;
    }

    public <T extends Enum<T>> Set<T> logAll(final String property, final Set<T> value) {
        if (!getLogger().isInfoEnabled()) {
            return value;
        }

        getLogger().info("Using \'" + property + "=" + join(", ", lowercase(value)) + "\'");

        return value;
    }

    protected static <T extends Enum<T>> String[] lowercase(final T... items) {
        final String[] values = new String[items.length];
        for (int i = 0; i < items.length; i++) {
            values[i] = items[i].name().toLowerCase();
        }
        return values;
    }

    protected static <T extends Enum<T>> String[] lowercase(final Collection<T> items) {
        final String[] values = new String[items.size()];
        int i = 0;
        for (final T item : items) {
            values[i++] = item.name().toLowerCase();
        }
        return values;
    }

    protected static <V extends Enum<V>> String possibleValues(final V v) {
        final Class<? extends Enum> enumType = v.getClass();
        return possibleValues(enumType);
    }

    protected static String possibleValues(final Class<? extends Enum> enumType) {
        return join(", ", lowercase(enumType.getEnumConstants()));
    }

    public static String join(final String delimiter, final Object... collection) {
        final StringBuilder sb = new StringBuilder();
        for (final Object obj : collection) {
            sb.append(obj).append(delimiter);
        }
        if (collection.length > 0) {
            sb.delete(sb.length() - delimiter.length(), sb.length());
        }
        return sb.toString();
    }

    public static final class NullOptions extends Options {

        private Log logger;

        public NullOptions() {
            super(null, null);
            this.logger = new NullLog();
        }

        @Override
        public Log getLogger() {
            return logger;
        }

        @Override
        public void setLogger(final Log logger) {
            this.logger = logger;
        }

        @Override
        public boolean has(final String property) {
            return false;
        }

        @Override
        public <T> T get(final String property, final T defaultValue) {
            return log(property, defaultValue);
        }

        @Override
        public int get(final String property, final int defaultValue) {
            return log(property, defaultValue);
        }

        @Override
        public long get(final String property, final long defaultValue) {
            return log(property, defaultValue);
        }

        @Override
        public boolean get(final String property, final boolean defaultValue) {
            return log(property, defaultValue);
        }

        @Override
        public <T extends Enum<T>> T get(final String property, final T defaultValue) {
            return log(property, defaultValue);
        }

        @Override
        public <T extends Enum<T>> Set<T> getAll(final String property, final T... defaultValue) {
            return EnumSet.copyOf(Arrays.asList(defaultValue));
        }

        @Override
        protected <T extends Enum<T>> Set<T> getAll(final String property, final Set<T> defaults, final Class<T> enumType) {
            if (getLogger().isDebugEnabled()) {
                String possibleValues = "  Possible values are: " + possibleValues(enumType);

                possibleValues += " or NONE or ALL";

                final String defaultValues;

                if (defaults.isEmpty()) {
                    defaultValues = "NONE";
                } else if (defaults.size() == enumType.getEnumConstants().length) {
                    defaultValues = "ALL";
                } else {
                    defaultValues = join(", ", lowercase(defaults));
                }

                getLogger().debug("Using default \'" + property + "=" + defaultValues + "\'" + possibleValues);
            }

            return defaults;
        }

        @Override
        public String get(final String property, final String defaultValue) {
            return log(property, defaultValue);
        }

        @Override
        public Class<?> get(final String property, final Class<?> defaultValue) {
            return log(property, defaultValue);
        }

        private <V> V log(final String property, final V value) {
            if (getLogger().isDebugEnabled()) {
                if (value instanceof Enum) {
                    final Enum anEnum = (Enum) value;
                    getLogger().debug("Using default \'" + property + "=" + anEnum.name().toLowerCase() + "\'.  Possible values are: " + possibleValues(anEnum));
                } else if (value instanceof Class) {
                    final Class clazz = (Class) value;
                    getLogger().debug("Using default \'" + property + "=" + clazz.getName() + "\'");
                } else if (value != null) {
                    logger.debug("Using default \'" + property + "=" + value + "\'");
                }
            }
            return value;
        }
    }

    /**
     * This adapter aims at supporting all configuration properties with tomee. prefix instead of openejb. prefix
     */
    public static class TomEEPropertyAdapter {
        public static final int OPENEJB_PREFIX_LENGHT = "openejb.".length();
        private final Properties delegate;

        public TomEEPropertyAdapter(final Properties properties) {
            this.delegate = properties;
        }

        public String getProperty(final String key) {
            String value = delegate.getProperty(key);

            if (value == null && key.startsWith("openejb.")) {
                value = delegate.getProperty(getTomeeKey(key), value);
            }

            return value;
        }

        public boolean containsKey(final String key) {
            return delegate.containsKey(key)
                || delegate.containsKey(getTomeeKey(key));
        }

        private String getTomeeKey(final String key) {
            return "tomee." + key.substring(OPENEJB_PREFIX_LENGHT);
        }
    }

    public interface Log {
        boolean isDebugEnabled();

        boolean isInfoEnabled();

        boolean isWarningEnabled();

        void warning(String message, Throwable t);

        void warning(String message);

        void debug(String message, Throwable t);

        void debug(String message);

        void info(String message, Throwable t);

        void info(String message);
    }

    public static class NullLog implements Log {

        @Override
        public boolean isDebugEnabled() {
            return false;
        }

        @Override
        public boolean isInfoEnabled() {
            return false;
        }

        @Override
        public boolean isWarningEnabled() {
            return false;
        }

        @Override
        public void warning(final String message, final Throwable t) {
        }

        @Override
        public void warning(final String message) {
        }

        @Override
        public void debug(final String message, final Throwable t) {
        }

        @Override
        public void debug(final String message) {
        }

        @Override
        public void info(final String message, final Throwable t) {
        }

        @Override
        public void info(final String message) {
        }
    }
}
