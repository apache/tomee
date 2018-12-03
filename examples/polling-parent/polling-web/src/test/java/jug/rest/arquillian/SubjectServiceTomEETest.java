/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jug.rest.arquillian;

import jug.dao.SubjectDao;
import jug.domain.Subject;
import jug.monitoring.VoteCounter;
import jug.rest.SubjectService;
import jug.routing.PollingRouter;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.ziplock.JarLocation;
import org.apache.ziplock.WebModule;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedInputStream;
import java.net.URL;

import static org.junit.Assert.fail;

@RunAsClient
@RunWith(Arquillian.class)
public class SubjectServiceTomEETest {

    @ArquillianResource
    private URL url;

    @Deployment
    public static WebArchive archive() {
        return new WebModule(SubjectServiceTomEETest.class).getArchive()
                .addClass(VoteCounter.class)
                .addPackage(Subject.class.getPackage()) // domain
                .addAsWebInfResource(new ClassLoaderAsset("META-INF/persistence.xml"), "persistence.xml")
                .addAsWebInfResource(new ClassLoaderAsset("META-INF/env-entries.properties"), "env-entries.properties")
                .addAsWebInfResource(new ClassLoaderAsset("META-INF/resources.xml"), "resources.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource(new StringAsset("polling-domain"), "exclusions.list")
                .addPackage(PollingRouter.class.getPackage()) // core
                .addPackage(SubjectDao.class.getPackage()) // core
                .addPackage(SubjectService.class.getPackage()) // front
                .addAsLibrary(JarLocation.jarLocation(IOUtils.class)) // helper for client test
                .addAsLibrary(JarLocation.jarLocation(Test.class)); // junit
    }

    @Test
    public void checkThereIsSomeOutput() throws Exception {
        final String base = url.toExternalForm();
        WebClient.create(base)
                .path("api/subject/create")
                .accept("application/json")
                .query("name", "SubjectServiceTomEETest")
                .post("SubjectServiceTomEETest");
        for (int i = 0; i < 2; i++) { // we have a dynamic datasource so let it round
            final URL url = new URL(base + "api/subject/list");
            final String output = IOUtils.toString(new BufferedInputStream(url.openStream()));
            if (output.contains("SubjectServiceTomEETest")) {
                return;
            }
        }
        fail("created entry not found");
    }
}
