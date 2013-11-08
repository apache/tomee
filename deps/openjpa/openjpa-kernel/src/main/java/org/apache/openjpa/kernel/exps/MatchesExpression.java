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
package org.apache.openjpa.kernel.exps;

import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.SimpleRegex;
import serp.util.Strings;

/**
 * Expression that compares two others.
 *
 * @author Abe White
 */
class MatchesExpression
    extends CompareExpression {

    private static final Localizer _loc = Localizer.forPackage(
        MatchesExpression.class);
    
    private final String _single;
    private final String _multi;
    private final boolean _affirmation;

    /**
     * Constructor. Supply values to compare.
     */
    public MatchesExpression(Val val1, Val val2,
        String single, String multi, String escape, boolean affirmation) {
        super(val1, val2);
        _single = single;
        _multi = multi;
        if (escape != null)
            throw new IllegalArgumentException(_loc.get(
                "escape-for-inmem-query-not-supported").getMessage());
        _affirmation = affirmation;
    }

    protected boolean compare(Object o1, Object o2) {
        if (o1 == null || o2 == null)
            return false;

        // case insensitive?
        String str = o2.toString();
        int idx = str.indexOf("(?i)");
        boolean uncase = false;
        if (idx != -1) {
            uncase = true;
            if (idx + 4 < str.length())
                str = str.substring(0, idx) + str.substring(idx + 4);
            else
                str = str.substring(0, idx);
        }

        // now translate from the single and multi character escape
        // sequences into an escape that conforms to the regexp syntax
        str = Strings.replace(str, _multi, ".*");
        str = Strings.replace(str, _single, ".");

        SimpleRegex re = new SimpleRegex(str, uncase);
        boolean matches = re.matches(o1.toString());
        return _affirmation ? matches : !matches;
    }
}

