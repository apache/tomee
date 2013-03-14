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
import org.apache.catalina.loader.VirtualWebappLoader;

public class LazyStopWebappLoader extends VirtualWebappLoader {
    private static final ThreadLocal<String> currentAppId = new ThreadLocal<String>();

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
        currentAppId.set(getContainer().getName()); // needed by classloader instantiated by next line
        try {
            super.startInternal();
        } finally {
            currentAppId.remove();
        }
    }

    public static String getCurrentAppId() {
        final String id = currentAppId.get();
        if (id == null) {
            currentAppId.remove();
        }
        return id;
    }

    public String getAppId() {
        if (getContainer() == null) {
            return null;
        }
        return getContainer().getName();
    }

    @Override
    public String toString() {
        return "LazyStop" + super.toString();
    }
}
