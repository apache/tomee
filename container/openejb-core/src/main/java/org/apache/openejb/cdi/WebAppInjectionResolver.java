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

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanCacheKey;
import org.apache.webbeans.container.InjectionResolver;
import org.apache.webbeans.spi.BDABeansXmlScanner;
import org.apache.webbeans.spi.ScannerService;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebAppInjectionResolver extends InjectionResolver {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, WebAppInjectionResolver.class);
    private final WebBeansContext context;

    private final boolean cacheResolutionFailure = Boolean.parseBoolean(SystemInstance.get().getProperty("openejb.cache.cdi-type-resolution-failure", "false"));

    private boolean startup;

    private final Set<BeanCacheKey> resolutionFailures = ConcurrentHashMap.newKeySet();

    public WebAppInjectionResolver(final WebBeansContext ctx) {
        super(ctx);
        context = ctx;
        startup = true;
    }

    @Override
    public Set<Bean<?>> implResolveByType(final boolean delegate, final Type injectionPointType, final Class<?> injectionPointClass, final Annotation... qualifiers) {
        // OWB will cache instances where the resolution by type is successful, but does not when it fails
        // The upshot is that if resolution happens again, the InjectionResolver will need to attempt
        // resolution again, before then trying implResolveByType() on the context.getParent() InjectionResolver,
        // and this will happen each and every time the resolution is attempted at runtime.
        //
        // This code attempts to cache lookup failures that happen outside of the startup phase. Caching resolution
        // failures should NOT be done during startup.
        //
        // The default for this behaviour is "off", and simply delegate to OWB, unless it is turned on.

        Set<Bean<?>> set;
        if (!startup && cacheResolutionFailure) {
            final ScannerService scannerService = context.getScannerService();
            String bdaBeansXMLFilePath = null;
            if (scannerService.isBDABeansXmlScanningEnabled()) {
                bdaBeansXMLFilePath = getBDABeansXMLPath(injectionPointClass);
            }

            final BeanCacheKey cacheKey = new BeanCacheKey(delegate, injectionPointType, bdaBeansXMLFilePath, this::findQualifierModel, qualifiers);

            if (resolutionFailures.contains(cacheKey)) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Resolution of " + cacheKey + " has previously failed, returning empty set from cache");
                }
                set = Collections.emptySet();
            } else {
                set = super.implResolveByType(delegate, injectionPointType, injectionPointClass, qualifiers);
                if (set.isEmpty()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Caching resolution failure of " + cacheKey);
                    }
                    resolutionFailures.add(cacheKey);
                }
            }
        } else {
            set = super.implResolveByType(delegate, injectionPointType, injectionPointClass, qualifiers);
        }

        if (context instanceof WebappWebBeansContext) {
            final WebappWebBeansContext wwbc = (WebappWebBeansContext) context;
            if (set.isEmpty() && wwbc.getParent() != null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Resolution of " + injectionPointType.getTypeName() + " from context failed, trying to resolve from parent context");
                }
                return wwbc.getParent().getBeanManagerImpl().getInjectionResolver().implResolveByType(delegate, injectionPointType, injectionPointClass, qualifiers);
            }
        }
        return set;
    }

    private String getBDABeansXMLPath(Class<?> injectionPointBeanClass) {
        if (injectionPointBeanClass == null) {
            return null;
        }

        ScannerService scannerService = context.getScannerService();
        BDABeansXmlScanner beansXMLScanner = scannerService.getBDABeansXmlScanner();
        return beansXMLScanner.getBeansXml(injectionPointBeanClass);
    }

    private AnnotatedType<? extends Annotation> findQualifierModel(final Class<?> qualifier) {
        return context.getBeanManagerImpl().getAdditionalAnnotatedTypeQualifiers().get(qualifier);
    }

    @Override
    public void setStartup(boolean startup) {
        this.startup = startup;
        super.setStartup(startup);
    }

    @Override
    public void clearCaches() {
        super.clearCaches();
        this.resolutionFailures.clear();
    }

    public int getCacheSize() {
        return this.resolutionFailures.size();
    }
}
