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

import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.NotSerializableException;
import java.io.ObjectOutput;
import java.io.ObjectStreamConstants;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.openejb.util.ArrayStack;

public class ObjectOutputStream extends OutputStream implements ObjectOutput, ObjectStreamConstants {

//    private Handles handles;
    private OutputStream out;
    private ArrayStack classDescStack;
    private byte buf[] = new byte[5000];
    private int count;

    public ObjectOutputStream(OutputStream out) throws IOException {
        this.out = out;

        classDescStack = new ArrayStack();
    }

    public void reset() throws IOException {
        resetStream();
        count = 0;
    }

    public void serializeObject(Object obj, OutputStream out) throws NotSerializableException, IOException {
        this.out = out;
        serializeObject(obj);
    }

    public void serializeObject(Object obj) throws NotSerializableException, IOException {

        if (!Serializable.class.isAssignableFrom(obj.getClass()) && !Externalizable.class.isAssignableFrom(obj.getClass())) {
            throw new NotSerializableException(obj.getClass().getName());
        }

        reset();

        writeShort(STREAM_MAGIC);
        writeShort(STREAM_VERSION);
        writeObject(obj);

    }

    public void writeObject(Object obj) throws IOException {
        try {
            if (obj == null) {
                write(TC_NULL);
                return;
            }
            Class clazz = obj.getClass();
            ClassDescriptor classDesc = null;

            if (clazz == ClassDescriptor.class) classDesc = (ClassDescriptor) obj;
            else
                classDesc = ClassDescriptor.lookupInternal(clazz);

            if (classDesc == null) {
                write(TC_NULL);
                return;
            }

            int tmpInt = findWireOffset(obj);
            if (tmpInt >= 0) {
                write(TC_REFERENCE);
                tmpInt += baseWireHandle;

                write((tmpInt >>> 24) & 0xFF);
                write((tmpInt >>> 16) & 0xFF);
                write((tmpInt >>> 8) & 0xFF);
                write((tmpInt >>> 0) & 0xFF);

                return;
            }

            if (obj instanceof Class) {
                write(TC_CLASS);
                write(TC_CLASSDESC);
                writeUTF(classDesc.getName());
                long value = classDesc.getSerialVersionUID();

                write((int) (value >>> 56) & 0xFF);
                write((int) (value >>> 48) & 0xFF);
                write((int) (value >>> 40) & 0xFF);
                write((int) (value >>> 32) & 0xFF);
                write((int) (value >>> 24) & 0xFF);
                write((int) (value >>> 16) & 0xFF);
                write((int) (value >>> 8) & 0xFF);
                write((int) (value >>> 0) & 0xFF);

                assignWireOffset(classDesc);
                classDesc.writeClassInfo(this);
                write(TC_ENDBLOCKDATA);
                writeObject(classDesc.getSuperclass());
                assignWireOffset(clazz);
                return;
            }

            if (obj instanceof ClassDescriptor) {
                write(TC_CLASSDESC);
                writeUTF(classDesc.getName());
                long value = classDesc.getSerialVersionUID();

                write((int) (value >>> 56) & 0xFF);
                write((int) (value >>> 48) & 0xFF);
                write((int) (value >>> 40) & 0xFF);
                write((int) (value >>> 32) & 0xFF);
                write((int) (value >>> 24) & 0xFF);
                write((int) (value >>> 16) & 0xFF);
                write((int) (value >>> 8) & 0xFF);
                write((int) (value >>> 0) & 0xFF);

                assignWireOffset(classDesc);
                write(classDesc.flags);
                tmpInt = classDesc.fields.length;
                write((tmpInt >>> 8) & 0xFF);
                write((tmpInt >>> 0) & 0xFF);
                FieldDescriptor field;
                for (int i = 0; i < classDesc.fields.length; i++) {
                    field = classDesc.fields[i];
                    write((int) field.typeCode);
                    writeUTF(field.name);
                    if (!field.type.isPrimitive()) writeObject(field.typeString);
                }
                write(TC_ENDBLOCKDATA);
                writeObject(classDesc.getSuperclass());
                return;
            }
            if (obj instanceof String) {
                write(TC_STRING);
                String s = ((String) obj).intern();
                assignWireOffset(s);
                writeUTF(s);
                return;
            }
            if (clazz.isArray()) {
                write(TC_ARRAY);
                writeObject(classDesc);
                assignWireOffset(obj);

                Class type = clazz.getComponentType();
                if (type.isPrimitive()) {
                    if (type == Integer.TYPE) {
                        int[] array = (int[]) obj;
                        tmpInt = array.length;

                        write((tmpInt >>> 24) & 0xFF);
                        write((tmpInt >>> 16) & 0xFF);
                        write((tmpInt >>> 8) & 0xFF);
                        write((tmpInt >>> 0) & 0xFF);

                        int value;
                        for (int i = 0; i < tmpInt; i++) {
                            value = array[i];

                            write((value >>> 24) & 0xFF);
                            write((value >>> 16) & 0xFF);
                            write((value >>> 8) & 0xFF);
                            write((value >>> 0) & 0xFF);

                        }
                        return;
                    } else if (type == Byte.TYPE) {
                        byte[] array = (byte[]) obj;
                        tmpInt = array.length;

                        write((tmpInt >>> 24) & 0xFF);
                        write((tmpInt >>> 16) & 0xFF);
                        write((tmpInt >>> 8) & 0xFF);
                        write((tmpInt >>> 0) & 0xFF);

                        write(array, 0, tmpInt);
                        return;
                    } else if (type == Long.TYPE) {
                        long[] array = (long[]) obj;
                        tmpInt = array.length;

                        write((tmpInt >>> 24) & 0xFF);
                        write((tmpInt >>> 16) & 0xFF);
                        write((tmpInt >>> 8) & 0xFF);
                        write((tmpInt >>> 0) & 0xFF);

                        long value;
                        for (int i = 0; i < tmpInt; i++) {
                            value = array[i];

                            write((int) (value >>> 56) & 0xFF);
                            write((int) (value >>> 48) & 0xFF);
                            write((int) (value >>> 40) & 0xFF);
                            write((int) (value >>> 32) & 0xFF);
                            write((int) (value >>> 24) & 0xFF);
                            write((int) (value >>> 16) & 0xFF);
                            write((int) (value >>> 8) & 0xFF);
                            write((int) (value >>> 0) & 0xFF);

                        }
                        return;
                    } else if (type == Float.TYPE) {
                        float[] array = (float[]) obj;
                        tmpInt = array.length;

                        write((tmpInt >>> 24) & 0xFF);
                        write((tmpInt >>> 16) & 0xFF);
                        write((tmpInt >>> 8) & 0xFF);
                        write((tmpInt >>> 0) & 0xFF);

                        int value;
                        for (int i = 0; i < tmpInt; i++) {
                            value = Float.floatToIntBits(array[i]);

                            write((value >>> 24) & 0xFF);
                            write((value >>> 16) & 0xFF);
                            write((value >>> 8) & 0xFF);
                            write((value >>> 0) & 0xFF);

                        }
                        return;
                    } else if (type == Double.TYPE) {
                        double[] array = (double[]) obj;
                        tmpInt = array.length;

                        write((tmpInt >>> 24) & 0xFF);
                        write((tmpInt >>> 16) & 0xFF);
                        write((tmpInt >>> 8) & 0xFF);
                        write((tmpInt >>> 0) & 0xFF);

                        long value;
                        for (int i = 0; i < tmpInt; i++) {
                            value = Double.doubleToLongBits(array[i]);

                            write((int) (value >>> 56) & 0xFF);
                            write((int) (value >>> 48) & 0xFF);
                            write((int) (value >>> 40) & 0xFF);
                            write((int) (value >>> 32) & 0xFF);
                            write((int) (value >>> 24) & 0xFF);
                            write((int) (value >>> 16) & 0xFF);
                            write((int) (value >>> 8) & 0xFF);
                            write((int) (value >>> 0) & 0xFF);

                        }
                        return;
                    } else if (type == Short.TYPE) {
                        short[] array = (short[]) obj;
                        tmpInt = array.length;

                        write((tmpInt >>> 24) & 0xFF);
                        write((tmpInt >>> 16) & 0xFF);
                        write((tmpInt >>> 8) & 0xFF);
                        write((tmpInt >>> 0) & 0xFF);

                        short value;
                        for (int i = 0; i < tmpInt; i++) {
                            value = array[i];

                            write((value >>> 8) & 0xFF);
                            write((value >>> 0) & 0xFF);

                        }
                        return;
                    } else if (type == Character.TYPE) {
                        char[] array = (char[]) obj;
                        tmpInt = array.length;

                        write((tmpInt >>> 24) & 0xFF);
                        write((tmpInt >>> 16) & 0xFF);
                        write((tmpInt >>> 8) & 0xFF);
                        write((tmpInt >>> 0) & 0xFF);

                        char value;
                        for (int i = 0; i < tmpInt; i++) {
                            value = array[i];

                            write((value >>> 8) & 0xFF);
                            write((value >>> 0) & 0xFF);

                        }
                        return;
                    } else if (type == Boolean.TYPE) {
                        boolean[] array = (boolean[]) obj;
                        tmpInt = array.length;

                        write((tmpInt >>> 24) & 0xFF);
                        write((tmpInt >>> 16) & 0xFF);
                        write((tmpInt >>> 8) & 0xFF);
                        write((tmpInt >>> 0) & 0xFF);

                        for (int i = 0; i < tmpInt; i++) {
                            write(array[i] ? 1 : 0);
                        }
                        return;
                    } else {
                        throw new InvalidClassException(clazz.getName());
                    }
                } else {
                    Object[] array = (Object[]) obj;
                    int length = array.length;

                    write((length >>> 24) & 0xFF);
                    write((length >>> 16) & 0xFF);
                    write((length >>> 8) & 0xFF);
                    write((length >>> 0) & 0xFF);

                    for (int i = 0; i < length; i++)
                        writeObject(array[i]);
                }
                return;
            }
            write(TC_OBJECT);
            writeObject(classDesc);
            assignWireOffset(obj);

            if (classDesc.isExternalizable()) {
                writeExternal((Externalizable) obj);
                return;
            }

            int stackMark = classDescStack.size();
            try {

                ClassDescriptor superClassDesc;
                while ((superClassDesc = classDesc.getSuperclass()) != null) {
                    classDescStack.push(classDesc);
                    classDesc = superClassDesc;
                }

                do {
                    if (classDesc.hasWriteObjectMethod()) {
                        /* DMB:  NOT COMPLETE - Should start writing in block data format
                         * and state the size of the data to come.
                         */

                        /* DMB:  NOT COMPLETE - Should Invoke the writeObject
                         * mehtod on the object.
                         * Invoking the write object method requires a
                         * sublcass of java.io.ObjectOutputStream to be
                         * passed in.  This implementation is not a subclass
                         * of java.io.ObjectOutputStream.
                         */

                        /* DMB:  NOT COMPLETE - Should stop writing in block data format.
                         * Denote the end of this mode by writing a terminator to the stream.
                         */

                    } else {
                        FieldDescriptor[] fields = classDesc.getFields();
                        Field field;
                        if (fields.length > 0) {
                            for (int i = 0; i < fields.length; i++) {
                                field = fields[i].getField();
                                if (field == null) throw new InvalidClassException(clazz.getName(), "Nonexistent field " + fields[i].getName());
                                try {
                                    switch (fields[i].getTypeCode()) {
                                        case 'B':
                                            write(field.getByte(obj));
                                            break;
                                        case 'C':
                                            char charvalue = field.getChar(obj);
                                            write((charvalue >>> 8) & 0xFF);
                                            write((charvalue >>> 0) & 0xFF);
                                            break;
                                        case 'I':
                                            int intvalue = field.getInt(obj);
                                            write((intvalue >>> 24) & 0xFF);
                                            write((intvalue >>> 16) & 0xFF);
                                            write((intvalue >>> 8) & 0xFF);
                                            write((intvalue >>> 0) & 0xFF);
                                            break;
                                        case 'Z':
                                            write((field.getBoolean(obj) ? 1 : 0));
                                            break;
                                        case 'J':
                                            long longvalue = field.getLong(obj);
                                            write((int) (longvalue >>> 56) & 0xFF);
                                            write((int) (longvalue >>> 48) & 0xFF);
                                            write((int) (longvalue >>> 40) & 0xFF);
                                            write((int) (longvalue >>> 32) & 0xFF);
                                            write((int) (longvalue >>> 24) & 0xFF);
                                            write((int) (longvalue >>> 16) & 0xFF);
                                            write((int) (longvalue >>> 8) & 0xFF);
                                            write((int) (longvalue >>> 0) & 0xFF);
                                            break;
                                        case 'F':
                                            int floatvalue = Float.floatToIntBits(field.getFloat(obj));
                                            write((floatvalue >>> 24) & 0xFF);
                                            write((floatvalue >>> 16) & 0xFF);
                                            write((floatvalue >>> 8) & 0xFF);
                                            write((floatvalue >>> 0) & 0xFF);
                                            break;
                                        case 'D':
                                            long doublevalue = Double.doubleToLongBits(field.getDouble(obj));
                                            write((int) (doublevalue >>> 56) & 0xFF);
                                            write((int) (doublevalue >>> 48) & 0xFF);
                                            write((int) (doublevalue >>> 40) & 0xFF);
                                            write((int) (doublevalue >>> 32) & 0xFF);
                                            write((int) (doublevalue >>> 24) & 0xFF);
                                            write((int) (doublevalue >>> 16) & 0xFF);
                                            write((int) (doublevalue >>> 8) & 0xFF);
                                            write((int) (doublevalue >>> 0) & 0xFF);
                                            break;
                                        case 'S':
                                            short shortvalue = field.getShort(obj);
                                            write((shortvalue >>> 8) & 0xFF);
                                            write((shortvalue >>> 0) & 0xFF);
                                            break;
                                        case '[':
                                        case 'L':
                                            writeObject(field.get(obj));
                                            break;
                                        default:
                                            throw new InvalidClassException(clazz.getName());
                                    }
                                } catch (IllegalAccessException e) {
                                    throw new InvalidClassException(clazz.getName(), e.getMessage());
                                } finally {
                                }
                            }
                        }
                    }
                } while (classDescStack.size() > stackMark && (classDesc = (ClassDescriptor) classDescStack.pop()) != null);

            } finally {
                /* If an error occcured, make sure we set the stack back
                 * the way it was before we started.
                 */

            }

        } finally {
        }
    }

