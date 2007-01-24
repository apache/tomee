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
package org.apache.openejb.test.stateless;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.apache.openejb.test.TestFailureException;
import org.apache.openejb.test.entity.bmp.BasicBmpHome;
import org.apache.openejb.test.stateful.BasicStatefulBusinessLocal;
import org.apache.openejb.test.stateful.BasicStatefulBusinessRemote;
import org.apache.openejb.test.stateful.BasicStatefulHome;

import javax.annotation.Resource;
import javax.ejb.CreateException;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.RemoteHome;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

@RemoteHome(org.apache.openejb.test.stateless.EncStatelessHome.class)
@Stateless
public class AnnotatedFieldInjectionStatelessBean {

    @Resource
    private SessionContext ejbContext;
    @EJB(beanName = "BasicBmpBean")
    private BasicBmpHome bmpHome;
    @EJB(beanName = "BasicStatefulBean")
    private BasicStatefulHome statefulHome;
    @EJB(beanName = "BasicStatelessBean")
    private BasicStatelessHome statelessHome;
    @Resource
    private String striing = "1";
    @Resource
    private Double doouble = 1.0D;
    @Resource
    private Long loong = 1L;
    @Resource
    private Float flooat = 1.0F;
    @Resource
    private Integer inteeger = 1;
    @Resource
    private Short shoort = (short)1;
    @Resource
    private Boolean booolean = true;
    @Resource
    private Byte byyte = (byte)1;
    @Resource
    private Character chaaracter = 'D';
    @Resource
    private DataSource daataSource;
    @PersistenceUnit(unitName = "openjpa-test-unit")
    private EntityManagerFactory emf;
    @PersistenceContext(unitName = "openjpa-test-unit")
    private EntityManager em;
    @EJB
    private BasicStatelessBusinessLocal statelessBusinessLocal;
    @EJB
    private BasicStatelessBusinessRemote statelessBusinessRemote;
    @EJB
    private BasicStatefulBusinessLocal statefulBusinessLocal;
    @EJB
    private BasicStatefulBusinessRemote statefulBusinessRemote;


    public void ejbCreate() throws CreateException {
    }

    public void lookupEntityBean() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJBObject is null", bmpHome);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatefulBean() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJBObject is null", statefulHome);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatelessBean() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJBObject is null", statelessHome);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatelessBusinessLocal() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJB BusinessLocal is null", statelessBusinessLocal);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatelessBusinessRemote() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJB BusinessRemote is null", statelessBusinessRemote);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatefulBusinessLocal() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJB BusinessLocal is null", statefulBusinessLocal);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatefulBusinessRemote() throws TestFailureException {
        try {
            Assert.assertNotNull("The EJB BusinessRemote is null", statefulBusinessRemote);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStringEntry() throws TestFailureException {
        try {
            String expected = new String("1");
            Assert.assertNotNull("The String looked up is null", striing);
            Assert.assertEquals(expected, striing);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupDoubleEntry() throws TestFailureException {
        try {
            Double expected = new Double(1.0D);

            Assert.assertNotNull("The Double looked up is null", doouble);
            Assert.assertEquals(expected, doouble);

        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupLongEntry() throws TestFailureException {
        try {
            Long expected = new Long(1L);

            Assert.assertNotNull("The Long looked up is null", loong);
            Assert.assertEquals(expected, loong);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupFloatEntry() throws TestFailureException {
        try {
            Float expected = new Float(1.0F);

            Assert.assertNotNull("The Float looked up is null", flooat);
            Assert.assertEquals(expected, flooat);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupIntegerEntry() throws TestFailureException {
        try {
            Integer expected = new Integer(1);

            Assert.assertNotNull("The Integer looked up is null", inteeger);
            Assert.assertEquals(expected, inteeger);

        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupShortEntry() throws TestFailureException {
        try {
            Short expected = new Short((short) 1);

            Assert.assertNotNull("The Short looked up is null", shoort);
            Assert.assertEquals(expected, shoort);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupBooleanEntry() throws TestFailureException {
        try {
            Boolean expected = new Boolean(true);

            Assert.assertNotNull("The Boolean looked up is null", booolean);
            Assert.assertEquals(expected, booolean);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupByteEntry() throws TestFailureException {
        try {
            Byte expected = new Byte((byte) 1);

            Assert.assertNotNull("The Byte looked up is null", byyte);
            Assert.assertEquals(expected, byyte);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupCharacterEntry() throws TestFailureException {
        try {
            Character expected = new Character('D');

            Assert.assertNotNull("The Character looked up is null", chaaracter);
            Assert.assertEquals(expected, chaaracter);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupResource() throws TestFailureException {
        try {
            Assert.assertNotNull("The DataSource is null", daataSource);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupPersistenceUnit() throws TestFailureException {
        try {
            Assert.assertNotNull("The EntityManagerFactory is null", emf);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupPersistenceContext() throws TestFailureException {
        try {
            Assert.assertNotNull("The EntityManager is null", em);

            try {
                // call a do nothing method to assure entity manager actually exists
                em.getFlushMode();
            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupSessionContext() throws TestFailureException {
        try {
// TODO: DMB: Can't seem to find where to make this work
//            Assert.assertNotNull("The SessionContext is null", ejbContext);
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }

    }
}
