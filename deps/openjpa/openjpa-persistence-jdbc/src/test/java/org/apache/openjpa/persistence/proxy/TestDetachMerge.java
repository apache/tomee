/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.persistence.proxy;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import org.apache.openjpa.conf.Compatibility;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

import org.apache.openjpa.persistence.proxy.entities.Address;
import org.apache.openjpa.persistence.proxy.entities.AnnuityHolderCategory;
import org.apache.openjpa.persistence.proxy.entities.AnnuityPersistebleObject;
import org.apache.openjpa.persistence.proxy.entities.ContactType;
import org.apache.openjpa.persistence.proxy.entities.IAnnuity;
import org.apache.openjpa.persistence.proxy.entities.IAnnuityHolder;
import org.apache.openjpa.persistence.proxy.entities.IContact;
import org.apache.openjpa.persistence.proxy.entities.IEquityAnnuity;
import org.apache.openjpa.persistence.proxy.entities.IFixedAnnuity;
import org.apache.openjpa.persistence.proxy.entities.IPayor;
import org.apache.openjpa.persistence.proxy.entities.IPayout;
import org.apache.openjpa.persistence.proxy.entities.IRider;
import org.apache.openjpa.persistence.proxy.entities.Person;
import org.apache.openjpa.persistence.proxy.entities.RiderType;
import org.apache.openjpa.persistence.proxy.entities.Annuity;
import org.apache.openjpa.persistence.proxy.entities.AnnuityHolder;
import org.apache.openjpa.persistence.proxy.entities.Contact;
import org.apache.openjpa.persistence.proxy.entities.EquityAnnuity;
import org.apache.openjpa.persistence.proxy.entities.FixedAnnuity;
import org.apache.openjpa.persistence.proxy.entities.Payor;
import org.apache.openjpa.persistence.proxy.entities.Payout;
import org.apache.openjpa.persistence.proxy.entities.Rider;
import org.apache.openjpa.persistence.proxy.entities.AnnuityType;


/*
 * Test the complicated interaction between Detached entities, Proxy classes
 * and Merging changes made in ProxyCollections back into entities.
 * 
 * This code is based on AcmeTest2, which was originally written by
 * Mohammad at IBM and contributed under ASL 2.0.
 */
public class TestDetachMerge extends SingleEMFTestCase {
            
    public void setUp() {
        setUp(DROP_TABLES, Address.class, Annuity.class, AnnuityHolder.class, AnnuityPersistebleObject.class,
            Contact.class, EquityAnnuity.class, FixedAnnuity.class, Payor.class, Payout.class,
            Person.class, Rider.class);
    }
    
    /* 
     * Test default 1.0 compatibility behavior, which should pass AS-IS
     */
    public void testAnnuity1Compat() throws Exception {
        OpenJPAEntityManagerFactorySPI emf1 = 
            (OpenJPAEntityManagerFactorySPI) OpenJPAPersistence.createEntityManagerFactory(
            "Annuity1Compat", "org/apache/openjpa/persistence/proxy/persistence1.xml");
        assertNotNull(emf1);

        Log log = emf1.getConfiguration().getLog("test");

        if (log.isTraceEnabled()) {
            Compatibility compat = emf1.getConfiguration().getCompatibilityInstance();
            assertNotNull(compat);
            log.trace("started testAnnuity1Compat()");
            log.trace("FlushBeforeDetach=" + compat.getFlushBeforeDetach());
            log.trace("CopyOnDetach=" + compat.getCopyOnDetach());
            log.trace("CascadeWithDetach=" + compat.getCascadeWithDetach());
            log.trace("IgnoreDetachedStateFieldForProxySerialization=" +
                compat.getIgnoreDetachedStateFieldForProxySerialization());
        }

        try {
            execute(emf1);
        } catch (RuntimeException e) {
            fail("testAnuity1Compat() should not have caused an execption!" + e);
        } finally {
            emf1.close();
        }
    }
    
