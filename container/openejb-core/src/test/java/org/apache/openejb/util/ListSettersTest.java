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
package org.apache.openejb.util;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertTrue;

public class ListSettersTest {
    @Test
    public void run() throws Exception {
        final PrintStream ps = System.out;
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        try {
            ListSetters.main(new String[]{"-c", Foo.class.getName()});
        } finally {
            System.setOut(ps);
        }
        assertTrue(new String(out.toByteArray()).contains("Str"));
        assertTrue(new String(out.toByteArray()).contains("Integer"));
    }

    public static class Foo {
        public void setStr(String s) {}
        public void setInteger(int s) {}
    }
}
