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
package org.apache.openejb.threads;

import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * TOMEE-4699: copying a ThreadContext iterates the source map, which fails with a
 * ConcurrentModificationException when the owning thread updates it at the same time.
 */
@RunWith(ApplicationComposer.class)
public class ThreadContextCopyRaceTest {
    @Module
    public EnterpriseBean bean() {
        return new SingletonBean(Facade.class).localBean();
    }

    @EJB
    private Facade facade;

    @Test
    public void copyingWhileTheOwnerUpdatesItsContext() throws Exception {
        facade.hammer();
    }

    public static class Filler {
    }

    @Singleton
    public static class Facade {
        public void hammer() throws Exception {
            final ThreadContext caller = ThreadContext.getThreadContext();
            assertNotNull(caller);

            // widen the window: an entrySet iterator over a bigger map spends longer exposed
            for (int i = 0; i < 16; i++) {
                caller.set(Filler.class, new Filler());
            }

            final AtomicBoolean running = new AtomicBoolean(true);
            final AtomicReference<Throwable> failure = new AtomicReference<>();

            // another thread updates the context while this one copies it, which is what the
            // managed executor used to do
            final Thread mutator = new Thread(() -> {
                try {
                    while (running.get()) {
                        caller.set(Filler.class, new Filler());
                        caller.remove(Filler.class);
                    }
                } catch (final Throwable t) {
                    failure.compareAndSet(null, t);
                }
            }, "thread-context-mutator");
            mutator.start();

            try {
                for (int i = 0; i < 200_000 && failure.get() == null; i++) {
                    new ThreadContext(caller);
                }
            } catch (final Throwable t) {
                failure.compareAndSet(null, t);
            } finally {
                running.set(false);
                mutator.join(60_000L);
            }

            assertNull("copying a ThreadContext must not race with its owner: " + failure.get(),
                failure.get());
        }
    }
}
