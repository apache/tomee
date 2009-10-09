/**
 *
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
package org.apache.openejb.resource.jdbc;

import java.sql.SQLException;

import javax.persistence.Basic;

import junit.framework.TestCase;

public class PasswordCodecTest extends TestCase {
	
	private static final String PLAIN_PWD = "david"; 
	
    public void testPlainCodec() {
    	PasswordCodec codec = new PlainTextPasswordCodec();
    	assertEquals(PLAIN_PWD, new String(codec.encode(PLAIN_PWD)));
    	assertEquals(PLAIN_PWD, codec.decode(PLAIN_PWD.toCharArray()));
    }
	
	public void testStaticDesCodec() {
		PasswordCodec codec = new StaticDESPasswordCodec();
		char[] tmp = codec.encode(PLAIN_PWD);
		assertEquals(PLAIN_PWD, codec.decode(tmp));
    }
	
	public void testGetDataSourcePlugin() throws Exception {
        // all current known plugins
        assertPluginClass("PlainText", PlainTextPasswordCodec.class);
        assertPluginClass("Static3DES", StaticDESPasswordCodec.class);

        // null
        try {
            BasicDataSourceUtil.getPasswordCodec(null);
            fail("Should throw an exception when no codec is found.");
        } catch (Exception e) {
            // OK
        }

        // empty string
        try {
            BasicDataSourceUtil.getPasswordCodec("");
            fail("Should throw an exception when no codec is found.");
        } catch (Exception e) {
            // OK
        }
        
        // try the FQN of the target codec
        assertNotNull(BasicDataSourceUtil.getPasswordCodec(PlainTextPasswordCodec.class.getName()));
    }

    private void assertPluginClass(String pluginName, Class<? extends PasswordCodec> pluginClass) throws SQLException {
        PasswordCodec plugin = BasicDataSourceUtil.getPasswordCodec(pluginName);
        assertNotNull(plugin);
        assertSame(pluginClass, plugin.getClass());
    }
    
}