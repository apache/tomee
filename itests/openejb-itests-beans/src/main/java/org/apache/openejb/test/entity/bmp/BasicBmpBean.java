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
package org.apache.openejb.test.entity.bmp;

import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.ejb.EJBException;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.ejb.NoSuchEntityException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.openejb.test.ApplicationException;
import org.apache.openejb.test.object.OperationsPolicy;

public class BasicBmpBean implements javax.ejb.EntityBean {

    //public static int keys = 100;
    public int primaryKey;
    public String firstName;
    public String lastName;
    public EntityContext ejbContext;
    public Hashtable allowedOperationsTable = new Hashtable();


    //=============================
    // Home interface methods
    //    

    /**
     * Maps to BasicBmpHome.sum
     *
     * Adds x and y and returns the result.
     *
     * @param x
     * @param y
     * @return x + y
     * @see BasicBmpHome#sum
     */
    public int ejbHomeSum(final int x, final int y) {
        testAllowedOperations("ejbHome");
        return x + y;
    }


    /**
     * Maps to BasicBmpHome.findEmptyCollection
     *
     * @return
     * @throws javax.ejb.FinderException
     * @see BasicBmpHome#sum
     */
    public java.util.Collection ejbFindEmptyCollection()
        throws javax.ejb.FinderException, java.rmi.RemoteException {
        return new java.util.Vector();
    }

    /**
     * Maps to BasicBmpHome.findEmptyEnumeration()
     *
     * @return empty enumeration
     * @throws javax.ejb.FinderException
     */
    public java.util.Enumeration ejbFindEmptyEnumeration()
        throws javax.ejb.FinderException {
        return (new java.util.Vector()).elements();
    }

    /**
     * Maps to BasicBmpHome.findByPrimaryKey
     *
     * @param primaryKey
     * @return
     * @throws javax.ejb.FinderException
     * @see BasicBmpHome#sum
     */
    public Integer ejbFindByPrimaryKey(final Integer primaryKey)
        throws javax.ejb.FinderException {
        boolean found = false;
        try {
            final InitialContext jndiContext = new InitialContext();
            final DataSource ds = (DataSource) jndiContext.lookup("java:comp/env/jdbc/basic/entityDatabase");
            final Connection con = ds.getConnection();

            try {
                final PreparedStatement stmt = con.prepareStatement("select * from entity where id = ?");
                try {
                    stmt.setInt(1, primaryKey.intValue());
                    found = stmt.executeQuery().next();
                } finally {
                    stmt.close();
                }
            } finally {
                con.close();
            }
        } catch (final Exception e) {
            throw new FinderException("FindByPrimaryKey failed");
        }

        if (found) return primaryKey;
        else throw new javax.ejb.ObjectNotFoundException();
    }

    /**
     * Maps to BasicBmpHome.findByPrimaryKey
     *
     * @param lastName
     * @return
     * @throws javax.ejb.FinderException
     * @see BasicBmpHome#sum
     */
    public java.util.Collection ejbFindByLastName(final String lastName)
        throws javax.ejb.FinderException {
        final java.util.Vector keys = new java.util.Vector();
        try {
            final InitialContext jndiContext = new InitialContext();
            final DataSource ds = (DataSource) jndiContext.lookup("java:comp/env/jdbc/basic/entityDatabase");
            final Connection con = ds.getConnection();

            try {
                final PreparedStatement stmt = con.prepareStatement("SELECT id FROM entity WHERE last_name = ?");
                try {
                    stmt.setString(1, lastName);
                    final ResultSet set = stmt.executeQuery();
                    while (set.next()) keys.add(new Integer(set.getInt("id")));
                } finally {
                    stmt.close();
                }
            } finally {
                con.close();
            }
        } catch (final Exception e) {
            throw new FinderException("FindByPrimaryKey failed");
        }

        if (keys.size() > 0) return keys;
        else throw new javax.ejb.ObjectNotFoundException();
    }

