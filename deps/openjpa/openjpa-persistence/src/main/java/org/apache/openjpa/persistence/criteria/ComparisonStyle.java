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
package org.apache.openjpa.persistence.criteria;


/**
 * Defines a comparison style to be applied for query-by-example attribute comparison.
 * 
 * @author Pinaki Poddar
 * 
 * @since 2.0.0
 *
 */
public interface ComparisonStyle {
    public static enum StringComparisonMode {
        EXACT,            // compares exactly preserving case
        CASE_INSENSITIVE, // compares ignoring case
        LIKE              // compares with LIKE() operator
    }
    
    /**
     * Affirms if the attribute comparisons are OR'ed.
     * Defaults to false.
     */
    boolean isDisjunction();
    
    /**
     * Sets whether the comparison to OR the attribute comparisons.
     */
    ComparisonStyle setDisjunction(boolean flag);
    
    /**
     * Affirms if the null-valued attribute be excluded from comparison.
     * Defaults to true.
     */
    boolean excludeNull();
    
    /**
     * Sets whether the comparison excludes null-valued attributes.
     */
    ComparisonStyle setExcludeNull(boolean flag);
    
    /**
     * Affirms if the identity attribute be excluded from comparison.
     * Defaults to true.
     */
    boolean excludeIdentity();
    
    /**
     * Sets whether the comparison excludes identity attribute.
     */
    ComparisonStyle setExcludeIdentity(boolean flag);
    
    /**
     * Affirms if the version attribute be excluded from comparison.
     * Defaults to true.
     */
    boolean excludeVersion();
    
    /**
     * Sets whether the comparison excludes version attribute.
     */
    ComparisonStyle setExcludeVersion(boolean flag);
    
    /**
     * Affirms if the default-valued attribute be excluded from comparison.
     * Defaults to true.
     */
    
    boolean excludeDefault();

    /**
     * Sets whether the comparison excludes default-valued attributes.
     */
    ComparisonStyle setExcludeDefault(boolean flag);
    
    /**
     * Gets how string-valued attributes be compared.
     */
    StringComparisonMode getStringComparsionMode();

    /**
     * Sets the comparison mode for String-valued attributes.
     */
    ComparisonStyle setStringComparisonMode(StringComparisonMode mode);

    /**
     * Default implementation.
     * 
     */
    static class Default implements ComparisonStyle {
        private boolean excludeDefault = true;
        private boolean excludeNull = true;
        private boolean excludeIdentity = true;
        private boolean excludeVersion = true;
        private boolean disjunction = false;
        private StringComparisonMode stringMode = StringComparisonMode.EXACT;
        
        public boolean excludeDefault() {
            return excludeDefault;
        }

        public boolean excludeNull() {
            return excludeNull;
        }

        public StringComparisonMode getStringComparsionMode() {
            return stringMode;
        }

        public boolean isDisjunction() {
            return disjunction;
        }
        
        public ComparisonStyle setExcludeDefault(boolean flag) {
            excludeDefault = flag;
            return this;
        }
        
        public ComparisonStyle setExcludeNull(boolean flag) {
            excludeNull = flag;
            return this;
        }
        
        public ComparisonStyle setStringComparisonMode(StringComparisonMode mode) {
            stringMode = mode;
            return this;
        }
        
        public ComparisonStyle setDisjunction(boolean flag) {
            disjunction = flag;
            return this;
        }
        
        public boolean excludeIdentity() {
            return excludeIdentity;
            
        }
        
        public ComparisonStyle setExcludeIdentity(boolean flag) {
            excludeIdentity = flag;
            return this;
        }
        
        public boolean excludeVersion() {
            return excludeVersion;
            
        }
        
        public ComparisonStyle setExcludeVersion(boolean flag) {
            excludeVersion = flag;
            return this;
        }
    }
}


