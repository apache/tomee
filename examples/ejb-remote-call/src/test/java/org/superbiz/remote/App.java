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
package org.superbiz.remote;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class App {

    public static void main(String[] args) throws NamingException, BusinessException {
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");
        properties.put(Context.PROVIDER_URL, "http://localhost:8080/tomee/ejb");

        Context ctx = new InitialContext(properties);
        Object ref = ctx.lookup("global/ejb-remote-call-8.0.1-SNAPSHOT/Calculator!org.superbiz.remote.Calculator");

        Calculator calculator = Calculator.class.cast(ref);
        System.out.println(calculator.sum(1, 2));

        System.out.println("Expecting Hello world: " + calculator.echo("Hello world"));
        try {
            System.out.println("Expecting checked exception: ");
            System.out.println(calculator.echo("CHECKED"));
        } catch (BusinessException e) {
            e.printStackTrace();
        }

        try {
            System.out.println("Expecting runtime exception: ");
            System.out.println(calculator.echo("RUNTIME"));
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

    }
}
