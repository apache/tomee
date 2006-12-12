/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.test.entity.cmr;

import org.apache.openejb.test.TestManager;
import org.apache.openejb.test.entity.cmp2.Cmp2TestSuite;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;

public class CmrTestSuite extends org.apache.openejb.test.TestSuite {
    public CmrTestSuite() {
        super();
        this.addTest(new OneToOneTests());
//        this.addTest(new OneToManyTests());
//        this.addTest(new ManyToManyTests());
//        this.addTest(new OneToOneCompoundPKTests());
//        this.addTest(new OneToManyCompoundPKTests());
//        this.addTest(new ManyToManyCompoundPKTests());
//        this.addTest(new CmrMappingTests());
    }

//    public static junit.framework.Test suite() {
//        return new CmrTestSuite();
//    }

//    /**
//     * Sets up the fixture, for example, open a network connection.
//     * This method is called before a test is executed.
//     */
//    protected void setUp() throws Exception {
//        Properties props = TestManager.getServer().getContextEnvironment();
//        props.put(Context.SECURITY_PRINCIPAL, "ENTITY_TEST_CLIENT");
//        props.put(Context.SECURITY_CREDENTIALS, "ENTITY_TEST_CLIENT");
//        new InitialContext(props);
//
//        TestManager.getDatabase().createEntityTable();
//    }

//    /**
//     * Tears down the fixture, for example, close a network connection.
//     * This method is called after a test is executed.
//     */
//    protected void tearDown() throws Exception {
//        TestManager.getDatabase().dropEntityTable();
//    }
}
