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

package org.apache.openejb.arquillian.tests.naming;

import org.apache.openejb.arquillian.tests.Runner;
import org.apache.ziplock.JarLocation;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import java.util.logging.Logger;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class IvmContextTest {
    private static final Logger logger = Logger.getLogger(IvmContextTest.class.getName());
    private static final String TEST_NAME = IvmContextTest.class.getSimpleName();
    private static final String RESOURCE_EJB_JAR_XML = "ejb-jar.xml";
    private static final String CONTENT_LOCATION_EJB_JAR_XML = "org/apache/openejb/arquillian/tests/naming/list-context-ejbjar.xml";

    @EJB
    private NamingBean namingBean;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, TEST_NAME + ".war")
                .addClass(IvmContextTest.class)
                .addClass(NamingBean.class)
                .addClass(Runner.class)
                .addAsLibraries(JarLocation.jarLocation(Test.class))
                .addAsWebInfResource(new ClassLoaderAsset(CONTENT_LOCATION_EJB_JAR_XML), RESOURCE_EJB_JAR_XML);
        return archive;
    }


    @Test
    public void testListContextTree() throws Exception {
        namingBean.verifyContextList();
    }

    @Test
    public void testContextListBindings() throws Exception {
        namingBean.verifyContextListBindings();
    }
}
