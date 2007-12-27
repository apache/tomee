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
package org.apache.openejb.test.entity.cmp2;

import org.apache.openejb.test.TestManager;
import org.apache.openejb.test.entity.cmr.CmrTestSuite;

import javax.naming.InitialContext;
import java.util.Properties;

/**
 * @version $Revision$ $Date$
 */
public class Cmp2TestSuite extends org.apache.openejb.test.FilteredTestSuite {

    public Cmp2TestSuite() {
        super();
        this.addTest(new Cmp2JndiTests());
        this.addTest(new Cmp2HomeIntfcTests());
        this.addTest(new Cmp2EjbHomeTests());
        this.addTest(new Cmp2EjbObjectTests());
        this.addTest(new Cmp2RemoteIntfcTests());
        this.addTest(new Cmp2HomeHandleTests());
        this.addTest(new Cmp2HandleTests());
        this.addTest(new Cmp2EjbMetaDataTests());
        this.addTest(new Cmp2AllowedOperationsTests());
        this.addTest(new Cmp2JndiEncTests());
        this.addTest(new Cmp2RmiIiopTests());
        this.addTest(new CmrTestSuite());

        this.addTest(new Complex2HomeIntfcTests());
        this.addTest(new Complex2EjbHomeTests());
        this.addTest(new Complex2EjbObjectTests());
        this.addTest(new Complex2RemoteIntfcTests());
        this.addTest(new Complex2HomeHandleTests());
        this.addTest(new Complex2HandleTests());
        this.addTest(new Complex2EjbMetaDataTests());

        this.addTest(new Unknown2HomeIntfcTests());
        this.addTest(new Unknown2EjbHomeTests());
        this.addTest(new Unknown2EjbObjectTests());
        this.addTest(new Unknown2RemoteIntfcTests());
        this.addTest(new Unknown2HomeHandleTests());
        this.addTest(new Unknown2HandleTests());
        this.addTest(new Unknown2EjbMetaDataTests());
    }

    public static junit.framework.Test suite() {
        return new Cmp2TestSuite();
    }

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() throws Exception {
        Properties props = TestManager.getServer().getContextEnvironment();
        //props.put(Context.SECURITY_PRINCIPAL, "ENTITY_TEST_CLIENT");
        //props.put(Context.SECURITY_CREDENTIALS, "ENTITY_TEST_CLIENT");
        new InitialContext(props);

        /*[2] Create database table */
        TestManager.getDatabase().createEntityTable();
//        TestManager.getDatabase().createEntityExplicitePKTable();
//        TestManager.getDatabase().createCMP2Model();
    }

    /**
     * Tears down the fixture, for example, close a network connection.
     * This method is called after a test is executed.
     */
    protected void tearDown() throws Exception {
        /*[1] Drop database table */
        TestManager.getDatabase().dropEntityTable();
//        TestManager.getDatabase().dropEntityExplicitePKTable();
//        TestManager.getDatabase().dropCMP2Model();
    }
}
