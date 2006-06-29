/**
 *
 * Copyright 2006 The Apache Software Foundation
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
package org.openejb.csm;

import org.apache.xbean.finder.ResourceFinder;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.openejb.jee.common.AbstractEjbRef;
import org.openejb.jee.common.JndiEnvironmentRef;
import org.openejb.jee.ejbjar.EjbJar;
import org.openejb.jee.ejbjar.Entity;
import org.openejb.jee.ejbjar.Session;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.math.BigInteger;

/**
 * @version $Revision$ $Date$
 */
public class XStreamAliasTool {


    private MappedItemVisitor visitor = new PrintVisitor();
    private List seen = new ArrayList();
    private static final String JAVAEE = "http://java.sun.com/xml/ns/javaee";
    private SchemaTypeSystem schema;
    private Map typeAliases = new HashMap();

    public XStreamAliasTool() throws Exception {
        seen.add(Object.class);
        seen.add(String.class);
        seen.add(QName.class);
        seen.add(Integer.class);
        seen.add(Integer.TYPE);
        seen.add(Boolean.TYPE);
        seen.add(Boolean.class);
        seen.add(Enum.class);
        schema = getSchema();
    }

    public static void main(String[] args) throws Exception {
        XStreamAliasTool tool = new XStreamAliasTool();
        tool.aliasType(Entity.class, "entity-beanType");
        tool.aliasType(Session.class, "session-beanType");
//        tool.aliasType(EnterpriseBean.class, "session-beanType");
        tool.aliasType(JndiEnvironmentRef.class, "ejb-refType");
        tool.aliasType(AbstractEjbRef.class, "ejb-refType");

        tool.printClass(EjbJar.class, "ejb-jar");

//        tool.printClass(Entity.class);
//        tool.printClass(Session.class);
//        tool.printClass(MessageDrivenBean.class);
    }

    public void aliasType(Class jType, String xType) {
        typeAliases.put(jType, xType);
        typeAliases.put(xType, jType);
    }

    public SchemaTypeSystem getSchema() throws Exception {
        List schemaList = new ArrayList();
        schemaList.add("META-INF/schema/xml.xsd");
        schemaList.add("META-INF/schema/javaee_web_services_client_1_2.xsd");
        schemaList.add("META-INF/schema/javaee_5.xsd");
//        schemaList.add("META-INF/schema/application-client_5.xsd");
//        schemaList.add("META-INF/schema/application_5.xsd");
        schemaList.add("META-INF/schema/ejb-jar_3_0.xsd");
//        schemaList.add("META-INF/schema/javaee_web_services_1_2.xsd");
//        schemaList.add("META-INF/schema/jsp_2_1.xsd");
//        schemaList.add("META-INF/schema/web-app_2_5.xsd");
//        schemaList.add("META-INF/schema/web-facesconfig_1_2.xsd");
//        schemaList.add("META-INF/schema/web-jsptaglibrary_2_1.xsd");

        XmlObject[] schemas = new XmlObject[schemaList.size()];

        Collection errors = new ArrayList();
        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setErrorListener(errors);

        for (int i = 0; i < schemaList.size(); i++) {
            String schemaFileName = (String) schemaList.get(i);
            InputStream is = XStreamAliasTool.class.getClassLoader().getResourceAsStream(schemaFileName);
            if (is == null) {
                throw new RuntimeException("Could not locate soap encoding schema");
            }
            schemas[i] = SchemaDocument.Factory.parse(is, xmlOptions);
            if (errors.size() != 0) {
                throw new XmlException(errors.toArray().toString());
            }
        }

        return XmlBeans.compileXsd(schemas, XmlBeans.getBuiltinTypeSystem(), xmlOptions);
    }

    private Stack<Class> classes = new Stack<Class>();
    private Stack<Element> elements = new Stack<Element>();

    public void printClass(Class clazz, String elementName) {
        QName qName = qname(elementName);
        SchemaGlobalElement xsdElement = schema.findElement(qName);
        if (xsdElement != null) {
            SchemaType type = xsdElement.getType();
            printClass(new MappedClass(clazz, new RootParticle(type, qName)));
        } else {
            printClass(clazz);
            System.out.println("Element not found " + elementName + " for class " + clazz.getName());
        }

    }

    public void printClass(Class clazz) {
        if (clazz == null || seen.contains(clazz)) {
            return;
        }

        SchemaType xmlType = getXmlType(clazz);
        RootParticle particle = new RootParticle(xmlType, xmlType.getName());
        printClass(new MappedClass(clazz, particle));
    }

