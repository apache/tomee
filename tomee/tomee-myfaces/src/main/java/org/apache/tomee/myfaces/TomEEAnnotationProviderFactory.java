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
package org.apache.tomee.myfaces;

import org.apache.myfaces.spi.AnnotationProvider;
import org.apache.myfaces.spi.AnnotationProviderFactory;
import org.apache.myfaces.spi.impl.DefaultAnnotationProviderFactory;

import jakarta.faces.context.ExternalContext;

public class TomEEAnnotationProviderFactory extends AnnotationProviderFactory {
    @Override
    public AnnotationProvider createAnnotationProvider(final ExternalContext externalContext) {
        AnnotationProvider annotationProvider = (AnnotationProvider) externalContext.getApplicationMap().get(DefaultAnnotationProviderFactory.ANNOTATION_PROVIDER_INSTANCE);
        if (annotationProvider == null) {
            annotationProvider = new TomEEAnnotationProvider();
            externalContext.getApplicationMap().put(DefaultAnnotationProviderFactory.ANNOTATION_PROVIDER_INSTANCE, annotationProvider);
        }
        return annotationProvider;
    }
}
