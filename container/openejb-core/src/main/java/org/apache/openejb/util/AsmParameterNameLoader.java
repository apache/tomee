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
package org.apache.openejb.util;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.xbean.asm.ClassReader;
import org.apache.xbean.asm.Label;
import org.apache.xbean.asm.MethodVisitor;
import org.apache.xbean.asm.Type;
import org.apache.xbean.asm.commons.EmptyVisitor;
import org.apache.xbean.recipe.ParameterNameLoader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.security.AccessController;
import java.security.PrivilegedAction;


/**
 * Implementation of ParameterNameLoader that uses ASM to read the parameter names from the local variable table in the
 * class byte code.
 * <p/>
 * This wonderful piece of code was taken from org.springframework.core.LocalVariableTableParameterNameDiscover
 */
public class AsmParameterNameLoader implements ParameterNameLoader {

    public static void install(){
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                try {
                    Field field = org.apache.xbean.recipe.ReflectionUtil.class.getDeclaredField("parameterNamesLoader");
                    field.setAccessible(true);
                    field.set(null, new AsmParameterNameLoader());
//                    if (field.get(null) == null){
//                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }

    /**
     * Weak map from Constructor to List&lt;String&gt;.
     */
    private final WeakHashMap<Constructor, List<String>> constructorCache = new WeakHashMap<Constructor, List<String>>();

    /**
     * Weak map from Method to List&lt;String&gt;.
     */
    private final WeakHashMap<Method, List<String>> methodCache = new WeakHashMap<Method, List<String>>();

    /**
     * Gets the parameter names of the specified method or null if the class was compiled without debug symbols on.
     *
     * @param method the method for which the parameter names should be retrieved
     * @return the parameter names or null if the class was compilesd without debug symbols on
     */
    public List<String> get(Method method) {
        // check the cache
        if (methodCache.containsKey(method)) {
            return methodCache.get(method);
        }

        Map<Method, List<String>> allMethodParameters = getAllMethodParameters(method.getDeclaringClass(), method.getName());
        return allMethodParameters.get(method);
    }

    /**
     * Gets the parameter names of the specified constructor or null if the class was compiled without debug symbols on.
     *
     * @param constructor the constructor for which the parameters should be retrieved
     * @return the parameter names or null if the class was compiled without debug symbols on
     */
    public List<String> get(Constructor constructor) {
        // check the cache
        if (constructorCache.containsKey(constructor)) {
            return constructorCache.get(constructor);
        }

        Map<Constructor, List<String>> allConstructorParameters = getAllConstructorParameters(constructor.getDeclaringClass());
        return allConstructorParameters.get(constructor);
    }

    /**
     * Gets the parameter names of all constructoror null if the class was compiled without debug symbols on.
     *
     * @param clazz the class for which the constructor parameter names should be retrieved
     * @return a map from Constructor object to the parameter names or null if the class was compiled without debug symbols on
     */
    public Map<Constructor, List<String>> getAllConstructorParameters(Class clazz) {
        // Determine the constructors?
        List<Constructor> constructors = new ArrayList<Constructor>(Arrays.asList(clazz.getConstructors()));
        constructors.addAll(Arrays.asList(clazz.getDeclaredConstructors()));
        if (constructors.isEmpty()) {
            return Collections.emptyMap();
        }

        // Check the cache
        if (constructorCache.containsKey(constructors.get(0))) {
            Map<Constructor, List<String>> constructorParameters = new HashMap<Constructor, List<String>>();
            for (Constructor constructor : constructors) {
                constructorParameters.put(constructor, constructorCache.get(constructor));
            }
            return constructorParameters;
        }

        // Load the parameter names using ASM
        Map<Constructor, List<String>> constructorParameters = new HashMap<Constructor, List<String>>();
        try {
            ClassReader reader = AsmParameterNameLoader.createClassReader(clazz);

            AsmParameterNameLoader.AllParameterNamesDiscoveringVisitor visitor = new AsmParameterNameLoader.AllParameterNamesDiscoveringVisitor(clazz);
            reader.accept(visitor, 0);

            Map exceptions = visitor.getExceptions();
            if (exceptions.size() == 1) {
                throw new OpenEJBRuntimeException((Exception) exceptions.values().iterator().next());
            }
            if (!exceptions.isEmpty()) {
                throw new OpenEJBRuntimeException(exceptions.toString());
            }

            constructorParameters = visitor.getConstructorParameters();
        } catch (IOException ex) {
        }

        // Cache the names
        for (Constructor constructor : constructors) {
            constructorCache.put(constructor, constructorParameters.get(constructor));
        }
        return constructorParameters;
    }

    /**
     * Gets the parameter names of all methods with the specified name or null if the class was compiled without debug symbols on.
     *
     * @param clazz      the class for which the method parameter names should be retrieved
     * @param methodName the of the method for which the parameters should be retrieved
     * @return a map from Method object to the parameter names or null if the class was compiled without debug symbols on
     */
    public Map<Method, List<String>> getAllMethodParameters(Class clazz, String methodName) {
        // Determine the constructors?
        Method[] methods = getMethods(clazz, methodName);
        if (methods.length == 0) {
            return Collections.emptyMap();
        }

        // Check the cache
        if (methodCache.containsKey(methods[0])) {
            Map<Method, List<String>> methodParameters = new HashMap<Method, List<String>>();
            for (Method method : methods) {
                methodParameters.put(method, methodCache.get(method));
            }
            return methodParameters;
        }

        // Load the parameter names using ASM
        Map<Method, List<String>>  methodParameters = new HashMap<Method, List<String>>();
        try {
            ClassReader reader = AsmParameterNameLoader.createClassReader(clazz);

            AsmParameterNameLoader.AllParameterNamesDiscoveringVisitor visitor = new AsmParameterNameLoader.AllParameterNamesDiscoveringVisitor(clazz, methodName);
            reader.accept(visitor, 0);

            Map exceptions = visitor.getExceptions();
            if (exceptions.size() == 1) {
                throw new OpenEJBRuntimeException((Exception) exceptions.values().iterator().next());
            }
            if (!exceptions.isEmpty()) {
                throw new OpenEJBRuntimeException(exceptions.toString());
            }

            methodParameters = visitor.getMethodParameters();
        } catch (IOException ex) {
        }

        // Cache the names
        for (Method method : methods) {
            methodCache.put(method, methodParameters.get(method));
        }
        return methodParameters;
    }

    private Method[] getMethods(Class clazz, String methodName) {
        List<Method> methods = new ArrayList<Method>(Arrays.asList(clazz.getMethods()));
        methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        List<Method> matchingMethod = new ArrayList<Method>(methods.size());
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                matchingMethod.add(method);
            }
        }
        return matchingMethod.toArray(new Method[matchingMethod.size()]);
    }

