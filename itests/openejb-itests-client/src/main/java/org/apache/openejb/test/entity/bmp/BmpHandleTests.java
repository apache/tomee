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
package org.apache.openejb.test.entity.bmp;

import javax.ejb.EJBObject;

/**
 * [7] Should be run as the seventh test suite of the BasicBmpTestClients
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class BmpHandleTests extends BasicBmpTestClient{

    public BmpHandleTests(){
        super("Handle.");
    }

    protected void setUp() throws Exception{
        super.setUp();
        Object obj = initialContext.lookup("client/tests/entity/bmp/BasicBmpHome");
        ejbHome = (BasicBmpHome)javax.rmi.PortableRemoteObject.narrow( obj, BasicBmpHome.class);
        ejbObject = ejbHome.createObject("Fifth Bean");
        ejbHandle = ejbObject.getHandle();
    }

    protected void tearDown() throws Exception{
        if(ejbObject !=null)
            ejbObject.remove();
        super.tearDown();
    }

    //=================================
    // Test handle methods
    //
    public void test01_getEJBObject(){

        try{
            EJBObject object = ejbHandle.getEJBObject();
            assertNotNull( "The EJBObject is null", object );
            // Wait until isIdentical is working.
            //assertTrue("EJBObjects are not identical", object.isIdentical(ejbObject));
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    /**
     * This remove method of the EJBHome is placed hear as it
     * is more a test on the handle then on the remove method
     * itself.
     */
    public void test02_EJBHome_remove(){
        try{
            ejbHome.remove(ejbHandle);
            try{
                ejbObject.businessMethod("Should throw an exception");
                assertTrue( "Calling business method after removing the EJBObject does not throw an exception", false );
            } catch (Exception e){
                assertTrue( true );
                return;
            }
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        } finally{
            ejbObject  = null;
        }
    }
    //
    // Test handle methods
    //=================================

}
