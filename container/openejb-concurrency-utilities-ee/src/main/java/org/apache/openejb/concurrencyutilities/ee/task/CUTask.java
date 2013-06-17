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
package org.apache.openejb.concurrencyutilities.ee.task;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;

import java.util.concurrent.Callable;

public abstract class CUTask<T> extends ManagedTaskListenerTask {
    private static final SecurityService<?> SECURITY_SERVICE = SystemInstance.get().getComponent(SecurityService.class);

    private final Context initialContext;

    public CUTask(final Object task) {
        super(task);

        initialContext = new Context(SECURITY_SERVICE.currentState(), ThreadContext.getThreadContext(), Thread.currentThread().getContextClassLoader());
    }

    protected T invoke(final Callable<T> call) throws Exception{
        initialContext.enter();

        Throwable throwable = null;
        try {
            taskStarting(future, executor, delegate); // do it in try to avoid issues if an exception is thrown
            return call.call();
        } catch (final Throwable t) {
            throwable = t;
            taskAborted(throwable);
            return rethrow(t);
        } finally {
            taskDone(future, executor, delegate, throwable);

            initialContext.exit();
        }
    }

    private T rethrow(final Throwable t) throws Exception {
        if (Exception.class.isInstance(t)) {
            throw Exception.class.cast(t);
        } else if (Error.class.isInstance(t)) {
            throw Error.class.cast(t);
        }
        throw new OpenEJBRuntimeException(t.getMessage(), t);
    }

    private static class Context {
        private final Object securityServiceState;
        private final ThreadContext threadContext;
        private final ClassLoader loader;

        private Context currentContext = null;

        private Context(final Object initialSecurityServiceState, final ThreadContext initialThreadContext, final ClassLoader initialLoader) {
            this.securityServiceState = initialSecurityServiceState;
            this.threadContext = initialThreadContext;
            this.loader = initialLoader;
        }

        public void enter() {
            final Thread thread = Thread.currentThread();

            final ClassLoader oldCl = thread.getContextClassLoader();
            thread.setContextClassLoader(loader);

            final ThreadContext oldCtx;
            if (threadContext != null) {
                oldCtx = ThreadContext.enter(new ThreadContext(threadContext));
            } else {
                oldCtx = null;
            }

            final Object threadState = SECURITY_SERVICE.currentState();
            SECURITY_SERVICE.setState(securityServiceState);

            currentContext = new Context(threadState, oldCtx, oldCl);
        }

        public void exit() {
            SECURITY_SERVICE.setState(currentContext.securityServiceState);
            if (currentContext.threadContext != null) {
                ThreadContext.exit(currentContext.threadContext);
            }
            Thread.currentThread().setContextClassLoader(currentContext.loader);
            currentContext = null;
        }
    }
}
