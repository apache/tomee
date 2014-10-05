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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.embedded;

import org.apache.openejb.util.DaemonThreadFactory;
import org.apache.openejb.util.Pipe;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

// only for debugging purpose (mainly on buildbot)
public class ThreadStackRule implements TestRule {
    @Override
    public Statement apply(final Statement base, final Description description) {
        if (System.getProperty("os.name", "unknown").toLowerCase().startsWith("windows")) {
            return base;
        }
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                final ScheduledExecutorService ses = Executors.newScheduledThreadPool(1, new DaemonThreadFactory(ThreadStackRule.class.getSimpleName() + "-"));
                final ScheduledFuture<?> task = ses.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        final RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();

                        String pid = bean.getName();
                        if (pid.contains("@")) {
                            pid = pid.substring(0, pid.indexOf("@"));
                        }

                        try {
                            Pipe.pipe(Runtime.getRuntime().exec("kill -3 " + pid));
                        } catch (final Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                }, 2, 2, TimeUnit.MINUTES);
                try {
                    base.evaluate();
                } finally {
                    task.cancel(true);
                    ses.shutdownNow();
                }
            }
        };
    }
}
