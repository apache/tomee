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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;

/**
 * The standard identifier rule.  Rules are used for specific configuration
 * of identifier types.  For example.  A rule could be used to indicate that
 * an identifier type should not be delimited or has a max length of 255 
 * characters.
 *
 */
public class IdentifierRule {
    
    public static final Set<String> EMPTY_SET = new HashSet<String>(0);
    public static final String DEFAULT_RULE = "default";
    public static char UNDERSCORE = '_';

    private String _name;
    private int _maxLength = 128;
    private boolean _nullable = false;
    private boolean _allowTruncation = false;
    private boolean _allowCompaction = true;
    private boolean _canDelimit = true;
    private boolean _mustDelimit = false;
    private boolean _mustBeginWithLetter = true;
    private boolean _onlyLettersDigitsUnderscores = true;
    private String _specialCharacters = "";
    private Set<String> _reservedWords = null;
    private boolean _delimitReservedWords = false;
    private String _wildcard = "%";

    public void setName(String name) {
        _name = name;
    }
    
    public String getName() {
        return _name;
    }

    public void setMaxLength(int maxLength) {
        _maxLength = maxLength;
    }

    public int getMaxLength() {
        return _maxLength;
    }

    public void setAllowTruncation(boolean allowTruncation) {
        _allowTruncation = allowTruncation;
    }

    public boolean isAllowTruncation() {
        return _allowTruncation;
    }

    public void setNullable(boolean nullable) {
        _nullable = nullable;
    }

    public boolean isNullable() {
        return _nullable;
    }

    public void setAllowCompaction(boolean allowCompaction) {
        _allowCompaction = allowCompaction;
    }

    public boolean getAllowCompaction() {
        return _allowCompaction;
    }

    public void setCanDelimit(boolean canDelimit) {
        _canDelimit = canDelimit;
    }

    public boolean getCanDelimit() {
        return _canDelimit;
    }

    public void setMustDelimit(boolean mustDelimit) {
        _mustDelimit = mustDelimit;
    }

    public boolean getMustDelimit() {
        return _mustDelimit;
    }

    public void setMustBeginWithLetter(boolean mustBeginWithLetter) {
        _mustBeginWithLetter = mustBeginWithLetter;
    }

    public boolean isMustBeginWithLetter() {
        return _mustBeginWithLetter;
    }

    public void setOnlyLettersDigitsUnderscores(boolean onlyLettersDigitsUnderscores) {
        _onlyLettersDigitsUnderscores = onlyLettersDigitsUnderscores;
    }

    public boolean isOnlyLettersDigitsUnderscores() {
        return _onlyLettersDigitsUnderscores;
    }

    public void setReservedWords(Set<String> reservedWords) {
        _reservedWords = reservedWords;
    }

    public Set<String> getReservedWords() {
        if (_reservedWords == null) {
            _reservedWords = new HashSet<String>();
        }
        return _reservedWords;
    }

    public void setSpecialCharacters(String specialCharacters) {
        _specialCharacters = specialCharacters;
    }

    public String getSpecialCharacters() {
        return _specialCharacters;
    }

    public void setDelimitReservedWords(boolean delimitReservedWords) {
        _delimitReservedWords = delimitReservedWords;
    }

    public boolean getDelimitReservedWords() {
        return _delimitReservedWords;
    }

    /**
     * SQL identifier rules:
     * 1) Can be up to 128 characters long
     * 2) Must begin with a letter
     * 3) Can contain letters, digits, and underscores
     * 4) Can't contain spaces or special characters such as #, $, &, %, or 
     *    punctuation.
     * 5) Can't be reserved words
     */
    public boolean requiresDelimiters(String identifier) {

        // Do not delimit single valued wildcards or "?" or names that have method-type
        // signatures (ex. getValue()).  These are considered special values in OpenJPA
        // and should not be delimited.
        if (_wildcard.equals(identifier) || "?".equals(identifier) ||
            identifier.endsWith("()")) {
            return false;
        }
        
        if (getMustDelimit()) {
            return true;
        }
        
        // Assert identifier begins with a letter
        char[] chars = identifier.toCharArray();
        if (isMustBeginWithLetter()) {
            if (!CharUtils.isAsciiAlpha(chars[0])) {
                return true;
            }
        }

        // Iterate through chars, asserting delimiting rules 
        for (char ch : chars) {
            if (isOnlyLettersDigitsUnderscores()) {
                if (!CharUtils.isAsciiAlphanumeric(ch) && !(ch == UNDERSCORE)) {
                    return true;
                }
            }
            // Look for special characters
            if (StringUtils.contains(getSpecialCharacters(), ch)) {
                return true;
            }
        }
        // Finally, look for reserved words
        if (getDelimitReservedWords()) {
            if (isReservedWord(identifier)) {
                return true;
            }
        }
        return false;
    }

    public boolean isReservedWord(String identifier) {
        return _reservedWords.contains(identifier);
    }
}
