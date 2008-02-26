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

import sun.reflect.ReflectionFactory;

import java.io.Externalizable;
import java.io.ObjectStreamException;
import java.io.ObjectInput;
import java.io.IOException;
import java.io.ObjectOutput;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Collections;
import java.util.WeakHashMap;

/**
 * @version $Rev$ $Date$
 */
public class PojoSerialization implements Externalizable {
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

    public PojoSerialization() {
    }

    public PojoSerialization(Object object) {
        this.object = object;
    }

    protected Object readResolve() throws ObjectStreamException {
        return object;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        byte b = in.readByte();
        if (b != CLASS) throw new IOException("Expected 'CLASS' byte " + CLASS + ", got: " + b);

        Class clazz = (Class) in.readObject();

        Object object = newInstance(clazz);

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

    public void writeExternal(ObjectOutput out) throws IOException {
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


    private transient ReflectionFactory reflectionFactory = ReflectionFactory.getReflectionFactory();
    private transient Map constructorCache = Collections.synchronizedMap(new WeakHashMap());

    public Object newInstance(Class type) {
        try {
            Constructor customConstructor = getMungedConstructor(type);
            return customConstructor.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot construct " + type.getName(), e);
        }
    }

    private Constructor getMungedConstructor(Class type) throws NoSuchMethodException {
        WeakReference ref = (WeakReference) constructorCache.get(type);
        if (ref == null || ref.get() == null) {
            Constructor javaLangObjectConstructor = Object.class.getDeclaredConstructor();
            ref = new WeakReference(reflectionFactory.newConstructorForSerialization(type, javaLangObjectConstructor));
            constructorCache.put(type, ref);
        }
        return (Constructor) ref.get();
    }

    private void setValue(Field field, Object object, Object value) {
        long offset = unsafe.objectFieldOffset(field);
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
                throw new IllegalStateException("Could not set field " +
                        object.getClass() + "." + field.getName() +
                        ": Unknown type " + type);
            }
        } else {
            unsafe.putObject(object, offset, value);
        }
    }

}
