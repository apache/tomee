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
 * -------------------------------------------------------------------------
 *  * Copyright (c) 2000-2005 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.

 *
 */
package org.apache.openejb;

import org.apache.xbean.asm8.AnnotationVisitor;
import org.apache.xbean.asm8.Attribute;
import org.apache.xbean.asm8.FieldVisitor;
import org.apache.xbean.asm8.Label;
import org.apache.xbean.asm8.MethodVisitor;
import org.apache.xbean.asm8.Opcodes;
import org.apache.xbean.asm8.Type;
import org.apache.xbean.asm8.shade.commons.EmptyVisitor;
import org.apache.xbean.asm8.signature.SignatureReader;
import org.apache.xbean.asm8.signature.SignatureVisitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class DependencyVisitor extends EmptyVisitor {
    Set<String> packages = new HashSet<>();

    Map<String, Map<String, Integer>> groups = new HashMap<>();

    Map<String, Integer> current;

    public Map<String, Map<String, Integer>> getGlobals() {
        return groups;
    }

    public Set<String> getPackages() {
        return packages;
    }

    // ClassVisitor

    public void visit(
        final int version,
        final int access,
        final String name,
        final String signature,
        final String superName,
        final String[] interfaces) {

        if (name.startsWith("org/apache/openejb/OpenEjbContainer")) {
            current = new HashMap<>();
        } else {
            final String p = getGroupKey(name);
            current = groups.computeIfAbsent(p, k -> new HashMap<>());

            if (signature == null) {
                addName(superName);
                addNames(interfaces);
            } else {
                addSignature(signature);
            }
        }

    }

    public AnnotationVisitor visitAnnotation(
        final String desc,
        final boolean visible) {
        addDesc(desc);
        return super.visitAnnotation(desc, visible);
    }

    public void visitAttribute(final Attribute attr) {
    }

    public FieldVisitor visitField(
        final int access,
        final String name,
        final String desc,
        final String signature,
        final Object value) {
        if (signature == null) {
            addDesc(desc);
        } else {
            addTypeSignature(signature);
        }
        if (value instanceof Type) {
            addType((Type) value);
        }
        return super.visitField(access, name, desc, signature, value);
    }

    public MethodVisitor visitMethod(
        final int access,
        final String name,
        final String desc,
        final String signature,
        final String[] exceptions) {
        if (signature == null) {
            addMethodDesc(desc);
        } else {
            addSignature(signature);
        }
        addNames(exceptions);
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    public void visitInnerClass(
        final String name,
        final String outerName,
        final String innerName,
        final int access) {
        // addName( outerName);
        // addName( innerName);
    }

    public void visitOuterClass(
        final String owner,
        final String name,
        final String desc) {
        // addName(owner);
        // addMethodDesc(desc);
    }

    // MethodVisitor

    public AnnotationVisitor visitParameterAnnotation(
        final int parameter,
        final String desc,
        final boolean visible) {
        addDesc(desc);
        return super.visitParameterAnnotation(parameter, desc, visible);
    }

    public void visitTypeInsn(final int opcode, final String desc) {
        if (desc.charAt(0) == '[') {
            addDesc(desc);
        } else {
            addName(desc);
        }
    }

    public void visitFieldInsn(
        final int opcode,
        final String owner,
        final String name,
        final String desc) {
        addName(owner);
        addDesc(desc);
    }

    public void visitMethodInsn(
        final int opcode,
        final String owner,
        final String name,
        final String desc) {
        addName(owner);
        addMethodDesc(desc);
    }

    public void visitMethodInsn(
        final int opcode,
        final String owner,
        final String name,
        final String desc,
        final boolean itf) {
        addName(owner);
        addMethodDesc(desc);
    }

    public void visitLdcInsn(final Object cst) {
        if (cst instanceof Type) {
            addType((Type) cst);
        }
    }

    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        addDesc(desc);
    }

    public void visitLocalVariable(
        final String name,
        final String desc,
        final String signature,
        final Label start,
        final Label end,
        final int index) {
        addTypeSignature(signature);
    }

    public void visitCode() {
    }

    public void visitFrame(
        final int type,
        final int nLocal,
        final Object[] local,
        final int nStack,
        final Object[] stack) {
    }

    public void visitInsn(final int opcode) {
    }

    public void visitIntInsn(final int opcode, final int operand) {
    }

    public void visitVarInsn(final int opcode, final int var) {
    }

    public void visitJumpInsn(final int opcode, final Label label) {
    }

    public void visitLabel(final Label label) {
    }

    public void visitIincInsn(final int var, final int increment) {
    }

    public void visitTableSwitchInsn(
        final int min,
        final int max,
        final Label dflt,
        final Label[] labels) {
    }

    public void visitLookupSwitchInsn(
        final Label dflt,
        final int[] keys,
        final Label[] labels) {
    }

    public void visitTryCatchBlock(
        final Label start,
        final Label end,
        final Label handler,
        final String type) {
        addName(type);
    }

    public void visitLineNumber(final int line, final Label start) {
    }

    public void visitMaxs(final int maxStack, final int maxLocals) {
    }

    // AnnotationVisitor

    public void visit(final String name, final Object value) {
        if (value instanceof Type) {
            addType((Type) value);
        }
    }

    public void visitEnum(
        final String name,
        final String desc,
        final String value) {
        addDesc(desc);
    }

    public AnnotationVisitor visitAnnotation(
        final String name,
        final String desc) {
        addDesc(desc);
        return super.visitAnnotation(name, desc);
    }

    // SignatureVisitor

    public void visitBaseType(final char descriptor) {
    }

    public void visitTypeVariable(final String name) {
        // TODO verify
    }

    public void visitClassType(final String name) {
        addName(name);
    }

    public void visitInnerClassType(final String name) {
        addName(name);
    }

    public void visitTypeArgument() {
    }

    // ---------------------------------------------

    private String getGroupKey(String name) {
        final int n = name.lastIndexOf('/');
        if (n > -1) {
            name = name.substring(0, n);
        }
        name = name.replace('/', '.');
        packages.add(name);
        return name;
    }

    private void addName(final String name) {
        if (name == null) {
            return;
        }
        final String p = getGroupKey(name);
        if (current.containsKey(p)) {
            current.put(p, current.get(p) + 1);
        } else {
            current.put(p, 1);
        }
    }

    private void addNames(final String[] names) {
        for (int i = 0; names != null && i < names.length; i++) {
            addName(names[i]);
        }
    }

    private void addDesc(final String desc) {
        addType(Type.getType(desc));
    }

    private void addMethodDesc(final String desc) {
        addType(Type.getReturnType(desc));
        final Type[] types = Type.getArgumentTypes(desc);
        for (Type type : types) {
            addType(type);
        }
    }

    private void addType(final Type t) {
        switch (t.getSort()) {
            case Type.ARRAY:
                addType(t.getElementType());
                break;
            case Type.OBJECT:
                addName(t.getClassName().replace('.', '/'));
                break;
        }
    }

    private void addSignature(final String signature) {
        if (signature != null) {
            new SignatureReader(signature).accept(new SignatureAdapter(this));
        }
    }

    private void addTypeSignature(final String signature) {
        if (signature != null) {
            new SignatureReader(signature).acceptType(new SignatureAdapter(this));
        }
    }

    private class SignatureAdapter extends SignatureVisitor {
        private final DependencyVisitor delegate;

        public SignatureAdapter(final DependencyVisitor dependencyVisitor) {
            super(Opcodes.ASM7);
            delegate = dependencyVisitor;
        }

        @Override
        public void visitFormalTypeParameter(final String name) {
            //delegate.visitFormalTypeParameter(name);
        }

        @Override
        public SignatureVisitor visitClassBound() {
            return this; //delegate.visitClassBound();
        }

        @Override
        public SignatureVisitor visitInterfaceBound() {
            return this; //delegate.visitInterfaceBound();
        }

        @Override
        public SignatureVisitor visitSuperclass() {
            return this; //delegate.visitSuperclass();
        }

        @Override
        public SignatureVisitor visitInterface() {
            return this; //delegate.visitInterface();
        }

        @Override
        public SignatureVisitor visitParameterType() {
            return this; //delegate.visitParameterType();
        }

        @Override
        public SignatureVisitor visitReturnType() {
            return this; //delegate.visitReturnType();
        }

        @Override
        public SignatureVisitor visitExceptionType() {
            return this; //delegate.visitExceptionType();
        }

        @Override
        public void visitBaseType(final char descriptor) {
            delegate.visitBaseType(descriptor);
        }

        @Override
        public void visitTypeVariable(final String name) {
            delegate.visitTypeVariable(name);
        }

        @Override
        public SignatureVisitor visitArrayType() {
            return this; //delegate.visitArrayType();
        }

        @Override
        public void visitClassType(final String name) {
            delegate.visitClassType(name);
        }

        @Override
        public void visitInnerClassType(final String name) {
            delegate.visitInnerClassType(name);
        }

        @Override
        public void visitTypeArgument() {
            delegate.visitTypeArgument();
        }

        @Override
        public SignatureVisitor visitTypeArgument(final char wildcard) {
            return this; //delegate.visitTypeArgument(wildcard);
        }

        @Override
        public void visitEnd() {
            delegate.visitEnd();
        }
    }
}