    public void writeString(String s) throws IOException {
        writeObject(s);
    }

    private void writeExternal(Externalizable ext) throws IOException {
//	    if (useDeprecatedExternalizableFormat) {
        if (false) {
            /* JDK 1.1 external data format.
             * Don't write in block data mode and no terminator tag.
             */
            /* This method accepts a java.io.OutputStream as a parameter */
            ext.writeExternal(this);
        } else {
            /* JDK 1.2 Externalizable data format writes in block data mode
             * and terminates externalizable data with TAG_ENDBLOCKDATA.
             */
            /* DMB:  NOT COMPLETE - Should start writing in block data format.
             * This states the size of the data to come
             */

            try {
                /* This method accepts a java.io.ObjectOutputStream as a parameter */
                ext.writeExternal(this);
            } finally {
                /* DMB:  NOT COMPLETE - Should stop writing in block data format.
                 * Denote the end of this mode by writing a terminator to the stream.
                 */

            }
        }
    }

    public void writeException(Throwable th) throws IOException {
        /* DMB:  NOT COMPLETE - Must write exceptions that occur during serialization
         * to the stream.
         */

    }

    public void writeReset() throws IOException {
        /* DMB:  NOT COMPLETE - Must write the reset byte when the reset() method
         * is called.
         */

    }

/*    public void write(int b) throws IOException {
        out.write(b);
    }
*/
/*    public void write(byte b[], int off, int len) throws IOException {
        for (int i = 0 ; i < len ; i++) {
            write(b[off + i]);
        }
    }
*/

