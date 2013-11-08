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
package org.apache.openjpa.persistence.meta;

import java.lang.reflect.Member;
import java.lang.reflect.Method;

import javax.persistence.EntityManager;

import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;
import org.apache.openjpa.persistence.meta.common.apps.MetaTest7;

/**
 * <p>
 * Tests the {@code getFactoryMethod} method
 * </p>
 * 
 */
public class TestFactoryMethod extends AbstractTestCase {

    private MetaDataRepository _repos = null;
    private ClassMetaData _metaTest7 = null;

    public TestFactoryMethod(String test) {
        super(test, "metacactusapp");
    }

    public void setUp() throws Exception {
        _repos = getRepository();
        _metaTest7 = _repos.getMetaData(MetaTest7.class, null, true);
    }

    protected MetaDataRepository getRepository() throws Exception {
        // return new OpenJPAConfigurationImpl().newMetaDataRepositoryInstance();
        // return getConfiguration().newMetaDataRepositoryInstance();
        EntityManager em = currentEntityManager();
        Broker broker = JPAFacadeHelper.toBroker(em);
        return broker.getConfiguration().newMetaDataRepositoryInstance();
    }

    /**
     * Tests that getFactoryMethod() identifies the method using both the name and the type. This is required when the
     * factory method is overloaded - i.e. same name but different types.
     */
    public void testFactoryMatchesByType() {
        FieldMetaData fieldMetaData = _metaTest7.getField("status");

        Member factoryMember = fieldMetaData.getFactoryMethod();

        assertEquals("valueOf", factoryMember.getName());

        Method factoryMethod = (Method) factoryMember;

        Class<?>[] parameterTypes = factoryMethod.getParameterTypes();
        assertEquals("Both valueOf methods take just 1 parameter", 1, parameterTypes.length);
        assertEquals("Need to match the type of the underlying field " + "or the factory method will fail when called",
            String.class, parameterTypes[0]);
    }

    /**
     * Tests that getFactoryMethod() identifies the method using both the name and the type. This is required when the
     * factory method is overloaded - i.e. same name but different types. This tests that an Externalizer returning int
     * can be matched with a Factory taking long (a widening conversion).
     */
    public void testFactoryMatchesByTypeWidening() {
        FieldMetaData fieldMetaData = _metaTest7.getField("intLongStatus");

        Member factoryMember = fieldMetaData.getFactoryMethod();

        assertEquals("valueOf", factoryMember.getName());

        Method factoryMethod = (Method) factoryMember;

        Class<?>[] parameterTypes = factoryMethod.getParameterTypes();
        assertEquals("Both valueOf methods take just 1 parameter", 1, parameterTypes.length);
        assertEquals("Need to match the type of the underlying field " + "or the factory method will fail when called",
            long.class, parameterTypes[0]);
    }

    /**
     * Tests that getFactoryMethod() identifies the method using both the name and the type. This is required when the
     * factory method is overloaded - i.e. same name but different types. This tests that an Externalizer returning int
     * can be matched with a Factory taking Integer (a boxing conversion).
     */
    public void testFactoryMatchesByTypeBoxing() {
        FieldMetaData fieldMetaData = _metaTest7.getField("intIntegerStatus");

        Member factoryMember = fieldMetaData.getFactoryMethod();

        assertEquals("valueOf", factoryMember.getName());

        Method factoryMethod = (Method) factoryMember;

        Class<?>[] parameterTypes = factoryMethod.getParameterTypes();
        assertEquals("Both valueOf methods take just 1 parameter", 1, parameterTypes.length);
        assertEquals("Need to match the type of the underlying field " + "or the factory method will fail when called",
            Integer.class, parameterTypes[0]);
    }

    /**
     * Tests that getFactoryMethod() identifies the method using both the name and the type. This is required when the
     * factory method is overloaded - i.e. same name but different types. This tests that an Externalizer returning
     * Integer can be matched with a Factory taking Integer (an identity conversion).
     */
    public void testFactoryMatchesByTypeWrapper() {
        FieldMetaData fieldMetaData = _metaTest7.getField("integerIntegerStatus");

        Member factoryMember = fieldMetaData.getFactoryMethod();

        assertEquals("valueOf", factoryMember.getName());

        Method factoryMethod = (Method) factoryMember;

        Class<?>[] parameterTypes = factoryMethod.getParameterTypes();
        assertEquals("Both valueOf methods take just 1 parameter", 1, parameterTypes.length);
        assertEquals("Need to match the type of the underlying field " + "or the factory method will fail when called",
            Integer.class, parameterTypes[0]);
    }

    /**
     * Tests that getFactoryMethod() identifies the method using both the name and the type. This is required when the
     * factory method is overloaded - i.e. same name but different types. This tests that an Externalizer returning
     * Integer can be matched with a Factory taking int (an unboxing conversion).
     */
    public void testFactoryMatchesByTypeUnboxing() {
        FieldMetaData fieldMetaData = _metaTest7.getField("integerIntStatus");

        Member factoryMember = fieldMetaData.getFactoryMethod();

        assertEquals("valueOf", factoryMember.getName());

        Method factoryMethod = (Method) factoryMember;

        Class<?>[] parameterTypes = factoryMethod.getParameterTypes();
        assertEquals("Both valueOf methods take just 1 parameter", 1, parameterTypes.length);
        assertEquals("Need to match the type of the underlying field " + "or the factory method will fail when called",
            int.class, parameterTypes[0]);
    }

    /**
     * Tests that getFactoryMethod() identifies the method using both the name and the type. This is required when the
     * factory method is overloaded - i.e. same name but different types. This tests that an Externalizer returning
     * Integer can be matched with a Factory taking long (an unboxing conversion following by a widening primitive
     * conversion).
     */
    public void testFactoryMatchesByTypeUnboxingWidening() {
        FieldMetaData fieldMetaData = _metaTest7.getField("integerLongStatus");

        Member factoryMember = fieldMetaData.getFactoryMethod();

        assertEquals("valueOf", factoryMember.getName());

        Method factoryMethod = (Method) factoryMember;

        Class<?>[] parameterTypes = factoryMethod.getParameterTypes();
        assertEquals("Both valueOf methods take just 1 parameter", 1, parameterTypes.length);
        assertEquals("Need to match the type of the underlying field " + "or the factory method will fail when called",
            long.class, parameterTypes[0]);
    }
}
