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

import javax.naming.InitialContext;
import java.util.Properties;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class BmpTestSuite extends org.apache.openejb.test.FilteredTestSuite {

    public BmpTestSuite() {
        super();
        this.addTest(new BmpJndiTests());
        this.addTest(new BmpHomeIntfcTests());
        this.addTest(new BmpEjbHomeTests());
        this.addTest(new BmpEjbObjectTests());
        this.addTest(new BmpRemoteIntfcTests());
        this.addTest(new BmpHomeHandleTests());
        this.addTest(new BmpHandleTests());
        this.addTest(new BmpEjbMetaDataTests());
        this.addTest(new BmpAllowedOperationsTests());
        this.addTest(new BmpJndiEncTests());
        this.addTest(new BmpContextLookupTests());
        this.addTest(new BmpRmiIiopTests());

    }

    public static junit.framework.Test suite() {
        return new BmpTestSuite();
    }

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() throws Exception {
        Properties props = TestManager.getServer().getContextEnvironment();
        //props.put(Context.SECURITY_PRINCIPAL, "ENTITY_TEST_CLIENT");
        //props.put(Context.SECURITY_CREDENTIALS, "ENTITY_TEST_CLIENT");
        InitialContext initialContext = new InitialContext(props);

        /*[2] Create database table */
        TestManager.getDatabase().createEntityTable();
    }

    /**
     * Tears down the fixture, for example, close a network connection.
     * This method is called after a test is executed.
     */
    protected void tearDown() throws Exception {
        /*[1] Drop database table */
        TestManager.getDatabase().dropEntityTable();
    }
}
