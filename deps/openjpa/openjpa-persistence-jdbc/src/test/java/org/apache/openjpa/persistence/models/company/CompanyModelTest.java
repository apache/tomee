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
package org.apache.openjpa.persistence.models.company;

import java.beans.*;
import java.util.*;

import junit.framework.*;
import org.apache.openjpa.persistence.test.*;

/** 
 * Generic test case that will be extended by a concrete company
 * model subclass.
 *  
 * @author  Marc Prud'hommeaux
 */
public abstract class CompanyModelTest 
    extends SingleEMTestCase {

    private static Map<Class,Class> factoryClasses;
    private Map<Class,Class> impls;

    @Override
    public void setUp() {
        // make a map of the implementations based on the class names in
        // the current package of the test subclass
        impls = new HashMap<Class,Class>();
        impls.put(IAddress.class, localClass("Address"));
        impls.put(ICompany.class, localClass("Company"));
        impls.put(ICustomer.class, localClass("Customer"));
        impls.put(IPerson.class, localClass("Person"));
        impls.put(IEmployee.class, localClass("Employee"));
        impls.put(IFullTimeEmployee.class, localClass("FullTimeEmployee"));
        impls.put(ILineItem.class, localClass("LineItem"));
        impls.put(IProductOrder.class, localClass("ProductOrder"));
        impls.put(IPartTimeEmployee.class, localClass("PartTimeEmployee"));
        impls.put(IProduct.class, localClass("Product"));

        super.setUp(impls.values().toArray(new Class[impls.size()]));
        checkModel();
    }

    @Override
    public void tearDown() throws Exception {
        impls.clear();
        impls = null;
        factoryClasses = null;
        super.tearDown();
    }
    
    private Class localClass(String name) {
        String pkg = getClass().getPackage().getName();
        try {
            return Class.forName(pkg + "." + name);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** 
     * Runs through basic queries against all of the properties of all
     * of the known persistent classes. We're just checking here to
     * make sure the queries can be executed without problem. Queries
     * should always return all known instances in the database.
     */
    public void testBasicQueries() throws Exception {
        for (Class c : impls.values()) {
            for (PropertyDescriptor pd :
                Introspector.getBeanInfo(c).getPropertyDescriptors()) {

                if (pd.getWriteMethod() == null) // ignore read-only
                    continue;

                Set<String> queries = new TreeSet<String>();
                getBasicQueries(queries, pd, "x.");

                StringBuilder str = new StringBuilder();

                // execute the individual queries
                for (String query : queries) {
                    find(c, "where " + query);
                    str.append(str.length() > 0 ? " or " : "").append(query);
                }

                // now execute all the queries combined
                find(c, "where " + str);
            }
        }
    }

    void getBasicQueries(Set<String> queries, PropertyDescriptor pd,
        String prefix) throws Exception {

        // max level of field traversal: 3
        // ### if (prefix.split("\\.").length > 3)
        if (prefix.split("\\.").length > 2)
            return;

        Class type = pd.getPropertyType();

        String name = prefix + pd.getName();

        if (!queries.add(name + " is not null"))
            return;

        queries.add(name + " is null");

        if (type.isAssignableFrom(Number.class) || type == int.class ||
            type == double.class || type == float.class ||
            type == long.class || type == short.class) {
            queries.add(name + " = 0");
            queries.add(name + " <> 0");
            queries.add(name + " > 0");
            queries.add(name + " < 0");
            queries.add(name + " >= 0");
            queries.add(name + " <= 0");
            queries.add("sqrt(" + name + ") <> 0");
            queries.add("abs(" + name + ") <> 0");
            // queries.add("mod(" + name + ", 5) <> 0");
        }

        if (type.isAssignableFrom(Collection.class)) {
            queries.add(name + " IS EMPTY");
            queries.add(name + " IS NOT EMPTY");
            queries.add("size(" + name + ") <> 0");
        }

        if (type.isAssignableFrom(String.class)) {
            queries.add("lower(" + name + ") = 'x'");
            queries.add("upper(" + name + ") = 'x'");
            queries.add("concat(" + name + ", " + name + ") = 'x'");
            queries.add("substring(" + name + ", 1, 2) = 'x'");
            queries.add("length(" + name + ") > 0");
            queries.add("locate(" + name + ", 'x', 1) > 0");
            queries.add("trim(leading ' ' from " + name + ") = 'x'");
        }

        if (type.isAssignableFrom(Date.class)) {
            queries.add(name + " <> CURRENT_TIMESTAMP");
        }

        // relation is an entity ... add all the relations
        if (impls.containsKey(type) || impls.containsValue(type)) {
            for (PropertyDescriptor desc :
                Introspector.getBeanInfo(type).getPropertyDescriptors()) {

                if (desc.getWriteMethod() == null) // ignore read-only
                    continue;

                // prevent recursion
                if (name.endsWith("." + desc.getName() + "."))
                    continue;

                getBasicQueries(queries, desc, name + ".");
            }
        }
    }

    void checkModel() {
        try {
            verifyModel();
        } catch (AssertionFailedError e) {
            // clear all existing instances
            clear(emf, impls.values().toArray(new Class[impls.size()]));

            // since the factory method needs to be static, we need to store
            // the classes statically
            factoryClasses = impls;
            try {
                final List<Exception> exceptions = new LinkedList<Exception>();
                XMLDecoder decoder = new XMLDecoder(CompanyModelTest.class.
                    getResourceAsStream("companies.xml"));
                decoder.setExceptionListener(new ExceptionListener() {
                    public void exceptionThrown(Exception e) {
                        exceptions.add(e);
                    }
                });
                Collection obs = (Collection) decoder.readObject();

                if (exceptions.size() > 0) {
                    throw new IllegalStateException(exceptions.get(0));
                }

                assertNotNull(obs);

                persist(obs.toArray());
                decoder.close();
            } finally {
                factoryClasses = null;
            }
        }

        verifyModel();
    }

    int queryCount(Class c, String query, Object... params) {
        return find(c, query, params).size();
    }

    int queryCount(Class c) {
        return find(c, null).size();
    }

    Class impl(Class c) {
        return impls.get(c);
    }

    void verifyModel() {
        assertEquals(2, queryCount(impl(ICompany.class)));
        assertEquals(11, queryCount(impl(IAddress.class)));
        assertEquals(3, queryCount(impl(IProduct.class)));
        assertEquals(2, queryCount(impl(IProductOrder.class)));
        assertEquals(3, queryCount(impl(ILineItem.class)));
        assertEquals(1, queryCount(impl(IPartTimeEmployee.class)));
        assertEquals(3, queryCount(impl(IFullTimeEmployee.class)));
        assertEquals(4, queryCount(impl(ICustomer.class)));

        assertEquals(3, queryCount(impl(IAddress.class),
            "where x.state = 'CA'"));

        assertEquals(1, queryCount(impl(ICompany.class),
            "where size(x.employees) = 4"));
        assertEquals(1, queryCount(impl(ICompany.class),
            "where size(x.employees) = 0"));

        assertEquals(2, queryCount(impl(ICustomer.class),
            "where size(x.orders) = 1"));

        assertEquals(1, queryCount(impl(IProductOrder.class),
            "where x.shippedDate is null"));
        assertEquals(1, queryCount(impl(IProductOrder.class),
            "where x.shippedDate is not null"));

        assertEquals(1, queryCount(impl(IEmployee.class),
            "where x.manager is null"));
        assertEquals(2, queryCount(impl(IEmployee.class),
            "where x.manager.manager is null"));
        assertEquals(1, queryCount(impl(IEmployee.class),
            "where x.manager.manager.manager is null"));

        assertEquals(2, queryCount(impl(IPerson.class),
            "where x.firstName like ?1 and x.lastName like ?1", "M%"));
        assertEquals(1, queryCount(impl(IPerson.class),
            "where x.homeAddress.state = 'CA'"));
    }

    /** 
     * Factory method that is called from the serialized XML. 
     */
    public static Object create(Class intf)
        throws InstantiationException, IllegalAccessException {
        return factoryClasses.get(intf).newInstance();
    }
}

