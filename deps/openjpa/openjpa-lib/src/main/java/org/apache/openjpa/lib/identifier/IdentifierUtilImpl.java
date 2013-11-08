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

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.lib.conf.Configurable;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.util.Localizer;

/**
 * Implementation class for the base identifier impl.
 *
 */
public class IdentifierUtilImpl implements IdentifierUtil, Configurable {
        
    private static final Localizer _loc = Localizer.forPackage
        (IdentifierUtilImpl.class);
    
    private IdentifierConfiguration _config = null;
    
    public IdentifierUtilImpl() {
        
    }
    
    public IdentifierUtilImpl(IdentifierConfiguration config) {
        _config = config;
    }

    public void setIdentifierConfiguration(IdentifierConfiguration config) {
        _config = config;
    }    

    public IdentifierConfiguration getIdentifierConfiguration() {
        return _config;
    }    

    public String combineNames(String rule, String name1, String name2) {
        return combineNames(getNamingRule(rule), name1, name2);
    }

    public String combineNames(String rule, String[] names) {
        return combineNames(getNamingRule(rule), names);
    }

    public String combineNames(IdentifierConfiguration config, String rule, String[] names) {
        return combineNames(config, getNamingRule(rule), names);
    }

    public String combineNames(IdentifierRule rule, 
        IdentifierRule[] rules, String[] names) {
        return combineNames(_config, rule, rules, names);
    }

    public String combineNames(IdentifierConfiguration config, IdentifierRule rule, 
        IdentifierRule[] rules, String[] names) {
        boolean delimited = false;
        String combined = null;
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (isDelimited(rules[i], name)) {
                delimited = true;
                name = removeDelimiters(config, rules[i], name);
            }
            if (i == 0) {
                combined = name;
            }
            else {
                combined = combined + config.getIdentifierConcatenator() + name;
            }
        }
        
        if (delimited) {
            combined = delimit(config, rule, combined);
        }
        
