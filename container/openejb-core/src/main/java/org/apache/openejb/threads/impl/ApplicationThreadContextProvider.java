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
package org.apache.openejb.threads.impl;

import jakarta.enterprise.concurrent.ContextServiceDefinition;
import jakarta.enterprise.concurrent.spi.ThreadContextProvider;
import jakarta.enterprise.concurrent.spi.ThreadContextRestorer;
import jakarta.enterprise.concurrent.spi.ThreadContextSnapshot;
import org.apache.openejb.AppContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.AppFinder;

import java.io.Serializable;
import java.util.Map;

public class ApplicationThreadContextProvider implements ThreadContextProvider, Serializable {
    public static final ApplicationThreadContextProvider INSTANCE = new ApplicationThreadContextProvider();

    @Override
    public ThreadContextSnapshot currentContext(final Map<String, String> props) {
        AppContext appContext = AppFinder.findAppContextOrWeb(Thread.currentThread().getContextClassLoader(), AppFinder.AppContextTransformer.INSTANCE);
        if (appContext == null) {
            return clearedContext(props);
        }

        return new ApplicationThreadContextSnapshot(appContext.getId());
    }

    @Override
    public ThreadContextSnapshot clearedContext(final Map<String, String> props) {
        return ApplicationThreadContextSnapshot.DO_NOTHING;
    }

    @Override
    public String getThreadContextType() {
        return ContextServiceDefinition.APPLICATION;
    }

    public static class ApplicationThreadContextSnapshot implements ThreadContextSnapshot, Serializable {
        public static final ApplicationThreadContextSnapshot DO_NOTHING = new ApplicationThreadContextSnapshot(null);

        private final Object appId;

        public ApplicationThreadContextSnapshot(final Object appId) {
            this.appId = appId;
        }

        @Override
        public ThreadContextRestorer begin() {
            if (appId == null) {
                return ApplicationThreadContextRestorer.DO_NOTHING;
            }

            final AppContext appContext = SystemInstance.get().getComponent(ContainerSystem.class).getAppContext(appId);
            final ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(appContext.getClassLoader());
            return new ApplicationThreadContextRestorer(oldCl);
        }
    }

    public static class ApplicationThreadContextRestorer implements ThreadContextRestorer {
        public static final ApplicationThreadContextRestorer DO_NOTHING = new ApplicationThreadContextRestorer(null);

        private final ClassLoader oldClassLoader;

        public ApplicationThreadContextRestorer(final ClassLoader oldClassLoader) {
            this.oldClassLoader = oldClassLoader;
        }

        @Override
        public void endContext() throws IllegalStateException {
            if (oldClassLoader != null) {
                Thread.currentThread().setContextClassLoader(oldClassLoader);
            }
        }
    }
}
