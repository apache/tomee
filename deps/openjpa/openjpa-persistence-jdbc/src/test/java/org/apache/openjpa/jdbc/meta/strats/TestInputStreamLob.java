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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.openjpa.jdbc.sql.PostgresDictionary;

/**
 * Defines all the abstract methods from AbstractLobTest to test
 * the LOB support with an InputStream.
 *
 * @author Ignacio Andreu
 * @since 1.1.0
 */

public class TestInputStreamLob extends AbstractLobTest {

    @Override
    public void setUp() throws Exception {
        supportedDatabases.add(PostgresDictionary.class);
        super.setUp();
    }

    protected LobEntity newLobEntity(String s, int id) {
        InputStreamLobEntity isle = new InputStreamLobEntity();
        isle.setId(id);
        if (s != null) {
            isle.setStream(new ByteArrayInputStream(s.getBytes()));
        } else {
            isle.setStream(null);
        }
        return isle;
    }

    protected LobEntity newLobEntityForLoadContent(String s, int id) {
        InputStreamLobEntity isle = new InputStreamLobEntity();
        isle.setId(id);
        isle.setStream(new InputStreamWrapper(s));
        return isle;
    }

    protected Class getLobEntityClass() {
        return InputStreamLobEntity.class;
    }

    protected String getSelectQuery() {
        return "SELECT o FROM InputStreamLobEntity o";
    }

    protected String getStreamContentAsString(Object o) throws IOException {
        InputStream is = (InputStream) o;
        String content = "";
        byte[] bs = new byte[1024];
        int read = -1;
        do {
            read = is.read(bs);
            if (read == -1) {
                return content;
            }
            content = content + (new String(bs)).substring(0, read);
        } while (true);
    }

    protected void changeStream(LobEntity le, String s) {
        le.setStream(new ByteArrayInputStream(s.getBytes()));
    }
}
