package org.superbiz.executor;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;


/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@RunWith(Arquillian.class)
public class ThreadFactoryServiceTest {

    @Inject
    private ThreadFactoryService factoryService;

    @Deployment()
    public static final WebArchive app() {
        return ShrinkWrap.create(WebArchive.class, "example.war")
                .addClasses(ThreadFactoryService.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    //TODO failing to load due to missing DeployerBusinessRemote
    @Test
    public void asyncTask() throws InterruptedException {
        //task was completed
        assertEquals(2, factoryService.asyncTask(1));
    }

    @Test
    public void asyncHangingTask() throws InterruptedException {
        // task was interrupted and operation was not completed.
        assertEquals(1, factoryService.asyncHangingTask(1));
    }
}