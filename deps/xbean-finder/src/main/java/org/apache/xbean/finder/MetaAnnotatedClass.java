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

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.URL;
import java.security.ProtectionDomain;

/**
* @version $Rev$ $Date$
*/
public class MetaAnnotatedClass<T> extends MetaAnnotatedElement<Class<T>> {

    public MetaAnnotatedClass(Class<T> clazz) {
        super(clazz, unroll(clazz));
    }

    public MetaAnnotatedClass<?> forName(String className) throws ClassNotFoundException {
        return to(target.forName(className));
    }

    private MetaAnnotatedClass<?> to(Class<?> clazz) {
        return new MetaAnnotatedClass(clazz);
    }

    public MetaAnnotatedClass<?> forName(String name, boolean initialize, ClassLoader loader) throws ClassNotFoundException {
        return to(target.forName(name, initialize, loader));
    }

    public T newInstance() throws InstantiationException, IllegalAccessException {
        return target.newInstance();
    }

    public boolean isInstance(Object obj) {
        return target.isInstance(obj);
    }

    public boolean isAssignableFrom(Class<?> cls) {
        return target.isAssignableFrom(cls);
    }

    public boolean isInterface() {
        return target.isInterface();
    }

    public boolean isArray() {
        return target.isArray();
    }

    public boolean isPrimitive() {
        return target.isPrimitive();
    }

    public boolean isAnnotation() {
        return target.isAnnotation();
    }

    public boolean isSynthetic() {
        return target.isSynthetic();
    }

    public String getName() {
        return target.getName();
    }

    public ClassLoader getClassLoader() {
        return target.getClassLoader();
    }

    public TypeVariable<Class<T>>[] getTypeParameters() {
        return target.getTypeParameters();
    }

    public MetaAnnotatedClass<? super T> getSuperclass() {
        return new MetaAnnotatedClass(target.getSuperclass());
    }

    public Type getGenericSuperclass() {
        return target.getGenericSuperclass();
    }

    public Package getPackage() {
        return target.getPackage();
    }

    public MetaAnnotatedClass<?>[] getInterfaces() {
        return to(target.getInterfaces());
    }

    public Type[] getGenericInterfaces() {
        return target.getGenericInterfaces();
    }

    public MetaAnnotatedClass<?> getComponentType() {
        return to(target.getComponentType());
    }

    public int getModifiers() {
        return target.getModifiers();
    }

    public Object[] getSigners() {
        return target.getSigners();
    }

    public MetaAnnotatedMethod getEnclosingMethod() {
        return to(target.getEnclosingMethod());
    }

    public MetaAnnotatedConstructor<?> getEnclosingConstructor() {
        return to(target.getEnclosingConstructor());
    }

    public MetaAnnotatedClass<?> getDeclaringClass() {
        return to(target.getDeclaringClass());
    }

    public MetaAnnotatedClass<?> getEnclosingClass() {
        return to(target.getEnclosingClass());
    }

    public String getSimpleName() {
        return target.getSimpleName();
    }

    public String getCanonicalName() {
        return target.getCanonicalName();
    }

    public boolean isAnonymousClass() {
        return target.isAnonymousClass();
    }

    public boolean isLocalClass() {
        return target.isLocalClass();
    }

    public boolean isMemberClass() {
        return target.isMemberClass();
    }

    public MetaAnnotatedClass<?>[] getClasses() {
        return to(target.getClasses());
    }

    public MetaAnnotatedField[] getFields() throws SecurityException {
        return to(target.getFields());
    }

    public MetaAnnotatedMethod[] getMethods() throws SecurityException {
        return to(target.getMethods());
    }

    public MetaAnnotatedConstructor<?>[] getConstructors() throws SecurityException {
        return to(target.getConstructors());
    }

    public MetaAnnotatedField getField(String name) throws NoSuchFieldException, SecurityException {
        return to(target.getField(name));
    }

    public MetaAnnotatedMethod getMethod(String name, Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
        return to(target.getMethod(name, parameterTypes));
    }

    public MetaAnnotatedConstructor<T> getConstructor(Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
        return new MetaAnnotatedConstructor(target.getConstructor(parameterTypes));
    }

    public MetaAnnotatedClass<?>[] getDeclaredClasses() throws SecurityException {
        return to(target.getDeclaredClasses());
    }

    public MetaAnnotatedField[] getDeclaredFields() throws SecurityException {
        return to(target.getDeclaredFields());
    }

    public MetaAnnotatedMethod[] getDeclaredMethods() throws SecurityException {
        return to(target.getDeclaredMethods());
    }

    public MetaAnnotatedConstructor<?>[] getDeclaredConstructors() throws SecurityException {
        return to(target.getDeclaredConstructors());
    }

    public MetaAnnotatedField getDeclaredField(String name) throws NoSuchFieldException, SecurityException {
        return to(target.getDeclaredField(name));
    }

    public MetaAnnotatedMethod getDeclaredMethod(String name, Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
        return to(target.getDeclaredMethod(name, parameterTypes));
    }

    public MetaAnnotatedConstructor<T> getDeclaredConstructor(Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
        return new MetaAnnotatedConstructor(target.getDeclaredConstructor(parameterTypes));
    }

    public InputStream getResourceAsStream(String name) {
        return target.getResourceAsStream(name);
    }

    public URL getResource(String name) {
        return target.getResource(name);
    }

    public ProtectionDomain getProtectionDomain() {
        return target.getProtectionDomain();
    }

    public boolean desiredAssertionStatus() {
        return target.desiredAssertionStatus();
    }

    public boolean isEnum() {
        return target.isEnum();
    }

    public T[] getEnumConstants() {
        return target.getEnumConstants();
    }

    public T cast(Object obj) {
        return target.cast(obj);
    }

    public <U> Class<? extends U> asSubclass(Class<U> clazz) {
        return target.asSubclass(clazz);
    }

    private MetaAnnotatedMethod[] to(Method[] a) {
        MetaAnnotatedMethod[] b = new MetaAnnotatedMethod[a.length];
        for (int i = 0; i < a.length; i++) {
            b[i] = new MetaAnnotatedMethod(a[i]);
        }
        return b;
    }

    private MetaAnnotatedMethod to(Method method) {
        return new MetaAnnotatedMethod(method);
    }

    private MetaAnnotatedConstructor<?>[] to(Constructor<?>[] a) {
        MetaAnnotatedConstructor<?>[] b = new MetaAnnotatedConstructor[a.length];
        for (int i = 0; i < a.length; i++) {
            b[i] = new MetaAnnotatedConstructor(a[i]);
        }
        return b;
    }

    private MetaAnnotatedConstructor<?> to(Constructor<?> constructor) {
        return new MetaAnnotatedConstructor(constructor);
    }

    private MetaAnnotatedClass<?>[] to(Class<?>[] a) {
        MetaAnnotatedClass<?>[] b = new MetaAnnotatedClass[a.length];
        for (int i = 0; i < a.length; i++) {
            b[i] = to(a[i]);
        }
        return b;
    }

    private MetaAnnotatedField[] to(Field[] a) {
        MetaAnnotatedField[] b = new MetaAnnotatedField[a.length];
        for (int i = 0; i < a.length; i++) {
            b[i] = new MetaAnnotatedField(a[i]);
        }
        return b;
    }

    private MetaAnnotatedField to(Field field) {
        return new MetaAnnotatedField(field);
    }

}