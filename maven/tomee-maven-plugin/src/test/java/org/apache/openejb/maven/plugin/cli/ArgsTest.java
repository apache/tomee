/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.apache.openejb.maven.plugin.cli;

import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class ArgsTest {
    @Test
    public void checkParsing() {
        assertEquals(asList("-Dfoo=bar"), Args.parse("-Dfoo=bar"));
        assertEquals(asList("-Dfoo=bar", "-D2=3", "-Dty"), Args.parse("-Dfoo=bar -D2=3 -Dty"));
        assertEquals(asList("-Dfoo=bar", "-D2=3", "-Dty"), Args.parse("-Dfoo=bar    -D2=3        -Dty"));
        assertEquals(asList("-Dkey with space=value"), Args.parse("\"-Dkey with space=value\""));
        assertEquals(asList("-Dkey with space"), Args.parse("\"-Dkey with space\""));
        assertEquals(asList("-Dkey with space=value with space"), Args.parse("\"-Dkey with space=value with space\""));
        assertEquals(asList("-Dfoo1=bar", "-Dkey with space=value with space", "-Dfoo=bar"), Args.parse("-Dfoo1=bar \"-Dkey with space=value with space\" -Dfoo=bar"));
    }
}
