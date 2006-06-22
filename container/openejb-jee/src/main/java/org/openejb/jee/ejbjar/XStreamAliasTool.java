/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.jee.ejbjar;

import javax.xml.namespace.QName;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @version $Revision$ $Date$
 */
public class XStreamAliasTool {

    private List seen = new ArrayList();

    public XStreamAliasTool() {
        seen.add(Object.class);
        seen.add(String.class);
        seen.add(QName.class);
        seen.add(Integer.class);
        seen.add(Integer.TYPE);
        seen.add(Boolean.TYPE);
        seen.add(Boolean.class);
        seen.add(Enum.class);
    }

    public static void main(String[] args) {
        XStreamAliasTool tool = new XStreamAliasTool();
        tool.printClass(EjbJar.class);
        tool.printClass(Entity.class);
        tool.printClass(Session.class);
        tool.printClass(MessageDrivenBean.class);
    }

    public void printClass(Class clazz) {
        if (clazz == null){
            return;
        }
        
        printClass(clazz.getSuperclass());
        if (seen.contains(clazz)) {
            return;
        }
        seen.add(clazz);

        if (Enum.class.isAssignableFrom(clazz)) {
            return;
        }

        String s = getXmlName(clazz);
        String className = clazz.getName().replaceFirst(".*\\.", "");
        className = className.replace('$', '.');
        System.out.println("\n        //--------  "+clazz.getSimpleName()+" -------\n");
        System.out.println("        xstream.alias(\"" + s + "\", " + className + ".class);");


        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            printField(field, clazz);
        }
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            Class fieldType = getGenericType(field);
            printClass(fieldType);
        }

    }

    private Class getGenericType(Field field) {
        Class type = field.getType();
        Type genericType = field.getGenericType();

        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            Type firstParamType = parameterizedType.getActualTypeArguments()[0];
            return (Class) firstParamType;
        } else if (genericType instanceof Class) {
            return (Class) genericType;
        } else {
            return type;
        }
    }


    private void printField(Field field, Class clazz) {
//        if (seen.contains(field)){
//            return;
//        }
//        seen.add(field);
//        Class clazz = field.getDeclaringClass();
        String className = clazz.getName().replaceFirst(".*\\.", "");
        className = className.replace('$', '.');

        String elementName = getXmlName(field.getName());

        String fieldName = field.getName();


        if (List.class.isAssignableFrom(field.getType())) {
            Class genericType = getGenericType(field);
            if (String.class.isAssignableFrom(genericType)) {
                System.out.println("        xstream.addImplicitCollection(" + className + ".class, \"" + fieldName + "\", \"" + elementName + "\", " + genericType.getSimpleName() + ".class);");
            } else {
                System.out.println("        xstream.addImplicitCollection(" + className + ".class, \"" + fieldName + "\");");
            }
        } else {
            System.out.println("        xstream.aliasField(\"" + elementName + "\", " + className + ".class, \"" + fieldName + "\");");
        }
    }


    private String getXmlName(Class clazz) {
        String className = clazz.getName().replaceFirst(".*(\\.|\\$)", "");

        return getXmlName(className);
    }


    private String getXmlName(String className) {
        StringBuffer stringBuffer = new StringBuffer(className);
        StringBuffer name = new StringBuffer();

        name.append(Character.toLowerCase(stringBuffer.charAt(0)));

        for (int i = 1; i < stringBuffer.length(); i++) {
            char c = stringBuffer.charAt(i);
            if (Character.isUpperCase(c)) {
                name.append("-");
                name.append(Character.toLowerCase(c));
            } else {
                name.append(c);
            }
        }

        String s = name.toString();
        return s;
    }

}
