package org.apache.openjpa.persistence.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

/**
 * Utility to test validity of identifier or parameter name.
 * 
 * @author Pinaki Poddar
 * @since 2.0.0
 *
 */
public class ReservedWords {
    public static final Set<String> KEYWORDS = new HashSet<String>();
    static {
        KEYWORDS.addAll(Arrays.asList(
            "ABS", "ALL", "AND", "ANY", "AS", "ASC", "AVG", 
            "BETWEEN", "BIT_LENGTH", "BOTH", "BY", 
            "CASE", "CHAR_LENGTH", "CHARACTER_LENGTH", "CLASS", "COALESCE", "CONCAT", "COUNT", 
            "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP",
            "DELETE", "DESC", "DISTINCT", 
            "ELSE", "EMPTY", "END", "ENTRY", "ESCAPE", "EXISTS", 
            "FALSE", "FETCH", "FROM", 
            "GROUP", 
            "HAVING", 
            "IN", "INDEX", "INNER", "IS", 
            "JOIN", 
            "KEY", 
            "LEADING", "LEFT", "LENGTH", "LIKE", "LOCATE", "LOWER", 
            "MAX", "MEMBER", "MIN", "MOD", 
            "NEW", "NOT", "NULL", "NULLIF", 
            "OBJECT", "OF", "OR", "ORDER", "OUTER", 
            "POSITION", 
            "SELECT", "SET", "SIZE", "SOME", "SQRT", "SUBSTRING", "SUM", 
            "THEN", "TRAILING", "TRIM", "TRUE", "TYPE", 
            "UNKNOWN[50]", "UPDATE", "UPPER", 
            "VALUE", 
            "WHEN", "WHERE"));
    };
    
    /**
     * Affirms if the given string matches any of the JPA reserved words in a case-insensitive manner.
     */
    public static boolean isKeyword(String name) {
        return name != null && KEYWORDS.contains(name.toUpperCase());
    }
    
    /**
     * Returns the special character contained in the given name if any.
     * 
     * @return null if no character in the given name is a special character.
     */
    public static Character hasSpecialCharacter(String name) {
        if (name == null)
            return null;
        char[] chars = name.toCharArray();
        if (!Character.isJavaIdentifierStart(chars[0]))
                return chars[0];
        for (int i = 1; i < chars.length; i++) {
            if (!Character.isJavaIdentifierPart(chars[i]))
                    return chars[i];
        }
        return null;
    }
}
