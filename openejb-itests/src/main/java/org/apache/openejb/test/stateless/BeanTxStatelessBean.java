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
package org.apache.openejb.test.stateless;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.RollbackException;
import javax.transaction.UserTransaction;

import org.apache.openejb.test.object.Account;
import org.apache.openejb.test.object.Transaction;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class BeanTxStatelessBean implements javax.ejb.SessionBean{

    
    private String name;
    private SessionContext ejbContext;
    private InitialContext jndiContext;
    public final String jndiDatabaseEntry = "jdbc/stateless/beanManagedTransaction/database";


    
    //=============================
    // Home interface methods
    //    
    
    //    
    // Home interface methods
    //=============================
    

    //=============================
    // Remote interface methods
    //    
    
    public Transaction getUserTransaction() throws RemoteException{
        
        UserTransaction ut = null;
        try{
            ut = ejbContext.getUserTransaction();
        } catch (IllegalStateException ise){
            throw new RemoteException(ise.getMessage());
        }
        if (ut == null) return null;
        return new Transaction(ut);
    }
    
    public Transaction jndiUserTransaction() throws RemoteException{
        UserTransaction ut = null;
        try{
            ut = (UserTransaction)jndiContext.lookup("java:comp/UserTransaction");
        } catch (Exception e){
            throw new RemoteException(e.getMessage());
        }
        if (ut == null) return null;
        return new Transaction(ut);
    }

    public void openAccount(Account acct, Boolean rollback) throws RemoteException, RollbackException{
        
        try{
            DataSource ds = (DataSource)javax.rmi.PortableRemoteObject.narrow( jndiContext.lookup("java:comp/env/database"), DataSource.class);
            Connection con = ds.getConnection();
            
            UserTransaction ut = ejbContext.getUserTransaction();
            /*[1] Begin the transaction */
            ut.begin();


            /*[2] Update the table */
            PreparedStatement stmt = con.prepareStatement("insert into Account (SSN, First_name, Last_name, Balance) values (?,?,?,?)");
            stmt.setString(1, acct.getSsn());
            stmt.setString(2, acct.getFirstName());
            stmt.setString(3, acct.getLastName());
            stmt.setInt(4, acct.getBalance());
            stmt.executeUpdate();

            /*[3] Commit or Rollback the transaction */
            if (rollback.booleanValue()) ut.setRollbackOnly();
            
            /*[4] Commit or Rollback the transaction */
            ut.commit();
            

            /*[4] Clean up */
            stmt.close();
            con.close();
        } catch (RollbackException re){
            throw re;
        } catch (Exception e){
            e.printStackTrace();
            throw new RemoteException("[Bean] "+e.getClass().getName()+" : "+e.getMessage());
        }
    }

    public Account retreiveAccount(String ssn) throws RemoteException {
        Account acct = new Account();
        try{
            DataSource ds = (DataSource)javax.rmi.PortableRemoteObject.narrow( jndiContext.lookup("java:comp/env/database"), DataSource.class);
            Connection con = ds.getConnection();

            PreparedStatement stmt = con.prepareStatement("select * from Account where SSN = ?");
            stmt.setString(1, ssn);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;
            
            acct.setSsn( rs.getString(1) );
            acct.setFirstName( rs.getString(2) );
            acct.setLastName( rs.getString(3) );
            acct.setBalance( rs.getInt(4) );

            stmt.close();
            con.close();
        } catch (Exception e){
            e.printStackTrace();
            throw new RemoteException("[Bean] "+e.getClass().getName()+" : "+e.getMessage());
        }
        return acct;
    }


    //    
    // Remote interface methods
    //=============================


    //=================================
    // SessionBean interface methods
    //    
    /**
     * 
     * @param name
     * @exception javax.ejb.CreateException
     */
    public void ejbCreate() throws javax.ejb.CreateException{
        try {
            jndiContext = new InitialContext(); 
        } catch (Exception e){
            throw new CreateException("Can not get the initial context: "+e.getMessage());
        }
    }
    /**
     * Set the associated session context. The container calls this method
     * after the instance creation.
     */
    public void setSessionContext(SessionContext ctx) throws EJBException,RemoteException {
        ejbContext = ctx;
    }
    /**
     * A container invokes this method before it ends the life of the session
     * object. This happens as a result of a client's invoking a remove
     * operation, or when a container decides to terminate the session object
     * after a timeout.
     */
    public void ejbRemove() throws EJBException,RemoteException {
    }
    /**
     * The activate method is called when the instance is activated
     * from its "passive" state. The instance should acquire any resource
     * that it has released earlier in the ejbPassivate() method.
     */
    public void ejbActivate() throws EJBException,RemoteException {
    }
    /**
     * The passivate method is called before the instance enters
     * the "passive" state. The instance should release any resources that
     * it can re-acquire later in the ejbActivate() method.
     */
    public void ejbPassivate() throws EJBException,RemoteException {
    }
    //    
    // SessionBean interface methods
    //==================================
    
}