    /* 
     * Test default 2.0 compatibility behavior, which should PASS
     */
    public void testAnnuity2Compat() throws Exception {
        OpenJPAEntityManagerFactorySPI emf2 = 
            (OpenJPAEntityManagerFactorySPI) OpenJPAPersistence.createEntityManagerFactory(
            "Annuity2Compat", "org/apache/openjpa/persistence/proxy/persistence2.xml");
        assertNotNull(emf2);

        Log log = emf2.getConfiguration().getLog("test");

        if (log.isTraceEnabled()) {
            Compatibility compat = emf2.getConfiguration().getCompatibilityInstance();
            assertNotNull(compat);
            log.trace("started testAnnuity2Compat()");
            log.trace("FlushBeforeDetach=" + compat.getFlushBeforeDetach());
            log.trace("CopyOnDetach=" + compat.getCopyOnDetach());
            log.trace("CascadeWithDetach=" + compat.getCascadeWithDetach());
            log.trace("IgnoreDetachedStateFieldForProxySerialization=" +
                compat.getIgnoreDetachedStateFieldForProxySerialization());
        }

        try {
            execute(emf2);
        } catch (RuntimeException e) {
            fail("testAnuity2Compat() should not have caused an execption!" + e);
        } finally {
            emf2.close();
        }
    }
    
    private void execute(OpenJPAEntityManagerFactorySPI myEMF) throws Exception {
        Log log = myEMF.getConfiguration().getLog("test");
        //EntityManager em = myEMF.createEntityManager();
        IContact contact = null;
        
        try {
            if (log.isTraceEnabled())
                log.trace("creating contact");
            try {       
                contact = createContact(myEMF);      
            } catch (Exception e) {
                log.error("Create Contact failed.", e);
                throw e;
            }
            
            try {
                verifyContactValues(myEMF, contact);
            } catch (Exception e) {
                log.error("Create Contact verification failed.", e);
                throw e;
                // do not return, as this might be a small bug that we can bypass
            }
            
            if (log.isTraceEnabled())
                log.trace("creating annuity holder");
            IAnnuityHolder annuityHolder = null;
            try {
                annuityHolder = createAnnuityHolder(myEMF, contact);           
            } catch (Exception e) {
                log.error("failed to create Annuity Holder Successfully.", e);
                throw e;
            }
            
            try {
                verifyAnnuityHolderValues(myEMF, annuityHolder);
            } catch (Exception e) {
                log.info("failed to verify create annuity holder successfuly.", e);
                throw e;
            }
            
            if (log.isTraceEnabled())
                log.trace("creating payor");
            IPayor payor = null;
            try {
                payor = createPayor(myEMF);
            } catch(Exception e) {
                log.error("failed to create payor successfuly.", e);
                throw e;
            }
            
            try {
                verifyPayorValues(myEMF, payor);
            } catch (Exception e) {
                log.error("failed to verify create payor successfuly.", e);
                throw e;
            }
            
            if (log.isTraceEnabled())
                log.trace("creating annuity");
            IAnnuity annuity =null;     
            AnnuityType annuityType = AnnuityType.FIXED;
            try {           
                annuity = createAnnuityWithRider(myEMF, annuityType);
            } catch (Exception e) {
                log.error("failed to create annuity successfuly.", e);
                throw e;
            }
            
            try {
                log.trace("verify annuity with rider");
                verifyAnnuityValues(myEMF, annuity, annuityType);
            } catch (Exception e) {
                log.error("failed to verify create annuity successfuly.", e);
                throw e;
            }       
            
            if (log.isTraceEnabled())
                log.trace("upating annuity");
            try {
                log.trace("create annuity with payout");
                annuity = createAnnuityPayout(myEMF, annuity);
            } catch (Exception e) {
                log.error("failed to create annuity successfuly.", e);
                throw e;
            }
            
            try {
                verifyAnnuityValues(myEMF, annuity, annuityType);
            } catch (Exception e) {
                log.error("failed to verify create annuity successfuly.", e);
                throw e;
            }       
            
            if (log.isTraceEnabled())
                log.trace("upating annuity");
            try {
                EntityManager em = createEM(myEMF);
                em.getTransaction().begin();
                    annuity.getPayors().add(payor);
                    annuity.setAnnuityHolderId(annuityHolder.getId());
                    annuity = em.merge(annuity);
                em.getTransaction().commit();
                closeEM(em);
            } catch (Exception e) {
                log.error("failed to update annuity successfuly.", e);
                throw e;
            }
            
            try {
                verifyAnnuityValues(myEMF, annuity, annuityType); 
            } catch (Exception e) {
                log.error("failed to verify annuity update successfuly.", e);
                throw e;
            }
        } finally {
            log.error("scenario: failed.");
        }
        
        if (log.isTraceEnabled())
            log.trace("scenario: completed.");
    }

