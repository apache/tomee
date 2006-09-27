package org.apache.openejb.util.io;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamConstants;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;

public class ClassDescriptor implements java.io.Serializable, ObjectStreamConstants {

    private static final ObjectStreamField[] serialPersistentFields = ObjectStreamClass.NO_FIELDS;

    protected ClassDescriptor(Class clazz, ClassDescriptor superdesc, boolean serializable, boolean externalizable) {

        if (externalizable) serializable = false;

        this.forClass = clazz;
        this.superdesc = superdesc;
        this.serializable = serializable;
        this.externalizable = externalizable;
        this.name = forClass.getName();

        /*
           * Enter this class in the table of known descriptors.
           * Otherwise, when the fields are read it may recurse
           * trying to find the descriptor for itself.
           */
        insertDescriptorFor(this);

        if (externalizable) fields = NO_FIELDS;
        else if (serializable) AccessController.doPrivileged(new AccessibleFieldInitializer(this));

        AccessController.doPrivileged(new SerializationPropertiesReflector(this));

        if (hasWriteObjectMethod) flags |= ObjectStreamConstants.SC_WRITE_METHOD;
        if (serializable) flags |= ObjectStreamConstants.SC_SERIALIZABLE;
        if (externalizable) flags |= ObjectStreamConstants.SC_EXTERNALIZABLE;
    }

    protected int flags = 0;

    protected void writeClassInfo(ObjectOutputStream out) throws IOException {
        out.writeByte(flags);
        out.writeShort(fields.length);

        for (int i = 0; i < fields.length; i++)
            fields[i].writeDesc(out);
    }

    private boolean serializable;
    private boolean externalizable;

    /*
     * Get the Serializability of the class.
     */
    protected boolean isSerializable() {
        return serializable;
    }

    /*
     * Get the externalizability of the class.
     */
    protected boolean isExternalizable() {
        return externalizable;
    }

    protected boolean isNonSerializable() {
        return ! (externalizable || serializable);
    }

    /*
     * Array of persistent fields of this class, sorted by
     * type and name.
     */

    public static final FieldDescriptor[] NO_FIELDS = new FieldDescriptor[0];
    protected FieldDescriptor[] fields;

    public FieldDescriptor[] getFields() {
        return fields;
    }

    public void setFields(FieldDescriptor[] fields) {
        this.fields = fields;
    }

    private boolean hasWriteObjectMethod;
    private boolean hasReadObjectMethod;
    private Method writeObjectMethod;
    private Method readObjectMethod;

    public boolean hasWriteObjectMethod() {
        return hasWriteObjectMethod;
    }

    public void hasWriteObjectMethod(boolean b) {
        hasWriteObjectMethod = b;
    }

    public Method getWriteObjectMethod() {
        return writeObjectMethod;
    }

    protected void setWriteObjectMethod(Method method) {
        writeObjectMethod = method;
        hasWriteObjectMethod = (method != null);
    }

    public boolean hasReadObjectMethod() {
        return hasReadObjectMethod;
    }

    public void hasReadObjectMethod(boolean b) {
        hasReadObjectMethod = b;
    }

    public Method getReadObjectMethod() {
        return readObjectMethod;
    }

    protected void setReadObjectMethod(Method method) {
        readObjectMethod = method;
        hasReadObjectMethod = (method != null);
    }

    /*
     * SerialVersionUID for the class this instance represents.
     */
    private long suid;

    public long getSerialVersionUID() {
        return suid;
    }

    protected void setSerialVersionUID(long suid) {
        this.suid = suid;
    }

    /*
     * The name of this descriptor
     */
    private String name;

    public String getName() {
        return name;
    }

    /*
     * Class that is a descriptor for in this virtual machine.
     */
    private Class forClass;

    public Class forClass() {
        return forClass;
    }

    /*
     * The descriptor of the supertype.
     */
    private ClassDescriptor superdesc;

    /*
     * Return the superclass descriptor of this descriptor.
     */
    protected ClassDescriptor getSuperclass() {
        return superdesc;
    }

    /*
     * Return the superclass descriptor of this descriptor.
     */
    protected void setSuperclass(ClassDescriptor s) {
        superdesc = s;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append(name);
        sb.append(": static final long serialVersionUID = ");
        sb.append(Long.toString(suid));
        sb.append("L;");
        return sb.toString();
    }

    public static StringBuffer getSignature(Class clazz) {
        StringBuffer buf = new StringBuffer();
        return getSignature(clazz, buf);
    }

