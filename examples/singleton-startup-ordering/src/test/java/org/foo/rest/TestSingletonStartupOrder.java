/*
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

package org.foo.rest;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.foo.SingletonA;
import org.foo.SingletonB;
import org.foo.SingletonC;
import org.foo.Supervisor;

import java.util.logging.Logger;

import static junit.framework.TestCase.assertTrue;


@RunWith(Arquillian.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestSingletonStartupOrder {
    private final static Logger LOGGER = Logger.getLogger(TestSingletonStartupOrder.class.getName());

    @Deployment()
    public static WebArchive createDeployment() {
        final WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test.war")
                                                .addClass(SingletonA.class)
                                                .addClass(SingletonB.class)
                                                .addClass(SingletonC.class)
                                                .addClass(Supervisor.class)
                                                .addAsWebInfResource(new StringAsset("<beans/>"), "beans.xml");
        return webArchive;
    }


    @Test
    public void firstTest(Supervisor supervisor) {
        LOGGER.info("SUPERVISOR: [" + supervisor.getRecord() + "]");
        assertTrue(supervisor.getRecord().equals("[SingletonA, SingletonB]"));
    }

    @Test
    public void secondTest(Supervisor supervisor, SingletonC singletonC) {
        LOGGER.info(singletonC.hello());
        LOGGER.info("SUPERVISOR: [" + supervisor.getRecord() + "]");
        assertTrue(supervisor.getRecord().equals("[SingletonA, SingletonB, SingletonC]"));
    }
}
