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
package org.apache.openejb.cdi;

import org.apache.openejb.assembler.classic.BeansInfo;
import org.apache.openejb.loader.SystemInstance;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.spi.BeanArchiveService;
import org.apache.webbeans.xml.DefaultBeanArchiveInformation;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class OpenEJBBeanInfoService implements BeanArchiveService {
    private Map<URL, BeanArchiveInformation> beanArchiveInfo = new HashMap<>();

    public Map<URL, BeanArchiveInformation> getBeanArchiveInfo() {
        return beanArchiveInfo;
    }

    public DefaultBeanArchiveInformation createBeanArchiveInformation(final BeansInfo.BDAInfo bda, final BeansInfo info, final ClassLoader loader) {
        String mode = bda.discoveryMode == null? "ALL" : bda.discoveryMode;
        if (info != null && info.version != null && !"1.0".equals(info.version) && info.discoveryMode == null) {
            throw new WebBeansConfigurationException("beans.xml with version 1.1 and higher must declare a bean-discovery-mode!");
        }
        if ("ALL".equalsIgnoreCase(mode) && bda.trim) {
            mode = "TRIM";
        }

        final DefaultBeanArchiveInformation information = new DefaultBeanArchiveInformation(bda.uri.toASCIIString());
        information.setVersion(info == null ? "1.1" : info.version);
        information.setBeanDiscoveryMode(BeanDiscoveryMode.valueOf(mode.trim().toUpperCase(Locale.ENGLISH)));
        information.setDecorators(bda.decorators);
        information.setInterceptors(bda.interceptors);
        if (info != null) {
            information.getAlternativeClasses().addAll(bda.alternatives);
            information.getAlternativeStereotypes().addAll(bda.stereotypeAlternatives);

            for (final BeansInfo.ExclusionEntryInfo exclusionInfo : info.excludes) {
                boolean skip = false;
                for (final String n : exclusionInfo.exclusion.availableClasses) {
                    if (!isClassAvailable(loader, n)) {
                        skip = true;
                        break;
                    }
                }
                if (!skip) {
                    for (final String n : exclusionInfo.exclusion.notAvailableClasses) {
                        if (isClassAvailable(loader, n)) {
                            skip = true;
                            break;
                        }
                    }
                }
                if (!skip) {
                    for (final String n : exclusionInfo.exclusion.systemPropertiesPresence) {
                        // our system instance is more powerful here
                        if (SystemInstance.get().getProperty(n) == null) {
                            skip = true;
                            break;
                        }
                    }
                }
                if (!skip) {
                    for (final String n : exclusionInfo.exclusion.systemProperties.stringPropertyNames()) {
                        // our system instance is more powerful here
                        if (!exclusionInfo.exclusion.systemProperties.getProperty(n).equals(SystemInstance.get().getProperty(n))) {
                            skip = true;
                            break;
                        }
                    }
                }
                if (skip) {
                    continue;
                }

                final String name = exclusionInfo.name;
                if (name.endsWith(".*")) {
                    information.addClassExclude(name.substring(0, name.length() - 2));
                }
                else if (name.endsWith(".**")) {
                    information.addPackageExclude(name.substring(0, name.length() - 3));
                }
                else {
                    information.addClassExclude(name);
                }
            }
        }

        return information;
    }

    private static boolean isClassAvailable(final ClassLoader loader, final String name) {
        try {
            loader.loadClass(name);
            return true;
        } catch (final Throwable e) {
            return false;
        }
    }

    @Override
    public BeanArchiveInformation getBeanArchiveInformation(final URL beanArchiveUrl) {
        return beanArchiveInfo.get(beanArchiveUrl);
    }

    @Override
    public Set<URL> getRegisteredBeanArchives() {
        return Collections.emptySet(); // avoid to register twice decorators/interceptors/stereotypes/...
    }

    @Override
    public void release() {
        // no-op
    }
}
