/**
 *
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
package org.apache.openejb.test.entity.cmr;

import junit.framework.AssertionFailedError;
import org.apache.openejb.test.TestFailureException;
import org.apache.openejb.test.entity.cmr.cmrmapping.ManyOwningSideLocal;
import org.apache.openejb.test.entity.cmr.cmrmapping.ManyOwningSideLocalHome;
import org.apache.openejb.test.entity.cmr.cmrmapping.OneInverseSideLocal;
import org.apache.openejb.test.entity.cmr.cmrmapping.OneInverseSideLocalHome;
import org.apache.openejb.test.entity.cmr.cmrmapping.OneOwningSideLocal;
import org.apache.openejb.test.entity.cmr.cmrmapping.OneOwningSideLocalHome;
import org.junit.Assert;

import jakarta.ejb.TransactionRolledbackLocalException;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @version $Revision$ $Date$
 */
public class CmrMappingTests extends AbstractCMRTest {
    private Integer compoundPK_20_10;
    private Integer compoundPK_20_10_field1;
    private Integer compoundPK_20_20;
    private Integer compoundPK_20_20_field1;

    private Set<Integer> oneOwningCreated = new HashSet<Integer>();
    private Set<Integer> oneInverseCreated = new HashSet<Integer>();
    private Set<Integer> manyCreated = new HashSet<Integer>();

    private OneInverseSideLocalHome oneInverseHome;
    private OneOwningSideLocalHome oneOwningHome;
    private ManyOwningSideLocalHome manyHome;

    public CmrMappingTests() {
        super("CmrMappingTests.");
    }

    protected void setUp() throws Exception {
        super.setUp();
        compoundPK_20_10 = new Integer(20);
        compoundPK_20_10_field1 = new Integer(10);
        compoundPK_20_20 = new Integer(20);
        compoundPK_20_20_field1 = new Integer(20);

        oneInverseHome = (OneInverseSideLocalHome) initialContext.lookup("java:openejb/local/client/tests/entity/cmp2/OneInverseSideBean/EJBHomeLocal");
        oneOwningHome = (OneOwningSideLocalHome) initialContext.lookup("java:openejb/local/client/tests/entity/cmp2/OneOwningSideBean/EJBHomeLocal");
        manyHome = (ManyOwningSideLocalHome) initialContext.lookup("java:openejb/local/client/tests/entity/cmp2/ManyOwningSideBean/EJBHomeLocal");
    }

    public void testOneToOneSetCMROnOwningSide() throws Exception {
        beginTransaction();
        try {

            final OneInverseSideLocal inverseLocal = createOneInverseSide(compoundPK_20_10_field1);
            final OneOwningSideLocal owningLocal = createOneOwningSide(compoundPK_20_10, compoundPK_20_10_field1);
            owningLocal.setOneInverseSide(inverseLocal);
            completeTransaction();

            validateOneToOneRelationship();
        } catch (final Throwable e) {
            e.printStackTrace();
            throw new TestFailureException(new AssertionFailedError("Received Exception " + e.getClass() + " : " + e.getMessage()));
        } finally {
            completeTransaction();
            cleanDb();
        }
    }

    private void cleanDb() {
        for (final Integer id : oneOwningCreated) {
            try {

                findOneOwningSide(id).remove();
            } catch (final Exception e) {
            } finally {
                try {
                    completeTransaction();
                } catch (final Exception ignored) {
                }
            }
        }
        oneOwningCreated.clear();

        for (final Integer id : oneInverseCreated) {
            try {

                findOneInverseSide(id).remove();
            } catch (final Exception e) {
            } finally {
                try {
                    completeTransaction();
                } catch (final Exception ignored) {
                }
            }
        }
        oneInverseCreated.clear();

        for (final Integer id : manyCreated) {
            try {

                findManyOwningSide(id).remove();
            } catch (final Exception e) {
            } finally {
                try {
                    completeTransaction();
                } catch (final Exception ignored) {
                }
            }
        }
        manyCreated.clear();
    }

