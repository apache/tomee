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

import java.util.Set;

import org.apache.openjpa.jdbc.identifier.DBIdentifier.DBIdentifierType;
import org.apache.openjpa.lib.identifier.IdentifierRule;

/**
 * The default DBIdentifier rule.  Closely matches SQL92 naming rules.
 */
public class DBIdentifierRule extends IdentifierRule {
    
    public static final String DEFAULT_SQL_92 = "DefaultSQL92";
    
    public static final String SPECIAL_CHARS = " #$&%!?,.:;\"\'";
    
    public DBIdentifierRule() {
        setName(DEFAULT_SQL_92);
        // SQL92 Identifier rule 1) Can be up to 128 characters long
//        setMaxLength(128);
        // OpenJPA allows names with a length of 255 by default
        setMaxLength(255);
        // SQL92 Identifier rule 2) Must begin with a letter
        setMustBeginWithLetter(true);
        // SQL92 Identifier rule 3) Can contain letters, digits, and underscores
        setOnlyLettersDigitsUnderscores(true);
        // SQL Identifier rule 4) Can't contain spaces or special characters such 
        // as #, $, &, %, or punctuation.
        setSpecialCharacters(SPECIAL_CHARS);
    }

    public DBIdentifierRule(DBIdentifierType id, Set<String> reservedWords) {
        this();
        setName(id.toString());
        // SQL Identifier rule 5) Can't be reserved words
        setReservedWords(reservedWords);
    }
}
