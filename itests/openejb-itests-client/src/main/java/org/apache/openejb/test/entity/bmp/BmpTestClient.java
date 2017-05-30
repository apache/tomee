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

import org.apache.openejb.test.TestManager;

import javax.ejb.EJBMetaData;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.naming.InitialContext;
import java.util.Properties;

public abstract class BmpTestClient extends org.apache.openejb.test.NamedTestCase {

    protected InitialContext initialContext;

    protected EJBMetaData ejbMetaData;
    protected HomeHandle ejbHomeHandle;
    protected Handle ejbHandle;
    protected Object ejbPrimaryKey;

    public BmpTestClient(final String name) {
        super("Entity.BMP." + name);
    }

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() throws Exception {

        final Properties properties = TestManager.getServer().getContextEnvironment();
        //properties.put(Context.SECURITY_PRINCIPAL, "ENTITY_TEST_CLIENT");
        //properties.put(Context.SECURITY_CREDENTIALS, "ENTITY_TEST_CLIENT");

        initialContext = new InitialContext(properties);

    }

    protected void tearDown() throws Exception {

    }


}
