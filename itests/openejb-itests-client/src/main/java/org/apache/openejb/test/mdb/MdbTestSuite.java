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
        suite.addTest(new MdbConnectionFactoryTests());
//        suite.addTest(new BasicMdbTests());
//        suite.addTest(new StatelessPojoRemoteJndiTests());
//        suite.addTest(new StatelessHomeIntfcTests());
//        suite.addTest(new StatelessPojoHomeIntfcTests());
//        suite.addTest(new StatelessRemoteBusinessIntfcTests());
//        suite.addTest(new StatelessEjbHomeTests() );
//        suite.addTest(new StatelessPojoEjbHomeTests() );
//        suite.addTest(new StatelessEjbObjectTests());
//        suite.addTest(new StatelessPojoEjbObjectTests());
//        suite.addTest(new StatelessRemoteIntfcTests());
//        suite.addTest(new StatelessPojoRemoteIntrfcTests());
//        suite.addTest(new StatelessHomeHandleTests());
//        suite.addTest(new StatelessPojoHomeHandleTests());
//        suite.addTest(new StatelessHandleTests());
//        suite.addTest(new StatelessPojoHandleTests());
//        suite.addTest(new StatelessEjbMetaDataTests());
//        suite.addTest(new StatelessPojoEjbMetaDataTests());
//        suite.addTest(new StatelessAllowedOperationsTests());
//        suite.addTest(new BMTStatelessAllowedOperationsTests());
//        suite.addTest(new StatelessBeanTxTests());
//        suite.addTest(new StatelessJndiEncTests());
//        suite.addTest(new StatelessContextLookupTests());
//        suite.addTest(new StatelessPojoContextLookupTests());
//        suite.addTest(new StatelessFieldInjectionTests());
//        suite.addTest(new StatelessSetterInjectionTests());
//        suite.addTest(new StatelessAnnotatedFieldInjectionTests());
//        suite.addTest(new StatelessRmiIiopTests());
//        suite.addTest(new MiscEjbTests());
//        /* TO DO
//        suite.addTest(new StatelessEjbContextTests());
//        suite.addTest(new BMTStatelessEjbContextTests());
//        suite.addTest(new BMTStatelessEncTests());
//        suite.addTest(new StatelessContainerManagedTransactionTests());
//        */
        return suite;
    }
}
