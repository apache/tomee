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

package org.apache.tomee.catalina.routing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Route {
    private static final String[] EMPTY_CONTEXT = new String[0];

    private final ThreadLocal<Matcher> matcher = new ThreadLocal<Matcher>();

    private Pattern originPattern;
    private String origin;
    private String destination;

    public Route from(final String value) {
        origin = value;
        originPattern = Pattern.compile(value);
        return this;
    }

    public Route to(final String value) {
        destination = value;
        return this;
    }

    public String cleanDestination(final String prefix) {
        String destination = this.destination;

        final Matcher matcher = this.matcher.get();
        if (matcher != null) {
            final String[] context = currentContext();
            for (int i = 0; i < context.length; i++) {
                destination = destination.replace("$" + (i + 1), context[i]);
            }
        }

        this.matcher.remove(); // single call to this method

        if (prefix == null) {
            return destination;
        }
        return destination.substring(prefix.length());
    }

    public String getOrigin() {
        return origin;
    }

    public boolean matches(final String uri) {
        final Matcher matcher = originPattern.matcher(uri);
        final boolean ok = matcher.matches();

        if (ok) {
            this.matcher.set(matcher);
        }

        return ok;
    }

    private String[] currentContext() {
        if (matcher.get().groupCount() > 0) {
            return buildContext(matcher.get());
        } else {
            return EMPTY_CONTEXT;
        }
    }

    private String[] buildContext(final Matcher matcher) {
        final Collection<String> values = new ArrayList<String>();
        for (int i = 1; i < matcher.groupCount() + 1; i++) {
            values.add(matcher.group(i));
        }
        return values.toArray(new String[values.size()]);
    }

    public String getRawDestination() {
        return destination;
    }
}
