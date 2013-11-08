/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.lib.meta;

import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.Properties;
import java.util.zip.ZipFile;

import org.apache.openjpa.lib.util.J2DoPrivHelper;

import serp.util.Strings;

/**
 * Iterator over directories in the classpath.
 *
 * @author Abe White
 * @nojavadoc
 */
public class ClasspathMetaDataIterator extends MetaDataIteratorChain {

    /**
     * Default constructor; iterates over all classpath elements.
     */
    public ClasspathMetaDataIterator() throws IOException {
        this(null, null);
    }

    /**
     * Constructor; supply the classpath directories to scan and an optional
     * resource filter. The given directories may be null to scan all
     * classpath directories.
     */
    public ClasspathMetaDataIterator(String[] dirs, MetaDataFilter filter)
        throws IOException {
        Properties props = AccessController.doPrivileged(
            J2DoPrivHelper.getPropertiesAction()); 
        String path = props.getProperty("java.class.path");
        String[] tokens = Strings.split(path,
            props.getProperty("path.separator"), 0);

        for (int i = 0; i < tokens.length; i++) {
            if (dirs != null && dirs.length != 0 && !endsWith(tokens[i], dirs))
                continue;

            File file = new File(tokens[i]);
            if (!(AccessController.doPrivileged(
                J2DoPrivHelper.existsAction(file))).booleanValue())
                continue;
            if (AccessController.doPrivileged(J2DoPrivHelper
                .isDirectoryAction(file)).booleanValue())
                addIterator(new FileMetaDataIterator(file, filter));
            else if (tokens[i].endsWith(".jar")) {
                try {
                    ZipFile zFile = AccessController
                        .doPrivileged(J2DoPrivHelper.newZipFileAction(file));
                    addIterator(new ZipFileMetaDataIterator(zFile, filter));
                } catch (PrivilegedActionException pae) {
                    throw (IOException) pae.getException();
                }
            }
        }
    }

    /**
     * Return true if the given token ends with any of the given strings.
     */
    private static boolean endsWith(String token, String[] suffs) {
        for (int i = 0; i < suffs.length; i++)
            if (token.endsWith(suffs[i]))
                return true;
        return false;
    }
}
