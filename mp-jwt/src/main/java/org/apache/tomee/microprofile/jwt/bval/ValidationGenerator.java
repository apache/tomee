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

import org.apache.openejb.util.proxy.LocalBeanProxyFactory;
import org.apache.openejb.util.proxy.ProxyGenerationException;
import org.apache.xbean.asm7.Opcodes;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
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
 *      public JsonWebToken red$$0() {
 *          return null;
 *      }
 *
 *      @Issuer("http://foo.bar.com")
 *      public JsonWebToken blue$$1() {
 *          return null;
 *      }
 *    }
 *
 */
public abstract class ValidationGenerator implements Opcodes {

    protected final Class<?> clazz;
    protected final List<MethodConstraints> constraints;
    protected final String suffix;

    public ValidationGenerator(final Class<?> clazz, final List<MethodConstraints> constraints, final String suffix) {
        this.clazz = clazz;
        this.constraints = new ArrayList<>(constraints);
        this.suffix = suffix;
        Collections.sort(constraints);
    }

    public Class<?> generateAndLoad() {
        return loadOrCreate();
    }

    public String getName() {
        return clazz.getName() + "$$" + suffix;
    }

    public Class<?> loadOrCreate() {
        final String constraintsClassName = getName();
        final ClassLoader classLoader = clazz.getClassLoader();

        try {
            return classLoader.loadClass(constraintsClassName);
        } catch (ClassNotFoundException e) {
            // ok, let's continue on and make it
        }

        final byte[] bytes;
        try {
            bytes = generate();
        } catch (ProxyGenerationException e) {
            throw new ValidationGenerationException(clazz, e);
        }

        if (bytes == null) return null;

        try {
            return LocalBeanProxyFactory.Unsafe.defineClass(classLoader, clazz, constraintsClassName, bytes);
        } catch (IllegalAccessException e) {
            throw new ValidationGenerationException(clazz, e);
        } catch (InvocationTargetException e) {
            throw new ValidationGenerationException(clazz, e.getCause());
        }
    }

    public abstract byte[] generate() throws ProxyGenerationException;

}
