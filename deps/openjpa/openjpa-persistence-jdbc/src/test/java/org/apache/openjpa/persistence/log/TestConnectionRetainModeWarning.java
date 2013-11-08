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
package org.apache.openjpa.persistence.log;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.apache.openjpa.lib.log.AbstractLog;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.log.LogFactory;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.persistence.PersistenceProviderImpl;
import org.apache.openjpa.persistence.test.AbstractPersistenceTestCase;

public class TestConnectionRetainModeWarning extends AbstractPersistenceTestCase implements LogFactory {
    private static List<String> messages = new ArrayList<String>();

    Localizer _loc = Localizer.forPackage(PersistenceProviderImpl.class);

    public void tearDown() throws Exception {
        super.tearDown();
        messages.clear();
    }

    // Start LogFactory implementation
    public Log getLog(String channel) {
        return new AbstractLog() {

            protected boolean isEnabled(short logLevel) {
                return true;
            }

            @Override
            public void trace(Object message) {
                messages.add(message.toString());
            }

            protected void log(short type, String message, Throwable t) {
                messages.add(message);
            }

            @Override
            public void error(Object message) {
                messages.add(message.toString());
            }

            @Override
            public void warn(Object message) {
                super.warn(message.toString());
            }

            @Override
            public void info(Object message) {
                messages.add(message.toString());
            }
        };
    }

    public void assertMessageContains(String s) {
        for (String message : messages) {
            if (message.contains(s)) {
                return;
            }
        }
        fail("Did not find message " + s + " in " + messages);
    }

    public void assertMessageNotFound(String s) {
        for (String message : messages) {
            if (message.contains(s)) {
                fail("Found unexpected messsage " + s);
            }
        }
    }

    public void testInfoMessage() {
        EntityManagerFactory emf =
            createEMF("openjpa.Log", this.getClass().getCanonicalName(), "openjpa.ConnectionRetainMode", "always");

        assertNotNull(emf);
        assertMessageContains(_loc.get("retain-always", getPersistenceUnitName()).toString());
        emf.close();
    }

    public void testInfoMessageNotFound() {
        EntityManagerFactory emf =
            createEMF("openjpa.Log", this.getClass().getCanonicalName(), "openjpa.ConnectionRetainMode", "on-demand");

        assertNotNull(emf);
        assertMessageNotFound(_loc.get("retain-always", getPersistenceUnitName()).toString());
        emf.close();
    }

}
