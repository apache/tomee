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
package org.apache.openejb.jee;

import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.opt.FieldAccessor_Byte;
import com.sun.xml.bind.v2.runtime.reflect.opt.MethodAccessor_Byte;

import javax.xml.bind.JAXBException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static com.sun.xml.bind.v2.bytecode.ClassTailor.toVMClassName;

/**
 * @version $Rev$ $Date$
 */
public class GeneratedAccessorFactory implements com.sun.xml.bind.AccessorFactory {

    private static final String fieldTemplateName;
    private static final String methodTemplateName;

    static {
        String s = FieldAccessor_Byte.class.getName();
        fieldTemplateName = s.substring(0, s.length() - "Byte".length()).replace('.', '/');

        s = MethodAccessor_Byte.class.getName();
        methodTemplateName = s.substring(0, s.length() - "Byte".length()).replace('.', '/');
    }


    @Override
    public Accessor createFieldAccessor(Class bean, Field field, boolean readOnly) throws JAXBException {
        int mods = field.getModifiers();
        if (Modifier.isPrivate(mods) || Modifier.isFinal(mods))
            // we can't access private fields
            return null;

        String newClassName = toVMClassName(field.getDeclaringClass()) + "$JaxbAccessorF_" + field.getName();

        return null;
    }

    @Override
    public Accessor createPropertyAccessor(Class bean, Method getter, Method setter) throws JAXBException {
        // make sure the method signatures are what we expect
        if (getter.getParameterTypes().length != 0)
            return null;
        Class<?>[] sparams = setter.getParameterTypes();
        if (sparams.length != 1)
            return null;
        if (sparams[0] != getter.getReturnType())
            return null;
        if (setter.getReturnType() != Void.TYPE)
            return null;
        if (getter.getDeclaringClass() != setter.getDeclaringClass())
            return null;
        if (Modifier.isPrivate(getter.getModifiers()) || Modifier.isPrivate(setter.getModifiers()))
            // we can't access private fields
            return null;

        Class t = sparams[0];
        String typeName = t.getName().replace('.', '_');
        if (t.isArray()) {
            typeName = "AOf_";
            String compName = t.getComponentType().getName().replace('.', '_');
            while (compName.startsWith("[L")) {
                compName = compName.substring(2);
                typeName += "AOf_";
            }
            typeName = typeName + compName;
        }

        String newClassName = toVMClassName(getter.getDeclaringClass()) + "$JaxbAccessorM_" + getter.getName() + '_' + setter.getName() + '_' + typeName;


        return null;
    }


}