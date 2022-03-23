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
package org.apache.openejb.service;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.SimpleLog;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.Archives;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import jakarta.inject.Inject;

import static org.junit.Assert.assertNotNull;

@SimpleLog
@Classes(cdi = true)
@RunWith(ApplicationComposer.class)
public class ScanJarServiceTest {
    @Configuration
    public Properties config() throws IOException {
        return new PropertiesBuilder()
                .p("scanner", "new://Service?class-name=" + ScanJarService.class.getName())
                .p("scanner.path", Archives.jarArchive(Collections.<String, Object>emptyMap(), "ScanJarServiceTest", MyBean.class).getAbsolutePath())
                .build();
    }

    @Inject
    private MyBean bean;

    @Test
    public void run() {
        assertNotNull(bean);
    }

    public static class MyBean {}
}
