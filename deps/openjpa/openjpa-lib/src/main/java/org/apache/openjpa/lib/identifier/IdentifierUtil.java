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
package org.apache.openjpa.lib.identifier;

/**
 * Base IdentifierUtil interface.  Defines many operations for operating
 * on strings using identifier rules.
 */
public interface IdentifierUtil {
    public static final String DOUBLE_QUOTE = "\"";
    public static final String DOT = ".";
    public static final String UNDERSCORE = "_";
    public static final String SPACE = " ";
    public static final String BAR = "|";
    public static final String EMPTY = "";
    public static final String PERCENT = "%";
    
    public static final char DOLLAR_CHAR = '$';
    public static final char UNDERSCORE_CHAR = '_';
    
    public static final String CASE_UPPER = "upper";
    public static final String CASE_LOWER = "lower";
    public static final String CASE_PRESERVE = "preserve";
    /**
     * Get the naming configuration.
     */
    public IdentifierConfiguration getIdentifierConfiguration();
    
    /**
     * Set the naming configuration to use for naming operations.  A naming
     * configuration must be set before calling any other methods.
     */
    void setIdentifierConfiguration(IdentifierConfiguration config);
    
    /**
     * Delimit the name if it requires delimiters
     * @param the rule to use for delimiting
     * @param name the name to delimit
     * @return the delimited name, if delimiting was necessary.
     */
    public String delimit(String rule, String name);

    /**
     * Delimit the name if it requires delimiters
     * @param the rule to use for delimiting
     * @param name the name to delimit
     * @return the delimited name, if delimiting was necessary.
     */
    public String delimit(IdentifierRule rule, String name);
    
    /**
     * Delimit the string with the option to force delimiting.  If force is
     * true, the name will delimited without checking to see if it 
     * requires delimiters.
     * @param the rule to use for delimiting
     * @param name the name to delimit
     * @param force add delimiters even if delimiting is not required
     * @return the delimited name, if delimiting was necessary.
     */
    public String delimit(String rule, String name, boolean force);

    /**
     * Delimit the string with the option to force delimiting.  If force is
     * true, the name will delimited without checking to see if it 
     * requires delimiters.
     * @param the rule to use for delimiting
     * @param name the name to delimit
     * @param force add delimiters even if delimiting is not required
     * @return the delimited name, if delimiting was necessary.
     */
    public String delimit(IdentifierRule rule, String name, boolean force);
    
    
    /**
     * Remove delimiters from a delimited name 
     * @param the rule to use for removing delimiters
     * @param name the name from which to remove delimiters
     */
    public String removeDelimiters(String rule, String name);

    /**
     * Remove delimiters from a delimited name 
     * @param the rule to use for removing delimiters
     * @param name the name from which to remove delimiters
     */
    public String removeDelimiters(IdentifierConfiguration config, String rule, String name);

    /**
     * Remove delimiters from a delimited name 
     * @param the rule to use for removing delimiters
     * @param name the name from which to remove delimiters
     */
    public String removeDelimiters(IdentifierRule rule, String name);
    
    /**
     * Determines whether a name is delimited.
     * @param the rule to use for removing delimiters
     * @param name the name to examine for delimiters
     */
    public boolean isDelimited(String rule, String name); 
    
    /**
     * Determines whether a name is delimited.
     * @param the rule to use for removing delimiters
     * @param name the name to examine for delimiters
     */
    public boolean isDelimited(IdentifierRule rule, String name); 

    /**
     * Determines whether a name requires delimiters based upon:
     * <ul>
     * <li> The SQL-92 Reference definition of a valid unquoted name</li>
     * <li> The naming rule specified</li> 
     * </ul>
     * @param the rule to use for removing delimiters
     * @param name the name to examine for delimiting requirements
     */
    public boolean requiresDelimiters(String rule, String name);

    /**
     * Determines whether a name requires delimiters based upon:
     * <ul>
     * <li> The SQL-92 Reference definition of a valid unquoted name</li>
     * <li> The naming rule specified</li> 
     * </ul>
     * @param the rule to use for removing delimiters
     * @param name the name to examine for delimiting requirements
     */
    public boolean requiresDelimiters(IdentifierRule rule, String name);
    
    /**
     * Combines names using delimiting rules and appropriate separators
     * @return a combined name
     *         ex. {"TH IS", THAT} -> "TH IS_THAT" 
     */
    public String combineNames(String rule, String[] rules, String[] names);

    /**
     * Combines names using delimiting rules and appropriate separators
     * @return a combined name
     *         ex. {"TH IS", THAT} -> "TH IS_THAT" 
     */
    public String combineNames(IdentifierRule rule, IdentifierRule[] rules, String[] names);

