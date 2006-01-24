package org.openejb.loader;

import java.util.Properties;
import java.util.HashMap;

import org.openejb.util.FileUtils;

public class SystemInstance {

    private final long startTime = System.currentTimeMillis();
    private final Properties properties;
    private final FileUtils home;
    private final FileUtils base;
    private final ClassLoader classLoader;
    private final HashMap components;
    private final ClassPath classPath;

    private SystemInstance(Properties properties) throws Exception {
        this.components = new HashMap();
        this.properties = new Properties();
        this.properties.putAll(System.getProperties());
        this.properties.putAll(properties);

        this.home = new FileUtils("openejb.home", "user.dir", this.properties);
        this.base = new FileUtils("openejb.base", "openejb.home", this.properties);
        this.classPath = ClassPathFactory.createClassPath(this.properties.getProperty("openejb.loader", "context"));
        this.classLoader = classPath.getClassLoader();

        this.properties.setProperty("openejb.home", home.getDirectory().getCanonicalPath());
        this.properties.setProperty("openejb.base", base.getDirectory().getCanonicalPath());
    }

    public long getStartTime() {
        return startTime;
    }

    public Properties getProperties() {
        return properties;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public Object setProperty(String key, String value) {
        return properties.setProperty(key, value);
    }

    public FileUtils getHome() {
        return home;
    }

    public FileUtils getBase() {
        return base;
    }

    public ClassPath getClassPath() {
        return classPath;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * I'm not sure how this will play out, but I've used class instances instead of strings
     * for lookups as class instances are classloader scoped and there is an implicit "namespace"
     * associated with that.  Theoretically, you can't lookup things that you can't already see
     * in your classloader.
     *
     * @param type
     * @return the object associated with the class type or null
     * @throws IllegalStateException of the component isn't found
     */
    public Object getComponent(Class type) throws IllegalStateException {
        Object component = components.get(type);
        if (component == null){
            throw new IllegalStateException("No such component exists: "+type.getName() +"(scope: "+type.getClassLoader()+")");
        }
        return components.get(type);
    }

    /**
     *
     * @param type the class type of the component required
     */
    public Object setComponent(Class type, Object value) {
        return components.put(type, value);
    }

    private static SystemInstance system;

    static {
        try {
            system = new SystemInstance(System.getProperties());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create default instance of SystemInstance", e);
        }
    }

    private static boolean initialized;

    public static void init(Properties properties) throws Exception {
        if (initialized) return;
        system = new SystemInstance(properties);
        initialized = true;
    }

    public static SystemInstance get() {
        return system;
    }

}
