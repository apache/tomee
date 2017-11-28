/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.connector.starter;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.application6.ApplicationDescriptor;
import org.jboss.shrinkwrap.descriptor.api.connector10.ConfigProperty;
import org.jboss.shrinkwrap.descriptor.api.connector10.ConnectorDescriptor;
import org.jboss.shrinkwrap.descriptor.api.connector10.Resourceadapter;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.concurrent.TimeUnit;

@RunWith(Arquillian.class)
public class Runner {

    @Deployment(testable = false)
    public static EnterpriseArchive createDeployment() {
        final JavaArchive apiJar = ShrinkWrap.create(JavaArchive.class, "api.jar");
        apiJar.addPackages(true, "org.tomitribe.connector.starter.api");
        System.out.println(apiJar.toString(true));
        System.out.println();

        final JavaArchive rarLib = ShrinkWrap.create(JavaArchive.class, "lib.jar");
        rarLib.addPackages(false,
                "org.tomitribe.connector.starter.adapter",
                "org.tomitribe.connector.starter.authenticator");

        System.out.println(rarLib.toString(true));
        System.out.println();

        final ResourceAdapterArchive rar = ShrinkWrap.create(ResourceAdapterArchive.class, "test.rar");
        rar.addAsLibraries(rarLib);

        final ConnectorDescriptor raXml = Descriptors.create(ConnectorDescriptor.class);

        rar.setResourceAdapterXML(new StringAsset(raXml.exportAsString()));
        System.out.println(rar.toString(true));
        System.out.println();

        final WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war");
        final WebAppDescriptor webXml = Descriptors.create(WebAppDescriptor.class);
        webXml.description("Test webapp");
        war.addPackages(true, "org.superbiz");
        war.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        war.setWebXML(new StringAsset(webXml.exportAsString()));

        System.out.println(war.toString(true));
        System.out.println();

        // Make the EAR
        final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "test.ear")
                .addAsModule(rar).addAsModule(war).addAsLibraries(apiJar);

        final ApplicationDescriptor applicationDescriptor = Descriptors.create(ApplicationDescriptor.class);
        applicationDescriptor.libraryDirectory("lib");
        applicationDescriptor.createModule().getOrCreateWeb().contextRoot("test").webUri("test.war");
        applicationDescriptor.getOrCreateModule().connector("test.rar");

        System.out.println(applicationDescriptor.exportAsString());
        ear.setApplicationXML(new StringAsset(applicationDescriptor.exportAsString()));

        System.out.println(ear.toString(true));
        System.out.println();

        return ear;
    }

    @Test
    public void run(){
        try {
            Thread.sleep(TimeUnit.HOURS.toMillis(1));
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
    }
}
