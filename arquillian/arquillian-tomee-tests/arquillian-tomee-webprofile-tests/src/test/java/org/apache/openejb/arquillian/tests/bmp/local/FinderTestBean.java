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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.arquillian.tests.bmp.local;

import jakarta.ejb.CreateException;
import jakarta.ejb.EJBException;
import jakarta.ejb.EJBObject;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

public class FinderTestBean implements SessionBean {

    public void setSessionContext(final SessionContext context) throws RemoteException {
    }

    public void ejbRemove() throws RemoteException {
    }

    public void ejbActivate() throws RemoteException {
    }

    public void ejbPassivate() throws RemoteException {
    }

    public void ejbCreate() throws CreateException {
    }

    public String runTest()
            throws Exception {
        final BigFinderHome bigFinderHome = (BigFinderHome) lookup("BigFinderHome");
        final LittleFinderHome littleFinderHome = (LittleFinderHome) lookup("LittleFinderHome");
        for (int i = 1; i < 300; ++i) {
            bigFinderHome.findN(i);

            final Collection littleList = littleFinderHome.findAll();
            for (final Object obj : littleList) {
                final StringBuilder msg = new StringBuilder();
                if (!(obj instanceof LittleFinder)) {
                    msg.append("Failed with " + i + " records. LittleFinder Remote is actually " + obj.getClass().getName() + " Implemented interfaces " + Arrays.toString(obj.getClass().getInterfaces()));
                    if (obj instanceof EJBObject) {
                        final Object pk = ((EJBObject) obj).getPrimaryKey();
                        msg.append(" Primary key value is " + pk);
                    }

                    throw new EJBException(msg.toString());
                }
            }
        }

        return "Test succeeded";
    }

    public static Object lookup(final String s) throws NamingException {
        final Properties p = new Properties();
        p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");
        final InitialContext ctx = new InitialContext(p);
        try {
            return ctx.lookup("java:comp/env/ejb/" + s);
        } finally {
            ctx.close();
        }
    }


}
