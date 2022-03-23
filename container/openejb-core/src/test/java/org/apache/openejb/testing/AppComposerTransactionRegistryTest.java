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
package org.apache.openejb.testing;

import org.apache.openejb.junit.ApplicationComposerRule;
import org.junit.Rule;
import org.junit.Test;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.transaction.TransactionSynchronizationRegistry;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@SimpleLog
@Classes(context = "app")
public class AppComposerTransactionRegistryTest {
    @Rule
    public final ApplicationComposerRule rule = new ApplicationComposerRule(this);

    @Test
    public void transactionSynchronizationRegistryLookupInNotManagedThread() throws Exception {
        final AtomicReference<Exception> ex = new AtomicReference<>();
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                final Object lookup;
                try {
                    lookup = new InitialContext().lookup("java:comp/TransactionSynchronizationRegistry");
                    assertNotNull("java:comp/TransactionSynchronizationRegistry lookup got an object", lookup);
                    assertTrue("java:comp/TransactionSynchronizationRegistry is a TransactionSynchronizationRegistry", TransactionSynchronizationRegistry.class.isInstance(lookup));
                } catch (final Exception e) {
                    ex.set(e);
                }
            }
        });
        thread.start();
        try {
            thread.join(TimeUnit.MINUTES.toMillis(1));
        } catch (InterruptedException e) {
            Thread.interrupted();
            fail();
        }
        assertNull("error: " + ex.get(), ex.get());
    }
}