    public static StringBuffer getSignature(Class clazz, StringBuffer buf) {
        if (clazz.isPrimitive()) {
            if (clazz == Integer.TYPE) buf.append('I');
            else if (clazz == Byte.TYPE) buf.append('B');
            else if (clazz == Long.TYPE) buf.append('J');
            else if (clazz == Float.TYPE) buf.append('F');
            else if (clazz == Double.TYPE) buf.append('D');
            else if (clazz == Short.TYPE) buf.append('S');
            else if (clazz == Character.TYPE) buf.append('C');
            else if (clazz == Boolean.TYPE) buf.append('Z');
            else if (clazz == Void.TYPE) buf.append('V');
        } else if (clazz.isArray()) {
            Class cl = clazz;
            while (cl.isArray()) {
                buf.append('[');
                cl = cl.getComponentType();
            }
            buf.append(getSignature(cl).toString());
        } else {
            buf.append('L');
            buf.append(clazz.getName().replace('.', '/'));
            buf.append(';');
        }
        return buf;
    }

    public static ClassDescriptor lookup(Class clazz) {
        ClassDescriptor desc = lookupInternal(clazz);
        if (desc.isSerializable() || desc.isExternalizable()) return desc;
        return null;
    }

    /*
     * Find the class descriptor for the specified class.
     * Package access only so it can be called from ObjectIn/OutStream.
     */
    static ClassDescriptor lookupInternal(Class clazz) {
        /* Synchronize on the hashtable so no two threads will do
           * this at the same time.
           */
        ClassDescriptor desc = null;
        synchronized (descriptorFor) {
            /* Find the matching descriptor if it already known */
            desc = findDescriptorFor(clazz);
            if (desc != null) {
                return desc;
            }

            /* Check if it's serializable */
            boolean serializable = Serializable.class.isAssignableFrom(clazz);

            /* If the class is only Serializable,
                * lookup the descriptor for the superclass.
                */
            ClassDescriptor superdesc = null;
            if (serializable) {
                Class superclass = clazz.getSuperclass();
                if (superclass != null)
                    superdesc = lookup(superclass);
            }

            /* Check if its' externalizable.
                * If it's Externalizable, clear the serializable flag.
                * Only one or the other may be set in the protocol.
                */
            boolean externalizable = false;
            if (serializable) {
                externalizable = ((superdesc != null) && superdesc.isExternalizable()) ||
                        Externalizable.class.isAssignableFrom(clazz);
                if (externalizable) {
                    serializable = false;
                }
            }

            /* Create a new version descriptor,
                * it put itself in the known table.
                */
            desc = new ClassDescriptor(clazz, superdesc,
                    serializable, externalizable);
        }
        return desc;
    }

    /*
     * findDescriptorFor a Class.  This looks in the cache for a
     * mapping from Class -> ObjectStreamClass mappings.  The hashCode
     * of the Class is used for the lookup since the Class is the key.
     * The entries are extended from java.lang.ref.SoftReference so the
     * gc will be able to free them if needed.
     */
    private static ClassDescriptor findDescriptorFor(Class clazz) {

        int hash = clazz.hashCode();
        int index = (hash & 0x7FFFFFFF) % descriptorFor.length;
        ClassDescriptorEntry e;
        ClassDescriptorEntry prev;

        /* Free any initial entries whose refs have been cleared */
        while ((e = descriptorFor[index]) != null && e.get() == null) {
            descriptorFor[index] = e.next;
        }

        /* Traverse the chain looking for a descriptor with forClass == clazz.
           * unlink entries that are unresolved.
           */
        prev = e;
        while (e != null) {
            ClassDescriptor desc = (ClassDescriptor) (e.get());
            if (desc == null) {
                // This entry has been cleared,  unlink it
                prev.next = e.next;
            } else {
                if (desc.forClass == clazz)
                    return desc;
                prev = e;
            }
            e = e.next;
        }
        return null;
    }

    /*
     * insertDescriptorFor a Class -> ClassDescriptor mapping.
     */
    private static void insertDescriptorFor(ClassDescriptor desc) {
        // Make sure not already present
        if (findDescriptorFor(desc.forClass) != null) {
            return;
        }

        int hash = desc.forClass.hashCode();
        int index = (hash & 0x7FFFFFFF) % descriptorFor.length;
        ClassDescriptorEntry e = new ClassDescriptorEntry(desc);
        e.next = descriptorFor[index];
        descriptorFor[index] = e;
    }

    /*
     * Entries held in the Cache of known ClassDescriptor objects.
     * Entries are chained together with the same hash value (modulo array size).
     */
    private static class ClassDescriptorEntry extends java.lang.ref.SoftReference {
        ClassDescriptorEntry next;

        ClassDescriptorEntry(ClassDescriptor c) {
            super(c);
        }
    }

    static private ClassDescriptorEntry[] descriptorFor = new ClassDescriptorEntry[61];

    static final Class[] OIS_ARGS = {java.io.ObjectInputStream.class};
    static final Class[] OOS_ARGS = {java.io.ObjectOutputStream.class};

    private class SerializationPropertiesReflector implements PrivilegedAction {

        ClassDescriptor desc;

        public SerializationPropertiesReflector(ClassDescriptor desc) {
            this.desc = desc;
        }

