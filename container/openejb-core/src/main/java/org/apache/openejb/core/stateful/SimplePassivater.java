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

package org.apache.openejb.core.stateful;

import org.apache.openejb.SystemException;
import org.apache.openejb.core.EnvProps;
import org.apache.openejb.core.ivm.EjbObjectInputStream;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.JavaSecurityManagers;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;

public class SimplePassivater implements PassivationStrategy {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");
    private File sessionDirectory;

    public SimplePassivater() throws SystemException {
        init(null);
    }

    @Override
    public void init(Properties props) throws SystemException {
        if (props == null) {
            props = new Properties();
        }

        final String dir = props.getProperty(EnvProps.IM_PASSIVATOR_PATH_PREFIX);

        try {
            if (dir != null) {
                sessionDirectory = SystemInstance.get().getBase().getDirectory(dir);
            } else {
                sessionDirectory = new File(JavaSecurityManagers.getSystemProperty("java.io.tmpdir", File.separator + "tmp"));
            }

            if (!sessionDirectory.exists() && !sessionDirectory.mkdirs()) {
                throw new IOException("Failed to create session directory: " + sessionDirectory.getAbsolutePath());
            }

            if (sessionDirectory.exists() && !sessionDirectory.isDirectory()) {
                throw new IOException("Session directory exists as a file: " + sessionDirectory.getAbsolutePath());
            }

            logger.info("Using directory " + sessionDirectory + " for stateful session passivation");

        } catch (final IOException e) {
            throw new SystemException(getClass().getName() + ".init(): can't use directory prefix " + dir + ":" + e, e);
        }
    }

    public void passivate(final Object primaryKey, final Object state) throws SystemException {
        try {
            final String filename = primaryKey.toString().replace(':', '=');

            final File sessionFile = new File(sessionDirectory, filename);

            logger.info("Passivating to file " + sessionFile);

            try (final OutputStream os = IO.write(sessionFile);
                 final ObjectOutputStream oos = new ObjectOutputStream(os)) {
                oos.writeObject(state);// passivate just the bean instance
            } finally {
                sessionFile.deleteOnExit();
            }

        } catch (final NotSerializableException nse) {
            logger.error("Passivation failed ", nse);
            throw (SystemException) new SystemException("The type " + nse.getMessage() + " is not serializable as mandated by the EJB specification.").initCause(nse);
        } catch (final Exception t) {
            logger.error("Passivation failed ", t);
            throw new SystemException(t);
        }
    }

    @Override
    public void passivate(final Map hash) throws SystemException {
        for (final Object o : hash.entrySet()) {
            passivate(((Map.Entry) o).getKey(), ((Map.Entry) o).getValue());
        }
    }

    @Override
    public Object activate(final Object primaryKey) throws SystemException {
        try {
            final String filename = primaryKey.toString().replace(':', '=');

            final File sessionFile = new File(sessionDirectory, filename);

            if (sessionFile.exists()) {
                logger.info("Activating from file " + sessionFile);

                try (final InputStream source = IO.read(sessionFile);
                     final ObjectInputStream ois = new EjbObjectInputStream(source)) {
                    return ois.readObject();
                } finally {
                    if (!sessionFile.delete()) {
                        sessionFile.deleteOnExit();
                    }
                }
            } else {
                logger.info("Activation failed: file not found " + sessionFile);
                return null;
            }
        } catch (final Exception t) {
            logger.info("Activation failed ", t);

            throw new SystemException(t);
        }
    }
}