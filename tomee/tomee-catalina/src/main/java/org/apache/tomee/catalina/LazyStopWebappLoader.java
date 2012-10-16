/**
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
package org.apache.tomee.catalina;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.loader.WebappLoader;

public class LazyStopWebappLoader extends WebappLoader {
    private static String currentAppId = null;

    private Context standardContext = null;

    public LazyStopWebappLoader(final Context ctx) {
        super(ctx.getParentClassLoader());
        standardContext = ctx;
    }

    public LazyStopWebappLoader() {
        // no-op
    }

    @Override
    public void backgroundProcess() {
        final ClassLoader classloader = super.getClassLoader();
        if (classloader instanceof LazyStopWebappClassLoader) {
            final LazyStopWebappClassLoader lazyStopWebappClassLoader = (LazyStopWebappClassLoader) classloader;
            lazyStopWebappClassLoader.restarting();
            try {
                super.backgroundProcess();
            } finally {
                lazyStopWebappClassLoader.restarted();
            }
        } else {
            super.backgroundProcess();
        }
    }

    @Override
    protected synchronized void startInternal() throws LifecycleException {
        currentAppId = standardContext.getName(); // needed by classloader instantiated by next line
        try {
            super.startInternal();
        } finally {
            currentAppId = null;
        }

        if (getClassLoader() instanceof LazyStopWebappClassLoader) {
            ((LazyStopWebappClassLoader) getClassLoader()).setRelatedContext(standardContext);
        }
    }

    public static String getCurrentAppId() {
        return currentAppId;
    }

    public String getAppId() {
        if (standardContext == null) {
            return null;
        }
        return standardContext.getName();
    }
}
