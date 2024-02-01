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
package org.apache.openejb.resource.thread;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;

import jakarta.enterprise.concurrent.ManagedThreadFactory;
import javax.naming.NamingException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.ThreadFactory;

final class ThreadFactories {
    private ThreadFactories() {
        // no-op
    }

    public static ManagedThreadFactory findThreadFactory(final String threadFactory)
            throws InstantiationException, IllegalAccessException, NamingException {
        try {
            final Class<?> aClass = Thread.currentThread().getContextClassLoader().loadClass(threadFactory);
            if (!ManagedThreadFactory.class.isAssignableFrom(aClass) && ThreadFactory.class.isAssignableFrom(aClass)) {
                return new ManageMyThreadFactory(ThreadFactory.class.cast(aClass.newInstance()));
            }
            return ManagedThreadFactory.class.cast(aClass.newInstance());
        } catch (final ClassNotFoundException e) {
            return ManagedThreadFactory.class.cast(SystemInstance.get().getComponent(ContainerSystem.class)
                    .getJNDIContext().lookup("openejb:Resource/" + threadFactory));
        }
    }

    private static final class ManageMyThreadFactory implements ManagedThreadFactory {
        private final ThreadFactory delegate;

        private ManageMyThreadFactory(final ThreadFactory delegate) {
            this.delegate = delegate;
        }

        @Override
        public Thread newThread(final Runnable r) {
            return delegate.newThread(r);
        }

        @Override
        public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            return null;
        }
    }
}
