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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.arquillian.tests.jta;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.Extension;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import static org.junit.Assert.fail;

// reproduces camel-cdi
public class TheExtension implements Extension {
    private Object mgr;
    private Object registry;

    private void captureJtaComponents(@Observes final AfterDeploymentValidation adv) {
        // here we need JTA lookups to work. These lookup are often done by spring in practise
        try {
            mgr = InitialContext.doLookup("java:comp/TransactionManager");
        } catch (final NamingException e) {
            fail();
        }
        try {
            registry = InitialContext.doLookup("java:comp/TransactionSynchronizationRegistry");
        } catch (final NamingException e) {
            fail();
        }
    }

    public Object getMgr() {
        return mgr;
    }

    public Object getRegistry() {
        return registry;
    }
}
