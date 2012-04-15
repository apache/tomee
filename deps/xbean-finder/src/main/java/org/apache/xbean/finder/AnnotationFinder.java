/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.xbean.finder;

import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.util.Classes;
import org.apache.xbean.finder.util.SingleLinkedList;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.signature.SignatureVisitor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ClassFinder searches the classpath of the specified classloader for
 * packages, classes, constructors, methods, or fields with specific annotations.
 * <p/>
 * For security reasons ASM is used to find the annotations.  Classes are not
 * loaded unless they match the requirements of a called findAnnotated* method.
 * Once loaded, these classes are cached.
 *
 * @version $Rev$ $Date$
 */
public class AnnotationFinder implements IAnnotationFinder {

    private final Set<Class<? extends Annotation>> metaroots = new HashSet<Class<? extends Annotation>>();

    private final Map<String, List<Info>> annotated = new HashMap<String, List<Info>>();

    protected final Map<String, ClassInfo> classInfos = new HashMap<String, ClassInfo>();
    protected final Map<String, ClassInfo> originalInfos = new HashMap<String, ClassInfo>();
    private final List<String> classesNotLoaded = new ArrayList<String>();
    private final int ASM_FLAGS = ClassReader.SKIP_CODE + ClassReader.SKIP_DEBUG + ClassReader.SKIP_FRAMES;
    private final Archive archive;

    private AnnotationFinder(AnnotationFinder parent, Iterable<String> classNames) {
        this.archive = new SubArchive(classNames);
        this.metaroots.addAll(parent.metaroots);
        for (Class<? extends Annotation> metaroot : metaroots) {
            final ClassInfo info = parent.classInfos.get(metaroot.getName());
            if (info == null) continue;
            readClassDef(info);
        }
        for (String name : classNames) {
            final ClassInfo info = parent.classInfos.get(name);
            if (info == null) continue;
            readClassDef(info);
        }

        resolveAnnotations(parent, new ArrayList<String>());
        for (ClassInfo classInfo : classInfos.values()) {
            if (isMetaRoot(classInfo)) {
                try {
                    metaroots.add((Class<? extends Annotation>) classInfo.get());
                } catch (ClassNotFoundException e) {
                    classesNotLoaded.add(classInfo.getName());
                }
            }
        }

        for (Class<? extends Annotation> metaroot : metaroots) {
            List<Info> infoList = annotated.get(metaroot.getName());
            for (Info info : infoList) {
                final String className = info.getName() + "$$";
                final ClassInfo i = parent.classInfos.get(className);
                if (i == null) continue;
                readClassDef(i);
            }
        }
    }