        public Object run() {
            try {
                Field field = null;
                field = desc.forClass().getDeclaredField("serialVersionUID");
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) {
                    field.setAccessible(true);
                    desc.setSerialVersionUID(field.getLong(desc.forClass()));
                } else
                    desc.setSerialVersionUID(computeSerialVersionUID(desc.forClass()));

            }
            catch (NoSuchFieldException ex) {
                desc.setSerialVersionUID(computeSerialVersionUID(desc.forClass()));
            }
            catch (IllegalAccessException ex) {
                desc.setSerialVersionUID(computeSerialVersionUID(desc.forClass()));
            }

            if (serializable) {
                desc.setWriteObjectMethod(getDeclaredMethod("writeObject",
                        ClassDescriptor.OOS_ARGS,
                        Modifier.PRIVATE,
                        Modifier.STATIC));

                desc.setWriteObjectMethod(getDeclaredMethod("readObject",
                        ClassDescriptor.OIS_ARGS,
                        Modifier.PRIVATE,
                        Modifier.STATIC));
            }
            return null;
        }

        private Method getDeclaredMethod(String methodName, Class[] args, int requiredModifierMask, int disallowedModifierMask) {
            Method method = null;
            try {
                method = forClass.getDeclaredMethod(methodName, args);
                if (method != null) {
                    int mods = method.getModifiers();
                    if ((mods & disallowedModifierMask) != 0 || (mods & requiredModifierMask) != requiredModifierMask)
                        method = null;
                    else
                        method.setAccessible(true);

                }
            }
            catch (NoSuchMethodException e) { /* This is not a problem. */ }
            return method;
        }

        private long computeSerialVersionUID(Class clazz) {
            /* In this implementation we are not using the
             * serialVersionID.  The classes on both ends must be compatable.
             */
            return 42L;
        }

    }

    private static class AccessibleFieldInitializer implements PrivilegedAction {

        ClassDescriptor desc;

        public AccessibleFieldInitializer(ClassDescriptor desc) {
            this.desc = desc;
        }

        public Object run() {
            FieldDescriptor[] fields = getDeclaredSerialPersistentFields(desc.forClass());
            if (fields == null) fields = reflectForFields(desc.forClass());

            if (fields.length > 1) Arrays.sort(fields);

            desc.setFields(fields);
            return null;
        }

        private FieldDescriptor[] getDeclaredSerialPersistentFields(Class clazz) throws SecurityException {
            java.lang.reflect.Field[] tmpFields = null;
            ObjectStreamField[] outputStreamFields;

            try {
                Field serialPersistentFields = clazz.getDeclaredField("serialPersistentFields");
                /* This field must be private according to the spec */
                if (!Modifier.isPrivate(serialPersistentFields.getModifiers())) return null;

                serialPersistentFields.setAccessible(true);

                /* Safely retreive the value of the class' serialPersistentFields.
                     * These fields are of the wrong type they are of the type
                     * java.io.ObjectStreamField and are nothing more than wrappers
                     * for a methods name and type to anyone outside the java.io package
                     * For this reason we must create our own more accessible FieldDescriptors
                     * that encompas the functionality we need.
                     */

                outputStreamFields = (ObjectStreamField[]) serialPersistentFields.get(clazz);
                tmpFields = new Field[outputStreamFields.length];

            }
            catch (NoSuchFieldException e) {
                return null;
            }
            catch (IllegalAccessException e) {
                return null;
            }
            catch (IllegalArgumentException e) {
                return null;
            }

            /* Validate the existence of the fields the class indicated
             * and instaniate a FieldDescriptor for it.
             */
            Field reflectedField;
            int validFields = 0;
            for (int i = outputStreamFields.length - 1; i >= 0; i--) {
                try {
                    reflectedField = clazz.getDeclaredField(outputStreamFields[i].getName());
                    if (outputStreamFields[i].getType() == reflectedField.getType()) {
                        reflectedField.setAccessible(true);
                        tmpFields[validFields++] = reflectedField;
                    }
                }
                catch (NoSuchFieldException e) { /* unimportant. keep going */}
            }

            FieldDescriptor[] validFieldDescriptors = new FieldDescriptor[validFields];

            for (validFields--; validFields >= 0; validFields--) {
                validFieldDescriptors[validFields] = new FieldDescriptor(tmpFields[validFields]);
            }

            return validFieldDescriptors;
        }

        private FieldDescriptor[] reflectForFields(Class clazz) {

            Field[] allFields = clazz.getDeclaredFields();
            AccessibleObject.setAccessible(allFields, true);
            Field[] tmpFields = new Field[allFields.length];

            int validFields = 0, modifiers;

            for (int i = 0; i < allFields.length; i++) {
                modifiers = allFields[i].getModifiers();
                if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers))
                    tmpFields[validFields++] = allFields[i];
            }

            FieldDescriptor[] validFieldDescriptors = new FieldDescriptor[validFields];

            for (validFields--; validFields >= 0; validFields--) {
                validFieldDescriptors[validFields] = new FieldDescriptor(tmpFields[validFields]);
            }

            return validFieldDescriptors;
        }
    }

}