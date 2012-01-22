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

public class Log4jLogStreamFactory implements LogStreamFactory {
    public LogStream createLogStream(LogCategory logCategory) {
        return new Log4jLogStream(logCategory);
    }

    public Log4jLogStreamFactory() {
        System.setProperty("openjpa.Log", "log4j");
        System.setProperty("org.apache.cxf.Logger", "org.apache.cxf.common.logging.Log4jLogger");
        // no need to configure internals:
        // by default we are using JUL and its default is ok.
        // if the user set log4j he wants to configure it himself
        // so let him doing
    }
}
