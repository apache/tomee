/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.lib.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.openjpa.lib.util.concurrent.AbstractConcurrentEventManager;

/**
 * Tests the {@link AbstractConcurrentEventManager}.
 *
 * @author Abe White
 */
public class TestAbstractEventManager extends TestCase {

    private EventManager _em = new EventManager();

    public TestAbstractEventManager(String test) {
        super(test);
    }

    public void testReentrantAdd() {
        Listener l1 = new Listener(Listener.ADD);
        Listener l2 = new Listener(Listener.NONE);
        _em.addListener(l1);
        _em.addListener(l2);
        _em.fireEvent(new Object());
        assertTrue(l1.fired);
        assertTrue(l2.fired);
        assertEquals(3, _em.getListeners().size());
    }

    public void testReentrantRemove() {
        Listener l1 = new Listener(Listener.REMOVE);
        Listener l2 = new Listener(Listener.NONE);
        _em.addListener(l1);
        _em.addListener(l2);
        _em.fireEvent(new Object());
        assertTrue(l1.fired);
        assertTrue(l2.fired);
        assertEquals(1, _em.getListeners().size());
        assertFalse(_em.getListeners().contains(l1));
    }

    public static Test suite() {
        return new TestSuite(TestAbstractEventManager.class);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    private static class EventManager extends AbstractConcurrentEventManager {

        protected void fireEvent(Object event, Object listener) {
            ((Listener) listener).fire();
        }
    }

    private class Listener {

        public static final int NONE = 0;
        public static final int ADD = 1;
        public static final int REMOVE = 2;

        public boolean fired;
        private final int _action;

        public Listener(int action) {
            _action = action;
        }

        public void fire() {
            fired = true;
            if (_action == ADD)
                _em.addListener(new Listener(NONE));
            else if (_action == REMOVE)
                assertTrue(_em.removeListener(this));
        }
    }
}

