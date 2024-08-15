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

import java.util.Map;

public class ApplicationThreadContextProvider implements ThreadContextProvider {
    @Override
    public ThreadContextSnapshot currentContext(final Map<String, String> props) {
        return new ApplicationThreadContextSnapshot(Thread.currentThread().getContextClassLoader());
    }

    @Override
    public ThreadContextSnapshot clearedContext(final Map<String, String> props) {
        return ApplicationThreadContextSnapshot.DO_NOTHING;
    }

    @Override
    public String getThreadContextType() {
        return ContextServiceDefinition.APPLICATION;
    }

    public static class ApplicationThreadContextSnapshot implements ThreadContextSnapshot {
        public static final ApplicationThreadContextSnapshot DO_NOTHING = new ApplicationThreadContextSnapshot(null);

        private final ClassLoader classLoader;

        public ApplicationThreadContextSnapshot(final ClassLoader classLoader) {
            this.classLoader = classLoader;
        }

        @Override
        public ThreadContextRestorer begin() {
            if (classLoader == null) {
                return ApplicationThreadContextRestorer.DO_NOTHING;
            }

            final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);
            return new ApplicationThreadContextRestorer(oldClassLoader);
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
