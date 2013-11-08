/*
 * TestSecondClassValues.java
 *
 * Created on October 13, 2006, 5:29 PM
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;



import org.apache.openjpa.persistence.kernel.common.apps.SCOTest;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;
import junit.framework.AssertionFailedError;

import org.apache.openjpa.persistence.OpenJPAEntityManager;

public class TestSecondClassValues extends BaseKernelTest {

    public static double DOUBLE_PRECISION = 0.0001D;
    public static float FLOAT_PRECISION = 0.0001F;
    // mprudhom: use optimistic so we don't hang on some databases
    private OpenJPAEntityManager pm;

    private String newline = System.getProperty("line.separator");

    /**
     * Creates a new instance of TestSecondClassValues
     */
    public TestSecondClassValues() {
    }

    public TestSecondClassValues(String name) {
        super(name);
    }

    public void setUp() {
        pm = getPM(true, false);
    }

    private int rnd() {
        return ((int) (Math.random() * 20)) + 5;
    }

    public void testMapDeletion() {
        OpenJPAEntityManager pm;
        pm = getPM();
        startTx(pm);
        SCOTest test = new SCOTest();
        pm.persist(test);
        Map map = new HashMap();
        map.put("foo", new Integer(1));
        map.put("bar", new Integer(2));
        for (int i = 0; i < 10; i++)
            map.put("baz#" + i, new Integer(i));

        test.setStrIntMap(map);
        Object id = pm.getObjectId(test);
        endTx(pm);

        startTx(pm);
        test = (SCOTest) pm.find(SCOTest.class, id);
        assertNotNull(test);
        map = test.getStrIntMap();
        assertEquals(12, map.size());
        assertEquals(new Integer(1), map.get("foo"));
        assertEquals(new Integer(2), map.get("bar"));
        map.remove("bar");
        endTx(pm);

        startTx(pm);
        test = (SCOTest) pm.find(SCOTest.class, id);
        assertNotNull(test);
        map = test.getStrIntMap();
        assertEquals(11, map.size());
        assertEquals(new Integer(1), map.get("foo"));
        assertTrue(map.get("bar") == null);

        map.clear();

        endTx(pm);

        startTx(pm);
        test = (SCOTest) pm.find(SCOTest.class, id);
        assertNotNull(test);
        map = test.getStrIntMap();
        assertEquals(0, map.size());
        endTx(pm);
    }

    public void testStringCollection()
        throws Exception {
        ArrayList list = new ArrayList();
        for (int i = 0; i < rnd(); i++)
            list.add(randomString());

        saveSecondClassCollection(list);
    }

    public void testLongCollection()
        throws Exception {
        ArrayList list = new ArrayList();
        for (int i = 0; i < rnd(); i++)
            list.add(randomLong());
        try {
            saveSecondClassCollection(list);
        } catch (AssertionFailedError afe) {
            bug(AbstractTestCase.Platform.EMPRESS, 889, afe,
                "Empress cannot store large long values");
        }
    }

    public void testShortCollection()
        throws Exception {
        ArrayList list = new ArrayList();
        for (int i = 0; i < rnd(); i++)
            list.add(randomShort());
        saveSecondClassCollection(list);
    }

    public void testBigIntegerCollection()
        throws Exception {
        ArrayList list = new ArrayList();
        for (int i = 0; i < rnd(); i++)
            list.add(randomBigInteger());
        saveSecondClassCollection(list);
    }

    public void testBigDecimalCollection()
        throws Exception {
        try {
            ArrayList list = new ArrayList();
            for (int i = 0; i < rnd(); i++)
                list.add(randomBigDecimal());
            saveSecondClassCollection(list);
        } catch (AssertionFailedError e) {
            bug(3, e, "Precision loss for BigDecimals");
        }
    }

    public void testIntegerCollection()
        throws Exception {
        ArrayList list = new ArrayList();
        for (int i = 0; i < rnd(); i++)
            list.add(randomInt());
        saveSecondClassCollection(list);
    }

    public void testByteCollection()
        throws Exception {
        ArrayList list = new ArrayList();
        for (int i = 0; i < rnd(); i++)
            list.add(randomByte());
        saveSecondClassCollection(list);
    }

    public void testBooleanCollection()
        throws Exception {
        ArrayList list = new ArrayList();
        for (int i = 0; i < rnd(); i++)
            list.add(randomBoolean());
        saveSecondClassCollection(list, true);
    }

    public void testFloatCollection()
        throws Exception {
        try {
            ArrayList list = new ArrayList();
            for (int i = 0; i < rnd(); i++)
                list.add(randomFloat());
            saveSecondClassCollection(list);
        } catch (AssertionFailedError afe) {
            bug(3, afe, "Loss of BigDecimal precision");
        }
    }

    public void testDoubleCollection()
        throws Exception {
        try {
            ArrayList list = new ArrayList();
            for (int i = 0; i < rnd(); i++)
                list.add(randomDouble());
            saveSecondClassCollection(list);
        } catch (AssertionFailedError afe) {
            bug(3, afe, "Loss of BigDecimal precision");
        }
    }

    public void testDateCollection()
        throws Exception {
        ArrayList list = new ArrayList();
        for (int i = 0; i < rnd(); i++)
            list.add(randomDate());

        list.add(new Date(472246800000L));

        saveSecondClassCollection(list);
    }

    public void testBigDecimalBigIntegerMap()
        throws Exception {
        try {
            HashMap map = new HashMap();
            for (int i = 0; i < rnd(); i++)
                map.put(randomBigDecimal(), randomBigInteger());
            saveSecondClassMap(map);
        } catch (AssertionFailedError e) {
            bug(3, e, "Precision loss for BigDecimals");
        }
    }

    public void testStrIntMap()
        throws Exception {
        HashMap map = new HashMap();
        for (int i = 0; i < rnd(); i++)
            map.put(randomString(), randomInt());
        saveSecondClassMap(map);
    }

    public void testIntLongMap() throws Exception {
        HashMap map = new HashMap();
        for (int i = 0; i < rnd(); i++)
            map.put(randomInt(), randomLong());
        try {
            saveSecondClassMap(map);
        }
        catch (AssertionFailedError afe) {
            bug(AbstractTestCase.Platform.EMPRESS, 889, afe,
                "Empress cannot store large long values");
        }
    }

    public void testFloatByteMap()
        throws Exception {
        try {
            HashMap map = new HashMap();
            for (int i = 0; i < rnd(); i++)
                map.put(randomFloat(), randomByte());
            saveSecondClassMap(map);
        } catch (AssertionFailedError afe) {
            bug(3, afe, "Loss of BigDecimal precision");
        }
    }

    public void testByteDoubleMap()
        throws Exception {
        try {
            HashMap map = new HashMap();
            for (int i = 0; i < rnd(); i++)
                map.put(randomByte(), randomDouble());
            saveSecondClassMap(map);
        } catch (AssertionFailedError afe) {
            bug(3, afe, "Loss of BigDecimal precision");
        }
    }

    public void testDoubleCharMap()
        throws Exception {
        try {
            HashMap map = new HashMap();
            for (int i = 0; i < rnd(); i++)
                map.put(randomDouble(), randomChar());
            saveSecondClassMap(map);
        } catch (AssertionFailedError afe) {
            bug(3, afe, "Loss of BigDecimal precision");
        }
    }

    public void testCharBooleanMap()
        throws Exception {
        HashMap map = new HashMap();
        for (int i = 0; i < rnd(); i++)
            map.put(randomChar(), randomBoolean());
        saveSecondClassMap(map);
    }

    public void testDateStrMap() throws Exception {
        HashMap map = new HashMap();
        for (int i = 0; i < rnd(); i++)
            map.put(randomDate(), randomString());

        map.put(new Date(472246800000L),
            "PostgreSQL ain't gonna like this date");
        assertNotNull("map is null testDateStrMap", map);
        saveSecondClassMap(map);
    }

    private void saveSecondClassMap(HashMap map)
        throws Exception {
        try {
            saveSecondClassMapInternal(map);
        } finally {
            commit();
        }
    }

    private void commit() {
        try {
            assertNotNull(pm);

//  		EntityTransaction trans = pm.getTransaction();
//  		if (trans != null && trans.isActive()) {
//  		trans.commit();
            endTx(pm);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void begin() {
        commit(); // make sure we are clean

        // get a fresh PM
        pm = getPM(true, false);
        startTx(pm);
    }

    /**
     * Save the specified map as a second-class object and validate
     * its contents by reading back in the object and comparing
     * all the values to the original. Furthermore, this method
     * deletes each element of both maps in turn and re-validates
     * each time to make sure updating of the map is working correctly.
     */
    private void saveSecondClassMapInternal(HashMap map) throws Exception {
        begin();
        SCOTest test = new SCOTest();
        pm.persist(test);
        int testID = test.getId();
        assertNotNull("Passed Map is null", map);
        Map smap = setGetMap(test, map, true);
        assertNotNull("Map is null in setGetMap", smap);
        commit();

        for (Iterator mapKey = ((HashMap) map.clone()).keySet().iterator();
            mapKey.hasNext();) {
            Object keyToDelete = mapKey.next();

            begin();
            SCOTest retrievedObject =
                (SCOTest) pm.find(SCOTest.class, testID);

            assertNotNull(
                "retrievedObject Obj is null - saveSecondClassMapInternal",
                retrievedObject);

            Map retrievedMap = setGetMap(retrievedObject, map, false);

            assertNotNull(
                "retrievedMap Obj is null - saveSecondClassMapInternal",
                retrievedMap);

            assertTrue(map.size() != 0);
            assertEquals(map.size(), retrievedMap.size());

            assertTrue("Incompatible types", map.keySet().iterator().next().
                getClass().isAssignableFrom(retrievedMap.keySet().
                iterator().next().getClass()));

            // check to make sure all the keys match up to the appropriate
            // values.
            for (Iterator i = map.keySet().iterator(); i.hasNext();) {
                Object key = i.next();
                assertTrue(key != null);
                assertTrue(map.get(key) != null);
                if (key.getClass() == Date.class
                    && retrievedMap.get(key) == null) {
                    getLog().trace("Time: "
                        + (((Date) key).getTime()));
                    getLog().trace("List: "
                        + dumpDates(retrievedMap.keySet()));

                    /*
                        bug (6, "Dates lose precision in some data stores "
                            + "(" + (((Date)key).getTime ()) + ","
                            + "[" + dumpDates (retrievedMap.keySet ()) + "])");
                    */
                }
                if ((key.getClass() == Double.class ||
                    key.getClass() == Float.class ||
                    key.getClass() == BigDecimal.class)
                    && retrievedMap.get(key) == null) {
                    /*
                         bug (3, "Doubles and Floats "
                             + " lose precision in some data stores");
                     */
                }

                assertTrue("The original map contained the object (class="
                    + key.getClass().getName() + ", value="
                    + key.toString() + "), but that object was null "
                    + "in the map that was retrieved "
                    + dump(retrievedMap.keySet()) + ".",
                    retrievedMap.get(key) != null);
                assertClassAndValueEquals(
                    map.get(key), retrievedMap.get(key));
            }

            // now delete the first key in both maps, and make sure
            // thinks are still OK.
            map.remove(keyToDelete);
            retrievedMap.remove(keyToDelete);
        }
    }

    private void saveSecondClassCollection(ArrayList collection)
        throws Exception {
        saveSecondClassCollection(collection, false);
    }

    private void saveSecondClassCollection(ArrayList collection,
        boolean useCustomCollator)
        throws Exception {
        try {
            saveSecondClassCollectionInternal(collection, useCustomCollator);
        } finally {
            commit();
        }
    }

    private void saveSecondClassCollectionInternal(ArrayList collection,
        boolean useCustomCollator)
        throws Exception {
        Object elementToDelete = null;

        if (useCustomCollator)
            Collections.sort(collection,
                new CollectionSorter());
        else
            Collections.sort(collection);

        OpenJPAEntityManager pm1 = getPM();
        startTx(pm1);

        SCOTest test = new SCOTest();
        pm1.persist(test);
        int testID = test.getId();

        Collection storedCollection = setGetCollection(test,
            (Collection) ((ArrayList) collection).clone(), true);

        assertNotNull("retrieved storedCollection is null", storedCollection);

        // make sure the pre-commit collections are identical!
        assertEquals("Pre-commit collections were not equal: " + newline
            + dump(collection) + newline + "!=" + newline
            + dump(storedCollection),
            collection.size(), storedCollection.size());

        endTx(pm1);

        int deletionIndex = 0;

        OpenJPAEntityManager pm2 = getPM();

        while (collection.size() > 0) {
            deletionIndex++;

            startTx(pm2);
            SCOTest retrievedObject =
                (SCOTest) pm2.find(SCOTest.class, testID);

            assertNotNull(
                "retrieved obj is null saveSecondClassCollectionInternal",
                retrievedObject);

            Collection identityCollection = new LinkedList(collection);
            assertNotNull(
                "identityCollection is null saveSecondClassCollectionInternal",
                identityCollection);
            Collection retrievedCollection = setGetCollection(
                retrievedObject, identityCollection, false);

            assertNotNull(
                "retrievedCollection is null saveSecondClassCollectionInternal",
                retrievedCollection);

            validateCollection(retrievedCollection);

            assertNotNull(retrievedCollection);
            assertTrue(collection.size() != 0);

            assertEquals("Retreived collection does not match original "
                + "after the " + deletionIndex + "th deletion ("
                + elementToDelete + "): "
                + newline
                + dump(collection) + newline + "!=" + newline
                + dump(retrievedCollection) + newline,
                collection.size(), retrievedCollection.size());

            /*
            try
            {
                assertEquals (collection.size retrievedCollection.size ());
            } catch (AssertionFailedError afe) {
                bug (AbstractTestCase.Platform.SQLSERVER, 2, afe,
                    "Second-class collections"
                    + " are not being retrieved correctly");
            }
            */

            // make sure the classes of the keys are the same.
            Iterator ci = collection.iterator();
            Object co = collection.iterator().next();

            Iterator rci = retrievedCollection.iterator();
            Object rco = retrievedCollection.iterator().next();

            assertNotNull(co);
            assertNotNull(rco);
            assertEquals(co.getClass(), rco.getClass());

            List sortedRetreivedCollection =
                new ArrayList(retrievedCollection);

            if (useCustomCollator)
                Collections.sort(sortedRetreivedCollection,
                    new CollectionSorter());
            else
                Collections.sort(sortedRetreivedCollection);

            // make sure the collection is OK
            for (Iterator i = collection.iterator(),
                j = sortedRetreivedCollection.iterator();
                i.hasNext() && j.hasNext();) {
                assertClassAndValueEquals(i.next(), j.next());
            }

            elementToDelete = collection.iterator().next();
            if (!(collection.remove(elementToDelete)))
                fail("Could not delete element "
                    + "(<" + elementToDelete.getClass().getName() + ">"
                    + elementToDelete + ") "
                    + "from " + dump(collection));

            if (!(retrievedCollection.remove(elementToDelete)))
                fail("Could not delete element (" + elementToDelete + ") "
                    + "from " + dump(retrievedCollection));

            endTx(pm2);
        }
    }

    private void assertClassAndValueEquals(Object o1, Object o2) {
        assertTrue("First object was null", o1 != null);
        assertTrue("Second object was null", o2 != null);

        assertTrue("Types did not match (class1="
            + o1.getClass().getName() + ", class2="
            + o2.getClass().getName() + ")",
            o1.getClass().isAssignableFrom(o2.getClass()));

        // floats and doubles are a little special: we only
        // compare them to a certain precision, after which
        // we give up.
        /*
          if (o1 instanceof Double)
              assertEquals (((Double)o1).doubleValue (),
                  ((Double)o2).doubleValue (),
                  DOUBLE_PRECISION);
          else if (o1 instanceof Float)
              assertEquals (((Float)o1).floatValue (),
                  ((Float)o2).floatValue (),
                  FLOAT_PRECISION);
          else if (o1 instanceof BigDecimal)
              // BigDecimal equalist is a little special: see
              // JDORuntimeTestCase.assertEquals(BigDecimal,BigDecimal)
              assertEquals ("BigDecimal did not match",
                  (BigDecimal)o1, (BigDecimal)o2);
          else
         */
        assertEquals("Object did not match (class1="
            + o1.getClass().getName() + ", class2="
            + o2.getClass().getName() + ")",
            o1, o2);
    }

    private String dump(Collection coll) {
        List list = new LinkedList(coll);
        try {
            Collections.sort(list);
        } catch (RuntimeException e) {

        }

        StringBuffer buf = new StringBuffer().append("[")
            .append("(size=").append(list.size()).append(")");

        Iterator it = list.iterator();
        if (it.hasNext())
            buf.append("<class=" + it.next().getClass().getName() + ">");

        for (Iterator i = list.iterator(); i.hasNext();)
            buf.append(i.next()).append(i.hasNext() ? "," : "");

        return buf.append("]").toString();
    }

    private String dumpDates(Collection coll) {
        StringBuffer buf = new StringBuffer();
        for (Iterator i = coll.iterator(); i.hasNext();)
            buf.append(((Date) i.next()).getTime()).append(
                i.hasNext() ? "," : "");

        return buf.toString();
    }

    /**
     * Generic setter/getter for setting the maps purposes.
     */
    private Map setGetMap(SCOTest test, HashMap map, boolean doSet) {
        if (map == null)
            return null;

        Object key = map.keySet().iterator().next();
        Object val = map.get(key);

        if (key instanceof Date && val instanceof String) {
            if (doSet)
                test.setDateStrMap(map);
            return test.getDateStrMap();
        } else if (key instanceof Character && val instanceof Boolean) {
            if (doSet)
                test.setCharBooleanMap(map);
            return test.getCharBooleanMap();
        } else if (key instanceof Double && val instanceof Character) {
            if (doSet)
                test.setDoubleCharMap(map);
            return test.getDoubleCharMap();
        } else if (key instanceof Byte && val instanceof Double) {
            if (doSet)
                test.setByteDoubleMap(map);
            return test.getByteDoubleMap();
        } else if (key instanceof Float && val instanceof Byte) {
            if (doSet)
                test.setFloatByteMap(map);
            return test.getFloatByteMap();
        } else if (key instanceof Long && val instanceof Float) {
            if (doSet)
                test.setLongFloatMap(map);
            return test.getLongFloatMap();
        } else if (key instanceof Integer && val instanceof Long) {
            if (doSet)
                test.setIntLongMap(map);
            return test.getIntLongMap();
        } else if (key instanceof String && val instanceof Integer) {
            if (doSet)
                test.setStrIntMap(map);
            return test.getStrIntMap();
        } else if (key instanceof BigDecimal && val instanceof BigInteger) {
            if (doSet)
                test.setBigDecimalBigIntegerMap(map);
            return test.getBigDecimalBigIntegerMap();
        }

        fail("Unknown map type");
        return null;
    }

    /**
     * Generic setter/getter for setting the collections purposes.
     */
    private Collection setGetCollection(SCOTest test,
        Collection collection, boolean doSet) {
        if (collection == null)
            return null;

        Object first = collection.iterator().next();

        if (first instanceof BigInteger) {
            if (doSet)
                test.setCBigInteger(collection);
            return test.getCBigInteger();
        } else if (first instanceof BigDecimal) {
            if (doSet)
                test.setCBigDecimal(collection);
            return test.getCBigDecimal();
        } else if (first instanceof Date) {
            if (doSet)
                test.setCDate(collection);
            return test.getCDate();
        } else if (first instanceof Character) {
            if (doSet)
                test.setCCharacter(collection);
            return test.getCCharacter();
        } else if (first instanceof Double) {
            if (doSet)
                test.setCDouble(collection);
            return test.getCDouble();
        } else if (first instanceof Byte) {
            if (doSet)
                test.setCByte(collection);
            return test.getCByte();
        } else if (first instanceof Float) {
            if (doSet)
                test.setCFloat(collection);
            return test.getCFloat();
        } else if (first instanceof Long) {
            if (doSet)
                test.setCLong(collection);
            return test.getCLong();
        } else if (first instanceof Integer) {
            if (doSet)
                test.setCInteger(collection);
            return test.getCInteger();
        } else if (first instanceof String) {
            if (doSet)
                test.setCString(collection);
            return test.getCString();
        } else if (first instanceof Short) {
            if (doSet)
                test.setCShort(collection);
            return test.getCShort();
        } else if (first instanceof Boolean) {
            if (doSet)
                test.setCBoolean(collection);
            return test.getCBoolean();
        }

        fail("Unknown collection type");
        return null;
    }

    /**
     * A simple sorter that should always return the same sort order.
     * The only reason we need ti use this, instead of relying on the
     * natural order in Collections.sort is that there seems to be
     * a bug somewhere that prevents sorting on collections of Boolean
     * objects.
     */
    public static class CollectionSorter
        implements Comparator {

        private Collator collator = Collator.getInstance();

        public CollectionSorter() {

        }

        public int compare(Object o1, Object o2) {
            if (o1 != null && !(o1 instanceof Boolean))
                return collator.compare(o1, o2);

            return collator.compare(o1.toString(), o2.toString());
        }
    }
}