    private IAnnuity createAnnuityPayout(OpenJPAEntityManagerFactorySPI myEMF, IAnnuity annuity) {
        EntityManager em = createEM(myEMF);
        em.getTransaction().begin();
        IPayout payout = new Payout();
        payout.setAnnuity(annuity);
        payout.setTaxableAmount(new BigDecimal(100.00));
        payout.setStartDate(Calendar.getInstance());
        payout.setEndDate(Calendar.getInstance());
        payout.setId(getId());
        em.persist(payout);
        em.getTransaction().commit();
        em.getTransaction().begin();
        annuity.getPayouts().add(payout);
        em.getTransaction().commit();
        closeEM(em);
        return annuity;
    }

    private IAnnuity createAnnuityWithRider(OpenJPAEntityManagerFactorySPI myEMF, AnnuityType annuityType) {
        EntityManager em = createEM(myEMF);
        em.getTransaction().begin();
        IAnnuity annuity = createAnnuity(annuityType);
        IRider rider1 = getRider();
        IRider rider2 = getRider();
        IRider rider3 = getRider();
        annuity.getRiders().add(rider1);
        annuity.getRiders().add(rider2);    
        annuity.getRiders().add(rider3);
        em.persist(annuity);
        em.getTransaction().commit();
        closeEM(em);
        return annuity;
    }

    private IAnnuity createAnnuity(AnnuityType annuityType) {
        if (AnnuityType.BASIC.equals(annuityType)) {
            Annuity annuity = new Annuity();
            annuity.setId(getId());
            annuity.setAmount(500.00);
            annuity.setAccountNumber("123456");
            return annuity;
        }
        if (AnnuityType.EQUITY.equals(annuityType)) {
            EquityAnnuity annuity = new EquityAnnuity();
            annuity.setId(getId());
            annuity.setAmount(500.00);
            annuity.setAccountNumber("123456");
            annuity.setFundNames("Something nothing wrong");
            annuity.setIndexRate(10.99);
            annuity.setLastPaidAmt(100.00);
            return annuity;
        }
        if (AnnuityType.FIXED.equals(annuityType)) {
            FixedAnnuity annuity = new FixedAnnuity();
            ((FixedAnnuity)annuity).setRate(10.0);        
            annuity.setId(getId());
            annuity.setAmount(500.00);
            annuity.setAccountNumber("123456");
            return annuity;
        }
        return null;
    }

    private IRider getRider() {
        IRider rider = new Rider();
        rider.setId(getId());
        rider.setRule("Pay");
        rider.setType(RiderType.REPLACE);
        rider.setEffectiveDate(new Date());
        return rider;
    }

    private void verifyAnnuityValues(OpenJPAEntityManagerFactorySPI myEMF, IAnnuity annuity, AnnuityType annuityType)
    throws Exception {
        IAnnuity results = findAnnuityById(myEMF, Annuity.class, annuity.getId());
        if (annuity instanceof IFixedAnnuity) {
            assertEqual((IFixedAnnuity)annuity, (IFixedAnnuity)results,
                "Fixed Annuity from Client is not equal to DB value", "Mismacth was found.");
        } else if (annuity instanceof IEquityAnnuity) {
            assertEqual((IEquityAnnuity)annuity, (IEquityAnnuity)results,
                    "Equity Annuity from Client is not equal to DB value", "Mismacth was found.");          
        } else {
            assertEqual(annuity, results,
                    "Basic Annuity from Client is not equal to DB value", "Mismacth was found.");           
        }
    
        assertEqual(annuity.getPayouts(), results.getPayouts(), 
                "Annuity payouts from Client is not equal to DB value", "Mismacth was found in number of payouts");     
        boolean found = false;
        if (annuity.getPayouts() != null) {
            IPayout clientPayout = null;
            for (int i=0; i<annuity.getPayouts().size(); i++) {     
                found = false;  // reset
                clientPayout = annuity.getPayouts().get(i);
                for (IPayout resultPayout: results.getPayouts()) {
                    if (clientPayout.getId().equals(resultPayout.getId())){
                        found = true;
                        assertEqual(clientPayout, resultPayout, 
                        "Annuity Payout from Client is not equal to DB value at location: " +i , "Mismacth was found");
                    }else{
                        continue;
                    }
                }
                if (!(found) && clientPayout != null) {
                    throw new RuntimeException("Annuity: Payout from client is not equal to DB.  " +
                            "Found Payout with id: " + clientPayout.getId() + 
                            " on the client side, but not in the database for annuity id:" + annuity.getId());
                    
                }
            }
        }
        
        assertRidersEqual(annuity.getRiders(), results.getRiders(), 
                "Annuity rider from Client is not equal to DB value", "Mismacth was found in number of rider");     
        if (annuity.getRiders() != null) {
            IRider clientRider = null;
            for (int i=0; i<annuity.getRiders().size(); i++) {      
                found = false;  // reset
                clientRider = annuity.getRiders().get(i);
                for (IRider resultRider : results.getRiders()) {
                    if (clientRider.getId().equals(resultRider.getId())){
                        found = true;
                        assertEqual(clientRider, resultRider, 
                        "Annuity rider from Client is not equal to DB value at location: " +i , "Mismacth was found");
                    }else{
                        continue;
                    }
                }
                if (!(found) && clientRider != null) {
                    throw new RuntimeException("Annuity: Rider from client is not equal to DB.  " +
                            "Found rider with id: " + clientRider.getId() + 
                            " on the client side, but not in the database for annuity id:" + annuity.getId());
                    
                }
            }
        }
        
        assertPayorsEqual(annuity.getPayors(), results.getPayors(), 
                "Annuity Payor from Client is not equal to DB value", "Mismacth was found.");
        if (annuity.getPayors() != null) {
            IPayor clientPayor = null;
            for (int i=0; i<annuity.getPayors().size(); i++) {      
                found = false;  // reset
                clientPayor = annuity.getPayors().get(i);
                for (IPayor resultPayor : results.getPayors()) {
                    if (clientPayor.getId().equals(resultPayor.getId())){
                        found = true;
                        assertEqual(annuity.getPayors().get(i), resultPayor, 
                        "Annuity payor from Client is not equal to DB value at location: " +i , "Mismacth was found");
                    }else{
                        continue;
                    }
                }
                if (!(found) && clientPayor != null) {
                    throw new RuntimeException("Annuity: Payor from client is not equal to DB.  " +
                            "Found payor with id: " + clientPayor.getId() + 
                            " on the client side, but not in the database for annuity id:" + annuity.getId());
                    
                }
            }
        }
    }
    
