/**
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
package org.apache.openejb.config.rules;

import org.apache.xbean.asm8.AnnotationVisitor;
import org.apache.xbean.asm8.MethodVisitor;
import org.apache.xbean.asm8.Type;
import org.apache.xbean.asm8.shade.commons.EmptyVisitor;

import java.util.HashSet;

public class KeysAnnotationVisitor extends EmptyVisitor {
    private ClassInfo current;
    private MethodInfo currentMethod;
    public static HashSet<ClassInfo> classInfos = new HashSet<ClassInfo>();

    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        current = new ClassInfo(name);
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean arg1) {
        if (desc.contains("RunWith"))
            return super.visitAnnotation(desc, arg1);
        if (desc.contains("Keys")) {
            current.methuds.add(currentMethod);
            return super.visitAnnotation(desc, arg1);
        }
        return null;
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        currentMethod = new MethodInfo(name);
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    @Override
    public void visit(final String name, final Object value) {
        if ("value".equals(name)) {
            if (value instanceof Type) {
                final Type type = (Type) value;
                final int sort = type.getSort();
                switch (sort) {
                    case Type.OBJECT:
                        if (type.getClassName().equals(ValidationRunner.class.getName())) {
                            classInfos.add(current);
                        }
                        break;
                }
            } else {
                currentMethod.keys.add((String) value);
            }
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String name, final String desc) {
        if (desc.contains("Key")) {
            return super.visitAnnotation(name, desc);
        }
        return null;
    }

    @Override
    public AnnotationVisitor visitArray(final String arg0) {
        return super.visitArray(arg0);
    }

    static class ClassInfo {
        String clazz;
        HashSet<MethodInfo> methuds;

        public ClassInfo(final String clazz) {
            this.clazz = clazz;
            methuds = new HashSet<>();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final ClassInfo other = (ClassInfo) obj;
            if (clazz == null) {
                if (other.clazz != null)
                    return false;
            } else if (!clazz.equals(other.clazz))
                return false;
            return true;
        }
    }

    static class MethodInfo {
        String methud;
        HashSet<String> keys;

        public MethodInfo(final String methud) {
            this.methud = methud;
            keys = new HashSet<>();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((methud == null) ? 0 : methud.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final MethodInfo other = (MethodInfo) obj;
            if (methud == null) {
                if (other.methud != null)
                    return false;
            } else if (!methud.equals(other.methud))
                return false;
            return true;
        }
    }
}
