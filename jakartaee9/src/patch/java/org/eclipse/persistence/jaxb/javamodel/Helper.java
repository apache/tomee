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
package org.eclipse.persistence.jaxb.javamodel;

import static org.eclipse.persistence.jaxb.JAXBContextFactory.PKG_SEPARATOR;
import static org.eclipse.persistence.jaxb.compiler.XMLProcessor.DEFAULT;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.persistence.internal.core.helper.CoreClassConstants;

import jakarta.xml.bind.JAXBElement;

import org.eclipse.persistence.internal.oxm.Constants;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>To provide helper methods and constants to assist
 * in integrating TopLink JAXB 2.0 Generation with the JDEV JOT APIs.
 * <p><b>Responsibilities:</b>
 * <ul>
 * <li>Make available a map of JOT - XML type pairs</li>
 * <li>Redirect method calls to the current JavaModel implementation as
 * required</li>
 * <li>Provide methods for accessing generics, annotations, etc. on a
 * given implementaiton's classes</li>
 * <li>Provide a dynamic proxy instance for a given JavaAnnotation in
 * the JOT implementation (for reflection a Java SDK annotation is
 * returned)</li>
 * </ul>
 *
 * @since Oracle TopLink 11.1.1.0.0
 * @see JavaModel
 * @see AnnotationProxy
 *
 */
public class Helper {
    protected ClassLoader loader;
    protected JavaModel jModel;
    private HashMap xmlToJavaTypeMap;
    private boolean facets;

    public final static String APBYTE = "byte[]";
    public final static String BIGDECIMAL = "java.math.BigDecimal";
    public final static String BIGINTEGER = "java.math.BigInteger";
    public final static String PBOOLEAN = "boolean";
    public final static String PBYTE = "byte";
    public final static String CALENDAR = "java.util.Calendar";
    public final static String CHARACTER = "java.lang.Character";
    public final static String CHAR = "char";
    public final static String OBJECT = "java.lang.Object";
    public final static String CLASS = "java.lang.Class";
    public final static String PDOUBLE = "double";
    public final static String PFLOAT = "float";
    public final static String PINT = "int";
    public final static String PLONG = "long";
    public final static String PSHORT = "short";
    public final static String QNAME_CLASS = "javax.xml.namespace.QName";
    public final static String STRING = "java.lang.String";
    public final static String ABYTE = "java.lang.Byte[]";
    public final static String BOOLEAN = "java.lang.Boolean";
    public final static String BYTE = "java.lang.Byte";
    public final static String GREGORIAN_CALENDAR = "java.util.GregorianCalendar";
    public final static String DOUBLE = "java.lang.Double";
    public final static String FLOAT = "java.lang.Float";
    public final static String INTEGER = "java.lang.Integer";
    public final static String UUID = "java.util.UUID";
    public final static String LONG = "java.lang.Long";
    public final static String SHORT = "java.lang.Short";
    public final static String UTIL_DATE = "java.util.Date";
    public final static String SQL_DATE = "java.sql.Date";
    public final static String SQL_TIME = "java.sql.Time";
    public final static String SQL_TIMESTAMP = "java.sql.Timestamp";
    public final static String DURATION = "javax.xml.datatype.Duration";
    public final static String XMLGREGORIANCALENDAR = "javax.xml.datatype.XMLGregorianCalendar";
    public final static String URI = "java.net.URI";
    public final static String URL = "java.net.URL";
    protected final static String JAVA_PKG = "java.";
    protected final static String JAVAX_PKG = "javax.";
    protected final static String JAKARTA_PKG = "jakarta.";
    protected final static String JAVAX_WS_PKG = "javax.xml.ws.";
    protected final static String JAKARTA_WS_PKG = "jakarta.xml.ws.";
    protected final static String JAVAX_RPC_PKG = "javax.xml.rpc.";
    protected final static String JAKARTA_RPC_PKG = "jakarta.xml.rpc.";

    private JavaClass collectionClass;
    private JavaClass setClass;
    private JavaClass listClass;
    private JavaClass mapClass;
    private JavaClass jaxbElementClass;
    private JavaClass objectClass;

