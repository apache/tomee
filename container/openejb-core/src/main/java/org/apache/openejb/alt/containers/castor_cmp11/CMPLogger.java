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
package org.apache.openejb.alt.containers.castor_cmp11;

import org.apache.openejb.util.Logger;

public class CMPLogger implements org.exolab.castor.persist.spi.LogInterceptor {
    protected final Logger logger = Logger.getInstance("OpenEJB.CastorCMP", "org.apache.openejb.alt.util.resources");
    protected final String db;

    public CMPLogger(String db) {
        this.db = db + ": ";
    }

    public void loading(java.lang.Object objClass, java.lang.Object identity) {
        logger.debug(db + "Loading an instance of " + objClass + " with identity \"" + identity + "\"");
    }

    public void creating(java.lang.Object objClass, java.lang.Object identity) {
        logger.debug(db + "Creating an instance of " + objClass + " with identity \"" + identity + "\"");
    }

    public void removing(java.lang.Object objClass, java.lang.Object identity) {
        logger.debug(db + "Removing an instance of " + objClass + " with identity \"" + identity + "\"");
    }

    public void storing(java.lang.Object objClass, java.lang.Object identity) {
        logger.debug(db + "Storing an instance of " + objClass + " with identity \"" + identity + "\"");
    }

    public void storeStatement(java.lang.String statement) {
        logger.debug(db + statement);
    }

    public void queryStatement(java.lang.String statement) {
        logger.debug(db + statement);
    }

    public void message(java.lang.String message) {
        logger.info(db + "JDO message:" + message);
    }

    public void exception(java.lang.Exception ex) {
        logger.info(db + "JDO exception:", ex);
    }

    public java.io.PrintWriter getPrintWriter() {
        return null;
    }
}

