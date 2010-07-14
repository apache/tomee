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

import java.util.HashSet;

import org.apache.openejb.config.rules.ValidationRunner;
import org.apache.xbean.asm.AnnotationVisitor;
import org.apache.xbean.asm.MethodVisitor;
import org.apache.xbean.asm.Type;
import org.apache.xbean.asm.commons.EmptyVisitor;

public class KeysAnnotationVisitor extends EmptyVisitor {
    private ClassInfo current;;
    private MethodInfo currentMethod;
    public static HashSet<ClassInfo> classInfos = new HashSet<ClassInfo>();

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        current = new ClassInfo(name);
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean arg1) {
        if (desc.contains("RunWith"))
            return this;
        if (desc.contains("Keys")) {
            current.methuds.add(currentMethod);
            return this;
        }
        return null;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        currentMethod = new MethodInfo(name);
        return this;
    }

    public void visit(String name, Object value) {
        if ("value".equals(name)) {
            if (value instanceof Type) {
                Type type = (Type) value;
                int sort = type.getSort();
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

    public AnnotationVisitor visitAnnotation(String name, String desc) {
        if (desc.contains("Key")) {
            return this;
        }
        return null;
    }

    public AnnotationVisitor visitArray(String arg0) {
        return this;
    }

    static class ClassInfo {
        String clazz;
        HashSet<MethodInfo> methuds;

        public ClassInfo(String clazz) {
            this.clazz = clazz;
            methuds = new HashSet<MethodInfo>();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ClassInfo other = (ClassInfo) obj;
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

        public MethodInfo(String methud) {
            this.methud = methud;
            keys = new HashSet<String>();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((methud == null) ? 0 : methud.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            MethodInfo other = (MethodInfo) obj;
            if (methud == null) {
                if (other.methud != null)
                    return false;
            } else if (!methud.equals(other.methud))
                return false;
            return true;
        }
    }
}
