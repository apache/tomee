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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.test.stateless;

import javax.ejb.EJBException;
/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 * @author <a href="mailto:manu.t.george@gmail.com">Manu George</a>
 */
public class StatelessLocalIntfcTests extends BasicStatelessLocalTestClient {

	public StatelessLocalIntfcTests() {
		super("LocalIntfc.");
	}
	 protected void setUp() throws Exception{
	        super.setUp();
	        Object obj = initialContext.lookup("client/tests/stateless/BasicStatelessPojoHomeLocal");
	        ejbLocalHome = (BasicStatelessLocalHome)obj;
	        ejbLocalObject = ejbLocalHome.create();
	    }
	    
	    //=================================
	    // Test local interface methods
	    //
	    public void test01_businessMethod(){
	        try{
	            String expected = "Success";
	            String actual = ejbLocalObject.businessMethod("sseccuS");
	            assertEquals(expected, actual);
	        } catch (Exception e){
	            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
	        }
	    }

	    /**
	     * Throw an application exception in the bean and make sure the exception
	     * reaches the client nicely.
	     */
	    public void test02_throwApplicationException(){
	        try{
	            ejbLocalObject.throwApplicationException();
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
	        String actual   = ejbLocalObject.businessMethod("sseccuS");
	        assertEquals(expected, actual);
	        } catch (Throwable e){
	            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
	        }
	    }

	    public void test04_throwSystemException(){
	        try{
	            ejbLocalObject.throwSystemException_NullPointer();
	        } catch (EJBException e){
	            //Good, so far.
	            
	            assertNotNull("Nested exception should not be is null", e );
	            assertTrue("Nested exception should be an instance of NullPointerException, but exception is "+e.getCausedByException().getClass().getName(), (e.getCausedByException() instanceof NullPointerException));
	            return;
	        } catch (Throwable e){
	            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
	        }
	        fail("An EJBException encapsulating a NullPointerException should have been thrown.");
	    }
	    
	    /**
	     * After a system exception the intance should be garbage collected
	     * but this is invisible to the client as it will just use another
	     * stateless session object. All the stateless session objects are 
	     * equal.  Refer 4.5.3 in EJB 3.0 core specification.
	     * This one seems to fail. we should double-check the spec on this.
	     */
	    public void test05_invokeAfterSystemException(){
	        try{
	            String expected = "Success";
	            String actual = ejbLocalObject.businessMethod("sseccuS");
	            assertEquals(expected, actual);
	        
	        } catch (Exception e){
	        	fail("The business method should have been executed.");
	        } catch (Throwable e){
	            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
	        }
	    }
	    //
	    // Test remote interface methods
	    //=================================

	    protected void tearDown() throws Exception {
	        super.tearDown();
	    }
	    
	    //TODO Tests for the below conditions need to be added.
	    /* Test an Application Exception that is a runtime exception
	     * Throwing an application exception within a transaction
	     * will rollback only if that method is marked for rollback
	     * Also FinderException, CreateException and RemoveException
	     * are application exceptions.
	     */
}