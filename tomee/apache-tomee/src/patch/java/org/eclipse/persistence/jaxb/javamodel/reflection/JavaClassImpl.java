/*
 * Copyright (c) 1998, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

// Contributors:
//     Oracle - initial API and implementation from Oracle TopLink
package org.eclipse.persistence.jaxb.javamodel.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.persistence.exceptions.JAXBException;
import org.eclipse.persistence.internal.security.PrivilegedAccessHelper;
import org.eclipse.persistence.jaxb.javamodel.JavaAnnotation;
import org.eclipse.persistence.jaxb.javamodel.JavaClass;
import org.eclipse.persistence.jaxb.javamodel.JavaClassInstanceOf;
import org.eclipse.persistence.jaxb.javamodel.JavaConstructor;
import org.eclipse.persistence.jaxb.javamodel.JavaField;
import org.eclipse.persistence.jaxb.javamodel.JavaMethod;
import org.eclipse.persistence.jaxb.javamodel.JavaPackage;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>A wrapper class for a JDK Class.  This implementation
 * of the EclipseLink JAXB 2.X Java model simply makes reflective calls on the
 * underlying JDK object.
 *
 * <p><b>Responsibilities:</b>
 * <ul>
 * <li>Provide access to the underlying JDK Class' name, package,
 * method/field names and parameters, annotations, etc.</li>
 * </ul>
 *
 * @since Oracle TopLink 11.1.1.0.0
 * @see org.eclipse.persistence.jaxb.javamodel.JavaClass
 * @see java.lang.Class
 */
public class JavaClassImpl implements JavaClass {

    protected ParameterizedType jType;
    protected Class jClass;
    protected JavaModelImpl javaModelImpl;
    protected boolean isMetadataComplete;
    protected JavaClass superClassOverride;

    protected static final String XML_REGISTRY_CLASS_NAME = "jakarta.xml.bind.annotation.XmlRegistry";

    public JavaClassImpl(Class javaClass, JavaModelImpl javaModelImpl) {
        this.jClass = javaClass;
        this.javaModelImpl = javaModelImpl;
        isMetadataComplete = false;
    }

    public JavaClassImpl(ParameterizedType javaType, Class javaClass, JavaModelImpl javaModelImpl) {
        this.jType = javaType;
        this.jClass = javaClass;
        this.javaModelImpl = javaModelImpl;
        isMetadataComplete = false;
    }

