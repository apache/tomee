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
package org.apache.openjpa.jdbc.identifier;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.lib.identifier.IdentifierConfiguration;
import org.apache.openjpa.lib.identifier.IdentifierRule;
import org.apache.openjpa.lib.identifier.IdentifierUtil;

/**
 * Static utility class used for operating on string based identifiers. 
 */
public class Normalizer {

    private static IdentifierUtil normalizer = 
        new DBIdentifierUtilImpl(new DefaultIdentifierConfiguration());
    
    private static IdentifierRule defaultRule;
    
    static {
        defaultRule = normalizer.getIdentifierConfiguration().getDefaultIdentifierRule();
    }

    public static IdentifierConfiguration getNamingConfiguration() {
        return normalizer.getIdentifierConfiguration();
    }
    
    /**
     * Normalizes a multi-part name
     * @param name
     * @return
     */
    public static String normalizeMulti(String name) {
        if (StringUtils.isEmpty(name)) {
            return name;
        }
        String[] names = normalizer.splitName(defaultRule, name);
        return normalizer.joinNames(defaultRule, names);
    }

    /**
     * Normalizes a single part name
     * @param name
     * @return
     */
    public static String normalizeString(String name) {
        if (StringUtils.isEmpty(name)) {
            return name;
        }
        if (!normalizer.isDelimited(defaultRule, name)) {
            // If not delimited, delimit the string if necessary
            return normalizer.delimit(defaultRule, name);
        }
        return name;
    }
    
    /**
     * Returns true if the name is delimited with default delimiters
     * @param name
     * @return
     */
    public static boolean isDelimited(String name) {
        if (StringUtils.isEmpty(name)) {
            return false;
        }
        return normalizer.isDelimited(defaultRule, name);
    }
    
    /**
     * Splits names into individual components and compares individually
     * for equality
     * @param name1
     * @param name2
     * @return
     */
    public static boolean fullNamesEqual(String name1, String name2) {
        if (StringUtils.isEmpty(name1) && StringUtils.isEmpty(name2)) {
            return true;
        }
        // Split multi-part names into individual components and compare
        // each component.  If delimited, do case compare.
        String[] names1 = normalizer.splitName(defaultRule, name1);
        String[] names2 = normalizer.splitName(defaultRule, name2);
        if (names1.length != names2.length) {
            return false;
        }
        for (int i = 0; i < names1.length; i++) {
            if (normalizer.isDelimited(defaultRule, names1[i])) {
                if (!StringUtils.equals(names1[i],names2[i])) {
                    return false;
                }
            } else {
                if (!StringUtils.equalsIgnoreCase(names1[i],names2[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Compares two string names for equality.  If delimited, does a
     * case comparison.  If not delimited, does a case insensitive 
     * comparison.
     * @param name1
     * @param name2
     * @return
     */
    public static boolean namesEqual(String name1, String name2) {
        if (StringUtils.isEmpty(name1) && StringUtils.isEmpty(name2)) {
            return true;
        }
        if (normalizer.isDelimited(defaultRule, name1)) {
            if (!StringUtils.equals(name1, name2)) {
                return false;
            }
        } else {
            if (!StringUtils.equalsIgnoreCase(name1, name2)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Normalizes a name, if not delimited, converts to uppercase.
     * @param name
     * @return
     */
    public static String normalizeUpper(String name) {
        String nName = normalizeString(name);
        // Do not convert delimited names to upper case.  They may have
        // been delimited to preserve case.
        if (!isDelimited(nName)) {
            nName = name.toUpperCase();
        }
        return nName;
    }
    
    /**
     * Splits a name into normalized components
     * @param name
     * @return
     */
    public static String[] splitName(String name) {
        return normalizer.splitName(defaultRule, name);
    }
    
    
    /**
     * Splits a name into normalized components using the specified
     * name delimiter (ex. schema:table, delim = : --> { schema, table }
     * @param name
     * @return
     */
    public static String[] splitName(String name, String delim) {
        return normalizer.splitName(defaultRule, name, delim);
    }

    /**
     * Joins multiple names using default identifier rules.
     * @param names
     * @return
     */
    public static String joinNames(String[] names) {
        return normalizer.joinNames(defaultRule, names);
    }

    /**
     * Joins multiple names using the specified delimiter.
     * @param names
     * @return
     */
    public static String joinNames(String[] names, String delimiter) {
        return normalizer.joinNames(defaultRule, names, delimiter);
    }
    
    /**
     * Joins two names using the default identifier rules.
     * @param names
     * @return
     */
    public static String joinNames(String name1, String name2) {
        return joinNames(new String[] { name1, name2});
    }
    

    /**
     * Truncates a name to the specified length while maintaining
     * delimiters.
     * @param name
     * @param length
     * @return
     */
    public static String truncate(String name, int length) {
        return normalizer.truncateName(defaultRule, name, length);
    }

    /**
     * Convert a normalized name to a name using the specified configuration and
     * naming rule.
     * Note: Currently only delimiters are converted.
     * @param config
     * @param rule
     * @param name
     * @return
     */
    public static String convert(IdentifierConfiguration config, String rule, String name) {
        return normalizer.convert(config, rule, name);
    }

    /**
     * Combines two names using default identifier rules.
     * @param name1
     * @param name2
     * @return
     */
    public static String combine(String name1, String name2) {
        return normalizer.combineNames(defaultRule, name1, name2);
    }

    /**
     * Combines multiple names using default identifier rules.
     * @param name1
     * @param name2
     * @return
     */
    public static String combine(String...names) {
        return normalizer.combineNames(defaultRule, names);
    }

    
    /**
     * Appends one string to another using default identifier rules.
     * @param name1
     * @param name2
     * @return
     */
    public static String append(String name1, String name2) {
        return normalizer.appendNames(defaultRule, name1, name2);
    }
    
    /**
     * Removes Hungarian notation from a string.
     * @param name1
     * @param name2
     * @return
     */
    public static String removeHungarianNotation(String name) {
        return normalizer.removeHungarianNotation(defaultRule, name);
    }

    /**
     * Removes default delimiters from a string.
     * @param name1
     * @param name2
     * @return
     */
    public static String removeDelimiters(String name) {
        return normalizer.removeDelimiters(defaultRule, name);
    }

    /**
     * Delimits a string if necessary, optionally forcing it to be
     * delimited.
     * @param name1
     * @param name2
     * @return
     */
    public static String delimit(String name, boolean force) {
        return normalizer.delimit(defaultRule, name, force);
    }

    /**
     * Determines whether a name can be split into multiple components.
     * @param name1
     * @param name2
     * @return
     */
    public static boolean canSplit(String name) {
        return normalizer.canSplit(defaultRule, name);
    }

    /**
     * Determines whether a name can be split into multiple components, taking
     * into account the specified delimiter.
     * @param name1
     * @param name2
     * @return
     */
    public static boolean canSplit(String name, String delim) {
        return normalizer.canSplit(defaultRule, name, delim);
    }
}
