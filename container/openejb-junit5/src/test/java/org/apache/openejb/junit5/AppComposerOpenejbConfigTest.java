/*
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
package org.apache.openejb.junit5;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit5.RunWithApplicationComposer;
import org.apache.openejb.testing.AppResource;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.jupiter.api.Test;

import javax.naming.Context;
import javax.naming.NamingException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWithApplicationComposer
public class AppComposerOpenejbConfigTest {
    @Configuration
    public String openejbXmlPath() {
        return "custom-openejb.xml";
    }

    @Module
    public EjbJar empty() {
        return new EjbJar();
    }

    @AppResource
    private Context ctx;

    @Test
    public void checkDsIsHere() throws NamingException {
        assertNotNull(ctx.lookup("openejb:Resource/app-composer"));
    }
}
