package org.apache.openejb.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Hashtable;
import java.util.jar.JarFile;

import org.apache.openejb.OpenEJBException;

public class JarUtils {

    private static Messages messages = new Messages("org.apache.openejb.util.resources");

    static {
        setHandlerSystemProperty();
    }

    private static boolean alreadySet = false;

    public static void setHandlerSystemProperty() {
        if (!alreadySet) {
            /*
             * Setup the java protocol handler path to include org.apache.openejb.util.urlhandler
             * so that org.apache.openejb.util.urlhandler.resource.Handler will be used for URLs
             * of the form "resource:/path".
             */
            /*try {
                String oldPkgs = System.getProperty( "java.protocol.handler.pkgs" );

                if ( oldPkgs == null )
                    System.setProperty( "java.protocol.handler.pkgs", "org.apache.openejb.util.urlhandler" );
                else if ( oldPkgs.indexOf( "org.apache.openejb.util.urlhandler" ) < 0 )
                    System.setProperty( "java.protocol.handler.pkgs", oldPkgs + "|" + "org.apache.openejb.util.urlhandler" );

            } catch ( SecurityException ex ) {
            }*/
            Hashtable urlHandlers = (Hashtable) AccessController.doPrivileged(
                    new PrivilegedAction() {
                        public Object run() {
                            java.lang.reflect.Field handlers = null;
                            try {
                                handlers = URL.class.getDeclaredField("handlers");
                                handlers.setAccessible(true);
                                return handlers.get(null);
                            } catch (Exception e2) {
                                e2.printStackTrace();
                            }
                            return null;
                        }
                    }
            );
            urlHandlers.put("resource", new org.apache.openejb.util.urlhandler.resource.Handler());
            alreadySet = true;
        }
    }

    public static File getJarContaining(String path) throws OpenEJBException {
        File jarFile = null;
        try {
            URL url = new URL("resource:/" + path);

            /*
             * If we loaded the configuration from a jar, either from a jar:
             * URL or a resource: URL, we must strip off the config file location
             * from the URL.
             */
            String jarPath = null;
            if (url.getProtocol().compareTo("resource") == 0) {
                String resource = url.getFile().substring(1);

                url = getContextClassLoader().getResource(resource);
                if (url == null) {
                    throw new OpenEJBException("Could not locate a jar containing the path " + path);
                }
            }

            if (url != null) {
                jarPath = url.getFile();
                jarPath = jarPath.substring(0, jarPath.indexOf('!'));
                jarPath = jarPath.substring("file:".length());
            }

            jarFile = new File(jarPath);
            jarFile = jarFile.getAbsoluteFile();
        } catch (Exception e) {
            throw new OpenEJBException("Could not locate a jar containing the path " + path, e);
        }
        return jarFile;
    }

    public static void addFileToJar(String jarFile, String file) throws OpenEJBException {
        ByteArrayOutputStream errorBytes = new ByteArrayOutputStream();

        /* NOTE: Sadly, we have to play this little game 
         * with temporarily switching the standard error
         * stream to capture the errors.
         * Although you can pass in an error stream in 
         * the constructor of the jar tool, they are not
         * used when an error occurs.
         */
        PrintStream newErr = new PrintStream(errorBytes);
        PrintStream oldErr = System.err;
        System.setErr(newErr);

        sun.tools.jar.Main jarTool = new sun.tools.jar.Main(newErr, newErr, "config_utils");

        String[] args = new String[]{"uf", jarFile, file};
        jarTool.run(args);

        System.setErr(oldErr);

        try {
            errorBytes.close();
            newErr.close();
        } catch (Exception e) {
            throw new OpenEJBException(messages.format("file.0020", jarFile, e.getLocalizedMessage()));
        }

        String error = new String(errorBytes.toByteArray());
        if (error.indexOf("java.io.IOException") != -1) {

            int begin = error.indexOf(':') + 1;
            int end = error.indexOf('\n');
            String message = error.substring(begin, end);
            throw new OpenEJBException(messages.format("file.0003", file, jarFile, message));
        }

    }

    public static JarFile getJarFile(String jarFile) throws OpenEJBException {
        /*[1.1]  Get the jar ***************/
        JarFile jar = null;
        try {
            File file = new File(jarFile);
            jar = new JarFile(file);
        } catch (FileNotFoundException e) {
            throw new OpenEJBException(messages.format("file.0001", jarFile, e.getLocalizedMessage()));
        } catch (IOException e) {
            throw new OpenEJBException(messages.format("file.0002", jarFile, e.getLocalizedMessage()));
        }
        return jar;
    }

    public static ClassLoader getContextClassLoader() {
        return (ClassLoader) java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public Object run() {
                        return Thread.currentThread().getContextClassLoader();
                    }
                }
        );
    }

}