    private void printClass(MappedClass mappedItem) {
        visitor.visit(mappedItem);
        Class clazz = mappedItem.getClazz();

        Class superclass = clazz.getSuperclass();
        if (superclass != null && superclass != Object.class){
            RootParticle particle = new RootParticle(mappedItem.getType(), qname(""));
            printClass(new MappedClass(superclass, particle));
        }

        if (seen.contains(clazz)) {
            return;
        }
        seen.add(clazz);

        if (Enum.class.isAssignableFrom(clazz)) {
            return;
        }

        String xmlName = mappedItem.getElementName();
        String className = clazz.getName().replaceFirst(".*\\.", "");
        className = className.replace('$', '.');
//        System.out.println("\n        //--------  " + clazz.getSimpleName() + " -------\n");
//        System.out.println("        xstream.alias(\"" + xmlName + "\", " + className + ".class);");


        FieldMatcher fieldMatcher = new FieldMatcher(mappedItem.getParticle(), clazz);

        MatchSet matchSet = fieldMatcher.match();
        List<MappedField> fields = matchSet.getMatched();

        validateMatches(fields);

        reportUnmatched(matchSet);

        processesFields(fields, clazz);
    }

    private void processesFields(List<MappedField> fields, Class clazz) {
        for (int i = 0; i < fields.size(); i++) {
            printField(fields.get(i), clazz);
        }

        for (int i = 0; i < fields.size(); i++) {
            printClass(fields.get(i));
        }
    }

    private void validateMatches(List<MappedField> fields) {
        for (int i = 0; i < fields.size(); i++) {
            MappedField mappedField = fields.get(i);
            if (mappedField.isJavaCollection() && !mappedField.isXmlCollection()){
                SchemaParticle particle = mappedField.getParticle();
                SchemaParticle model = particle.getType().getContentModel();
                if (particle.getIntMaxOccurs() == 1 && model == null){
//                    System.out.println("        // not assignable to a list "+ mappedField);
                } else if (particle.getIntMaxOccurs() == 1 && model.getParticleType() == SchemaParticle.SEQUENCE){
//                    System.out.println("        // vague list match "+ mappedField);
                } else {
//                    System.out.println("        // Haze: Java collection found and no clear xml collection: "+mappedField);
                }
            }
        }
    }

    private void printClass(MappedField mappedItem) {
        visitor.visit(mappedItem);

        // if this is a collection with a CHOICE of
        // possible subtypes, we'll need to find
        // the matching java class for each and verify
        // that it is assignable to the generic type in
        // the collection.  then we print each one.
        if (mappedItem.isExplicitCollection() && mappedItem.getParticle().getType().getContentModel().getParticleType() == SchemaParticle.CHOICE) {
            SchemaParticle model = mappedItem.getParticle().getType().getContentModel();
            SchemaParticle[] children = model.getParticleChildren();
            for (int i = 0; i < children.length; i++) {
                SchemaParticle child = children[i];
                String name = child.getName().getLocalPart();
                SchemaType type = child.getType();
                Class subType = findJavaClass(mappedItem, name, type);
                if (subType != null) {
                    MappedClass mappedClass = new MappedClass(subType, child);
                    printClass(mappedClass);
                }
            }
        }

        if (mappedItem.isExplicitCollection() && mappedItem.getParticle().getType().getContentModel().getParticleType() == SchemaParticle.ELEMENT) {
            SchemaParticle child = mappedItem.getParticle().getType().getContentModel();
            String name = child.getName().getLocalPart();
            SchemaType type = child.getType();
            Class subType = findJavaClass(mappedItem, name, type);
            if (subType != null) {
                MappedClass mappedClass = new MappedClass(subType, child);
                printClass(mappedClass);
            }
        }


        if (mappedItem.isJavaCollection() && !mappedItem.isXmlCollection()){
//            System.out.println("        // Haze: Java collection found and no clear xml collection: "+mappedItem);
            Class genericType = mappedItem.getGenericType();
            SchemaParticle model = mappedItem.getType().getContentModel();
            if (model != null && (model.getParticleType() == SchemaParticle.SEQUENCE || model.getParticleType() == SchemaParticle.CHOICE)){
                SchemaParticle[] children = model.getParticleChildren();
                // Find the child that matches the most
                MatchSet bestMatch = null;
                for (int i = 0; i < children.length; i++)
                {
                    SchemaParticle child = children[i];
                    FieldMatcher matcher = new FieldMatcher(child, genericType);
                    MatchSet matchSet = matcher.match();

                    if (bestMatch == null || matchSet.getMatched().size() > bestMatch.getMatched().size()){
                        bestMatch = matchSet;
                    } else if (matchSet.getMatched().size() == bestMatch.getMatched().size()){
                        if (matchSet.getUnmatchedFields().size() < bestMatch.getUnmatchedFields().size()) {
                            bestMatch = matchSet;
                        }
                    }
                }

                if (bestMatch != null){
                    printClass(new MappedClass(bestMatch.getClazz(), bestMatch.getParticle()));
                }
            }


        } else {
            Class clazz = mappedItem.getGenericType();
            if (seen.contains(clazz)) {
                return;
            }

            SchemaParticle particle = mappedItem.getParticle();
            printClass(clazz.getSuperclass());
            seen.add(clazz);

            if (Enum.class.isAssignableFrom(clazz)) {
                return;
            }

            String xmlName;
            if (mappedItem.isCollection()) {
                xmlName = mappedItem.getCollectionItemXmlName();
            } else {
                xmlName = particle.getName().getLocalPart();
            }

            if (xmlName != null) {
                String className = clazz.getName().replaceFirst(".*\\.", "");
                className = className.replace('$', '.');
    //            System.out.println("\n        //--------  " + clazz.getSimpleName() + " -------\n");
    //            System.out.println("        xstream.alias(\"" + xmlName + "\", " + className + ".class);");
            }


            FieldMatcher fieldMatcher = new FieldMatcher(mappedItem.getParticle(), clazz);

            MatchSet matchSet = fieldMatcher.match();
            List<MappedField> fields = matchSet.getMatched();

            validateMatches(fields);

            reportUnmatched(matchSet);

            processesFields(fields, clazz);
        }

    }

