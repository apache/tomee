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

import org.apache.catalina.LifecycleException;
import org.apache.catalina.loader.WebappClassLoader;
import org.apache.openejb.loader.SystemInstance;

public class LazyStopWebappClassLoader extends WebappClassLoader {
    public static final String TOMEE_WEBAPP_FIRST = "tomee.webapp-first";

    private boolean restarting;

    public LazyStopWebappClassLoader() {
        setDelegate(!SystemInstance.get().getOptions().get(TOMEE_WEBAPP_FIRST, true));
    }

    public LazyStopWebappClassLoader(final ClassLoader parent) {
        super(parent);
    }

    @Override
    public void stop() throws LifecycleException {
        // in our destroyapplication method we need a valid classloader to TomcatWebAppBuilder.afterStop()
        // exception: restarting we really stop it for the moment
        if (restarting) {
            internalStop();
        }
    }

    public void internalStop() throws LifecycleException {
        if (isStarted()) {
            super.stop();
        }
    }

    public void restarting() {
        restarting = true;
    }

    public void restarted() {
        restarting = false;
    }

    public boolean isRestarting() {
        return restarting;
    }
}
