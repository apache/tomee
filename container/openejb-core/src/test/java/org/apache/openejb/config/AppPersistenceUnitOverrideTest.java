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
package org.apache.openejb.config;

import org.apache.openejb.api.configuration.PersistenceUnitDefinition;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.ApplicationConfiguration;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.SimpleLog;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.log.LogFactory;
import org.apache.openjpa.lib.log.NoneLogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

import static org.junit.Assert.assertFalse;

@RunWith(ApplicationComposer.class)
@PersistenceUnitDefinition
@Classes
@SimpleLog
public class AppPersistenceUnitOverrideTest {
    @ApplicationConfiguration
    public Properties properties() {
        MyLogFactory.CHANNELS.clear();
        return new PropertiesBuilder().p("openjpa.Log", MyLogFactory.class.getName()).build();
    }

    @Test
    public void run() {
        assertFalse(MyLogFactory.CHANNELS.isEmpty());
    }

    public static class MyLogFactory implements LogFactory {
        public static final Collection<String> CHANNELS = new LinkedList<>();

        @Override
        public Log getLog(final String channel) {
            CHANNELS.add(channel);
            return new NoneLogFactory.NoneLog();
        }
    }
}