    public void testOneToOneSetCMROnOwningSideResetPK() throws Exception {
        beginTransaction();
        try {

            final OneInverseSideLocal inverseLocal = createOneInverseSide(compoundPK_20_10_field1);
            final OneOwningSideLocal owningLocal = createOneOwningSide(compoundPK_20_20, compoundPK_20_20_field1);
            owningLocal.setOneInverseSide(inverseLocal);
// todo should fail when we have fk as part of pk
//            Assert.fail();
            completeTransaction();
        } catch (final TransactionRolledbackLocalException e) {
            if (!(e.getCause() instanceof IllegalStateException)) {
                e.printStackTrace();
                throw new TestFailureException(new AssertionFailedError("Received Exception " + e.getClass() + " : " + e.getMessage()));
            }
        } catch (final Throwable e) {
            e.printStackTrace();
            throw new TestFailureException(new AssertionFailedError("Received Exception " + e.getClass() + " : " + e.getMessage()));
        } finally {
            completeTransaction();
            cleanDb();
        }
    }

    public void testOneToOneSetCMROnInverseSide() throws Exception {
        beginTransaction();
        try {

            final OneInverseSideLocal inverseLocal = createOneInverseSide(compoundPK_20_10_field1);
            final OneOwningSideLocal owningLocal = createOneOwningSide(compoundPK_20_10, compoundPK_20_10_field1);
            inverseLocal.setOneOwningSide(owningLocal);
            completeTransaction();

            validateOneToOneRelationship();
        } catch (final Throwable e) {
            e.printStackTrace();
            throw new TestFailureException(new AssertionFailedError("Received Exception " + e.getClass() + " : " + e.getMessage()));
        } finally {
            completeTransaction();
            cleanDb();
        }
    }

    public void testOneToOneSetCMROnInverseSideResetPK() throws Exception {
        beginTransaction();
        try {

            final OneInverseSideLocal inverseLocal = createOneInverseSide(compoundPK_20_10_field1);
            final OneOwningSideLocal owningLocal = createOneOwningSide(compoundPK_20_20, compoundPK_20_20_field1);
            inverseLocal.setOneOwningSide(owningLocal);
// todo should fail when we have fk as part of pk
//            Assert.fail();
            completeTransaction();
        } catch (final TransactionRolledbackLocalException e) {
            if (!(e.getCause() instanceof IllegalStateException)) {
                e.printStackTrace();
                throw new TestFailureException(new AssertionFailedError("Received Exception " + e.getClass() + " : " + e.getMessage()));
            }
        } catch (final Throwable e) {
            e.printStackTrace();
            throw new TestFailureException(new AssertionFailedError("Received Exception " + e.getClass() + " : " + e.getMessage()));
        } finally {
            completeTransaction();
            cleanDb();
        }
    }

    public void testOneToOneDoNotSetCMR() throws Exception {
        beginTransaction();
        try {

            createOneOwningSide(compoundPK_20_10, compoundPK_20_10_field1);
            completeTransaction();
// todo should fail when we have fk as part of pk
//            Assert.fail();
            completeTransaction();
        } catch (final IllegalStateException e) {
        } catch (final Throwable e) {
            e.printStackTrace();
            throw new TestFailureException(new AssertionFailedError("Received Exception " + e.getClass() + " : " + e.getMessage()));
        } finally {
            completeTransaction();
            cleanDb();
        }
    }

    public void testOneToManySetCMROnOwningSide() throws Exception {
        beginTransaction();
        try {

            final OneInverseSideLocal inverseLocal = createOneInverseSide(compoundPK_20_10_field1);
            final ManyOwningSideLocal owningLocal = createManyOwningSide(compoundPK_20_10, compoundPK_20_10_field1);
            owningLocal.setOneInverseSide(inverseLocal);
            completeTransaction();

            validateOneToManyRelationship();
        } catch (final Throwable e) {
            e.printStackTrace();
            throw new TestFailureException(new AssertionFailedError("Received Exception " + e.getClass() + " : " + e.getMessage()));
        } finally {
            completeTransaction();
            cleanDb();
        }
    }

