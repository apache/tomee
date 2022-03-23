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

import org.apache.openejb.loader.SystemInstance;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class WARArquillianTest {
    @Inject
    private ABean bean;

    @Deployment
    public static WebArchive archive() {
        return ShrinkWrap.create(WebArchive.class, WARArquillianTest.class.getSimpleName().concat(".war"))
                    .addClasses(ABean.class)
                    .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("classes/META-INF/beans.xml"));
    }

    @Test
    public void checkItIsStarted() {
        assertTrue(SystemInstance.isInitialized());
    }

    @Test
    public void checkInjections() {
        assertNotNull(bean);
    }

    public static class ABean {}
}
