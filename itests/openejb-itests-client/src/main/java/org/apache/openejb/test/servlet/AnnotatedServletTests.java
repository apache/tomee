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
package org.apache.openejb.test.servlet;

public class AnnotatedServletTests extends ServletTestClient {
    protected JndiTestServlet testServlet;

    public AnnotatedServletTests(){
        super("AnnotatedServlet.");
    }

    protected void setUp() throws Exception{
        super.setUp();
        testServlet = newServletProxy(JndiTestServlet.class);
    }

    public void test01_lookupStringEntry() {
        testServlet.lookupStringEntry();
    }

    public void test02_lookupDoubleEntry() {
        testServlet.lookupDoubleEntry();
    }

    public void test03_lookupLongEntry() {
        testServlet.lookupLongEntry();
    }

    public void test04_lookupFloatEntry() {
        testServlet.lookupFloatEntry();
    }

    public void test05_lookupIntegerEntry() {
        testServlet.lookupIntegerEntry();
    }

    public void test06_lookupShortEntry() {
        testServlet.lookupShortEntry();
    }

    public void test07_lookupBooleanEntry() {
        testServlet.lookupBooleanEntry();
    }

    public void test08_lookupByteEntry() {
        testServlet.lookupByteEntry();
    }

    public void test09_lookupCharacterEntry() {
        testServlet.lookupCharacterEntry();
    }

    public void test10_lookupEntityBean() {
        testServlet.lookupEntityBean();
    }

    public void test11_lookupStatefulBean() {
        testServlet.lookupStatefulBean();
    }

    public void test12_lookupStatelessBean() {
        testServlet.lookupStatelessBean();
    }

    public void test13_lookupResource() {
        testServlet.lookupResource();
    }

    public void test14_lookupPersistenceUnit() {
        testServlet.lookupPersistenceUnit();
    }

    public void test15_lookupPersistenceContext() {
        testServlet.lookupPersistenceContext();
    }

    public void test19_lookupStatelessBusinessLocal() {
        testServlet.lookupStatelessBusinessLocal();
    }

    public void test20_lookupStatelessBusinessRemote() {
        testServlet.lookupStatelessBusinessRemote();
    }

    public void test21_lookupStatefulBusinessLocal() {
        testServlet.lookupStatefulBusinessLocal();
    }

    public void test22_lookupStatefulBusinessRemote() {
        testServlet.lookupStatefulBusinessRemote();
    }

    public void test23_lookupJMSConnectionFactory() {
        testServlet.lookupJMSConnectionFactory();
    }
}