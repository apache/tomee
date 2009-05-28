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
package org.apache.openejb.loader;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import java.util.concurrent.atomic.AtomicReference;

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
 *
 * @version $Rev$ $Date$
 */
public class Options {

    private final Options parent;
    private final Properties properties;

    public Options(Properties properties) {
        this(properties, new NullOptions());
    }

    public Options(Properties properties, Options parent) {
        this.parent = parent;
        this.properties = properties;
    }

    public void setLogger(Log logger) {
        parent.setLogger(logger);
    }

    public Log getLogger() {
        return parent.getLogger();
    }

    public boolean has(String property) {
        return properties.containsKey(property) || parent.has(property);
    }

    public String get(String property, String defaultValue) {
        String value = properties.getProperty(property);

        return value != null ? log(property, value) : parent.get(property, defaultValue);
    }

    public int get(String property, int defaultValue) {
        String value = properties.getProperty(property);

        if (value == null) return parent.get(property, defaultValue);

        try {
            return log(property, Integer.parseInt(value));
        } catch (NumberFormatException e) {
            warn(property, value, e);
            return parent.get(property, defaultValue);
        }
    }

    public long get(String property, long defaultValue) {
        String value = properties.getProperty(property);

        if (value == null) return parent.get(property, defaultValue);

        try {
            return log(property, Long.parseLong(value));
        } catch (NumberFormatException e) {
            warn(property, value, e);
            return parent.get(property, defaultValue);
        }
    }

    public boolean get(String property, boolean defaultValue) {
        String value = properties.getProperty(property);

        if (value == null) return parent.get(property, defaultValue);

        try {
            return log(property, Boolean.parseBoolean(value));
        } catch (NumberFormatException e) {
            warn(property, value, e);
            return parent.get(property, defaultValue);
        }
    }

