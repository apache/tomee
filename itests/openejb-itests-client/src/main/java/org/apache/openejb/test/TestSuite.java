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

import java.util.Enumeration;

import junit.framework.Test;
import junit.framework.TestResult;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 * 
 * @version $Rev$ $Date$
 */
public class TestSuite extends junit.framework.TestSuite {
    
    public TestSuite() {
        super();
    }
    
    /**
     * Runs the tests and collects their result in a TestResult.
     */
    public void run(TestResult result) {
        try{
            setUp();

            for (Enumeration e= tests(); e.hasMoreElements(); ) {
                if ( result.shouldStop() ) break;
                Test test= (Test)e.nextElement();
                test.run(result);
            }

            tearDown();
        } catch (Exception e) {
            result.addError(this, e);
        }
    }

    protected void setUp() throws Exception{
    }

    protected void tearDown() throws Exception{
    }

}
    

