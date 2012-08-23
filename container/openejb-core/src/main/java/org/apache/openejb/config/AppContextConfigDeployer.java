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
package org.apache.openejb.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.sys.SaxAppCtxConfig;
import org.apache.openejb.loader.IO;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

public class AppContextConfigDeployer implements DynamicDeployer {
    private static final String CONFIG_NAME = "app-ctx.xml";

    @Override
    public AppModule deploy(final AppModule appModule) throws OpenEJBException {
        final Collection<DeploymentModule> deploymentModule = appModule.getDeploymentModule();
        deploymentModule.add(appModule);

        for (DeploymentModule module : deploymentModule) {
            final Object o = module.getAltDDs().get(CONFIG_NAME);
            if (o instanceof URL) {
                configure(appModule, (URL) o);
            } else if (o != null) {
                throw new OpenEJBException("Unknown application.properties type: " + o.getClass().getName());
            }
        }

        return appModule;
    }

    private static void configure(final AppModule appModule, final URL url) throws OpenEJBException {
        InputStream is = null;
        try {
            is = IO.read(url);
            SaxAppCtxConfig.parse(appModule, new InputSource(is)); // work directly on the module, avoid temp objects
        } catch (SAXException e) {
            throw new OpenEJBException("can't parse " + url.toExternalForm(), e);
        } catch (ParserConfigurationException e) {
            throw new OpenEJBException("can't configure the parser for " + url.toExternalForm(), e);
        } catch (IOException e) {
            throw new OpenEJBException("can't read " + url.toExternalForm(), e);
        } finally {
            IO.close(is);
        }
    }
}