    public void write(int b) {
        try {
            buf[count++] = (byte) b;
        } catch (ArrayIndexOutOfBoundsException e) {
            byte newbuf[] = new byte[Math.max(buf.length << 1, count)];
            System.arraycopy(buf, 0, newbuf, 0, count - 1);
            buf = newbuf;
        }
    }

    public synchronized void write(byte b[], int off, int len) {
        if (len == 0) return;

        int newcount = count + len;
        if (newcount > buf.length) {
            byte newbuf[] = new byte[Math.max(buf.length << 1, newcount)];
            System.arraycopy(buf, 0, newbuf, 0, count);
            buf = newbuf;
        }
        System.arraycopy(b, off, buf, count, len);
        count = newcount;
    }

    public void flush() throws IOException {
//    	out.flush();
    }

    public byte[] toByteArray() {
        byte newbuf[] = new byte[count];
        System.arraycopy(buf, 0, newbuf, 0, count);
        return newbuf;
    }

    public int size() {
        return count;
    }

    public final void writeBoolean(boolean v) throws IOException {
        write(v ? 1 : 0);
    }

    public final void writeByte(int v) throws IOException {
        write(v);
    }

    public final void writeShort(int v) throws IOException {
        write((v >>> 8) & 0xFF);
        write((v >>> 0) & 0xFF);
    }

