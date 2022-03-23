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
package org.superbiz.cdi.produces.field;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jakarta.ejb.embeddable.EJBContainer;
import jakarta.inject.Inject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LoggerTest {

    @Inject
    Logger logger;

    private EJBContainer container;

    @Before
    public void setUp() {
        try {
            container = EJBContainer.createEJBContainer();
            container.getContext().bind("inject", this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void cleanUp() {
        try {
            container.getContext().unbind("inject");
            container.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLogHandler() {
        assertNotNull(logger);
        assertFalse("Handler should not be a ConsoleHandler", logger.getHandler() instanceof ConsoleHandler);
        assertFalse("Handler should not be a FileHandler", logger.getHandler() instanceof FileHandler);
        assertTrue("Handler should be a DatabaseHandler", logger.getHandler() instanceof DatabaseHandler);
        logger.log("##### Testing write\n");
        logger = null;
    }

}
