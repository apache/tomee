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

import org.apache.xbean.asm8.ClassWriter;
import org.apache.xbean.asm8.MethodVisitor;
import org.apache.xbean.asm8.Opcodes;
import org.apache.xbean.asm8.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Generate concrete implementations of EjbPostCreatexxx
 * methods for a bean class.  This is implemented in a separate
 * class because it is used by both the CMP1 and CMP2 generators.
 *
 * @version $Rev$ $Date$
 */
public class PostCreateGenerator {
    private final Class beanClass;
    private final ClassWriter cw;

    /**
     * Constructor for a PostCreateGenerator.
     *
     * @param beanClass The source EJB implementation class (the user
     *                  provided class).
     * @param cw        The ClassWriter instance used for constructing the
     *                  instantiation class.  This has already gone through
     *                  other generation steps, we're implementing additional
     *                  stages of the process.
     */
    public PostCreateGenerator(final Class beanClass, final ClassWriter cw) {
        this.beanClass = beanClass;
        this.cw = cw;
    }


    /**
     * Generate the ejbPostCreatexxxx methods.  Inorder to
     * be considered for generation, there must A) be a
     * corresponding ejbCreatexxxx method and B) the
     * target method must either not exist or exist but be
     * abstract.
     */
    public void generate() {
        // ok, scan the class for the ejbCreate methods and check to see if 
        // we need to provide an ejbPostCreate implementation. 
        for (final Method ejbCreate : beanClass.getMethods()) {
            if (!ejbCreate.getName().startsWith("ejbCreate")) {
                continue;
            }

            final StringBuilder ejbPostCreateName = new StringBuilder(ejbCreate.getName());
            ejbPostCreateName.replace(0, "ejbC".length(), "ejbPostC");

            // if there is a concrete method here, we just skip this. 
            if (hasMethod(beanClass, ejbPostCreateName.toString(), ejbCreate.getParameterTypes())) {
                continue;
            }
            // we need to generate this method 
            createEjbPostCreate(ejbPostCreateName.toString(), ejbCreate);
        }
    }

    /**
     * Test whether a class provides a concrete implementation
     * of a class with the given name and parameter types.
     *
     * @param beanClass The source implementation class.
     * @param name      The required method name.
     * @param args      The list of argument types.
     * @return true if the method exists and is NOT abstract.  Returns
     * false if the method is not found or IS abstract.
     */
    private boolean hasMethod(final Class beanClass, final String name, final Class... args) {
        try {
            final Method method = beanClass.getMethod(name, args);
            return !Modifier.isAbstract(method.getModifiers());
        } catch (final NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Generate an ejbPostCreatexxxx method corresponding
     * to an ejbCreatexxxx method definition.  These provided
     * methods are just empty stubs.
     *
     * @param ejbPostCreateName The name we're creating under.
     * @param ejbCreate         The matching ejbCreate method.  The post create method
     *                          will match this one in terms of method signature.
     */
    public void createEjbPostCreate(final String ejbPostCreateName, final Method ejbCreate) {
        final String methodDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getArgumentTypes(ejbCreate));
        final MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, ejbPostCreateName, methodDescriptor, null, null);
        mv.visitCode();
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, ejbCreate.getParameterTypes().length + 1);
        mv.visitEnd();
    }
}
