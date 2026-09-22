/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jug.rest;

import jug.dao.SubjectDao;
import jug.domain.Subject;
import jug.monitoring.VoteCounter;
import jug.routing.DataSourceInitializer;
import jug.routing.PollingRouter;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.loader.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class SubjectServiceTest {

    @Inject
    private DataSourceInitializer init;

    @Resource(name = "ClientRouter", type = PollingRouter.class)
    private PollingRouter router;

    @ArquillianResource
    private URL base;

    @Deployment
    public static WebArchive archive() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(SubjectServiceTest.class, VoteCounter.class)
                .addPackage(Subject.class.getPackage()) // domain
                .addAsWebInfResource(new ClassLoaderAsset("META-INF/persistence.xml"), "persistence.xml")
                .addAsWebInfResource(new ClassLoaderAsset("META-INF/env-entries.properties"), "env-entries.properties")
                .addAsWebInfResource(new ClassLoaderAsset("META-INF/resources.xml"), "resources.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource(new StringAsset("polling-domain"), "exclusions.list")
                .addPackage(PollingRouter.class.getPackage()) // core
                .addPackage(SubjectDao.class.getPackage()) // core
                .addPackage(SubjectService.class.getPackage()); // front
    }

    @Before
    public void inject() {
        init.init();
    }

    @Test
    public void createVote() throws IOException {
        final Response response = WebClient.create(base.toExternalForm())
                .path("api/subject/create")
                .accept("application/json")
                .query("name", "TOMEE_JUG_JSON")
                .post("was it cool?");
        final String output = IO.slurp((InputStream) response.getEntity());
        assertTrue("output doesn't contain TOMEE_JUG_JSON '" + output + "'", output.contains("TOMEE_JUG_JSON"));
    }
}
