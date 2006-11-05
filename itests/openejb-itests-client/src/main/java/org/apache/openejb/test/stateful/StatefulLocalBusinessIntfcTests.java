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
package org.apache.openejb.test.stateful;

/**
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class StatefulLocalBusinessIntfcTests extends StatefulTestClient {
    private BasicStatefulBusinessLocal businessLocal;

    public StatefulLocalBusinessIntfcTests(){
        super("LocalBusinessIntfc.");
    }

    protected void setUp() throws Exception{
        super.setUp();
    }

    //=================================
    // Test remote interface methods
    //
    public void test00_lookupBusinessInterface() throws Exception {
        Object obj = initialContext.lookup("client/tests/stateful/BasicStatefulPojoHomeBusinessLocal");
        assertNotNull(obj);
        assertTrue("instance of BasicStatefulBusinessLocal", obj instanceof BasicStatefulBusinessLocal);
        businessLocal = (BasicStatefulBusinessLocal) obj;
    }

    public void test01_businessMethod(){
        try{
            String expected = "Success";
            String actual = businessLocal.businessMethod("sseccuS");
            assertEquals(expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }

        try{
            Integer expected = new Integer(42);
            Object actual = businessLocal.echo(expected);
            assertSame("pass by reference", expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    /**
     * Throw an application exception and make sure the exception
     * reaches the bean nicely.
     */
    public void test02_throwApplicationException(){
        try{
            businessLocal.throwApplicationException();
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
            String actual   = businessLocal.businessMethod("sseccuS");
            assertEquals(expected, actual);
        } catch (Throwable e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    // TODO: check which exception should be thrown
    public void _test04_throwSystemException(){
        try{
            businessLocal.throwSystemException_NullPointer();
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
    //TODO: implement
    public void TODO_test05_invokeAfterSystemException(){
//        try{
//        businessLocal.businessMethod("This refernce is invalid");
//        fail("A java.rmi.NoSuchObjectException should have been thrown.");
//        } catch (java.rmi.NoSuchObjectException e){
//            // Good.
//        } catch (Throwable e){
//            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
//        }
    }
    //
    // Test remote interface methods
    //=================================

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
