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

package org.apache.openejb.test.mdb;

import org.apache.openejb.test.TestFailureException;
import org.apache.openejb.test.TestManager;

public class MdbInterceptorTests extends MdbTestClient {

    public MdbInterceptorTests() {
        super("MDBInterceptor.");
        // TODO Auto-generated constructor stub
    }
    protected InterceptorMdbObject ejbObject;



    protected void setUp() throws Exception {
        super.setUp();
        ejbObject = MdbProxy.newProxyInstance(InterceptorMdbObject.class, connectionFactory, "InterceptorMdbBean");
        TestManager.getDatabase().createEntityTable();
    }

    protected void tearDown() throws Exception {
        MdbProxy.destroyProxy(ejbObject);
        try {
            TestManager.getDatabase().dropEntityTable();
        } catch (Exception e){
            throw e;
        } finally {
            super.tearDown();
        }
    }

    public void test01_checkClassLevelBusinessMethodInterception() {
        try{
            ejbObject.checkClassLevelBusinessMethodInterception();
        } catch (TestFailureException e){
            throw e.error;
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test02_checkMethodLevelBusinessMethodInterception() {
        try{
            ejbObject.checkMethodLevelBusinessMethodInterception();
        } catch (TestFailureException e){
            throw e.error;
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test03_checkClassLevelCreateMethodInterception() {
        try{
            ejbObject.checkClassLevelCreateMethodInterception();
        } catch (TestFailureException e){
            throw e.error;
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test04_checkMethodLevelCreateMethodInterception() {
        try{
            ejbObject.checkMethodLevelCreateMethodInterception();
        } catch (TestFailureException e){
            throw e.error;
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }


}
