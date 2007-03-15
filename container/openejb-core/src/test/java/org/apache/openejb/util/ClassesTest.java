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

/**
 * @version $Rev$ $Date$
 */
public class ClassesTest extends TestCase {

    public void test() throws Exception {

        String[] input = {
                "java.lang.String", "java.lang.String[]", "java.lang.String[][]",
                "boolean", "boolean[]", "boolean[][]",
                "byte", "byte[]", "byte[][]",
                "short", "short[]", "short[][]",
                "int", "int[]", "int[][]",
                "long", "long[]", "long[][]",
                "float", "float[]", "float[][]",
                "double", "double[]", "double[][]",
                "char", "char[]", "char[][]",
        };

        String[] expected = {
                "java.lang.String", "[Ljava.lang.String;", "[[Ljava.lang.String;",
                "boolean", "[Z", "[[Z",
                "byte", "[B", "[[B",
                "short", "[S", "[[S",
                "int", "[I", "[[I",
                "long", "[J", "[[J",
                "float", "[F", "[[F",
                "double", "[D", "[[D",
                "char", "[C", "[[C",
        };

        for (int i = 0; i < input.length; i++) {
            Class clazz = Classes.forName(input[i], this.getClass().getClassLoader());
            assertEquals(expected[i], clazz.getName());
        }
    }

}

