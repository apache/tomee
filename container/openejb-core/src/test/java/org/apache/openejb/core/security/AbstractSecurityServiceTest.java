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
package org.apache.openejb.core.security;

import org.apache.openejb.core.security.jacc.BasicJaccProvider;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.ContainerProperties;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@Classes
@RunWith(ApplicationComposer.class)
@ContainerProperties(
        @ContainerProperties.Property(
                name = "org.apache.openejb.core.security.JaccProvider",
                value = "org.apache.openejb.core.security.AbstractSecurityServiceTest$MyJaacProv"))
public class AbstractSecurityServiceTest {
    @Test
    public void run() {
        assertTrue(MyJaacProv.class.isInstance(JaccProvider.get()));
    }

    public static class MyJaacProv extends BasicJaccProvider {
    }
}
