/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.moviefun;

import org.apache.tomee.arquillian.remote.RemoteTomEEConfiguration;
import org.apache.tomee.arquillian.remote.RemoteTomEEContainer;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Not implemented yet")
/*
 * This test deploys the ear in a manual fashion. Normally the remote adapter deploys using the deployer.
 * Here we'll use the arquillian adapter to control the lifecycle of the server, but we'll do the deploy
 * by hand into the webapps directory directly. The EAR deployment was fixed in TOMEE-2145.
 *
 */
public class DeployInWebAppsDirectoryTest {

    @Test
    public void test() throws Exception {
        final RemoteTomEEConfiguration configuration = new RemoteTomEEConfiguration();
        configuration.setGroupId("org.apache.tomee");
        configuration.setArtifactId("apache-tomee");
        configuration.setClassifier("plus");
        configuration.setVersion("7.0.5-SNAPSHOT");
        configuration.setHttpPort(-1);

        final RemoteTomEEContainer container = new RemoteTomEEContainer();
        container.setup(configuration);

        // TODO: copy application in manually

        container.start();


        container.stop();


    }



}
