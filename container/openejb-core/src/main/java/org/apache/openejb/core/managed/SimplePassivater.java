/*
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
package org.apache.openejb.core.managed;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.openejb.SystemException;
import org.apache.openejb.core.EnvProps;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;


public class SimplePassivater implements PassivationStrategy {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");
    private File sessionDirectory;

    public SimplePassivater() throws SystemException {
        init(null);
    }

    public void init(Properties props) throws SystemException {
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
            throw new SystemException(getClass().getName() + ".init(): can't use directory prefix " + dir + ":" + e, e);
        }
    }

    public void passivate(Object primaryKey, Object state) throws SystemException {
        try {
            String filename = primaryKey.toString().replace(':', '=');

            File sessionFile = new File(sessionDirectory, filename);

            logger.info("Passivating to file " + sessionFile);
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(sessionFile));

            oos.writeObject(state);// passivate just the bean instance
            oos.close();
            sessionFile.deleteOnExit();
        } catch (java.io.NotSerializableException nse) {
            logger.error("Passivation failed ", nse);
            throw (SystemException) new SystemException("The type " + nse.getMessage() + " is not serializable as mandated by the EJB specification.").initCause(nse);
        } catch (Exception t) {
            logger.error("Passivation failed ", t);
            throw new SystemException(t);
        }
    }

    public void passivate(Map hash) throws SystemException {
        for (Object id : hash.keySet()) {
            passivate(id, hash.get(id));
        }
    }

    public Object activate(Object primaryKey) throws SystemException {
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

            throw new SystemException(t);
        }
    }
}