    private static ClassReader createClassReader(Class declaringClass) throws IOException {
        InputStream in = null;
        try {
            ClassLoader classLoader = declaringClass.getClassLoader();
            in = classLoader.getResourceAsStream(declaringClass.getName().replace('.', '/') + ".class");
            ClassReader reader = new ClassReader(in);
            return reader;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static class AllParameterNamesDiscoveringVisitor extends EmptyVisitor {
        private final Map<Constructor, List<String>> constructorParameters = new HashMap<Constructor, List<String>>();
        private final Map<Method, List<String>> methodParameters = new HashMap<Method, List<String>>();
        private final Map<String, Exception> exceptions = new HashMap<String, Exception>();
        private final String methodName;
        private final Map<String, Method> methodMap = new HashMap<String, Method>();
        private final Map<String, Constructor> constructorMap = new HashMap<String, Constructor>();

        public AllParameterNamesDiscoveringVisitor(Class type, String methodName) {
            this.methodName = methodName;

            List<Method> methods = new ArrayList<Method>(Arrays.asList(type.getMethods()));
            methods.addAll(Arrays.asList(type.getDeclaredMethods()));
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    methodMap.put(Type.getMethodDescriptor(method), method);
                }
            }
        }

        public AllParameterNamesDiscoveringVisitor(Class type) {
            this.methodName = "<init>";

            List<Constructor> constructors = new ArrayList<Constructor>(Arrays.asList(type.getConstructors()));
            constructors.addAll(Arrays.asList(type.getDeclaredConstructors()));
            for (Constructor constructor : constructors) {
                Type[] types = new Type[constructor.getParameterTypes().length];
                for (int j = 0; j < types.length; j++) {
                    types[j] = Type.getType(constructor.getParameterTypes()[j]);
                }
                constructorMap.put(Type.getMethodDescriptor(Type.VOID_TYPE, types), constructor);
            }
        }

        public Map<Constructor, List<String>> getConstructorParameters() {
            return constructorParameters;
        }

        public Map<Method, List<String>> getMethodParameters() {
            return methodParameters;
        }

        public Map<String, Exception> getExceptions() {
            return exceptions;
        }

        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (!name.equals(this.methodName)) {
                return null;
            }

            try {
                final List<String> parameterNames;
                final boolean isStaticMethod;

                if (methodName.equals("<init>")) {
                    Constructor constructor = constructorMap.get(desc);
                    if (constructor == null) {
                        return null;
                    }
                    parameterNames = new ArrayList<String>(constructor.getParameterTypes().length);
                    parameterNames.addAll(Collections.<String>nCopies(constructor.getParameterTypes().length, null));
                    constructorParameters.put(constructor, parameterNames);
                    isStaticMethod = false;
                } else {
                    Method method = methodMap.get(desc);
                    if (method == null) {
                        return null;
                    }
                    parameterNames = new ArrayList<String>(method.getParameterTypes().length);
                    parameterNames.addAll(Collections.<String>nCopies(method.getParameterTypes().length, null));
                    methodParameters.put(method, parameterNames);
                    isStaticMethod = Modifier.isStatic(method.getModifiers());
                }

                return new EmptyVisitor() {
                    // assume static method until we get a first parameter name
                    public void visitLocalVariable(String name, String description, String signature, Label start, Label end, int index) {
                        if (isStaticMethod) {
                            if (index < parameterNames.size()) {
                                parameterNames.set(index, name);
                            }
                        } else if (index > 0) {
                            // for non-static the 0th arg is "this" so we need to offset by -1
                            index--;
                            if (index < parameterNames.size()) {
                                parameterNames.set(index, name);
                            }
                        }
                    }
                };
            } catch (Exception e) {
                this.exceptions.put(signature, e);
            }
            return null;
        }
    }
}