    public void testEjbSelectWithCMR() throws Exception {
        beginTransaction();
        try {

            final OneInverseSideLocal inverseLocal = createOneInverseSide(compoundPK_20_10_field1);
            final ManyOwningSideLocal owningLocal = createManyOwningSide(compoundPK_20_10, compoundPK_20_10_field1);
            owningLocal.setOneInverseSide(inverseLocal);
            completeTransaction();

            owningLocal.testEJBSelect();
        } catch (final Throwable e) {
            e.printStackTrace();
            throw new TestFailureException(new AssertionFailedError("Received Exception " + e.getClass() + " : " + e.getMessage()));
        } finally {
            completeTransaction();
            cleanDb();
        }
    }

    public void testOneToManySetCMROnOwningSideResetPK() throws Exception {
        beginTransaction();
        try {

            final OneInverseSideLocal inverseLocal = createOneInverseSide(compoundPK_20_10_field1);
            final ManyOwningSideLocal owningLocal = createManyOwningSide(compoundPK_20_20, compoundPK_20_20_field1);
            owningLocal.setOneInverseSide(inverseLocal);
// todo should fail when we have fk as part of pk
//            Assert.fail();
            completeTransaction();
        } catch (final TransactionRolledbackLocalException e) {
            if (!(e.getCause() instanceof IllegalStateException)) {
                e.printStackTrace();
                throw new TestFailureException(new AssertionFailedError("Received Exception " + e.getClass() + " : " + e.getMessage()));
            }
        } catch (final Throwable e) {
            e.printStackTrace();
            throw new TestFailureException(new AssertionFailedError("Received Exception " + e.getClass() + " : " + e.getMessage()));
        } finally {
            completeTransaction();
            cleanDb();
        }
    }

    public void testOneToManySetCMROnInverseSide() throws Exception {
        beginTransaction();
        try {

            final OneInverseSideLocal inverseLocal = createOneInverseSide(compoundPK_20_10_field1);
            final ManyOwningSideLocal owningLocal = createManyOwningSide(compoundPK_20_10, compoundPK_20_10_field1);
            inverseLocal.setManyOwningSide(Collections.singleton(owningLocal));
            completeTransaction();

            validateOneToManyRelationship();
        } catch (final Throwable e) {
            e.printStackTrace();
            throw new TestFailureException(new AssertionFailedError("Received Exception " + e.getClass() + " : " + e.getMessage()));
        } finally {
            completeTransaction();
            cleanDb();
        }
    }

    public void testOneToManySetCMROnInverseSideResetPK() throws Exception {
        beginTransaction();
        try {

            final OneInverseSideLocal inverseLocal = createOneInverseSide(compoundPK_20_10_field1);
            final ManyOwningSideLocal owningLocal = createManyOwningSide(compoundPK_20_20, compoundPK_20_20_field1);
            inverseLocal.setManyOwningSide(Collections.singleton(owningLocal));
// todo should fail when we have fk as part of pk
//            Assert.fail();
            completeTransaction();
        } catch (final TransactionRolledbackLocalException e) {
            if (!(e.getCause() instanceof IllegalStateException)) {
                e.printStackTrace();
                throw new TestFailureException(new AssertionFailedError("Received Exception " + e.getClass() + " : " + e.getMessage()));
            }
        } catch (final Throwable e) {
            e.printStackTrace();
            throw new TestFailureException(new AssertionFailedError("Received Exception " + e.getClass() + " : " + e.getMessage()));
        } finally {
            completeTransaction();
            cleanDb();
        }
    }

