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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.microprofile.jwt.bval;

import org.apache.xbean.asm8.AnnotationVisitor;
import org.apache.xbean.asm8.ClassWriter;
import org.apache.xbean.asm8.MethodVisitor;
import org.apache.xbean.asm8.Type;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.lang.reflect.Method;
import java.util.List;

/**
 * We allow CDI and EJB beans to use BeanValidation to validate a JsonWebToken
 * instance by simply creating contstraints and putting them on that method.
 *
 * BeanValidation doesn't "see" them there so we have to generate a class
 * that has the annotations in a place BeanValidation can see.
 *
 * To accomplish this, for every method that has BeanValidation constraints
 * we generate an equivalent method that has those same annotations and
 * returns JsonWebToken.
 *
 * We can then pass the generated method to BeanValidation's
 * ExecutableValidator.validateReturnValue and pass in the JsonWebToken instance
 *
 * The only purpose of this generated class and these generated methods is to
 * make BeanValidation happy.  If BeanValidation added something like this:
 *
 *   getValidator().validate(Object instance, Annotation[] annotations);
 *
 * Then all the code here could be deleted.
 *
 * A short example of the kind of code it generates.
 *
 * This class:
 *
 *    public class Colors {
 *      @Issuer("http://foo.bar.com")
 *      public void red(String foo) {
 *      }
 *
 *      @Issuer("http://foo.bar.com")
 *      public boolean blue(boolean b) {
 *          return b;
 *      }
 *
 *      public void green() {
 *      }
 *    }
 *
 * Would result in this generated class:
 *
 *    public class Colors$$JwtConstraints {
 *
 *      private Colors$$JwtConstraints() {
 *      }
 *
 *      @Issuer("http://foo.bar.com")
 *      public JsonWebToken red(String foo) {
 *          return null;
 *      }
 *
 *      @Issuer("http://foo.bar.com")
 *      public JsonWebToken blue(boolean b) {
 *          return null;
 *      }
 *    }
 *
 */
public class JwtValidationGenerator extends ValidationGenerator {

    public JwtValidationGenerator(final Class<?> clazz, final List<MethodConstraints> constraints) {
        super(clazz, constraints, "JwtConstraints");
    }

    protected void generateMethods(final ClassWriter cw) {
        for (final MethodConstraints methodConstraints : constraints) {
            final Method method = methodConstraints.getMethod();
            final String name = method.getName();

            // Declare a method of return type JsonWebToken for use with
            // a call to BeanValidation's ExecutableValidator.validateReturnValue
            final Type returnType = Type.getType(JsonWebToken.class);
            final Type[] parameterTypes = Type.getArgumentTypes(method);
            final String descriptor = Type.getMethodDescriptor(returnType, parameterTypes);

            final int access = method.isVarArgs() ? ACC_PUBLIC + ACC_VARARGS : ACC_PUBLIC;

            final MethodVisitor mv = cw.visitMethod(access, name, descriptor, null, null);

            // Put the method name on the
            final AnnotationVisitor av = mv.visitAnnotation(Type.getDescriptor(Generated.class), true);
            av.visit("value", this.getClass().getName());
            av.visitEnd();

            // track the MethodVisitor
            // We will later copy over the annotations
            generatedMethods.put(method.getName() + Type.getMethodDescriptor(method), new ConstrainedMethodVisitor(mv, methodConstraints));

            // The method will simply return null
            mv.visitCode();
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
        }
    }
}
