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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.observer;

import org.apache.openejb.observer.event.AfterEvent;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.net.URI;

public class InvalidObserversTest extends Assert {


    @Test
    public void noObserverMethods() {
        final ObserverManager observers = new ObserverManager();
        observers.addObserver(new Object() {
        });
    }

    @Test
    public void methodIsStatic() {
        a(new StaticMethod());
    }

    public static class StaticMethod {
        public static void observe(final @Observes URI event1) {
        }
    }

    @Test
    public void methodIsPrivate() {
        a(new Object() {
            private void observe(final @Observes URI event) {
            }
        });
    }

    @Test
    public void methodIsProtected() {
        a(new Object() {
            protected void observe(final @Observes URI event) {
            }
        });
    }

    @Test
    public void methodIsDefaultScoped() {
        a(new Object() {
            void observe(final @Observes URI event) {
            }
        });
    }

    @Test
    public void tooManyParameters() {
        a(new Object() {
            public void observe(final @Observes URI event1, final @Observes Class event2) {
            }
        });
    }

    @Test
    public void parameterIsInterface() {
        a(new Object() {
            public void observe(final @Observes Serializable serializable) {
            }
        });
    }

    @Test
    public void parameterIsPrimitive() {
        a(new Object() {
            public void observe(final @Observes int param) {
            }
        });
    }

    @Test
    public void parameterIsArray() {
        a(new Object() {
            public void observe(final @Observes URI[] param) {
            }
        });
    }

    @Test
    public void missingTypeParameter() {
        a(new Object() {

            public void observe(final @Observes AfterEvent afterEvent) {
            }

        });
    }

    @Test
    public <T> void typeParameterIsType() {
        a(new Object() {

            public void observe(final @Observes AfterEvent<T> afterEvent) {
            }

        });
    }

    @Test
    public void typeParameterIsInterface() {
        a(new Object() {

            public void observe(final @Observes AfterEvent<Serializable> afterEvent) {
            }

        });
    }

    private void a(final Object observer) {
        try {
            final ObserverManager observers = new ObserverManager();
            observers.addObserver(observer);
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // pass
        }
    }

}
