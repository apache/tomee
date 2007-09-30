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
package org.apache.openejb.config;

import org.apache.openejb.OpenEJBException;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.EmptyVisitor;

import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceProperty;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class PersistenceContextAnnFactory {
    private static boolean useAsm;
    static {
        boolean isPersistenceContextAnnotationValid = false;
        try {
            // Tomcat persistence context class is missing the properties method
            Class<?> persistenceContextClass = Class.forName("javax.persistence.PersistenceContext");
            persistenceContextClass.getMethod("properties", (Class[]) null);
            isPersistenceContextAnnotationValid = true;
        } catch (Exception e) {
        }
        useAsm = !isPersistenceContextAnnotationValid;
    }

    public Map<String, AsmPersistenceContext> contexts = new HashMap<String, AsmPersistenceContext>();
    private final Set<String> processed = new HashSet<String>();

    public void addAnnotations(Class c) throws OpenEJBException {
        if (!useAsm) return;
        if (processed.contains(c.getName())) return;

        try {
            URL u = c.getResource("/" + c.getName().replace('.', '/') + ".class");
            ClassReader r = new ClassReader(u.openStream());
            r.accept(new PersistenceContextReader(), true);
        } catch (IOException e) {
            throw new OpenEJBException("Unable to read class " + c.getName());
        }

        processed.add(c.getName());
    }

    public PersistenceContextAnn create(PersistenceContext persistenceContext, AnnotationDeployer.Member member) throws OpenEJBException {
        if (useAsm) {
            if (member != null) {
                addAnnotations(member.getDeclaringClass());
            }

            String name = persistenceContext.name();
            if (name == null || name.equals("")) {
                name = (member == null) ? null : member.getDeclaringClass().getName() + "/" + member.getName();
            }

            AsmPersistenceContext asmPersistenceContext = contexts.get(name);
            if (asmPersistenceContext == null) {
                throw new NullPointerException("PersistenceContext " + name + " not found");
            }
            return asmPersistenceContext;
        } else {
            return new DirectPersistenceContext(persistenceContext);
        }
    }

    private static class DirectPersistenceContext implements PersistenceContextAnn {
        private final PersistenceContext persistenceContext;


        public DirectPersistenceContext(PersistenceContext persistenceContext) {
            if (persistenceContext == null) throw new NullPointerException("persistenceContext is null");
            this.persistenceContext = persistenceContext;
        }

        public String name() {
            return persistenceContext.name();
        }

        public String unitName() {
            return persistenceContext.unitName();
        }

        public String type() {
            if (persistenceContext.type() == null) return null;
            return persistenceContext.type().toString();
        }

        public Map<String, String> properties() {
            Map<String, String> properties = new LinkedHashMap<String, String>();
            for (PersistenceProperty property : persistenceContext.properties()) {
                properties.put(property.name(), property.value());
            }
            return properties;
        }

        public String toString() {
            return persistenceContext.toString();
        }
    }

    private static class AsmPersistenceContext implements PersistenceContextAnn {
        public String name;
        public String unitName;
        public String type;
        public final Map<String, String> properties = new LinkedHashMap<String, String>();

        public String name() {
            return name;
        }

        public String unitName() {
            return unitName;
        }

        public String type() {
            return type;
        }

        public Map<String, String> properties() {
            return properties;
        }

        public String toString() {
            return "@PersistenceContext(name = \"" + name + "\", unitName = \"" + unitName + "\", type = PersistenceContextType." + type + ", " + properties + ")";
        }
    }

    private class PersistenceContextReader extends EmptyVisitor {
        private String className;
        private String currentName;

        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            className = name.replace("/", ".");
            super.visit(version, access, name, signature, superName, interfaces);
        }

        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            currentName = name;
            return super.visitField(access, name, desc, signature, value);
        }

        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            currentName = name;

            // setFoo -> foo
            if (currentName.startsWith("set")) {
                currentName = currentName.substring(3);
            }
            if (currentName.length() > 0) {
                currentName = Character.toLowerCase(currentName.charAt(0)) + currentName.substring(1);
            }

            return super.visitMethod(access, name, desc, signature, exceptions);
        }

        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return visitAnnotation(null, desc);
        }

        public AnnotationVisitor visitAnnotation(String name, String desc) {
            if ("Ljavax/persistence/PersistenceContext;".equals(desc)) {
                PersistenceContextVisitor visitor = new PersistenceContextVisitor(className, currentName);
                return visitor;
            } else if ("Ljavax/persistence/PersistenceContexts;".equals(desc)) {
                return this;
            }
            return new EmptyVisitor();
        }

        public AnnotationVisitor visitParameterAnnotation(int i, String string, boolean b) {
            return new EmptyVisitor();
        }

        public AnnotationVisitor visitAnnotationDefault() {
            return new EmptyVisitor();
        }

        public AnnotationVisitor visitArray(String string) {
            return this;
        }
    }

    private class PersistenceContextVisitor implements AnnotationVisitor {
        private AsmPersistenceContext persistenceContext = new AsmPersistenceContext();

        public PersistenceContextVisitor(String className, String memberName) {
            persistenceContext.name = className + "/" + memberName;
        }

        public void visit(String name, Object value) {
            setValue(name, value == null ? null : value.toString());
        }

        public void visitEnum(String name, String type, String value) {
            setValue(name, value == null ? null : value.toString());
        }

        public AnnotationVisitor visitAnnotation(String name, String desc) {
            setValue(name, desc);
            return null;
        }

        private void setValue(String name, String value) {
            if ("name".equals(name)) {
                persistenceContext.name = value;
            } else if ("unitName".equals(name)) {
                persistenceContext.unitName = value;
            } else if ("type".equals(name)) {
                persistenceContext.type = value;
            }
        }

        public AnnotationVisitor visitArray(String string) {
            return new EmptyVisitor() {
                private String name;
                private String value;

                public void visit(String n, Object v) {
                    if ("name".equals(n)) {
                        name = (v == null ? null : v.toString());
                    } else if ("value".equals(n)) {
                        value = (v == null ? null : v.toString());
                    }
                }

                public void visitEnd() {
                    persistenceContext.properties.put(name, value);
                }
            };
        }

        public void visitEnd() {
            contexts.put(persistenceContext.name, persistenceContext);
        }
    }
}
