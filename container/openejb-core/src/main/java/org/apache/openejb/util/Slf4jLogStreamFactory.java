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
package org.apache.openejb.util;

/**
 * Simple Slf4jLogStream, not much configuration needed, as slf4j is just a facade 
 *
 */

public class Slf4jLogStreamFactory implements LogStreamFactory {
	@Override
	public LogStream createLogStream(LogCategory logCategory) {
		return new Slf4jLogStream(logCategory);
	}

    public Slf4jLogStreamFactory() {
        System.setProperty("openjpa.Log", "slf4j");
        System.setProperty("org.apache.cxf.Logger", "org.apache.cxf.common.logging.Slf4jLogger");
        // no need to configure internals:
        // by default we are using JUL
        // if the user set log4j he wants to configure it himself
        // so let him doing
    }
}
