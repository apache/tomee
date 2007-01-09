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
package org.apache.openejb.core.stateful;

import org.apache.openejb.SystemException;
import org.apache.openejb.core.EnvProps;
import org.apache.openejb.loader.SystemInstance;

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

    public void init(Properties props) throws org.apache.openejb.SystemException {
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
            throw new org.apache.openejb.SystemException(getClass().getName() + ".init(): can't use directory prefix " + dir + ":" + e);
        }
    }

    public void passivate(Object primaryKey, Object state)
            throws org.apache.openejb.SystemException {
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
            throw (SystemException) new SystemException("The type " + nse.getMessage() + " in the bean class " + ((BeanEntry) state).bean.getClass().getName() + " is not serializable as mandated by the EJB specification.").initCause(nse);
        }
        catch (Exception t) {
            logger.info("Passivation failed ", t);

            throw new org.apache.openejb.SystemException(t);
        }

    }

    public void passivate(Hashtable hash) throws org.apache.openejb.SystemException {
        Enumeration enumeration = hash.keys();
        while (enumeration.hasMoreElements()) {
            Object id = enumeration.nextElement();
            passivate(id, hash.get(id));
        }
    }

    public Object activate(Object primaryKey) throws org.apache.openejb.SystemException {

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

            throw new org.apache.openejb.SystemException(t);
        }

    }

}