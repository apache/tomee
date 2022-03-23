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
package org.apache.openejb.core.asynch;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import jakarta.ejb.ApplicationException;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Classes(innerClassesAsBean = true)
@RunWith(ApplicationComposer.class)
public class AsyncAppExceptionTest {
    @EJB
    private MyAsync async;

    @Test
    public void run() {
        try {
            async.letItFail().get();
        } catch (final InterruptedException e) {
            Thread.interrupted();
            fail();
        } catch (final ExecutionException e) {
            assertTrue(MyException.class.isInstance(e.getCause()));
        }
    }

    @Singleton
    public static class MyAsync {
        @Asynchronous
        public Future<Boolean> letItFail() throws MyException {
            throw new MyException();
        }
    }

    @ApplicationException
    public static class MyException extends RuntimeException {
    }
}
