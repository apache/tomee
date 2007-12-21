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
package org.apache.openejb.test.stateful;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.openejb.test.stateless.AnnotatedSetterInjectionStatelessPojoLocalHomeIntfcTests;
import org.apache.openejb.test.FilteredTestSuite;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 * @version $Rev$ $Date$
 */
public class StatefulLocalTestSuite extends junit.framework.TestCase {

    public StatefulLocalTestSuite(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new FilteredTestSuite();
        suite.addTest(new StatefulJndiTests());
        suite.addTest(new StatefulPojoLocalJndiTests());
        suite.addTest(new StatefulHomeIntfcTests());
        suite.addTest(new StatefulPojoLocalHomeIntfcTests());
        suite.addTest(new StatefulPojoLocalIntfcTests());
        suite.addTest(new StatefulLocalBusinessIntfcTests());
        // MNour: Why we put this remote test into the suite for local tests ???
        suite.addTest(new StatefulRemoteBusinessIntfcTests());
        suite.addTest(new StatefulEjbHomeTests());
        suite.addTest(new StatefulPojoEjbLocalHomeTests());
        suite.addTest(new StatefulEjbObjectTests());
        suite.addTest(new StatefulPojoEjbLocalObjectTests());
        suite.addTest(new StatefulRemoteIntfcTests());
        suite.addTest(new StatefulHomeHandleTests());
        suite.addTest(new StatefulHandleTests());
        suite.addTest(new StatefulEjbMetaDataTests());
        suite.addTest(new StatefulBeanTxTests());
        // suite.addTest(new StatefulAllowedOperationsTests());
        // suite.addTest(new BMTStatefulAllowedOperationsTests());
        suite.addTest(new StatefulJndiEncTests());
        suite.addTest(new StatefulContextLookupTests());
        suite.addTest(new StatefulPojoContextLookupTests());
        suite.addTest(new StatefulFieldInjectionTests());
        suite.addTest(new StatefulSetterInjectionTests());
        // suite.addTest(new StatefulPersistenceContextTests());
        suite.addTest(new StatefulRmiIiopTests());
        //suite.addTest(new StatefulInterceptorTests());
        suite.addTest(new StatefulDefaultInterceptorTests());

        /*///////////////////////////
        * Annotated test clients
        *///////////////////////////

        // Annotated field injection test clients
        suite.addTest(new AnnotatedFieldInjectionStatefulPojoLocalHomeIntfcTests());

        // Annotated setter injection test clients
        suite.addTest(new AnnotatedSetterInjectionStatelessPojoLocalHomeIntfcTests());

        /* TO DO
        suite.addTest(new StatefulEjbContextTests());
        suite.addTest(new BMTStatefulEjbContextTests());
        suite.addTest(new BMTStatefulEncTests());
        suite.addTest(new StatefulContainerManagedTransactionTests());
        */

        return suite;
    }

}
