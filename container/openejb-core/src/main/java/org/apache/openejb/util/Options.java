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
package org.apache.openejb.util;

import static org.apache.openejb.util.Join.join;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class Options {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, Options.class);

    public static int getInt(Properties p, String property, int defaultValue) {
        String value = p.getProperty(property);
        try {
            if (value != null) return Integer.parseInt(value);
            else return defaultValue;
        } catch (NumberFormatException e) {
            warn(property, value, defaultValue, e);
            return defaultValue;
        }
    }

    public static long getLong(Properties p, String property, long defaultValue) {
        String value = p.getProperty(property);
        try {
            if (value != null) return Long.parseLong(value);
            else return defaultValue;
        } catch (NumberFormatException e) {
            warn(property, value, defaultValue, e);
            return defaultValue;
        }
    }

    public static boolean getBoolean(Properties p, String property, boolean defaultValue) {
        String value = p.getProperty(property);
        try {
            if (value != null) return Boolean.parseBoolean(value);
            else return defaultValue;
        } catch (NumberFormatException e) {
            warn(property, value, defaultValue, e);
            return defaultValue;
        }
    }

    public static <T extends Enum<T>> T getEnum(Properties p, String property, T defaultValue) {
        String value = p.getProperty(property);
        if (value == null) return defaultValue;

        if (defaultValue == null) throw new IllegalArgumentException("Must supply a default for property " + property);

        Class<T> enumType = (Class<T>) defaultValue.getClass();

        try {
            return valueOf(enumType, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            warn(property, value, enumType, defaultValue);
            return defaultValue;
        }
    }

    public static <T extends Enum<T>> Set<T> getEnums(Properties p, String property, T... defaultValue) {
        String value = p.getProperty(property);

        if (value == null) return EnumSet.copyOf(Arrays.asList(defaultValue));

        Class<T> enumType;
        try {
            T t = defaultValue[0];
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
            warn(property, value, enumType, defaultValue);
            return EnumSet.copyOf(Arrays.asList(defaultValue));
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

    private static void warn(String property, String value, Class<? extends Enum> enumType, Enum... defaults) {
        String defaultValues = join(", ", lowercase(defaults));
        String possibleValues = join(", ", lowercase(enumType.getEnumConstants()));
        logger.warning("Cannot parse supplied value \"" + value + "\" for option \"" + property + "\".  Using default of \"" + defaultValues + "\".  Possible values are: " + possibleValues);
    }

    private static void warn(String property, String value, Object defaultValue, Exception e) {
        logger.warning("Cannot parse supplied value \"" + value + "\" for option \"" + property + "\".  Using default of \"" + defaultValue + "\"", e);
    }

    private static <T extends Enum<T>> String[] lowercase(T... items) {
        String[] values = new String[items.length];
        for (int i = 0; i < items.length; i++) {
            values[i] = items[i].name().toLowerCase();
        }
        return values;
    }

}