    private FieldMatcher matcher(MappedField mappedField){

        SchemaParticle particle = mappedField.getParticle();
        String particleName = particle.getName().getLocalPart();
        SchemaType type = particle.getType();
        String typeName = type.getName().getLocalPart();
        SchemaParticle model = type.getContentModel();
        Class genericType = mappedField.getGenericType();
        Class fieldType = mappedField.getField().getType();
        return new FieldMatcher(particle, genericType);
    }

    private void reportUnmatched(MatchSet matchSet) {
        Map<String, Field> unmatchedFields = matchSet.getUnmatchedFields();
        for (Iterator iterator = unmatchedFields.values().iterator(); iterator.hasNext();) {
            Field field = (Field) iterator.next();
//            System.out.println("        // F: "+field.getName());
        }

        Map<String, SchemaParticle> unmatchedParticles = matchSet.getUnmatchedParticles();
        for (Iterator iterator = unmatchedParticles.keySet().iterator(); iterator.hasNext();) {
            String name =  (String) iterator.next();
//            System.out.println("        // X: "+name);
        }
    }

    private Class findJavaClass(MappedField mappedField, String name, SchemaType type) {
        Class genericType = mappedField.getGenericType();

        Class clazz = findClass(genericType, name);
        for (int i = 0; i < seen.size() && clazz == null; i++) {
            Class seenClass = (Class) seen.get(i);
            clazz = findClass(seenClass, name);
        }

        if (genericType.isAssignableFrom(clazz)){
            return clazz;
        }
        return null;
    }

    private Class findClass(Class genericType, String name) {
        ClassLoader classLoader = genericType.getClassLoader();
        if (classLoader == null) {
            return null;
        }
        String packageName = genericType.getPackage().getName();
        String shortClassName = NameConverter.getJavaClassName(name);
        String className = packageName + "." + shortClassName;
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            try {
                ResourceFinder resourceFinder = new ResourceFinder("");
                Map<String, URL> resourcesMap = resourceFinder.getResourcesMap(packageName.replace(".", "/"));
                ClassMatcher matcher = new ClassMatcher();
                ClassMatcher.Entry entry = matcher.match(shortClassName, resourcesMap, ".class");
                if (entry != null){
                    shortClassName = entry.getKey().replaceAll("\\.class$", "");
                    try {
                        return classLoader.loadClass(packageName +"."+shortClassName);
                    } catch (ClassNotFoundException e1) {
                        e1.printStackTrace();
                        return null;
                    }
                }
            } catch (IOException e1) {
                e1.printStackTrace();
                return null;
            }
            return null;
        }
    }

    private void printField(MappedField mappedField, Class clazz) {
        Field field = mappedField.getField();
        String className = clazz.getName().replaceFirst(".*\\.", "");
        className = className.replace('$', '.');


        String elementName = mappedField.getParticle().getName().getLocalPart();

        String fieldName = field.getName();


//        if (mappedField.isImplicitCollection()) {
//            Class genericType = mappedField.getGenericType();
//            if (String.class.isAssignableFrom(genericType)) {
//                System.out.println("        xstream.addImplicitCollection(" + className + ".class, \"" + fieldName + "\", \"" + elementName + "\", " + genericType.getSimpleName() + ".class);");
//            } else {
//                System.out.println("        xstream.addImplicitCollection(" + className + ".class, \"" + fieldName + "\");");
//            }
//        } else {
//            System.out.println("        xstream.aliasField(\"" + elementName + "\", " + className + ".class, \"" + fieldName + "\");");
//        }
    }

    private Stack<FieldMatcher> matchers = new Stack<FieldMatcher>();

    private SchemaType getXmlType(Class clazz) {
        String alias = (String) typeAliases.get(clazz);
        String name = alias != null ? alias : NameConverter.getXmlName(clazz);

        SchemaType type = null;

        // Look for a top level element
        SchemaGlobalElement xsdElement = schema.findElement(qname(name));
        if (xsdElement != null) {
            return xsdElement.getType();
        }

        // Look for a top level type
        type = schema.findType(qname(name));
        if (type != null) return type;

        type = schema.findType(qname(name + "Type"));
        if (type != null) return type;

        // Look for a sub element
        Element element = elements.peek();
        SchemaType parentType = element.getType();

        Map<String, SchemaParticle> children = FieldMatcher.collectChildren(parentType);
        SchemaParticle particle = children.get(name);

        if (particle != null) {
            return particle.getType();
        }

//        System.out.println("        // unmatched "+ clazz.getName());
        return null;
    }

    private QName qname(String name) {
        return new QName(JAVAEE, name);
    }
}
