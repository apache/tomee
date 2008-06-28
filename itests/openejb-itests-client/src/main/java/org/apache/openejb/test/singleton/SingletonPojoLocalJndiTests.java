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
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 * 
 * @version $Rev: 450640 $ $Date: 2006-09-27 16:58:13 -0700 (Wed, 27 Sep 2006) $
 */
public class SingletonPojoLocalJndiTests extends BasicSingletonLocalTestClient {

	
    public SingletonPojoLocalJndiTests(){
        super("LocalJNDI.");
    }

    public void test01_initialContext(){
        try{
            assertNotNull("The InitialContext reference is null.", initialContext);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test02_Jndi_lookupHome(){
        try{
        	// Here we use the Java casting as what is done while looking-up a local bean
        	ejbLocalHome = (BasicSingletonLocalHome) initialContext.lookup("client/tests/singleton/BasicSingletonPojoHomeLocal");
            assertNotNull("The EJBLocalHome is null", ejbLocalHome);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

}
