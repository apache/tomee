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

import org.apache.openejb.spi.Serializer;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

// optimization: replace HashTable with HashMap (vc no debug hashmap)

public class RAFPassivater implements PassivationStrategy {

    int fileID = 0;
    Hashtable masterTable = new Hashtable();

    static class Pointer {
        int fileid;
        long filepointer;
        int bytesize;

        public Pointer(int file, long pointer, int bytecount) {
            fileid = file;
            filepointer = pointer;
            bytesize = bytecount;
        }
    }

    public void init(Properties props) throws org.apache.openejb.SystemException {
    }

    public synchronized void passivate(Hashtable stateTable)
            throws org.apache.openejb.SystemException {
        try {
            fileID++;

            RandomAccessFile ras = new RandomAccessFile(System.getProperty("java.io.tmpdir", File.separator + "tmp") + File.separator + "passivation" + fileID + ".ser", "rw");
            Enumeration enumeration = stateTable.keys();
            Pointer lastPointer = null;
            while (enumeration.hasMoreElements()) {
                Object id = enumeration.nextElement();
                Object obj = stateTable.get(id);
                byte [] bytes = Serializer.serialize(obj);
                long filepointer = ras.getFilePointer();

                if (lastPointer == null) lastPointer = new Pointer(fileID, filepointer, (int) (filepointer));
                else
                    lastPointer = new Pointer(fileID, filepointer, (int) (filepointer - lastPointer.filepointer));

                masterTable.put(id, lastPointer);
                ras.write(bytes);
            }
            ras.close();
        } catch (Exception e) {
            throw new org.apache.openejb.SystemException(e);
        }
    }

    public synchronized Object activate(Object primaryKey)
            throws org.apache.openejb.SystemException {

        Pointer pointer = (Pointer) masterTable.get(primaryKey);
        if (pointer == null)
            return null;

        try {
            RandomAccessFile ras = new RandomAccessFile(System.getProperty("java.io.tmpdir", File.separator + "tmp") + File.separator + "passivation" + pointer.fileid + ".ser", "r");
            byte [] bytes = new byte[(int) pointer.bytesize];
            ras.seek(pointer.filepointer);
            ras.readFully(bytes);
            ras.close();
            return Serializer.deserialize(bytes);
        } catch (Exception e) {
            throw new org.apache.openejb.SystemException(e);
        }

    }

}