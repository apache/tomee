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
package org.apache.openjpa.jdbc.meta.strats;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Defines all the abstract methods from AbstractLobTest to tests the
 * the LOB support with a Reader.
 *
 * @author Ignacio Andreu
 * @since 1.1.0
 */

public class TestReaderLob extends AbstractLobTest {

    protected LobEntity newLobEntity(String s, int id) {
        ReaderLobEntity rle = new ReaderLobEntity();
        rle.setId(id);
        if (s != null) {
            rle.setStream(new CharArrayReader(s.toCharArray()));
        } else {
            rle.setStream(null);
        }
        return rle;
    }

    protected LobEntity newLobEntityForLoadContent(String s, int id) {
        ReaderLobEntity rle = new ReaderLobEntity();
        rle.setId(id);
        rle.setStream(new ReaderWrapper(s));
        return rle;
    }

    protected Class getLobEntityClass() {
        return ReaderLobEntity.class;
    }

    protected String getSelectQuery() {
        return "SELECT o FROM ReaderLobEntity o";
    }

    protected String getStreamContentAsString(Object o) throws IOException {
        Reader r = (Reader) o;
        String content = "";
        char[] cs = new char[4];
        int read = -1;
        do {
            read = r.read(cs);
            if (read == -1) {
                return content;
            }
            content = content + (new String(cs)).substring(0, read);
        } while (true);
    }

    protected void changeStream(LobEntity le, String s) {
        le.setStream(new CharArrayReader(s.toCharArray()));
    }
}
