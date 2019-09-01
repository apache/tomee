/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.cdi.produces.field;

import javax.enterprise.inject.Produces;

public class LogFactory {

    private int type = 2;

    @Produces
    LogHandler handler;

    public LogFactory() {
        handler = getLogHandler();
    }

    public LogHandler getLogHandler() {
        switch (type) {
            case 1:
                return new FileHandler("@Produces created FileHandler!");
            case 2:
                return new DatabaseHandler("@Produces created DatabaseHandler!");
            case 3:
            default:
                return new ConsoleHandler("@Produces created ConsoleHandler!");
        }

    }
}
