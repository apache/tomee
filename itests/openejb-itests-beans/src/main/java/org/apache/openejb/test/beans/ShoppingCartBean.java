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

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ShoppingCartBean implements SessionBean, javax.ejb.SessionSynchronization {

    String name;
    SessionContext context;
    Context jndiContext;
    Context envContext;
    Boolean useJdbc = Boolean.FALSE;

    public void ejbCreate(String name) throws javax.ejb.CreateException {
        //testAllowedOperations("ejbCreate");
        try {

            jndiContext = new InitialContext();

            String author = (String) jndiContext.lookup("java:comp/env/author");

            Double price = (Double) jndiContext.lookup("java:comp/env/price");

        } catch (javax.naming.NamingException re) {
            throw new RuntimeException("Using JNDI failed");
        }

    }

    public Calculator getCalculator() {

        try {

            boolean test = context.isCallerInRole("TheMan");

            jndiContext = new InitialContext();

            CalculatorHome home = (CalculatorHome) jndiContext.lookup("java:comp/env/ejb/calculator");
            Calculator calc = home.create();
            return calc;

        } catch (java.rmi.RemoteException re) {
            throw new RuntimeException("Getting calulator bean failed");
        } catch (javax.naming.NamingException re) {
            throw new RuntimeException("Using JNDI failed");
        }


    }

    public void doJdbcCall() {

        Connection con = null;
        try {

            javax.sql.DataSource ds = (javax.sql.DataSource) jndiContext.lookup("java:comp/env/jdbc/orders");

            con = ds.getConnection();

            Statement stmt = con.createStatement();
            try {
                ResultSet rs = stmt.executeQuery("select * from Employees");
                while (rs.next())
                    System.out.println(rs.getString(2));

                Calculator calc = getCalculator();
                calc.add(1, 1);
                calc.sub(1, 2);

                int i = 1;
            } finally {
                stmt.close();
            }

        } catch (java.rmi.RemoteException re) {
            throw new RuntimeException("Accessing Calculator bean failed");
        } catch (javax.naming.NamingException ne) {
            throw new RuntimeException("Using JNDI failed");
        } catch (java.sql.SQLException se) {
            throw new RuntimeException("Getting JDBC data source failed");
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException se) {
                    se.printStackTrace();
                }
            }
        }

    }

    public String getName() {

        return name;
    }

    public void setName(String name) {
        //testAllowedOperations("setName");
        this.name = name;
    }

    public void setSessionContext(SessionContext cntx) {
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

    public void afterCompletion(boolean commit) {
        // do nothing
    }

    private void testAllowedOperations(String methodName) {
        System.out.println("******************************************************");
        System.out.println("\nTesting Allowed Operations for " + methodName + "() method\n");
        try {
            context.getEJBObject();
            System.out.println("SessionContext.getEJBObject() ... Allowed");
        } catch (IllegalStateException ise) {
            System.out.println("SessionContext.getEJBObject() ... Failed");
        }
        try {
            context.getEJBHome();
            System.out.println("SessionContext.getEJBHome() ... Allowed");
        } catch (IllegalStateException ise) {
            System.out.println("SessionContext.getEJBHome() ... Failed");
        }
        try {
            context.getCallerPrincipal();
            System.out.println("SessionContext.getCallerPrincipal() ... Allowed");
        } catch (IllegalStateException ise) {
            System.out.println("SessionContext.getCallerPrincipal() ... Failed");
        }
        try {
            context.isCallerInRole("ROLE");
            System.out.println("SessionContext.isCallerInRole() ... Allowed");
        } catch (IllegalStateException ise) {
            System.out.println("SessionContext.isCallerInRole() ... Failed");
        }
        try {
            context.getRollbackOnly();
            System.out.println("SessionContext.getRollbackOnly() ... Allowed");
        } catch (IllegalStateException ise) {
            System.out.println("SessionContext.getRollbackOnly() ... Failed");
        }
        try {
            context.setRollbackOnly();
            System.out.println("SessionContext.setRollbackOnly() ... Allowed");
        } catch (IllegalStateException ise) {
            System.out.println("SessionContext.setRollbackOnly() ... Failed");
        }
        try {
            context.getUserTransaction();
            System.out.println("SessionContext.getUserTransaction() ... Allowed");
        } catch (IllegalStateException ise) {
            System.out.println("SessionContext.getUserTransaction() ... Failed");
        }
    }
}
    