    public final void writeChar(int v) throws IOException {
        write((v >>> 8) & 0xFF);
        write((v >>> 0) & 0xFF);
    }

    public final void writeInt(int v) throws IOException {
        write((v >>> 24) & 0xFF);
        write((v >>> 16) & 0xFF);
        write((v >>> 8) & 0xFF);
        write((v >>> 0) & 0xFF);
    }

    public final void writeLong(long v) throws IOException {
        write((int) (v >>> 56) & 0xFF);
        write((int) (v >>> 48) & 0xFF);
        write((int) (v >>> 40) & 0xFF);
        write((int) (v >>> 32) & 0xFF);
        write((int) (v >>> 24) & 0xFF);
        write((int) (v >>> 16) & 0xFF);
        write((int) (v >>> 8) & 0xFF);
        write((int) (v >>> 0) & 0xFF);
    }

    public final void writeFloat(float v) throws IOException {
        writeInt(Float.floatToIntBits(v));
    }

    public final void writeDouble(double v) throws IOException {
        writeLong(Double.doubleToLongBits(v));
    }

    public final void writeBytes(String s) throws IOException {
        int tmpLen = s.length();
        for (int i = 0; i < tmpLen; i++) {
            write((byte) s.charAt(i));
        }
    }

    public final void writeChars(String s) throws IOException {
        int tmpLen = s.length();
        for (int i = 0; i < tmpLen; i++) {
            int v = s.charAt(i);
            write((v >>> 8) & 0xFF);
            write((v >>> 0) & 0xFF);
        }
    }

