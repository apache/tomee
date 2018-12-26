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
package org.apache.openejb.arquillian.tests.bmp.remote;

import org.apache.ziplock.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;

/**
 * @version $Rev$ $Date$
 */
@RunWith(Arquillian.class)
public class BmpRemoteEntityTest {

    @ArquillianResource
    private URL url;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, BmpRemoteEntityTest.class.getSimpleName() + ".war")
                .addClasses(BigFinder.class, BigFinderBean.class, BigFinderHome.class, BigFinderPK.class,
                        FinderServlet.class, FinderTest.class, FinderTestBean.class, FinderTestHome.class, IntegerPK.class,
                        LittleFinder.class, LittleFinderBean.class, LittleFinderHome.class, LittleFinderPK.class, StringPK.class)
                .addAsWebInfResource(new ClassLoaderAsset("org/apache/openejb/arquillian/tests/bmp/remote/ejb-jar.xml"), "ejb-jar.xml")
                .addAsWebInfResource(new ClassLoaderAsset("org/apache/openejb/arquillian/tests/bmp/remote/web.xml"), "web.xml");

        System.out.println(archive.toString(true));
        return archive;
    }

    @Test
    @RunAsClient
    public void checkBmp() throws Exception {
        final String output = IO.slurp(new URL(url.toExternalForm()));
        System.out.println(output);
        Assert.assertTrue(output.contains("Test succeeded"));
    }
}
