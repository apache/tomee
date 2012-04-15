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
package org.apache.xbean.finder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Temporary interface to bridge the gap between the two finder impls
 * @version $Rev$ $Date$
 */
public interface IAnnotationFinder {
    boolean isAnnotationPresent(Class<? extends Annotation> annotation);

    List<String> getClassesNotLoaded();

    List<Package> findAnnotatedPackages(Class<? extends Annotation> annotation);

    List<Class<?>> findAnnotatedClasses(Class<? extends Annotation> annotation);

    List<Class<?>> findInheritedAnnotatedClasses(Class<? extends Annotation> annotation);

    List<Method> findAnnotatedMethods(Class<? extends Annotation> annotation);

    List<Constructor> findAnnotatedConstructors(Class<? extends Annotation> annotation);

    List<Field> findAnnotatedFields(Class<? extends Annotation> annotation);

    List<Class<?>> findClassesInPackage(String packageName, boolean recursive);

    <T> List<Class<? extends T>> findSubclasses(Class<T> clazz);

    <T> List<Class<? extends T>> findImplementations(Class<T> clazz);

    List<Annotated<Method>> findMetaAnnotatedMethods(Class<? extends Annotation> annotation);

    List<Annotated<Field>> findMetaAnnotatedFields(Class<? extends Annotation> annotation);

    List<Annotated<Class<?>>> findMetaAnnotatedClasses(Class<? extends Annotation> annotation);

    List<String> getAnnotatedClassNames();
}
