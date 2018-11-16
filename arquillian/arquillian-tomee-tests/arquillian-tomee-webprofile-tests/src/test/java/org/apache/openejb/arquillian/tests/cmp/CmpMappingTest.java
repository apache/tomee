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
package org.apache.openejb.arquillian.tests.cmp;

import org.apache.ziplock.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;

/**
 * @version $Rev$ $Date$
 */
@RunWith(Arquillian.class)
public class CmpMappingTest {

    @ArquillianResource
    private URL url;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, CmpMappingTest.class.getSimpleName() + ".war")
                .addClasses(CmpServlet.class, MyCmpBean.class, MyLocalHome.class, MyLocalObject.class, MyRemoteHome.class, MyRemoteObject.class)
                .addAsWebInfResource(new ClassLoaderAsset("org/apache/openejb/arquillian/tests/cmp/openejb-cmp-orm.xml"), "openejb-cmp-orm.xml")
                .addAsWebInfResource(new ClassLoaderAsset("org/apache/openejb/arquillian/tests/cmp/ejb-jar.xml"), "ejb-jar.xml");

        System.out.println(archive.toString(true));
        return archive;
    }

    @Test
    @RunAsClient
    public void checkCmpJpaEntityORMMappings() throws Exception {
        final String output = IO.readString(new URL(url.toExternalForm()));
        System.out.println(output);
    }
}
