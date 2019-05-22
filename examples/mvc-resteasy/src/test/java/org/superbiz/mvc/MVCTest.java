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
package org.superbiz.mvc;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@RunWith(Arquillian.class)
public class MVCTest {

    @ArquillianResource
    private URL base;

    @Drone
    private WebDriver webDriver;

    @Deployment
    public static WebArchive createDeployment() {
        File[] files = Maven.resolver()
                            .loadPomFromFile("pom.xml")
                            .importRuntimeDependencies()
                            .resolve()
                            .withTransitivity()
                            .asFile();

        return ShrinkWrap.create(WebArchive.class, "test.war")
                         .addPackages(true, "org.superbiz.mvc")
                         .addAsLibraries(files)
                         .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                         .addAsWebInfResource(new File("src/main/webapp/WEB-INF/web.xml"), "/web.xml")
                         .addAsWebInfResource(new File("src/main/webapp/WEB-INF/views/hello.jsp"), "/views/hello.jsp");
    }

    @Test
    @RunAsClient
    public void test() {
        webDriver.get(this.base.toExternalForm() + "app/hello?name=TomEE");
        WebElement h1 = webDriver.findElement(By.tagName("h1"));
        assertNotNull(h1);
        assertTrue(h1.getText().contains("Welcome TomEE !"));
    }
}