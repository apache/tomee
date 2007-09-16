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
package org.apache.openejb.examples.counter;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;

public class CounterImplTest extends TestCase {

	//START SNIPPET: setup	
	private InitialContext initialContext;

    protected void setUp() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
        properties.setProperty("openejb.deployments.classpath.include", ".*counter-stateful.*");

        initialContext = new InitialContext(properties);
    }
    //END SNIPPET: setup	

    /**
     * Lookup the Counter bean via its remote home interface
     *
     * @throws Exception
     */
    //START SNIPPET: remote	
    public void testCounterViaRemoteInterface() throws Exception {
        Object object = initialContext.lookup("CounterImplRemote");

		assertNotNull(object);
		assertTrue(object instanceof CounterRemote);
		CounterRemote counter = (CounterRemote) object;
		assertEquals(0, counter.reset());
		assertEquals(1, counter.increment());
		assertEquals(2, counter.increment());
		assertEquals(0, counter.reset());
    }
    //END SNIPPET: remote	
    
    /**
     * Lookup the Counter bean via its local home interface
     *
     * @throws Exception
     */
    //START SNIPPET: local	
    public void testCounterViaLocalInterface() throws Exception {
        Object object = initialContext.lookup("CounterImplLocal");

		assertNotNull(object);
		assertTrue(object instanceof CounterLocal);
		CounterLocal counter = (CounterLocal) object;
		assertEquals(0, counter.reset());
		assertEquals(1, counter.increment());
		assertEquals(2, counter.increment());
		assertEquals(0, counter.reset());
    }
    //END SNIPPET: local	

}