    public void testOneToManyDoNotSetCMR() throws Exception {
        beginTransaction();
        try {

            createManyOwningSide(compoundPK_20_10, compoundPK_20_10_field1);
            completeTransaction();
// todo should fail when we have fk as part of pk
//            Assert.fail();
            completeTransaction();
        } catch (final IllegalStateException e) {
        } catch (final Throwable e) {
            e.printStackTrace();
            throw new TestFailureException(new AssertionFailedError("Received Exception " + e.getClass() + " : " + e.getMessage()));
        } finally {
            completeTransaction();
            cleanDb();
        }
    }

    private OneInverseSideLocal createOneInverseSide(final Integer id) throws Exception {
        final OneInverseSideLocalHome home = oneInverseHome;
        final OneInverseSideLocal oneInverseSideLocal = home.create(id);
        oneInverseCreated.add(id);
        return oneInverseSideLocal;
    }

    private OneInverseSideLocal findOneInverseSide(final Integer id) throws Exception {
        final OneInverseSideLocalHome home = oneInverseHome;
        return home.findByPrimaryKey(id);
    }

    private void validateOneToOneRelationship() throws Exception {
        try {
            final OneInverseSideLocal inverseLocal = findOneInverseSide(compoundPK_20_10_field1);

            final OneOwningSideLocal oneOwningSide = inverseLocal.getOneOwningSide();
            Assert.assertNotNull(oneOwningSide);
            Assert.assertEquals(compoundPK_20_10, oneOwningSide.getPrimaryKey());

            final OneInverseSideLocal inverseBackRef = oneOwningSide.getOneInverseSide();
            Assert.assertNotNull(inverseBackRef);
            Assert.assertEquals(compoundPK_20_10_field1, inverseBackRef.getPrimaryKey());
            completeTransaction();
        } finally {
            completeTransaction();
        }
    }

    private OneOwningSideLocal createOneOwningSide(final Integer id, final Integer field1) throws Exception {
        final OneOwningSideLocalHome home = oneOwningHome;
        final OneOwningSideLocal oneOwningSideLocal = home.create(id, field1);
        oneOwningCreated.add(id);
        return oneOwningSideLocal;
    }

    private OneOwningSideLocal findOneOwningSide(final Integer id) throws Exception {
        final OneOwningSideLocalHome home = oneOwningHome;
        return home.findByPrimaryKey(id);
    }

    private ManyOwningSideLocal createManyOwningSide(final Integer id, final Integer field1) throws Exception {
        final ManyOwningSideLocalHome home = manyHome;
        final ManyOwningSideLocal manyOwningSideLocal = home.create(id, field1);
        manyCreated.add(id);
        return manyOwningSideLocal;
    }

    private ManyOwningSideLocal findManyOwningSide(final Integer id) throws Exception {
        final ManyOwningSideLocalHome home = manyHome;
        return home.findByPrimaryKey(id);
    }

    private void validateOneToManyRelationship() throws NotSupportedException, SystemException, Exception, HeuristicMixedException, HeuristicRollbackException, RollbackException {
        try {
            final OneInverseSideLocal inverseLocal = findOneInverseSide(compoundPK_20_10_field1);


            // verify one side has a set containing the many bean
            final Set set = inverseLocal.getManyOwningSide();
            Assert.assertEquals(1, set.size());
            final ManyOwningSideLocal owningLocal = (ManyOwningSideLocal) set.iterator().next();
            Assert.assertEquals(compoundPK_20_10, owningLocal.getPrimaryKey());

            // verify the many bean has a back reference to the one
            final OneInverseSideLocal oneInverseSide = owningLocal.getOneInverseSide();
            Assert.assertNotNull(oneInverseSide);
            Assert.assertEquals(compoundPK_20_10_field1, oneInverseSide.getPrimaryKey());

            completeTransaction();
        } finally {
            completeTransaction();
        }
    }
}
