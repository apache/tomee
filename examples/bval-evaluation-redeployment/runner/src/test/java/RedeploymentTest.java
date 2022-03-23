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

import org.apache.cxf.jaxrs.client.WebClient;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.core.MediaType;
import java.io.File;

@RunWith(Arquillian.class)
public class RedeploymentTest {
    private static final String projectDir;

    static {
        String userDir = System.getProperty("user.dir");
        String runnerDir = "runner";

        //If running this test alone from runner subdir, exclude the containing
        //directory from the path
        if (userDir.contains(runnerDir)) {
            userDir = userDir.substring(0, userDir.length() - runnerDir.length());
        }

        projectDir = userDir;
    }

    public RedeploymentTest() {
    }

    @Deployment(name = "webapp1", managed = false)
    public static Archive<?> webapp1() {
        return ShrinkWrap.createFromZipFile(WebArchive.class, new File(projectDir + "/WebApp1/target/WebApp1.war"));
    }

    @Deployment(name = "webapp2", managed = false)
    public static Archive<?> webapp2() {
        return ShrinkWrap.createFromZipFile(WebArchive.class, new File(projectDir + "/WebApp2/target/WebApp2.war"));
    }

    @ArquillianResource
    private Deployer deployer;

    @Test
    public void validateTest() throws Exception {

        final String port = System.getProperty("server.http.port", "8080");
        System.out.println("");
        System.out.println("===========================================");
        System.out.println("Running test on port: " + port);

        final String urlPath1 = "http://localhost:" + port + "/WebApp1/test/";
        final String urlPath2 = "http://localhost:" + port + "/WebApp2/test/";

        deployer.deploy("webapp1");
        int result = WebClient.create(urlPath1)
                .type(MediaType.APPLICATION_JSON_TYPE).post("validd").getStatus();

        System.out.println(result);
        Assert.assertEquals(406, result);

        deployer.undeploy("webapp1");
        deployer.deploy("webapp2");

        result = WebClient.create(urlPath2)
                .type(MediaType.APPLICATION_JSON_TYPE).post("validd").getStatus();

        System.out.println(result);
        Assert.assertEquals(406, result);

        result = WebClient.create(urlPath2)
                 .type(MediaType.APPLICATION_JSON_TYPE).post("valid").getStatus();

        System.out.println(result);
        Assert.assertEquals(200, result);
 
        deployer.undeploy("webapp2");

        System.out.println("===========================================");
        System.out.println("");
    }

}
