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
package org.apache.tomee.catalina.naming.resources;

import org.apache.catalina.core.StandardContext;
import org.apache.naming.resources.FileDirContext;

import java.io.File;

// we need a FileDirContext (so doesn't work with not exploded wars) for boot time
// note lifecycle is a quick one mainly used internally, other listeners are not intended to be used
public class EmptyDirContext extends FileDirContext {

    private final StandardContext context;

    public EmptyDirContext(final StandardContext standardContext) {
        this.context = standardContext;
    }

    public StandardContext getContext() {
        return context;
    }

    @Override
    public boolean isCached() {
        return false;
    }

    @Override
    public void setCached(final boolean cached) {
        // no-op
    }

    @Override
    protected File file(final String name) {
        if (shouldLookup(name)) {
            return super.file(name);
        }
        return null;
    }

    private static boolean shouldLookup(final String name) {
        return name != null && !name.equals("/WEB-INF/classes")
                && (name.matches("/?WEB-INF/[^/]*\\.?[^/]")
                || name.startsWith("/WEB-INF/lib") || name.startsWith("WEB-INF/lib")
                || name.startsWith("/META-INF/"));
    }
}
