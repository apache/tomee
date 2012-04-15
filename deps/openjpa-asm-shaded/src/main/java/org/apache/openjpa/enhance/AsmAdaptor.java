package org.apache.openjpa.enhance;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import org.apache.xbean.asm.ClassReader;
import org.apache.xbean.asm.ClassWriter;
import serp.bytecode.BCClass;

public class AsmAdaptor {
    private static final int Java7_MajorVersion = 51;

    @SuppressWarnings("deprecation")
    public static void write(BCClass bc) throws IOException {
        if (bc.getMajorVersion() < Java7_MajorVersion) {
            bc.write();
        } else {
            String name = bc.getName();
            int dotIndex = name.lastIndexOf('.') + 1;
            name = name.substring(dotIndex);
            Class<?> type = bc.getType();

            OutputStream out = new FileOutputStream(
                    URLDecoder.decode(type.getResource(name + ".class").getFile()));
            try {
                writeJava7(bc, out);
            } finally {
                out.flush();
                out.close();
            }
        }
    }

    public static void write(BCClass bc, File outFile) throws IOException {
        if (bc.getMajorVersion() < Java7_MajorVersion) {
            bc.write(outFile);
        } else {
            OutputStream out = new FileOutputStream(outFile);
            try {
                writeJava7(bc, out);
            } finally {
                out.flush();
                out.close();
            }
        }
    }

    public static byte[] toByteArray(BCClass bc, byte[] returnBytes) throws IOException {
        if (bc.getMajorVersion() >= Java7_MajorVersion) {
            returnBytes = toJava7ByteArray(bc, returnBytes);
        }
        return returnBytes;
    }

    private static void writeJava7(BCClass bc, OutputStream out) throws IOException {
        byte[] java7Bytes = toJava7ByteArray(bc, bc.toByteArray());
        out.write(java7Bytes);
    }

    private static byte[] toJava7ByteArray(BCClass bc, byte[] classBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(classBytes);
        BufferedInputStream bis = new BufferedInputStream(bais);

        ClassWriter cw = new BCClassWriter(ClassWriter.COMPUTE_FRAMES, bc.getClassLoader());
        ClassReader cr = new ClassReader(bis);
        cr.accept(cw, 0);
        return cw.toByteArray();
    }

    private static class BCClassWriter extends ClassWriter {
        private final ClassLoader _loader;

        BCClassWriter(int flags, ClassLoader loader) {
            super(flags);
            _loader = loader;
        }

        @Override
        protected String getCommonSuperClass(String type1, String type2) {
            Class<?> class1;
            Class<?> class2;
            try {
                class1 = _loader.loadClass(type1.replace('/', '.'));
                class2 = _loader.loadClass(type2.replace('/', '.'));
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
            if (class1.isAssignableFrom(class2)) {
                return type1;
            }
            if (class2.isAssignableFrom(class1)) {
                return type2;
            }
            if (class1.isInterface() || class2.isInterface()) {
                return "java/lang/Object";
            }
            do {
                class1 = class1.getSuperclass();
            } while (!class1.isAssignableFrom(class2));
            return class1.getName().replace('.', '/');
        }
    }
}
