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

import jakarta.ejb.EJBException;
import jakarta.ejb.SessionContext;
import java.rmi.RemoteException;

public class DefaultGreetings implements Greetings {


    @Override
    public String morning(String name) {
        return "Good Morning: " + name;
    }

    @Override
    public String afternoon(String name) {
        return "Good Afternoon: " + name;
    }

    @Override
    public String hello(final String input) throws GreetingsException {
        if ("CHECKED".equals(input)) {
            throw new GreetingsException("This is a checked exception");
        }

        if ("RUNTIME".equals(input)) {
            throw new RuntimeException("This is a runtime exception");
        }

        if (input == null) {
            return "Input was null";
        }

        return "Input was: " + input;
    }


    @Override
    public void ejbActivate() throws EJBException, RemoteException {

    }

    @Override
    public void ejbPassivate() throws EJBException, RemoteException {

    }

    @Override
    public void ejbRemove() throws EJBException, RemoteException {

    }

    @Override
    public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException {

    }
}
