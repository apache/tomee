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
import org.apache.openejb.core.ThreadContext;

import java.util.Map;

public class TxThreadContextProvider implements ThreadContextProvider {
    @Override
    public ThreadContextSnapshot currentContext(final Map<String, String> props) {
        // TODO: is there anything we need to mess around with here? ClassLoader?
        return new TxThreadContextSnapshot(ThreadContext.getThreadContext());
    }

    @Override
    public ThreadContextSnapshot clearedContext(final Map<String, String> props) {
        return new TxThreadContextSnapshot(null);
    }

    @Override
    public String getThreadContextType() {
        return ContextServiceDefinition.TRANSACTION;
    }

    public class TxThreadContextSnapshot implements ThreadContextSnapshot {

        private final ThreadContext threadContext;

        public TxThreadContextSnapshot(final ThreadContext threadContext) {
            this.threadContext = threadContext;
        }

        @Override
        public ThreadContextRestorer begin() {
            final ThreadContext restoreContext = (threadContext == null) ?
                    ThreadContext.clear() :
                    ThreadContext.enter(threadContext);

            return new TxThreadContextRestorer(restoreContext);
        }
    }

    public class TxThreadContextRestorer implements ThreadContextRestorer {

        private final ThreadContext restoreContext;

        public TxThreadContextRestorer(final ThreadContext restoreContext) {
            this.restoreContext = restoreContext;
        }

        @Override
        public void endContext() throws IllegalStateException {
            if (restoreContext != null) {
                ThreadContext.exit(restoreContext);
            }
        }
    }
}
