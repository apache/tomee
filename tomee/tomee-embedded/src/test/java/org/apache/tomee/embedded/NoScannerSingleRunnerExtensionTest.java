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
package org.apache.tomee.embedded;

import org.apache.openejb.testing.Application;
import org.apache.openejb.testing.Classes;
import org.apache.tomee.embedded.junit.jupiter.RunWithTomEEEmbedded;
import org.apache.tomee.embedded.junit.jupiter.TomEEEmbeddedExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

// just a manual test to check it works, can't be executed with the rest of the suite,
// we could use a different surefire execution if we want to add it to the default run
//-Dtomee.application-composer.application=org.apache.tomee.embedded.NoScannerSingleRunnerExtensionTest$ScanApp
@RunWithTomEEEmbedded
public class NoScannerSingleRunnerExtensionTest {
    @Application // app can have several injections/helpers
    private ScanApp app;

    @Test
    public void run() {
        assertNotNull(app);
        app.check();
    }

    @Application
    @Classes(value = ScanMe.class)
    public static class ScanApp {
        @Inject
        private ScanMe ok;

        @Inject
        private Instance<NotScanned> ko;

        public void check() {
            assertNotNull(ok);
            assertTrue(ko.isUnsatisfied());
        }
    }

    @ApplicationScoped
    public static class ScanMe {
    }

    @ApplicationScoped
    public static class NotScanned {
    }
}
