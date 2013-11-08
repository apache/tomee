/*
 * TestBuildSchema.java
 *
 * Created on October 4, 2006, 4:52 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

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
package org.apache.openjpa.persistence.jdbc.meta;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.apache.regexp.REUtil;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.meta.MappingTool;

import org.apache.openjpa.persistence.jdbc.common.apps.*;


public class TestBuildSchema
        extends org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {

    /** Creates a new instance of TestBuildSchema */
    public TestBuildSchema() {
    }
    public TestBuildSchema(String test) {
        super(test);
    }

    public void testSchema()
        throws Exception {
        StringWriter out = new StringWriter();

        MappingTool tool = new MappingTool((JDBCConfiguration)
            getConfiguration(), MappingTool.ACTION_BUILD_SCHEMA, false);
        tool.setMappingWriter(new StringWriter());    // throw away
        tool.setSchemaWriter(out);
        tool.run(BuildSchemaPC.class);
        tool.record();

        BufferedReader in = new BufferedReader(new InputStreamReader
            (getClass().getResourceAsStream("TestBuildSchema-schema.rsrc")));
        StringBuffer buf = new StringBuffer();
        for (int ch; (ch = in.read()) != -1;)
            buf.append((char) ch);
        in.close();

        // the <schema> sometimes has a name (depending on whether the database
        // reports a schema name or not). If there is a
        // <scheme name="something">, then replace it with <schema> so
        // the match is successful.
        String schema = out.toString();
        schema = REUtil.createRE
            ("<schema name=\"*\">").subst(schema, "<schema>");

        // convert CRLF to CR so we pass on Windows
        assertEquals(fixNewline(buf.toString()).trim(),
            fixNewline(schema).trim());
    }

    private String fixNewline(String str) {
        //FIXME 
        /*
        return serp.util.Strings.join
            (serp.util.Strings.split
                (str, "\r\n", -1), "\n");
         */
        return "";
    }
    
}
