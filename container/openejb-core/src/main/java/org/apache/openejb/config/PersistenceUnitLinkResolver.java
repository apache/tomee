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
package org.apache.openejb.config;

import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.util.URLs;
import org.apache.openejb.util.UniqueDefaultLinkResolver;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

// TODO: review if some more info shouldn't be propagated to module tree to make it faster
public class PersistenceUnitLinkResolver extends UniqueDefaultLinkResolver<PersistenceUnit> {
    private final AppModule module;

    public PersistenceUnitLinkResolver(final AppModule appModule) {
        module = appModule;
    }

    @Override
    protected Collection<PersistenceUnit> tryToResolveForEar(final Collection<PersistenceUnit> values, final URI moduleUri, final String link) {
        if (module == null || module.isStandaloneModule()) { // can't help
            return values;
        }

        final WebModule war = extractWebApp(moduleUri);
        if (war != null) { // keep only values related to this war
            final Iterator<PersistenceUnit> it = values.iterator();
            while (it.hasNext()) {
                if (!isIn(it.next(), war)) {
                    it.remove();
                }
            }
            return values;
        }

        // else remove all webapp info
        final Iterator<PersistenceUnit> it = values.iterator();
        while (it.hasNext()) {
            final PersistenceUnit next = it.next();
            for (WebModule webModule : module.getWebModules()) {
                if (isIn(next, webModule)) {
                    it.remove();
                }
            }
        }

        return values;
    }

    private boolean isIn(final PersistenceUnit value, final WebModule war) {
        final Collection<URL> urls = (Collection<URL>) war.getAltDDs().get(DeploymentLoader.EAR_WEBAPP_PERSISTENCE_XML_JARS);
        if (urls == null || urls.isEmpty()) {
            return false;
        }

        final Collection<String> strUrls = new ArrayList<String>();
        for (URL url : urls) {
            strUrls.add(URLs.toFilePath(url));
        }

        for (PersistenceModule persistenceModule : module.getPersistenceModules()) {
            final Persistence persistence = persistenceModule.getPersistence();
            final String rootUrl;
            try {
                rootUrl = URLs.toFilePath(new URL(persistenceModule.getRootUrl()));
            } catch (MalformedURLException e) {
                continue;
            }

            for (PersistenceUnit unit : persistence.getPersistenceUnit()) {
                if (unit == value) {
                    if (strUrls.contains(rootUrl)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private WebModule extractWebApp(final URI moduleUri) {
        if (module == null || module.getJarLocation() == null) {
            return null;
        }

        final File appModuleFile = new File(module.getJarLocation());

        final File moduleFile;
        try {
            moduleFile = URLs.toFile(moduleUri.toURL());
        } catch (MalformedURLException e) {
            return null;
        }

        for (WebModule webModule : module.getWebModules()) {
            if (webModule.getJarLocation() != null && isParent(new File(webModule.getJarLocation()), moduleFile, appModuleFile)) {
                return webModule;
            }
        }

        return null;
    }

    private static boolean isParent(final File file, final File moduleFile, final File appModuleFile) {
        File current = file;
        while (current != null) {
            if (current.equals(moduleFile)) {
                return true;
            }

            if (current.equals(appModuleFile)) {
                return false;
            }

            current = current.getParentFile();
        }
        return false;
    }
}
