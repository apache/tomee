/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openjpa.lib.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.util.Arrays;

import serp.bytecode.BCClass;
import serp.bytecode.BCClassLoader;
import serp.bytecode.BCMethod;
import serp.bytecode.Code;
import serp.bytecode.Project;

/**
 * Dynamically generates concrete implementations of abstract classes.
 *
 * @author Marc Prud'hommeaux
 * @author Pinaki Poddar
 * 
 * @nojavadoc
 * @since 1.3.0
 */
public class ConcreteClassGenerator {
    /**
     * Get the constructor of the concrete, dynamic wrapper class of the given abstract class 
     * with matching argument types.
     * @param the argTypes of the constructor to look for. null signify default constructor.
     */
    public static <T> Constructor<T> getConcreteConstructor(Class<T> abstractClass, Class<?>... argTypes)
    throws ClassNotFoundException {
        Class<? extends T> cls = makeConcrete(abstractClass);
        Constructor<?>[] constructors = cls.getConstructors();
        int args = argTypes == null ? 0 : argTypes.length;
        for (Constructor<?> cons : constructors) {
            Class<?>[] params = cons.getParameterTypes();
            if (params.length != args)
                continue;
            boolean match = false;
            for (int i = 0; i < params.length; i++) {
                match = params[i].isAssignableFrom(argTypes[i]);
                if (!match)
                    break;
            }
            if (match) {
                return (Constructor<T>)cons;
            }
        }
        throw new RuntimeException(abstractClass + " has no constructor with " + 
                (args == 0 ? "void" : Arrays.toString(argTypes)));
    }
    
    /** 
     *  Takes an abstract class and returns a concrete implementation. Note
     *  that it doesn't actually implement any abstract methods, it
     *  merely makes an abstract class loadable. Abstract methods will
     *  throw a {@link AbstractMethodError}.
     *  
     *  @param  abstractClass  the abstract class
     *  @return a concrete class
     */
    public static <T> Class<? extends T> makeConcrete(Class<T> abstractClass)
        throws ClassNotFoundException {
        if (abstractClass == null)
            return null;

        if (!Modifier.isAbstract(abstractClass.getModifiers()))
            return abstractClass;

        Project project = new Project();
        BCClassLoader loader = AccessController.doPrivileged(J2DoPrivHelper
            .newBCClassLoaderAction(project, abstractClass.getClassLoader()));

        String name = abstractClass.getName()+"_";
        BCClass bc = AccessController.doPrivileged(J2DoPrivHelper.
            loadProjectClassAction(project, name));
        
        bc.setSuperclass(abstractClass);

        Constructor<?>[] constructors = abstractClass.getConstructors();
        if (constructors == null || constructors.length == 0) {
            bc.addDefaultConstructor().makePublic();
        } else {
            for (int i = 0; i < constructors.length; i++) {
                Constructor<?> con = constructors[i];
                Class<?>[] args = con.getParameterTypes();

                BCMethod bccon = bc.declareMethod("<init>", void.class, args);
                Code code = bccon.getCode(true);

                code.xload().setThis();

                for (int j = 0; j < args.length; j++) {
                    code.aload().setParam(j);
                    code.checkcast().setType(args[j]);
                }

                code.invokespecial().setMethod(abstractClass, "<init>", void.class, args);
                code.vreturn();

                code.calculateMaxStack();
                code.calculateMaxLocals();
            }
        }

        Class<?> cls = Class.forName(bc.getName(), false, loader);
        return (Class<? extends T>)cls;
    }

    /**
     * Construct a new instance by the given constructor and its arguments.
     * Hopefully faster than looking for constructor in overloaded implementations. 
     */
    public static <T> T newInstance(Constructor<T> cons, Object... params) {
        try {
            return cons.newInstance(params);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    
    /** 
     *  Utility method for safely invoking a constructor that we do
     *  not expect to throw exceptions. 
     *  
     *  @param  c          the class to construct
     *  @param  paramTypes the types of the parameters
     *  @param  params     the parameter values
     *  @return            the new instance
     */
    public static <T> T newInstance(Class<T> c, Class<?>[] paramTypes, Object[] params) {
        try {
            return c.getConstructor(paramTypes).newInstance(params);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /** 
     *  @see #newInstance(java.lang.Class,java.lang.Class[],java.lang.Object[])
     */
    public static <T> T newInstance(Class<T> c) {
        return newInstance(c, new Class[] { }, new Object[] { });
    }

    /** 
     *  @see #newInstance(java.lang.Class,java.lang.Class[],java.lang.Object[])
     */
    public static <T,P> T newInstance(Class<T> c, Class<? extends P> paramType, P param) {
        return newInstance(c,
            new Class[] { paramType },
            new Object[] { param });
    }

    /** 
     *  @see #newInstance(java.lang.Class,java.lang.Class[],java.lang.Object[])
     */
    public static <T,P1,P2> T newInstance(Class<T> c, Class<? extends P1> paramType1, P1 param1,
        Class<? extends P2> paramType2, P2 param2) {
        return newInstance(c,
            new Class[] { paramType1, paramType2 },
            new Object[] { param1, param2 });
    }

    /** 
     *  @see #newInstance(java.lang.Class,java.lang.Class[],java.lang.Object[])
     */
    public static <T,P1,P2,P3> T newInstance(Class<T> c, Class<? extends P1> paramType1, P1 param1,
        Class<? extends P2> paramType2, P2 param2, Class<? extends P3> paramType3, P3 param3) {
        return newInstance(c,
            new Class[] { paramType1, paramType2, paramType3 },
            new Object[] { param1, param2, param3 });
    }

    /** 
     *  @see #newInstance(java.lang.Class,java.lang.Class[],java.lang.Object[])
     */
    public static <T,P1,P2,P3,P4> T newInstance(Class<T> c, Class<? extends P1> paramType1, P1 param1,
        Class<? extends P2> paramType2, P2 param2, Class<? extends P3> paramType3, P3 param3,
        Class<? extends P4> paramType4, P4 param4) {
        return newInstance(c,
            new Class[] { paramType1, paramType2, paramType3, paramType4 },
            new Object[] { param1, param2, param3, param4 });
    }
}

