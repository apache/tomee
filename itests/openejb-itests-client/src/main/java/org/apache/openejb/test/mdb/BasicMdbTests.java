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

import javax.jms.Destination;

/**
 * [5] Should be run as the fifth test suite of the BasicStatelessTestClients
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class BasicMdbTests extends MdbTestClient {
    protected BasicMdbObject basicMdbObject;

    public BasicMdbTests(){
        super("BasicMdb.");
    }

    protected void setUp() throws Exception {
        super.setUp();
        Destination destination = (Destination) initialContext.lookup("client/tests/messagedriven/mdb/BasicMdb");
        basicMdbObject = MdbProxy.newProxyInstance(BasicMdbObject.class, connectionFactory, destination);
    }


    protected void tearDown() throws Exception {
        MdbProxy.destroyProxy(basicMdbObject);
        super.tearDown();
    }

    //=================================
    // Test remote interface methods
    //
    public void test01_businessMethod(){
        try{
            String expected = "Success";
            String actual = basicMdbObject.businessMethod("sseccuS");
            assertEquals(expected, actual);
        } catch (Exception e){
            e.printStackTrace();
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    /**
     * Throw an application exception and make sure the exception
     * reaches the bean nicely.
     */
    public void Xtest02_throwApplicationException(){
        try{
            basicMdbObject.throwApplicationException();
        } catch (org.apache.openejb.test.ApplicationException e){
            //Good.  This is the correct behaviour
            return;
        } catch (Throwable e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
        fail("An ApplicationException should have been thrown.");
    }

    /**
     * After an application exception we should still be able to
     * use our bean
     */
    public void test03_invokeAfterApplicationException(){
        try{
        String expected = "Success";
        String actual   = basicMdbObject.businessMethod("sseccuS");
        assertEquals(expected, actual);
        } catch (Throwable e){
            e.printStackTrace();
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void Xtest04_throwSystemException(){
        try{
            basicMdbObject.throwSystemException_NullPointer();
        } catch (Exception e){
            //Good, so far.
            Throwable n = e.getCause();
            assertNotNull("Nested exception should not be is null", n );
            assertTrue("Nested exception should be an instance of NullPointerException, but exception is "+n.getClass().getName(), (n instanceof NullPointerException));
            return;
        } catch (Throwable e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
        fail("A NullPointerException should have been thrown.");
    }

    /**
     * After a system exception the intance should be garbage collected
     * and the remote reference should be invalidated.
     *
     * This one seems to fail. we should double-check the spec on this.
     */
    public void TODO_test05_invokeAfterSystemException(){
        try{
        basicMdbObject.businessMethod("This refernce is invalid");
        fail("A java.rmi.NoSuchObjectException should have been thrown.");
        } catch (Exception e){
            // Good.
        } catch (Throwable e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    //
    // Test remote interface methods
    //=================================
}
