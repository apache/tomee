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
package org.apache.openejb.tck.cdi.tomee;

import org.apache.openejb.loader.Files;
import org.apache.webbeans.test.tck.ELImpl;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import java.io.File;

import static org.apache.openejb.loader.JarLocation.jarLocation;

public final class EnrichmentDumper {
    public static void main(String[] args) {
        final File output = new File(Files.mkdirs(new File(args[0])), "tomee-porting.jar");
        if (!output.exists() || output.delete()) {
            ShrinkWrap.create(JavaArchive.class)
                    .merge(ShrinkWrap.createFromZipFile(JavaArchive.class, jarLocation(ELImpl.class)))
                    .addClasses(BeansImpl.class)
                    .as(ZipExporter.class)
                    .exportTo(output);
        }
    }

    private EnrichmentDumper() {
        // no-op
    }
}
