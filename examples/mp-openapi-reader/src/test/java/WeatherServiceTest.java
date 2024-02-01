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

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.apache.openejb.loader.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.superbiz.microprofile.openapi.CustomReader;
import org.superbiz.microprofile.openapi.WeatherService;
import org.superbiz.microprofile.openapi.moviefun.Movie;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@RunWith(Arquillian.class)
public class WeatherServiceTest {

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        final WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addClass(WeatherService.class)
                .addClass(CustomReader.class)
                .addPackages(true, Movie.class.getPackage())
                .addAsResource("META-INF/services/jakarta.servlet.ServletContainerInitializer")
                .addAsResource("META-INF/persistence.xml")
                .addAsResource("META-INF/microprofile-config.properties")
                .addAsResource(new File("src/main/webapp/my-openapi.json"), "my-openapi.json")
                .addAsResource(new File("src/main/webapp/openapi/openapi.yaml"), "openapi/openapi.yaml")
                .addAsWebInfResource(new StringAsset("<beans/>"), "beans.xml");
        return webArchive;
    }

    @ArquillianResource
    private URL base;

    private Client client;

    @Before
    public void before() {
        this.client = ClientBuilder.newClient();
    }

    @After
    public void after() {
        this.client.close();
    }

    @Test
    public void openapi() throws IOException {
        final WebTarget webTarget = client.target(base.toExternalForm());

        final Response response = webTarget.path("/openapi")
                                           .request()
                                           .accept("application/json")
                                           .buildGet().invoke();
        final InputStream entity = (InputStream) response.getEntity();
        final String payload = IO.slurp(entity);

        // assert that it returns a payload
        Assert.assertNotNull(payload);

        // now check if the 2 openapi documents (yaml and json) got successfully loaded and merged
        Assert.assertTrue(payload.contains("/rest/weather/day/status")); // from one openapi file
        Assert.assertTrue(payload.contains("/rest/movies")); // from the other one
    }

}