    /**
     * INTERNAL:
     * This is the preferred constructor.
     *
     * This constructor builds the map of XML-Java type pairs,
     * and sets the JavaModel and ClassLoader.
     *
     * @param model
     */
    public Helper(JavaModel model) {
        xmlToJavaTypeMap = buildXMLToJavaTypeMap();
        setJavaModel(model);
        setClassLoader(model.getClassLoader());
        collectionClass = getJavaClass(CoreClassConstants.Collection_Class);
        listClass = getJavaClass(CoreClassConstants.List_Class);
        setClass = getJavaClass(CoreClassConstants.Set_Class);
        mapClass = getJavaClass(CoreClassConstants.Map_Class);
        jaxbElementClass = getJavaClass(JAXBElement.class);
        objectClass = getJavaClass(CoreClassConstants.OBJECT);
    }

    /**
     * Builds a map of Java types to XML types.
     *
     * @return
     */
    private HashMap buildXMLToJavaTypeMap() {
        HashMap javaTypes = new HashMap();
        // jaxb 2.0 spec pairs
        javaTypes.put(APBYTE, Constants.BASE_64_BINARY_QNAME);
        javaTypes.put(BIGDECIMAL, Constants.DECIMAL_QNAME);
        javaTypes.put(BIGINTEGER, Constants.INTEGER_QNAME);
        javaTypes.put(PBOOLEAN, Constants.BOOLEAN_QNAME);
        javaTypes.put(PBYTE, Constants.BYTE_QNAME);
        javaTypes.put(CALENDAR, Constants.DATE_TIME_QNAME);
        javaTypes.put(PDOUBLE, Constants.DOUBLE_QNAME);
        javaTypes.put(PFLOAT, Constants.FLOAT_QNAME);
        javaTypes.put(PINT, Constants.INT_QNAME);
        javaTypes.put(PLONG, Constants.LONG_QNAME);
        javaTypes.put(PSHORT, Constants.SHORT_QNAME);
        javaTypes.put(QNAME_CLASS, Constants.QNAME_QNAME);
        javaTypes.put(STRING, Constants.STRING_QNAME);
        javaTypes.put(CHAR, Constants.STRING_QNAME);
        javaTypes.put(CHARACTER, Constants.STRING_QNAME);
        // other pairs
        javaTypes.put(ABYTE, Constants.BYTE_QNAME);
        javaTypes.put(BOOLEAN, Constants.BOOLEAN_QNAME);
        javaTypes.put(BYTE, Constants.BYTE_QNAME);
        javaTypes.put(CLASS, Constants.STRING_QNAME);
        javaTypes.put(GREGORIAN_CALENDAR, Constants.DATE_TIME_QNAME);
        javaTypes.put(DOUBLE, Constants.DOUBLE_QNAME);
        javaTypes.put(FLOAT, Constants.FLOAT_QNAME);
        javaTypes.put(INTEGER, Constants.INT_QNAME);
        javaTypes.put(LONG, Constants.LONG_QNAME);
        javaTypes.put(OBJECT, Constants.ANY_TYPE_QNAME);
        javaTypes.put(SHORT, Constants.SHORT_QNAME);
        javaTypes.put(UTIL_DATE, Constants.DATE_TIME_QNAME);
        javaTypes.put(SQL_DATE, Constants.DATE_QNAME);
        javaTypes.put(SQL_TIME, Constants.TIME_QNAME);
        javaTypes.put(SQL_TIMESTAMP, Constants.DATE_TIME_QNAME);
        javaTypes.put(DURATION, Constants.DURATION_QNAME);
        javaTypes.put(UUID, Constants.STRING_QNAME);
        javaTypes.put(URI, Constants.STRING_QNAME);
        javaTypes.put(URL, Constants.ANY_URI_QNAME);
        return javaTypes;
    }

    /**
     * Return a given method's generic return type as a JavaClass.
     *
     * @param meth
     * @return
     */
    public JavaClass getGenericReturnType(JavaMethod meth) {
        JavaClass result = meth.getReturnType();
        JavaClass jClass = null;
        if (result == null) { return null; }

        Collection args = result.getActualTypeArguments();
        if (args.size() >0) {
            jClass = (JavaClass) args.iterator().next();
        }
        return jClass;
    }

