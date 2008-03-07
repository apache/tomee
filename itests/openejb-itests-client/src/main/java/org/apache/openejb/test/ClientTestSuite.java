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
package org.apache.openejb.test;

import junit.framework.TestSuite;

import org.apache.openejb.test.entity.bmp.BmpTestSuite;
import org.apache.openejb.test.entity.cmp.CmpTestSuite;
import org.apache.openejb.test.entity.cmp2.Cmp2TestSuite;
import org.apache.openejb.test.stateful.StatefulTestSuite;
import org.apache.openejb.test.stateless.StatelessTestSuite;
import org.apache.openejb.test.mdb.MdbTestSuite;
import org.apache.openejb.test.servlet.ServletTestSuite;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class ClientTestSuite extends junit.framework.TestCase {
    
    public ClientTestSuite(String name){
        super(name);
    }

    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite();
        if (Boolean.getBoolean("openejb.test.servlets")) {
            suite.addTest(ServletTestSuite.suite());
        }
        suite.addTest( StatelessTestSuite.suite() );
        suite.addTest( StatefulTestSuite.suite() );
        suite.addTest( BmpTestSuite.suite() );
        suite.addTest( CmpTestSuite.suite() );
        suite.addTest( Cmp2TestSuite.suite() );
        suite.addTest( MdbTestSuite.suite() );
        return suite;
    }
}
