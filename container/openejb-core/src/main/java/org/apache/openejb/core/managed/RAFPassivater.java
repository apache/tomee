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

import org.apache.openejb.SystemException;
import org.apache.openejb.spi.Serializer;
import org.apache.openejb.util.JavaSecurityManagers;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

// optimization: replace HashTable with HashMap (vc no debug hashmap)

public class RAFPassivater implements PassivationStrategy {

    int fileID;
    Hashtable masterTable = new Hashtable();

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

    public void init(final Properties props) throws SystemException {
    }

    public synchronized void passivate(final Map stateTable)
        throws SystemException {
        try (final RandomAccessFile ras =
                     new RandomAccessFile(JavaSecurityManagers.getSystemProperty("java.io.tmpdir", File.separator + "tmp") +
                             File.separator + "passivation" + fileID + ".ser", "rw")) {
            fileID++;

            final Iterator iterator = stateTable.keySet().iterator();
            Pointer lastPointer = null;
            while (iterator.hasNext()) {
                final Object id = iterator.next();
                final Object obj = stateTable.get(id);
                final byte[] bytes = Serializer.serialize(obj);
                final long filepointer = ras.getFilePointer();

                if (lastPointer == null) {
                    lastPointer = new Pointer(fileID, filepointer, (int) filepointer);
                } else {
                    lastPointer = new Pointer(fileID, filepointer, (int) (filepointer - lastPointer.filepointer));
                }

                masterTable.put(id, lastPointer);
                ras.write(bytes);
            }
        } catch (final Exception e) {
            throw new SystemException(e);
        }
    }

    public synchronized Object activate(final Object primaryKey)
        throws SystemException {

        final Pointer pointer = (Pointer) masterTable.get(primaryKey);
        if (pointer == null) {
            return null;
        }

        try (final RandomAccessFile ras =
                     new RandomAccessFile(JavaSecurityManagers.getSystemProperty("java.io.tmpdir", File.separator + "tmp") +
                             File.separator + "passivation" + pointer.fileid + ".ser", "r")) {
            final byte[] bytes = new byte[pointer.bytesize];
            ras.seek(pointer.filepointer);
            ras.readFully(bytes);
            ras.close();
            return Serializer.deserialize(bytes);
        } catch (final Exception e) {
            throw new SystemException(e);
        }

    }

}