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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.tomee.microprofile.tck.restclient;

import org.apache.openejb.loader.JarLocation;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.path.BasicPath;

import java.util.Map;

public class MicroProfileRestClientTCKArchiveProcessor implements ApplicationArchiveProcessor {
    @Override
    public void process(final Archive<?> archive, final TestClass testClass) {
        if (archive instanceof WebArchive) {

            WebArchive webArchive = (WebArchive) archive;
            webArchive.addAsLibrary(JarLocation.jarLocation(org.eclipse.jetty.server.Handler.class));
            webArchive.addAsWebInfResource(new StringAsset(""), "beans.xml");
            final Map<ArchivePath, Node> content = webArchive.getContent();

            final Node node = content.get(new BasicPath("META-INF/certificates-dir.txt"));
            if (node != null) {
                webArchive.addAsResource(node.getAsset(), "META-INF/certificates-dir.txt");
            }
        }
    }
}
