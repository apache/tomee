/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.lib.util;

import java.util.Properties;

/**
 * A specialization of the {@link Properties} map type with added
 * convenience methods to retrieve and set options as primitive values.
 * The internal representation of all data is kept in string form.
 *
 * @author Abe White
 * @nojavadoc
 */
public class TypedProperties extends Properties {

    /**
     * Default constructor.
     */
    public TypedProperties() {
        super();
    }

    /**
     * Construct the properties instance with the given set of defaults.
     *
     * @see Properties#Properties(Properties)
     */
    public TypedProperties(Properties defaults) {
        super(defaults);
    }

    /**
     * Return the property under the given key as a boolean, or false if
     * it does not exist and has no set default.
     */
    public boolean getBooleanProperty(String key) {
        return getBooleanProperty(key, false);
    }

    /**
     * Return the property under the given key as a boolean, or the given
     * default if it does not exist.
     */
    public boolean getBooleanProperty(String key, boolean def) {
        String val = getProperty(key);
        if (val == null)
            return def;
        return "t".equalsIgnoreCase(val) || "true".equalsIgnoreCase(val);
    }

    /**
     * Return the property under the given key as a float, or 0 if
     * it does not exist and has no set default.
     *
     * @throws NumberFormatException on parse error
     */
    public float getFloatProperty(String key) {
        return getFloatProperty(key, 0F);
    }

    /**
     * Return the property under the given key as a float, or the given
     * default if it does not exist.
     *
     * @throws NumberFormatException on parse error
     */
    public float getFloatProperty(String key, float def) {
        String val = getProperty(key);
        return (val == null) ? def : Float.parseFloat(val);
    }

    /**
     * Return the property under the given key as a double, or 0 if
     * it does not exist and has no set default.
     *
     * @throws NumberFormatException on parse error
     */
    public double getDoubleProperty(String key) {
        return getDoubleProperty(key, 0D);
    }

    /**
     * Return the property under the given key as a double, or the given
     * default if it does not exist.
     *
     * @throws NumberFormatException on parse error
     */
    public double getDoubleProperty(String key, double def) {
        String val = getProperty(key);
        return (val == null) ? def : Double.parseDouble(val);
    }

    /**
     * Return the property under the given key as a long, or 0 if
     * it does not exist and has no set default.
     *
     * @throws NumberFormatException on parse error
     */
    public long getLongProperty(String key) {
        return getLongProperty(key, 0L);
    }

    /**
     * Return the property under the given key as a double, or the given
     * default if it does not exist.
     *
     * @throws NumberFormatException on parse error
     */
    public long getLongProperty(String key, long def) {
        String val = getProperty(key);
        return (val == null) ? def : Long.parseLong(val);
    }

    /**
     * Return the property under the given key as an int, or 0 if
     * it does not exist and has no set default.
     *
     * @throws NumberFormatException on parse error
     */
    public int getIntProperty(String key) {
        return getIntProperty(key, 0);
    }

    /**
     * Return the property under the given key as an int, or the given
     * default if it does not exist.
     *
     * @throws NumberFormatException on parse error
     */
    public int getIntProperty(String key, int def) {
        String val = getProperty(key);
        return (val == null) ? def : Integer.parseInt(val);
    }

    /**
     * Overrides {@link Properties#setProperty(String,String)} to remove
     * the key if the given value is <code>null</code>.
     *
     * @see Properties#setProperty(String,String)
     */
    public Object setProperty(String key, String val) {
        if (val == null)
            return remove(key);
        return super.setProperty(key, val);
    }

    /**
     * Set the given key to a string version of the given value.
     *
     * @see Properties#setProperty(String,String)
     */
    public void setProperty(String key, boolean val) {
        setProperty(key, String.valueOf(val));
    }

    /**
     * Set the given key to a string version of the given value.
     *
     * @see Properties#setProperty(String,String)
     */
    public void setProperty(String key, double val) {
        setProperty(key, String.valueOf(val));
    }

    /**
     * Set the given key to a string version of the given value.
     *
     * @see Properties#setProperty(String,String)
     */
    public void setProperty(String key, float val) {
        setProperty(key, String.valueOf(val));
    }

    /**
     * Set the given key to a string version of the given value.
     *
     * @see Properties#setProperty(String,String)
     */
    public void setProperty(String key, int val) {
        setProperty(key, String.valueOf(val));
    }

    /**
     * Set the given key to a string version of the given value.
     *
     * @see Properties#setProperty(String,String)
     */
    public void setProperty(String key, long val) {
        setProperty(key, String.valueOf(val));
    }

    /**
     * Remove the given property.
     */
    public String removeProperty(String key) {
        Object val = remove(key);
        return (val == null) ? null : val.toString();
    }

    /**
     * Remove the given property, or return the given default if it does
     * not exist.
     */
    public String removeProperty(String key, String def) {
        if (!containsKey(key))
            return def;
        return removeProperty(key);
    }

    /**
     * Remove the property under the given key as a boolean.
     */
    public boolean removeBooleanProperty(String key) {
        String val = removeProperty(key);
        return "t".equalsIgnoreCase(val) || "true".equalsIgnoreCase(val);
    }

    /**
     * Remove the property under the given key as a boolean, or return the
     * given default if it does not exist.
     */
    public boolean removeBooleanProperty(String key, boolean def) {
        if (!containsKey(key))
            return def;
        return removeBooleanProperty(key);
    }

    /**
     * Remove the property under the given key as a double.
     *
     * @throws NumberFormatException on parse error
     */
    public double removeDoubleProperty(String key) {
        String val = removeProperty(key);
        return (val == null) ? 0D : Double.parseDouble(val);
    }

    /**
     * Remove the property under the given key as a double, or return the
     * given default if it does not exist.
     *
     * @throws NumberFormatException on parse error
     */
    public double removeDoubleProperty(String key, double def) {
        if (!containsKey(key))
            return def;
        return removeDoubleProperty(key);
    }

    /**
     * Remove the property under the given key as a float.
     *
     * @throws NumberFormatException on parse error
     */
    public float removeFloatProperty(String key) {
        String val = removeProperty(key);
        return (val == null) ? 0F : Float.parseFloat(val);
    }

    /**
     * Remove the property under the given key as a float, or return the
     * given default if it does not exist.
     *
     * @throws NumberFormatException on parse error
     */
    public float removeFloatProperty(String key, float def) {
        if (!containsKey(key))
            return def;
        return removeFloatProperty(key);
    }

    /**
     * Remove the property under the given key as a int.
     *
     * @throws NumberFormatException on parse error
     */
    public int removeIntProperty(String key) {
        String val = removeProperty(key);
        return (val == null) ? 0 : Integer.parseInt(val);
    }

    /**
     * Remove the property under the given key as a int, or return the
     * given default if it does not exist.
     *
     * @throws NumberFormatException on parse error
     */
    public int removeIntProperty(String key, int def) {
        if (!containsKey(key))
            return def;
        return removeIntProperty(key);
    }

    /**
     * Remove the property under the given key as a long.
     *
     * @throws NumberFormatException on parse error
     */
    public long removeLongProperty(String key) {
        String val = removeProperty(key);
        return (val == null) ? 0L : Long.parseLong(val);
    }

    /**
     * Remove the property under the given key as a long, or return the
     * given default if it does not exist.
     *
     * @throws NumberFormatException on parse error
     */
    public long removeLongProperty(String key, long def) {
        if (!containsKey(key))
            return def;
        return removeLongProperty(key);
    }
}
