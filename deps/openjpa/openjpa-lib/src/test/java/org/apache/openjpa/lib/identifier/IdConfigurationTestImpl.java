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

import java.util.HashMap;
import java.util.Map;

import org.apache.openjpa.lib.identifier.IdentifierConfiguration;
import org.apache.openjpa.lib.identifier.IdentifierRule;
import org.apache.openjpa.lib.identifier.IdentifierUtil;

public class IdConfigurationTestImpl implements IdentifierConfiguration {

    Map<String, IdentifierRule> _rules = new HashMap<String, IdentifierRule>();
    private final String conversionKey = getLeadingDelimiter() + getIdentifierDelimiter() + getTrailingDelimiter();

    public IdConfigurationTestImpl() {
        _rules.put("DEFAULT", _defRule);
    }
    
    private IdentifierRule _defRule = new IdentifierRule();
    
    public boolean delimitAll() {
        return false;
    }

    public IdentifierRule getDefaultIdentifierRule() {
        return _defRule;
    }

    public String getDelimitedCase() {
        return IdentifierUtil.CASE_PRESERVE;
    }

    public String getLeadingDelimiter() {
        return "`";
    }

    public String getIdentifierDelimiter() {
        return ":";
    }

    public String getIdentifierConcatenator() {
        return "-";
    }

    public <T> IdentifierRule getIdentifierRule(T t) {
        IdentifierRule r =  _rules.get(t);
        if (r == null) {
            return getDefaultIdentifierRule();
        }
        return r;
    }

    @SuppressWarnings("unchecked")
    public <T> Map<T, IdentifierRule> getIdentifierRules() {
        return (Map<T, IdentifierRule>) _rules;
    }

    public String getTrailingDelimiter() {
        return "`";
    }

    public String getSchemaCase() {
        return IdentifierUtil.CASE_UPPER;
    }

    public boolean getSupportsDelimitedIdentifiers() {
        return true;
    }

    public String getConversionKey() {
        return conversionKey;
    }
}
