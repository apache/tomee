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
package org.superbiz.dynamic;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import jakarta.ejb.embeddable.EJBContainer;

import static org.junit.Assert.assertTrue;

public class SocialTest {

    private static SocialBean social;
    private static EJBContainer container;

    @BeforeClass
    public static void init() throws Exception {
        container = EJBContainer.createEJBContainer();
        social = (SocialBean) container.getContext().lookup("java:global/dynamic-implementation/SocialBean");
    }

    @AfterClass
    public static void close() {
        container.close();
    }

    @Test
    public void simple() {
        assertTrue(social.facebookStatus().contains("think"));
        assertTrue(social.twitterStatus().contains("eat"));
        assertTrue(social.status().contains("virtual"));
    }
}
