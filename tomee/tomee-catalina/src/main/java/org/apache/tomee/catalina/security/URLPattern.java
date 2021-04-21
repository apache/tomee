/*
 * Copyright 2018 OmniFaces.
 * Copyright 2003-2011 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.catalina.security;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Guillermo González de Agüero
 */

/**
 * Utility class for <code>ModuleConfiguration</code>.  This class is used to generate qualified patterns, HTTP
 * method sets, complements of HTTP method sets, and HTTP method sets w/ transport restrictions for URL patterns that
 * are found in the web deployment descriptor.
 *
 * @version $Rev$ $Date$
 */
public class URLPattern {
    public final static int NA = 0x00;
    public final static int INTEGRAL = 0x01;
    public final static int CONFIDENTIAL = 0x02;

    private final URLPatternCheck type;
    private final String pattern;
    private final HTTPMethods httpMethods;
    private int transport;

    /**
     * Construct an instance of the utility class for <code>WebModuleConfiguration</code>.
     *
     * @param pat the URL pattern that this instance is to collect information on
     * @see "JSR 115, section 3.1.3" Translating Servlet Deployment Descriptors
     */
    public URLPattern(String pat, Set<String> methods, boolean isHttpMethodExcluded) {
        if (pat == null)
            throw new IllegalArgumentException("URL pattern cannot be null");
        if (pat.length() == 0)
            throw new IllegalArgumentException("URL pattern cannot be empty");
        if (pat.equals("/") || pat.equals("/*")) {
            type = DEFAULT;
            pat = "/";
        } else if (pat.charAt(0) == '/' && pat.endsWith("/*")) {
            type = PATH_PREFIX;
        } else if (pat.charAt(0) == '*') {
            type = EXTENSION;
        } else {
            type = EXACT;
        }
        pattern = pat;
        httpMethods = new HTTPMethods(methods, isHttpMethodExcluded);
    }

    /**
     * Get a qualifed URL pattern relative to a particular set of URL patterns.  This algorithm is described in
     * JSR 115, section 3.1.3.1 "Qualified URL Pattern Names".
     *
     * @param patterns the set of possible URL patterns that could be used to qualify this pattern
     * @return a qualifed URL pattern
     */
    public String getQualifiedPattern(Set<URLPattern> patterns) {
        if (type == EXACT) {
            return pattern;
        } else {
            HashSet<String> bucket = new HashSet<String>();
            StringBuilder result = new StringBuilder(pattern);

            // Collect a set of qualifying patterns, depending on the type of this pattern.
            for (URLPattern p : patterns) {
                if (type.check(this, p)) {
                    bucket.add(p.pattern);
                }
            }
            // append the set of qualifying patterns
            for (String aBucket : bucket) {
                result.append(':');
                result.append(aBucket);
            }
            return result.toString();
        }
    }

    /**
     * Add a method to the union of HTTP methods associated with this URL pattern.  An empty Set  is short hand for
     * the set of all HTTP methods.
     *
     * @param methods the HTTP methods to be added to the set.
     */
    public void addMethods(Set<String> methods, boolean isExcluded) {
        httpMethods.add(methods, isExcluded);
    }

    public boolean removeMethods(URLPattern other) {
        return httpMethods.remove(other.getHTTPMethods()) != null;
    }

    /**
     * Return the set of HTTP methods that have been associated with this URL pattern.
     *
     * @return a set of HTTP methods
     */
    public String getMethods() {
        return httpMethods.getHttpMethods();
    }


    public String getComplementedMethods() {
        return httpMethods.getComplementedHttpMethods();
    }

    public HTTPMethods getHTTPMethods() {
        return httpMethods;
    }

    public HTTPMethods getComplementedHTTPMethods() {
        return new HTTPMethods(httpMethods, true);
    }

    public String getMethodsWithTransport() {
        return getMethodsWithTransport(httpMethods, transport);
    }

