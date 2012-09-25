/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.util;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.event.Event;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Provider;

import org.apache.webbeans.config.BeanTypeSetResolver;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.exception.WebBeansException;
import org.apache.webbeans.logger.WebBeansLoggerFacade;

/**
 * Utility classes with respect to the class operations.
 *
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @since 1.0
 */
@SuppressWarnings("unchecked")
public final class ClassUtil
{
    public static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPERS_MAP = new HashMap<Class<?>, Class<?>>();

    private static final Logger logger = WebBeansLoggerFacade.getLogger(ClassUtil.class);

    static
    {
        PRIMITIVE_TO_WRAPPERS_MAP.put(Integer.TYPE,Integer.class);
        PRIMITIVE_TO_WRAPPERS_MAP.put(Float.TYPE,Float.class);
        PRIMITIVE_TO_WRAPPERS_MAP.put(Double.TYPE,Double.class);
        PRIMITIVE_TO_WRAPPERS_MAP.put(Character.TYPE,Character.class);
        PRIMITIVE_TO_WRAPPERS_MAP.put(Long.TYPE,Long.class);
        PRIMITIVE_TO_WRAPPERS_MAP.put(Byte.TYPE,Byte.class);
        PRIMITIVE_TO_WRAPPERS_MAP.put(Short.TYPE,Short.class);
        PRIMITIVE_TO_WRAPPERS_MAP.put(Boolean.TYPE,Boolean.class);
        PRIMITIVE_TO_WRAPPERS_MAP.put(Void.TYPE,Void.class);
    }

    /*
     * Private constructor
     */
    private ClassUtil()
    {
        throw new UnsupportedOperationException();
    }

    public static Object newInstance(WebBeansContext webBeansContext, Class<?> clazz)
    {
        try
        {
            if(System.getSecurityManager() != null)
            {
                return webBeansContext.getSecurityService().doPrivilegedObjectCreate(clazz);
            }            
            
            return clazz.newInstance();
            
        }
        catch(Exception e)
        {
            Throwable cause = e;
            if(e instanceof PrivilegedActionException)
            {
                cause = e.getCause();
            }
            
            String error = "Error occurred while creating an instance of class : " + clazz.getName(); 
            logger.log(Level.SEVERE, error, cause);
            throw new WebBeansException(error,cause); 
        
        }
    }

    public static Class<?> getClassFromName(String name)
    {
        Class<?> clazz = null;
        ClassLoader loader = null;
        try
        {
            loader = WebBeansUtil.getCurrentClassLoader();
            clazz = Class.forName(name, true , loader);
            //X clazz = loader.loadClass(name);
            return clazz;

        }
        catch (ClassNotFoundException e)
        {
            try
            {
                loader = ClassUtil.class.getClassLoader(); 
                clazz = Class.forName(name, true , loader);

                return clazz;

            }
            catch (ClassNotFoundException e1)
            {
                try
                {
                    loader = ClassLoader.getSystemClassLoader();
                    clazz = Class.forName(name, true , loader);

                    return clazz;

                }
                catch (ClassNotFoundException e2)
                {
                    return null;
                }

            }
        }

    }

    /**
     * Check final modifier.
     * 
     * @param modifier modifier
     * @return true or false
     */
    public static boolean isFinal(Integer modifier)
    {
        Asserts.nullCheckForModifier(modifier);

        return Modifier.isFinal(modifier);
    }

    /**
     * Check abstract modifier.
     * 
     * @param modifier modifier
     * @return true or false
     */
    public static boolean isAbstract(Integer modifier)
    {
        Asserts.nullCheckForModifier(modifier);

        return Modifier.isAbstract(modifier);
    }

    /**
     * Check interface modifier.
     * 
     * @param modifier modifier
     * @return true or false
     */
    public static boolean isInterface(Integer modifier)
    {
        Asserts.nullCheckForModifier(modifier);

        return Modifier.isInterface(modifier);
    }

