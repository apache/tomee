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
import org.apache.openejb.core.ThreadContext;
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

        return new ApplicationThreadContextSnapshot(appContext.getId(), ThreadContext.getThreadContext());
    }

    @Override
    public ThreadContextSnapshot clearedContext(final Map<String, String> props) {
        return ThreadContextProviderUtil.NOOP_SNAPSHOT;
    }

    @Override
    public String getThreadContextType() {
        return ContextServiceDefinition.APPLICATION;
    }

    public static class ApplicationThreadContextSnapshot implements ThreadContextSnapshot, Serializable {
        private final Object appId;
        private final ThreadContext threadContext;

        public ApplicationThreadContextSnapshot(final Object appId, final ThreadContext threadContext) {
            this.appId = appId;
            this.threadContext = threadContext;
        }

        @Override
        public ThreadContextRestorer begin() {
            if (appId == null) {
                return ThreadContextProviderUtil.NOOP_RESTORER;
            }

            final AppContext appContext = SystemInstance.get().getComponent(ContainerSystem.class).getAppContext(appId);
            final ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(appContext.getClassLoader());

            // Don't touch ThreadContext if it is already correct or none was captured
            boolean changeThreadContext = threadContext != null && threadContext != ThreadContext.getThreadContext();
            ThreadContext oldThreadContext = changeThreadContext ? ThreadContext.enter(new ThreadContext(threadContext), false) : null;
            return new ApplicationThreadContextRestorer(oldCl, oldThreadContext, changeThreadContext);
        }

        @Override
        public String toString() {
            return "ApplicationThreadContextSnapshot@" + System.identityHashCode(this) +
                    "{appId=" + appId +
                    "{threadContext=" + threadContext +
                    '}';
        }

    }

    public static class ApplicationThreadContextRestorer implements ThreadContextRestorer {
        private final ClassLoader oldClassLoader;
        private final ThreadContext oldThreadContext;
        private final boolean exitThreadContext;

        public ApplicationThreadContextRestorer(final ClassLoader oldClassLoader, final ThreadContext oldThreadContext, boolean exitThreadContext) {
            this.oldClassLoader = oldClassLoader;
            this.oldThreadContext = oldThreadContext;
            this.exitThreadContext = exitThreadContext;
        }

        @Override
        public void endContext() throws IllegalStateException {
            if (oldClassLoader != null) {
                Thread.currentThread().setContextClassLoader(oldClassLoader);
            }

            if (exitThreadContext) {
                ThreadContext.exit(oldThreadContext);
            }
        }

        @Override
        public String toString() {
            return "ApplicationThreadContextRestorer@" + System.identityHashCode(this) +
                    "{oldClassLoader=" + oldClassLoader +
                    "{oldThreadContext=" + oldThreadContext +
                    "{exitThreadContext=" + exitThreadContext +
                    '}';
        }

    }
}
