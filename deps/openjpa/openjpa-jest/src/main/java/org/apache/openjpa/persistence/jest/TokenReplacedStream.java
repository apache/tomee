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

package org.apache.openjpa.persistence.jest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;

/**
 * Reads from an input stream and writes to an output stream after replacing matched tokens
 * by their counterpart.
 * 
 *  
 * @author Pinaki Poddar
 *
 */
public class TokenReplacedStream {
    /**
     * Read the given input stream and replaces the tokens as it reads. The replaced stream is written to the
     * given output stream.
     * 
     * @param in a non-null input stream
     * @param out a character oriented writer
     * @param replacements an even number of Strings. Any occurrence of the even-indexed i-th String in the
     * input stream will be replaced by the (i+1)-th String in the output writer. 
     */
    public void replace(InputStream in, Writer out, String... prs) throws IOException {
        BufferedReader inRdr = new BufferedReader(new InputStreamReader(in));
        replace(inRdr, out, prs);
    }

    public void replace(Reader in, Writer out, String... prs) throws IOException {
        if (prs.length%2 != 0) 
            throw new IllegalArgumentException("Even number of pattern/string pairs: " + Arrays.toString(prs) 
                + ". Must be even number of arguments.");
        Pattern[] patterns = new Pattern[prs.length/2];
        for (int i = 0; i < prs.length; i += 2) {
            patterns[i/2] = new Pattern(prs[i], prs[i+1]);
        }
        
        StringBuilder tmp = new StringBuilder();
        for (int c = 0; (c = in.read()) != -1;) {
            int cursor = match((char)c, patterns);
            if (cursor < 0) { // no pattern recognized at all
                if (tmp.length() > 0) { // append  partial match then discard partial memory
                    for (int j = 0; j < tmp.length(); j++) {
                        out.write(tmp.charAt(j));
                    }
                    tmp.delete(0, tmp.length());
                } 
                out.write((char)c); // directly output
            } else {
                Pattern p = matched(patterns); // has any pattern matched completely
                if (p != null) { // a pattern matched completely
                    char[] replace = p.replace().toCharArray();
                    for (int j = 0; j < replace.length; j++) {
                        out.write(replace[j]);
                    }
                    reset(patterns);
                    tmp.delete(0, tmp.length());
                } else {
                    tmp.append((char)c); // remember partial match
                }
            }
        }
    }

    /**
     * Match the given character to all patterns and return the index of highest match. 
     * @param c a character to match
     * @param patterns an array of patterns
     * @return -1 if character matched no pattern 
     */
    int match(char c, Pattern...patterns) {
        if (patterns == null)
            return -1;
        int result = -1;
        for (Pattern p : patterns) {
            result = Math.max(result, p.match(c));
        }
        return result;
    }
    
    /**
     * Gets the pattern if any in matched state
     * @param patterns
     * @return
     */
    Pattern matched(Pattern...patterns) {
        if (patterns == null)
            return null;
        for (Pattern p : patterns) {
            if (p.isMatched()) return p;
        }
        return null;
    }
    
    /**
     * Resets all the patterns.
     * @param patterns
     */
    void reset(Pattern...patterns) {
        if (patterns == null)
            return;
        for (Pattern p : patterns) {
            p.reset();
        }
    }
    
    public static class Pattern {
        private final char[] chars;
        private final String _replace;
        private int _cursor;
        
        /**
         * Construct a pattern and its replacement.
         */
        public Pattern(String s, String replace) {
            if (s == null || s.length() == 0)
                throw new IllegalArgumentException("Pattern [" + s + "] can not be empty or null ");
            if (replace == null)
                throw new IllegalArgumentException("Replacement [" + replace + "] is null for pattern [" + s + "]");
            chars = s.toCharArray();
            _cursor = -1;
            _replace = replace;
        }
        
        /**
         * Match the given character with the current cursor and advance the matching length.
         * @param c
         * @return the matching length. -1 denotes the pattern did not match the character.
         */
        public int match(char c) {
            if (c != chars[++_cursor]) {
                reset();
            }
            return _cursor;
        }
        
        /**
         * Reset the cursor. Subsequent matching will begin at start.
         */
        public void reset() {
            _cursor = -1;
        }    
        
        /**
         * Is this pattern matched fully?
         * A pattern is fully matched when the matching length is equal to the length of the pattern string.
         */
        public boolean isMatched() {
            return _cursor == chars.length-1;
        }
        
        /**
         * Gets the string to be replaced.
         */
        public String replace() {
            return _replace;
        }
        
        public String toString() {
            return new String(chars) + ":" + _cursor;
        }
    }

}
