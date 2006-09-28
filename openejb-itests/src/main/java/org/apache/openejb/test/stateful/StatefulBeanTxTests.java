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
package org.apache.openejb.test.stateful;

import java.util.Properties;

import javax.ejb.EJBMetaData;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.transaction.RollbackException;

import org.apache.openejb.test.TestManager;
import org.apache.openejb.test.object.Account;
import org.apache.openejb.test.object.Transaction;

/**
 * [1] Should be run as the first test suite of the StatefulTestClients
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class StatefulBeanTxTests extends org.apache.openejb.test.NamedTestCase{

    public final static String jndiEJBHomeEntry = "client/tests/stateful/BeanManagedTransactionTests/EJBHome";

    protected BeanTxStatefulHome   ejbHome;
    protected BeanTxStatefulObject ejbObject;

    protected EJBMetaData       ejbMetaData;
    protected HomeHandle        ejbHomeHandle;
    protected Handle            ejbHandle;
    protected Integer           ejbPrimaryKey;

    protected InitialContext initialContext;

    public StatefulBeanTxTests(){
        super("Stateful.BeanManagedTransaction.");
    }

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() throws Exception {

        Properties properties = TestManager.getServer().getContextEnvironment();
        properties.put(Context.SECURITY_PRINCIPAL, "STATEFUL_test00_CLIENT");
        properties.put(Context.SECURITY_CREDENTIALS, "STATEFUL_test00_CLIENT");

        initialContext = new InitialContext(properties);

        /*[1] Get bean */
        Object obj = initialContext.lookup(jndiEJBHomeEntry);
        ejbHome = (BeanTxStatefulHome)javax.rmi.PortableRemoteObject.narrow( obj, BeanTxStatefulHome.class);
        ejbObject = ejbHome.create("Transaction Bean");

        /*[2] Create database table */
        TestManager.getDatabase().createAccountTable();
    }

    /**
     * Tears down the fixture, for example, close a network connection.
     * This method is called after a test is executed.
     */
    protected void tearDown() throws Exception {
        /*[1] Drop database table */
        TestManager.getDatabase().dropAccountTable();
    }


    /**
     * <B>11.6.1 Bean-managed transaction demarcation</B>
     * <P>
     * The Container must make the javax.transaction.UserTransaction interface available to
     * the enterprise bean’s business method via the javax.ejb.EJBContext interface and under the
     * environment entry java:comp/UserTransaction. When an instance uses the javax.trans-action.
     * UserTransaction interface to demarcate a transaction, the Container must enlist all the
     * resource managers used by the instance between the begin() and commit()—or rollback()—
     * methods with the transaction. When the instance attempts to commit the transaction, the Container is
     * responsible for the global coordination of the transaction commit.
     * </P>
     * <P>--------------------------------------------------------</P>
     * <P>
     * Check that a javax.transaction.UserTransaction can be obtained from
     * the javax.ejb.EJBContext
     * </P>
     */
    public void test01_EJBContext_getUserTransaction(){
        try{
           Transaction t = ejbObject.getUserTransaction();
           assertNotNull("UserTransaction is null.", t);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    /**
     *
     * <B>11.6.1 Bean-managed transaction demarcation</B>
     * <P>
     * The Container must make the javax.transaction.UserTransaction interface available to
     * the enterprise bean’s business method via the javax.ejb.EJBContext interface and under the
     * environment entry java:comp/UserTransaction. When an instance uses the javax.trans-action.
     * UserTransaction interface to demarcate a transaction, the Container must enlist all the
     * resource managers used by the instance between the begin() and commit()—or rollback()—
     * methods with the transaction. When the instance attempts to commit the transaction, the Container is
     * responsible for the global coordination of the transaction commit.
     * </P>
     * <P>--------------------------------------------------------</P>
     * <P>
     * Check that a javax.transaction.UserTransaction can be obtained from
     * the environment entry java:comp/UserTransaction
     * </P>
     */
    public void test02_java_comp_UserTransaction(){
        try{
            Transaction t = ejbObject.jndiUserTransaction();
            assertNotNull("UserTransaction is null. Could not retreive a UserTransaction from the bean's JNDI namespace.", t);
        } catch (Exception e){
            fail("Could not retreive a UserTransaction from the bean's JNDI namespace. Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    /**
     * <B>11.6.1 Bean-managed transaction demarcation</B>
     * <P>
     * The Container must throw the java.lang.IllegalStateException if an instance of a bean
     * with bean-managed transaction demarcation attempts to invoke the setRollbackOnly() or
     * getRollbackOnly() method of the javax.ejb.EJBContext interface.
     * </P>
     * <P>--------------------------------------------------------</P>
     * <P>
     * Test that setRollbackOnly() throws a java.lang.IllegalStateException
     * </P>
     */
    public void TODO_test03_EJBContext_setRollbackOnly(){
        try{

        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    /**
     * <B>11.6.1 Bean-managed transaction demarcation</B>
     * <P>
     * The Container must throw the java.lang.IllegalStateException if an instance of a bean
     * with bean-managed transaction demarcation attempts to invoke the setRollbackOnly() or
     * getRollbackOnly() method of the javax.ejb.EJBContext interface.
     * </P>
     * <P>--------------------------------------------------------</P>
     * <P>
     * Test that getRollbackOnly() throws a java.lang.IllegalStateException
     * </P>
     */
    public void TODO_test04_EJBContext_getRollbackOnly(){
        try{

        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    /**
     *
     */
    public void test05_singleTransactionCommit(){
        try{
            Account expected = new Account("123-45-6789","Joe","Cool",40000);
            Account actual = new Account();

            ejbObject.openAccount(expected, new Boolean(false));
            actual = ejbObject.retreiveAccount( expected.getSsn() );

            assertNotNull( "The transaction was not commited.  The record is null", actual );
            assertEquals( "The transaction was not commited cleanly.", expected, actual );
        } catch (RollbackException re){
            fail("Transaction was rolledback.  Received Exception "+re.getClass()+ " : "+re.getMessage());
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    /**
     * This test does work for the IntraVM Server, but it fails on 
     * the Remote Server.  For some reason, when the RollbackException is
     * sent to the client, the server blocks.
     */
    public void BUG_test06_singleTransactionRollback(){
        Account expected = new Account("234-56-7890","Charlie","Brown", 20000);
        Account actual   = new Account();

        // Try and add the account in a transaction.  This should fail and 
        // throw a RollbackException
        try{
            ejbObject.openAccount(expected, new Boolean(true));
            fail( "A javax.transaction.RollbackException should have been thrown." );
        } catch (RollbackException re){
            // Good.
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
        
      //// Now check that the account really wasn't added.
      //try{
      //    actual = ejbObject.retreiveAccount( expected.getSsn() );
      //    //assertTrue( "The transaction was commited when it should have been rolledback.", !expected.equals(actual) );
      //} catch (Exception e){
      //    fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
      //}
    }


    /**
     * <B>11.6.1 Bean-managed transaction demarcation</B>
     * <P>
     * The Container must allow the enterprise bean instance to serially perform several transactions in a
     * method.
     * </P>
     */
    public void TODO_test07_serialTransactions(){
        try{

        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    /**
     * <B>11.6.1 Bean-managed transaction demarcation</B>
     * <P>
     * When an instance attempts to start a transaction using the
     * begin() method of the javax.transaction.UserTransaction
     * interface while the instance has not committed the previous
     * transaction, the Container must throw the
     * javax.transaction.NotSupportedException in the begin() method.
     * </P>
     */
    public void TODO_test08_nestedTransactions(){
        try{

        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    /**
     * <B>11.6.1 Bean-managed transaction demarcation</B>
     * <P>
     * In the case of a stateful session bean, it is possible that the
     * business method that started a transaction completes without
     * committing or rolling back the transaction. In such a case, the
     * Container must retain the association between the transaction
     * and the instance across multiple client calls until the instance
     * commits or rolls back the transaction. When the client invokes
     * the next business method, the Container must invoke the business
     * method in this transaction context.
     * </P>
     */
    public void TODO_test09_methodSpanningTransactions(){
        try{

        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    /**
     * <B>11.6.1 Bean-managed transaction demarcation</B>
     * <P>
     * The actions performed by the Container for an instance with bean-managed transaction are summarized
     * by the following table. T1 is a transaction associated with a client request, T2 is a transaction that is cur-rently
     * associated with the instance (i.e. a transaction that was started but not completed by a previous
     * business method).
     * </P>
     * <PRE>
     * =========================================================================
     * Container’s actions for methods of beans with bean-managed transaction
     * =========================================================================
     *
     *            |      IF     |          AND             |          THEN
     *  scenario  |   Client’s  | Transaction currently    | Transaction associated
     *            | transaction | associated with instance | with the method is
     * ___________|_____________|__________________________|________________________
     *            |             |                          |
     *    1       |  none       |  none                    |  none
     * ___________|_____________|__________________________|________________________
     *            |             |                          |
     *    2       |  T1         |  none                    |  none
     * ___________|_____________|__________________________|________________________
     *            |             |                          |
     *    3       |  none       |  T2                      |  T2
     * ___________|_____________|__________________________|________________________
     *            |             |                          |
     *    4       |  T1         |  T2                      |  T2
     * ___________|_____________|__________________________|________________________
     * </PRE>
     * <P>
     * If the client request is not associated with a transaction and the instance is not associated with a
     * transaction, the container invokes the instance with an unspecified transaction context.
     * </P>
     * <P>--------------------------------------------------------</P>
     * <P>
     * Test scenario 1: none none<BR>
     * If the client's transaction is none and the transaction currently
     * associated with instance none then the transaction associated with the method is none.
     * </P>
     */
    public void TODO_test10_scenario1_NoneNone(){
        try{

        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    /**
     * <B>11.6.1 Bean-managed transaction demarcation</B>
     * <P>
     * The actions performed by the Container for an instance with bean-managed transaction are summarized
     * by the following table. T1 is a transaction associated with a client request, T2 is a transaction that is cur-rently
     * associated with the instance (i.e. a transaction that was started but not completed by a previous
     * business method).
     * </P>
     * <PRE>
     * =========================================================================
     * Container’s actions for methods of beans with bean-managed transaction
     * =========================================================================
     *
     *            |      IF     |          AND             |          THEN
     *  scenario  |   Client’s  | Transaction currently    | Transaction associated
     *            | transaction | associated with instance | with the method is
     * ___________|_____________|__________________________|________________________
     *            |             |                          |
     *    1       |  none       |  none                    |  none
     * ___________|_____________|__________________________|________________________
     *            |             |                          |
     *    2       |  T1         |  none                    |  none
     * ___________|_____________|__________________________|________________________
     *            |             |                          |
     *    3       |  none       |  T2                      |  T2
     * ___________|_____________|__________________________|________________________
     *            |             |                          |
     *    4       |  T1         |  T2                      |  T2
     * ___________|_____________|__________________________|________________________
     * </PRE>
     * <P>
     * If the client is associated with a transaction T1, and the instance is not associated with a transaction,
     * the container suspends the client’s transaction association and invokes the method with
     * an unspecified transaction context. The container resumes the client’s transaction association
     * (T1) when the method completes.
     * </P>
     * <P>--------------------------------------------------------</P>
     * <P>
     * Test scenario 2: T1 none<BR>
     * </P>
     */
    public void TODO_test11_scenario2_T1None(){
        try{

        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    /**
     * <B>11.6.1 Bean-managed transaction demarcation</B>
     * <P>
     * The actions performed by the Container for an instance with bean-managed transaction are summarized
     * by the following table. T1 is a transaction associated with a client request, T2 is a transaction that is cur-rently
     * associated with the instance (i.e. a transaction that was started but not completed by a previous
     * business method).
     * </P>
     * <PRE>
     * =========================================================================
     * Container’s actions for methods of beans with bean-managed transaction
     * =========================================================================
     *
     *            |      IF     |          AND             |          THEN
     *  scenario  |   Client’s  | Transaction currently    | Transaction associated
     *            | transaction | associated with instance | with the method is
     * ___________|_____________|__________________________|________________________
     *            |             |                          |
     *    1       |  none       |  none                    |  none
     * ___________|_____________|__________________________|________________________
     *            |             |                          |
     *    2       |  T1         |  none                    |  none
     * ___________|_____________|__________________________|________________________
     *            |             |                          |
     *    3       |  none       |  T2                      |  T2
     * ___________|_____________|__________________________|________________________
     *            |             |                          |
     *    4       |  T1         |  T2                      |  T2
     * ___________|_____________|__________________________|________________________
     * </PRE>
     * <P>
     * If the client request is not associated with a transaction and the instance is already associated
     * with a transaction T2, the container invokes the instance with the transaction that is associated
     * with the instance (T2). This case can never happen for a stateless Session Bean.
     * </P>
     * <P>--------------------------------------------------------</P>
     * <P>
     * Test scenario 3: none T2<BR>
     * </P>
     */
    public void TODO_test12_scenario3_NoneT2(){
        try{

        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    /**
     * <B>11.6.1 Bean-managed transaction demarcation</B>
     * <P>
     * The actions performed by the Container for an instance with bean-managed transaction are summarized
     * by the following table. T1 is a transaction associated with a client request, T2 is a transaction that is cur-rently
     * associated with the instance (i.e. a transaction that was started but not completed by a previous
     * business method).
     * </P>
     * <PRE>
     * =========================================================================
     * Container’s actions for methods of beans with bean-managed transaction
     * =========================================================================
     *
     *            |      IF     |          AND             |          THEN
     *  scenario  |   Client’s  | Transaction currently    | Transaction associated
     *            | transaction | associated with instance | with the method is
     * ___________|_____________|__________________________|________________________
     *            |             |                          |
     *    1       |  none       |  none                    |  none
     * ___________|_____________|__________________________|________________________
     *            |             |                          |
     *    2       |  T1         |  none                    |  none
     * ___________|_____________|__________________________|________________________
     *            |             |                          |
     *    3       |  none       |  T2                      |  T2
     * ___________|_____________|__________________________|________________________
     *            |             |                          |
     *    4       |  T1         |  T2                      |  T2
     * ___________|_____________|__________________________|________________________
     * </PRE>
     * <P>
     * If the client is associated with a transaction T1, and the instance is already associated with a
     * transaction T2, the container suspends the client’s transaction association and invokes the
     * method with the transaction context that is associated with the instance (T2). The container
     * resumes the client’s transaction association (T1) when the method completes. This case can
     * never happen for a stateless Session Bean.
     * </P>
     * <P>--------------------------------------------------------</P>
     * <P>
     * Test scenario 4: T1 T2<BR>
     * </P>
     */
    public void TODO_test13_scenario4_T1T2(){
        try{

        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
}