    /**
     * Check for class that has a final method or not.
     * 
     * @param clazz check methods of it
     * @return true or false
     */
    public static boolean hasFinalMethod(Class<?> clazz)
    {
        Asserts.nullCheckForClass(clazz);

        Method[] methods = SecurityUtil.doPrivilegedGetDeclaredMethods(clazz);
        for (Method m : methods)
        {
            if (isFinal(m.getModifiers()))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Check the class is inner or not
     * 
     * @param clazz to check
     * @return true or false
     */
    public static boolean isInnerClazz(Class<?> clazz)
    {
        Asserts.nullCheckForClass(clazz);

        return clazz.isMemberClass();
    }

    public static Class<?>  getPrimitiveWrapper(Class<?> clazz)
    {
        Asserts.nullCheckForClass(clazz);
        
        return PRIMITIVE_TO_WRAPPERS_MAP.get(clazz);

    }
    

    /**
     * Gets the class of the given type arguments.
     * <p>
     * If the given type {@link Type} parameters is an instance of the
     * {@link ParameterizedType}, it returns the raw type otherwise it return
     * the casted {@link Class} of the type argument.
     * </p>
     * 
     * @param type class or parametrized type
     * @return
     */
    public static Class<?> getClass(Type type)
    {
        return getClazz(type);
    }

    /**
     * Gets the declared methods of the given class.
     * 
     * @param clazz class instance
     * @return the declared methods
     */
    public static Method[] getDeclaredMethods(Class<?> clazz)
    {
        Asserts.nullCheckForClass(clazz);
        return SecurityUtil.doPrivilegedGetDeclaredMethods(clazz);
    }

    /**
     * Check that method has any formal arguments.
     * 
     * @param method method instance
     * @return true or false
     */
    public static boolean isMethodHasParameter(Method method)
    {
        Asserts.nullCheckForMethod(method);

        return method.getParameterTypes().length > 0;
    }

    /**
     * Gets the return type of the method.
     * 
     * @param method method instance
     * @return return type
     */
    public static Class<?> getReturnType(Method method)
    {
        Asserts.nullCheckForMethod(method);
        return method.getReturnType();
    }

    /**
     * Check method throws checked exception or not.
     * 
     * @param method method instance
     */
    public static boolean isMethodHasCheckedException(Method method)
    {
        Asserts.nullCheckForMethod(method);

        Class<?>[] et = method.getExceptionTypes();

        if (et.length > 0)
        {
            for (Class<?> type : et)
            {
                if (!Error.class.isAssignableFrom(type) && !RuntimeException.class.isAssignableFrom(type))
                {
                    return true;
                }
            }

        }

        return false;
    }

    /**
     * Call method on the instance with given arguments.
     * 
     * @param method method instance
     * @param instance object instance
     * @param args arguments
     * @return the method result
     */
    public static Object callInstanceMethod(Method method, Object instance, Object[] args)
    {
        Asserts.nullCheckForMethod(method);
        Asserts.assertNotNull(instance, "instance parameter can not be null");

        try
        {
            if (args == null)
            {
                args = new Object[] {};
            }
            return method.invoke(instance, args);

        }
        catch (Exception e)
        {
            throw new WebBeansException("Exception occurs in the method call with method : " + method.getName() + " in class : " + instance.getClass().getName(), e);
        }

    }

    public static List<Class<?>> getSuperClasses(Class<?> clazz, List<Class<?>> list)
    {
        Asserts.nullCheckForClass(clazz);

        Class<?> sc = clazz.getSuperclass();
        if (sc != null)
        {
            list.add(sc);
            getSuperClasses(sc, list);
        }

        return list;

    }

    public static Class<?>[] getMethodParameterTypes(Method method)
    {
        Asserts.nullCheckForMethod(method);
        return method.getParameterTypes();
    }

    private static Set<String> getObjectMethodNames()
    {
        if (objectMethodNames == null)
        {
            // not much synchronisation needed...
            Set<String> list = new HashSet<String>();
            Class<?> clazz = Object.class;

            Method[] methods = SecurityUtil.doPrivilegedGetDeclaredMethods(clazz);
            for (Method method : methods)
            {
                list.add(method.getName());
            }
            objectMethodNames = list;
        }

        return objectMethodNames;
    }
    private static volatile Set<String> objectMethodNames= null;
    


    public static boolean isObjectMethod(String methodName)
    {
        return getObjectMethodNames().contains(methodName);
    }

    public static boolean isMoreThanOneMethodWithName(String methodName, Class<?> clazz)
    {
        Asserts.assertNotNull(methodName, "methodName parameter can not be null");
        Asserts.nullCheckForClass(clazz);

        Method[] methods = SecurityUtil.doPrivilegedGetDeclaredMethods(clazz);
        int methodCount = 0;
        for (Method m : methods)
        {
            if (m.getName().equals(methodName))
            {
                methodCount++;
            }
        }

        return methodCount > 1;

    }

    /**
     * Gets java package if exist.
     * 
     * @param packageName package name
     * @return the package with given name
     */
    public Package getPackage(String packageName)
    {
        Asserts.assertNotNull(packageName, "packageName parameter can not be null");

        return Package.getPackage(packageName);
    }

    /**
     * Returns true if type is an instance of <code>ParameterizedType</code>
     * else otherwise.
     * 
     * @param type type of the artifact
     * @return true if type is an instance of <code>ParameterizedType</code>
     */
    public static boolean isParametrizedType(Type type)
    {
        Asserts.assertNotNull(type, "type parameter can not be null");
        if (type instanceof ParameterizedType)
        {
            return true;
        }

        return false;
    }
    
    /**
     * Returns true if type is an instance of <code>WildcardType</code>
     * else otherwise.
     * 
     * @param type type of the artifact
     * @return true if type is an instance of <code>WildcardType</code>
     */    
    public static boolean isWildCardType(Type type)
    {
        Asserts.assertNotNull(type, "type parameter can not be null");
        
        return type instanceof WildcardType;
    }
    
    public static boolean isUnboundedTypeVariable(Type type)
    {
        Asserts.assertNotNull(type, "type parameter can not be null");
        
        if (type instanceof TypeVariable)
        {
            TypeVariable wc = (TypeVariable)type;
            Type[] upper = wc.getBounds();            
            
            
            if(upper.length > 1)
            {
                return false;
            }            
            else
            {
                Type arg = upper[0];
                if(!(arg instanceof Class))
                {
                    return false;
                }
                else
                {
                    Class<?> clazz = (Class<?>)arg;
                    if(!clazz.equals(Object.class))
                    {
                        return false;
                    }                    
                }
            }            
        }
        else
        {
            return false;
        }

        return true;
    }
    
    
    /**
     * Returns true if type is an instance of <code>TypeVariable</code>
     * else otherwise.
     * 
     * @param type type of the artifact
     * @return true if type is an instance of <code>TypeVariable</code>
     */    
    public static boolean isTypeVariable(Type type)
    {
        Asserts.assertNotNull(type, "type parameter can not be null");

        return type instanceof TypeVariable;
    }
    

    /**
     * Returna true if the class is not abstract and interface.
     *     
     * @param clazz class type
     * @return true if the class is not abstract and interface
     */
    public static boolean isConcrete(Class<?> clazz)
    {
        Asserts.nullCheckForClass(clazz);

        Integer modifier = clazz.getModifiers();

        if (!isAbstract(modifier) && !isInterface(modifier))
        {
            return true;
        }

        return false;
    }

    /**
     * See specification 5.2.3.
     * @param beanType bean type
     * @param requiredType required type
     * @return true if assignable
     */
    public static boolean isAssignable(Type beanType, Type requiredType)
    {
        Asserts.assertNotNull(beanType, "beanType parameter can not be null");
        Asserts.assertNotNull(requiredType, "requiredType parameter can not be null");
        
        //Bean and required types are ParametrizedType
        if (beanType instanceof ParameterizedType && requiredType instanceof ParameterizedType)
        {
            return isAssignableForParametrized((ParameterizedType) beanType, (ParameterizedType) requiredType);
        }
        //Both type is class type
        else if (beanType instanceof Class && requiredType instanceof Class)
        {
            Class<?> clzBeanType = (Class<?>)beanType;
            Class<?> clzReqType = (Class<?>)requiredType;
            
            if(clzBeanType.isPrimitive())
            {
                clzBeanType = getPrimitiveWrapper(clzBeanType);
            }
            
            if(clzReqType.isPrimitive())
            {
                clzReqType = getPrimitiveWrapper(clzReqType);
            }
            
            return clzReqType.equals(clzBeanType);
        }
        //Bean type is Parametrized and required type is class type
        else if(beanType instanceof ParameterizedType && requiredType instanceof Class)
        {
            boolean ok = true;
            ParameterizedType ptBean = (ParameterizedType)beanType;
            Class<?> clazzBeanType = (Class<?>)ptBean.getRawType();
            Class<?> clazzReqType = (Class<?>)requiredType;
            if(isClassAssignable(clazzReqType, clazzBeanType ))
            {
                Type[]  beanTypeArgs = ptBean.getActualTypeArguments();               
                for(Type actual : beanTypeArgs)
                {
                    if(!ClassUtil.isUnboundedTypeVariable(actual))
                    {
                        if(actual instanceof Class)
                        {
                            Class<?> clazz = (Class<?>)actual;
                            if(!clazz.equals(Object.class))
                            {
                                ok = false;
                                break;
                            }
                        }
                        else
                        {
                            ok = false;
                            break;
                        }
                    }
                }                
            }
            else
            {
                ok = false;
            }
            
            
            return ok;
        }
        //Bean type is class and required type is parametrized
        else if(beanType instanceof Class && requiredType instanceof ParameterizedType)
        {
            Class<?> clazzBeanType = (Class<?>)beanType;
            ParameterizedType ptReq = (ParameterizedType)requiredType;
            Class<?> clazzReqType = (Class<?>)ptReq.getRawType();
            
            if(Provider.class.isAssignableFrom(clazzReqType) ||
                    Event.class.isAssignableFrom(clazzReqType))
            {
                if(isClassAssignable(clazzReqType, clazzBeanType))
                {
                    return true;
                }    
            }
                        
            return false;
        }
        else
        {
            return false;
        }
    }

    /**
     * Checks that event is applicable
     * for the given observer type.
     * @param eventType event type
     * @param observerType observer type
     * @return true if event is applicable
     */
    public static boolean checkEventTypeAssignability(Type eventType, Type observerType)
    {
        //Observer type is a TypeVariable
        if(isTypeVariable(observerType))
        {
            Class<?> eventClass = getClass(eventType);
                        
            TypeVariable<?> tvBeanTypeArg = (TypeVariable<?>)observerType;
            Type tvBound = tvBeanTypeArg.getBounds()[0];
            
            if(tvBound instanceof Class)
            {
                Class<?> clazzTvBound = (Class<?>)tvBound;                
                if(clazzTvBound.isAssignableFrom(eventClass))
                {
                    return true;
                }                    
            }
        }
        //Both of them are ParametrizedType
        else if(observerType instanceof ParameterizedType && eventType instanceof ParameterizedType)
        {
            return isAssignableForParametrized((ParameterizedType)eventType, (ParameterizedType)observerType);
        }
        //Observer is class and Event type is Parametrized
        else if(observerType instanceof Class && eventType instanceof ParameterizedType)
        {
            Class<?> clazzBeanType = (Class<?>)observerType;
            ParameterizedType ptEvent = (ParameterizedType)eventType;
            Class<?> eventClazz = (Class<?>)ptEvent.getRawType();
            
            if(isClassAssignable(clazzBeanType, eventClazz))
            {
                return true;
            }
            
            return false;            
        }
        //Both of them is class type
        else if(observerType instanceof Class && eventType instanceof Class)
        {
            return isClassAssignable((Class<?>)observerType, (Class<?>) eventType);
        }
        
        return false;
    }
    
    
    /**
     * Returns true if rhs is assignable type
     * to the lhs, false otherwise.
     * 
     * @param lhs left hand side class
     * @param rhs right hand side class
     * @return true if rhs is assignable to lhs
     */
    public static boolean isClassAssignable(Class<?> lhs, Class<?> rhs)
    {
        Asserts.assertNotNull(lhs, "lhs parameter can not be null");
        Asserts.assertNotNull(rhs, "rhs parameter can not be null");

        if(lhs.isPrimitive())
        {
            lhs = getPrimitiveWrapper(lhs);
        }
        
        if(rhs.isPrimitive())
        {
            rhs = getPrimitiveWrapper(rhs);
        }
        
        if (lhs.isAssignableFrom(rhs))
        {
            return true;
        }

        return false;
    }

    /**
     * Returns true if given bean's api type is injectable to
     * injection point required type.
     * 
     * @param beanType bean parametrized api type
     * @param requiredType injection point parametrized api type
     * @return if injection is possible false otherwise
     */
    public static boolean isAssignableForParametrized(ParameterizedType beanType, ParameterizedType requiredType)
    {
        Class<?> beanRawType = (Class<?>) beanType.getRawType();
        Class<?> requiredRawType = (Class<?>) requiredType.getRawType();

        if (ClassUtil.isClassAssignable(requiredRawType,beanRawType))
        {
            //Bean api type actual type arguments
            Type[] beanTypeArgs = beanType.getActualTypeArguments();
            
            //Injection point type actual arguments
            Type[] requiredTypeArgs = requiredType.getActualTypeArguments();
            
            if(beanTypeArgs.length != requiredTypeArgs.length)
            {                
                return false;
            }
            else
            {
                return isAssignableForParametrizedCheckArguments(beanTypeArgs, requiredTypeArgs);
            }
        }

        return false;
    }
    
    /**
     * Check parametrized type actual type arguments.
     * @param beanTypeArgs bean type actual type arguments
     * @param requiredTypeArgs required type actual type arguments.
     * @return true if assignment applicable
     */
    private static boolean isAssignableForParametrizedCheckArguments(Type[] beanTypeArgs, Type[] requiredTypeArgs)
    {
        Type requiredTypeArg = null;
        Type beanTypeArg = null;
        for(int i = 0; i< requiredTypeArgs.length;i++)
        {
            requiredTypeArg = requiredTypeArgs[i];
            beanTypeArg = beanTypeArgs[i];
            
            //Required type is parametrized and bean type is parametrized
            if(ClassUtil.isParametrizedType(requiredTypeArg) && ClassUtil.isParametrizedType(beanTypeArg))
            {
                return checkBeanAndRequiredTypeIsParametrized(beanTypeArg, requiredTypeArg);
            }
            //Required type is wildcard
            else if(ClassUtil.isWildCardType(requiredTypeArg))
            {
                return checkRequiredTypeIsWildCard(beanTypeArg, requiredTypeArg);
            }
            //Required type is actual type and bean type is type variable
            else if(requiredTypeArg instanceof Class && ClassUtil.isTypeVariable(beanTypeArg))
            {
                return checkRequiredTypeIsClassAndBeanTypeIsVariable(beanTypeArg, requiredTypeArg);
            }
            //Required type is Type variable and bean type is type variable
            else if(ClassUtil.isTypeVariable(requiredTypeArg) && ClassUtil.isTypeVariable(beanTypeArg))
            {
                return checkBeanTypeAndRequiredIsTypeVariable(beanTypeArg, requiredTypeArg);
            }      
            
            //Both type is actual type
            else if((beanTypeArg instanceof Class) && (requiredTypeArg instanceof Class))
            {
                if(isClassAssignable((Class<?>)requiredTypeArg,(Class<?>)beanTypeArg))
                {
                    return true;
                }
            }
            //Bean type is actual type and required type is type variable
            else if((beanTypeArg instanceof Class) && (ClassUtil.isTypeVariable(requiredTypeArg)))
            {
                return checkRequiredTypeIsTypeVariableAndBeanTypeIsClass(beanTypeArg, requiredTypeArg);
            }
        }
        
        return false;
    }

    /**
     * Check parametrized bean type and parametrized
     * required types.
     * @param beanTypeArg parametrized bean type
     * @param requiredTypeArg parametrized required type
     * @return true if types are assignables
     * @since 1.1.1
     */
    public static boolean checkBeanAndRequiredTypeIsParametrized(Type beanTypeArg, Type requiredTypeArg)
    {
        ParameterizedType ptRequiredTypeArg = (ParameterizedType)requiredTypeArg;
        ParameterizedType ptBeanTypeArg = (ParameterizedType)beanTypeArg;
        
        //Equal raw types
        if(ptRequiredTypeArg.getRawType().equals(ptBeanTypeArg.getRawType()))
        {
            //Check arguments
            Type[] actualArgsRequiredType = ptRequiredTypeArg.getActualTypeArguments();
            Type[] actualArgsBeanType = ptRequiredTypeArg.getActualTypeArguments();
            
            if(actualArgsRequiredType.length > 0 && actualArgsBeanType.length == actualArgsRequiredType.length)
            {
                return isAssignableForParametrizedCheckArguments(actualArgsBeanType, actualArgsRequiredType);
            }
            else
            {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Check bean type and required type.
     * <p>
     * Required type is a wildcard type.
     * </p>
     * @param beanTypeArg bean type
     * @param requiredTypeArg required type
     * @return true if condition satisfies
     * @since 1.1.1
     */
    public static boolean checkRequiredTypeIsWildCard(Type beanTypeArg, Type requiredTypeArg)
    {
        WildcardType wctRequiredTypeArg = (WildcardType)requiredTypeArg;
        Type upperBoundRequiredTypeArg =  wctRequiredTypeArg.getUpperBounds()[0];
        Type[] lowerBoundRequiredTypeArgs =  wctRequiredTypeArg.getLowerBounds();
        
        if(beanTypeArg instanceof Class)
        {
            Class<?> clazzBeanTypeArg = (Class<?>)beanTypeArg;
            if(upperBoundRequiredTypeArg instanceof Class)
            {
                //Check upper bounds
                Class<?> clazzUpperBoundTypeArg = (Class<?>)upperBoundRequiredTypeArg;
                if(clazzUpperBoundTypeArg != Object.class)
                {
                    if(!clazzUpperBoundTypeArg.isAssignableFrom(clazzBeanTypeArg))
                    {                                       
                        return false;
                    }
                }
                
                //Check lower bounds
                if(lowerBoundRequiredTypeArgs.length > 0 &&  lowerBoundRequiredTypeArgs[0] instanceof Class)
                {
                    Class<?> clazzLowerBoundTypeArg = (Class<?>)lowerBoundRequiredTypeArgs[0];
                    
                    if(clazzLowerBoundTypeArg != Object.class)
                    {
                        if(!clazzBeanTypeArg.isAssignableFrom(clazzLowerBoundTypeArg))
                        {
                            return false;
                        }                                
                    }
                }
            }                    
        }
        else if(ClassUtil.isTypeVariable(beanTypeArg))
        {
            TypeVariable<?> tvBeanTypeArg = (TypeVariable<?>)beanTypeArg;
            Type tvBound = tvBeanTypeArg.getBounds()[0];
            
            if(tvBound instanceof Class)
            {
                Class<?> clazzTvBound = (Class<?>)tvBound;
                
                if(upperBoundRequiredTypeArg instanceof Class)
                {
                    Class<?> clazzUpperBoundTypeArg = (Class<?>)upperBoundRequiredTypeArg;                    
                    if(clazzUpperBoundTypeArg != Object.class && clazzTvBound != Object.class)
                    {
                        if(!clazzUpperBoundTypeArg.isAssignableFrom(clazzTvBound))
                        {   
                            return false;
                        }                       
                    }
                    
                    //Check lower bounds
                    if(lowerBoundRequiredTypeArgs.length > 0 &&  lowerBoundRequiredTypeArgs[0] instanceof Class)
                    {
                        Class<?> clazzLowerBoundTypeArg = (Class<?>)lowerBoundRequiredTypeArgs[0];
                        
                        if(clazzLowerBoundTypeArg != Object.class)
                        {
                            if(!clazzTvBound.isAssignableFrom(clazzLowerBoundTypeArg))
                            {
                                return false;
                            }                                    
                        }
                    }                    
                }                                    
            }
        }
         
        return true;
    }
    
    /**
     * Checking bean type and required type.
     * <p>
     * Required type is class and bean type is
     * a type variable.
     * </p>
     * @param beanTypeArg bean type 
     * @param requiredTypeArg required type
     * @return true if condition satisfy
     */
    public static boolean checkRequiredTypeIsClassAndBeanTypeIsVariable(Type beanTypeArg, Type requiredTypeArg)
    {
        Class<?> clazzRequiredType = (Class<?>)requiredTypeArg;
        
        TypeVariable<?> tvBeanTypeArg = (TypeVariable<?>)beanTypeArg;
        Type tvBound = tvBeanTypeArg.getBounds()[0];
        
        if(tvBound instanceof Class)
        {
            Class<?> clazzTvBound = (Class<?>)tvBound;
            
            if(clazzTvBound != Object.class)
            {
                if(!clazzTvBound.isAssignableFrom(clazzRequiredType))
                {
                    return false;
                }                                    
            }            
        }
        
        return true;
    }
    
    public static boolean checkRequiredTypeIsTypeVariableAndBeanTypeIsClass(Type beanTypeArg, Type requiredTypeArg)
    {
        Class<?> clazzBeanType = (Class<?>)beanTypeArg;
        
        TypeVariable<?> tvRequiredTypeArg = (TypeVariable<?>)requiredTypeArg;
        Type tvBound = tvRequiredTypeArg.getBounds()[0];
        
        if(tvBound instanceof Class)
        {
            Class<?> clazzTvBound = (Class<?>)tvBound;
            
            if(clazzTvBound.isAssignableFrom(clazzBeanType))
            {
                return true;
            }                    
        }
        
        return false;
    }
    

    public static boolean checkBeanTypeAndRequiredIsTypeVariable(Type beanTypeArg, Type requiredTypeArg)
    {
        TypeVariable<?> tvBeanTypeArg = (TypeVariable<?>)beanTypeArg;
        Type tvBeanBound = tvBeanTypeArg.getBounds()[0];
        
        TypeVariable<?> tvRequiredTypeArg = (TypeVariable<?>)requiredTypeArg;
        Type tvRequiredBound = tvRequiredTypeArg.getBounds()[0];
        
        if(tvBeanBound instanceof Class && tvRequiredBound instanceof Class)
        {
            Class<?> clazzTvBeanBound = (Class<?>)tvBeanBound;
            Class<?> clazzTvRequiredBound = (Class<?>)tvRequiredBound;
            
            if(clazzTvBeanBound.isAssignableFrom(clazzTvRequiredBound))
            {
                return true;
            }                    
        }
        
        return false;
    }

    /**
     * @param clazz webbeans implementation class
     * @param methodName name of the method that is searched
     * @param parameterTypes parameter types of the method(it can be subtype of
     *            the actual type arguments of the method)
     * @return the list of method that satisfies the condition
     */
    public static List<Method> getClassMethodsWithTypes(Class<?> clazz, String methodName, List<Class<?>> parameterTypes)
    {
        Asserts.nullCheckForClass(clazz);
        Asserts.assertNotNull(methodName, "methodName parameter can not be null");
        Asserts.assertNotNull(parameterTypes, "parameterTypes parameter can not be null");

        List<Method> methodList = new ArrayList<Method>();

        Method[] methods = SecurityUtil.doPrivilegedGetDeclaredMethods(clazz);

        int j = 0;
        for (Method method : methods)
        {
            if (method.getName().equals(methodName))
            {
                Class<?>[] defineTypes = method.getParameterTypes();

                if (defineTypes.length != parameterTypes.size())
                {
                    continue;
                }

                boolean ok = true;

                if (parameterTypes.size() > 0)
                {
                    ok = false;
                }

                if (!ok)
                {
                    for (Class<?> defineType : defineTypes)
                    {
                        if (defineType.isAssignableFrom(parameterTypes.get(j)))
                        {
                            ok = true;
                        }
                        else
                        {
                            ok = false;
                        }

                        j++;
                    }
                }

                if (ok)
                {
                    methodList.add(method);
                }
            }

        }

        return methodList;
    }

    public static Method getClassMethodWithTypes(Class<?> clazz, String methodName, List<Class<?>> parameterTypes)
    {
        Asserts.nullCheckForClass(clazz);
        Asserts.assertNotNull(methodName, "methodName parameter can not be null");
        Asserts.assertNotNull(parameterTypes, "parameterTypes parameter can not be null");

        Method[] methods = SecurityUtil.doPrivilegedGetDeclaredMethods(clazz);

        int j = 0;
        for (Method method : methods)
        {
            if (method.getName().equals(methodName))
            {
                if (parameterTypes != null && parameterTypes.size() > 0)
                {
                    Class<?>[] defineTypes = method.getParameterTypes();

                    if (defineTypes.length != parameterTypes.size())
                    {
                        continue;
                    }

                    boolean ok = false;

                    for (Class<?> defineType : defineTypes)
                    {
                        if (defineType.equals(parameterTypes.get(j)))
                        {
                            ok = true;
                        }
                        else
                        {
                            ok = false;
                        }
                    }

                    if (ok)
                    {
                        return method;
                    }
                }
                else
                {
                    return method;
                }
            }
        }

        return null;
    }

    public static boolean isArray(Class<?> clazz)
    {
        Asserts.nullCheckForClass(clazz);

        return clazz.isArray();
    }

    /**
     * Learn whether the specified class is defined with type parameters.
     * @param clazz to check
     * @return true if there are type parameters
     * @since 1.1.1
     */
    public static boolean isDefinitionContainsTypeVariables(Class<?> clazz)
    {
        Asserts.nullCheckForClass(clazz);
        
        return (clazz.getTypeParameters().length > 0) ? true : false;
    }

    /**
     * Returns declared type arguments if {@code type} is a
     * {@link ParameterizedType} instance, else an empty array.
     * Get the actual type arguments of a type.
     * @param type
     * @return array of type arguments available
     * @since 1.1.1
     */
    public static Type[] getActualTypeArguments(Type type)
    {
        Asserts.assertNotNull(type, "type parameter can not be null");

        if (type instanceof ParameterizedType)
        {
            return ((ParameterizedType) type).getActualTypeArguments();

        }
        else
        {
            return new Type[0];
        }
    }


    public static Set<Type> setTypeHierarchy(Set<Type> set, Type clazz)
    {
        BeanTypeSetResolver resolver = new BeanTypeSetResolver(clazz);
        resolver.startConfiguration();
        set.addAll(resolver.getHierarchy());
        
        return set;
    }

    /**
     * Return raw class type for given type.
     * @param type base type instance
     * @return class type for given type
     */
    public static Class<?> getClazz(Type type)
    {
        Class<?> raw = null;
        
        if(type instanceof ParameterizedType)
        {
            ParameterizedType pt = (ParameterizedType)type;
            raw = (Class<?>)pt.getRawType();                
        }
        else if(type instanceof Class)
        {
            raw = (Class<?>)type;
        }
        else if(type instanceof GenericArrayType)
        {
            GenericArrayType arrayType = (GenericArrayType)type;
            raw = getClazz(arrayType.getGenericComponentType());
        }
        
        return raw;
    }

    public static Set<Type> setInterfaceTypeHierarchy(Set<Type> set, Class<?> clazz)
    {
        Asserts.nullCheckForClass(clazz);

        do
        {
            Class<?>[] interfaces = clazz.getInterfaces();

            for (Class<?> cl : interfaces)
            {
                set.add(cl);

                setTypeHierarchy(set, cl);
            }
            clazz = clazz.getSuperclass();
        } while (clazz != null && clazz != Object.class);

        return set;
    }

    /**
     * Return true if it does not contain type variable for wildcard type
     * false otherwise.
     * 
     * @param pType parameterized type
     * @return true if it does not contain type variable for wildcard type
     */
    public static boolean checkParametrizedType(ParameterizedType pType)
    {
        Asserts.assertNotNull(pType, "pType argument can not be null");
        
        Type[] types = pType.getActualTypeArguments();

        for (Type type : types)
        {
            if (type instanceof ParameterizedType)
            {
                return checkParametrizedType((ParameterizedType) type);
            }
            else if ((type instanceof TypeVariable) || (type instanceof WildcardType))
            {
                return false;
            }
        }

        return true;
    }

    public static Field[] getFieldsWithType(WebBeansContext webBeansContext, Class<?> clazz, Type type)
    {
        Asserts.nullCheckForClass(clazz);
        Asserts.assertNotNull(type, "type parameter can not be null");

        List<Field> fieldsWithType = new ArrayList<Field>();
        Field[] fields = webBeansContext.getSecurityService().doPrivilegedGetDeclaredFields(clazz);
        for (Field field : fields)
        {
            if(field.getType().equals(type))
            {
                fieldsWithType.add(field);
            }
        }

        return fieldsWithType.toArray(new Field[fieldsWithType.size()]);

    }

    /**
     * Returns injection point raw type.
     * 
     * @param injectionPoint injection point definition
     * @return injection point raw type
     */
    public static Class<?> getRawTypeForInjectionPoint(InjectionPoint injectionPoint)
    {
        Class<?> rawType = null;
        Type type = injectionPoint.getType();
        
        if(type instanceof Class)
        {
            rawType = (Class<?>) type;
        }
        else if(type instanceof ParameterizedType)
        {
            ParameterizedType pt = (ParameterizedType)type;            
            rawType = (Class<?>)pt.getRawType();                                                
        }
        
        return rawType;
    }

    /**
     * Learn whether <code>superClassMethod</code> is overridden by <code>subClassMethod</code>.
     * @param subClassMethod potentially overriding
     * @param superClassMethod potentially overridden
     * @return true if overridden
     * @since 1.1.1
     */
    public static boolean isOverridden(Method subClassMethod, Method superClassMethod)
    {
        if (subClassMethod.getName().equals(superClassMethod.getName()) && Arrays.equals(subClassMethod.getParameterTypes(), superClassMethod.getParameterTypes()))
        {
            int modifiers = superClassMethod.getModifiers();
            if(Modifier.isPrivate(modifiers))
            {
                return false;
            }
            
            if(!Modifier.isProtected(modifiers) && !Modifier.isPublic(modifiers))                 
            {
                Class<?> superClass = superClassMethod.getDeclaringClass();
                Class<?> subClass = subClassMethod.getDeclaringClass();
                
                //Same package
                if(!subClass.getPackage().getName().equals(superClass.getPackage().getName()))
                {
                    return false;
                }
            }
            
            return true;
        }
        
        return false;
    }
    
}
