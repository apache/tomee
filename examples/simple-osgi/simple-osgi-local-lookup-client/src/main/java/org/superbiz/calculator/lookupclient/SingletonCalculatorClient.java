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

package org.superbiz.calculator.lookupclient;

import org.superbiz.osgi.calculator.CalculatorLocal;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

@Singleton
@Startup
public class SingletonCalculatorClient {
    @EJB
    private CalculatorLocal calculator;

    @PostConstruct
    public void logInit() {
        System.out.println();
        checkCalculator();
        tryLookup();
        System.out.println();
    }

    private void tryLookup() {
        Properties p = new Properties();
        p.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");
        try {
            Context ctx = new InitialContext(p);
            CalculatorLocal local = (CalculatorLocal) ctx.lookup("CalculatorBeanLocal");
            System.out.println("lookup OK: " + local.sayHello());
        } catch (NamingException e) {
            System.out.println("can't lookup bean: " + e.getMessage());
        }
    }

    private void checkCalculator() {
        if (calculator == null) {
            System.out.println(calculator + " is null -> FAILED!");
        } else {
            System.out.println("calculator OK: " + calculator.sayHello());
        }
    }
}
