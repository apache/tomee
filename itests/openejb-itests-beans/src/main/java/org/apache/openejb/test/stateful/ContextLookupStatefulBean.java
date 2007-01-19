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
package org.apache.openejb.test.stateful;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.apache.openejb.test.TestFailureException;
import org.apache.openejb.test.entity.bmp.BasicBmpHome;
import org.apache.openejb.test.entity.bmp.BasicBmpObject;
import org.apache.openejb.test.stateless.BasicStatelessHome;
import org.apache.openejb.test.stateless.BasicStatelessObject;

import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.SessionSynchronization;
import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import java.rmi.RemoteException;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class ContextLookupStatefulBean implements javax.ejb.SessionBean, SessionSynchronization {


    private String name;
    private SessionContext ejbContext;
    private EntityManager extendedEntityManager;

    // Used for testing propigation
    private static EntityManager inheritedDelegate;

    //=============================
    // Home interface methods
    //

    /**
     * Maps to EncStatefulHome.create
     *
     * @param name
     * @throws javax.ejb.CreateException
     * @see EncStatefulHome#create
     */
    public void ejbCreate(String name) throws CreateException {
        this.name = name;
    }
    //
    // Home interface methods
    //=============================

    //=============================
    // Remote interface methods
    //

    public void lookupEntityBean() throws TestFailureException {
        try {
            try {
                BasicBmpHome home = (BasicBmpHome) ejbContext.lookup("stateful/beanReferences/bmp_entity");
                Assert.assertNotNull("The EJBHome looked up is null", home);

                BasicBmpObject object = home.createObject("Enc Bean");
                Assert.assertNotNull("The EJBObject is null", object);
            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatefulBean() throws TestFailureException {
        try {
            try {
                BasicStatefulHome home = (BasicStatefulHome) ejbContext.lookup("stateful/beanReferences/stateful");
                Assert.assertNotNull("The EJBHome looked up is null", home);

                BasicStatefulObject object = home.createObject("Enc Bean");
                Assert.assertNotNull("The EJBObject is null", object);
            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatelessBean() throws TestFailureException {
        try {
            try {
                BasicStatelessHome home = (BasicStatelessHome) ejbContext.lookup("stateful/beanReferences/stateless");
                Assert.assertNotNull("The EJBHome looked up is null", home);

                BasicStatelessObject object = home.createObject();
                Assert.assertNotNull("The EJBObject is null", object);
            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStringEntry() throws TestFailureException {
        try {
            try {
                String expected = new String("1");
                String actual = (String) ejbContext.lookup("stateful/references/String");

                Assert.assertNotNull("The String looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupDoubleEntry() throws TestFailureException {
        try {
            try {
                Double expected = new Double(1.0D);
                Double actual = (Double) ejbContext.lookup("stateful/references/Double");

                Assert.assertNotNull("The Double looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupLongEntry() throws TestFailureException {
        try {
            try {
                Long expected = new Long(1L);
                Long actual = (Long) ejbContext.lookup("stateful/references/Long");

                Assert.assertNotNull("The Long looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupFloatEntry() throws TestFailureException {
        try {
            try {
                Float expected = new Float(1.0F);
                Float actual = (Float) ejbContext.lookup("stateful/references/Float");

                Assert.assertNotNull("The Float looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupIntegerEntry() throws TestFailureException {
        try {
            try {
                Integer expected = new Integer(1);
                Integer actual = (Integer) ejbContext.lookup("stateful/references/Integer");

                Assert.assertNotNull("The Integer looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupShortEntry() throws TestFailureException {
        try {
            try {
                Short expected = new Short((short) 1);
                Short actual = (Short) ejbContext.lookup("stateful/references/Short");

                Assert.assertNotNull("The Short looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupBooleanEntry() throws TestFailureException {
        try {
            try {
                Boolean expected = new Boolean(true);
                Boolean actual = (Boolean) ejbContext.lookup("stateful/references/Boolean");

                Assert.assertNotNull("The Boolean looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupByteEntry() throws TestFailureException {
        try {
            try {
                Byte expected = new Byte((byte) 1);
                Byte actual = (Byte) ejbContext.lookup("stateful/references/Byte");

                Assert.assertNotNull("The Byte looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupCharacterEntry() throws TestFailureException {
        try {
            try {
                Character expected = new Character('D');
                Character actual = (Character) ejbContext.lookup("stateful/references/Character");

                Assert.assertNotNull("The Character looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupResource() throws TestFailureException {
        try {
            try {
                Object obj = ejbContext.lookup("datasource");
                Assert.assertNotNull("The DataSource is null", obj);
                Assert.assertTrue("Not an instance of DataSource", obj instanceof DataSource);
            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }
    
    public void lookupPersistenceUnit() throws TestFailureException{
        try{
            try{
                InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);                
                EntityManagerFactory emf = (EntityManagerFactory)ctx.lookup("java:comp/env/persistence/TestUnit");
                Assert.assertNotNull("The EntityManagerFactory is null", emf );

            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }
    
    public void lookupPersistenceContext() throws TestFailureException{
        try{
            try{
                InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);
                EntityManager em = (EntityManager)ctx.lookup("java:comp/env/persistence/TestContext");
                Assert.assertNotNull("The EntityManager is null", em);

                // call a do nothing method to assure entity manager actually exists
                em.getFlushMode();
            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }


    public void lookupSessionContext() throws TestFailureException{
        try{
            try{
                InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                // lookup in enc
                SessionContext sctx = (SessionContext)ctx.lookup("java:comp/env/sessioncontext");
                Assert.assertNotNull("The SessionContext got from java:comp/env/sessioncontext is null", sctx );

                // lookup using global name
                EJBContext ejbCtx = (EJBContext)ctx.lookup("java:comp/EJBContext");
                Assert.assertNotNull("The SessionContext got from java:comp/EJBContext is null ", ejbCtx );                

                // verify context was set via legacy set method
                Assert.assertNotNull("The SessionContext is null from setter method", ejbContext );
            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
        
    }
    
    //
    // Remote interface methods
    //=============================

    //=================================
    // SessionBean interface methods
    //

    /**
     * Set the associated session context. The container calls this method
     * after the instance creation.
     */
    public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException {
        ejbContext = ctx;
    }

    /**
     * A container invokes this method before it ends the life of the session
     * object. This happens as a result of a client's invoking a remove
     * operation, or when a container decides to terminate the session object
     * after a timeout.
     */
    public void ejbRemove() throws EJBException, RemoteException {
    }

    /**
     * The activate method is called when the instance is activated
     * from its "passive" state. The instance should acquire any resource
     * that it has released earlier in the ejbPassivate() method.
     */
    public void ejbActivate() throws EJBException, RemoteException {
    }

    /**
     * The passivate method is called before the instance enters
     * the "passive" state. The instance should release any resources that
     * it can re-acquire later in the ejbActivate() method.
     */
    public void ejbPassivate() throws EJBException, RemoteException {
    }
    //
    // SessionBean interface methods
    //==================================

    //============================================
    // SessionSynchronization interface methods
    //

    /**
     * The afterBegin method notifies a session Bean instance that a new
     * transaction has started, and that the subsequent business methods on the
     * instance will be invoked in the context of the transaction.
     */
    public void afterBegin() throws EJBException, RemoteException {
    }

    /**
     * The beforeCompletion method notifies a session Bean instance that
     * a transaction is about to be committed. The instance can use this
     * method, for example, to write any cached data to a database.
     */
    public void beforeCompletion() throws EJBException, RemoteException {
    }

    /**
     * The afterCompletion method notifies a session Bean instance that a
     * transaction commit protocol has completed, and tells the instance
     * whether the transaction has been committed or rolled back.
     */
    public void afterCompletion(boolean committed) throws EJBException, RemoteException {
    }
    //
    // SessionSynchronization interface methods
    //============================================

}
