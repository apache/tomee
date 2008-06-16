/**
 *
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
package org.apache.openejb.core.cmp.cmp2;

import java.lang.reflect.Method;
import java.util.HashMap; 
import java.util.Map; 

import javax.ejb.FinderException;

import org.apache.openejb.Container;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.core.cmp.CmpContainer;

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
    static private HashMap<Class<?>, Method> selectMethods = new HashMap<Class<?>, Method>(); 
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
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
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
     * 
     * @return The method to be used to process the method invocation. 
     */
    public static Method getSelectMethod(Class<?> returnType) 
    {
        // perform a lookup on the return type.  If it is not found directly in the 
        // mapping table, this is some sort of reference type.  
        Method method = selectMethods.get(returnType); 
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
     * @param di     The ejb object we're executing on behalf of.
     * @param methodSignature
     *               The signature of the selectxxxx method being invoked.
     * @param args   The arguments to the select.  These need to match
     *               the method signature.
     * 
     * @exception FinderException
     */
    public static void execute_void(Object di, String methodSignature, Object... args) throws FinderException {
        DeploymentInfo deploymentInfo = (DeploymentInfo) di;
        Container container = deploymentInfo.getContainer();
        if (!(container instanceof CmpContainer)) {
            throw new FinderException("Deployment is not connected to a CmpContainer " + deploymentInfo.getDeploymentID());
        }
        CmpContainer cmpContainer = (CmpContainer) container;
        
        cmpContainer.update(deploymentInfo, methodSignature, args);
    }
    
    
    /**
     * The single execution stub for all non-primitive 
     * select operations.  This method has an additional 
     * returnType parameter used to instantiate the return 
     * value. 
     * 
     * @param di         The EJB object we're operating against.
     * @param methodSignature
     *                   The signature of the ejbSelectxxxx method.
     * @param returnType The return type signature of the method.
     * @param args       The select arguments.
     * 
     * @return An object of the specified type...which might be 
     *         one of the collection types.
     * @exception FinderException
     */
    public static Object execute_Object(Object di, String methodSignature, String returnType, Object... args) throws FinderException {
        DeploymentInfo deploymentInfo = (DeploymentInfo) di;
        Container container = deploymentInfo.getContainer();
        if (!(container instanceof CmpContainer)) {
            throw new FinderException("Deployment is not connected to a CmpContainer " + deploymentInfo.getDeploymentID());
        }
        CmpContainer cmpContainer = (CmpContainer) container;
        
        return cmpContainer.select(deploymentInfo, methodSignature, returnType, args);
    }
    
    
    public static char execute_char(Object di, String methodSignature, Object... args) throws FinderException {
        DeploymentInfo deploymentInfo = (DeploymentInfo) di;
        Container container = deploymentInfo.getContainer();
        if (!(container instanceof CmpContainer)) {
            throw new FinderException("Deployment is not connected to a CmpContainer " + deploymentInfo.getDeploymentID());
        }
        CmpContainer cmpContainer = (CmpContainer) container;
        
        Character result = (Character)cmpContainer.select(deploymentInfo, methodSignature, "char", args);
        return result.charValue(); 
    }
    
    
    public static byte execute_byte(Object di, String methodSignature, Object... args) throws FinderException {
        DeploymentInfo deploymentInfo = (DeploymentInfo) di;
        Container container = deploymentInfo.getContainer();
        if (!(container instanceof CmpContainer)) {
            throw new FinderException("Deployment is not connected to a CmpContainer " + deploymentInfo.getDeploymentID());
        }
        CmpContainer cmpContainer = (CmpContainer) container;
        
        Number result = (Number)cmpContainer.select(deploymentInfo, methodSignature, "byte", args);
        return result.byteValue(); 
    }
    
    
    public static boolean execute_boolean(Object di, String methodSignature, Object... args) throws FinderException {
        DeploymentInfo deploymentInfo = (DeploymentInfo) di;
        Container container = deploymentInfo.getContainer();
        if (!(container instanceof CmpContainer)) {
            throw new FinderException("Deployment is not connected to a CmpContainer " + deploymentInfo.getDeploymentID());
        }
        CmpContainer cmpContainer = (CmpContainer) container;
        
        Boolean result = (Boolean)cmpContainer.select(deploymentInfo, methodSignature, "byte", args);
        return result.booleanValue(); 
    }
    
    
    public static short execute_short(Object di, String methodSignature, Object... args) throws FinderException {
        DeploymentInfo deploymentInfo = (DeploymentInfo) di;
        Container container = deploymentInfo.getContainer();
        if (!(container instanceof CmpContainer)) {
            throw new FinderException("Deployment is not connected to a CmpContainer " + deploymentInfo.getDeploymentID());
        }
        CmpContainer cmpContainer = (CmpContainer) container;
        
        Number result = (Number)cmpContainer.select(deploymentInfo, methodSignature, "short", args);
        return result.shortValue(); 
    }
    
    
    public static int execute_int(Object di, String methodSignature, Object... args) throws FinderException {
        DeploymentInfo deploymentInfo = (DeploymentInfo) di;
        Container container = deploymentInfo.getContainer();
        if (!(container instanceof CmpContainer)) {
            throw new FinderException("Deployment is not connected to a CmpContainer " + deploymentInfo.getDeploymentID());
        }
        CmpContainer cmpContainer = (CmpContainer) container;
        
        Number result = (Number)cmpContainer.select(deploymentInfo, methodSignature, "int", args);
        return result.intValue(); 
    }
    
    
    public static long execute_long(Object di, String methodSignature, Object... args) throws FinderException {
        DeploymentInfo deploymentInfo = (DeploymentInfo) di;
        Container container = deploymentInfo.getContainer();
        if (!(container instanceof CmpContainer)) {
            throw new FinderException("Deployment is not connected to a CmpContainer " + deploymentInfo.getDeploymentID());
        }
        CmpContainer cmpContainer = (CmpContainer) container;
        
        Number result = (Number)cmpContainer.select(deploymentInfo, methodSignature, "long", args);
        return result.longValue(); 
    }
    
    
    public static float execute_float(Object di, String methodSignature, Object... args) throws FinderException {
        DeploymentInfo deploymentInfo = (DeploymentInfo) di;
        Container container = deploymentInfo.getContainer();
        if (!(container instanceof CmpContainer)) {
            throw new FinderException("Deployment is not connected to a CmpContainer " + deploymentInfo.getDeploymentID());
        }
        CmpContainer cmpContainer = (CmpContainer) container;
        
        Number result = (Number)cmpContainer.select(deploymentInfo, methodSignature, "float", args);
        return result.floatValue(); 
    }
    
    
    public static double execute_double(Object di, String methodSignature, Object... args) throws FinderException {
        DeploymentInfo deploymentInfo = (DeploymentInfo) di;
        Container container = deploymentInfo.getContainer();
        if (!(container instanceof CmpContainer)) {
            throw new FinderException("Deployment is not connected to a CmpContainer " + deploymentInfo.getDeploymentID());
        }
        CmpContainer cmpContainer = (CmpContainer) container;
        
        Number result = (Number)cmpContainer.select(deploymentInfo, methodSignature, "double", args);
        return result.doubleValue(); 
    }
}
