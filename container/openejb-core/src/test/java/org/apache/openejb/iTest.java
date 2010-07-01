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
package org.apache.openejb;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.openejb.test.TestManager;
import org.apache.openejb.test.singleton.SingletonLocalTestSuite;
import org.apache.openejb.test.entity.bmp.BmpLocalTestSuite;
import org.apache.openejb.test.entity.cmp.CmpLocalTestSuite;
import org.apache.openejb.test.entity.cmp2.Cmp2TestSuite;
import org.apache.openejb.test.entity.cmr.CmrTestSuite;
import org.apache.openejb.test.mdb.MdbTestSuite;
import org.apache.openejb.test.stateful.StatefulLocalTestSuite;
import org.apache.openejb.test.stateless.StatelessLocalTestSuite;

/**
 * @version $Revision$ $Date$
 */
public class iTest extends org.apache.openejb.test.TestSuite {

    /**
     * To run this from your ide, set -Dopenejb.home=target/test-classes/
     * @throws Exception
     */
    protected void setUp() throws Exception {
        try {
            OpenEJB.destroy();
            TestManager.stop();
        } catch (Exception e) {
            // do nothing - exception ignored
        }
//        org.apache.log4j.BasicConfigurator.configure();
        System.setProperty("openejb.test.server", org.apache.openejb.test.IvmTestServer.class.getName());
//        System.setProperty("openejb.test.database", org.apache.openejb.test.DerbyTestDatabase.class.getName());
        System.setProperty("openejb.test.database", org.apache.openejb.test.HsqldbTestDatabase.class.getName());
        System.setProperty("openejb.test.jms", org.apache.openejb.test.ActiveMqLocalTestJms.class.getName());

        System.setProperty("openejb.deployments.classpath", "true");

        // m2 executes tests in a module home directory (e.g. container/openejb-persistence)
        // Derby creates derby.log file in derby.system.home
        // @see http://publib.boulder.ibm.com/infocenter/cscv/v10r1/index.jsp?topic=/com.ibm.cloudscape.doc/cdevdvlp25889.html
        System.setProperty("derby.system.home", "target");

        // Copied from org.apache.openejb.SomeoneBrokeSurefireAndThisIsADirtyHackForItTest that's now gone
        System.setProperty("openejb.assembler", org.apache.openejb.assembler.classic.Assembler.class.getName());
        System.setProperty("openejb.deployments.classpath.include", ".*openejb-itests-[^a].*");
        System.setProperty("openejb.deployments.classpath.filter.descriptors", "true");
        System.setProperty("openejb.deployments.classpath.filter.systemapps", "false");
        System.setProperty("openejb.deployments.classpath.ear", "false");

        TestManager.init(null);
        TestManager.start();
    }

    protected void tearDown() throws Exception {
        TestManager.stop();
        OpenEJB.destroy();
    }

    public static Test suite() {
        TestSuite suite = new iTest();
        suite.addTest(SingletonLocalTestSuite.suite());
        suite.addTest(StatelessLocalTestSuite.suite());
        suite.addTest(StatefulLocalTestSuite.suite());
        suite.addTest(BmpLocalTestSuite.suite());
        suite.addTest(CmpLocalTestSuite.suite());
        suite.addTest(Cmp2TestSuite.suite());
        suite.addTest(new CmrTestSuite());
        suite.addTest(MdbTestSuite.suite());
        return suite;
    }
}
