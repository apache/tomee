/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.test.servlet;

import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.test.TestContext;
import org.junit.Test;

import java.util.logging.Logger;

/**
 * This test listener class is used for running the tests from the web page.
 * <p>
 * NOT : Actually this is not used, it is created and used as an experimental.
 * </p>
 * 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @since 1.0
 */
public class TestListener
{
    Logger log = WebBeansLoggerFacade.getLogger(TestListener.class);

    private void init()
    {
        log.info("Initializing the test contexts");
        TestContext.initTests();
    }

    /**
     * Ending all tests
     */
    public void contextDestroyed(Object arg0)
    {
        log.info("Ending all tests");

        TestContext.endAllTests(arg0);
    }

    @Test
    public void contextInitialized()
    {

    }

    /**
     * Initialize and start all tests from the web application.
     */
    public void contextInitialized(Object arg0)
    {
        init();

        log.info("Starting all tests");

        TestContext.startAllTests(arg0);
    }

}
