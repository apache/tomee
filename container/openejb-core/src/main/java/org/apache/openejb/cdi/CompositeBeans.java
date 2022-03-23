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

import org.apache.openejb.jee.Beans;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlTransient;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@XmlAccessorType(XmlAccessType.FIELD)
public class CompositeBeans extends Beans {
    @XmlTransient
    private final Map<URL, String> discoveryByUrl = new HashMap<>();

    @XmlTransient
    private final Map<URL, Collection<String>> interceptorsByUrl = new HashMap<>();

    @XmlTransient
    private final Map<URL, Collection<String>> decoratorsByUrl = new HashMap<>();

    @XmlTransient
    private final Map<URL, Collection<String>> alternativesByUrl = new HashMap<>();

    @XmlTransient
    private final Map<URL, Collection<String>> alternativeStereotypesByUrl = new HashMap<>();

    public Map<URL, String> getDiscoveryByUrl() {
        return discoveryByUrl;
    }

    public void mergeClasses(final URL url, final Beans beans) {
        // just for jaxb tree
        getAlternativeClasses().addAll(beans.getAlternativeClasses());
        getAlternativeStereotypes().addAll(beans.getAlternativeStereotypes());
        getDecorators().addAll(beans.getDecorators());
        getInterceptors().addAll(beans.getInterceptors());

        // for runtime
        interceptorsByUrl.put(url, beans.getInterceptors());
        decoratorsByUrl.put(url, beans.getDecorators());
        alternativesByUrl.put(url, beans.getAlternativeClasses());
        alternativeStereotypesByUrl.put(url, beans.getAlternativeStereotypes());
    }

    public Map<URL, Collection<String>> getInterceptorsByUrl() {
        return interceptorsByUrl;
    }

    public Map<URL, Collection<String>> getDecoratorsByUrl() {
        return decoratorsByUrl;
    }

    public Map<URL, Collection<String>> getAlternativesByUrl() {
        return alternativesByUrl;
    }

    public Map<URL, Collection<String>> getAlternativeStereotypesByUrl() {
        return alternativeStereotypesByUrl;
    }
}
