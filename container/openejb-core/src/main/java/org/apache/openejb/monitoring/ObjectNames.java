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
package org.apache.openejb.monitoring;

import static java.lang.String.format;
import java.lang.annotation.Annotation;

/**
 * Generate JMX object names.
 */
public class ObjectNames {

    /**
     * Produce a standardized JMX object name.
     *
     * @param clazz
     * @return JMX object name of the form "[package_name]:name=[class_name]"
     */
    public static String generatedNameOf(Class<?> clazz) {
        return format("%s:name=%s",
                clazz.getPackage().getName(),
                clazz.getSimpleName());
    }

    /**
     * Produce a generated JMX object name.
     *
     * @param clazz
     * @param annotation
     * @return JMX object name of the form "[package_name]:type=[class_name],name=[ann_class_name]"
     */
    public static String generatedNameOf(Class<?> clazz, Annotation annotation) {
        return format("%s:type=%s,name=%s",
                clazz.getPackage().getName(),
                clazz.getSimpleName(),
                annotation.annotationType().getSimpleName());
    }

    /**
     * Produce a generated JMX object name.
     *
     * @param clazz
     * @param annotationClass
     * @return JMX object name of the form "[package_name]:type=[class_name],name=[ann_class_name]"
     */
    public static String generatedNameOf(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        return format("%s:type=%s,name=%s",
                clazz.getPackage().getName(),
                clazz.getSimpleName(),
                annotationClass.getSimpleName());
    }
}