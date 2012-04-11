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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.maven.plugin.dd.merger;

import org.apache.maven.plugin.logging.Log;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.JaxbOpenejbJar3;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.maven.plugin.dd.Merger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

public class OpenEJBJarMerger extends Merger<OpenejbJar> {
    public OpenEJBJarMerger(final Log logger) {
        super(logger);
    }

    @Override
    public OpenejbJar merge(OpenejbJar reference, OpenejbJar toMerge) {
        new EnvEntriesMerger(log).merge(reference.getProperties(), toMerge.getProperties());

        for (EjbDeployment deployment : toMerge.getEjbDeployment()) {
            if (reference.getDeploymentsByEjbName().containsKey(deployment.getEjbName())) {
                log.warn("ejb deployement " + deployment.getEjbName() + " already present");
            } else {
                reference.addEjbDeployment(deployment);
            }
        }

        return reference;
    }

    @Override
    public OpenejbJar createEmpty() {
        return new OpenejbJar();
    }

    @Override
    public OpenejbJar read(URL url) {
        try {
            return JaxbOpenejbJar3.unmarshal(OpenejbJar.class, new BufferedInputStream(url.openStream()));
        } catch (Exception e) {
            return createEmpty();
        }
    }

    @Override
    public String descriptorName() {
        return "openejb-jar.xml";
    }

    @Override
    public void dump(File dump, OpenejbJar object) throws Exception {
        final BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(dump));
        try {
            JaxbOpenejbJar3.marshal(OpenejbJar.class, object, stream);
        } finally {
            stream.close();
        }
    }
}
