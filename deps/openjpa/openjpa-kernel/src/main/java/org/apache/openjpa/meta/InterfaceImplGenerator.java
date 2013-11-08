/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.meta;

import java.lang.reflect.Method;
import java.io.ByteArrayInputStream;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.enhance.PCEnhancer;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import serp.bytecode.BCClass;
import serp.bytecode.BCClassLoader;
import serp.bytecode.BCField;
import serp.bytecode.BCMethod;
import serp.bytecode.Code;
import serp.bytecode.Constants;
import serp.bytecode.Project;

/**
 * Creates implementations of managed interfaces.  Will throw exceptions
 * on unknown properties.
 *
 * @author Steve Kim
 */
class InterfaceImplGenerator {
    private static final Localizer _loc = Localizer.forPackage
        (InterfaceImplGenerator.class);
    private static final String POSTFIX = "openjpaimpl";

    private final MetaDataRepository _repos;
    private final Map<Class<?>,Class<?>> _impls = new WeakHashMap<Class<?>,Class<?>>();
    private final Project _project = new Project();
 
    // distinct project / loader for enhanced version of class
    private final Project _enhProject = new Project();

    /**
     * Constructor.  Supply repository.
     */
    public InterfaceImplGenerator(MetaDataRepository repos) {
        _repos = repos;
    }

    /**
     * Create a concrete implementation of the given type, possibly
     * returning a cached version of the class.
     */
    public synchronized Class<?> createImpl(ClassMetaData meta) {
        Class<?> iface = meta.getDescribedType();

        // check cache.
        Class<?> impl = _impls.get(iface);
        if (impl != null)
            return impl;

        ClassLoader parentLoader = AccessController.doPrivileged(
            J2DoPrivHelper.getClassLoaderAction(iface)); 
        BCClassLoader loader = AccessController
            .doPrivileged(J2DoPrivHelper.newBCClassLoaderAction(_project,
                parentLoader));
        BCClassLoader enhLoader = AccessController
            .doPrivileged(J2DoPrivHelper.newBCClassLoaderAction(_enhProject,
                parentLoader));
        BCClass bc = _project.loadClass(getClassName(meta));
        bc.declareInterface(iface);
        ClassMetaData sup = meta.getPCSuperclassMetaData();
        if (sup != null) {
            bc.setSuperclass(sup.getInterfaceImpl());
            enhLoader = AccessController
                .doPrivileged(J2DoPrivHelper.newBCClassLoaderAction(
                    _enhProject, AccessController
                        .doPrivileged(J2DoPrivHelper.getClassLoaderAction(sup
                            .getInterfaceImpl()))));
        }

        FieldMetaData[] fields = meta.getDeclaredFields();
        Set<Method> methods = new HashSet<Method>();
        for (int i = 0; i < fields.length; i++) 
            addField(bc, iface, fields[i], methods);
        invalidateNonBeanMethods(bc, iface, methods);

        // first load the base Class<?> as the enhancer requires the class
        // to be available
        try {
            meta.setInterfaceImpl(Class.forName(bc.getName(), true, loader));
        } catch (Throwable t) {
            throw new InternalException(_loc.get("interface-load", iface, 
                loader), t).setFatal(true);
        }
        // copy the BCClass<?> into the enhancer project.
        bc = _enhProject.loadClass(new ByteArrayInputStream(bc.toByteArray()), 
            loader);
        PCEnhancer enhancer = new PCEnhancer(_repos, bc, meta);

        int result = enhancer.run();
        if (result != PCEnhancer.ENHANCE_PC)
            throw new InternalException(_loc.get("interface-badenhance", 
                iface)).setFatal(true);
        try {
            // load the Class<?> for real.
            impl = Class.forName(bc.getName(), true, enhLoader);
        } catch (Throwable t) {
            throw new InternalException(_loc.get("interface-load2", iface, 
                enhLoader), t).setFatal(true);
        }
        // cache the generated impl.
        _impls.put(iface, impl);
        return impl;
    }

