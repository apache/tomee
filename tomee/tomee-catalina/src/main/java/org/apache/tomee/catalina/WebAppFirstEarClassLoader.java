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
package org.apache.tomee.catalina;

import org.apache.catalina.LifecycleException;

// a TomEEWebappClassLoader enforcing webapp first classloading in ears
public class WebAppFirstEarClassLoader extends TomEEWebappClassLoader {
    static {
        ClassLoader.registerAsParallelCapable();
    }

    private String[] forceSkip;

    public WebAppFirstEarClassLoader() {
        super();
    }

    public WebAppFirstEarClassLoader(final ClassLoader parent) {
        super(parent);
    }

    @Override
    public void start() throws LifecycleException {
        super.start();
    }

    @Override
    public Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        if (forceSkip != null) {
            for (final String p : forceSkip) {
                if (name.startsWith(p)) {
                    return getParent().loadClass(name);
                }
            }
        }
        return super.loadClass(name, resolve);
    }

    @Override
    protected boolean defaultEarBehavior() {
        return false;
    }

    void setForceSkip(final String[] forceSkip) {
        this.forceSkip = forceSkip;
    }
}
