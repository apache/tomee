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
package org.superbiz.injection.jms;

import junit.framework.TestCase;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;

public class MessagingBeanTest extends TestCase {

    public void test() throws Exception {
        Properties p = new Properties();
        p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
        InitialContext context = new InitialContext(p);

        MessagingLocal bean = (MessagingLocal) context.lookup("MessagingBeanLocal");

        bean.sendMessage("Hello World!");
        bean.sendMessage("How are you?");
        bean.sendMessage("Still spinning?");

        assertEquals(bean.receiveMessage(), "Hello World!");
        assertEquals(bean.receiveMessage(), "How are you?");
        assertEquals(bean.receiveMessage(), "Still spinning?");
    }
}
//END SNIPPET: code
