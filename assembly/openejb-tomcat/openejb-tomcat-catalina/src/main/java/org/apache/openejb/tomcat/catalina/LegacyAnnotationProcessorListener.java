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
package org.apache.openejb.tomcat.catalina;

import org.apache.openejb.tomcat.common.LegacyAnnotationProcessor;

/**
 * @version $Rev$ $Date$
 */
public class LegacyAnnotationProcessorListener {
    protected final LegacyAnnotationProcessor annotationProcessor;

    public LegacyAnnotationProcessorListener(LegacyAnnotationProcessor annotationProcessor) {
        this.annotationProcessor = annotationProcessor;
    }

    protected void postConstruct(Object object) {
        try {
            annotationProcessor.postConstruct(object);
        } catch (Exception e) {
            throw new IllegalStateException("PostConstruct Failed " + object.getClass(), e);
        }
    }

    protected void processAnnotations(Object object) {
        try {
            annotationProcessor.processAnnotations(object);
        } catch (Exception e) {
            throw new IllegalStateException("Dependency Injection Failed " + object.getClass(), e);
        }
    }

    protected void preDestroy(Object object) {
        try {
            annotationProcessor.preDestroy(object);
        } catch (Exception e) {
            throw new IllegalStateException("PreDestroy Failed " + object.getClass(), e);
        }
    }
}
