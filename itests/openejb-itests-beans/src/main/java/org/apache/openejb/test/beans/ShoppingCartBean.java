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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.test.beans;

import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ShoppingCartBean implements SessionBean, jakarta.ejb.SessionSynchronization {

    String name;
    SessionContext context;
    Context jndiContext;
    Context envContext;
    Boolean useJdbc = Boolean.FALSE;

    public void ejbCreate(final String name) throws jakarta.ejb.CreateException {
        //testAllowedOperations("ejbCreate");
        try {

            jndiContext = new InitialContext();

            final String author = (String) jndiContext.lookup("java:comp/env/author");

            final Double price = (Double) jndiContext.lookup("java:comp/env/price");

        } catch (final javax.naming.NamingException re) {
            throw new RuntimeException("Using JNDI failed");
        }

    }

    public Calculator getCalculator() {

        try {

            final boolean test = context.isCallerInRole("TheMan");

            jndiContext = new InitialContext();

            final CalculatorHome home = (CalculatorHome) jndiContext.lookup("java:comp/env/ejb/calculator");
            final Calculator calc = home.create();
            return calc;

        } catch (final java.rmi.RemoteException re) {
            throw new RuntimeException("Getting calulator bean failed");
        } catch (final javax.naming.NamingException re) {
            throw new RuntimeException("Using JNDI failed");
        }


    }

    public void doJdbcCall() {

        Connection con = null;
        try {

            final javax.sql.DataSource ds = (javax.sql.DataSource) jndiContext.lookup("java:comp/env/jdbc/orders");

            con = ds.getConnection();

            final Statement stmt = con.createStatement();
            try {
                final ResultSet rs = stmt.executeQuery("select * from Employees");
                while (rs.next())
                    System.out.println(rs.getString(2));

                final Calculator calc = getCalculator();
                calc.add(1, 1);
                calc.sub(1, 2);

                final int i = 1;
            } finally {
                stmt.close();
            }

        } catch (final java.rmi.RemoteException re) {
            throw new RuntimeException("Accessing Calculator bean failed");
        } catch (final javax.naming.NamingException ne) {
            throw new RuntimeException("Using JNDI failed");
        } catch (final java.sql.SQLException se) {
            throw new RuntimeException("Getting JDBC data source failed");
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (final SQLException se) {
                    se.printStackTrace();
                }
            }
        }

    }

    public String getName() {

        return name;
    }

    public void setName(final String name) {
        //testAllowedOperations("setName");
        this.name = name;
    }

    public void setSessionContext(final SessionContext cntx) {
        context = cntx;
        //testAllowedOperations("setSessionContext");
    }

    public void ejbActivate() {
        //testAllowedOperations("ejbActivate");
    }

    public void ejbPassivate() {
        //testAllowedOperations("ejbPassivate");
    }

    public void ejbRemove() {
        //testAllowedOperations("ejbRemove");
    }

    public void afterBegin() {
        // do nothing
    }

    public void beforeCompletion() {
        // do nothing
    }

    public void afterCompletion(final boolean commit) {
        // do nothing
    }

    private void testAllowedOperations(final String methodName) {
        System.out.println("******************************************************");
        System.out.println("\nTesting Allowed Operations for " + methodName + "() method\n");
        try {
            context.getEJBObject();
            System.out.println("SessionContext.getEJBObject() ... Allowed");
        } catch (final IllegalStateException ise) {
            System.out.println("SessionContext.getEJBObject() ... Failed");
        }
        try {
            context.getEJBHome();
            System.out.println("SessionContext.getEJBHome() ... Allowed");
        } catch (final IllegalStateException ise) {
            System.out.println("SessionContext.getEJBHome() ... Failed");
        }
        try {
            context.getCallerPrincipal();
            System.out.println("SessionContext.getCallerPrincipal() ... Allowed");
        } catch (final IllegalStateException ise) {
            System.out.println("SessionContext.getCallerPrincipal() ... Failed");
        }
        try {
            context.isCallerInRole("ROLE");
            System.out.println("SessionContext.isCallerInRole() ... Allowed");
        } catch (final IllegalStateException ise) {
            System.out.println("SessionContext.isCallerInRole() ... Failed");
        }
        try {
            context.getRollbackOnly();
            System.out.println("SessionContext.getRollbackOnly() ... Allowed");
        } catch (final IllegalStateException ise) {
            System.out.println("SessionContext.getRollbackOnly() ... Failed");
        }
        try {
            context.setRollbackOnly();
            System.out.println("SessionContext.setRollbackOnly() ... Allowed");
        } catch (final IllegalStateException ise) {
            System.out.println("SessionContext.setRollbackOnly() ... Failed");
        }
        try {
            context.getUserTransaction();
            System.out.println("SessionContext.getUserTransaction() ... Allowed");
        } catch (final IllegalStateException ise) {
            System.out.println("SessionContext.getUserTransaction() ... Failed");
        }
    }
}
    