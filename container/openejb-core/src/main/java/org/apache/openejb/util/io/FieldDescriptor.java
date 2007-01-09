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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.util.io;

import java.io.IOException;
import java.io.InvalidClassException;
import java.lang.reflect.Field;

public class FieldDescriptor implements java.io.Serializable, Comparable {

    public FieldDescriptor(Field field) {
        this.field = field;
        this.name = field.getName();
        this.type = field.getType();

        if (type.isPrimitive()) {
            if (type == Integer.TYPE) typeCode = 'I';
            else if (type == Byte.TYPE) typeCode = 'B';
            else if (type == Long.TYPE) typeCode = 'J';
            else if (type == Float.TYPE) typeCode = 'F';
            else if (type == Double.TYPE) typeCode = 'D';
            else if (type == Short.TYPE) typeCode = 'S';
            else if (type == Character.TYPE) typeCode = 'C';
            else if (type == Boolean.TYPE) typeCode = 'Z';
            else if (type == Void.TYPE) typeCode = 'V';
        } else if (type.isArray()) {
            typeCode = '[';
            typeString = ClassDescriptor.getSignature(type).toString().intern();
        } else {
            typeCode = 'L';
            StringBuffer buf = new StringBuffer();
            buf.append(typeCode);
            buf.append(type.getName().replace('.', '/'));
            buf.append(';');
            typeString = buf.toString().intern();
        }

    }

    public FieldDescriptor(String name, Class type) {

    }

    protected String typeString;

    public String getTypeString() {
        return typeString;
    }

    /*
     * The name of this field
     */
    protected String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    protected Field field;

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    protected char typeCode;

    public char getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(char typeCode) {
        this.typeCode = typeCode;
    }

    protected Class type;

    public int compareTo(Object o) {
        FieldDescriptor f2 = (FieldDescriptor) o;
        boolean thisprim = (this.typeString == null);
        boolean otherprim = (f2.typeString == null);

        if (thisprim != otherprim) {
            return (thisprim ? -1 : 1);
        }
        return this.name.compareTo(f2.name);
    }

    protected ClassDescriptor classDesc;

    public ClassDescriptor getClassDescriptor() {
        return classDesc;
    }

    public void setClassDescriptor(ClassDescriptor classDesc) {
        this.classDesc = classDesc;
    }

    public void writeDesc(ObjectOutputStream out) throws IOException {
        out.writeByte((int) typeCode);
        out.writeUTF(name);
        if (!type.isPrimitive()) out.writeString(typeString);
    }

    public void write(Object o, ObjectOutputStream out) throws IOException, InvalidClassException {

        if (field == null) throw new InvalidClassException(classDesc.forClass().getName(), "Nonexistent field " + name);
        try {
            switch (typeCode) {
                case 'B':
                    out.writeByte(field.getByte(o));
                    break;
                case 'C':
                    out.writeChar(field.getChar(o));
                    break;
                case 'I':
                    out.writeInt(field.getInt(o));
                    break;
                case 'Z':
                    out.writeBoolean(field.getBoolean(o));
                    break;
                case 'J':
                    out.writeLong(field.getLong(o));
                    break;
                case 'F':
                    out.writeFloat(field.getFloat(o));
                    break;
                case 'D':
                    out.writeDouble(field.getDouble(o));
                    break;
                case 'S':
                    out.writeShort(field.getShort(o));
                    break;
                case '[':
                case 'L':
                    out.writeObject(field.get(o));
                    break;
                default:
                    throw new InvalidClassException(classDesc.forClass().getName());
            }
        }
        catch (IllegalAccessException e) {
            throw new InvalidClassException(classDesc.forClass().getName(), e.getMessage());
        }
    }
}