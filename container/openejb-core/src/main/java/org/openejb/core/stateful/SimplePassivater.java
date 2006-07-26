package org.openejb.core.stateful;

import org.openejb.SystemException;
import org.openejb.core.EnvProps;
import org.openejb.loader.SystemInstance;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

public class SimplePassivater implements PassivationStrategy {
    private File sessionDirectory;
    final static protected org.apache.log4j.Category logger = org.apache.log4j.Category.getInstance("OpenEJB");

    public SimplePassivater() throws SystemException {
        init(null);
    }

    public void init(Properties props) throws org.openejb.SystemException {
        if (props == null) {
            props = new Properties();
        }

        String dir = props.getProperty(EnvProps.IM_PASSIVATOR_PATH_PREFIX);

        try {

            if (dir != null) {
                sessionDirectory = SystemInstance.get().getBase().getDirectory(dir);
            } else {
                sessionDirectory = new File(System.getProperty("java.io.tmpdir", File.separator + "tmp"));
            }
            logger.info("Using directory " + sessionDirectory + " for stateful session passivation");
        } catch (java.io.IOException e) {
            throw new org.openejb.SystemException(getClass().getName() + ".init(): can't use directory prefix " + dir + ":" + e);
        }
    }

    public void passivate(Object primaryKey, Object state)
            throws org.openejb.SystemException {
        try {

            String filename = primaryKey.toString().replace(':', '=');

            File sessionFile = new File(sessionDirectory, filename);

            logger.info("Passivating to file " + sessionFile);
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(sessionFile));

            oos.writeObject(state);// passivate just the bean instance
            oos.close();
            sessionFile.deleteOnExit();
        }
        catch (java.io.NotSerializableException nse) {
            logger.info("Passivation failed ", nse);
            throw new org.openejb.SystemException("The type " + nse.getMessage() + " in the bean class " + ((BeanEntry) state).bean.getClass().getName() + " is not serializable as mandated by the EJB specification.");
        }
        catch (Exception t) {
            logger.info("Passivation failed ", t);

            throw new org.openejb.SystemException(t);
        }

    }

    public void passivate(Hashtable hash) throws org.openejb.SystemException {
        Enumeration enumeration = hash.keys();
        while (enumeration.hasMoreElements()) {
            Object id = enumeration.nextElement();
            passivate(id, hash.get(id));
        }
    }

    public Object activate(Object primaryKey) throws org.openejb.SystemException {

        try {

            String filename = primaryKey.toString().replace(':', '=');

            File sessionFile = new File(sessionDirectory, filename);

            if (sessionFile.exists()) {
                logger.info("Activating from file " + sessionFile);

                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(sessionFile));
                Object state = ois.readObject();
                ois.close();
                sessionFile.delete();
                return state;
            } else {
                logger.info("Activation failed: file not found " + sessionFile);
                return null;
            }

        } catch (Exception t) {
            logger.info("Activation failed ", t);

            throw new org.openejb.SystemException(t);
        }

    }

}