/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.openejb.test.TestManager;
import org.openejb.test.entity.bmp.BmpTestSuite;
import org.openejb.test.entity.bmp.BmpLocalTestSuite;
import org.openejb.test.entity.cmp.CmpTestSuite;
import org.openejb.test.entity.cmp.CmpLocalTestSuite;
import org.openejb.test.stateful.StatefulTestSuite;
import org.openejb.test.stateful.StatefulLocalTestSuite;
import org.openejb.test.stateless.StatelessTestSuite;
import org.openejb.test.stateless.StatelessLocalTestSuite;

import java.util.Collections;
import java.util.Arrays;

/**
 * @version $Revision$ $Date$
 */
public class iTest extends org.openejb.test.TestSuite {

    /**
     * To run this from your ide, set -Dopenejb.home=target/test-classes/
     * @throws Exception
     */
    protected void setUp() throws Exception {
        org.apache.log4j.BasicConfigurator.configure();
        System.setProperty("openejb.test.server", org.openejb.test.IvmTestServer.class.getName());
        System.setProperty("openejb.test.database", org.openejb.test.InstantDbTestDatabase.class.getName());
        System.setProperty("openejb.deployments.classpath", "true");
        TestManager.init(null);
        TestManager.start();
    }

    protected void tearDown() throws Exception {
        TestManager.stop();
        OpenEJB.destroy();
    }

    public static Test suite() {
        TestSuite suite = new iTest();
        suite.addTest(StatelessLocalTestSuite.suite());
        suite.addTest(StatefulLocalTestSuite.suite());
        suite.addTest(BmpLocalTestSuite.suite());
        suite.addTest(CmpLocalTestSuite.suite());
        return suite;
    }
}
