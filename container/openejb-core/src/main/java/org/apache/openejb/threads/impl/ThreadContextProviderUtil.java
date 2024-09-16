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

import jakarta.enterprise.concurrent.spi.ThreadContextRestorer;
import jakarta.enterprise.concurrent.spi.ThreadContextSnapshot;

import java.io.Serializable;

public class ThreadContextProviderUtil {
    public static final ThreadContextSnapshot NOOP_SNAPSHOT = new NoOpThreadContextSnapshot();
    public static final ThreadContextRestorer NOOP_RESTORER = new NoOpThreadContextRestorer();

    public static class NoOpThreadContextSnapshot implements ThreadContextSnapshot, Serializable {
        @Override
        public ThreadContextRestorer begin() {
            return NOOP_RESTORER;
        }
    }

    public static class NoOpThreadContextRestorer implements ThreadContextRestorer {
        @Override
        public void endContext() throws IllegalStateException {}
    }
}
