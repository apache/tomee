/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.openejb.arquillian.tests.resource;

import org.apache.openejb.api.resource.DestroyableResource;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;

@RunWith(Arquillian.class)
public class ResourceTest {

    @EJB
    private TestEjb ejb;

    @Deployment
    public static EnterpriseArchive createDeployment() {

        final JavaArchive ejbJar = ShrinkWrap.create(JavaArchive.class, "test-ejb.jar")
                .addAsResource("META-INF/resources.xml", "META-INF/resources.xml")
                .addClass(ResourceTest.class)
                .addClass(Destroyable.class)
                .addClass(Hello.class)
                .addClass(TestEjb.class);

        final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "test.ear")
                .addAsModule(ejbJar);

        return ear;
    }

    @Test
    public void test() throws Exception {
        Assert.assertTrue(ejb.isPostConstructCalled());
    }

    @Singleton
    @Lock(LockType.READ)
    public static class TestEjb {

        @Resource(name = "test/Hello")
        private Hello hello;

        public boolean isPostConstructCalled() {
            return hello.isPostConstructCalled();
        }
    }

    public static class Hello {

        private boolean postConstructCalled = false;
        private boolean preDestroyCalled = false;

        @PostConstruct
        public void postConstruct() {
            postConstructCalled = true;
        }

        @PreDestroy
        public void preDestroy() {
            preDestroyCalled = true;
        }

        public boolean isPostConstructCalled() {
            return postConstructCalled;
        }

        public boolean isPreDestroyCalled() {
            return preDestroyCalled;
        }
    }

    public static class Destroyable implements DestroyableResource {

        private boolean destroyCalled = false;

        @Override
        public void destroyResource() {
            destroyCalled = true;
        }

        public boolean isDestroyCalled() {
            return destroyCalled;
        }
    }

}