    /**
     * Combines names using the specified delimiting rule and appropriate separators
     * @return a combined name
     *         ex. {"TH IS", THAT} -> "TH IS_THAT" 
     */
    public String combineNames(String rule, String[] names);

    /**
     * Combines names using the specified delimiting rule and appropriate separators
     * @return a combined name
     *         ex. {"TH IS", THAT} -> "TH IS_THAT" 
     */
    public String combineNames(IdentifierRule rule, String[] names);
    
    /**
     * Combines two names using delimiting rules and appropriate separators
     */
    public String combineNames(String rule, String name1, String name2);

    /**
     * Combines two names using delimiting rules and appropriate separators
     */
    public String combineNames(IdentifierRule rule, String name1, String name2);

    /**
     * Joins several names with different naming rules into a single string
     * using appropriate delimiters and separators
     */
    public String joinNames(String[] rules, String[] names);
    
    /**
     * Joins several names with different naming rules into a single string
     * using appropriate delimiters and separators
     */
    public String joinNames(IdentifierRule[] rules, String[] names);

    /**
     * Joins several names with different naming rules into a single string
     * using appropriate delimiters and separators
     */
    public String joinNames(String rule, String[] names);
    
    /**
     * Joins several names with different naming rules into a single string
     * using appropriate delimiters and separators
     */
    public String joinNames(IdentifierRule rule, String[] names);

    /**
     * Joins several names with different naming rules into a single string
     * using the specified delimiter
     */
    public String joinNames(IdentifierRule rule, String[] names, String delimiter);
    /**
     * Joins several names with different naming rules into a single string
     * using the specified delimiter
     */
    public String joinNames(String rule, String[] names, String delimiter);

    /**
     * Splits a combined name name using the provided naming rule
     * @param name  the multi-value name
     * @return individual components of the name
     *         ex. schema.table --> { schema, table }
     */
    public String[] splitName(String rule, String name);
    
    /**
     * Splits a combined name name using the provided naming rule
     * @param name  the multi-value name
     * @return individual components of the name
     *         ex. schema.table --> { schema, table }
     */
    public String[] splitName(IdentifierRule rule, String name);

    /**
     * Splits a combined name name using the provided naming rule and
     * delimiter.
     * @param name  the multi-value name
     * @return individual components of the name
     *         ex. schema.table --> { schema, table }
     */
    public String[] splitName(String rule, String name, String delimiter);

    /**
     * Splits a combined name name using the provided naming rule and
     * delimiter.
     * @param name  the multi-value name
     * @return individual components of the name
     *         ex. schema.table --> { schema, table }
     */
    public String[] splitName(IdentifierRule rule, String name, String delimiter);

    /**
     * Returns whether a name is considered a reserved word
     */
    public boolean isReservedWord(String rule, String name);
    
    /**
     * Returns whether a name is considered a reserved word
     */
    public boolean isReservedWord(IdentifierRule rule, String name);
    
    /**
     * Convert the string using this naming configuration to the supplied
     * naming configuration.
     */
    public String convert(IdentifierConfiguration config, String rule, String name);
    
    /**
     * Truncates a name while maintaining delimiters.
     */
    public String truncateName(String rule, String name, int length);

    /**
     * Truncates a name while maintaining delimiters.
     */
    public String truncateName(IdentifierRule rule, String name, int length);

    /**
     * Append the names together while maintaining delimiters.
     */
    public String appendNames(IdentifierRule rule, String name1, String name2);

    /**
     * Append the names together while maintaining delimiters.
     */
    public String appendNames(String rule, String name1, String name2);
    
    /**
     * Converts a qualified string-based name defined using the base configuration to the
     * specified configuration.  Returns the converted name.
     */
    public String convertFull(IdentifierConfiguration config, String rule, String fullName);
    
    /**
     * Removes Hungarian notation from the specified string.
     */
    public String removeHungarianNotation(String rule, String name);
    /**
     * Removes Hungarian notation from the specified string.
     */
    public String removeHungarianNotation(IdentifierRule rule, String name);

    /**
     * Determines whether a name can be split into multiple components.
     */
    public boolean canSplit(String rule, String name);

    /**
     * Determines whether a name can be split into multiple components.
     */
    public boolean canSplit(IdentifierRule rule, String name);

    /**
     * Determines whether a name can be split into multiple components, taking
     * into account the specified delimiter.
     */
    public boolean canSplit(String rule, String name, String delim);

    /**
     * Determines whether a name can be split into multiple components, taking
     * into account the specified delimiter.
     */
    public boolean canSplit(IdentifierRule rule, String name, String delim);
}
