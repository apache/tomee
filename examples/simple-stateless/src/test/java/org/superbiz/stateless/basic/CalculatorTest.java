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
package org.superbiz.stateless.basic;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;

import junit.framework.TestCase;
import org.superbiz.stateless.basic.CalculatorLocal;
import org.superbiz.stateless.basic.CalculatorRemote;

public class CalculatorTest extends TestCase {

	//START SNIPPET: setup	
	private Context context;

    protected void setUp() throws Exception {

//        Map<Object, Object> map = new HashMap<Object, Object>();
//        map.put(EJBContainer.APP_NAME, "test");
//        EJBContainer ejbContainer = EJBContainer.createEJBContainer(map);
        EJBContainer ejbContainer = EJBContainer.createEJBContainer();

        context = ejbContainer.getContext();
    }
    
    //END SNIPPET: setup    

    /**
     * Lookup the Calculator bean via its remote home interface
     *
     * @throws Exception
     */
    //START SNIPPET: remote
    public void testCalculatorViaRemoteInterface() throws Exception {
        Object object = context.lookup("java:global/simple-stateless/CalculatorImpl!"+CalculatorRemote.class.getName());

		assertNotNull(object);
		assertTrue(object instanceof CalculatorRemote);
		CalculatorRemote calc = (CalculatorRemote) object;
		assertEquals(10, calc.sum(4,6));
		assertEquals(12, calc.multiply(3,4));
    }
    //END SNIPPET: remote
    
    /**
     * Lookup the Calculator bean via its local home interface
     *
     * @throws Exception
     */
    //START SNIPPET: local    
    public void testCalculatorViaLocalInterface() throws Exception {
        Object object = context.lookup("java:global/simple-stateless/CalculatorImpl");

		assertNotNull(object);
		assertTrue(object instanceof CalculatorLocal);
		CalculatorLocal calc = (CalculatorLocal) object;
		assertEquals(10, calc.sum(4,6));
		assertEquals(12, calc.multiply(3,4));
    }
    //END SNIPPET: local

}
