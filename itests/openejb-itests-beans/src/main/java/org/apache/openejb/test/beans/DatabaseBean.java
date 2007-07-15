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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 * @version $Rev$ $Date$
 */
public class DatabaseBean implements javax.ejb.SessionBean {

    private static final long serialVersionUID = 1L;
    
    public SessionContext context;
    public InitialContext jndiContext;

    public DatabaseBean() {
        this.getClass();        
    }

    public void ejbCreate() throws javax.ejb.CreateException {
        try {
            jndiContext = new InitialContext();
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
    }

    public void executeQuery(String statement) throws java.sql.SQLException {
        try {

            DataSource ds = (DataSource) jndiContext.lookup("java:comp/env/database");
            Connection con = ds.getConnection();

            try {
                PreparedStatement stmt = con.prepareStatement(statement);
                try {
                    stmt.executeQuery();
                } finally {
                    stmt.close();
                }
            } finally {
                con.close();
            }
        } catch (Exception e) {
            throw new EJBException("Cannot execute the statement: " + statement + e.getMessage());
        }
    }

    public boolean execute(String statement) throws java.sql.SQLException {
        boolean retval;
        Connection con = null;
        try {

            DataSource ds = (DataSource) jndiContext.lookup("java:comp/env/database");
            con = ds.getConnection();

            Statement stmt = con.createStatement();
            try {
                retval = stmt.execute(statement);
            } finally {
                stmt.close();
            }

        } catch (javax.naming.NamingException e) {
//        } catch (Exception e){
//            e.printStackTrace();
            //throw new RemoteException("Cannot execute the statement: "+statement, e);
            throw new EJBException("Cannot lookup the Database bean." + e.getMessage());
        } finally {
            if (con != null) {
                con.close();
            }
        }
        return retval;
    }

    public void ejbPassivate() {
        // never called
    }

    public void ejbActivate() {
        // never called
    }

    public void ejbRemove() {
    }

    public void setSessionContext(javax.ejb.SessionContext cntx) {
        context = cntx;
    }
} 
   