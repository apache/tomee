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

import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.lib.util.SimpleRegex;

/**
 * Tests if the target matches the wildcard expression given in the
 * argument. The wildcard '?' is used to represent any single character,
 * while '*' is used to represent any series of 0 or more characters.
 *  Examples:<br />
 * <code> "address.street.ext:wildcardMatch (\"?ain*reet\")"
 * </code>
 *
 * @deprecated Use <code>matches()</code> instead.
 * @nojavadoc
 */
public class WildcardMatch
    implements FilterListener {

    public static final String TAG = "wildcardMatch";

    public String getTag() {
        return TAG;
    }

    public boolean expectsArguments() {
        return true;
    }

    public boolean expectsTarget() {
        return true;
    }

    public Object evaluate(Object target, Class targetClass, Object[] args,
        Class[] argClasses, Object candidate, StoreContext ctx) {
        if (target == null || args[0] == null)
            return Boolean.FALSE;

        // create a regexp for the wildcard expression by subbing '.' for '?'
        // and '.*' for '*'
        String wild = args[0].toString().replace('?', '.');
        for (int st = 0, i; (i = wild.indexOf("*", st)) != -1; st = i + 3)
            wild = wild.substring(0, i) + "." + wild.substring(i);

        SimpleRegex re = new SimpleRegex(wild, false);
        return (re.matches(target.toString())) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Class getType(Class targetClass, Class[] argClasses) {
        return boolean.class;
    }
}
