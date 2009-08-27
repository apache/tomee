/**
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
//START SNIPPET: code
package org.superbiz.mdb;

import junit.framework.TestCase;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;

public class ChatBeanTest extends TestCase {
    protected void setUp() throws Exception {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
    }

    public void test() throws Exception {
        InitialContext context = new InitialContext();

        MessagingClientLocal bean = (MessagingClientLocal) context.lookup("MessagingClientBeanLocal");

        bean.sendMessage("Hello World!");

        assertEquals(bean.receiveMessage(), "Hello, Test Case!");

        bean.sendMessage("How are you?");

        assertEquals(bean.receiveMessage(), "I'm doing well.");

        bean.sendMessage("Still spinning?");

        assertEquals(bean.receiveMessage(), "Once every day, as usual.");
    }
}
//END SNIPPET: code
