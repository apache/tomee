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

package org.apache.tomee.webapp.test;

import org.apache.tomee.webapp.command.CommandSession;
import org.apache.tomee.webapp.command.impl.RunScript;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RunScriptTest {

    @Test
    public void getInstanceTest() throws Exception {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("scriptCode", Util.readJsFile("/Test.js"));
        params.put("engineName", "js");

        final RunScript shell = new RunScript();
        final Object result = shell.execute(new CommandSession() {
            @Override
            public boolean login(String user, String password) {
                return false;
            }

            @Override
            public Object get(String key) {
                return null;
            }

            @Override
            public void set(String key, Object value) {

            }
        }, params);

        assertEquals("myValue", result);
    }
}