    /**
     * Return a JavaClass instance created based the provided class.
     * This assumes that the provided class exists on the classpath
     * - null is returned otherwise.
     *
     * @param javaClass
     * @return
     */
    public JavaClass getJavaClass(Class javaClass) {
        return jModel.getClass(javaClass);
    }

    /**
     * Return array of JavaClass instances created based on the provided classes.
     * This assumes provided classes exist on the classpath.
     *
     * @param classes
     * @return JavaClass array
     */
    public JavaClass[] getJavaClassArray(Class... classes) {
        if (0 == classes.length) {
            return new JavaClass[0];
        }
        JavaClass[] result = new JavaClass[classes.length];
        int i = 0;
        for (Class clazz : classes) {
            result[i++] = getJavaClass(clazz);
        }
        return result;
    }

    /**
     * Return a JavaClass instance created based on fully qualified
     * class name.  This assumes that a class with the provided name
     * exists on the classpath - null is returned otherwise.
     *
     * @param javaClassName
     * @return
     */
    public JavaClass getJavaClass(String javaClassName) {
        return jModel.getClass(javaClassName);
    }

    /**
     * Return a map of default Java types to XML types.
     * @return
     */
    public HashMap getXMLToJavaTypeMap() {
        return xmlToJavaTypeMap;
    }

    /**
     * Returns a either a dynamic proxy instance that allows an element
     * to be treated as an annotation (for JOT), or a Java annotation
     * (for Reflection), or null if the specified annotation does not
     * exist.
     * Intended to be used in conjunction with isAnnotationPresent.
     *
     * @param element
     * @param annotationClass
     * @return
     * @see #isAnnotationPresent
     */
    public Annotation getAnnotation(JavaHasAnnotations element, Class annotationClass) {
        JavaAnnotation janno = element.getAnnotation(jModel.getClass(annotationClass));
        if (janno == null) {
            return null;
        }
        return jModel.getAnnotation(janno, annotationClass);
    }

    /**
     * Returns a JavaClass instance wrapping the provided field's resolved
     * type.
     *
     * @param field
     * @return
     */
    public JavaClass getType(JavaField field) {
        JavaClass type = field.getResolvedType();
        try {
            return jModel.getClass(type.getRawName());
        } catch (Exception x) {}
        return null;
    }

    /**
     * Return a JavaClass instance based on the @see jakarta.xml.bind.JAXBElement .
     *
     * Replacement of direct access to JAXBELEMENT_CLASS field.
     *
     * @return
     */
    public JavaClass getJaxbElementClass() {
        return jaxbElementClass;
    }

    /**
     * Return a JavaClass instance based on the @see java.lang.Object .
     *
     * Replacement of direct access to OBJECT_CLASS field.
     *
     * @return
     */
    public JavaClass getObjectClass() {
        return objectClass;
    }

    /**
     * Indicates if element contains a given annotation.
     *
     * @param element
     * @param annotationClass
     * @return
     */
    public boolean isAnnotationPresent(JavaHasAnnotations element, Class annotationClass) {
        if (element == null || annotationClass == null) {
            return false;
        }
        return (element.getAnnotation(jModel.getClass(annotationClass)) != null);
    }

    /**
     * Indicates if a given JavaClass is a built-in Java type.
     *
     * A JavaClass is considered to be a built-in type if:
     * 1 - the XMLToJavaTypeMap map contains a key equal to the provided
     *     JavaClass' raw name
     * 2 - the provided JavaClass' raw name starts with "java."
     * 3 - the provided JavaClass' raw name starts with "javax.", with
     *     the exception of "jakarta.xml.ws." and "javax.xml.rpc"
     * @param jClass
     * @return
     */
    public boolean isBuiltInJavaType(JavaClass jClass) {
        String rawName = jClass.getRawName();
        if(null == rawName) {
            return true;
        }
        return (getXMLToJavaTypeMap().containsKey(rawName) || rawName.startsWith(JAVA_PKG) || ((rawName.startsWith(JAVAX_PKG) || rawName.startsWith(JAKARTA_PKG)) && !(
                rawName.startsWith(JAVAX_WS_PKG) ||
                rawName.startsWith(JAVAX_RPC_PKG) ||
                rawName.startsWith(JAKARTA_WS_PKG) ||
                rawName.startsWith(JAKARTA_RPC_PKG)
        )));
    }

