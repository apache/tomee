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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.client;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @version $Rev$ $Date$
 */
public class DataSourceMetaData implements Externalizable {

    private static final long serialVersionUID = 5531437575034018602L;
    private transient String jdbcUrl;
    private transient String jdbcDriver;
    private transient String defaultPassword;
    private transient String defaultUserName;
    private transient ProtocolMetaData metaData;

    public DataSourceMetaData() {
    }

    public DataSourceMetaData(final String jdbcDriver, final String jdbcUrl, final String defaultUserName, final String defaultPassword) {
        this.defaultPassword = defaultPassword;
        this.defaultUserName = defaultUserName;
        this.jdbcDriver = jdbcDriver;
        this.jdbcUrl = jdbcUrl;
    }

    public void setMetaData(final ProtocolMetaData metaData) {
        this.metaData = metaData;
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        final byte version = in.readByte(); // future use

        jdbcDriver = (String) in.readObject();
        jdbcUrl = (String) in.readObject();
        defaultUserName = (String) in.readObject();
        defaultPassword = (String) in.readObject();
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        // write out the version of the serialized data for future use
        out.writeByte(1);

        out.writeObject(jdbcDriver);
        out.writeObject(jdbcUrl);
        out.writeObject(defaultUserName);
        out.writeObject(defaultPassword);
    }

    public String getDefaultPassword() {
        return defaultPassword;
    }

    public String getDefaultUserName() {
        return defaultUserName;
    }

    public String getJdbcDriver() {
        return jdbcDriver;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }
}
