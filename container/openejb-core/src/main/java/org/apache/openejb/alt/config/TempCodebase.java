package org.apache.openejb.alt.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.loader.FileUtils;
import org.apache.openejb.util.SafeToolkit;

import java.util.HashMap;
import java.net.URL;
import java.io.File;

public class TempCodebase {

    protected static final HashMap tempCodebases = new HashMap();

    private final String codebase;
    private final ClassLoader classLoader;

    public TempCodebase(String codebase) throws OpenEJBException {
        this.codebase = codebase;
        ClassLoader cl = null;
        try {
            URL[] urlCodebase = new URL[1];
            urlCodebase[0] = createTempCopy(codebase).toURL();
            cl = new java.net.URLClassLoader(urlCodebase, TempCodebase.class.getClassLoader());
        } catch (java.net.MalformedURLException mue) {
            throw new OpenEJBException(SafeToolkit.messages.format("cl0001", codebase, mue.getMessage()));
        } catch (SecurityException se) {
            throw new OpenEJBException(SafeToolkit.messages.format("cl0002", codebase, se.getMessage()));
        }
        this.classLoader = cl;
    }

    public String getCodebase() {
        return codebase;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public static TempCodebase getTempCodebase(String codebase) throws OpenEJBException {
        if (codebase == null) {
            codebase = "CLASSPATH";
        }
        TempCodebase tempCodebase = (TempCodebase) tempCodebases.get(codebase);
        if (tempCodebase == null) {
            tempCodebase = new TempCodebase(codebase);
            tempCodebases.put(codebase, tempCodebase);
        }
        return tempCodebase;
    }

    public Class loadClass(String className) throws OpenEJBException {
        ClassLoader cl = getClassLoader();
        Class clazz = null;
        try {
            clazz = cl.loadClass(className);
        } catch (ClassNotFoundException cnfe) {
            throw new OpenEJBException(SafeToolkit.messages.format("cl0007", className, codebase));
        }
        return clazz;
    }

    public static void unloadTempCodebase(String codebase) {

        tempCodebases.remove(codebase);
    }

    protected static ClassLoader getCodebaseTempClassLoader(String codebase) throws OpenEJBException {
        if (codebase == null) codebase = "CLASSPATH";

        ClassLoader cl = (ClassLoader) tempCodebases.get(codebase);
        if (cl == null) {
            synchronized (SafeToolkit.codebases) {
                cl = (ClassLoader) SafeToolkit.codebases.get(codebase);
                if (cl == null) {
                    try {
                        URL[] urlCodebase = new URL[1];
                        urlCodebase[0] = createTempCopy(codebase).toURL();

// make sure everything works if we were not loaded by the system class loader
                        cl = new java.net.URLClassLoader(urlCodebase, SafeToolkit.class.getClassLoader());

                        tempCodebases.put(codebase, cl);
                    } catch (java.net.MalformedURLException mue) {
                        throw new OpenEJBException(SafeToolkit.messages.format("cl0001", codebase, mue.getMessage()));
                    } catch (SecurityException se) {
                        throw new OpenEJBException(SafeToolkit.messages.format("cl0002", codebase, se.getMessage()));
                    }
                }
            }
        }
        return cl;
    }

    protected static ClassLoader getTempClassLoader(String codebase) throws OpenEJBException {
        ClassLoader cl = null;
        try {
            URL[] urlCodebase = new URL[1];
            urlCodebase[0] = createTempCopy(codebase).toURL();

            cl = new java.net.URLClassLoader(urlCodebase, SafeToolkit.class.getClassLoader());
        } catch (java.net.MalformedURLException mue) {
            throw new OpenEJBException(SafeToolkit.messages.format("cl0001", codebase, mue.getMessage()));
        } catch (SecurityException se) {
            throw new OpenEJBException(SafeToolkit.messages.format("cl0002", codebase, se.getMessage()));
        }
        return cl;
    }

    protected static File createTempCopy(String codebase) throws OpenEJBException {
        File file = null;

        try {
            File codebaseFile = new File(codebase);
//            if (codebaseFile.isDirectory()) return codebaseFile;

            file = File.createTempFile("openejb_validate", ".jar", null);
            file.deleteOnExit();

            FileUtils.copyFile(file, codebaseFile);
        } catch (Exception e) {
            throw new OpenEJBException(SafeToolkit.messages.format("cl0002", codebase, e.getMessage()));
        }
        return file;
    }
}
