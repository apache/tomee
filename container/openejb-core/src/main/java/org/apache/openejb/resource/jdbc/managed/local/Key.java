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

package org.apache.openejb.resource.jdbc.managed.local;

import javax.sql.CommonDataSource;
import java.util.Objects;

public class Key {
    private final CommonDataSource ds;
    private final String user;
    private final String pwd;
    private final int hash;

    public Key(final CommonDataSource ds, final String user, final String pwd) {
        this.ds = ds;
        this.user = user;
        this.pwd = pwd;

        int result = ds.hashCode();
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (pwd != null ? pwd.hashCode() : 0);
        hash = result;
    }

    public CommonDataSource getDs() {
        return ds;
    }

    public String getUser() {
        return user;
    }

    public String getPwd() {
        return pwd;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Key key = Key.class.cast(o);
        return (ds == key.ds || ds.equals(key.ds)) &&
                Objects.equals(user, key.user) &&
                Objects.equals(pwd, key.pwd);
    }

    @Override
    public String toString() {
        return "Key{" +
                "ds=" + ds +
                ", user='" + user + '\'' +
                ", pwd='*****'" +
                ", hash=" + hash +
                '}';
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
