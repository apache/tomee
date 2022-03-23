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

package org.apache.openejb.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.loader.IO;
import org.apache.xbean.asm9.AnnotationVisitor;
import org.apache.xbean.asm9.ClassReader;
import org.apache.xbean.asm9.FieldVisitor;
import org.apache.xbean.asm9.MethodVisitor;
import org.apache.xbean.asm9.Opcodes;
import org.apache.xbean.asm9.shade.commons.EmptyVisitor;

import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceProperty;
import jakarta.persistence.SynchronizationType;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class PersistenceContextAnnFactory {

    @SuppressWarnings("FieldMayBeFinal") // This gets set by reflection
    private static boolean useAsm;

    static {
        boolean isPersistenceContextAnnotationValid = false;
        try {
            // Tomcat persistence context class is missing the properties method
            final Class<?> persistenceContextClass = Class.forName("jakarta.persistence.PersistenceContext");
            persistenceContextClass.getMethod("properties", (Class[]) null);
            isPersistenceContextAnnotationValid = true;
        } catch (final Exception e) {
            // no-op
        }
        useAsm = !isPersistenceContextAnnotationValid;
    }

    public Map<String, AsmPersistenceContext> contexts = new HashMap<>();
    private final Set<String> processed = new HashSet<>();

    public void addAnnotations(final Class c) throws OpenEJBException {
        if (!useAsm) {
            return;
        }
        if (processed.contains(c.getName())) {
            return;
        }

        try {
            final URL u = c.getResource("/" + c.getName().replace('.', '/') + ".class");
            final ClassReader r = new ClassReader(IO.read(u));
            r.accept(new PersistenceContextReader(contexts), ClassReader.SKIP_DEBUG);
        } catch (final IOException e) {
            throw new OpenEJBException("Unable to read class " + c.getName());
        }

        processed.add(c.getName());
    }

    public PersistenceContextAnn create(final PersistenceContext persistenceContext, final AnnotationDeployer.Member member) throws OpenEJBException {
        if (useAsm) {
            if (member != null) {
                addAnnotations(member.getDeclaringClass());
            }

            String name = persistenceContext.name();
            if (name == null || name.isEmpty()) {
                name = member == null ? null : member.getDeclaringClass().getName() + "/" + member.getName();
            }

            final AsmPersistenceContext asmPersistenceContext = contexts.get(name);
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


        public DirectPersistenceContext(final PersistenceContext persistenceContext) {
            if (persistenceContext == null) {
                throw new NullPointerException("persistenceContext is null");
            }
            this.persistenceContext = persistenceContext;
        }

        public String name() {
            return persistenceContext.name();
        }

        @Override
        public String synchronization() {
            return persistenceContext.synchronization().name();
        }

        public String unitName() {
            return persistenceContext.unitName();
        }

        public String type() {
            if (persistenceContext.type() == null) {
                return null;
            }
            return persistenceContext.type().toString();
        }

        public Map<String, String> properties() {
            final Map<String, String> properties = new LinkedHashMap<>();
            for (final PersistenceProperty property : persistenceContext.properties()) {
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
        public String synchronization = SynchronizationType.SYNCHRONIZED.name(); // default
        public final Map<String, String> properties = new LinkedHashMap<>();

        public String name() {
            return name;
        }

        @Override
        public String synchronization() {
            return synchronization;
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

    private static final class PersistenceContextReader extends EmptyVisitor {
        private String className;
        private String currentName;
        private final Map<String, AsmPersistenceContext> contexts;

        private PersistenceContextReader(final Map<String, AsmPersistenceContext> contexts) {
            this.contexts = contexts;
        }

        public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
            className = name.replace("/", ".");
            super.visit(version, access, name, signature, superName, interfaces);
        }

        public FieldVisitor visitField(final int access, final String name, final String desc, final String signature, final Object value) {
            currentName = name;
            return super.visitField(access, name, desc, signature, value);
        }

        public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
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

        public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
            return visitAnnotation(null, desc);
        }

        @Override
        public AnnotationVisitor visitAnnotation(final String name, final String desc) {
            if ("Ljavax/persistence/PersistenceContext;".equals(desc)) {
                return new PersistenceContextVisitor(className, currentName, contexts);
            } else if ("Ljavax/persistence/PersistenceContexts;".equals(desc)) {
                return super.visitAnnotation(name, desc);
            }
            return new EmptyVisitor().annotationVisitor();
        }

        @Override
        public AnnotationVisitor visitArray(final String string) {
            return annotationVisitor();
        }

        @Override
        public AnnotationVisitor visitMethodParameterAnnotation(final int i, final String string, final boolean b) {
            return new EmptyVisitor().annotationVisitor();
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(final int i, final String string, final boolean b) {
            return new EmptyVisitor().annotationVisitor();
        }

        @Override
        public AnnotationVisitor visitAnnotationDefault() {
            return new EmptyVisitor().annotationVisitor();
        }
    }

    private static class PersistenceContextVisitor extends AnnotationVisitor {
        private final Map<String, AsmPersistenceContext> contexts;
        private final AsmPersistenceContext persistenceContext = new AsmPersistenceContext();

        public PersistenceContextVisitor(final String className, final String memberName, final Map<String, AsmPersistenceContext> contexts) {
            super(Opcodes.ASM9);
            this.contexts = contexts;
            persistenceContext.name = className + "/" + memberName;
        }

        public void visit(final String name, final Object value) {
            setValue(name, value == null ? null : value.toString());
        }

        public void visitEnum(final String name, final String type, final String value) {
            setValue(name, value == null ? null : value.toString());
        }

        public AnnotationVisitor visitAnnotation(final String name, final String desc) {
            setValue(name, desc);
            return this;
        }

        private void setValue(final String name, final String value) {
            if ("name".equals(name)) {
                persistenceContext.name = value;
            } else if ("unitName".equals(name)) {
                persistenceContext.unitName = value;
            } else if ("type".equals(name)) {
                persistenceContext.type = value;
            } else if ("synchronization".equals(name)) {
                persistenceContext.synchronization = value;
            }
        }

        public AnnotationVisitor visitArray(final String string) {
            return new EmptyVisitor() {
                private String name;
                private String value;

                public void visit(final String n, final Object v) {
                    if ("name".equals(n)) {
                        name = v == null ? null : v.toString();
                    } else if ("value".equals(n)) {
                        value = v == null ? null : v.toString();
                    }
                }

                public void visitEnd() {
                    persistenceContext.properties.put(name, value);
                }
            }.annotationVisitor();
        }

        public void visitEnd() {
            contexts.put(persistenceContext.name, persistenceContext);
        }
    }
}
