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
package org.apache.tomee.embedded.junit;

import org.apache.tomee.embedded.TomEEEmbeddedApplicationRunner;

import jakarta.enterprise.inject.Vetoed;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

@Vetoed
public class TomEEEmbeddedBase {
    private static final AtomicReference<TomEEEmbeddedApplicationRunner> RUNNER = new AtomicReference<>();

    public void start(final Object marker) throws Exception {
        getRunner().start(marker.getClass(), (Properties) null);
    }

    public void close() {
        final TomEEEmbeddedApplicationRunner runner = RUNNER.get();
        if (runner != null) {
            runner.close();
        }
    }

    public void setApp(final Object o) {
        getRunner().setApp(o);
    }

    public void composerInject(final Object target) throws IllegalAccessException {
        getRunner().composerInject(target);
    }

    private TomEEEmbeddedApplicationRunner getRunner() {
        final TomEEEmbeddedApplicationRunner runner = RUNNER.get();
        if (runner == null) {
            RUNNER.compareAndSet(null, new TomEEEmbeddedApplicationRunner());
        }
        return RUNNER.get();
    }

}
