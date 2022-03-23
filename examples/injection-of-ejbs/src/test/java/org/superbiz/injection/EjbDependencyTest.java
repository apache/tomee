/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.injection;

import junit.framework.TestCase;

import jakarta.ejb.embeddable.EJBContainer;
import javax.naming.Context;

/**
 * A test case for DataReaderImpl ejb, testing both the remote and local interface
 */
//START SNIPPET: code
public class EjbDependencyTest extends TestCase {

    public void test() throws Exception {
        final Context context = EJBContainer.createEJBContainer().getContext();

        DataReader dataReader = (DataReader) context.lookup("java:global/injection-of-ejbs/DataReader");

        assertNotNull(dataReader);

        assertEquals("LOCAL:42", dataReader.readDataFromLocalStore());
        assertEquals("REMOTE:42", dataReader.readDataFromRemoteStore());
        assertEquals("LOCALBEAN:42", dataReader.readDataFromLocalBeanStore());
    }
}
//END SNIPPET: code
