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
package org.apache.openejb.assembler.classic.enricher;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.component.ClassLoaderEnricher;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.ProvisioningUtil;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.observer.event.ObserverAdded;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class ResolverClassLoaderEnricherObserver {
    private static boolean initDone = false;

    private String configFile = "additional-lib";

    public void initEnricher(@Observes final ObserverAdded event) {
        if (initDone || configFile == null || !ResolverClassLoaderEnricherObserver.class.isInstance(event.getObserver())) {
            return;
        }

        final File file = new File(configFile);
        if (file.exists()) {
            final ClassLoaderEnricher enricher = SystemInstance.get().getComponent(ClassLoaderEnricher.class);

            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }

                    final File lib = new File(ProvisioningUtil.realLocation(line));
                    if (lib.exists()) {
                        enricher.addUrl(lib.toURI().toURL());
                    } else {
                        throw new OpenEJBRuntimeException("can't find " + line);
                    }
                }
            } catch (Exception e) {
                throw new OpenEJBRuntimeException(e);
            } finally {
                IO.close(reader);
            }
        }

        initDone = true;
    }

    public void setConfigFile(final String configFile) {
        this.configFile = configFile;
    }
}
