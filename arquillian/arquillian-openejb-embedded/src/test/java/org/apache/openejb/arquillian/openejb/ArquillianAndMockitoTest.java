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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class ArquillianAndMockitoTest {
    @Inject
    private AFooBean bean;

    @Mock @Produces
    private static AnInterface mock;

    @Deployment
    public static JavaArchive archive() {
        return ShrinkWrap.create(JavaArchive.class, ArquillianAndMockitoTest.class.getSimpleName().concat(".jar"))
                .addClasses(AnInterface.class, AFooBean.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
    }

    @Test
    public void mockWorks() {
        assertNotNull(bean);
        assertNotNull(mock);
        assertNotNull(bean.get());
    }

    public static interface AnInterface {}

    public static class AFooBean {
        @Inject
        private AnInterface mock;

        public AnInterface get() {
            return mock;
        }
    }
}
