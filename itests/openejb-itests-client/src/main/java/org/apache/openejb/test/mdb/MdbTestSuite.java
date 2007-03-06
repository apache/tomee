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
package org.apache.openejb.test.mdb;

import junit.framework.TestSuite;
import org.apache.openejb.test.stateless.BMTStatelessAllowedOperationsTests;
import org.apache.openejb.test.stateless.MiscEjbTests;
import org.apache.openejb.test.stateless.StatelessAllowedOperationsTests;
import org.apache.openejb.test.stateless.StatelessAnnotatedFieldInjectionTests;
import org.apache.openejb.test.stateless.StatelessBeanTxTests;
import org.apache.openejb.test.stateless.StatelessContextLookupTests;
import org.apache.openejb.test.stateless.StatelessEjbHomeTests;
import org.apache.openejb.test.stateless.StatelessEjbMetaDataTests;
import org.apache.openejb.test.stateless.StatelessEjbObjectTests;
import org.apache.openejb.test.stateless.StatelessFieldInjectionTests;
import org.apache.openejb.test.stateless.StatelessHandleTests;
import org.apache.openejb.test.stateless.StatelessHomeHandleTests;
import org.apache.openejb.test.stateless.StatelessJndiEncTests;
import org.apache.openejb.test.stateless.StatelessPojoContextLookupTests;
import org.apache.openejb.test.stateless.StatelessPojoEjbHomeTests;
import org.apache.openejb.test.stateless.StatelessPojoEjbMetaDataTests;
import org.apache.openejb.test.stateless.StatelessPojoEjbObjectTests;
import org.apache.openejb.test.stateless.StatelessPojoHandleTests;
import org.apache.openejb.test.stateless.StatelessPojoHomeHandleTests;
import org.apache.openejb.test.stateless.StatelessPojoHomeIntfcTests;
import org.apache.openejb.test.stateless.StatelessPojoRemoteIntrfcTests;
import org.apache.openejb.test.stateless.StatelessRemoteBusinessIntfcTests;
import org.apache.openejb.test.stateless.StatelessRemoteIntfcTests;
import org.apache.openejb.test.stateless.StatelessRmiIiopTests;
import org.apache.openejb.test.stateless.StatelessSetterInjectionTests;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 *
 * @version $Rev: 499359 $ $Date: 2007-01-24 03:19:37 -0800 (Wed, 24 Jan 2007) $
 */
public class MdbTestSuite extends junit.framework.TestCase {
    public MdbTestSuite(String name){
        super(name);
    }

    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite();

        // verify the famework is working
        suite.addTest(new MdbConnectionFactoryTests());
        suite.addTest(new BasicMdbTests());

        // allowed operations
        suite.addTest(new MdbAllowedOperationsTests());
        suite.addTest(new BmtMdbAllowedOperationsTests());

//        suite.addTest(new StatelessBeanTxTests());
//        suite.addTest(new StatelessJndiEncTests());
//        suite.addTest(new StatelessContextLookupTests());
//        suite.addTest(new StatelessPojoContextLookupTests());
//        suite.addTest(new StatelessFieldInjectionTests());
//        suite.addTest(new StatelessSetterInjectionTests());
//        suite.addTest(new StatelessAnnotatedFieldInjectionTests());
//        suite.addTest(new StatelessRmiIiopTests());
//        suite.addTest(new MiscEjbTests());
        /* TO DO
        suite.addTest(new StatelessEjbContextTests());
        suite.addTest(new BMTStatelessEjbContextTests());
        suite.addTest(new BMTStatelessEncTests());
        suite.addTest(new StatelessContainerManagedTransactionTests());
        */
        return suite;
    }
}
