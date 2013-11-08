/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.lib.meta;

import java.io.IOException;

import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Localizer;

import serp.bytecode.lowlevel.ConstantPoolTable;

/**
 * Filter that looks for classes with one of a set of annotations.
 * See JDK 1.5 JVM spec for details on annotation bytecode:<br />
 * java.sun.com/docs/books/vmspec/2nd-edition/ClassFileFormat-final-draft.pdf
 *
 * @author Abe White
 * @nojavadoc
 */
public class ClassAnnotationMetaDataFilter implements MetaDataFilter {

    private final String[] _annos;
    
    private static final Localizer _loc = Localizer.forPackage
        (ClassAnnotationMetaDataFilter.class);
    private Log _log = null;

    /**
     * Constructor; supply annotation to match against.
     */
    public ClassAnnotationMetaDataFilter(Class<?> anno) {
        this(new Class[]{ anno });
    }

    /**
     * Constructor; supply annotations to match against.
     */
    public ClassAnnotationMetaDataFilter(Class<?>[] annos) {
        _annos = new String[annos.length];
        for (int i = 0; i < annos.length; i++)
            _annos[i] = "L" + annos[i].getName().replace('.', '/') + ";";
    }

    public boolean matches(Resource rsrc) throws IOException {
        if (_annos.length == 0 || !rsrc.getName().endsWith(".class"))
            return false;

        try {
            ConstantPoolTable table = new ConstantPoolTable(rsrc.getContent());
            int idx = table.getEndIndex();
            idx += 6; // skip access, cls, super

            // skip interfaces
            int interfaces = table.readUnsignedShort(idx);
            idx += 2 + interfaces * 2;

            // skip fields and methods
            int fields = table.readUnsignedShort(idx);
            idx += 2;
            for (int i = 0; i < fields; i++)
                idx += skipFieldOrMethod(table, idx);
            int methods = table.readUnsignedShort(idx);
            idx += 2;
            for (int i = 0; i < methods; i++)
                idx += skipFieldOrMethod(table, idx);

            // look for annotation attrs
            int attrs = table.readUnsignedShort(idx);
            idx += 2;
            int name;
            for (int i = 0; i < attrs; i++) {
                name = table.readUnsignedShort(idx);
                idx += 2;
                if ("RuntimeVisibleAnnotations".equals(table.readString
                    (table.get(name))))
                    return matchAnnotations(table, idx + 4);
                idx += 4 + table.readInt(idx);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            /*
             * This ArrayIndexOutOfBoundsException indicates an incorrectly
             * formed .class file. We will eat the exception, log a trace
             * message (if a log exists), and return "false" to indicate there
             * was no match.
             */
            Error cfe = new ClassFormatError(rsrc.getName());
            cfe.initCause(e);
            if (_log != null && _log.isTraceEnabled())
                _log.trace(_loc.get("class-arg", rsrc.getName()), cfe);
        }
        return false;
    }

    /**
     * Return whether the given annotations match our candidates.
     */
    private boolean matchAnnotations(ConstantPoolTable table, int idx) {
        int annos = table.readUnsignedShort(idx);
        idx += 2;

        int type;
        int props;
        for (int i = 0; i < annos; i++) {
            type = table.readUnsignedShort(idx);
            idx += 2;
            if (matchAnnotation(table.readString(table.get(type))))
                return true;

            props = table.readUnsignedShort(idx);
            idx += 2;
            for (int j = 0; j < props; j++) {
                idx += 2; // name
                idx += skipAnnotationPropertyValue(table, idx);
            }
        }
        return false;
    }

    /**
     * Return whether the given annotation matches our candidates.
     */
    private boolean matchAnnotation(String name) {
        for (int i = 0; i < _annos.length; i++)
            if (name.equals(_annos[i]))
                return true;
        return false;
    }

    /**
     * Skip an annotation property value, returning the number of bytes skipped.
     */
    private static int skipAnnotationPropertyValue(ConstantPoolTable table,
        int idx) {
        int skipped = 0;
        switch (table.readByte(idx + skipped++)) {
            case 'Z': // bool
            case 'B': // byte
            case 'C': // char
            case 'D': // double
            case 'F': // float
            case 'I': // int
            case 'J': // long
            case 'S': // short
            case 's': // string
            case 'c': // class
                skipped += 2;
                break;
            case 'e': // enum ptr
                skipped += 4;
                break;
            case '[': // array
                int size = table.readUnsignedShort(idx + skipped);
                skipped += 2;
                for (int i = 0; i < size; i++)
                    skipped +=
                        skipAnnotationPropertyValue(table, idx + skipped);
                break;
            case '@': // anno
                skipped += 2; // type
                int props = table.readUnsignedShort(idx + skipped);
                skipped += 2;
                for (int j = 0; j < props; j++) {
                    skipped += 2; // name
                    skipped +=
                        skipAnnotationPropertyValue(table, idx + skipped);
                }
                break;
        }
        return skipped;
    }

    /**
     * Skip the current field or method, returning the number of bytes skipped.
     */
    private static int skipFieldOrMethod(ConstantPoolTable table, int idx) {
        int attrs = table.readUnsignedShort(idx + 6);
        int skipped = 8;
        int len;
        for (int i = 0; i < attrs; i++) {
            len = table.readInt(idx + skipped + 2);
            skipped += 6 + len;
        }
        return skipped;
    }

    public Log getLog() {
        return _log;
    }

    public void setLog(Log _log) {
        this._log = _log;
    }
}
