/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.injection.jpa;

import jakarta.annotation.Resource;
import jakarta.ejb.Singleton;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Singleton
public class MoviesDirect {

    @Resource(name="moviesDatabaseUnmanaged")
    private DataSource ds;

    public int count() {
        try (final Connection connection = ds.getConnection()) {
            try (final PreparedStatement ps = connection.prepareStatement("select count(1) from movie")) {
                try (final ResultSet rs = ps.executeQuery()) {
                    if (rs != null && rs.next()) {
                        return rs.getInt(1);
                    } else {
                        return 0;
                    }
                }
            }
        } catch (final Exception e) {
            throw new RuntimeException("Unable to execute query against the database");
        }
    }
}
