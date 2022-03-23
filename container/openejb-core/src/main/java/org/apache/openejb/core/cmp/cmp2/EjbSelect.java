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

package org.apache.openejb.core.cmp.cmp2;

import org.apache.openejb.BeanContext;
import org.apache.openejb.Container;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.core.cmp.CmpContainer;

import jakarta.ejb.FinderException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * DO NOT REFACTOR THIS CLASS.  This class is referenced directly by generated code.
 *
 * The execute_xxxx() methods below are used for executing the meat of the generated
 * ejbSelectxxxx methods.  Primitive types convert and return the return type directly and the
 * generated method will give the correct return instruction.  Reference types are resolved using
 * the execute_Object() method, and the generated code is responsible for casting the return value
 * to the proper return type.
 */
public class EjbSelect {
    // our table of select methods for quick lookup 
    private static final HashMap<Class<?>, Method> selectMethods = new HashMap<Class<?>, Method>();

    static {
        try {
            selectMethods.put(Object.class, EjbSelect.class.getMethod("execute_Object", Object.class, String.class, String.class, Object[].class));
            selectMethods.put(Void.TYPE, EjbSelect.class.getMethod("execute_void", Object.class, String.class, Object[].class));
            selectMethods.put(Boolean.TYPE, EjbSelect.class.getMethod("execute_boolean", Object.class, String.class, Object[].class));
            selectMethods.put(Byte.TYPE, EjbSelect.class.getMethod("execute_byte", Object.class, String.class, Object[].class));
            selectMethods.put(Character.TYPE, EjbSelect.class.getMethod("execute_char", Object.class, String.class, Object[].class));
            selectMethods.put(Short.TYPE, EjbSelect.class.getMethod("execute_short", Object.class, String.class, Object[].class));
            selectMethods.put(Integer.TYPE, EjbSelect.class.getMethod("execute_int", Object.class, String.class, Object[].class));
            selectMethods.put(Long.TYPE, EjbSelect.class.getMethod("execute_long", Object.class, String.class, Object[].class));
            selectMethods.put(Float.TYPE, EjbSelect.class.getMethod("execute_float", Object.class, String.class, Object[].class));
            selectMethods.put(Double.TYPE, EjbSelect.class.getMethod("execute_double", Object.class, String.class, Object[].class));
        } catch (final NoSuchMethodException e) {
            throw new OpenEJBRuntimeException(e);
        }
    }


    /**
     * Retrieve the execution stub for the specified
     * return type.  If this is one of the primitive types,
     * the stub will directly return the primitive value.
     * All reference types get mapped to the generic Object
     * return type, so they'll need to be cast to the
     * appropriate class by the generated wrapper method.
     *
     * @param returnType The class of the return type.
     * @return The method to be used to process the method invocation.
     */
    public static Method getSelectMethod(final Class<?> returnType) {
        // perform a lookup on the return type.  If it is not found directly in the 
        // mapping table, this is some sort of reference type.  
        final Method method = selectMethods.get(returnType);
        if (method == null) {
            return selectMethods.get(Object.class);
        }

        return method;
    }


    /**
     * Perform a select operation when the return value is
     * a void.  This one is slightly different from the
     * rest, as the container operation performed is an
     * update() rather than a select() because there's
     * no value to return.
     *
     * @param obj             The ejb object we're executing on behalf of.
     * @param methodSignature The signature of the selectxxxx method being invoked.
     * @param args            The arguments to the select.  These need to match
     *                        the method signature.
     * @throws FinderException
     */
    public static void execute_void(final Object obj, final String methodSignature, final Object... args) throws FinderException {
        final BeanContext beanContext = (BeanContext) obj;
        final Container container = beanContext.getContainer();
        if (!(container instanceof CmpContainer)) {
            throw new FinderException("Deployment is not connected to a CmpContainer " + beanContext.getDeploymentID());
        }
        final CmpContainer cmpContainer = (CmpContainer) container;

        cmpContainer.update(beanContext, methodSignature, args);
    }


    /**
     * The single execution stub for all non-primitive
     * select operations.  This method has an additional
     * returnType parameter used to instantiate the return
     * value.
     *
     * @param obj             The EJB object we're operating against.
     * @param methodSignature The signature of the ejbSelectxxxx method.
     * @param returnType      The return type signature of the method.
     * @param args            The select arguments.
     * @return An object of the specified type...which might be
     * one of the collection types.
     * @throws FinderException
     */
    public static Object execute_Object(final Object obj, final String methodSignature, final String returnType, final Object... args) throws FinderException {
        final BeanContext beanContext = (BeanContext) obj;
        final Container container = beanContext.getContainer();
        if (!(container instanceof CmpContainer)) {
            throw new FinderException("Deployment is not connected to a CmpContainer " + beanContext.getDeploymentID());
        }
        final CmpContainer cmpContainer = (CmpContainer) container;

        return cmpContainer.select(beanContext, methodSignature, returnType, args);
    }


