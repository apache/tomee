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
package org.apache.openejb.monitoring;

import org.junit.Assert;
import org.junit.Test;

import javax.management.ObjectName;

public class ObjectNameBuilderTest {

    @Test
    public void testQuoting() throws Exception {
        final ObjectNameBuilder jmxName = new ObjectNameBuilder("openejb.management");
        jmxName.set("J2EEServer", "openejb");
        jmxName.set("J2EEApplication", null);
        jmxName.set("j2eeType", "");
        jmxName.set("name", "test_with_*_in_it");

        final ObjectName objectName = jmxName.build();
        Assert.assertEquals("openejb.management:J2EEServer=openejb,J2EEApplication=<empty>,j2eeType=<empty>,name=\"test_with_\\*_in_it\"", objectName.toString());
    }


}