/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.slice;

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;

public abstract class SingleEMFTestCase
    extends PersistenceTestCase {

    protected OpenJPAEntityManagerFactorySPI emf;

    /**
     * Call {@link #setUp(Object...)} with no arguments so that the emf
     * set-up happens even if <code>setUp()</code> is not called from the
     * subclass.
     */
    public void setUp() throws Exception {
        setUp(new Object[0]);
    }

    /**
     * Initialize entity manager factory. Put {@link #CLEAR_TABLES} in
     * this list to tell the test framework to delete all table contents
     * before running the tests.
     *
     * @param props list of persistent types used in testing and/or 
     * configuration values in the form key,value,key,value...
     */
    protected void setUp(Object... props) {
        emf = createEMF(props);
    }

    /**
     * Closes the entity manager factory.
     */
    public void tearDown() throws Exception {
        super.tearDown();

        if (emf == null)
            return;

        try {
            clear(emf);
        } catch (Exception e) {
            // if a test failed, swallow any exceptions that happen
            // during tear-down, as these just mask the original problem.
            if (testResult.wasSuccessful())
                throw e;
        } finally {
            closeEMF(emf);
        }
    }
    
    protected ClassMapping getMapping(String name) {
        return (ClassMapping) emf.getConfiguration()
                .getMetaDataRepositoryInstance().getMetaData(name,
                        getClass().getClassLoader(), true);
    }
}
