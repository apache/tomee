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
package org.superbiz.injection;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;

/**
 *  A test case for DataReaderImpl ejb, testing both the remote and local interface 
 *
 */
//START SNIPPET: code
public class EjbDependencyTest extends TestCase {

    private InitialContext initialContext;

    protected void setUp() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");

        initialContext = new InitialContext(properties);
    }

    public void test() throws Exception {
        DataReaderLocal dataReader = (DataReaderLocal) initialContext.lookup("DataReaderImplLocal");

        assertNotNull(dataReader);

        assertEquals("LOCAL:42", dataReader.readDataFromLocalStore());
    	assertEquals("REMOTE:42", dataReader.readDataFromRemoteStore());
    }
}
//END SNIPPET: code
