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

import jakarta.enterprise.concurrent.ContextServiceDefinition;
import jakarta.enterprise.concurrent.spi.ThreadContextProvider;
import jakarta.enterprise.concurrent.spi.ThreadContextRestorer;
import jakarta.enterprise.concurrent.spi.ThreadContextSnapshot;

import java.util.Map;

public class TxThreadContextProvider implements ThreadContextProvider {
    @Override
    public ThreadContextSnapshot currentContext(final Map<String, String> props) {
        return new TxThreadContextSnapshot();
    }

    @Override
    public ThreadContextSnapshot clearedContext(final Map<String, String> props) {
        return new TxThreadContextSnapshot();
    }

    @Override
    public String getThreadContextType() {
        return ContextServiceDefinition.TRANSACTION;
    }

    public class TxThreadContextSnapshot implements ThreadContextSnapshot {


        public TxThreadContextSnapshot() {
        }

        @Override
        public ThreadContextRestorer begin() {
            return new TxThreadContextRestorer();
        }
    }

    public class TxThreadContextRestorer implements ThreadContextRestorer {

        public TxThreadContextRestorer() {
        }

        @Override
        public void endContext() throws IllegalStateException {
        }
    }
}
