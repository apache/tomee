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
package org.apache.openejb.util;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.Class;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

    // sun.misc.Unsafe unsafe;
    private static final Object unsafe;
    private static final Method allocateInstance;
    private static final Method objectFieldOffset;
    private static final Method putInt;
    private static final Method putLong;
    private static final Method putShort;
    private static final Method putChar;
    private static final Method putByte;
    private static final Method putFloat;
    private static final Method putDouble;
    private static final Method putBoolean;
    private static final Method putObject;

    static {
        final Class<?> unsafeClass;
        try {
            unsafeClass = AccessController.doPrivileged(new PrivilegedAction<Class<?>>() {
                public Class<?> run() {
                    try {
                        return Thread.currentThread().getContextClassLoader().loadClass("sun.misc.Unsafe");
                    } catch (Exception e) {
                        try {
                            return ClassLoader.getSystemClassLoader().loadClass("sun.misc.Unsafe");
                        } catch (ClassNotFoundException e1) {
                            throw new IllegalStateException("Cannot get sun.misc.Unsafe", e);
                        }
                    }
                }
            });
        } catch (Exception e) {
            throw new IllegalStateException("Cannot get sun.misc.Unsafe class", e);
        }

        unsafe = AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                try {
                    Field field = unsafeClass.getDeclaredField("theUnsafe");
                    field.setAccessible(true);
                    return field.get(null);
                } catch (Exception e) {
                    throw new IllegalStateException("Cannot get sun.misc.Unsafe", e);
                }
            }
        });
        allocateInstance = AccessController.doPrivileged(new PrivilegedAction<Method>() {
            public Method run() {
                try {
                    Method mtd = unsafeClass.getDeclaredMethod("allocateInstance", Class.class);
                    mtd.setAccessible(true);
                    return mtd;
                } catch (Exception e) {
                    throw new IllegalStateException("Cannot get sun.misc.Unsafe.allocateInstance", e);
                }
            }
        });
        objectFieldOffset = AccessController.doPrivileged(new PrivilegedAction<Method>() {
            public Method run() {
                try {
                    Method mtd = unsafeClass.getDeclaredMethod("objectFieldOffset", Field.class);
                    mtd.setAccessible(true);
                    return mtd;
                } catch (Exception e) {
                    throw new IllegalStateException("Cannot get sun.misc.Unsafe.objectFieldOffset", e);
                }
            }
        });
        putInt = AccessController.doPrivileged(new PrivilegedAction<Method>() {
            public Method run() {
                try {
                    Method mtd = unsafeClass.getDeclaredMethod("putInt", Object.class, long.class, int.class);
                    mtd.setAccessible(true);
                    return mtd;
                } catch (Exception e) {
                    throw new IllegalStateException("Cannot get sun.misc.Unsafe.putInt", e);
                }
            }
        });
        putLong = AccessController.doPrivileged(new PrivilegedAction<Method>() {
            public Method run() {
                try {
                    Method mtd = unsafeClass.getDeclaredMethod("putLong", Object.class, long.class, long.class);
                    mtd.setAccessible(true);
                    return mtd;
                } catch (Exception e) {
                    throw new IllegalStateException("Cannot get sun.misc.Unsafe.putLong", e);
                }
            }
        });
        putShort = AccessController.doPrivileged(new PrivilegedAction<Method>() {
            public Method run() {
                try {
                    Method mtd = unsafeClass.getDeclaredMethod("putShort", Object.class, long.class, short.class);
                    mtd.setAccessible(true);
                    return mtd;
                } catch (Exception e) {
                    throw new IllegalStateException("Cannot get sun.misc.Unsafe.putShort", e);
                }
            }
        });
        putChar = AccessController.doPrivileged(new PrivilegedAction<Method>() {
            public Method run() {
                try {
                    Method mtd = unsafeClass.getDeclaredMethod("putChar", Object.class, long.class, char.class);
                    mtd.setAccessible(true);
                    return mtd;
                } catch (Exception e) {
                    throw new IllegalStateException("Cannot get sun.misc.Unsafe.putChar", e);
                }
            }
        });
        putByte = AccessController.doPrivileged(new PrivilegedAction<Method>() {
            public Method run() {
                try {
                    Method mtd = unsafeClass.getDeclaredMethod("putByte", Object.class, long.class, byte.class);
                    mtd.setAccessible(true);
                    return mtd;
                } catch (Exception e) {
                    throw new IllegalStateException("Cannot get sun.misc.Unsafe.putByte", e);
                }
            }
        });
        putFloat = AccessController.doPrivileged(new PrivilegedAction<Method>() {
            public Method run() {
                try {
                    Method mtd = unsafeClass.getDeclaredMethod("putFloat", Object.class, long.class, float.class);
                    mtd.setAccessible(true);
                    return mtd;
                } catch (Exception e) {
                    throw new IllegalStateException("Cannot get sun.misc.Unsafe.putFloat", e);
                }
            }
        });
        putDouble = AccessController.doPrivileged(new PrivilegedAction<Method>() {
            public Method run() {
                try {
                    Method mtd = unsafeClass.getDeclaredMethod("putDouble", Object.class, long.class, double.class);
                    mtd.setAccessible(true);
                    return mtd;
                } catch (Exception e) {
                    throw new IllegalStateException("Cannot get sun.misc.Unsafe.putDouble", e);
                }
            }
        });
        putBoolean = AccessController.doPrivileged(new PrivilegedAction<Method>() {
            public Method run() {
                try {
                    Method mtd = unsafeClass.getDeclaredMethod("putBoolean", Object.class, long.class, boolean.class);
                    mtd.setAccessible(true);
                    return mtd;
                } catch (Exception e) {
                    throw new IllegalStateException("Cannot get sun.misc.Unsafe.putBoolean", e);
                }
            }
        });
        putObject = AccessController.doPrivileged(new PrivilegedAction<Method>() {
            public Method run() {
                try {
                    Method mtd = unsafeClass.getDeclaredMethod("putObject", Object.class, long.class, Object.class);
                    mtd.setAccessible(true);
                    return mtd;
                } catch (Exception e) {
                    throw new IllegalStateException("Cannot get sun.misc.Unsafe.putObject", e);
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
            object = allocateInstance.invoke(unsafe, clazz);
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
                        throw (IOException) new IOException("Cannot find field " + fieldName).initCause(e);
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
            offset = (Long) objectFieldOffset.invoke(unsafe, field);
        } catch (Exception e) {
            throw new IllegalStateException("Failed getting offset for: field=" + field.getName() + "  class=" + field.getDeclaringClass().getName(), e);
        }

        Class type = field.getType();
        try {
            if (type.isPrimitive()) {
                if (type.equals(Integer.TYPE)) {
                    putInt.invoke(unsafe, object, offset, ((Integer) value).intValue());
                } else if (type.equals(Long.TYPE)) {
                    putLong.invoke(unsafe, object, offset, ((Long) value).longValue());
                } else if (type.equals(Short.TYPE)) {
                    putShort.invoke(unsafe, object, offset, ((Short) value).shortValue());
                } else if (type.equals(Character.TYPE)) {
                    putChar.invoke(unsafe, object, offset, ((Character) value).charValue());
                } else if (type.equals(Byte.TYPE)) {
                    putByte.invoke(unsafe, object, offset, ((Byte) value).byteValue());
                } else if (type.equals(Float.TYPE)) {
                    putFloat.invoke(unsafe, object, offset, ((Float) value).floatValue());
                } else if (type.equals(Double.TYPE)) {
                    putDouble.invoke(unsafe, object, offset, ((Double) value).doubleValue());
                } else if (type.equals(Boolean.TYPE)) {
                    putBoolean.invoke(unsafe, object, offset, ((Boolean) value).booleanValue());
                } else {
                    throw new IllegalStateException("Unknown primitive type: " + type.getName());
                }
            } else {
                putObject.invoke(unsafe, object, offset, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
