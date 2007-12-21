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

import junit.framework.Test;
import junit.framework.TestResult;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
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
        try {
            List<Test> tests = getTests();
            if (tests.size() == 0) return;

            setUp();

            try {
                for (Test test : tests) {
                    if (result.shouldStop()) break;
                    test.run(result);
                }
            } finally {
                tearDown();
            }

        } catch (Exception e) {
            result.addError(this, e);
        }
    }

    protected List<Test> getTests() {
        return Collections.list(tests());
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

}
    