        return combined;
    }
    
    public String combineNames(IdentifierConfiguration config, IdentifierRule rule, String name1, String name2) {
        boolean delimit = false;
        if (isDelimited(rule, name1)) {
            name1 = removeDelimiters(config, rule, name1);
            delimit = true;
        }
        if (isDelimited(rule, name2)) {
            name2 = removeDelimiters(config, rule, name2);
            delimit = true;
        }
        String name = name1 + config.getIdentifierConcatenator() + name2;
        return delimit(config, rule, name, delimit);
    }

    public String combineNames(IdentifierConfiguration config, IdentifierRule namingRule, String[] names) {
        boolean delimited = false;
        String combined = null;
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (isDelimited(config, namingRule, name)) {
                delimited = true;
                name = removeDelimiters(config, namingRule, name);
            }
            if (i == 0) {
                combined = name;
            }
            else {
                combined = combined + config.getIdentifierConcatenator() + name;
            }
        }
        if (delimited) {
            combined = delimit(config, namingRule, combined);
        }
        
        return combined;
    }

    public String appendNames(IdentifierRule rule, String name1, String name2) {
        if (isDelimited(rule, name1)) {
            name1 = removeDelimiters(rule, name1);
        }
        if (isDelimited(rule, name2)) {
            name2 = removeDelimiters(rule, name2);
        }
        if (name1 == null) {
            name1 = IdentifierUtil.EMPTY;
        }
        if (name2 == null) {
            name2 = IdentifierUtil.EMPTY;
        }
        String name = name1 + name2;
        return delimit(rule, name);
    }

    /**
     * Joins multiple names together using the standard delimiting rules
     * ex. ( {"s", "t", "c"} --> "s"."t"."c" }
     */
    public String joinNames(IdentifierRule[] rules, String[] names) {
        
        if (names == null || names.length == 0) {
            return null;
        }
        StringBuilder combinedName = new StringBuilder();
        for (int i = 0; i < names.length; i++) {
            combinedName.append(delimit(rules[i], names[i]));
            if (i < (names.length -1)) {
                combinedName.append(_config.getIdentifierDelimiter());
            }
        }
        return combinedName.toString();
    }

    public String joinNames(String rule, String[] names) {
        return joinNames(_config, getNamingRule(rule), names, _config.getIdentifierDelimiter());
    }

    public String joinNames(IdentifierRule rule, String[] names) {
        return joinNames(_config, rule, names, _config.getIdentifierDelimiter());
    }

    public String joinNames(IdentifierRule rule, String[] names, String delimiter) {
        return joinNames(_config, rule, names, delimiter);
    }

    public String joinNames(String rule, String[] names, String delimiter) {
        return joinNames(_config, getNamingRule(rule), names, delimiter);
    }

    /**
     * Join names using a single naming rule and specified delimiter
     * @param rule
     * @param names
     * @return
     */
    public String joinNames(IdentifierConfiguration config, IdentifierRule rule, String[] names, String delimiter) {
        
        if (names == null || names.length == 0) {
            return null;
        }
        StringBuilder combinedName = new StringBuilder();
        for (int i = 0; i < names.length; i++) {
            if (names[i] != null) {
                combinedName.append(delimit(config, rule, names[i], false));
                if (i < (names.length -1)) {
                    combinedName.append(delimiter);
                }
            }
        }
        return combinedName.toString();
    }

    public String[] splitName(IdentifierConfiguration config, IdentifierRule nrule, String name) {
        return splitName(nrule, name, config.getIdentifierDelimiter());
    }

    public String[] splitName(IdentifierRule nrule, String name) {
        return splitName(nrule, name, _config.getIdentifierDelimiter());
    }

    
    /**
     * Splits names using single naming rule and appropriate separators
     * @param name  the multi-value name
     * @return individual components of the name
     *         ex. schema.table --> { schema, table }
     */    
    public String[] splitName(IdentifierRule nrule, String name, String nameDelim) {
        if (!canSplit(nrule, name, nameDelim) || StringUtils.isEmpty(name)) {
            return new String[] {name};
        }
        // "schema"."table"
        // "sch.ma"."table"
        // "sch""ma".table
        
        // Split names by object delimiter not between name delimiters
        ArrayList<String> names = new ArrayList<String>(2);
        String pname = name;

        // for each name
        int ndLen = nameDelim.length();
        while (!StringUtils.isEmpty(name)) {
            pname = splitNameCharDelimiters(name, nameDelim);
            names.add(pname);
            if ((pname.length() + ndLen) >= name.length()) {
                break;
            }
            name = name.substring(pname.length() + ndLen); 
        }
        return names.toArray(new String[names.size()]);
    }

    /**
     * Gets the first part of a name when single character delimiters are
     * in use.
     * @param pname
     * @return
     */
    private String splitNameCharDelimiters(String name, String nameDelim) {
        StringBuilder sname = new StringBuilder("");
        char ld = _config.getLeadingDelimiter().charAt(0);
        char td = _config.getTrailingDelimiter().charAt(0);
        char nd = nameDelim.charAt(0);
        int dlvl = 0;
        boolean wasLd = false;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == ld) {
                // Handle case where delimiters are the same
                if (td == ld && wasLd) {
                    dlvl--;
                    wasLd = false;
                } else {
                    wasLd = true;
                    dlvl++;
                }
            } else if (c == td) {
                dlvl--;
            } else if (c == nd) {
                if (dlvl == 0  && sname.length() > 0) {
                    return sname.toString();
                }
            }
            sname.append(c);
        }
        return sname.toString();
    }

    /**
     * Returns whether a name is double quoted
     * @return
     */
    public static boolean isDoubleQuoted(String name) {
        if (name == null || name.length() < 3) {
            return false;
        }
        return name.startsWith(DOUBLE_QUOTE) && 
               name.endsWith(DOUBLE_QUOTE);
    }

    public String delimit(IdentifierConfiguration config, IdentifierRule rule, String name) {
        return delimit(config, rule, name, false);
    }

    
    public String delimit(IdentifierRule rule, String name) {
        return delimit(_config, rule, name, false);
    }

    public String delimit(IdentifierRule rule, String name, boolean force) {
        return delimit(_config, rule, name, force);
    }
    
    public String delimit(IdentifierConfiguration config, IdentifierRule rule, String name, boolean force) {
        if (!rule.getCanDelimit() || StringUtils.isEmpty(name)) {
            return name;
        }

        if ((force && !isDelimited(config, rule, name)) || requiresDelimiters(config, rule, name)) {
            return config.getLeadingDelimiter() + name + config.getTrailingDelimiter();
        }
        return name;
    }

    public boolean isDelimited(IdentifierRule rule, String name) {
        return isDelimited(_config, rule, name);
    }

    public boolean isDelimited(IdentifierConfiguration config, IdentifierRule rule, String name) {
         if (name == null || name.length() < 3) {
            return false;
        }
        return name.startsWith(config.getLeadingDelimiter()) &&
            name.endsWith(config.getTrailingDelimiter());
    }

    public String removeDelimiters(IdentifierConfiguration config, String rule,
        String name) {
        return removeDelimiters(_config, getNamingRule(rule), name, _config.getLeadingDelimiter(),
            _config.getTrailingDelimiter());
    }

    public String removeDelimiters(IdentifierRule rule, String name) {
        return removeDelimiters(_config, rule, name, _config.getLeadingDelimiter(),
            _config.getTrailingDelimiter());
    }

    public String removeDelimiters(IdentifierConfiguration config, IdentifierRule rule, String name) {
        return removeDelimiters(config, rule, name, _config.getLeadingDelimiter(),
            _config.getTrailingDelimiter());
    }

    public boolean requiresDelimiters(IdentifierRule rule, String name) {
        return requiresDelimiters(_config, rule, name);
    }

    public boolean requiresDelimiters(IdentifierConfiguration config, IdentifierRule rule, String name) {
        if (rule == null) {
            throw new IllegalArgumentException(_loc.get("no-rules-provided").getMessage());
        }
        if (rule.getCanDelimit() && !isDelimited(config, rule, name) && rule.requiresDelimiters(name)) {
            return true;
        }
        return false;
    }
    
    /**
     * Returns whether a name is considered a reserved word.
     */
    public boolean isReservedWord(IdentifierRule rule, String name) {
        if (rule == null) {
            throw new IllegalArgumentException("Naming rule is null!");
        }
        if (rule.getReservedWords() == null) {
            return false;
        }
        if (!isDelimited(rule, name)) {
            name = name.toUpperCase();
        }
        return rule.getReservedWords().contains(name);
    }

    public boolean isReservedWord(String rule, String name) {
        return isReservedWord(_config.getIdentifierRule(rule), name);
    }
    

    protected String removeDelimiters(IdentifierConfiguration config, IdentifierRule rule, String name, String leading,
        String trailing) {
        if (name == null) {
            return null;
        }
        if (isDelimited(config, rule, name)) {
            String id = name.substring(leading.length(),
                (name.length() - trailing.length()));
            return id;
        }
        return name;
    }
    
    public String combineNames(String rule, String[] rules, String[] names) {
        return combineNames(getNamingRule(rule), getNamingRules(rules), names);
    }

    public String truncateName(String rule, String name, int length) {
        return truncateName(getNamingRule(rule), name, length);
    }

    public String truncateName(IdentifierRule namingRule, String name, int length) {
        String tName = name;
        boolean delimited = isDelimited(namingRule, name);
        if (delimited) {
            tName = removeDelimiters(namingRule, name);
        }
        if (tName.length() <= length) {
            return name;
        }
        tName = tName.substring(0, tName.length() - length);
        if (delimited) {
            tName = delimit(namingRule, tName);
        }
        return tName;
    }

    public String delimit(String rule, String name) {
        return delimit(getNamingRule(rule), name);
    }

    public String delimit(String rule, String name, boolean force) {
        return delimit(getNamingRule(rule), name, force);
    }

    public boolean isDelimited(String rule, String name) {
        return isDelimited(getNamingRule(rule), name);
    }

    public String removeDelimiters(String rule, String name) {
        return removeDelimiters(getNamingRule(rule), name);
    }

    public boolean requiresDelimiters(String rule, String name) {
        return requiresDelimiters(getNamingRule(rule), name);
    }

    public String[] splitName(IdentifierConfiguration config, String rule, String name) {
        return splitName(config, getNamingRule(rule), name);
    }

    public String[] splitName(String rule, String name) {
        return splitName(_config, getNamingRule(rule), name);
    }

    public String joinNames(String[] rules, String[] names) {
        return joinNames(getNamingRules(rules), names);
    }
    
    private IdentifierRule getNamingRule(String rule) {
        return _config.getIdentifierRule(rule);
    }
    
    public String appendNames(String rule, String name1, String name2) {
        return appendNames(getNamingRule(rule), name1, name2);
    }

    public String removeHungarianNotation(IdentifierRule rule, String name) {
        boolean delimited = isDelimited(rule, name);
        if (delimited) {
            name = removeDelimiters(rule, name);
        }
        char[] chname = name.toCharArray();
        int newStart = 0;

        for (int i = 0; i < chname.length; i++) {
            if (Character.isUpperCase(chname[i]))
            {
                newStart = i;
                break;
            }
        }

        name = name.substring(newStart);
        if (delimited) {
            name = delimit(rule, name);
        }
        return name;
    }

    public String removeHungarianNotation(String rule, String name) {
        return removeHungarianNotation(getNamingRule(rule), name);
    }
    
    public String[] splitName(String nrule, String name, String nameDelim) {
        return splitName(getNamingRule(nrule), name, nameDelim);
    }

    public String convert(IdentifierConfiguration config, String rule, String name) {
        // Already using same delimiter, no need to convert
        if (!needsConversion(config)) {
            return name;
        }
        // Otherwise, remove delimiters and add appropriate delimiters
        IdentifierRule orule = getIdentifierConfiguration().getIdentifierRule(rule);
        IdentifierRule nrule = config.getIdentifierRule(rule);
        boolean delimit = isDelimited(config, orule, name);
        if (delimit) {
            name = removeDelimiters(config, orule, name, config.getLeadingDelimiter(), 
                config.getTrailingDelimiter());
            return delimit(getIdentifierConfiguration(), nrule, name, delimit);
        }
        return name;
    }

    public String convertFull(IdentifierConfiguration config, String rule, String fullName) {
        if (!needsConversion(config)) {
            return fullName;
        }
        // Split
        String[] names = splitName(config, rule, fullName);
        // Convert
        for (int i = 0; i < names.length; i++) {
            names[i] = convert(config, rule, names[i]);
        }
        // If a single part name, return it.
        if (names.length == 1) {
            return names[0];
        }
        // Join if multiple names
        return joinNames(getIdentifierConfiguration(), config.getIdentifierRule(rule), names, 
            getIdentifierConfiguration().getIdentifierDelimiter());
    }    

    public String combineFull(IdentifierConfiguration config, String rule, String fullName) {
        if (!needsConversion(config)) {
            return fullName;
        }
        // Split
        String[] names = splitName(config, rule, fullName);
        // Convert
        for (int i = 0; i < names.length; i++) {
            names[i] = convert(config, rule, names[i]);
        }
        // Join
        return joinNames(config, config.getIdentifierRule(rule), names, config.getIdentifierDelimiter());
    }    


    protected boolean needsConversion(IdentifierConfiguration config) {
    	return (config != getIdentifierConfiguration()) 
           && !(config.getConversionKey().equals(getIdentifierConfiguration().getConversionKey()));
    }

    private IdentifierRule[] getNamingRules(String[] rules) {
        IdentifierRule[] nrules = new IdentifierRule[rules.length];
        for (int i = 0; i < rules.length; i++) {
            nrules[i] = _config.getIdentifierRule(rules[i]);
        }
        return nrules;
    }

    public void endConfiguration() {
    }

    public void setConfiguration(Configuration conf) {
    }

    public void startConfiguration() {
    }

    public boolean canSplit(String rule, String name) {
        return canSplit(getNamingRule(rule), name, _config.getIdentifierDelimiter());
    }

    public boolean canSplit(IdentifierRule rule, String name) {
        return canSplit(rule, name, _config.getIdentifierDelimiter());
    }

    public boolean canSplit(String rule, String name, String delim) {
        return canSplit(getNamingRule(rule), name);
    }

    public boolean canSplit(IdentifierRule rule, String name, String delim) {
        if (name == null || name.length() == 0) {
            return false;
        }
        return name.contains(delim);
    }

    public String combineNames(IdentifierRule rule, String[] names) {
        return combineNames(_config, rule, names);
    }

    public String combineNames(IdentifierRule rule, String name1, String name2) {
        return combineNames(_config, rule, name1, name2);
    }
}
