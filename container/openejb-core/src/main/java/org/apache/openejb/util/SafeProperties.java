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
package org.apache.openejb.util;

import java.util.Properties;

import org.apache.openejb.OpenEJBException;

public class SafeProperties {

    private String systemLocation;
    private Properties props;

    public SafeProperties(Properties props, String systemLocation) throws OpenEJBException {
        if (props == null) OpenEJBErrorHandler.propertiesObjectIsNull(systemLocation);
        this.props = props;
        this.systemLocation = systemLocation;
    }

    public String getProperty(String key) throws OpenEJBException {
        String value = props.getProperty(key);
        if (value == null) OpenEJBErrorHandler.propertyNotFound(key, systemLocation + " properties object");
        return value;
    }

    public String getProperty(String key, String defaultValue) throws OpenEJBException {
        String value = props.getProperty(key);
        if (value == null)
            return defaultValue;
        else
            return value;
    }

    public int getPropertyAsInt(String key) throws OpenEJBException {
        int integer = 0;
        String value = getProperty(key);
        try {
            integer = Integer.parseInt(value);
        }
        catch (NumberFormatException nfe) {
            OpenEJBErrorHandler.propertyValueIsIllegal(key, value);
        }
        return integer;
    }

    public int getPropertyAsInt(String key, int defaultValue) throws OpenEJBException {
        int integer = defaultValue;
        String value = getProperty(key, String.valueOf(defaultValue));
        try {
            integer = Integer.parseInt(value);
        }
        catch (NumberFormatException nfe) {
            OpenEJBErrorHandler.propertyValueIsIllegal(key, value);
        }
        return integer;
    }

    public Integer getPropertyAsInteger(String key, Integer defaultValue) throws OpenEJBException {
        Integer integer = null;
        String value = getProperty(key, defaultValue.toString());
        try {
            integer = new Integer(value);
        }
        catch (NumberFormatException nfe) {
            OpenEJBErrorHandler.propertyValueIsIllegal(key, value);
        }
        return integer;
    }

    public Integer getPropertyAsInteger(String key) throws OpenEJBException {
        Integer integer = null;
        String value = getProperty(key);
        try {
            integer = new Integer(value);
        }
        catch (NumberFormatException nfe) {
            OpenEJBErrorHandler.propertyValueIsIllegal(key, value);
        }
        return integer;
    }

    public boolean getPropertyAsBoolean(String key) throws OpenEJBException {
        Integer integer = null;
        String value = getProperty(key);
        return new Boolean(value).booleanValue();
    }

    public Boolean getPropertyAsBoolean(String key, Boolean defaultValue) throws OpenEJBException {
        Integer integer = null;
        String value = getProperty(key, defaultValue.toString());
        return new Boolean(value);
    }

}