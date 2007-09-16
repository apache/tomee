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
package org.apache.openejb.examples.injection;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;

/**
 *  A test case for DataReaderImpl ejb, testing both the remote and local interface 
 *
 */
public class EjbDependencyTest extends TestCase {
	
	private static final String REMOTE_STORE_RESULT = "REMOTE:42";
	private static final String LOCAL_STORE_RESULT = "LOCAL:42";
	
	//START SNIPPET: setup
	private InitialContext initialContext;

    protected void setUp() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
        properties.setProperty("openejb.deployments.classpath.include", ".*injection.*");

        initialContext = new InitialContext(properties);
    }
    //END SNIPPET: setup

    //START SNIPPET: test
    public void testViaLocalInterface() throws Exception {
    	Object object = initialContext.lookup("DataReaderImplLocal");
    	
    	assertNotNull(object);
    	assertEquals(LOCAL_STORE_RESULT, ((DataReaderLocal)object).readDataFromLocalStore());
    	assertEquals(REMOTE_STORE_RESULT, ((DataReaderLocal)object).readDataFromRemoteStore());
    }
    //END SNIPPET: test
    
    public void testViaRemoteInterface() throws Exception {
    	Object object = initialContext.lookup("DataReaderImplRemote");
    	
    	assertNotNull(object);
    	assertEquals(LOCAL_STORE_RESULT, ((DataReaderRemote)object).readDataFromLocalStore());
    	assertEquals(REMOTE_STORE_RESULT, ((DataReaderRemote)object).readDataFromRemoteStore());
    }
}