    /*  These are to speed up the writing of strings.
     *  This method is called frequently and placing these here
     *  prevents constant allocation and garbage collection.
     */

    private char[] utfCharBuf = new char[32];

    public final void writeUTF(String str) throws IOException {

        int len = str.length();

        if (utfCharBuf.length < len) utfCharBuf = new char[len];

        str.getChars(0, len, utfCharBuf, 0);

        int mark = count;
        write(0);
        write(0);
        for (int i = 0; i < len; i++) {
            int c = utfCharBuf[i];
            if ((c >= 0x0001) && (c <= 0x007F)) {
                write(c);
            } else if (c > 0x07FF) {
                write(0xE0 | ((c >> 12) & 0x0F));
                write(0x80 | ((c >> 6) & 0x3F));
                write(0x80 | ((c >> 0) & 0x3F));
            } else {
                write(0xC0 | ((c >> 6) & 0x1F));
                write(0x80 | ((c >> 0) & 0x3F));
            }
        }

//       	if (tmpUtflen > 65535)throw new UTFDataFormatException();

        len = count - mark - 2;
        buf[mark] = (byte) ((len >>> 8) & 0xFF);
        buf[mark + 1] = (byte) ((len >>> 0) & 0xFF);

    }

    /* Object references are mapped to the wire handles through a hashtable
     * WireHandles are integers generated by the ObjectOutputStream,
     * they need only be unique within a stream.
     * Objects are assigned sequential handles and stored in wireHandle2Object.
     * The handle for an object is its index in wireHandle2Object.
     * Object with the "same" hashcode are chained using wireHash2Handle.
     * The hashcode of objects is used to index through the wireHash2Handle.
     * -1 is the marker for unused cells in wireNextHandle
     */
    private ArrayList wireHandle2Object;
    private int nextWireOffset;

