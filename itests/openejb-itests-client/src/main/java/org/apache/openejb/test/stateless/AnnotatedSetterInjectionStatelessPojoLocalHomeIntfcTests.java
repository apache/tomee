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
package org.apache.openejb.test.stateless;

import javax.ejb.EJB;


/**
 * [2] Should be run as the second test suite of the BasicStatelessTestClients
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 * @author <a href="mailto:nour.mohammad@gmail.com">Mohammad Nour El-Din</a>
 * 
 * @version $Rev$ $Date$
 */
public class AnnotatedSetterInjectionStatelessPojoLocalHomeIntfcTests extends AnnotatedSetterInjectionStatelessLocalTestClient {

    public AnnotatedSetterInjectionStatelessPojoLocalHomeIntfcTests(){
        super("AnnotatedSetterInjectionLocalHomeIntfc.");
    }
    
    protected void setUp() throws Exception{
        super.setUp();
    }
    
	@EJB(name="client/tests/stateless/BasicStatelessPojoHomeLocal",
		 beanInterface = BasicStatelessLocalHome.class)
    public void setEjbLocalHome(BasicStatelessLocalHome bslEjbHome) {
    	ejbLocalHome = bslEjbHome;
    }
    
    //===============================
    // Test home interface methods
    //
    public void test01_create(){
        try{
            ejbLocalObject = ejbLocalHome.create();
            assertNotNull( "The EJBObject is null", ejbLocalObject );
        } catch (Exception e){
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }        
    }
    //
    // Test home interface methods
    //===============================

}