    /**
     * Maps to BasicBmpHome.create
     *
     * @param name
     * @return
     * @throws javax.ejb.CreateException
     * @see BasicBmpHome#createObject
     */
    public Integer ejbCreateObject(final String name)
        throws javax.ejb.CreateException {
        try {
            final StringTokenizer st = new StringTokenizer(name, " ");
            firstName = st.nextToken();
            lastName = st.nextToken();

            final InitialContext jndiContext = new InitialContext();

            final DataSource ds = (DataSource) jndiContext.lookup("java:comp/env/jdbc/basic/entityDatabase");

            final Connection con = ds.getConnection();

            try {
                // Support for Oracle because Oracle doesn't do auto increment
//          PreparedStatement stmt = con.prepareStatement("insert into entity (id, first_name, last_name) values (?,?,?)");
//          stmt.setInt(1, keys++);
//          stmt.setString(2, firstName);
//          stmt.setString(3, lastName);
//          stmt.executeUpdate();
                PreparedStatement stmt = con.prepareStatement("insert into entity (first_name, last_name) values (?,?)");
                try {
                    stmt.setString(1, firstName);
                    stmt.setString(2, lastName);
                    stmt.executeUpdate();
                } finally {
                    stmt.close();
                }

                stmt = con.prepareStatement("select id from entity where first_name = ? AND last_name = ?");
                try {
                    stmt.setString(1, firstName);
                    stmt.setString(2, lastName);
                    final ResultSet set = stmt.executeQuery();
                    while (set.next()) primaryKey = set.getInt("id");
                } finally {
                    stmt.close();
                }
            } finally {
                con.close();
            }

            return new Integer(primaryKey);

        } catch (final Exception e) {
            e.printStackTrace();
            throw new javax.ejb.CreateException("can't create: " + e.getClass().getName() + " " + e.getMessage());
        }
    }

    public void ejbPostCreateObject(final String name)
        throws javax.ejb.CreateException {
    }

    //    
    // Home interface methods
    //=============================


    //=============================
    // Remote interface methods
    //    

    /**
     * Maps to BasicBmpObject.businessMethod
     *
     * @return
     * @see BasicBmpObject#businessMethod
     */
    public String businessMethod(final String text) {
        testAllowedOperations("businessMethod");
        final StringBuffer b = new StringBuffer(text);
        return b.reverse().toString();
    }


    /**
     * Throws an ApplicationException when invoked
     */
    public void throwApplicationException() throws ApplicationException {
        throw new ApplicationException("Testing ability to throw Application Exceptions");
    }

    /**
     * Throws a java.lang.NullPointerException when invoked
     * This is a system exception and should result in the
     * destruction of the instance and invalidation of the
     * remote reference.
     */
    public void throwSystemException_NullPointer() {
        throw new NullPointerException("Testing ability to throw System Exceptions");
    }


    /**
     * Maps to BasicBmpObject.getPermissionsReport
     *
     * Returns a report of the bean's
     * runtime permissions
     *
     * @return
     * @see BasicBmpObject#getPermissionsReport
     */
    public Properties getPermissionsReport() {
        /* TO DO: */
        return null;
    }

    /**
     * Maps to BasicBmpObject.getAllowedOperationsReport
     *
     * Returns a report of the allowed opperations
     * for one of the bean's methods.
     *
     * @param methodName The method for which to get the allowed opperations report
     * @return
     * @see BasicBmpObject#getAllowedOperationsReport
     */
    public OperationsPolicy getAllowedOperationsReport(final String methodName) {
        return (OperationsPolicy) allowedOperationsTable.get(methodName);
    }

    //    
    // Remote interface methods
    //=============================


    //================================
    // EntityBean interface methods
    //    