    /* the next five members implement an inline hashtable. */
    private int[] wireHash2Handle;
    private int[] wireNextHandle;
    private int wireHashSizePower = 2;
    private int wireHashLoadFactor = 7;
    private int wireHashCapacity = (1 << wireHashSizePower) * wireHashLoadFactor;

    /*
     * Insert the specified object into the hash array and link if
     * necessary. Put the new object into the hash table and link the
     * previous to it. Newer objects occur earlier in the list.
     */
    private void hashInsert(Object obj, int offset) {
        int hash = System.identityHashCode(obj);
        int index = (hash & 0x7FFFFFFF) % wireHash2Handle.length;
        wireNextHandle[offset] = wireHash2Handle[index];
        wireHash2Handle[index] = offset;
    }

    /*
    * Locate and return if found the handle for the specified object.
    * -1 is returned if the object does not occur in the array of
    * known objects.
    */
    private int findWireOffset(Object obj) {
        int hash = System.identityHashCode(obj);
        int index = (hash & 0x7FFFFFFF) % wireHash2Handle.length;

        for (int handle = wireHash2Handle[index];
             handle >= 0;
             handle = wireNextHandle[handle]) {

            if (wireHandle2Object.get(handle) == obj)
                return handle;
        }
        return -1;
    }

    /* Allocate a handle for an object.
     * The Vector is indexed by the wireHandleOffset
     * and contains the object.
     * Allow caller to specify the hash method for the object.
     */
    private void assignWireOffset(Object obj)
            throws IOException {
        if (nextWireOffset == wireNextHandle.length) {
            int[] oldnexthandles = wireNextHandle;
            wireNextHandle = new int[nextWireOffset * 2];
            System.arraycopy(oldnexthandles, 0,
                    wireNextHandle, 0,
                    nextWireOffset);
        }
        if (nextWireOffset >= wireHashCapacity) {
            growWireHash2Handle();
        }
        wireHandle2Object.add(obj);
        hashInsert(obj, nextWireOffset);
        nextWireOffset++;
        return;
    }

    private void growWireHash2Handle() {

        wireHashSizePower++;
        wireHash2Handle = new int[(1 << wireHashSizePower) - 1];
        Arrays.fill(wireHash2Handle, -1);

        for (int i = 0; i < nextWireOffset; i++) {
            wireNextHandle[i] = 0;
        }

        for (int i = 0; i < wireHandle2Object.size(); i++) {
            hashInsert(wireHandle2Object.get(i), i);
        }

        wireHashCapacity = (1 << wireHashSizePower) * wireHashLoadFactor;
    }

    /*
     * Internal reset function to reinitialize the state of the stream.
     * Reset state of things changed by using the stream.
     */
    private void resetStream() throws IOException {
        if (wireHandle2Object == null) {
            wireHandle2Object = new ArrayList();
            wireNextHandle = new int[4];
            wireHash2Handle = new int[ (1 << wireHashSizePower) - 1];
        } else {

            wireHandle2Object.clear();
            for (int i = 0; i < nextWireOffset; i++) {
                wireNextHandle[i] = 0;
            }
        }
        nextWireOffset = 0;
        Arrays.fill(wireHash2Handle, -1);

        if (classDescStack == null)
            classDescStack = new ArrayStack();
        else
            classDescStack.setSize(0);

    }
}