    private void assertEqual(IAnnuity annuity, IAnnuity results, String string, String string2) throws Exception {
        if(annuity == null && results == null)
            return;
        if (annuity == null)
            throw new RuntimeException("Annuity: Annuities ! equal (Annuity was null).");
        if (!annuity.getId().equals(results.getId())) {
            throw new RuntimeException("Annuity: Annuities ! equal (Annuity ids not the same).");
        }
        assertPayorsEqual(annuity.getPayors(), results.getPayors(), string, string2);
        assertRidersEqual(annuity.getRiders(), results.getRiders(), string, string2);
        assertEqual(annuity.getPayouts(),results.getPayouts(), string, string2);
    }

    private void assertEqual(IEquityAnnuity annuity, IEquityAnnuity results, String string, String string2)
    throws Exception {
        if(annuity == null && results == null)
            return;
        if (annuity == null)
            throw new RuntimeException("Annuity: Annuities ! equal (EquityAnnuity was null).");
        if (!annuity.getId().equals(results.getId())) {
            throw new RuntimeException("Annuity: Annuities ! equal (EquityAnnuity ids not the same).");
        }
        
        assertPayorsEqual(annuity.getPayors(), results.getPayors(), string, string2);
        assertRidersEqual(annuity.getRiders(), results.getRiders(), string, string2);
        assertEqual(annuity.getPayouts(),results.getPayouts(), string, string2);
    }

    private void assertEqual(IFixedAnnuity annuity, IFixedAnnuity results, String string, String string2)
    throws Exception {
        if(annuity == null && results == null)
            return;
        if (annuity == null)
            throw new RuntimeException("Annuity: Annuities ! equal (FixedAnnuity was null).");
        if (!annuity.getId().equals(results.getId())) {
            throw new RuntimeException("Annuity: Annuities ! equal (FixedAnnuity ids not the same).");
        }
        assertPayorsEqual(annuity.getPayors(), results.getPayors(), string, string2);
        assertRidersEqual(annuity.getRiders(), results.getRiders(), string, string2);
        assertEqual(annuity.getPayouts(),results.getPayouts(), string, string2);
    }

    private IAnnuity findAnnuityById(OpenJPAEntityManagerFactorySPI myEMF, Class<Annuity> class1, String id) {
        EntityManager em = createEM(myEMF);
        IAnnuity ann = em.find(class1, id);
        closeEM(em);
        return ann;
    }