    public void setJavaModelImpl(JavaModelImpl javaModel) {
        this.javaModelImpl = javaModel;
    }
    public Collection getActualTypeArguments() {
        ArrayList<JavaClass> argCollection = new ArrayList<JavaClass>();
        if (jType != null) {
            Type[] params = jType.getActualTypeArguments();
            for (Type type : params) {
                if (type instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) type;
                    argCollection.add(new JavaClassImpl(pt, (Class) pt.getRawType(), javaModelImpl));
                } else if(type instanceof WildcardType){
                    Type[] upperTypes = ((WildcardType)type).getUpperBounds();
                    if(upperTypes.length >0){
                        Type upperType = upperTypes[0];
                        if(upperType instanceof Class){
                            argCollection.add(javaModelImpl.getClass((Class) upperType));
                        }
                    }
                } else if (type instanceof Class) {
                    argCollection.add(javaModelImpl.getClass((Class) type));
                } else if(type instanceof GenericArrayType) {
                    Class genericTypeClass = (Class)((GenericArrayType)type).getGenericComponentType();
                    genericTypeClass = java.lang.reflect.Array.newInstance(genericTypeClass, 0).getClass();
                    argCollection.add(javaModelImpl.getClass(genericTypeClass));
                } else if(type instanceof TypeVariable) {
                    Type[] boundTypes = ((TypeVariable) type).getBounds();
                    if(boundTypes.length > 0) {
                        Type boundType = boundTypes[0];
                        if(boundType instanceof Class) {
                            argCollection.add(javaModelImpl.getClass((Class) boundType));
                        }
                    }
                }
            }
        }
        return argCollection;
    }

    public String toString() {
        return getName();
    }

    /**
     * Assumes JavaType is a JavaClassImpl instance
     */
    public JavaAnnotation getAnnotation(JavaClass arg0) {
        // the only annotation we will return if isMetadataComplete == true is XmlRegistry
        if (arg0 != null && (!isMetadataComplete || arg0.getQualifiedName().equals(XML_REGISTRY_CLASS_NAME))) {
            Class annotationClass = ((JavaClassImpl) arg0).getJavaClass();
            if (javaModelImpl.getAnnotationHelper().isAnnotationPresent(getAnnotatedElement(), annotationClass)) {
                return new JavaAnnotationImpl(this.javaModelImpl.getAnnotationHelper().getAnnotation(getAnnotatedElement(), annotationClass));
            }
        }
        return null;
    }

    public Collection<JavaAnnotation> getAnnotations() {
        List<JavaAnnotation> annotationCollection = new ArrayList<JavaAnnotation>();
        if (!isMetadataComplete) {
            Annotation[] annotations = javaModelImpl.getAnnotationHelper().getAnnotations(getAnnotatedElement());
            for (Annotation annotation : annotations) {
                annotationCollection.add(new JavaAnnotationImpl(annotation));
            }
        }
        return annotationCollection;
    }

    public Collection<JavaClass> getDeclaredClasses() {
        List<JavaClass> classCollection = new ArrayList<JavaClass>();
        Class[] classes = jClass.getDeclaredClasses();
        for (Class javaClass : classes) {
            classCollection.add(javaModelImpl.getClass(javaClass));
        }
        return classCollection;
    }

    public JavaField getDeclaredField(String arg0) {
        try {
            return getJavaField(jClass.getDeclaredField(arg0));
        } catch (NoSuchFieldException nsfe) {
            return null;
        }
    }

    public Collection<JavaField> getDeclaredFields() {
        List<JavaField> fieldCollection = new ArrayList<JavaField>();
        Field[] fields = PrivilegedAccessHelper.getDeclaredFields(jClass);

        for (Field field : fields) {
            field.setAccessible(true);
            fieldCollection.add(getJavaField(field));
        }
        return fieldCollection;
    }

    /**
     * Assumes JavaType[] contains JavaClassImpl instances
     */
    public JavaMethod getDeclaredMethod(String arg0, JavaClass[] arg1) {
        if (arg1 == null) {
            arg1 = new JavaClass[0];
        }
        Class[] params = new Class[arg1.length];
        for (int i=0; i<arg1.length; i++) {
            JavaClass jType = arg1[i];
            if (jType != null) {
                params[i] = ((JavaClassImpl) jType).getJavaClass();
            }
        }
        try {
            return getJavaMethod(jClass.getDeclaredMethod(arg0, params));
        } catch (NoSuchMethodException nsme) {
            return null;
        }
    }

    public Collection getDeclaredMethods() {
        ArrayList<JavaMethod> methodCollection = new ArrayList<JavaMethod>();
        Method[] methods = jClass.getDeclaredMethods();
        for (Method method : methods) {
            methodCollection.add(getJavaMethod(method));
        }
        return methodCollection;
    }

    public JavaConstructor getConstructor(JavaClass[] paramTypes) {
        if (paramTypes == null) {
            paramTypes = new JavaClass[0];
        }
        Class[] params = new Class[paramTypes.length];
        for (int i=0; i<paramTypes.length; i++) {
            JavaClass jType = paramTypes[i];
            if (jType != null) {
                params[i] = ((JavaClassImpl) jType).getJavaClass();
            }
        }
        try {
            Constructor constructor = PrivilegedAccessHelper.getConstructorFor(jClass, params, true);
            return new JavaConstructorImpl(constructor, javaModelImpl);
        } catch (NoSuchMethodException nsme) {
            return null;
        }
    }

    public JavaConstructor getDeclaredConstructor(JavaClass[] paramTypes) {
        if (paramTypes == null) {
            paramTypes = new JavaClass[0];
        }
        Class[] params = new Class[paramTypes.length];
        for (int i=0; i<paramTypes.length; i++) {
            JavaClass jType = paramTypes[i];
            if (jType != null) {
                params[i] = ((JavaClassImpl) jType).getJavaClass();
            }
        }
        try {
            return new JavaConstructorImpl(PrivilegedAccessHelper.getDeclaredConstructorFor(this.jClass, params, true), javaModelImpl);
        } catch (NoSuchMethodException nsme) {
            return null;
        }
    }

    public Collection getConstructors() {
        Constructor[] constructors = this.jClass.getConstructors();
        ArrayList<JavaConstructor> constructorCollection = new ArrayList(constructors.length);
        for(Constructor next:constructors) {
            constructorCollection.add(new JavaConstructorImpl(next, javaModelImpl));
        }
        return constructorCollection;
    }

    public Collection getDeclaredConstructors() {
        Constructor[] constructors = this.jClass.getDeclaredConstructors();
        ArrayList<JavaConstructor> constructorCollection = new ArrayList(constructors.length);
        for(Constructor next:constructors) {
            constructorCollection.add(new JavaConstructorImpl(next, javaModelImpl));
        }
        return constructorCollection;
    }

    public JavaField getField(String arg0) {
        try {
            Field field = PrivilegedAccessHelper.getField(jClass, arg0, true);
            return getJavaField(field);
        } catch (NoSuchFieldException nsfe) {
            return null;
        }
    }

    public Collection getFields() {
        ArrayList<JavaField> fieldCollection = new ArrayList<JavaField>();
        Field[] fields = PrivilegedAccessHelper.getFields(jClass);
        for (Field field : fields) {
            fieldCollection.add(getJavaField(field));
        }
        return fieldCollection;
    }

    public Class getJavaClass() {
        return jClass;
    }

    /**
     * Assumes JavaType[] contains JavaClassImpl instances
     */
    public JavaMethod getMethod(String arg0, JavaClass[] arg1) {
        if (arg1 == null) {
            arg1 = new JavaClass[0];
        }
        Class[] params = new Class[arg1.length];
        for (int i=0; i<arg1.length; i++) {
            JavaClass jType = arg1[i];
            if (jType != null) {
                params[i] = ((JavaClassImpl) jType).getJavaClass();
            }
        }
        try {
            Method method = PrivilegedAccessHelper.getMethod(jClass, arg0, params, true);
            return getJavaMethod(method);
        } catch (NoSuchMethodException nsme) {
            return null;
        }
    }

    public Collection getMethods() {
        ArrayList<JavaMethod> methodCollection = new ArrayList<JavaMethod>();
        Method[] methods = PrivilegedAccessHelper.getMethods(jClass);
        for (Method method : methods) {
            methodCollection.add(getJavaMethod(method));
        }
        return methodCollection;
    }

    public String getName() {
        return jClass.getName();
    }

    public JavaPackage getPackage() {
        return new JavaPackageImpl(jClass.getPackage(), javaModelImpl, isMetadataComplete);
    }

    public String getPackageName() {
        if(jClass.getPackage() != null){
            return jClass.getPackage().getName();
        }else{
            Class nonInnerClass = jClass;
            Class enclosingClass = jClass.getEnclosingClass();
            while(enclosingClass != null){
                nonInnerClass = enclosingClass;
                enclosingClass = nonInnerClass.getEnclosingClass();
            }
            String className = nonInnerClass.getCanonicalName();
            if(className !=null){
                int index = className.lastIndexOf('.');
                if(index > -1){
                    return className.substring(0, index);
                }
            }
        }
        return null;
    }

    public String getQualifiedName() {
        return jClass.getName();
    }

    public String getRawName() {
        return jClass.getCanonicalName();
    }

    public JavaClass getSuperclass() {
        if(this.superClassOverride != null) {
            return this.superClassOverride;
        }
        if(jClass.isInterface()) {
            Class[] superInterfaces = jClass.getInterfaces();
            if(superInterfaces != null) {
                if(superInterfaces.length == 1) {
                    return javaModelImpl.getClass(superInterfaces[0]);
                } else {
                    Class parent = null;
                    for(Class next:superInterfaces) {
                        if(!(next.getName().startsWith("java.")
                                || next.getName().startsWith("javax.")
                                || next.getName().startsWith("jakarta."))) {
                            if(parent == null) {
                                parent = next;
                            } else {
                                throw JAXBException.invalidInterface(jClass.getName());
                            }
                        }
                    }
                    return javaModelImpl.getClass(parent);
                }
            }
        }
        return javaModelImpl.getClass(jClass.getSuperclass());
    }

    @Override
    public Type[] getGenericInterfaces() {
        return jClass.getGenericInterfaces();
    }

    public Type getGenericSuperclass() {
        return jClass.getGenericSuperclass();
    }

    public boolean hasActualTypeArguments() {
        return getActualTypeArguments().size() > 0;
    }

    public JavaField getJavaField(Field field) {
        return new JavaFieldImpl(field, javaModelImpl, isMetadataComplete);
    }

    public JavaMethod getJavaMethod(Method method) {
        return new JavaMethodImpl(method, javaModelImpl, isMetadataComplete);
    }

    public JavaClass getOwningClass() {
        return javaModelImpl.getClass(jClass.getEnclosingClass());
    }

    public boolean isAnnotation() {
        return jClass.isAnnotation();
    }

    public boolean isArray() {
        return jClass.isArray();
    }

    public AnnotatedElement getAnnotatedElement() {
        return jClass;
    }

    public boolean isAssignableFrom(JavaClass arg0) {
        if (!(arg0 instanceof JavaClassImpl)) {
            return false;
        }
        if(hasCustomSuperClass(arg0)) {
            return this.customIsAssignableFrom(arg0);
        }
        return jClass.isAssignableFrom(((JavaClassImpl) arg0).getJavaClass());
    }

    private boolean customIsAssignableFrom(JavaClass arg0) {
        JavaClassImpl jClass = (JavaClassImpl)arg0;
        Class cls = jClass.getJavaClass();

        if(cls == this.jClass) {
            return true;
        }
        Class[] interfaces = cls.getInterfaces();
        for(Class nextInterface:interfaces) {
            if(nextInterface == this.jClass) {
                return true;
            }
            if(customIsAssignableFrom(javaModelImpl.getClass(nextInterface))) {
                return true;
            }
        }

        if(!(jClass.isInterface())) {
            JavaClassImpl superJavaClass = (JavaClassImpl)jClass.getSuperclass();
            if(superJavaClass.getName().equals("java.lang.Object")) {
                return this.jClass == superJavaClass.getJavaClass();
            }
            return customIsAssignableFrom(superJavaClass);
        }
        return false;
    }

    private boolean hasCustomSuperClass(JavaClass arg0) {
        if(arg0 == null) {
            return false;
        }
        if(!this.javaModelImpl.hasXmlBindings()) {
            return false;
        }
        if(!(arg0.getClass() == this.getClass())) {
            return false;
        }
        if(arg0.getName().equals("java.lang.Object")) {
            return false;
        }
        JavaClassImpl jClass = (JavaClassImpl)arg0;
        return jClass.getSuperClassOverride() != null || hasCustomSuperClass(jClass.getSuperclass());
    }

    public boolean isEnum() {
        return jClass.isEnum();
    }

    public boolean isInterface() {
        return jClass.isInterface();
    }

    public boolean isMemberClass() {
        return jClass.isMemberClass();
    }

    public boolean isPrimitive() {
        return jClass.isPrimitive();
    }

    public boolean isAbstract() {
        return Modifier.isAbstract(getModifiers());
    }

    public boolean isPrivate() {
        return Modifier.isPrivate(getModifiers());
    }

    public boolean isProtected() {
        return Modifier.isProtected(getModifiers());
    }

    public boolean isPublic() {
        return Modifier.isPublic(getModifiers());
    }

    public boolean isStatic() {
        return Modifier.isStatic(getModifiers());
    }

    public int getModifiers() {
        return jClass.getModifiers();
    }

    public boolean isFinal() {
        return Modifier.isFinal(getModifiers());
    }

    public boolean isSynthetic() {
        return jClass.isSynthetic();
    }

    @Override
    public JavaClassInstanceOf instanceOf() {
        return JavaClassInstanceOf.JAVA_CLASS_IMPL;
    }

    public JavaClass getComponentType() {
        if(!isArray()) {
            return null;
        }
        return javaModelImpl.getClass(this.jClass.getComponentType());
    }

    public JavaClass getSuperClassOverride() {
        return superClassOverride;
    }

    public void setSuperClassOverride(JavaClass superClassOverride) {
        this.superClassOverride = superClassOverride;
    }
    /**
     * Set the indicator for XML metadata complete - if true,
     * annotations will be ignored.
     *
     * @param isMetadataComplete
     */
    void setIsMetadataComplete(Boolean isMetadataComplete) {
       if(isMetadataComplete != null){
            this.isMetadataComplete = isMetadataComplete;
        }
    }

    public JavaAnnotation getDeclaredAnnotation(JavaClass arg0) {
        // the only annotation we will return if isMetadataComplete == true is XmlRegistry
        if (arg0 != null && (!isMetadataComplete || arg0.getQualifiedName().equals(XML_REGISTRY_CLASS_NAME))) {
            Class annotationClass = ((JavaClassImpl) arg0).getJavaClass();
            Annotation[] annotations = javaModelImpl.getAnnotationHelper().getDeclaredAnnotations(getAnnotatedElement());
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(annotationClass)) {
                    return new JavaAnnotationImpl(annotation);
                }
            }
        }
        return null;
    }

    public Collection getDeclaredAnnotations() {
        List<JavaAnnotation> annotationCollection = new ArrayList<JavaAnnotation>();
        if (!isMetadataComplete) {
            Annotation[] annotations = javaModelImpl.getAnnotationHelper().getDeclaredAnnotations(getAnnotatedElement());
            for (Annotation annotation : annotations) {
                annotationCollection.add(new JavaAnnotationImpl(annotation));
            }
        }
        return annotationCollection;
    }

}