    public static char execute_char(final Object obj, final String methodSignature, final Object... args) throws FinderException {
        final BeanContext beanContext = (BeanContext) obj;
        final Container container = beanContext.getContainer();
        if (!(container instanceof CmpContainer)) {
            throw new FinderException("Deployment is not connected to a CmpContainer " + beanContext.getDeploymentID());
        }
        final CmpContainer cmpContainer = (CmpContainer) container;

        final Character result = (Character) cmpContainer.select(beanContext, methodSignature, "char", args);
        return result;
    }


    public static byte execute_byte(final Object obj, final String methodSignature, final Object... args) throws FinderException {
        final BeanContext beanContext = (BeanContext) obj;
        final Container container = beanContext.getContainer();
        if (!(container instanceof CmpContainer)) {
            throw new FinderException("Deployment is not connected to a CmpContainer " + beanContext.getDeploymentID());
        }
        final CmpContainer cmpContainer = (CmpContainer) container;

        final Number result = (Number) cmpContainer.select(beanContext, methodSignature, "byte", args);
        return result.byteValue();
    }


    public static boolean execute_boolean(final Object obj, final String methodSignature, final Object... args) throws FinderException {
        final BeanContext beanContext = (BeanContext) obj;
        final Container container = beanContext.getContainer();
        if (!(container instanceof CmpContainer)) {
            throw new FinderException("Deployment is not connected to a CmpContainer " + beanContext.getDeploymentID());
        }
        final CmpContainer cmpContainer = (CmpContainer) container;

        final Boolean result = (Boolean) cmpContainer.select(beanContext, methodSignature, "byte", args);
        return result;
    }


    public static short execute_short(final Object obj, final String methodSignature, final Object... args) throws FinderException {
        final BeanContext beanContext = (BeanContext) obj;
        final Container container = beanContext.getContainer();
        if (!(container instanceof CmpContainer)) {
            throw new FinderException("Deployment is not connected to a CmpContainer " + beanContext.getDeploymentID());
        }
        final CmpContainer cmpContainer = (CmpContainer) container;

        final Number result = (Number) cmpContainer.select(beanContext, methodSignature, "short", args);
        return result.shortValue();
    }


    public static int execute_int(final Object obj, final String methodSignature, final Object... args) throws FinderException {
        final BeanContext beanContext = (BeanContext) obj;
        final Container container = beanContext.getContainer();
        if (!(container instanceof CmpContainer)) {
            throw new FinderException("Deployment is not connected to a CmpContainer " + beanContext.getDeploymentID());
        }
        final CmpContainer cmpContainer = (CmpContainer) container;

        final Number result = (Number) cmpContainer.select(beanContext, methodSignature, "int", args);
        return result.intValue();
    }


    public static long execute_long(final Object obj, final String methodSignature, final Object... args) throws FinderException {
        final BeanContext beanContext = (BeanContext) obj;
        final Container container = beanContext.getContainer();
        if (!(container instanceof CmpContainer)) {
            throw new FinderException("Deployment is not connected to a CmpContainer " + beanContext.getDeploymentID());
        }
        final CmpContainer cmpContainer = (CmpContainer) container;

        final Number result = (Number) cmpContainer.select(beanContext, methodSignature, "long", args);
        return result.longValue();
    }


    public static float execute_float(final Object obj, final String methodSignature, final Object... args) throws FinderException {
        final BeanContext beanContext = (BeanContext) obj;
        final Container container = beanContext.getContainer();
        if (!(container instanceof CmpContainer)) {
            throw new FinderException("Deployment is not connected to a CmpContainer " + beanContext.getDeploymentID());
        }
        final CmpContainer cmpContainer = (CmpContainer) container;

        final Number result = (Number) cmpContainer.select(beanContext, methodSignature, "float", args);
        return result.floatValue();
    }


    public static double execute_double(final Object obj, final String methodSignature, final Object... args) throws FinderException {
        final BeanContext beanContext = (BeanContext) obj;
        final Container container = beanContext.getContainer();
        if (!(container instanceof CmpContainer)) {
            throw new FinderException("Deployment is not connected to a CmpContainer " + beanContext.getDeploymentID());
        }
        final CmpContainer cmpContainer = (CmpContainer) container;

        final Number result = (Number) cmpContainer.select(beanContext, methodSignature, "double", args);
        return result.doubleValue();
    }
}
