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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import static org.apache.openejb.loader.JarLocation.jarLocation;
import static org.junit.Assert.assertTrue;

public class EffectiveTomEEXmlTest {
    @Test
    public void run() throws Exception {
        final PrintStream ps = System.out;
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        try {
            final File filePath = new File(jarLocation(EffectiveTomEEXmlTest.class), getClass().getSimpleName() + ".xml");
            EffectiveTomEEXml.main(new String[]{ "-p", filePath.getAbsolutePath() });
        } finally {
            System.setOut(ps);
        }
        assertTrue(new String(out.toByteArray()).contains("JdbcDriver=org.hsqldb.jdbcDriver"));
    }
}
