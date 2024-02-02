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
package org.apache.openejb.junit5;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.openejb.jee.WebApp;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.junit.jupiter.api.Test;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@RunWithApplicationComposer
public class AppComposerMemoryReleaseTest {
	
    @Module
    @Classes(innerClassesAsBean = true, cdi = true)
    public WebApp web() {
        return new WebApp();
    }
    
    @Inject
    private MemoryPayloadBean memoryPayloadBean;
    
    /*
     * Bean producing high memory payload easy to locate 
     * by retained size or classname in heapdump.
     */
	@ApplicationScoped
	public static class MemoryPayloadBean implements Serializable {
		private static final long serialVersionUID = -5814319377355637770L;
		private static final int PAYLOAD_SIZE = 1024 * 1024 * 16;
		private String payload;
		
		public MemoryPayloadBean() {
			super();
			/*
			 * Big Memory Payload
			 */
			char[] array = new char[PAYLOAD_SIZE];
			Arrays.fill(array, '@');
			this.payload = new String(array);
		}

		public String getPayload() {
			return this.payload;
		}
	}
	
    @Test
    public void run() {
        assertNotNull(memoryPayloadBean);
        assertNotNull(memoryPayloadBean.getPayload());
    }
}
