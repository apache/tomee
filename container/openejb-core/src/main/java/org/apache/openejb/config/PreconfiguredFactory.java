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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Info;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;

import javax.xml.bind.JAXBException;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @version $Rev$ $Date$
 */
public class PreconfiguredFactory {

    public static final String APP_INFO_XML = "META-INF/app-info.xml";
    public static final String APP_INFO_XML_PATH = "$PATH";
    public static final boolean FORCE_SCANNING = SystemInstance.get().getOptions().get("openejb.force.scanning", false);

    public static void dump(final Writer output, final AppInfo info) throws OpenEJBException {
        FileOutputStream fos = null;
        try {
            Info.marshal(info, output);
        } catch (JAXBException e) {
            throw new OpenEJBException(e);
        } finally {
            IO.close(fos);
        }
    }

    public static AppInfo loadDump(final String modulePath, final InputStream input) throws OpenEJBException {
        if (input == null || modulePath == null) {
            throw new OpenEJBException("input and modulePath can't be null");
        }

        String read;
        try {
            read = IO.slurp(input);
        } catch (IOException e) {
            throw new OpenEJBException(e);
        }

        // manage path
        read = read.replace(APP_INFO_XML_PATH, modulePath);

        // TODO: manage resources + containers

        final InputStream fis = new BufferedInputStream(new ByteArrayInputStream(read.getBytes()));
        try {
            return Info.unmarshal(fis);
        } catch (JAXBException e) {
            throw new OpenEJBException(e);
        } finally {
            IO.close(fis);
        }
    }

    public static AppInfo loadDump(final File file) {
        if (!file.exists() || FORCE_SCANNING) {
            return null;
        }

        InputStream is = null;
        if (file.isDirectory()) {
            final File xml = new File(file, APP_INFO_XML);
            if (xml.exists()) {
                try {
                    is = new FileInputStream(xml);
                } catch (FileNotFoundException e) {
                    // ignored: if this method returns null simply deploy the app normally
                }
            }
        } else { // an archive
            try {
                final JarFile jar = new JarFile(file);
                final JarEntry entry = jar.getJarEntry(APP_INFO_XML);
                if (entry != null) {
                    is = jar.getInputStream(entry);
                }
            } catch (Exception e) {
                // ignored too
            }
        }

        try {
            return loadDump(file.getAbsolutePath(), is);
        } catch (OpenEJBException e) {
            // ignored, it will return null and a standard deployment should be tried
        } finally {
            IO.close(is);
        }

        return null;
    }

    public static AppInfo configureApplication(final File file) throws OpenEJBException {
        return loadDump(file);
    }
}
