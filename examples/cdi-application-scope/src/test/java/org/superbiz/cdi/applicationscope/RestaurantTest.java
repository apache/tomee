/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.cdi.applicationscope;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jakarta.ejb.EJB;
import jakarta.ejb.embeddable.EJBContainer;

import static org.junit.Assert.assertEquals;

public class RestaurantTest {

    private static String TOMATO_SOUP = "Tomato Soup";
    private EJBContainer container;

    @EJB
    private Waiter joe;

    @Before
    public void startContainer() throws Exception {
        container = EJBContainer.createEJBContainer();
        container.getContext().bind("inject", this);
    }

    @Test
    public void orderSoup() {
        String someSoup = joe.orderSoup(TOMATO_SOUP);
        assertEquals(TOMATO_SOUP, someSoup);

        String sameSoup = joe.orderWhatTheOtherGuyHad();
        assertEquals(TOMATO_SOUP, sameSoup);
    }

    @After
    public void closeContainer() throws Exception {
        container.close();
    }
}
