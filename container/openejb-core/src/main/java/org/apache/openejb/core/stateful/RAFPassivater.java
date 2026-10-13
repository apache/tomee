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
import org.apache.openejb.spi.Serializer;
import org.apache.openejb.util.JavaSecurityManagers;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class RAFPassivater implements PassivationStrategy {

    int fileID;
    HashMap masterTable = new HashMap();
    private Path directory;

    static class Pointer {

        int fileid;
        long filepointer;
        int bytesize;

        public Pointer(final int file, final long pointer, final int bytecount) {
            fileid = file;
            filepointer = pointer;
            bytesize = bytecount;
        }
    }

    @Override
    public void init(final Properties props) throws SystemException {
    }

    // the files are deserialized on activation, so keep them in an owner-only
    // directory with an unpredictable name instead of the shared tmp dir
    private File passivationFile(final int id) throws IOException {
        if (directory == null) {
            final String tmp = JavaSecurityManagers.getSystemProperty("java.io.tmpdir", File.separator + "tmp");
            directory = Files.createTempDirectory(Paths.get(tmp), "openejb-passivation-");
        }
        return directory.resolve("passivation" + id + ".ser").toFile();
    }

    private File createPassivationFile(final int id) throws IOException {
        final File file = passivationFile(id);
        // fails if the file already exists and never follows a link
        Files.createFile(file.toPath());
        return file;
    }

    @Override
    public synchronized void passivate(final Map stateTable)
        throws SystemException {
        final int id = fileID++;
        try (final RandomAccessFile ras = new RandomAccessFile(createPassivationFile(id), "rw")) {
            final Iterator iterator = stateTable.keySet().iterator();
            while (iterator.hasNext()) {
                final Object key = iterator.next();
                final byte[] bytes = Serializer.serialize(stateTable.get(key));
                masterTable.put(key, new Pointer(id, ras.getFilePointer(), bytes.length));
                ras.write(bytes);
            }
        } catch (final Exception e) {
            throw new SystemException(e);
        }
    }

    @Override
    public synchronized Object activate(final Object primaryKey)
        throws SystemException {

        final Pointer pointer = (Pointer) masterTable.get(primaryKey);
        if (pointer == null) {
            return null;
        }

        try (final RandomAccessFile ras = new RandomAccessFile(passivationFile(pointer.fileid), "r")) {
            final byte[] bytes = new byte[pointer.bytesize];
            ras.seek(pointer.filepointer);
            ras.readFully(bytes);
            return Serializer.deserialize(bytes);
        } catch (final Exception e) {
            throw new SystemException(e);
        }

    }

}