    public void setClassLoader(ClassLoader loader) {
        this.loader = loader;
    }

    public void setJavaModel(JavaModel model) {
        jModel = model;
    }
    public ClassLoader getClassLoader() {
        return loader;
    }

    public Class getClassForJavaClass(JavaClass javaClass){
        String javaClassName = javaClass.getRawName();
        if (javaClass.isPrimitive() || javaClass.isArray() && javaClass.getComponentType().isPrimitive()){
            if (CoreClassConstants.APBYTE.getCanonicalName().equals(javaClassName)){
                return Byte[].class;
            }
            if (CoreClassConstants.PBYTE.getCanonicalName().equals(javaClassName)){
                return Byte.class;
            }
            if (CoreClassConstants.PBOOLEAN.getCanonicalName().equals(javaClassName)){
                return Boolean.class;
            }
            if (CoreClassConstants.PSHORT.getCanonicalName().equals(javaClassName)){
                return Short.class;
            }
            if (CoreClassConstants.PFLOAT.getCanonicalName().equals(javaClassName)){
                return Float.class;
            }
            if (CoreClassConstants.PCHAR.getCanonicalName().equals(javaClassName)){
                return Character.class;
            }
            if (CoreClassConstants.PDOUBLE.getCanonicalName().equals(javaClassName)){
                return Double.class;
            }
            if (CoreClassConstants.PINT.getCanonicalName().equals(javaClassName)){
                return Integer.class;
            }
            if (CoreClassConstants.PLONG.getCanonicalName().equals(javaClassName)){
                return Long.class;
            }
            return null;
        }
        return org.eclipse.persistence.internal.helper.Helper.getClassFromClasseName(javaClass.getQualifiedName(), loader);
    }

    /**
     * Convenience method to determine if a class exists in a given ArrayList.
     */
    public boolean classExistsInArray(JavaClass theClass, List<JavaClass> existingClasses) {
        for (JavaClass jClass : existingClasses) {
            if (areClassesEqual(jClass, theClass)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convenience method to determine if two JavaClass instances are equal.
     *
     * @param classA
     * @param classB
     * @return
     */
    private boolean areClassesEqual(JavaClass classA, JavaClass classB) {
        if (classA == classB) {
            return true;
        }
        if (!(classA.getQualifiedName().equals(classB.getQualifiedName()))) {
            return false;
        }

        Collection classAargs = classA.getActualTypeArguments();
        Collection classBargs = classB.getActualTypeArguments();
        if (classAargs != null) {
            if (classBargs == null) {
                return false;
            }
            if (classAargs.size() != classBargs.size()) {
                return false;
            }

            Iterator classAargsIter = classAargs.iterator();
            Iterator classBargsIter = classBargs.iterator();

            while(classAargsIter.hasNext()){
                JavaClass nestedClassA = (JavaClass) classAargsIter.next();
                JavaClass nestedClassB = (JavaClass) classBargsIter.next();
                if (!areClassesEqual(nestedClassA, nestedClassB)) {
                    return false;
                }
            }
            return true;
        }
        if (classBargs == null) {
            return true;
        }
        return false;
    }


    /**
     * Prepends a package name to a given java type name, if it is not already present.
     *
     * @param javaTypeName Java type name that may/may not contain 'packageName'
     * @param packageName package name to prepend to javaTypeName if not already
     * @return fully qualified java type name
     */
    public static String getQualifiedJavaTypeName(String javaTypeName, String packageName) {
        // prepend the package name if not already present
        if (packageName != null && packageName.length() > 0 && !packageName.equals(DEFAULT) && !javaTypeName.contains(PKG_SEPARATOR)) {
            return packageName + PKG_SEPARATOR + javaTypeName;
        }
        return javaTypeName;
    }

    public boolean isCollectionType(JavaClass type) {
     if (collectionClass.isAssignableFrom(type)
             || listClass.isAssignableFrom(type)
             || setClass.isAssignableFrom(type)) {
             return true;
         }
         return false;
    }

    public boolean isMapType(JavaClass type) {
        return mapClass.isAssignableFrom(type);
    }

    public boolean isFacets() {
        return facets;
    }

    public void setFacets(boolean facets) {
        this.facets = facets;
    }
}