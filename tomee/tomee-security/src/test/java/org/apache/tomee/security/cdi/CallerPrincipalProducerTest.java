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
package org.apache.tomee.security.cdi;

import jakarta.security.enterprise.SecurityContext;
import org.apache.openejb.util.reflection.Reflections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.security.Principal;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class CallerPrincipalProducerTest {

    private CallerPrincipalProducer producer;
    private SecurityContext securityContext;

    @Before
    public void setUp() {
        producer = new CallerPrincipalProducer();
        securityContext = Mockito.mock(SecurityContext.class);
        Reflections.set(producer, "securityContext", securityContext);
    }

    @Test
    public void producesCurrentCallerPrincipal() {
        final Principal expected = () -> "alice";
        Mockito.when(securityContext.getCallerPrincipal()).thenReturn(expected);

        assertSame(expected, producer.callerPrincipal());
    }

    @Test
    public void unauthenticatedCallerIsNull() {
        // SecurityContext.getCallerPrincipal returns null when no caller is authenticated; the producer
        // must propagate that null so @Inject Principal reflects the live authentication state.
        Mockito.when(securityContext.getCallerPrincipal()).thenReturn(null);

        assertNull(producer.callerPrincipal());
    }

    @Test
    public void callerChangesAreReflectedOnEachLookup() {
        // @Dependent scope means each resolution hits the producer afresh; verify it re-reads
        // SecurityContext every time rather than caching.
        final Principal alice = () -> "alice";
        final Principal bob = () -> "bob";
        Mockito.when(securityContext.getCallerPrincipal()).thenReturn(alice, bob, null);

        assertSame(alice, producer.callerPrincipal());
        assertSame(bob, producer.callerPrincipal());
        assertNull(producer.callerPrincipal());
    }
}
