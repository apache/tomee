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
package org.apache.openjpa.persistence;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.CacheRetrieveMode;
import javax.persistence.CacheStoreMode;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.kernel.DataCacheRetrieveMode;
import org.apache.openjpa.kernel.DataCacheStoreMode;

/**
 * Enumerates configuration property keys defined in JPA 2.0 Specification.
 * <br>
 * Provides static utility functions to read their values from supplied map of properties.
 * <br>
 * Provides static utility functions to convert them to values that are fit for OpenJPA implementation.  
 * <br>
 * @author Pinaki Poddar
 * @since 2.0.0
 *
 */
public class JPAProperties {
    private static final String REGEX_DOT           = "\\.";
    public static final String PREFIX              = "javax.persistence.";
    
    public static final String PROVIDER            = PREFIX + "provider";
    public static final String TRANSACTION_TYPE    = PREFIX + "transactionType";
    
    public static final String DATASOURCE          = PREFIX + "dataSource";
    public static final String DATASOURCE_JTA      = PREFIX + "jtaDataSource";
    public static final String DATASOURCE_NONJTA   = PREFIX + "nonJtaDataSource";
    
    public static final String JDBC_DRIVER          = PREFIX + "jdbc.driver";
    public static final String JDBC_URL             = PREFIX + "jdbc.url";
    public static final String JDBC_USER            = PREFIX + "jdbc.user";
    public static final String JDBC_PASSWORD        = PREFIX + "jdbc.password";
    
    public static final String LOCK_SCOPE           = PREFIX + "lock.scope";
    public static final String LOCK_TIMEOUT         = PREFIX + "lock.timeout";
    
    public static final String QUERY_TIMEOUT        = PREFIX + "query.timeout";
    
    public static final String CACHE_MODE           = PREFIX + "sharedCache.mode";
    public static final String CACHE_STORE_MODE     = PREFIX + "cache.storeMode";
    public static final String CACHE_RETRIEVE_MODE  = PREFIX + "cache.retrieveMode";
    
    public static final String VALIDATE_FACTORY     = PREFIX + "validation.factory";
    public static final String VALIDATE_MODE        = PREFIX + "validation.mode";
    public static final String VALIDATE_PRE_PERSIST = PREFIX + "validation.group.pre-persist";
    public static final String VALIDATE_PRE_REMOVE  = PREFIX + "validation.group.pre-remove";
    public static final String VALIDATE_PRE_UPDATE  = PREFIX + "validation.group.pre-update";
    public static final String VALIDATE_GROUP_DEFAULT = "javax.validation.groups.Default";
    
    private static Map<String,String> _names = new HashMap<String, String>();
    
    /**
     * Record the given kernel property key (which is a bean property name without any suffix)
     * corresponding to the given original JPA/OpenJPA property used by the user to set the values.
     */
    static void record(String kernel, String user) {
        _names.put(kernel, user);
    }
    
    /**
     * Gets the original JPA Property name corresponding to the kernel property key 
     * (which is a bean property name without any suffix).
     */
    static String getUserName(String beanProperty) {
        return _names.containsKey(beanProperty) ? _names.get(beanProperty) : beanProperty;
    }
    
    /**
     * Is the given key appears to be a valid JPA specification defined key?
     * 
     * @return true if the given string merely prefixed with <code>javax.persistence.</code>.
     * Does not really check all the keys defined in the specification.
     */
    public static boolean isValidKey(String key) {
        return key != null && key.startsWith(PREFIX);
    }
    
    /**
     * Gets a bean-style property name from the given key.
     * 
     * @param key must begin with JPA property prefix <code>javax.persistence</code>
     * 
     * @return concatenates each part of the string leaving out <code>javax.persistence.</code> prefix. 
     * Part of string is what appears between DOT character.
     */
    public static String getBeanProperty(String key) {
        if (!isValidKey(key))
            throw new IllegalArgumentException("Invalid JPA property " + key);
        String[] parts = key.split(REGEX_DOT);
        StringBuilder buf = new StringBuilder();
        for (int i = 2; i < parts.length; i++) {
            buf.append(StringUtils.capitalize(parts[i]));
        }
        return buf.toString();
    }
    
    /**
     * Convert the given user value to a value consumable by OpenJPA kernel constructs.
     * 
     * @return the same value if the given key is not a valid JPA property key or the value is null.
     */
    public static <T> T  convertToKernelValue(Class<T> resultType, String key, Object value) {
        if (value == null)
            return null;
        if (JPAProperties.isValidKey(key)) {
            // works because enum values are identical String
            if (value instanceof CacheRetrieveMode || (value instanceof String && CACHE_RETRIEVE_MODE.equals(key))) {
                return (T)DataCacheRetrieveMode.valueOf(value.toString().trim().toUpperCase());
            } else if (value instanceof CacheStoreMode || (value instanceof String && CACHE_STORE_MODE.equals(key))) {
                return (T)DataCacheStoreMode.valueOf(value.toString().trim().toUpperCase());
            }
        }
        return (T)value;
    }
    
    /**
     * Convert the given kernel value to a value visible to the user.
     * 
     * @return the same value if the given key is not a valid JPA property key or the value is null.
     */
    public static Object convertToUserValue(String key, Object value) {
        if (value == null)
            return null;
        if (JPAProperties.isValidKey(key)) {
            // works because enum values are identical String
            if (value instanceof DataCacheRetrieveMode) {
                return CacheRetrieveMode.valueOf(value.toString().trim().toUpperCase());
            } else if (value instanceof DataCacheStoreMode) {
                return CacheStoreMode.valueOf(value.toString().trim().toUpperCase());
            }
        }
        return value;
    }
    
    /**
     * Get the value of the given key from the given properties after converting it to the given
     * enumerated value.
     */
    public static <E extends Enum<E>> E getEnumValue(Class<E> type, String key, Map<String,Object> prop) {
        return getEnumValue(type, null, key, prop);
    }
    
    /**
     * Gets a enum value of the given type from the given properties looking up with the given key.
     * Converts the original value from a String or ordinal number, if necessary.
     * Conversion from an integral number to enum value is only attempted if the allowed enum values
     * are provided as non-null, non-empty array. 
     * 
     * @return null if the key does not exist in the given properties.
     */
    public static <E extends Enum<E>> E getEnumValue(Class<E> type, E[] values, String key, Map<String,Object> prop) {
        if (prop == null)
            return null;
        return getEnumValue(type, values, prop.get(key));
    }
    
    /**
     * Gets a enum value of the given type from the given value.
     * Converts the original value from a String, if necessary.
     * 
     * @return null if the key does not exist in the given properties.
     */
    public static <E extends Enum<E>> E  getEnumValue(Class<E> type, Object val) {
        return getEnumValue(type, null, val);
    }
    
    /**
     * Gets a enum value of the given type from the given value.
     * Converts the original value from a String or ordinal number, if necessary.
     * Conversion from an integral number to enum value is only attempted if the allowed enum values
     * are provided as non-null, non-empty array. 
     * 
     * @return null if the key does not exist in the given properties.
     */
    public static <E extends Enum<E>> E  getEnumValue(Class<E> type, E[] values, Object val) {
        if (val == null)
            return null;
        if (type.isInstance(val))
            return (E)val;
        if (val instanceof String) {
            return Enum.valueOf(type, val.toString().trim().toUpperCase());
        }
        if (values != null && values.length > 0 && val instanceof Number) {
            return values[((Number)val).intValue()];
        }
        return null; 
    }
}
