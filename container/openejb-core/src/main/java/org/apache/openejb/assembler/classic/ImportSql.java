package org.apache.openejb.assembler.classic;

import org.apache.openejb.OpenEJBRuntimeException;
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

public class ImportSql {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, EntityManagerFactoryCallable.class.getName());

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
        } catch (IOException e) {
            throw new OpenEJBRuntimeException("can't look for init sql script", e);
        }
    }

    public boolean hasSomethingToImport() {
        return !done && imports != null && imports.hasMoreElements();
    }

    public void doImport() {
        Statement statement;
        if (hasSomethingToImport()) {
            Connection connection = null;
            try {
                connection = dataSource.getConnection();
                statement = connection.createStatement();
            } catch (SQLException e) {
                LOGGER.error("can't create a statement, import scripts will be ignored", e);
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException ignored) {
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
                } catch (SQLException e) {
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
        } catch (IOException e) {
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
                    if (!trimmedSql.toLowerCase().startsWith("select")) {
                        statement.executeUpdate(trimmedSql);
                    } else { // why could it be the case?
                        statement.executeQuery(trimmedSql);
                    }

                    SQLWarning warnings = statement.getWarnings();
                    while (warnings != null) {
                        LOGGER.warning(warnings.getMessage());
                        warnings = warnings.getNextWarning();
                    }
                } catch (SQLException e) {
                    LOGGER.error("error importing script " + script.toExternalForm(), e);
                }
            }
        } catch (IOException e) {
            LOGGER.error("can't import " + script.toExternalForm(), e);
        }
    }
}
