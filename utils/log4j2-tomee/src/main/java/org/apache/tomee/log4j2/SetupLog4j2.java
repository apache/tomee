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
package org.apache.tomee.log4j2;

import org.apache.openejb.assembler.classic.event.AssemblerCreated;
import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.util.classloader.URLClassLoaderFirst;
import org.apache.tomee.loader.TomcatHelper;

import java.util.Collection;

public class SetupLog4j2 {
    public void setup(@Observes final AssemblerCreated initEvent) {
        try {
            ParentClassLoaderFinder.Helper.get().loadClass("org.apache.logging.log4j.core.util.ShutdownCallbackRegistry");
            doSetup();
        } catch (final ClassNotFoundException e) {
            // no-op
        }
    }

    private void doSetup() {
        // org.apache.openejb.log4j2.CaptureLog4j2ShutdownHooks is likely int the container so just skip the API, luckily it has no dep :)
        final Collection<String> forcedSkip = URLClassLoaderFirst.FORCED_SKIP;
        forcedSkip.add("org.apache.logging.log4j.api.");
        forcedSkip.add("org.apache.logging.log4j.core.");

        TomcatHelper.getServer().addLifecycleListener(new Log4j2ShutdownHooksExecutor());
    }
}
