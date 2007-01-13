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

import org.apache.openejb.test.TestFailureException;
import org.apache.openejb.test.stateless.BasicStatelessHome;
import org.apache.openejb.test.stateless.BasicStatelessObject;
import org.apache.openejb.test.entity.bmp.BasicBmpHome;
import org.apache.openejb.test.entity.bmp.BasicBmpObject;

import javax.ejb.SessionContext;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class ContextLookupStatefulPojoBean {

    private EntityManager extendedEntityManager;
    private SessionContext ejbContext;

    // Used for testing propigation
    private static EntityManager inheritedDelegate;

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

    public void lookupExtendedPersistenceContext() throws TestFailureException{
        try{
            try{
                InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);
                EntityManager em = (EntityManager)ctx.lookup("java:comp/env/persistence/ExtendedTestContext");
                Assert.assertNotNull("The EntityManager is null", em);

                // call a do nothing method to assure entity manager actually exists
                em.getFlushMode();

                if (extendedEntityManager != null) {
                    Assert.assertSame("Extended entity manager should be the same instance that was found last time",
                            extendedEntityManager,
                            em);
                    Assert.assertSame("Extended entity manager delegate should be the same instance that was found last time",
                            extendedEntityManager.getDelegate(),
                            em.getDelegate());
                }
                extendedEntityManager = em;
            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    public void lookupPropagatedPersistenceContext() throws TestFailureException{
        try{
            try{
                InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);
                EntityManager em = (EntityManager)ctx.lookup("java:comp/env/persistence/ExtendedTestContext");
                Assert.assertNotNull("The EntityManager is null", em);

                // call a do nothing method to assure entity manager actually exists
                em.getFlushMode();

                // get the raw entity manager so we can test it below
                inheritedDelegate = (EntityManager) em.getDelegate();

                // The extended entity manager is not propigated to a non-extended entity manager unless there is a transaction
                EntityManager nonExtendedEm = (EntityManager)ctx.lookup("java:comp/env/persistence/TestContext");
                nonExtendedEm.getFlushMode();
                EntityManager nonExtendedDelegate = ((EntityManager) nonExtendedEm.getDelegate());
                Assert.assertTrue("non-extended entity manager should be open", nonExtendedDelegate.isOpen());
                Assert.assertNotSame("Extended non-extended entity manager shound not be the same instance as extendend entity manager when accessed out side of a transactions",
                        inheritedDelegate,
                        nonExtendedDelegate);

                // When the non-extended entity manager is accessed within a transaction is should see the stateful extended context.
                //
                // Note: this code also tests EBJ 3.0 Persistence spec 5.9.1 "UserTransaction is begun within the method, the
                // container associates the persistence context with the JTA transaction and calls EntityManager.joinTransaction."
                // If our the extended entity manager were not associted with the transaction, the non-extended entity manager would
                // not see it.
                UserTransaction userTransaction = ejbContext.getUserTransaction();
                userTransaction.begin();
                try {
                    Assert.assertSame("Extended non-extended entity manager to be same instance as extendend entity manager",
                            inheritedDelegate,
                            nonExtendedEm.getDelegate());
                } finally {
                    userTransaction.commit();
                }

                // When a stateful bean with an extended entity manager creates another stateful bean, the new bean will
                // inherit the extended entity manager (assuming it contains an extended entity manager for the same persistence
                // unit).
                EncStatefulHome home = (EncStatefulHome) ejbContext.getEJBHome();
                EncStatefulObject encStatefulObject = home.create("PropagatedPersistenceContext");

                // test the new stateful bean recieved the context
                encStatefulObject.testPropgation();

                // remove the bean
                encStatefulObject.remove();
            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    public void testPropgation() throws TestFailureException {
        if (inheritedDelegate == null) return;
        try {
            try{
                InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);
                EntityManager em = (EntityManager)ctx.lookup("java:comp/env/persistence/ExtendedTestContext");
                Assert.assertNotNull("The EntityManager is null", em);

                // call a do nothing method to assure entity manager actually exists
                em.getFlushMode();

                EntityManager delegate = (EntityManager) em.getDelegate();
                Assert.assertSame("Extended entity manager delegate should be the same instance that was found last time",
                        inheritedDelegate,
                        delegate);
            } catch (Exception e){
                e.printStackTrace();
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }


    /**
     * Set the associated session context. The container calls this method
     * after the instance creation.
     */
    public void setSessionContext(SessionContext ctx) {
        ejbContext = ctx;
    }
}
