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
package org.superbiz.registry;

import junit.framework.TestCase;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;

public class PropertiesRegistryTest extends TestCase {

    public void test() throws Exception {

        Context context = EJBContainer.createEJBContainer().getContext();

        PropertyRegistry one = (PropertyRegistry) context.lookup("java:global/simple-singleton/PropertyRegistry");

        PropertyRegistry two = (PropertyRegistry) context.lookup("java:global/simple-singleton/PropertyRegistry");


        one.setProperty("url", "http://superbiz.org");

        String url = two.getProperty("url");

        assertEquals("http://superbiz.org", url);


        two.setProperty("version", "1.0.5");

        String version = one.getProperty("version");

        assertEquals("1.0.5", version);

    }
}
