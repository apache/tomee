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
package org.apache.tomee.microprofile.openapi;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.ProcessBean;
import org.apache.openejb.config.NewLoaderLogic;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.tomee.microprofile.TomEEMicroProfileListener;

import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MPOpenAPICDIExtension implements Extension {

    private final Collection<AnnotatedType<?>> openapiClasses = new ArrayList<>();

    /**
     * The container must fire an event before it begins the bean discovery process.
     *
     * @param bbd {@code BeforeBeanDiscovery} CDI Event
     */
    @SuppressWarnings("UnusedDeclaration")
    private void beforeBeanDiscovery(@Observes final BeforeBeanDiscovery bbd) {
        System.out.println(bbd);
    }

    /**
     * The container must fire an event for each Java class or interface it
     * discovers in a bean archive, before it reads the declared annotations.
     *
     * @param pat {@code ProcessAnnotatedType} CDI Event
     * @param <T> A {@code Class}
     */
    @SuppressWarnings("UnusedDeclaration")
    private <T> void processAnnotatedType(@Observes final ProcessAnnotatedType<T> pat) {
        System.out.println("processAnnotatedType " + pat.getAnnotatedType().getJavaClass().getSimpleName());

        // instead of blindly add the discovered bean, let's try to reuse our NewLoaderLogic to filter out
        // classes from jars we don't need to scan for annotations

        try {
            final CodeSource src = pat.getAnnotatedType().getJavaClass().getProtectionDomain().getCodeSource();
            if (src != null && !NewLoaderLogic.skip(src.getLocation())) {
                openapiClasses.add(pat.getAnnotatedType());
            }
        } catch(final NoClassDefFoundError | IncompatibleClassChangeError e) {
            Logger.getInstance(LogCategory.MICROPROFILE, TomEEMicroProfileListener.class)
                  .warning("Can't load MicroProfile OpenAPI class " + pat.getAnnotatedType(), e);
            // ignored
        }
    }

    public Collection<Class> getClasses() {
        return openapiClasses.stream().map(AnnotatedType::getJavaClass).collect(Collectors.toSet());
    }
}