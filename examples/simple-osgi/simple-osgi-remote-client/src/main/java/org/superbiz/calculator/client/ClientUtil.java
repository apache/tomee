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

package org.superbiz.calculator.client;

import org.apache.openejb.client.RemoteInitialContextFactory;
import org.superbiz.osgi.calculator.CalculatorRemote;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;

public final class ClientUtil {
    private ClientUtil() {
        // no-op
    }

    public static void invoke() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
        properties.setProperty(Context.PROVIDER_URL, "ejbd://localhost:4201");
        Context remoteContext = new InitialContext(properties);
        CalculatorRemote calculator = (CalculatorRemote) remoteContext.lookup("CalculatorBeanRemote");
        System.out.println("Server answered: " + calculator.sayHello());
    }
}