    private void assertEqual(List<IPayout> payouts, List<IPayout> payouts2, String string, String string2)
    throws Exception {
        if (payouts == null && payouts2 == null) 
            return;
        if (payouts == null)
            throw new RuntimeException("Annuity: IPayout list not the same (payouts was null)!");
        if (payouts.size() != payouts2.size())
            throw new RuntimeException("Annuity: IPayout list not the same (payouts size not the same)!");
        for (int i = 0; i < payouts.size(); i++) {
            IPayout payout = payouts.get(i);
            boolean found = false;
            for (int j = 0; i < payouts2.size(); j++) {
                try {
                    assertEqual(payout, payouts2.get(j), string, string2);
                    found = true;
                    break;
                } catch (Exception e) {
                    continue;
                }
            }
            if (!found) {
                throw new RuntimeException("Annuity: IPayout list not the same (no match found)!");
            }
        }
    }

    private void assertEqual(IPayout clientPayout, IPayout resultPayout, String string, String string2)
    throws Exception {
        if (clientPayout == null && resultPayout == null) 
            return;
        if (clientPayout == null)
            throw new RuntimeException("Annuity: IPayout not the same (clientPayout was null)! " +
                string + " " + string2);
        if (clientPayout.getId().equals(resultPayout.getId()))
            return;
        throw new RuntimeException("Annuity: IPayout not the same (clientPayout ids not the same)! " +
            string + " " + string2);
    }

    private void assertRidersEqual(List<IRider> riders, List<IRider> riders2, String string, String string2)
    throws Exception {
        if (riders == null && riders2 == null) 
            return;
        if (riders == null)
            throw new RuntimeException("Annuity: IRider list not the same (riders was null)!");
        if (riders.size() != riders2.size())
            throw new RuntimeException("Annuity: IRider list not the same (riders size not the same)!");
        for (int i = 0; i < riders.size(); i++) {
            IRider rider = riders.get(i);
            boolean found = false;
            for (int j = 0; i < riders2.size(); j++) {
                try {
                    assertEqual(rider, riders2.get(j), string, string2);
                    found = true;
                    break;
                } catch (Exception e) {
                    continue;
                }
            }
            if (!found) {
                throw new RuntimeException("Annuity: IRider list not the same (match not found)!");
            }
        }
    }

    private void assertEqual(IRider clientRider, IRider resultRider, String string, String string2) throws Exception {
        if (clientRider == null && resultRider == null) 
            return;
        if (clientRider == null)
            throw new RuntimeException("Annuity: IRider not the same (clientRider was null)! " +
                string + " " + string2);
        if (clientRider.getId().equals(resultRider.getId()))
            return;
        throw new RuntimeException("Annuity: IRider not the same (no match found)! " +
            string + " " + string2);
    }

    private void assertPayorsEqual(List<IPayor> payors, List<IPayor> payors2, String string, String string2)
    throws Exception {
        if (payors == null && payors2 == null) 
            return;
        if (payors == null)
            throw new RuntimeException("Annuity: IPayor list not the same (payors was null)!");
        if (payors.size() != payors2.size())
            throw new RuntimeException("Annuity: IPayor list not the same (payors size not the same)! payors=" +
                payors.toArray().toString() + ", payors2=" + payors2.toString());
        for (int i = 0; i < payors.size(); i++) {
            IPayor payor = payors.get(i);
            boolean found = false;
            for (int j = 0; i < payors2.size(); j++) {
                try {
                    assertEqual(payor, payors2.get(j), string, string2);
                    found = true;
                    break;
                } catch (Exception e) {
                    continue;
                }
            }
            if (!found) {
                throw new RuntimeException("Annuity: IPayor list not the same (no match found)!");
            }
        }
    }

    private void verifyPayorValues(OpenJPAEntityManagerFactorySPI myEMF, IPayor payor) throws Exception {
        IPayor results = null; 
        results = findPayorById(myEMF, Payor.class, payor.getId());
        assertEqual(payor, results,
                "Payor from Client is not equal to DB value.", "Mismacth was found.");
    }

    private IPayor findPayorById(OpenJPAEntityManagerFactorySPI myEMF, Class<Payor> class1, String id) {
        EntityManager em = createEM(myEMF);
        IPayor ip = em.find(class1, id);
        closeEM(em);
        return ip;
    }

    private void assertEqual(IPayor payor, IPayor results, String string, String string2) throws Exception {
        if (payor == null && results == null) 
            return;
        if (payor == null)
            throw new RuntimeException("Annuity: IPayor not the same (payor was null)! " +
                string + " " + string2);
        if (payor.getId().equals(results.getId()))
            return;
        throw new RuntimeException("Annuity: IPayor not the same (no match found)! " +
            string + " " + string2);
    }

