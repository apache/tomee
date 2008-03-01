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
package org.apache.openejb.util;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

public class PojoSerialization implements Serializable {
    private static final byte CLASS = (byte) 50;
    private static final byte FIELD = (byte) 51;
    private static final byte DONE = (byte) 52;

    private Object object;

    private static final sun.misc.Unsafe unsafe;

    static {
        unsafe = (sun.misc.Unsafe) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                try {
                    Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
                    field.setAccessible(true);
                    return field.get(null);
                } catch (Exception e) {
                    throw new IllegalStateException("Cannot get sun.misc.Unsafe", e);
                }
            }
        });
    }

    /**
     * Constructor for externalization
     */
    public PojoSerialization() {
    }

    public PojoSerialization(Object object) {
        this.object = object;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        write(out);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        read(in);
    }

    protected Object readResolve() throws ObjectStreamException {
        return object;
    }

    protected void read(ObjectInput in) throws IOException, ClassNotFoundException {
        byte b = in.readByte();
        if (b != CLASS) throw new IOException("Expected 'CLASS' byte " + CLASS + ", got: " + b);

        Class clazz = (Class) in.readObject();

        Object object;
        try {
            object = unsafe.allocateInstance(clazz);
        } catch (Exception e) {
            throw (IOException) new IOException("Cannot construct " + clazz.getName()).initCause(e);
        }

        while (b != DONE) {
            b = in.readByte();
            switch (b) {
                case FIELD: {
                    String fieldName = in.readUTF();
                    Object value = in.readObject();
                    Field field = null;
                    try {
                        field = clazz.getDeclaredField(fieldName);
                    } catch (NoSuchFieldException e) {
                        throw (IOException) new IOException("Cannot find field " + field.getName()).initCause(e);
                    }
                    setValue(field, object, value);
                }
                break;
                case CLASS: {
                    clazz = (Class) in.readObject();
                }
                break;
            }
        }

        this.object = object;
    }

    protected void write(ObjectOutput out) throws IOException {
        List<Class> classes = new ArrayList<Class>();
        Class c = object.getClass();
        while (c != null && !c.equals(Object.class)) {
            classes.add(c);
            c = c.getSuperclass();
        }

        for (Class clazz : classes) {
            out.writeByte(CLASS);
            out.writeObject(clazz);

            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (Modifier.isTransient(field.getModifiers())) continue;
                field.setAccessible(true);
                out.writeByte(FIELD);
                out.writeUTF(field.getName());
                try {
                    out.writeObject(field.get(object));
                } catch (IllegalAccessException e) {
                    throw (IOException) new IOException("Cannot write field " + field.getName()).initCause(e);
                }
            }
        }
        out.writeByte(DONE);
    }

    private void setValue(Field field, Object object, Object value) {
        long offset;
        try {
            offset = unsafe.objectFieldOffset(field);
        } catch (Exception e) {
            throw new IllegalStateException("Failed getting offset for: field=" + field.getName() + "  class=" + field.getDeclaringClass().getName(), e);
        }

        Class type = field.getType();
        if (type.isPrimitive()) {
            if (type.equals(Integer.TYPE)) {
                unsafe.putInt(object, offset, ((Integer) value).intValue());
            } else if (type.equals(Long.TYPE)) {
                unsafe.putLong(object, offset, ((Long) value).longValue());
            } else if (type.equals(Short.TYPE)) {
                unsafe.putShort(object, offset, ((Short) value).shortValue());
            } else if (type.equals(Character.TYPE)) {
                unsafe.putChar(object, offset, ((Character) value).charValue());
            } else if (type.equals(Byte.TYPE)) {
                unsafe.putByte(object, offset, ((Byte) value).byteValue());
            } else if (type.equals(Float.TYPE)) {
                unsafe.putFloat(object, offset, ((Float) value).floatValue());
            } else if (type.equals(Double.TYPE)) {
                unsafe.putDouble(object, offset, ((Double) value).doubleValue());
            } else if (type.equals(Boolean.TYPE)) {
                unsafe.putBoolean(object, offset, ((Boolean) value).booleanValue());
            } else {
                throw new IllegalStateException("Unknown primitive type: " + type.getName());
            }
        } else {
            unsafe.putObject(object, offset, value);
        }
    }

}
