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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.dyni;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.apache.openejb.util.Join;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateful;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;


/**
 * @version $Rev$ $Date$
 */
@RunWith(ApplicationComposer.class)
public class DynamicStatefulTest extends Assert {

    @EJB
    private Colors colors;

    @Module
    public Class[] dynamic() {
        return new Class[]{Colors.class};
    }

    @Test
    public void test() throws Exception {

        assertNotNull(colors);
        assertEquals("red", colors.red());
        assertEquals("handle:blue(hello)", colors.blue("hello"));
        assertEquals("handle:green()", colors.green());
    }


    @Stateful
    public static abstract class Colors implements InvocationHandler {

        public String red() {
            return "red";
        }

        public abstract String green();

        public abstract String blue(String s);

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            return "handle:" + method.getName() + "(" + Join.join(",", args) + ")";
        }
    }
}
