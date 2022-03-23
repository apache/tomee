/**
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
package org.apache.openejb.arquillian.openejb;

import org.apache.webbeans.exception.WebBeansDeploymentException;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.beans10.BeansDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class ExceptionInjectionTest {
    @ArquillianResource
    private DeploymentException de;

    @ArquillianResource
    private WebBeansDeploymentException owbException;

    @ArquillianResource
    private DeploymentException oejbException;

    @Deployment(testable = false)
    @ShouldThrowException(jakarta.enterprise.inject.spi.DeploymentException.class)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class)
                .addAsWebInfResource(new StringAsset(Descriptors.create(BeansDescriptor.class)
                        .getOrCreateInterceptors()
                            .clazz("i.dont.exist.so.i.ll.make.the.deployment.fail")
                        .up()
                        .exportAsString()), ArchivePaths.create("beans.xml"));
    }

    @Test
    public void checkSomeExceptionsOfTheHierarchy() {
        assertNotNull(de);
        assertNotNull(owbException);
        assertNotNull(oejbException);
    }
}
