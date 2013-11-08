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
package org.apache.openjpa.lib.util;

/**
 * Allows for simple regex style testing of strings. The wildcard '.'
 * is used to represent any single character, while '.*' is used to represent
 * any series of 0 or more characters. Examples:<br />
 * <code> SimpleRegex re = new SimpleRegex("the qu.ck .* dog", true);
 * boolean matches = re.matches("The quick fox jumped over the lazy dog");
 * </code>
 *
 * @nojavadoc
 */
public class SimpleRegex {

    private final String expr;
    private final boolean caseInsensitive;

    public SimpleRegex(String expr, boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;

        // If we're case insensitive, toLowerCase the expr.  We'll toLowerCase
        // each target, too, in the matches call.
        if (caseInsensitive)
            this.expr = expr.toLowerCase();
        else
            this.expr = expr;
    }

    public boolean matches(String target) {
        // If we're case insensitive, toLowerCase the target
        if (caseInsensitive)
            target = target.toLowerCase();

        // By default, we are not position independent("mobile"). We only
        // become position independent once we hit our first ".*".
        boolean mobile = false;

        // Find occurrences of ".*" in the expression.
        int exprPos = 0;
        int targetPos = 0;
        while (true) {
            // Find the next occurrence of ".*"
            int star = expr.indexOf(".*", exprPos);

            // If we're at a ".*" now, simply skip it and become position
            // independent
            if (star == exprPos) {
                mobile = true;
                exprPos += 2;
                continue;
            }
            // If there are no more ".*"s, then we're effectively no longer
            // position independent(assuming we even were before), since
            // we must match the end of the string
            else if (star == -1) {
                int len = expr.length() - exprPos;

                // If we're not mobile, then the remainder of the expr and
                // target must be the same length.  If not, then it's not a
                // match.  If we're mobile, then the length doesn't have to
                // be the same as long as the remainder of the expression
                // is equal to the end of the target
                if (!mobile && targetPos != target.length() - len)
                    return false;

                // In anycase, the remaining length of the target must be
                // at least as long as the remaining length of the expression.
                // (We check now to avoid sending a negative start pos to
                // indexOf)
                if (target.length() < len)
                    return false;

                // Match the end of the target to the remainder of the
                // expression
                int match = indexOf(target, target.length() - len, exprPos,
                    len, true);
                if (match != -1)
                    return true;
                return false;
            }

            // Match the fragment of the expression to the target
            int match = indexOf(target, targetPos, exprPos,
                star - exprPos, !mobile);
            if (match == -1)
                return false;
            targetPos = match + star - exprPos;
            exprPos = star + 2;
            mobile = true;
        }
    }

    /**
     * Match a section of target to a fragment of the expression.
     * If we're only to match the beginning of the target, beginOnly
     * will be true, otherwise we can match anymore in the target(starting
     * at the targetStart position). A "." in the expression matches any
     * character.
     */
    private int indexOf(String target, int targetStart,
        int exprStart, int exprLength, boolean beginOnly) {
        // Run through the target seeing if there is a match
        while (target.length() - targetStart >= exprLength) {
            // Assume success.  If there isn't a match we'll break out
            boolean found = true;
            for (int i = 0; i < exprLength; i++) {
                // "." in the expr matches any character in the target
                if (expr.charAt(exprStart + i) != '.' &&
                    expr.charAt(exprStart + i) !=
                        target.charAt(targetStart + i)) {
                    found = false;
                    break;
                }
            }
            if (found)
                return targetStart;

            // If we're position dependent(beginOnly == true), then don't
            // continue the search
            if (beginOnly)
                return -1;

            targetStart++;
        }
        return -1;
    }
}
