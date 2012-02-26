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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.jee.accessors;

import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.ApplicationClient;
import org.apache.openejb.jee.Beans;
import org.apache.openejb.jee.Connector;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.FacesConfig;
import org.apache.openejb.jee.HandlerChains;
import org.apache.openejb.jee.JAXBContextFactory;
import org.apache.openejb.jee.JavaWsdlMapping;
import org.apache.openejb.jee.Persistence;
import org.apache.openejb.jee.TldTaglib;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebFragment;
import org.apache.openejb.jee.Webservices;
import org.apache.openejb.jee.bval.ValidationConfigType;
import org.apache.openejb.jee.jpa.EntityMappings;
import org.apache.openejb.jee.jpa.fragment.PersistenceFragment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.junit.Test;

public class GenerateAccessorsTest {

    @Test
    public void test() throws Exception {
        JAXBContextFactory.newInstance(
                Application.class,
                ApplicationClient.class,
                Beans.class,
                Connector.class,
                EjbJar.class,
                FacesConfig.class,
                HandlerChains.class,
                JavaWsdlMapping.class,
                Persistence.class,
                TldTaglib.class,
                WebApp.class,
                WebFragment.class,
                Webservices.class,
                ValidationConfigType.class,
                EntityMappings.class,
                OpenejbJar.class,
                org.apache.openejb.jee.jpa.unit.Persistence.class,
                PersistenceFragment.class
        );
    }
}
