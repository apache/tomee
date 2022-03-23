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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.openejb.arquillian.tests.cmp.sample;

import javax.naming.Context;
import javax.naming.InitialContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;


public class MoviesServlet2 extends HttpServlet {

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        try {

            final PrintWriter pw = resp.getWriter();

            final Context initial = new InitialContext();

            final MoviesBusinessHome home = (MoviesBusinessHome) initial.lookup("java:comp/env/ejb/MoviesBusiness");

            final MoviesBusiness moviesBusiness = home.create();
            moviesBusiness.doLogic();

            final DataSource ds = (DataSource) initial.lookup("java:comp/env/db/DataSource");
            try (final Connection connection = ds.getConnection();
                 final PreparedStatement ps = connection.prepareStatement(
                         "select TABLE_NAME, COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH " +
                                 "from INFORMATION_SCHEMA.COLUMNS where TABLE_SCHEMA = 'PUBLIC' and TABLE_NAME in ('ACTOR', 'MOVIE', 'ACTOR_MOVIE')");

                 final ResultSet rs = ps.executeQuery()) {

                final ResultSetMetaData metaData = rs.getMetaData();
                final int columnCount = metaData.getColumnCount();

                final String[] columnNames = new String[columnCount];

                for (int c = 0; c < columnCount; c++) {
                    columnNames[c] = metaData.getColumnName(c + 1);
                }

                while (rs.next()) {
                    final StringBuilder sb = new StringBuilder();

                    for (int c = 0; c < columnCount; c++) {
                        if (c > 0) {
                            sb.append(", ");
                        }

                        sb.append(columnNames[c]).append(": ").append(rs.getString(c + 1));
                    }

                    pw.println(sb.toString());
                }
            }

            pw.flush();

        } catch (final Exception ex) {
            throw new ServletException(ex);
        }
    }

}