    private IPayor createPayor(OpenJPAEntityManagerFactorySPI myEMF) {
        EntityManager em = createEM(myEMF);
        em.getTransaction().begin();
        IPayor payor = new Payor();
        payor.setId(getId());
        payor.setName("Payor");
        em.persist(payor);
        em.getTransaction().commit();
        closeEM(em);
        return payor;
    }

    private void verifyAnnuityHolderValues(OpenJPAEntityManagerFactorySPI myEMF, IAnnuityHolder annuityHolder)
    throws Exception {
        IAnnuityHolder result = null;
        result = findHolderById(myEMF, AnnuityHolder.class, annuityHolder.getId());
        assertEqual(annuityHolder, result, 
            "Annuity Holder from Client is not equal to DB value.", "Mismacth was found.");
        assertEqual(annuityHolder.getContact(), result.getContact(), 
                "Annuity Holder Contact from Client is not equal to DB value.", "Mismacth was found.");
    }
    
    private IAnnuityHolder findHolderById(OpenJPAEntityManagerFactorySPI myEMF, Class<AnnuityHolder> class1, String id)
    {
        EntityManager em = createEM(myEMF);
        IAnnuityHolder result = em.find(class1, id);
        closeEM(em);
        return result;
    }

    private EntityManager createEM(OpenJPAEntityManagerFactorySPI myEMF) {
        return myEMF.createEntityManager();
    }

    private void assertEqual(IAnnuityHolder annuityHolder, IAnnuityHolder results, String string, String string2)
    throws Exception {
            if (annuityHolder == null && results == null) 
                return;
            if (annuityHolder == null)
                throw new RuntimeException("Annuity: IAnnuityHolder not the same (annuityHolder was null)! " +
                    string + " " + string2);
            if (annuityHolder.getId().equals(results.getId()))
                return;
            throw new RuntimeException("Annuity: IAnnuityHolder not the same (no match found)! " +
                string + " " + string2);
    }

    private void assertEqual(IContact contact, IContact contact2, String string, String string2) throws Exception {
        if (contact == null && contact2 == null) 
            return;
        if (contact == null)
            throw new RuntimeException("Annuity: Contacts not the same (contact was null)! " +
                string + " " + string2);
        if (contact.getId().equals(contact2.getId()))
            return;
        throw new RuntimeException("Annuity: Contacts not the same (no match found)! " +
            string + " " + string2);
    }

    private IAnnuityHolder createAnnuityHolder(OpenJPAEntityManagerFactorySPI myEMF, IContact contact) {
        EntityManager em = createEM(myEMF);
        em.getTransaction().begin();
        IAnnuityHolder annuityHolder = new AnnuityHolder();
        annuityHolder.setCategory(AnnuityHolderCategory.METAL);
        annuityHolder.setContact(contact);
        annuityHolder.setId(getId());
        annuityHolder.setFirstName("bob");
        annuityHolder.setDateOfBirth(new Date());
        annuityHolder.setGovernmentId("US");
        annuityHolder.setLastName("dog");
        annuityHolder.setTimeOfBirth(new Date());
        em.persist(annuityHolder);
        em.getTransaction().commit();
        closeEM(em);
        return annuityHolder;
    }

    private void verifyContactValues(OpenJPAEntityManagerFactorySPI myEMF, IContact contact) throws Exception {
        // read the contact with id.
        IContact results = null;
        results = findContactById(myEMF, Contact.class, contact.getId());
        assertEqual(contact, results, 
            "Contact from Client is not equal to DB value.", "Mismacth was found.");     
    }

    private IContact findContactById(OpenJPAEntityManagerFactorySPI myEMF, Class<Contact> class1, String id) {
        EntityManager em = createEM(myEMF);
        IContact ic = em.find(class1, id);
        closeEM(em);
        return ic;
    }

    private IContact createContact(OpenJPAEntityManagerFactorySPI myEMF) {
        EntityManager em = createEM(myEMF);
        em.getTransaction().begin();
        IContact contact = null;
        contact = new Contact();
        contact.setContactType(ContactType.BUSINESS);
        contact.setId(getId());
        contact.setEmail("here@there");
        contact.setPhone("555-5555");
        em.persist(contact);
        em.getTransaction().commit();
        closeEM(em);
        return contact;
    }

    private String getId() {
        UUID uid = UUID.randomUUID();
        return uid.toString();
    }

}
