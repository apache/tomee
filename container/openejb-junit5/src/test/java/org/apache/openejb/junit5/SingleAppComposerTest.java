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
package org.apache.openejb.junit5;

import org.apache.openejb.junit5.app.MyApp;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.testing.Application;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

//-Djunit.jupiter.testclass.order.default=org.apache.openejb.junit5.order.AppComposerTestClassOrderer -Dtomee.application-composer.application=org.apache.openejb.junit5.app.MyApp
@RunWithApplicationComposer(mode = ExtensionMode.PER_JVM)
public class SingleAppComposerTest {

    @Application
    private MyApp app;

    @Test
    public void run() {
        assertNotNull(app);
        app.check();
        SystemInstance.get().setProperty("key", "Set-Via-SingleAppComposerTest-In-Same-JVM");
    }
}