    public AnnotationFinder(Archive archive) {
        this.archive = archive;

        for (Archive.Entry entry : archive) {
            final String className = entry.getName();
            try {
                readClassDef(entry.getBytecode());
            } catch (NoClassDefFoundError e) {
                throw new NoClassDefFoundError("Could not fully load class: " + className + "\n due to:" + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // keep track of what was originally from the archives
        originalInfos.putAll(classInfos);
    }

    public boolean hasMetaAnnotations() {
        return metaroots.size() > 0;
    }

    private void readClassDef(ClassInfo info) {
        classInfos.put(info.name, info);
        index(info);
        index(info.constructors);
        index(info.methods);
        index(info.fields);
    }

    private void resolveAnnotations(AnnotationFinder parent, List<String> scanned) {
        // Get a list of the annotations that exist before we start
        final List<String> annotations = new ArrayList<String>(annotated.keySet());

        for (String annotation : annotations) {
            if (scanned.contains(annotation)) continue;
            final ClassInfo info = parent.classInfos.get(annotation);
            if (info == null) continue;
            readClassDef(info);
        }

        // If the "annotated" list has grown, then we must scan those
        if (annotated.keySet().size() != annotations.size()) {
            resolveAnnotations(parent, annotations);
        }
    }


    private void index(List<? extends Info> infos) {
        for (Info i : infos) {
            index(i);
        }
    }

    private void index(Info i) {
        for (AnnotationInfo annotationInfo : i.getAnnotations()) {
            index(annotationInfo, i);
        }
    }

    public List<String> getAnnotatedClassNames() {
        return new ArrayList<String>(originalInfos.keySet());
    }

    public Archive getArchive() {
        return archive;
    }

    /**
     * The link() method must be called to successfully use the findSubclasses and findImplementations methods
     *
     * @return
     * @throws java.io.IOException
     */
    public AnnotationFinder link() {

        enableFindSubclasses();

        enableFindImplementations();

        enableMetaAnnotations();

        return this;
    }

    public AnnotationFinder enableMetaAnnotations() {
        // diff new and old lists
        resolveAnnotations(new ArrayList<String>());

        linkMetaAnnotations();

        return this;
    }

    public AnnotationFinder enableFindImplementations() {
        for (ClassInfo classInfo : classInfos.values().toArray(new ClassInfo[classInfos.size()])) {

            linkInterfaces(classInfo);

        }

        return this;
    }

    public AnnotationFinder enableFindSubclasses() {
        for (ClassInfo classInfo : classInfos.values().toArray(new ClassInfo[classInfos.size()])) {

            linkParent(classInfo);
        }

        return this;
    }

    /**
     * Used to support meta annotations
     * <p/>
     * Once the list of classes has been read from the Archive, we
     * iterate over all the annotations that are used by those classes
     * and recursively resolve any annotations those annotations use.
     *
     * @param scanned
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private void resolveAnnotations(List<String> scanned) {
        // Get a list of the annotations that exist before we start
        final List<String> annotations = new ArrayList<String>(annotated.keySet());

        for (String annotation : annotations) {
            if (scanned.contains(annotation)) continue;
            readClassDef(annotation);
        }

        // If the "annotated" list has grown, then we must scan those
        if (annotated.keySet().size() != annotations.size()) {
            resolveAnnotations(annotations);
        }


//        for (ClassInfo classInfo : classInfos.values()) {
//            for (AnnotationInfo annotationInfo : classInfo.getAnnotations()) {
//                for (AnnotationInfo info : annotationInfo.getAnnotations()) {
//                    final String annotation = info.getName();
//
//                    if (hasName(annotation, "Metaroot") && !scanned.contains(annotation)) {
//                        readClassDef(annotation);
//                    }
//                }
//            }
//        }
    }

    private void linkMetaAnnotations() {
        for (ClassInfo classInfo : classInfos.values()) {
            if (isMetaRoot(classInfo)) {
                try {
                    metaroots.add((Class<? extends Annotation>) classInfo.get());
                } catch (ClassNotFoundException e) {
                    classesNotLoaded.add(classInfo.getName());
                }
            }
        }

        for (Class<? extends Annotation> metaroot : metaroots) {
            List<Info> infoList = annotated.get(metaroot.getName());
            for (Info info : infoList) {
                readClassDef(info.getName() + "$$");
            }
        }
    }

    private boolean isMetaRoot(ClassInfo classInfo) {
        if (!classInfo.isAnnotation()) return false;

        if (classInfo.getName().equals("javax.annotation.Metatype")) return true;
        if (isSelfAnnotated(classInfo, "Metatype")) return true;
        if (isSelfAnnotated(classInfo, "Metaroot")) return false;

        for (AnnotationInfo annotationInfo : classInfo.getAnnotations()) {
            final ClassInfo annotation = classInfos.get(annotationInfo.getName());
            if (annotation == null) return false;
            if (annotation.getName().equals("javax.annotation.Metaroot")) return true;
            if (isSelfAnnotated(annotation, "Metaroot")) return true;
        }

        return false;
    }

    private boolean isSelfAnnotated(ClassInfo classInfo, String metatype) {
        if (!classInfo.isAnnotation()) return false;

        final String name = classInfo.getName();
        if (!hasName(name, metatype)) return false;

        for (AnnotationInfo info : classInfo.getAnnotations()) {
            if (info.getName().equals(name)) return true;
        }

        return true;
    }

    private boolean hasName(String className, String simpleName) {
        return className.equals(simpleName) || className.endsWith("." + simpleName) || className.endsWith("$" + simpleName);
    }

    private void linkParent(ClassInfo classInfo) {
        if (classInfo.superType == null) return;
        if (classInfo.superType.equals("java.lang.Object")) return;

        ClassInfo parentInfo = classInfo.superclassInfo;

        if (parentInfo == null) {

            parentInfo = classInfos.get(classInfo.superType);

            if (parentInfo == null) {

                if (classInfo.clazz != null) {
                    readClassDef(((Class<?>) classInfo.clazz).getSuperclass());
                } else {
                    readClassDef(classInfo.superType);
                }

                parentInfo = classInfos.get(classInfo.superType);

                if (parentInfo == null) return;

                linkParent(parentInfo);
            }

            classInfo.superclassInfo = parentInfo;
        }

        if (!parentInfo.subclassInfos.contains(classInfo)) {
            parentInfo.subclassInfos.add(classInfo);
        }
    }

    private void linkInterfaces(ClassInfo classInfo) {
        final List<ClassInfo> infos = new ArrayList<ClassInfo>();

        if (classInfo.clazz != null) {
            final Class<?>[] interfaces = classInfo.clazz.getInterfaces();

            for (Class<?> clazz : interfaces) {
                ClassInfo interfaceInfo = classInfos.get(clazz.getName());

                if (interfaceInfo == null) {
                    readClassDef(clazz);
                }

                interfaceInfo = classInfos.get(clazz.getName());

                if (interfaceInfo != null) {
                    infos.add(interfaceInfo);
                }
            }
        } else {
            for (String className : classInfo.interfaces) {
                ClassInfo interfaceInfo = classInfos.get(className);

                if (interfaceInfo == null) {
                    readClassDef(className);
                }

                interfaceInfo = classInfos.get(className);

                if (interfaceInfo != null) {
                    infos.add(interfaceInfo);
                }
            }
        }

        for (ClassInfo info : infos) {
            linkInterfaces(info);
        }
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
        List<Info> infos = annotated.get(annotation.getName());
        return infos != null && !infos.isEmpty();
    }

    /**
     * Returns a list of classes that could not be loaded in last invoked findAnnotated* method.
     * <p/>
     * The list will only contain entries of classes whose byte code matched the requirements
     * of last invoked find* method, but were unable to be loaded and included in the results.
     * <p/>
     * The list returned is unmodifiable.  Once obtained, the returned list will be a live view of the
     * results from the last findAnnotated* method call.
     * <p/>
     * This method is not thread safe.
     *
     * @return an unmodifiable live view of classes that could not be loaded in previous findAnnotated* call.
     */
    public List<String> getClassesNotLoaded() {
        return Collections.unmodifiableList(classesNotLoaded);
    }

    public List<Package> findAnnotatedPackages(Class<? extends Annotation> annotation) {
        classesNotLoaded.clear();
        List<Package> packages = new ArrayList<Package>();
        List<Info> infos = getAnnotationInfos(annotation.getName());
        for (Info info : infos) {
            if (info instanceof PackageInfo) {
                PackageInfo packageInfo = (PackageInfo) info;
                try {
                    Package pkg = packageInfo.get();
                    // double check via proper reflection
                    if (pkg.isAnnotationPresent(annotation)) {
                        packages.add(pkg);
                    }
                } catch (ClassNotFoundException e) {
                    classesNotLoaded.add(packageInfo.getName());
                }
            }
        }
        return packages;
    }

    public List<Class<?>> findAnnotatedClasses(Class<? extends Annotation> annotation) {
        classesNotLoaded.clear();
        List<Class<?>> classes = new ArrayList<Class<?>>();
        List<Info> infos = getAnnotationInfos(annotation.getName());
        for (Info info : infos) {
            if (info instanceof ClassInfo) {
                ClassInfo classInfo = (ClassInfo) info;
                try {
                    Class clazz = classInfo.get();
                    // double check via proper reflection
                    if (clazz.isAnnotationPresent(annotation)) {
                        classes.add(clazz);
                    }
                } catch (ClassNotFoundException e) {
                    classesNotLoaded.add(classInfo.getName());
                }
            }
        }
        return classes;
    }

    public List<Annotated<Class<?>>> findMetaAnnotatedClasses(Class<? extends Annotation> annotation) {
        classesNotLoaded.clear();
        Set<Class<?>> classes = findMetaAnnotatedClasses(annotation, new HashSet<Class<?>>());

        List<Annotated<Class<?>>> list = new ArrayList<Annotated<Class<?>>>();

        for (Class<?> clazz : classes) {
            if (Annotation.class.isAssignableFrom(clazz) && isMetaAnnotation((Class<? extends Annotation>) clazz)) continue;
            list.add(new MetaAnnotatedClass(clazz));
        }

        return list;
    }

    private static boolean isMetaAnnotation(Class<? extends Annotation> clazz) {
        for (Annotation annotation : clazz.getDeclaredAnnotations()) {
            if (isMetatypeAnnotation(annotation.annotationType())) return true;
        }

        return false;
    }

    private static boolean isMetatypeAnnotation(Class<? extends Annotation> type) {
        if (isSelfAnnotated(type, "Metatype")) return true;

        for (Annotation annotation : type.getAnnotations()) {
            if (isSelfAnnotated(annotation.annotationType(), "Metaroot")) return true;
        }

        return false;
    }

    private static boolean isSelfAnnotated(Class<? extends Annotation> type, String name) {
        return type.isAnnotationPresent(type) && type.getSimpleName().equals(name) && validTarget(type);
    }

    private static boolean validTarget(Class<? extends Annotation> type) {
        final Target target = type.getAnnotation(Target.class);

        if (target == null) return false;

        final ElementType[] targets = target.value();

        return targets.length == 1 && targets[0] == ElementType.ANNOTATION_TYPE;
    }


    private Set<Class<?>> findMetaAnnotatedClasses(Class<? extends Annotation> annotation, Set<Class<?>> classes) {
        List<Info> infos = getAnnotationInfos(annotation.getName());
        for (Info info : infos) {
            if (info instanceof ClassInfo) {
                ClassInfo classInfo = (ClassInfo) info;
                try {
                    Class clazz = classInfo.get();

                    if (classes.contains(clazz)) continue;

                    // double check via proper reflection
                    if (clazz.isAnnotationPresent(annotation)) {
                        classes.add(clazz);
                    }

                    String meta = info.getMetaAnnotationName();
                    if (meta != null) {
                        classes.addAll(findMetaAnnotatedClasses((Class<? extends Annotation>) clazz, classes));
                    }
                } catch (ClassNotFoundException e) {
                    classesNotLoaded.add(classInfo.getName());
                }
            }
        }
        return classes;
    }

    /**
     * Naive implementation - works extremelly slow O(n^3)
     *
     * @param annotation
     * @return list of directly or indirectly (inherited) annotated classes
     */
    public List<Class<?>> findInheritedAnnotatedClasses(Class<? extends Annotation> annotation) {
        classesNotLoaded.clear();
        List<Class<?>> classes = new ArrayList<Class<?>>();
        List<Info> infos = getAnnotationInfos(annotation.getName());
        for (Info info : infos) {
            try {
                if (info instanceof ClassInfo) {
                    classes.add(((ClassInfo) info).get());
                }
            } catch (ClassNotFoundException cnfe) {
                // TODO: ignored, but a log message would be appropriate
            }
        }
        boolean annClassFound;
        List<ClassInfo> tempClassInfos = new ArrayList<ClassInfo>(classInfos.values());
        do {
            annClassFound = false;
            for (int pos = 0; pos < tempClassInfos.size(); pos++) {
                ClassInfo classInfo = tempClassInfos.get(pos);
                try {
                    // check whether any superclass is annotated
                    String superType = classInfo.getSuperType();
                    for (Class clazz : classes) {
                        if (superType.equals(clazz.getName())) {
                            classes.add(classInfo.get());
                            tempClassInfos.remove(pos);
                            annClassFound = true;
                            break;
                        }
                    }
                    // check whether any interface is annotated
                    List<String> interfces = classInfo.getInterfaces();
                    for (String interfce : interfces) {
                        for (Class clazz : classes) {
                            if (interfce.replaceFirst("<.*>", "").equals(clazz.getName())) {
                                classes.add(classInfo.get());
                                tempClassInfos.remove(pos);
                                annClassFound = true;
                                break;
                            }
                        }
                    }
                } catch (ClassNotFoundException e) {
                    classesNotLoaded.add(classInfo.getName());
                } catch (NoClassDefFoundError e) {
                    classesNotLoaded.add(classInfo.getName());
                }
            }
        } while (annClassFound);
        return classes;
    }

    public List<Method> findAnnotatedMethods(Class<? extends Annotation> annotation) {
        classesNotLoaded.clear();
        List<ClassInfo> seen = new ArrayList<ClassInfo>();
        List<Method> methods = new ArrayList<Method>();
        List<Info> infos = getAnnotationInfos(annotation.getName());
        for (Info info : infos) {
            if (info instanceof MethodInfo && !info.getName().equals("<init>")) {
                MethodInfo methodInfo = (MethodInfo) info;
                ClassInfo classInfo = methodInfo.getDeclaringClass();

                if (seen.contains(classInfo)) continue;

                seen.add(classInfo);

                try {
                    Class clazz = classInfo.get();
                    for (Method method : clazz.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(annotation)) {
                            methods.add(method);
                        }
                    }
                } catch (ClassNotFoundException e) {
                    classesNotLoaded.add(classInfo.getName());
                }
            }
        }
        return methods;
    }

    public List<Annotated<Method>> findMetaAnnotatedMethods(Class<? extends Annotation> annotation) {
        classesNotLoaded.clear();

        Set<Method> methods = findMetaAnnotatedMethods(annotation, new HashSet<Method>(), new HashSet<String>());

        List<Annotated<Method>> targets = new ArrayList<Annotated<Method>>();

        for (Method method : methods) {
            targets.add(new MetaAnnotatedMethod(method));
        }

        return targets;
    }

    private Set<Method> findMetaAnnotatedMethods(Class<? extends Annotation> annotation, Set<Method> methods, Set<String> seen) {
        List<Info> infos = getAnnotationInfos(annotation.getName());

        for (Info info : infos) {

            String meta = info.getMetaAnnotationName();
            if (meta != null) {
                if (meta.equals(annotation.getName())) continue;
                if (!seen.add(meta)) continue;


                ClassInfo metaInfo = classInfos.get(meta);

                Class<?> clazz;
                try {
                    clazz = metaInfo.get();
                } catch (ClassNotFoundException e) {
                    classesNotLoaded.add(metaInfo.getName());
                    continue;
                }

                findMetaAnnotatedMethods((Class<? extends Annotation>) clazz, methods, seen);

            } else if (info instanceof MethodInfo && !((MethodInfo) info).isConstructor()) {

                MethodInfo methodInfo = (MethodInfo) info;

                ClassInfo classInfo = methodInfo.getDeclaringClass();

                try {
                    Class clazz = classInfo.get();
                    for (Method method : clazz.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(annotation)) {
                            methods.add(method);
                        }
                    }
                } catch (ClassNotFoundException e) {
                    classesNotLoaded.add(classInfo.getName());
                }
            }
        }

        return methods;
    }

    public List<Annotated<Field>> findMetaAnnotatedFields(Class<? extends Annotation> annotation) {
        classesNotLoaded.clear();

        Set<Field> fields = findMetaAnnotatedFields(annotation, new HashSet<Field>(), new HashSet<String>());

        List<Annotated<Field>> targets = new ArrayList<Annotated<Field>>();

        for (Field field : fields) {
            targets.add(new MetaAnnotatedField(field));
        }

        return targets;
    }

    private Set<Field> findMetaAnnotatedFields(Class<? extends Annotation> annotation, Set<Field> fields, Set<String> seen) {
        List<Info> infos = getAnnotationInfos(annotation.getName());

        for (Info info : infos) {

            String meta = info.getMetaAnnotationName();
            if (meta != null) {
                if (meta.equals(annotation.getName())) continue;
                if (!seen.add(meta)) continue;


                ClassInfo metaInfo = classInfos.get(meta);

                Class<?> clazz;
                try {
                    clazz = metaInfo.get();
                } catch (ClassNotFoundException e) {
                    classesNotLoaded.add(metaInfo.getName());
                    continue;
                }

                findMetaAnnotatedFields((Class<? extends Annotation>) clazz, fields, seen);

            } else if (info instanceof FieldInfo) {

                FieldInfo fieldInfo = (FieldInfo) info;

                ClassInfo classInfo = fieldInfo.getDeclaringClass();

                try {
                    Class clazz = classInfo.get();
                    for (Field field : clazz.getDeclaredFields()) {
                        if (field.isAnnotationPresent(annotation)) {
                            fields.add(field);
                        }
                    }
                } catch (ClassNotFoundException e) {
                    classesNotLoaded.add(classInfo.getName());
                }
            }
        }

        return fields;
    }

    public List<Constructor> findAnnotatedConstructors(Class<? extends Annotation> annotation) {
        classesNotLoaded.clear();
        List<ClassInfo> seen = new ArrayList<ClassInfo>();
        List<Constructor> constructors = new ArrayList<Constructor>();
        List<Info> infos = getAnnotationInfos(annotation.getName());
        for (Info info : infos) {
            if (info instanceof MethodInfo && info.getName().equals("<init>")) {
                MethodInfo methodInfo = (MethodInfo) info;
                ClassInfo classInfo = methodInfo.getDeclaringClass();

                if (seen.contains(classInfo)) continue;

                seen.add(classInfo);

                try {
                    Class clazz = classInfo.get();
                    for (Constructor constructor : clazz.getConstructors()) {
                        if (constructor.isAnnotationPresent(annotation)) {
                            constructors.add(constructor);
                        }
                    }
                } catch (ClassNotFoundException e) {
                    classesNotLoaded.add(classInfo.getName());
                }
            }
        }
        return constructors;
    }

    public List<Field> findAnnotatedFields(Class<? extends Annotation> annotation) {
        classesNotLoaded.clear();
        List<ClassInfo> seen = new ArrayList<ClassInfo>();
        List<Field> fields = new ArrayList<Field>();
        List<Info> infos = getAnnotationInfos(annotation.getName());
        for (Info info : infos) {
            if (info instanceof FieldInfo) {
                FieldInfo fieldInfo = (FieldInfo) info;
                ClassInfo classInfo = fieldInfo.getDeclaringClass();

                if (seen.contains(classInfo)) continue;

                seen.add(classInfo);

                try {
                    Class clazz = classInfo.get();
                    for (Field field : clazz.getDeclaredFields()) {
                        if (field.isAnnotationPresent(annotation)) {
                            fields.add(field);
                        }
                    }
                } catch (ClassNotFoundException e) {
                    classesNotLoaded.add(classInfo.getName());
                }
            }
        }
        return fields;
    }

    public List<Class<?>> findClassesInPackage(String packageName, boolean recursive) {
        classesNotLoaded.clear();
        List<Class<?>> classes = new ArrayList<Class<?>>();
        for (ClassInfo classInfo : classInfos.values()) {
            try {
                if (recursive && classInfo.getPackageName().startsWith(packageName)) {
                    classes.add(classInfo.get());
                } else if (classInfo.getPackageName().equals(packageName)) {
                    classes.add(classInfo.get());
                }
            } catch (ClassNotFoundException e) {
                classesNotLoaded.add(classInfo.getName());
            }
        }
        return classes;
    }

    public <T> List<Class<? extends T>> findSubclasses(Class<T> clazz) {
        if (clazz == null) throw new NullPointerException("class cannot be null");

        classesNotLoaded.clear();

        final ClassInfo classInfo = classInfos.get(clazz.getName());

        List<Class<? extends T>> found = new ArrayList<Class<? extends T>>();

        if (classInfo == null) return found;

        findSubclasses(classInfo, found, clazz);

        return found;
    }

    private <T> void findSubclasses(ClassInfo classInfo, List<Class<? extends T>> found, Class<T> clazz) {

        for (ClassInfo subclassInfo : classInfo.subclassInfos) {

            try {
                found.add(subclassInfo.get().asSubclass(clazz));
            } catch (ClassNotFoundException e) {
                classesNotLoaded.add(subclassInfo.getName());
            }

            findSubclasses(subclassInfo, found, clazz);
        }
    }

    private <T> List<Class<? extends T>> _findSubclasses(Class<T> clazz) {
        if (clazz == null) throw new NullPointerException("class cannot be null");

        List<Class<? extends T>> classes = new ArrayList<Class<? extends T>>();


        for (ClassInfo classInfo : classInfos.values()) {

            try {

                if (clazz.getName().equals(classInfo.superType)) {

                    if (clazz.isAssignableFrom(classInfo.get())) {

                        classes.add(classInfo.get().asSubclass(clazz));

                        classes.addAll(_findSubclasses(classInfo.get().asSubclass(clazz)));
                    }
                }

            } catch (ClassNotFoundException e) {
                classesNotLoaded.add(classInfo.getName());
            }

        }

        return classes;
    }

    public <T> List<Class<? extends T>> findImplementations(Class<T> clazz) {
        if (clazz == null) throw new NullPointerException("class cannot be null");
        if (!clazz.isInterface()) new IllegalArgumentException("class must be an interface");
        classesNotLoaded.clear();

        final String interfaceName = clazz.getName();

        // Collect all interfaces extending the main interface (recursively)
        // Collect all implementations of interfaces
        // i.e. all *directly* implementing classes
        List<ClassInfo> infos = collectImplementations(interfaceName);

        // Collect all subclasses of implementations
        List<Class<? extends T>> classes = new ArrayList<Class<? extends T>>();
        for (ClassInfo info : infos) {
            try {
                final Class<? extends T> impl = (Class<? extends T>) info.get();

                if (clazz.isAssignableFrom(impl)) {
                    classes.add(impl);

                    // Optimization: Don't need to call this method if parent class was already searched


                    classes.addAll(_findSubclasses(impl));
                }

            } catch (ClassNotFoundException e) {
                classesNotLoaded.add(info.getName());
            }
        }
        return classes;
    }

    private List<ClassInfo> collectImplementations(String interfaceName) {
        final List<ClassInfo> infos = new ArrayList<ClassInfo>();

        for (ClassInfo classInfo : classInfos.values()) {

            if (classInfo.interfaces.contains(interfaceName)) {

                infos.add(classInfo);

                try {

                    final Class clazz = classInfo.get();

                    if (clazz.isInterface() && !clazz.isAnnotation()) {

                        infos.addAll(collectImplementations(classInfo.name));

                    }

                } catch (ClassNotFoundException ignore) {
                    // we'll deal with this later
                }
            }
        }
        return infos;
    }

    protected List<Info> getAnnotationInfos(String name) {
        final List<Info> infos = annotated.get(name);
        if (infos != null) return infos;
        return Collections.EMPTY_LIST;
    }

    protected List<Info> initAnnotationInfos(String name) {
        List<Info> infos = annotated.get(name);
        if (infos == null) {
            infos = new SingleLinkedList<Info>();
            annotated.put(name, infos);
        }
        return infos;
    }

    protected void readClassDef(String className) {
        if (classInfos.containsKey(className)) return;
        try {
            readClassDef(archive.getBytecode(className));
        } catch (Exception e) {
            if (className.endsWith("$$")) return;
            classesNotLoaded.add(className);
        }
    }

    protected void readClassDef(InputStream in) throws IOException {
        try {
            ClassReader classReader = new ClassReader(in);
            classReader.accept(new InfoBuildingVisitor(), ASM_FLAGS);
        } finally {
            in.close();
        }
    }

    protected void readClassDef(Class clazz) {
        List<Info> infos = new ArrayList<Info>();

        Package aPackage = clazz.getPackage();
        if (aPackage != null) {
            final PackageInfo info = new PackageInfo(aPackage);
            for (AnnotationInfo annotation : info.getAnnotations()) {
                List<Info> annotationInfos = initAnnotationInfos(annotation.getName());
                if (!annotationInfos.contains(info)) {
                    annotationInfos.add(info);
                }
            }
        }

        ClassInfo classInfo = new ClassInfo(clazz);
        infos.add(classInfo);
        classInfos.put(clazz.getName(), classInfo);
        for (Method method : clazz.getDeclaredMethods()) {
            infos.add(new MethodInfo(classInfo, method));
        }

        for (Constructor constructor : clazz.getConstructors()) {
            infos.add(new MethodInfo(classInfo, constructor));
        }

        for (Field field : clazz.getDeclaredFields()) {
            infos.add(new FieldInfo(classInfo, field));
        }

        for (Info info : infos) {
            for (AnnotationInfo annotation : info.getAnnotations()) {
                List<Info> annotationInfos = initAnnotationInfos(annotation.getName());
                annotationInfos.add(info);
            }
        }
    }

    public AnnotationFinder select(Class<?>... clazz) {
        String[] names = new String[clazz.length];
        int i = 0;
        for (Class<?> name : clazz) {
            names[i++] = name.getName();
        }

        return new AnnotationFinder(this, Arrays.asList(names));
    }

    public AnnotationFinder select(String... clazz) {
        return new AnnotationFinder(this, Arrays.asList(clazz));
    }

    public AnnotationFinder select(Iterable<String> clazz) {
        return new AnnotationFinder(this, clazz);
    }

    public class SubArchive implements Archive {
        private List<Entry> classes = new ArrayList<Entry>();

        public SubArchive(String... classes) {
            for (String name : classes) {
                this.classes.add(new E(name));
            }
        }

        public SubArchive(Iterable<String> classes) {
            for (String name : classes) {
                this.classes.add(new E(name));
            }
        }

        public InputStream getBytecode(String className) throws IOException, ClassNotFoundException {
            return archive.getBytecode(className);
        }

        public Class<?> loadClass(String className) throws ClassNotFoundException {
            return archive.loadClass(className);
        }

        public Iterator<Entry> iterator() {
            return classes.iterator();
        }

        public class E implements Entry {
            private final String name;

            public E(String name) {
                this.name = name;
            }

            public String getName() {
                return name;
            }

            public InputStream getBytecode() throws IOException {
                return new ByteArrayInputStream(new byte[0]);
            }
        }
    }

    public class Annotatable {
        private final List<AnnotationInfo> annotations = new ArrayList<AnnotationInfo>();

        public Annotatable(AnnotatedElement element) {
            for (Annotation annotation : getAnnotations(element)) {
                annotations.add(new AnnotationInfo(annotation.annotationType().getName()));
            }
        }

        public Annotatable() {
        }

        public Annotation[] getDeclaredAnnotations() {
            return new Annotation[0];
        }

        public List<AnnotationInfo> getAnnotations() {
            return annotations;
        }

        public String getMetaAnnotationName() {
            return null;
        }

        /**
         * Utility method to get around some errors caused by
         * interactions between the Equinox class loaders and
         * the OpenJPA transformation process.  There is a window
         * where the OpenJPA transformation process can cause
         * an annotation being processed to get defined in a
         * classloader during the actual defineClass call for
         * that very class (e.g., recursively).  This results in
         * a LinkageError exception.  If we see one of these,
         * retry the request.  Since the annotation will be
         * defined on the second pass, this should succeed.  If
         * we get a second exception, then it's likely some
         * other problem.
         *
         * @param element The AnnotatedElement we need information for.
         * @return An array of the Annotations defined on the element.
         */
        private Annotation[] getAnnotations(AnnotatedElement element) {
            try {
                return element.getAnnotations();
            } catch (LinkageError e) {
                return element.getAnnotations();
            }
        }

    }

    public static interface Info {

        String getMetaAnnotationName();

        String getName();

        List<AnnotationInfo> getAnnotations();

        Annotation[] getDeclaredAnnotations();
    }

    public class PackageInfo extends Annotatable implements Info {
        private final String name;
        private final ClassInfo info;
        private final Package pkg;

        public PackageInfo(Package pkg) {
            super(pkg);
            this.pkg = pkg;
            this.name = pkg.getName();
            this.info = null;
        }

        public PackageInfo(String name) {
            info = new ClassInfo(name, null);
            this.name = name;
            this.pkg = null;
        }

        public String getName() {
            return name;
        }

        public Package get() throws ClassNotFoundException {
            return (pkg != null) ? pkg : info.get().getPackage();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PackageInfo that = (PackageInfo) o;

            if (name != null ? !name.equals(that.name) : that.name != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }
    }

    public class ClassInfo extends Annotatable implements Info {
        private String name;
        private final List<MethodInfo> methods = new SingleLinkedList<MethodInfo>();
        private final List<MethodInfo> constructors = new SingleLinkedList<MethodInfo>();
        private String superType;
        private ClassInfo superclassInfo;
        private final List<ClassInfo> subclassInfos = new SingleLinkedList<ClassInfo>();
        private final List<String> interfaces = new SingleLinkedList<String>();
        private final List<FieldInfo> fields = new SingleLinkedList<FieldInfo>();
        private Class<?> clazz;


        public ClassInfo(Class clazz) {
            super(clazz);
            this.clazz = clazz;
            this.name = clazz.getName();
            Class superclass = clazz.getSuperclass();
            this.superType = superclass != null ? superclass.getName() : null;
            for (Class intrface : clazz.getInterfaces()) {
                this.interfaces.add(intrface.getName());
            }
        }

        public ClassInfo(String name, String superType) {
            this.name = name;
            this.superType = superType;
        }

        @Override
        public String getMetaAnnotationName() {
            for (AnnotationInfo info : getAnnotations()) {
                for (Class<? extends Annotation> metaroot : metaroots) {
                    if (info.getName().equals(metaroot.getName())) return name;
                }
            }

            if (name.endsWith("$$")) {
                ClassInfo info = classInfos.get(name.substring(0, name.length() - 2));
                if (info != null) {
                    return info.getMetaAnnotationName();
                }
            }

            return null;
        }

        public String getPackageName() {
            return name.indexOf(".") > 0 ? name.substring(0, name.lastIndexOf(".")) : "";
        }

        public List<MethodInfo> getConstructors() {
            return constructors;
        }

        public List<String> getInterfaces() {
            return interfaces;
        }

        public List<FieldInfo> getFields() {
            return fields;
        }

        public List<MethodInfo> getMethods() {
            return methods;
        }

        public String getName() {
            return name;
        }

        public String getSuperType() {
            return superType;
        }

        public boolean isAnnotation() {
            return "java.lang.Object".equals(superType) && interfaces.size() == 1 && "java.lang.annotation.Annotation".equals(interfaces.get(0));
        }

        public Class<?> get() throws ClassNotFoundException {
            if (clazz != null) return clazz;
            try {
                String fixedName = name.replaceFirst("<.*>", "");
                this.clazz = archive.loadClass(fixedName);
                return clazz;
            } catch (ClassNotFoundException notFound) {
                classesNotLoaded.add(name);
                throw notFound;
            }
        }

        public String toString() {
            return name;
        }
    }

    public class MethodInfo extends Annotatable implements Info {
        private final ClassInfo declaringClass;
        private final String descriptor;
        private final String name;
        private final List<List<AnnotationInfo>> parameterAnnotations = new ArrayList<List<AnnotationInfo>>();
        private Member method;

        public MethodInfo(ClassInfo info, Constructor constructor) {
            super(constructor);
            this.declaringClass = info;
            this.name = "<init>";
            this.descriptor = null;
        }

        public MethodInfo(ClassInfo info, Method method) {
            super(method);
            this.declaringClass = info;
            this.name = method.getName();
            this.descriptor = method.getReturnType().getName();
            this.method = method;
        }

        public MethodInfo(ClassInfo declarignClass, String name, String descriptor) {
            this.declaringClass = declarignClass;
            this.name = name;
            this.descriptor = descriptor;
        }

        @Override
        public String getMetaAnnotationName() {
            return declaringClass.getMetaAnnotationName();
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            super.getDeclaredAnnotations();
            try {
                return ((AnnotatedElement) get()).getDeclaredAnnotations();
            } catch (ClassNotFoundException e) {
                return super.getDeclaredAnnotations();
            }
        }

        public boolean isConstructor() {
            return getName().equals("<init>");
        }

        public List<List<AnnotationInfo>> getParameterAnnotations() {
            return parameterAnnotations;
        }

        public List<AnnotationInfo> getParameterAnnotations(int index) {
            if (index >= parameterAnnotations.size()) {
                for (int i = parameterAnnotations.size(); i <= index; i++) {
                    List<AnnotationInfo> annotationInfos = new ArrayList<AnnotationInfo>();
                    parameterAnnotations.add(i, annotationInfos);
                }
            }
            return parameterAnnotations.get(index);
        }

        public String getName() {
            return name;
        }

        public ClassInfo getDeclaringClass() {
            return declaringClass;
        }

        public String toString() {
            return declaringClass + "@" + name;
        }

        public Member get() throws ClassNotFoundException {
            if (method == null) {
                method = toMethod();
            }

            return method;
        }

        private Method toMethod() throws ClassNotFoundException {
            org.objectweb.asm.commons.Method method = new org.objectweb.asm.commons.Method(name, descriptor);

            Class<?> clazz = this.declaringClass.get();
            List<Class> parameterTypes = new ArrayList<Class>();

            for (Type type : method.getArgumentTypes()) {
                String paramType = type.getClassName();
                try {
                    parameterTypes.add(Classes.forName(paramType, clazz.getClassLoader()));
                } catch (ClassNotFoundException cnfe) {
                    throw new IllegalStateException("Parameter class could not be loaded for type " + paramType, cnfe);
                }
            }

            Class[] parameters = parameterTypes.toArray(new Class[parameterTypes.size()]);

            IllegalStateException noSuchMethod = null;
            while (clazz != null) {
                try {
                    return clazz.getDeclaredMethod(name, parameters);
                } catch (NoSuchMethodException e) {
                    if (noSuchMethod == null) {
                        noSuchMethod = new IllegalStateException("Callback method does not exist: " + clazz.getName() + "." + name, e);
                    }
                    clazz = clazz.getSuperclass();
                }
            }

            throw noSuchMethod;
        }

    }

    public class FieldInfo extends Annotatable implements Info {
        private final String name;
        private final String type;
        private final ClassInfo declaringClass;
        private Field field;

        public FieldInfo(ClassInfo info, Field field) {
            super(field);
            this.declaringClass = info;
            this.name = field.getName();
            this.type = field.getType().getName();
            this.field = field;
        }

        public FieldInfo(ClassInfo declaringClass, String name, String type) {
            this.declaringClass = declaringClass;
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public ClassInfo getDeclaringClass() {
            return declaringClass;
        }

        public String getType() {
            return type;
        }

        public String toString() {
            return declaringClass + "#" + name;
        }

        @Override
        public String getMetaAnnotationName() {
            return declaringClass.getMetaAnnotationName();
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            super.getDeclaredAnnotations();
            try {
                return ((AnnotatedElement) get()).getDeclaredAnnotations();
            } catch (ClassNotFoundException e) {
                return super.getDeclaredAnnotations();
            }
        }

        public Member get() throws ClassNotFoundException {
            if (field == null) {
                field = toField();
            }

            return field;
        }

        private Field toField() throws ClassNotFoundException {

            Class<?> clazz = this.declaringClass.get();

            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                throw new IllegalStateException(name, e);
            }

        }
    }

    public class AnnotationInfo extends Annotatable implements Info {
        private final String name;

        public AnnotationInfo(Annotation annotation) {
            this(annotation.getClass().getName());
        }

        public AnnotationInfo(Class<? extends Annotation> annotation) {
            this.name = annotation.getName().intern();
        }

        public AnnotationInfo(String name) {
            name = Type.getType(name).getClassName();
            this.name = name.intern();
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return name;
        }
    }

    private void index(AnnotationInfo annotationInfo, Info info) {
        initAnnotationInfos(annotationInfo.getName()).add(info);
    }

    public class InfoBuildingVisitor extends EmptyVisitor {
        private Info info;

        public InfoBuildingVisitor() {
        }

        public InfoBuildingVisitor(Info info) {
            this.info = info;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            if (name.endsWith("package-info")) {
                info = new PackageInfo(javaName(name));
            } else {

                ClassInfo classInfo = new ClassInfo(javaName(name), javaName(superName));

//                if (signature == null) {
                    for (String interfce : interfaces) {
                        classInfo.getInterfaces().add(javaName(interfce));
                    }
//                } else {
//                    // the class uses generics
//                    new SignatureReader(signature).accept(new GenericAwareInfoBuildingVisitor(GenericAwareInfoBuildingVisitor.TYPE.CLASS, classInfo));
//                }
                info = classInfo;

                classInfos.put(classInfo.getName(), classInfo);
            }
        }

        private String javaName(String name) {
            return (name == null) ? null : name.replace('/', '.');
        }

        @Override
        public void visitInnerClass(String name, String outerName, String innerName, int access) {
            super.visitInnerClass(name, outerName, innerName, access);
        }

        @Override
        public void visitAttribute(Attribute attribute) {
            super.visitAttribute(attribute);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            AnnotationInfo annotationInfo = new AnnotationInfo(desc);
            info.getAnnotations().add(annotationInfo);
            index(annotationInfo, info);
            return new InfoBuildingVisitor(annotationInfo);
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            ClassInfo classInfo = ((ClassInfo) info);
            FieldInfo fieldInfo = new FieldInfo(classInfo, name, desc);
            classInfo.getFields().add(fieldInfo);
            return new InfoBuildingVisitor(fieldInfo);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            ClassInfo classInfo = ((ClassInfo) info);
            MethodInfo methodInfo = new MethodInfo(classInfo, name, desc);

            classInfo.getMethods().add(methodInfo);
            return new InfoBuildingVisitor(methodInfo);
        }


        @Override
        public AnnotationVisitor visitParameterAnnotation(int param, String desc, boolean visible) {
            MethodInfo methodInfo = ((MethodInfo) info);
            List<AnnotationInfo> annotationInfos = methodInfo.getParameterAnnotations(param);
            AnnotationInfo annotationInfo = new AnnotationInfo(desc);
            annotationInfos.add(annotationInfo);
            return new InfoBuildingVisitor(annotationInfo);
        }
    }

    public static class GenericAwareInfoBuildingVisitor implements SignatureVisitor {

        public enum TYPE {
            CLASS
        }

        public enum STATE {
            BEGIN, END, SUPERCLASS, INTERFACE, FORMAL_TYPE_PARAM
        }

        private Info info;
        private GenericAwareInfoBuildingVisitor.TYPE type;
        private GenericAwareInfoBuildingVisitor.STATE state;

        private static boolean debug = false;

        public GenericAwareInfoBuildingVisitor() {
        }

        public GenericAwareInfoBuildingVisitor(GenericAwareInfoBuildingVisitor.TYPE type, Info info) {
            this.type = type;
            this.info = info;
            this.state = GenericAwareInfoBuildingVisitor.STATE.BEGIN;
        }

        public void visitFormalTypeParameter(String s) {
            if (debug) System.out.println(" s=" + s);
            switch (state) {
                case BEGIN:
                    ((ClassInfo) info).name += "<" + s;
            }
            state = GenericAwareInfoBuildingVisitor.STATE.FORMAL_TYPE_PARAM;
        }

        public SignatureVisitor visitClassBound() {
            if (debug) System.out.println(" visitClassBound()");
            return this;
        }

        public SignatureVisitor visitInterfaceBound() {
            if (debug) System.out.println(" visitInterfaceBound()");
            return this;
        }

        public SignatureVisitor visitSuperclass() {
            if (debug) System.out.println(" visitSuperclass()");
            state = GenericAwareInfoBuildingVisitor.STATE.SUPERCLASS;
            return this;
        }

        public SignatureVisitor visitInterface() {
            if (debug) System.out.println(" visitInterface()");
            ((ClassInfo) info).getInterfaces().add("");
            state = GenericAwareInfoBuildingVisitor.STATE.INTERFACE;
            return this;
        }

        public SignatureVisitor visitParameterType() {
            if (debug) System.out.println(" visitParameterType()");
            return this;
        }

        public SignatureVisitor visitReturnType() {
            if (debug) System.out.println(" visitReturnType()");
            return this;
        }

        public SignatureVisitor visitExceptionType() {
            if (debug) System.out.println(" visitExceptionType()");
            return this;
        }

        public void visitBaseType(char c) {
            if (debug) System.out.println(" visitBaseType(" + c + ")");
        }

        public void visitTypeVariable(String s) {
            if (debug) System.out.println(" visitTypeVariable(" + s + ")");
        }

        public SignatureVisitor visitArrayType() {
            if (debug) System.out.println(" visitArrayType()");
            return this;
        }

        public void visitClassType(String s) {
            if (debug) System.out.println(" visitClassType(" + s + ")");
            switch (state) {
                case INTERFACE:
                    List<String> interfces = ((ClassInfo) info).getInterfaces();
                    int idx = interfces.size() - 1;
                    String interfce = interfces.get(idx);
                    if (interfce.length() == 0) {
                        interfce = javaName(s);
                    } else {
                        interfce += javaName(s);
                    }
                    interfces.set(idx, interfce);
                    break;
                case SUPERCLASS:
                    if (!s.equals("java/lang/Object")) {
                        ((ClassInfo) info).superType = javaName(s);
                    }
            }
        }

        public void visitInnerClassType(String s) {
            if (debug) System.out.println(" visitInnerClassType(" + s + ")");
        }

        public void visitTypeArgument() {
            if (debug) System.out.println(" visitTypeArgument()");
            switch (state) {
                case INTERFACE:
                    List<String> interfces = ((ClassInfo) info).getInterfaces();
                    int idx = interfces.size() - 1;
                    String interfce = interfces.get(idx);
                    interfce += "<";
                    interfces.set(idx, interfce);
            }
        }

        public SignatureVisitor visitTypeArgument(char c) {
            if (debug) System.out.println(" visitTypeArgument(" + c + ")");
            switch (state) {
                case INTERFACE:
                    List<String> interfces = ((ClassInfo) info).getInterfaces();
                    int idx = interfces.size() - 1;
                    String interfce = interfces.get(idx);
                    interfce += "<";
                    interfces.set(idx, interfce);
            }
            return this;
        }

        public void visitEnd() {
            if (debug) System.out.println(" visitEnd()");
            switch (state) {
                case INTERFACE:
                    List<String> interfces = ((ClassInfo) info).getInterfaces();
                    int idx = interfces.size() - 1;
                    String interfce = interfces.get(idx);
                    interfce += ">";
                    interfces.set(idx, interfce);
                    break;
                case FORMAL_TYPE_PARAM:
                    String name = ((ClassInfo) info).name;
                    if (name.contains("<")) {
                        ((ClassInfo) info).name += ">";
                    }
            }
            state = GenericAwareInfoBuildingVisitor.STATE.END;
        }

        private String javaName(String name) {
            return (name == null) ? null : name.replace('/', '.');
        }

    }

}