    public static String getMethodsWithTransport(HTTPMethods methods, int transport) {
        StringBuilder buffer = methods.getHttpMethodsBuffer();


        if (transport != NA) {
            buffer.append(":");

            if (transport != 0x03) {
                if (transport == INTEGRAL) {
                    buffer.append("INTEGRAL");
                } else {
                    buffer.append("CONFIDENTIAL");
                }
            }
        }

        return buffer.toString();
    }

    public void setTransport(String trans) {
        switch (transport) {
            case NA: {
                if ("INTEGRAL".equals(trans)) {
                    transport = INTEGRAL;
                } else if ("CONFIDENTIAL".equals(trans)) {
                    transport = CONFIDENTIAL;
                }
                break;
            }

            case INTEGRAL: {
                if ("CONFIDENTIAL".equals(trans)) {
                    transport = CONFIDENTIAL;
                }
                break;
            }
        }
    }

    public int getTransport() {
        return transport;
    }

    /**
     * TODO this is kinda weird without an explanation
     * @param obj object to compare with
     * @return if this equals obj
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof URLPattern)) return false;

        URLPattern test = (URLPattern) obj;

        return pattern.equals(test.pattern);
    }

    public int hashCode() {
        return pattern.hashCode();
    }

    boolean matches(URLPattern p) {
        String test = p.pattern;

        // their pattern values are String equivalent
        if (pattern.equals(test)) return true;

        return type.matches(pattern, test);
    }

    private final static URLPatternCheck EXACT = new URLPatternCheck() {
        public boolean check(URLPattern base, URLPattern test) {
            return matches(base.pattern, test.pattern);
        }

        public boolean matches(String base, String test) {
            return base.equals(test);
        }
    };

    private final static URLPatternCheck PATH_PREFIX = new URLPatternCheck() {
        public boolean check(URLPattern base, URLPattern test) {
            return ((test.type == PATH_PREFIX || test.type == EXACT)
                    && base.matches(test)
                    && !base.equals(test));
        }

        /**
         * This pattern is a path-prefix pattern (that is, it starts with "/" and ends with "/*") and the argument
         * pattern starts with the substring of this pattern, minus its last 2 characters, and the next character of
         * the argument pattern, if there is one, is "/"
         *
         * @param base the base pattern
         * @param test the pattern to be tested
         * @return <code>true</code> if <code>test</code> is matched by <code>base</code>
         */
        public boolean matches(String base, String test) {
            int length = base.length() - 2;
            if (length > test.length()) return false;

            for (int i = 0; i < length; i++) {
                if (base.charAt(i) != test.charAt(i)) return false;
            }

            if (test.length() == length)
                return true;
            else if (test.charAt(length) != '/') return false;

            return true;
        }
    };

    private final static URLPatternCheck EXTENSION = new URLPatternCheck() {
        public boolean check(URLPattern base, URLPattern test) {
            if (test.type == PATH_PREFIX) return true;

            if (test.type == EXACT) return matches(base.pattern, test.pattern);

            return false;
        }

        /**
         * This pattern is an extension pattern (that is, it startswith "*.") and the argument pattern ends with
         * this pattern.
         *
         * @param base the base pattern
         * @param test the pattern to be tested
         * @return <code>true</code> if <code>test</code> is matched by <code>base</code>
         */
        public boolean matches(String base, String test) {
            return test.endsWith(base.substring(1));
        }
    };

    private final static URLPatternCheck DEFAULT = new URLPatternCheck() {
        public boolean check(URLPattern base, URLPattern test) {
            return base.matches(test) && !base.equals(test);
        }

        /**
         * This pattern is the path-prefix pattern "/*" or the reference pattern is the special default pattern,
         * "/", which matches all argument patterns.
         *
         * @param base the base pattern
         * @param test the pattern to be tested
         * @return <code>true</code> if <code>test</code> is matched by <code>base</code>
         * @see "JSR 115"
         */
        public boolean matches(String base, String test) {
            return true;
        }
    };
}