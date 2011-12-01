package org.apache.openejb.util.classloader;

/**
 * Simply to be able to get rid of the openwebbeans classloader stuff
 * without patching it.
 *
 * @author rmannibucau
 */
public class MultipleClassLoader extends ClassLoader {
    private final ClassLoader second;

    public MultipleClassLoader(ClassLoader first, ClassLoader second) {
        super(first);
        this.second = second;
    }

    @Override public Class<?> loadClass(String name) throws ClassNotFoundException {
        try {
            return super.loadClass(name);
        } catch (ClassNotFoundException cnfe) {
            if (second != getParent()) {
                return loadClassSecond(name);
            }
            throw cnfe;
        } catch (NoClassDefFoundError ncdfe) {
            if (second != getParent()) {
                return loadClassSecond(name);
            }
            throw ncdfe;
        }
    }

    public Class<?> loadClassSecond(String name) throws ClassNotFoundException {
        return second.loadClass(name);
    }
}
