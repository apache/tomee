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
package org.apache.openejb.assembler.classic;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.ContainerProperties;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.naming.NamingException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Classes
@RunWith(ApplicationComposer.class)
@ContainerProperties({
        @ContainerProperties.Property(name = "r1", value = "new://Resource?class-name=org.apache.openejb.assembler.classic.LazyResourceTest$MyResource1"),
        @ContainerProperties.Property(name = "r2", value = "new://Resource?class-name=org.apache.openejb.assembler.classic.LazyResourceTest$MyResource2"),
        @ContainerProperties.Property(name = "r2.Lazy", value = "true"),
        @ContainerProperties.Property(name = "sub/r3", value = "new://Resource?class-name=org.apache.openejb.assembler.classic.LazyResourceTest$MyResource3"),
        @ContainerProperties.Property(name = "sub/r3.Lazy", value = "true")
})
public class LazyResourceTest {
    @Test
    public void lazy() throws NamingException {
        assertEquals(1, MyResource1.count);
        assertEquals(0, MyResource2.count);
        assertTrue(MyResource2.class.isInstance(
                SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext().lookup("openejb/Resource/r2")));
        assertEquals(1, MyResource2.count);
    }

    public static class MyResource1 {
        public static volatile int count = 0;

        public MyResource1() {
            count++;
        }
    }
    public static class MyResource2 {
        public static volatile int count = 0;

        public MyResource2() {
            count++;
        }
    }
    public static class MyResource3 {
    }
}
