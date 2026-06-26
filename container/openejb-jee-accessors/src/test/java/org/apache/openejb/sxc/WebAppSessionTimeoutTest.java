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
package org.apache.openejb.sxc;

import org.apache.openejb.jee.WebApp;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class WebAppSessionTimeoutTest {

    /**
     * TOMEE-4589: session-timeout is an xsd:integer whose lexical value is whitespace
     * collapsed, so a value surrounded by newlines/indentation is valid and must parse.
     */
    @Test
    public void sessionTimeoutWithWhitespace() throws Exception {
        final URL url = getClass().getClassLoader().getResource("web-session-timeout-whitespace.xml");
        final WebApp webApp = WebXml.unmarshal(url);
        assertNotNull(webApp);
        assertEquals(1, webApp.getSessionConfig().size());
        assertEquals(Integer.valueOf(30), webApp.getSessionConfig().get(0).getSessionTimeout());
    }
}
