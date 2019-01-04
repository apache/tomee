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
import org.apache.openejb.observer.event.BeforeEvent;
import org.apache.openejb.observer.event.ObserverFailed;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.apache.openejb.observer.Util.caller;
import static org.apache.openejb.observer.Util.description;

public class ObserverFeaturesTest {


    @Test
    public void observeAll() {
        a(new Object() {
            public void observe(final @Observes Object event) {
                pass();
            }
        }, new Date());
    }

    @Test
    public void noFalsePositive() {
        a(new Object() {
            public void observe(final @Observes Integer event) {
                fail();
            }

            public void observe(final @Observes Date event) {
                pass();
            }
        }, new Date());
    }

    @Test
    public void inheritance() {
        a(new Object() {
            public void observe(final @Observes Number event) {
                pass();
            }
        }, 42);
    }

    @Test
    public void overloaded() {
        a(new Object() {
            public void number(final @Observes Number event) {
                fail();
            }

            public void integer(final @Observes Integer event) {
                pass();
            }
        }, 42);
    }

    @Test
    @Assert({"before", "observe"})
    public void beforeEvent() {
        a(new Object() {
            public void before(final @Observes BeforeEvent<Integer> event) {
                invoked();
            }

            public void observe(final @Observes Integer event) {
                invoked();
            }
        }, 42);
    }

    @Test
    @Assert({"observe", "after"})
    public void afterEvent() {
        a(new Object() {
            public void after(final @Observes AfterEvent<Integer> event) {
                invoked();
            }

            public void observe(final @Observes Integer event) {
                invoked();
            }
        }, 42);
    }


    @Test
    @Assert({"before", "after"})
    public void beforeInvokeAfter() {
        a(new Object() {
            public void after(final @Observes AfterEvent<Integer> event) {
                invoked();
            }

            public void before(final @Observes BeforeEvent<Integer> event) {
                invoked();
            }
        }, 42);
    }

    @Test
    public void noFalseBeforePositive() {
        a(new Object() {
            public void integer(final @Observes BeforeEvent<Integer> event) {
                pass();
            }

            public void date(final @Observes BeforeEvent<Date> event) {
                fail();
            }
        }, 42);
    }

    @Test
    @Assert("integer")
    public void noFalseAfterPositive() {
        a(new Object() {
            public void integer(final @Observes AfterEvent<Integer> event) {
                invoked();
            }

            public void date(final @Observes AfterEvent<Date> event) {
                invoked();
            }
        }, 42);
    }

    @Test
    public void beforeInheritance() {
        a(new Object() {
            public void number(final @Observes AfterEvent<Number> event) {
                pass();
            }
        }, 42);
    }

    @Test
    public void beforeObject() {
        a(new Object() {
            public void integer(final @Observes AfterEvent<Object> event) {
                pass();
            }
        }, 42);
    }

    @Test
    public void beforeInheritanceOverloaded() {
        a(new Object() {
            public void integer(final @Observes AfterEvent<Integer> event) {
                pass();
            }

            public void number(final @Observes AfterEvent<Number> event) {
                fail();
            }
        }, 42);
    }

    @Test
    public void afterInheritance() {
        a(new Object() {
            public void number(final @Observes AfterEvent<Number> event) {
                pass();
            }
        }, 42);
    }

    @Test
    public void afterObject() {
        a(new Object() {
            public void integer(final @Observes AfterEvent<Object> event) {
                pass();
            }
        }, 42);
    }

    @Test
    public void afterInheritanceOverloaded() {
        a(new Object() {
            public void integer(final @Observes AfterEvent<Integer> event) {
                pass();
            }

            public void number(final @Observes AfterEvent<Number> event) {
                fail();
            }
        }, 42);
    }


    @Test
    @Assert({"number", "afterInteger", "beforeDate", "object", "afterObject", "object", "afterObject"})
    public void sequence() {
        a(new Object() {
            public void object(final @Observes Object event) {
                invoked();
            }

            public void number(final @Observes Number event) {
                invoked();
            }

            public void afterInteger(final @Observes AfterEvent<Integer> event) {
                invoked();
            }

            public void afterObject(final @Observes AfterEvent<Object> event) {
                invoked();
            }

            public void beforeDate(final @Observes BeforeEvent<Date> event) {
                invoked();
            }
        }, 42, new Date(), URI.create("foo:bar"));
    }

    @Test
    @Assert({"number", "failed"})
    public void failure() {
        a(new Object() {
            public void number(final @Observes Integer event) {
                invoked();
                throw new RuntimeException("testing exceptions");
            }

            public void failed(final @Observes ObserverFailed event) {
                invoked();
            }
        }, 42);
    }

    @Test
    @Assert({"number", "failed"})
    public void circularFailureDirect() {
        a(new Object() {
            public void number(final @Observes Integer event) {
                invoked();
                throw new RuntimeException("testing exceptions");
            }

            public void failed(final @Observes ObserverFailed event) {
                invoked();
                throw new RuntimeException("testing exceptions");
            }
        }, 42);
    }

    @Test
    @Assert({
        "number.Integer",
        "afterObject.AfterEvent<ObserverFailed{number}>",
        "afterObject.AfterEvent<ObserverFailed{afterObject}>",
        "afterObject.AfterEvent<Integer>",
    })
    public void circularFailureAfterObject() {
        a(new Object() {
            public void number(final @Observes Integer event) {
                invoked(description(event));
                throw new RuntimeException("testing exceptions");
            }

            public void afterObject(final @Observes AfterEvent<Object> event) {
                invoked(description(event));
                throw new RuntimeException("testing exceptions");
            }
        }, 42);
    }

    @Test
    @Assert({
        "number.Integer",
        "afterObject.AfterEvent<Integer>",
        "failed.ObserverFailed{afterObject}",
        "afterObject.AfterEvent<ObserverFailed{afterObject}>",
    })
    public void circluarFailureProtection() {
        a(new Object() {
            public void number(final @Observes Integer event) {
                invoked(description(event));
            }

            public void afterObject(final @Observes AfterEvent<Object> event) {
                invoked(description(event));
                throw new RuntimeException("testing exceptions");
            }

            public void failed(final @Observes ObserverFailed event) {
                invoked(description(event));
                throw new RuntimeException("testing exceptions");
            }
        }, 42);
    }

    private final List<Boolean> conditions = new ArrayList<>();
    private final List<String> invocations = new ArrayList<>();

    @Before
    public void init() {
        conditions.clear();
    }

    public void pass() {
        conditions.add(true);
    }

    public void fail() {
        conditions.add(false);
    }

    public void invoked() {
        final Method method = caller(2);
        invocations.add(method.getName());
    }

    public void invoked(final String suffix) {
        final Method method = caller(2);
        invocations.add(method.getName() + "." + suffix);
    }

    private void a(final Object observer, final Object... events) {
        final ObserverManager observers = new ObserverManager();
        observers.addObserver(observer);

        conditions.clear();
        invocations.clear();

        for (final Object event : events) {
            observers.fireEvent(event);
        }

        final Method testMethod = caller(2);
        final Assert annotation = testMethod.getAnnotation(Assert.class);
        if (annotation != null) {

            Util.assertEvent(invocations, annotation.value());

        } else {

            org.junit.Assert.assertNotEquals(0, conditions.size());
            for (final Boolean condition : conditions) {
                org.junit.Assert.assertTrue(condition);
            }
        }
    }


}
