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
package org.apache.openejb.test.stateful;


/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 * @author <a href="mailto:manu.t.george@gmail.com">Manu Theruvilpallil George</a>
 */
public class StatefulPojoLocalIntfcTests extends BasicStatefulLocalTestClient {

    public StatefulPojoLocalIntfcTests(){
        super("PojoLocalIntfc.");
    }
    
    protected void setUp() throws Exception{
        super.setUp();
        ejbLocalHome = (BasicStatefulLocalHome) initialContext.lookup("client/tests/stateful/BasicStatefulPojoHomeLocal");
        ejbLocalObject = ejbLocalHome.create("Third Bean");
    }
    
   
    //
    // Test local interface methods of ejb3 stateful bean
    //===============================
    public void test01_isIdentical(){
        try{            
            String str = ejbLocalObject.businessMethod("Hello");
            assertTrue( "The Strings are not equal", str.equals("olleH") );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }


}
