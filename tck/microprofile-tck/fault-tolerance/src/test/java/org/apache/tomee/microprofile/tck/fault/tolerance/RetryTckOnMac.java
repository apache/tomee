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
package org.apache.tomee.microprofile.tck.fault.tolerance;

import org.eclipse.microprofile.fault.tolerance.tck.RetryTest;
import org.eclipse.microprofile.fault.tolerance.tck.config.ConfigAnnotationAsset;
import org.eclipse.microprofile.fault.tolerance.tck.retry.clientserver.RetryClassLevelClientForMaxRetries;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import java.util.Locale;

/**
 * Copied from SmallRye Fault Tolerance
 */
public class RetryTckOnMac implements ApplicationArchiveProcessor {
    // TODO
    //  The `RetryTest.testClassLevelRetryMaxDuration` TCK test often fails in CI on the macOS machines.
    //  This is because the test doesn't properly scale delays and timeouts, unlike majority of the tests
    //  in the TCK, and `Thread.sleep(100)` often takes roughly 200 millis on the macOS machines in CI.
    //  Here, we work around that problem by reducing jitter on the affected method to 0, thereby significantly
    //  reducing the impact of the `Thread.sleep()` slowdown.

    @Override
    public void process(Archive<?> applicationArchive, TestClass testClass) {
        if (isMac() && RetryTest.class.getName().equals(testClass.getName())) {
            ConfigAnnotationAsset config = (ConfigAnnotationAsset) applicationArchive
                    .getAsType(JavaArchive.class, "/WEB-INF/lib/ftRetry.jar")
                    .get("/META-INF/microprofile-config.properties")
                    .getAsset();
            config.set(RetryClassLevelClientForMaxRetries.class, "serviceB", Retry.class, "jitter", "0");
        }
    }

    private boolean isMac() {
        String os = System.getProperty("os.name", "unknown").toLowerCase(Locale.ROOT);
        return os.contains("mac") || os.contains("darwin");
    }
}
