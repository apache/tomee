/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.test.singleton;

import junit.framework.TestSuite;
import org.apache.openejb.test.FilteredTestSuite;

/**
 * @version $Rev: 606348 $ $Date: 2007-12-21 15:57:58 -0800 (Fri, 21 Dec 2007) $
 */
public class SingletonLocalTestSuite extends junit.framework.TestCase {

    public SingletonLocalTestSuite(String name) {
        super(name);
    }

    public static junit.framework.Test suite() {
        TestSuite suite = new FilteredTestSuite();
        suite.addTest(new SingletonJndiTests());
        suite.addTest(new SingletonPojoLocalJndiTests());

        suite.addTest(new SingletonHomeIntfcTests());
        suite.addTest(new SingletonPojoLocalHomeIntfcTests());

        suite.addTest(new SingletonEjbHomeTests());
        suite.addTest(new SingletonPojoEjbLocalHomeTests());

        suite.addTest(new SingletonEjbObjectTests());
        suite.addTest(new SingletonPojoEjbLocalObjectTests());

        suite.addTest(new SingletonRemoteIntfcTests());
        suite.addTest(new SingletonLocalIntfcTests());
        suite.addTest(new SingletonLocalBusinessIntfcTests());
        suite.addTest(new SingletonRemoteBusinessIntfcTests());

        suite.addTest(new SingletonHomeHandleTests());
        suite.addTest(new SingletonHandleTests());

        suite.addTest(new SingletonEjbMetaDataTests());
        suite.addTest(new SingletonAllowedOperationsTests());
        suite.addTest(new BMTSingletonAllowedOperationsTests());
        suite.addTest(new SingletonBeanTxTests());
        suite.addTest(new SingletonJndiEncTests());
        suite.addTest(new SingletonContextLookupTests());
        suite.addTest(new SingletonPojoContextLookupTests());
        suite.addTest(new SingletonFieldInjectionTests());
        suite.addTest(new SingletonSetterInjectionTests());
        suite.addTest(new SingletonAnnotatedFieldInjectionTests());
        suite.addTest(new SingletonRmiIiopTests());
        suite.addTest(new MiscEjbTests());
        suite.addTest(new SingletonInterceptorTests());
        suite.addTest(new SingletonDefaultInterceptorTests());
        /*///////////////////////////
        * Annotated test clients
        *///////////////////////////

        // Annotated field injection test clients
        suite.addTest(new AnnotatedFieldInjectionSingletonPojoLocalHomeIntfcTests());

        // Annotated setter injection test clients
        suite.addTest(new AnnotatedSetterInjectionSingletonPojoLocalHomeIntfcTests());

        /* TO DO
        suite.addTest(new SingletonEjbContextTests());
        suite.addTest(new BMTSingletonEjbContextTests());
        suite.addTest(new BMTSingletonEncTests());
        suite.addTest(new SingletonContainerManagedTransactionTests());
        */
        return suite;
    }
}
