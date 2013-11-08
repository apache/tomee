/*
 * BaseKernelTest.java
 *
 * Created on October 9, 2006, 12:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

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
package org.apache.openjpa.persistence.kernel;

import java.util.*;

import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.lib.conf.ConfigurationProvider;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.OpenJPAEntityManager;


public abstract class BaseKernelTest
        extends org.apache.openjpa.persistence.common.utils.AbstractTestCase
{    
    private static Map _sysprops = new HashMap();    
    
    /** Creates a new instance of BaseKernelTest */
    public BaseKernelTest() {
    }
    
    public BaseKernelTest(String name) {
        super(name, "kernelcactusapp");
    }
    
    protected OpenJPAEntityManager getPM() {
        return currentEntityManager();
    }
    
    protected OpenJPAEntityManager getPM(boolean optimistic,
            boolean retainValues) 
    {
        OpenJPAEntityManager em = currentEntityManager();
        em.setNontransactionalRead(true);
        em.setRetainState(retainValues);
        if(em.getTransaction().isActive())
        {
            em.getTransaction().commit();
            em.setOptimistic(optimistic);
        }

        return em;
    }
    
    protected Object persist(Object ob) {
        //FIXME  - this is just a workaround 
        //  Broker broker = .getBrokerFactory().newBroker();
        Broker broker = JPAFacadeHelper.toBroker(currentEntityManager());
        broker.begin();
        broker.persist(ob, null);
        Object id = broker.getObjectId(ob);
        broker.commit();
        broker.close();
        return id;
    }
    

    protected Properties getProperties() {
        return getProperties(null);
    }

    /**
     * Return the runtime properties, optionally overriding or setting
     * some via the given array, which should be in the form
     * { key, val, key, val, ... }.
     */
    protected synchronized Properties getProperties(String[] props) {
        Properties p = new Properties(System.getProperties());
        String str = p.getProperty("openjpa.properties", "kodo.properties");
        if (str != null && str.length() > 0) {
            // cache system properties to reduce load on file system
            Properties loaded = (Properties) _sysprops.get(str);
            if (loaded == null) {
                loaded = new Properties();
                ConfigurationProvider cp =
                    (ConfigurationProvider) Configurations.getProperty(
                            str, null);

                if (cp != null)
                    loaded.putAll(cp.getProperties());
                _sysprops.put(str, loaded);
            }
            p.putAll(loaded);
        }

        for (int i = 0; props != null && i < props.length; i += 2) {
            if (props[i + 1] != null) {
                // remove any duplicate kodo/openjpa property so we don't clash
                if (props[i].startsWith("openjpa."))
                    p.remove("openjpa." + props[i].substring(5));
                else if (props[i].startsWith("openjpa."))
                    p.remove("openjpa." + props[i].substring(8));

                p.setProperty(props[i], props[i + 1]);
            } else {
                p.remove(props[i]);
            }
        }
        return p;
    }
//
//
//    /**
//     * Assert that the given List contain the exact same
//     * elements. This is different than the normal List contract, which
//     * states that list1.equals(list2) if each element e1.equals(e2).
//     * This method asserts that e1 == n2.
//     */
//    public static void assertIdentical(List c1, List c2) {
//        assertEquals(c1.size(), c2.size());
//        for (Iterator i1 = c1.iterator(), i2 = c2.iterator();
//            i1.hasNext() && i2.hasNext();)
//            assertTrue(i1.next() == i2.next());
//    }
//
//    /**
//     * Assert that the collection parameter is already ordered
//     * according to the specified comparator.
//     */
//    public void assertOrdered(Collection c, Comparator comp) {
//        List l1 = new LinkedList(c);
//        List l2 = new LinkedList(c);
//        assertEquals(l1, l2);
//        Collections.sort(l2, comp);
//        assertEquals(l1, l2);
//        Collections.sort(l1, comp);
//        assertEquals(l1, l2);
//    }
//
//    ////////////////////
//    // Assertion Helpers
//    ////////////////////
//
//    public void assertNotEquals(Object a, Object b) {
//        if (a == null && b != null)
//            return;
//        if (a != null && b == null)
//            return;
//        if (!(a.equals(b)))
//            return;
//        if (!(b.equals(a)))
//            return;
//
//        fail("expected !<" + a + ">.equals(<" + b + ">)");
//    }
//
//    public void assertSize(int size, Object ob) {
//        if (ob == null) {
//            assertEquals(size, 0);
//            return;
//        }
//
//        if (ob instanceof Collection)
//            ob = ((Collection) ob).iterator();
//        if (ob instanceof Iterator) {
//            Iterator i = (Iterator) ob;
//            int count = 0;
//            while (i.hasNext()) {
//                count++;
//                i.next();
//            }
//
//            assertEquals(size, count);
//        } else
//            fail("assertSize: expected Collection, Iterator, "
//                + "Query, or Extent, but got "
//                + ob.getClass().getName());
//    }
    
}
