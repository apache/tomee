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
package org.apache.tomee.log4j2;

import org.apache.logging.log4j.core.util.Cancellable;
import org.apache.logging.log4j.core.util.ShutdownCallbackRegistry;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

public class CaptureLog4j2ShutdownHooks implements ShutdownCallbackRegistry {
    static final Collection<Runnable> HOOKS = new CopyOnWriteArraySet<Runnable>();

    public Cancellable addShutdownCallback(final Runnable callback) {
        HOOKS.add(callback);
        return new Cancellable() {
            @Override
            public void cancel() {
                HOOKS.remove(callback);
            }

            @Override
            public void run() {
                cancel();
                callback.run();
            }
        };
    }
}