    /**
     * Add bean getters and setters, also recording seen methods
     * into the given set.
     */
    private void addField (BCClass bc, Class<?> iface, FieldMetaData fmd, 
        Set<Method> methods) {
        String name = fmd.getName();
        Class<?> type = fmd.getDeclaredType();
        BCField field = bc.declareField(name, type);
        field.setAccessFlags(Constants.ACCESS_PRIVATE);

        // getter
        name = StringUtils.capitalize(name);
        String prefix = isGetter(iface, fmd) ? "get" : "is";
        BCMethod meth = bc.declareMethod(prefix + name, type, null);
        meth.makePublic();
        Code code = meth.getCode(true);
        code.aload().setThis();
        code.getfield().setField(field);
        code.xreturn().setType(type);
        code.calculateMaxStack();
        code.calculateMaxLocals();
        methods.add(getMethodSafe(iface, meth.getName(), null));

        // setter
        meth = bc.declareMethod("set" + name, void.class, new Class[]{type});
        meth.makePublic();
        code = meth.getCode(true);
        code.aload().setThis();
        code.xload().setParam(0).setType(type);
        code.putfield().setField(field);
        code.vreturn();
        code.calculateMaxStack();
        code.calculateMaxLocals();
        methods.add(getMethodSafe(iface, meth.getName(), type));
    }

    /**
     * Invalidate methods on the interface which are not managed.
     */
    private void invalidateNonBeanMethods(BCClass bc, Class<?> iface, 
        Set<Method> methods) {
        Method[] meths = (Method[]) AccessController.doPrivileged(
            J2DoPrivHelper.getDeclaredMethodsAction(iface)); 
        BCMethod meth;
        Code code;
        Class<?> type = _repos.getMetaDataFactory().getDefaults().
            getUnimplementedExceptionType();
        for (int i = 0; i < meths.length; i++) {
            if (methods.contains(meths[i]))
                continue;
            meth = bc.declareMethod(meths[i].getName(), 
                meths[i].getReturnType(), meths[i].getParameterTypes());
            meth.makePublic();
            code = meth.getCode(true);
            code.anew().setType(type);
            code.dup();
            code.invokespecial().setMethod(type, "<init>", void.class, null);
            code.athrow();
            code.calculateMaxLocals();
            code.calculateMaxStack();
        }
    }

    /**
     * Return a unique Class<?> name.
     */
    protected final String getClassName(ClassMetaData meta) {
        Class<?> iface = meta.getDescribedType();
        return iface.getName() + "$" + System.identityHashCode(iface) + POSTFIX;
    }

    /**
     * Convenience method to return the given method / arg.
     */
    private static Method getMethodSafe(Class<?> iface, String name, Class<?> arg) {
        try {
            return AccessController.doPrivileged(
                J2DoPrivHelper.getDeclaredMethodAction(
                    iface, name, arg == null ? null : new Class[]{arg}));
        } catch (PrivilegedActionException pae) {
            throw new InternalException (_loc.get ("interface-mismatch", name));
        }
    }

    private static boolean isGetter(Class<?> iface, FieldMetaData fmd) {
        if (fmd.getType() != boolean.class && fmd.getType() != Boolean.class)
            return true;
        try {
            Method meth = AccessController.doPrivileged(
                J2DoPrivHelper.getDeclaredMethodAction(iface, "is" +
                    StringUtils.capitalize(fmd.getName()), (Class[]) null));
            return meth == null;
        } catch (PrivilegedActionException pae) {}
        return true;
    }

    boolean isImplType(Class<?> cls) {
        return (cls.getName().endsWith(POSTFIX)
            && cls.getName().indexOf('$') != -1);
    }

    public Class<?> toManagedInterface(Class<?> cls) {
        Class<?>[] ifaces = cls.getInterfaces();
        for (int i = 0; i < ifaces.length; i++) {
            if (_impls.get(ifaces[i]) == cls)
                return ifaces[i];
        }
        throw new IllegalArgumentException(cls.getName());
    }
}
