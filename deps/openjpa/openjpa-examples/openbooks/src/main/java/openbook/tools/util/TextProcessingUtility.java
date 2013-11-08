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
package openbook.tools.util;

public class TextProcessingUtility {
    /**
     * Replaces special characters &lt; and &gt; in a string.
     * The complexity is <em>not</em> to replace if the tags represent a hyperlink. 
     */
    public static String replaceHTMLSpecialCharacters(String txt) {
        boolean inHyperlink = false;
        StringBuilder buf = new StringBuilder();
        int L = txt.length();
        for (int i = 0; i < L; i++) {
            char ch = txt.charAt(i);
            if (!inHyperlink) {
                if (ch == '<') {
                    String ahead  = txt.substring(i, Math.min(i+3, L));
                    if (ahead.equalsIgnoreCase("<A ")) {
                        inHyperlink = true;
                        buf.append(ch);
                    } else {
                        buf.append(ch == '>' ? "&gt;" : ch == '<' ? "&lt;" : ch);
                    }
                } else {
                    buf.append(ch == '>' ? "&gt;" : ch == '<' ? "&lt;" : ch);
                }
            } else { // inside a hyperlink tag. Do not translate characters
                buf.append(ch);
                if (ch == '>') {
                    String lookback = txt.substring(Math.max(0, i-4), i);
                    if (lookback.equalsIgnoreCase("</A")) {
                        inHyperlink = false;
                    }
                }
            }
        }
        return buf.toString();
    }

}
