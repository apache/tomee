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
package org.apache.openejb.config;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NewLoaderLogicTest {
    @Test
    public void ensureExclusions() throws Exception {
        assertTrue(NewLoaderLogic.skip("openejb-core"));
        assertTrue(NewLoaderLogic.skip("openejb-core.jar"));
        assertTrue(NewLoaderLogic.skip("openejb-core-12345.jar"));
        assertTrue(NewLoaderLogic.skip("tomee-catalina-12345.jar"));
        assertFalse(NewLoaderLogic.skip("openejb-noexclude.jar"));
        assertFalse(NewLoaderLogic.skip("business-foo-1.2.3.jar"));
    }
}
