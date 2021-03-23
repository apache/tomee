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

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit5.RunWithApplicationComposer;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testing.RandomPort;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWithApplicationComposer
public class RandomPortTest {
    @Module
    public EjbJar jar() {
        return new EjbJar();
    }

    @RandomPort("httpejb")
    private int port;

    @RandomPort("httpejb")
    private URL portUrl;

    @Test
    public void checkRandom() {
        assertTrue(port > 0);
        assertNotNull(portUrl);
        assertEquals(port, portUrl.getPort());
    }
}
