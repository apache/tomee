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
package org.apache.openejb.server.groovy;

import java.util.Properties;
import jakarta.ejb.embeddable.EJBContainer;
import javax.script.ScriptException;

import org.apache.openejb.util.OpenEJBScripter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OpenEJBGroovyShellTest {
    private static EJBContainer container;

    @BeforeClass
    public static void start() {
        final Properties properties = new Properties();
        properties.setProperty(EJBContainer.APP_NAME, OpenEJBGroovyShellTest.class.getSimpleName());
        properties.setProperty("openejb.deployments.classpath.filter.systemapps", "false");
        container = EJBContainer.createEJBContainer(properties);
    }

    @AfterClass
    public static void close() {
        container.close();
    }

    @Test
    public void callUsingCDI() throws ScriptException {
        final OpenEJBScripter shell = new OpenEJBScripter();
        final Object out = shell.evaluate("groovy", "bm.beanFromName('OpenEJBGroovyShellTest', 'bar').test()");
        assertEquals("ok", out);
    }
}
