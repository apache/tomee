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
package org.apache.openejb.junit5;

import org.apache.openejb.itest.failover.ejb.Calculator;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit5.RunWithApplicationComposer;
import org.apache.openejb.testing.Jars;
import org.apache.openejb.testing.Module;
import org.junit.jupiter.api.Test;

import javax.ejb.EJB;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWithApplicationComposer
public class AppComposerJarsTest {
    @Module
    @Jars("failover-ejb-")
    public WebApp war() {
        return new WebApp();
    }

    @EJB
    private Calculator calculator;

    @Test
    public void externalBeanFound() {
        assertNotNull(calculator);
        assertEquals(3, calculator.sum(1, 2));
    }
}
