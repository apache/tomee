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
package org.apache.openejb.util;

import junit.framework.TestCase;

import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class OptionsTest extends TestCase {

    public void testEnum() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("caseSensitive", Colors.RED.toString());
        properties.setProperty("caseInsensitive", "blue");

        assertSame(Colors.RED, Options.getEnum(properties, "caseSensitive", Colors.GREEN));
        assertSame(Colors.BLUE, Options.getEnum(properties, "caseInsensitive", Colors.GREEN));
        assertSame(Colors.GREEN, Options.getEnum(properties, "default", Colors.GREEN));
    }

    public static enum Colors {
        RED, GREEN, BLUE;
    }
}