    /**
     * A container invokes this method to instruct the
     * instance to synchronize its state by loading it state from the
     * underlying database.
     */
    public void ejbLoad() throws EJBException, RemoteException {

        try {
            final InitialContext jndiContext = new InitialContext();
            final DataSource ds = (DataSource) jndiContext.lookup("java:comp/env/jdbc/basic/entityDatabase");
            final Connection con = ds.getConnection();

            try {
                final PreparedStatement stmt = con.prepareStatement("select * from entity where id = ?");
                try {
                    stmt.setInt(1, primaryKey);
                    final ResultSet rs = stmt.executeQuery();
                    if (!rs.next()) {
                        throw new NoSuchEntityException("" + primaryKey);
                    }
                    lastName = rs.getString("last_name");
                    firstName = rs.getString("first_name");
                    if (rs.next()) {
                        throw new EJBException("Found more than one entity with id " + primaryKey);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                con.close();
            }
        } catch (final NamingException | SQLException e) {
            throw new EJBException(e);
        }
    }

    /**
     * Set the associated entity context. The container invokes this method
     * on an instance after the instance has been created.
     */
    public void setEntityContext(final EntityContext ctx) throws EJBException, RemoteException {
        ejbContext = ctx;
        testAllowedOperations("setEntityContext");
    }

    /**
     * Unset the associated entity context. The container calls this method
     * before removing the instance.
     */
    public void unsetEntityContext() throws EJBException, RemoteException {
        testAllowedOperations("unsetEntityContext");
    }

    /**
     * A container invokes this method to instruct the
     * instance to synchronize its state by storing it to the underlying
     * database.
     */
    public void ejbStore() throws EJBException, RemoteException {
        try {
            final InitialContext jndiContext = new InitialContext();
            final DataSource ds = (DataSource) jndiContext.lookup("java:comp/env/jdbc/basic/entityDatabase");
            final Connection con = ds.getConnection();

            try {
                final PreparedStatement stmt = con.prepareStatement("update entity set first_name = ?, last_name = ? where id = ?");
                try {
                    stmt.setString(1, firstName);
                    stmt.setString(2, lastName);
                    stmt.setInt(3, primaryKey);
                    stmt.execute();
                } finally {
                    stmt.close();
                }
            } finally {
                con.close();
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A container invokes this method before it removes the EJB object
     * that is currently associated with the instance. This method
     * is invoked when a client invokes a remove operation on the
     * enterprise Bean's home interface or the EJB object's remote interface.
     * This method transitions the instance from the ready state to the pool
     * of available instances.
     */
    public void ejbRemove() throws RemoveException, EJBException, RemoteException {
        try {
            final InitialContext jndiContext = new InitialContext();
            final DataSource ds = (DataSource) jndiContext.lookup("java:comp/env/jdbc/basic/entityDatabase");
            final Connection con = ds.getConnection();

            try {
                final PreparedStatement stmt = con.prepareStatement("delete from entity where id = ?");
                try {
                    stmt.setInt(1, primaryKey);
                    stmt.executeUpdate();
                } finally {
                    stmt.close();
                }
            } finally {
                con.close();
            }

        } catch (final Exception e) {
            e.printStackTrace();
            throw new javax.ejb.EJBException(e);
        }
    }

    /**
     * A container invokes this method when the instance
     * is taken out of the pool of available instances to become associated
     * with a specific EJB object. This method transitions the instance to
     * the ready state.
     */
    public void ejbActivate() throws EJBException, RemoteException {
        primaryKey = (Integer) ejbContext.getPrimaryKey();
        testAllowedOperations("ejbActivate");
    }

    /**
     * A container invokes this method on an instance before the instance
     * becomes disassociated with a specific EJB object. After this method
     * completes, the container will place the instance into the pool of
     * available instances.
     */
    public void ejbPassivate() throws EJBException, RemoteException {
        testAllowedOperations("ejbPassivate");
        primaryKey = -1;
    }


    //    
    // EntityBean interface methods
    //================================

    protected void testAllowedOperations(final String methodName) {
        final OperationsPolicy policy = new OperationsPolicy();

        /*[0] Test getEJBHome /////////////////*/
        try {
            ejbContext.getEJBHome();
            policy.allow(policy.Context_getEJBHome);
        } catch (final IllegalStateException ise) {
        }

        /*[1] Test getCallerPrincipal /////////*/
        try {
            ejbContext.getCallerPrincipal();
            policy.allow(policy.Context_getCallerPrincipal);
        } catch (final IllegalStateException ise) {
        }

        /*[2] Test isCallerInRole /////////////*/
        try {
            ejbContext.isCallerInRole("TheMan");
            policy.allow(policy.Context_isCallerInRole);
        } catch (final IllegalStateException ise) {
        }

        /*[3] Test getRollbackOnly ////////////*/
        try {
            ejbContext.getRollbackOnly();
            policy.allow(policy.Context_getRollbackOnly);
        } catch (final IllegalStateException ise) {
        }

        /*[4] Test setRollbackOnly ////////////*/
        try {
            ejbContext.setRollbackOnly();
            policy.allow(policy.Context_setRollbackOnly);
        } catch (final IllegalStateException ise) {
        }

        /*[5] Test getUserTransaction /////////*/
        try {
            ejbContext.getUserTransaction();
            policy.allow(policy.Context_getUserTransaction);
        } catch (final IllegalStateException ise) {
        }

        /*[6] Test getEJBObject ///////////////*/
        try {
            ejbContext.getEJBObject();
            policy.allow(policy.Context_getEJBObject);
        } catch (final IllegalStateException ise) {
        }

        /*[7] Test Context_getPrimaryKey ///////////////
         *
         * Can't really do this
         */

        /*[8] Test JNDI_access_to_java_comp_env ///////////////*/
        try {
            final InitialContext jndiContext = new InitialContext();

            final String actual = (String) jndiContext.lookup("java:comp/env/stateless/references/JNDI_access_to_java_comp_env");

            policy.allow(policy.JNDI_access_to_java_comp_env);
        } catch (final IllegalStateException | NamingException ise) {
        }

        allowedOperationsTable.put(methodName, policy);
    }

}