    public Class get(String property, Class defaultValue) {
        String className = properties.getProperty(property);

        if (className == null) return parent.get(property, defaultValue);

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            return log(property, classLoader.loadClass(className));
        } catch (Exception e) {
            getLogger().warning("Could not load " + property + " : " + className, e);
            return parent.get(property, defaultValue);
        }
    }

    public <T extends Enum<T>> T get(String property, T defaultValue) {
        String value = properties.getProperty(property);

        if (value == null) return parent.get(property, defaultValue);

        if (defaultValue == null) throw new IllegalArgumentException("Must supply a default for property " + property);

        Class<T> enumType = (Class<T>) defaultValue.getClass();

        try {
            return log(property, valueOf(enumType, value.toUpperCase()));
        } catch (IllegalArgumentException e) {
            warn(property, value);
            return parent.get(property, defaultValue);
        }
    }

    public <T extends Enum<T>> Set<T> getAll(String property, T... defaultValue) {
        EnumSet<T> defaults = EnumSet.copyOf(Arrays.asList(defaultValue));
        return get(property, defaults);
    }

    public <T extends Enum<T>> Set<T> get(String property, Set<T> defaultValue) {

        String value = properties.getProperty(property);

        if (value == null) return parent.get(property, (Set) defaultValue);

        Class<T> enumType;
        try {
            T t = defaultValue.iterator().next();
            enumType = (Class<T>) t.getClass();
        } catch (Exception e) {
            throw new IllegalArgumentException("Must supply a default for property " + property);
        }

        try {
            String[] values = value.split(",");
            EnumSet<T> set = EnumSet.noneOf(enumType);

            for (String s : values) {
                s = s.trim();
                set.add(valueOf(enumType, s.toUpperCase()));
            }
            return set;
        } catch (IllegalArgumentException e) {
            warn(property, value);
            return parent.get(property, (Set) defaultValue);
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
    public static <T extends Enum<T>> T valueOf(Class<T> enumType, String name) {
        Map<String, T> map = new HashMap<String, T>();
        for (T t : enumType.getEnumConstants()) {
            map.put(t.name().toUpperCase(), t);
        }

        T value = map.get(name.toUpperCase());

        // Call Enum.valueOf for the clean exception
        if (value == null) Enum.valueOf(enumType, name);

        return value;
    }

    private void warn(String property, String value) {
        getLogger().warning("Cannot parse supplied value \"" + value + "\" for option \"" + property + "\"");
    }

    private void warn(String property, String value, Exception e) {
        getLogger().warning("Cannot parse supplied value \"" + value + "\" for option \"" + property + "\"", e);
    }

    private <V> V log(String property, V value) {
        if (value instanceof Class) {
            Class clazz = (Class) value;
            getLogger().info("Using \'" + property + "=" + clazz.getName() + "\'");
        } else {
            getLogger().info("Using \'" + property + "=" + value + "\'");
        }
        return value;
    }


    protected static <T extends Enum<T>> String[] lowercase(T... items) {
        String[] values = new String[items.length];
        for (int i = 0; i < items.length; i++) {
            values[i] = items[i].name().toLowerCase();
        }
        return values;
    }

    protected static <T extends Enum<T>> String[] lowercase(Collection<T> items) {
        String[] values = new String[items.size()];
        int i = 0;
        for (T item : items) {
            values[i++] = item.name().toLowerCase();
        }
        return values;
    }

    protected static <V extends Enum<V>> String possibleValues(V v) {
        Class<? extends Enum> enumType = v.getClass();
        return possibleValues(enumType);
    }

    protected static String possibleValues(Class<? extends Enum> enumType) {
        return join(", ", lowercase(enumType.getEnumConstants()));
    }


    public static String join(String delimiter, Object... collection) {
        StringBuilder sb = new StringBuilder();
        for (Object obj : collection) {
            sb.append(obj).append(delimiter);
        }
        if (collection.length > 0) sb.delete(sb.length() - delimiter.length(), sb.length());
        return sb.toString();
    }

    private final static class NullOptions extends Options {

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
        public void setLogger(Log logger) {
            this.logger = logger;
        }

        @Override
        public boolean has(String property) {
            return false;
        }

        @Override
        public int get(String property, int defaultValue) {
            return log(property, defaultValue);
        }

        @Override
        public long get(String property, long defaultValue) {
            return log(property, defaultValue);
        }

        @Override
        public boolean get(String property, boolean defaultValue) {
            return log(property, defaultValue);
        }

        @Override
        public <T extends Enum<T>> T get(String property, T defaultValue) {
            return log(property, defaultValue);
        }

        @Override
        public <T extends Enum<T>> Set<T> getAll(String property, T... defaultValue) {
            return EnumSet.copyOf(Arrays.asList(defaultValue));
        }

        @Override
        public <T extends Enum<T>> Set<T> get(String property, Set<T> defaults) {
            if (getLogger().isDebugEnabled()) {
                Iterator<T> iterator = defaults.iterator();
                String possibleValues = "";
                if (iterator.hasNext()) {
                    T v = iterator.next();
                    possibleValues = "  Possible values are: " + possibleValues(v);
                }

                String defaultValues = join(", ", lowercase(defaults));

                getLogger().debug("Using default \'" + property + "=" + defaultValues + "\'" + possibleValues);
            }

            return defaults;
        }

        @Override
        public String get(String property, String defaultValue) {
            return log(property, defaultValue);
        }

        @Override
        public Class get(String property, Class defaultValue) {
            return log(property, defaultValue);
        }

        private <V> V log(String property, V value) {
            if (getLogger().isDebugEnabled()) {
                if (value instanceof Enum) {
                    Enum anEnum = (Enum) value;
                    getLogger().debug("Using default \'" + property + "=" + anEnum.name().toLowerCase() + "\'.  Possible values are: " + possibleValues(anEnum));
                } else if (value instanceof Class) {
                    Class clazz = (Class) value;
                    getLogger().debug("Using default \'" + property + "=" + clazz.getName() + "\'");
                } else if (value != null) {
                    logger.debug("Using default \'" + property + "=" + value + "\'");
                }
            }
            return value;
        }
    }

    public static interface Log {
        public boolean isDebugEnabled();

        public boolean isInfoEnabled();

        public boolean isWarningEnabled();

        public void warning(String message, Throwable t);

        public void warning(String message);

        public void debug(String message, Throwable t);

        public void debug(String message);

        public void info(String message, Throwable t);

        public void info(String message);
    }

    public static class NullLog implements Log {
        public boolean isDebugEnabled() {
            return false;
        }

        public boolean isInfoEnabled() {
            return false;
        }

        public boolean isWarningEnabled() {
            return false;
        }

        public void warning(String message, Throwable t) {
        }

        public void warning(String message) {
        }

        public void debug(String message, Throwable t) {
        }

        public void debug(String message) {
        }

        public void info(String message, Throwable t) {
        }

        public void info(String message) {
        }
    }
}
