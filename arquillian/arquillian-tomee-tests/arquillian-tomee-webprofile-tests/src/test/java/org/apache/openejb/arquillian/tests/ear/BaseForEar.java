/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.arquillian.tests.ear;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import jakarta.inject.Inject;

import static org.junit.Assert.assertEquals;

public abstract class BaseForEar {
    @Deployment
    public static EnterpriseArchive createDeployment() {
        return ShrinkWrap.create(EnterpriseArchive.class, "red.ear")
            .addAsModule(ShrinkWrap.create(WebArchive.class, "bean.war")
                .addClasses(
                    BaseForEar.class, EarNoTestMethodTest.class, // test stack and marking the webapp as the module under test
                    Bean.class) // the tested instance
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml"));
    }

    @Inject
    private Bean bean;

    @Test
    public void run() {
        assertEquals(Test.class.getName(), bean.getMessage());
    }
}
