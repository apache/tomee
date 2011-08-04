/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.core.ivm.naming;

import java.util.Arrays;

public class ParsedName implements java.io.Serializable {
    final static int IS_EQUAL = 0;
    final static int IS_LESS = -1;
    final static int IS_GREATER = 1;

    String [] components;
    int pos = 0;
    int hashcode;

    public ParsedName(String path) {
        path = normalize(path);

        if (path == null || path.equals("/")) {

            components = new String[1];
            components[0] = "";
            hashcode = 0;
        } else if (path.length() > 0) {
            java.util.StringTokenizer st = new java.util.StringTokenizer(path, "/");
            components = new String[st.countTokens()];
            for (int i = 0; st.hasMoreTokens() && i < components.length; i++)
                components[i] = st.nextToken();
            hashcode = components[0].hashCode();
        } else {

            components = new String[]{""};
            hashcode = 0;
        }
    }

    public String getComponent() {
        return components[pos];
    }

    public boolean next() {
        if (components.length > pos + 1) {
            hashcode = components[++pos].hashCode();
            return true;
        } else {
            return false;// maintain position
        }
    }

    public void reset() {
        pos = 0;
        hashcode = components[0].hashCode();
    }

    public int getPos() {
        return pos;
    }

    public void reset(int pos) {
        if (pos < 0 || pos >= components.length) throw new IllegalArgumentException("pos out of range 0 to " + components.length);
        this.pos = pos;
        hashcode = components[pos].hashCode();
    }

    public int compareTo(int otherHash) {
        if (hashcode == otherHash)
            return 0;
        else if (hashcode > otherHash)
            return 1;
        else
            return -1;
    }

    public int getComponentHashCode() {
        return hashcode;
    }

    public int compareTo(String other) {
        int otherHash = other.hashCode();
        return compareTo(otherHash);
    }

    public static void main(String [] args) {

        ParsedName name = new ParsedName("comp/env/jdbc/mydatabase");
        while (name.next()) System.out.println(name.getComponent());
    }

    public ParsedName remaining() {
        ParsedName name = new ParsedName("");
        int next = pos +1;
        if (next > components.length) return name;
        
        String[] dest = new String[components.length - next];
        System.arraycopy(components, next, dest, 0, dest.length);
        name.components = dest;

        return name;
    }

    @Override
    public String toString() {
        return "ParsedName{" +
                "path=" + path() +
                ", component=" + getComponent() +
                '}';
    }

    public String path() {
        if (components.length == 0) {
            return "";
        }
        StringBuffer buffer = new StringBuffer(components[0]);
        for (int i = 1; i < components.length; ++i) {
            buffer.append('/');
            buffer.append(components[i]);
        }
        return buffer.toString();
    }

    /* A normal Unix pathname contains no duplicate slashes and does not end
with a slash.  It may be the empty string. */

    /* Normalize the given pathname, whose length is len, starting at the given
       offset; everything before this offset is already normal. */
    private String normalize(String pathname, int len, int off) {
        if (len == 0) return pathname;
        int n = len;
        while ((n > 0) && (pathname.charAt(n - 1) == '/')) n--;
        if (n == 0) return "/";
        StringBuffer sb = new StringBuffer(pathname.length());
        if (off > 0) sb.append(pathname.substring(0, off));
        char prevChar = 0;
        for (int i = off; i < n; i++) {
            char c = pathname.charAt(i);
            if ((prevChar == '/') && (c == '/')) continue;
            sb.append(c);
            prevChar = c;
        }
        return sb.toString();
    }

    /* Check that the given pathname is normal.  If not, invoke the real
       normalizer on the part of the pathname that requires normalization.
       This way we iterate through the whole pathname string only once. */
    private String normalize(String pathname) {
        int n = pathname.length();
        char prevChar = 0;
        for (int i = 0; i < n; i++) {
            char c = pathname.charAt(i);
            if ((prevChar == '/') && (c == '/'))
                return normalize(pathname, n, i - 1);
            prevChar = c;
        }
        if (prevChar == '/') return normalize(pathname, n, n - 1);
        return pathname;
    }

}
