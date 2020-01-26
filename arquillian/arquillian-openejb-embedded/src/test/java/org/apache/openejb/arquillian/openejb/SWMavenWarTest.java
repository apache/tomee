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

import org.apache.openejb.util.classloader.URLClassLoaderFirst;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class SWMavenWarTest {
    @Deployment
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "sw-mvn.war")
                .addClass(SWBean.class)
                .addAsLibraries(Maven.resolver()
                        .loadPomFromFile("src/test/resources/a-pom.xml")
                        .importCompileAndRuntimeDependencies().resolve().withTransitivity().asFile());
    }

    @Singleton
    @TransactionManagement(TransactionManagementType.BEAN)
    public static class SWBean {
        public Class<?> gherkin() throws Exception {
            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
            assertThat(loader.getParent(), instanceOf(URLClassLoaderFirst.class));
            return loader.loadClass("gherkin.Main");
        }
    }

    @EJB
    private SWBean bean;

    @Test
    public void check() throws Exception {
        bean.gherkin(); // if fail will throw an exception
    }
}
