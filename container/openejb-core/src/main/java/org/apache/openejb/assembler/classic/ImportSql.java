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

package org.apache.openejb.assembler.classic;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.sql.DataSource;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Locale;

public class ImportSql {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, EntityManagerFactoryCallable.class.getName());

    public static final String OPENEJB_SQL_IMPORT = "openejb.sql.import";

    public static final String IMPORT_FILE_PREFIX = "import-";
    public static final String IMPORT_FILE_EXTENSION = ".sql";

    private final DataSource dataSource;
    private boolean done;
    private final Enumeration<URL> imports;

    public ImportSql(final ClassLoader cl, final String resource, final DataSource ds) {
        dataSource = ds;
        done = false;

        if (dataSource == null) {
            throw new NullPointerException("datasource can't be null");
        }

        try {
            imports = cl.getResources(IMPORT_FILE_PREFIX.concat(resource).concat(IMPORT_FILE_EXTENSION));
        } catch (final IOException e) {
            throw new OpenEJBRuntimeException("can't look for init sql script", e);
        }
    }

    public boolean hasSomethingToImport() {
        return !done && imports != null && imports.hasMoreElements() && SystemInstance.get().getOptions().get(OPENEJB_SQL_IMPORT, true);
    }

    public void doImport() {
        final Statement statement;
        if (hasSomethingToImport()) {
            Connection connection = null;
            try {
                connection = dataSource.getConnection();
                statement = connection.createStatement();
            } catch (final SQLException e) {
                LOGGER.error("can't create a statement, import scripts will be ignored", e);
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (final SQLException ignored) {
                        // no-op
                    }
                }
                return;
            }

            try {
                while (imports.hasMoreElements()) {
                    final URL scriptToImport = imports.nextElement();
                    LOGGER.info("importing " + scriptToImport.toExternalForm());

                    importSql(scriptToImport, statement);
                }
            } finally {
                try {
                    connection.close();
                } catch (final SQLException e) {
                    // ignored
                }
                done = true;
            }
        }
    }

    private void importSql(final URL script, final Statement statement) {
        final BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(script.openStream())));
        } catch (final IOException e) {
            LOGGER.error("can't open " + script.toExternalForm(), e);
            return;
        }

        try {
            for (String sql = bufferedReader.readLine(); sql != null; sql = bufferedReader.readLine()) {
                String trimmedSql = sql.trim();

                // empty or comment
                if (trimmedSql.isEmpty() || trimmedSql.startsWith("--") || trimmedSql.startsWith("//") || trimmedSql.startsWith("/*")) {
                    continue;
                }

                if (trimmedSql.endsWith(";")) {
                    trimmedSql = trimmedSql.substring(0, trimmedSql.length() - 1);
                }

                try {
                    if (!trimmedSql.toLowerCase(Locale.ENGLISH).startsWith("select")) {
                        statement.executeUpdate(trimmedSql);
                    } else { // why could it be the case?
                        statement.executeQuery(trimmedSql);
                    }

                    SQLWarning warnings = statement.getWarnings();
                    while (warnings != null) {
                        LOGGER.warning(warnings.getMessage());
                        warnings = warnings.getNextWarning();
                    }
                } catch (final SQLException e) {
                    LOGGER.error("error importing script " + script.toExternalForm(), e);
                }
            }
        } catch (final IOException e) {
            LOGGER.error("can't import " + script.toExternalForm(), e);
        }
    }
}
