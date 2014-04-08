/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.test.singleton;


/**
 * [2] Should be run as the second test suite of the BasicSingletonTestClients
 * 
 * 
 * @version $Rev: 450640 $ $Date: 2006-09-27 16:58:13 -0700 (Wed, 27 Sep 2006) $
 */
public class SingletonPojoLocalHomeIntfcTests extends BasicSingletonLocalTestClient {

    public SingletonPojoLocalHomeIntfcTests(){
        super("PojoLocalHomeIntfc.");
    }
    
    protected void setUp() throws Exception{
        super.setUp();
        ejbLocalHome = (BasicSingletonLocalHome) initialContext.lookup("client/tests/singleton/BasicSingletonPojoHomeLocal");
    }
    
    //===============================
    // Test home interface methods
    //
    public void test01_create(){
        try{
            ejbLocalObject = ejbLocalHome.create();
            assertNotNull( "The EJBObject is null", ejbLocalObject );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }        
    }
    //
    // Test home interface methods
    //===============================

}

