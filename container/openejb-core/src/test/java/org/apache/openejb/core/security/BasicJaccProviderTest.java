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
package org.apache.openejb.core.security;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.ContainerProperties;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import java.security.Policy;


@Classes(innerClassesAsBean = true)
@RunWith(ApplicationComposer.class)
@ContainerProperties(
        @ContainerProperties.Property(
                name = "jakarta.security.jacc.policy.provider",
                value = "org.apache.openejb.core.security.BasicJaccProviderTest$MyPolicy"))
public class BasicJaccProviderTest {

    @EJB
    private SimpleSingleton myBean;

    @Test
    public void run() throws Exception {
        Assert.assertNotNull("Singleton bean could not be created", myBean);
        Assert.assertEquals("tset", myBean.reverse("test"));
    }

    public static class MyPolicy extends Policy {
    }

    @Singleton
    public static class SimpleSingleton {
        public String reverse(final String input) {
            if (input == null) {
                return null;
            }

            if (input.length() == 0) {
                return "";
            }

            char[] chars = new char[input.length()];
            for (int i = 0; i < input.length(); i++) {
                chars[i] = input.charAt((input.length() - 1) - i);
            }

            return new String(chars);
        }
    }
}
