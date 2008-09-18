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

import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class Options {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, Options.class);

    public static int getInt(Properties p, String property, int defaultValue){
        String value = p.getProperty(property);
        try {
            if (value != null) return Integer.parseInt(value);
            else return defaultValue;
        } catch (NumberFormatException e) {
            warn(property, value, defaultValue, e);
            return defaultValue;
        }
    }

    public static long getLong(Properties p, String property, long defaultValue){
        String value = p.getProperty(property);
        try {
            if (value != null) return Long.parseLong(value);
            else return defaultValue;
        } catch (NumberFormatException e) {
            warn(property, value, defaultValue, e);
            return defaultValue;
        }
    }

    public static boolean getBoolean(Properties p, String property, boolean defaultValue){
        String value = p.getProperty(property);
        try {
            if (value != null) return Boolean.parseBoolean(value);
            else return defaultValue;
        } catch (NumberFormatException e) {
            warn(property, value, defaultValue, e);
            return defaultValue;
        }
    }

    public static <T extends Enum<T>> T getEnum(Properties p, String property, T defaultValue){
        String value = p.getProperty(property);
        try {
            if (value != null) {
                Class<T> enumType = (Class<T>) defaultValue.getClass();
                return Enum.valueOf(enumType, value.toUpperCase());
            } else {
                return defaultValue;
            }
        } catch (IllegalArgumentException e) {
            warn(property, value, defaultValue, e);
            return defaultValue;
        }
    }


    private static void warn(String property, String value, Object defaultValue, Exception e) {
        logger.warning("Cannot parse supplied value \"" + value + "\" for option \"" + property + "\".  Using default of \"" + defaultValue + "\"", e);
    }
}
