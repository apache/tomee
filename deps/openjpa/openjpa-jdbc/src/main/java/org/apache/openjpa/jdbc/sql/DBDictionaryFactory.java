/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.jdbc.sql;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.conf.JDBCConfigurationImpl;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.lib.conf.PluginValue;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.StoreException;
import org.apache.openjpa.util.UserException;

/**
 * Factory class to instantiate a dictionary. It will use
 * the following heuristic:
 * <ul>
 * <li>Check to see if there is a DictionaryClass property,
 * and if so, use that to instantiate the dictionary.</li>
 * <li>Check the URL in the JDBCConfiguration against a list
 * of pre-defined URLs for various dictionaries.</li>
 * <li>Check the driver in the JDBCConfiguration against a list of known
 * patterns.</li>
 * <li>Acquire a connection and check its database metadata.</li>
 * <li>Return an instance of the generic DBDictionary.</li>
 * </ul>
 *
 * @author Marc Prud'hommeaux
 * @nojavadoc
 */
public class DBDictionaryFactory {

    private static final Localizer _loc = Localizer.forPackage
        (DBDictionaryFactory.class);

    /**
     * Create the dictionary for the given class name and properties.
     */
    public static DBDictionary newDBDictionary(JDBCConfiguration conf,
        String dclass, String props) {
        return newDBDictionary(conf, dclass, props, null);
    }

    /**
     * Attempt to create the dictionary from the given connection URL and
     * driver name, either or both of which may be null. If the dictionary
     * cannot be calculated, returns null.
     */
    public static DBDictionary calculateDBDictionary(JDBCConfiguration conf,
        String url, String driver, String props) {
        String dclass = dictionaryClassForString(getProtocol(url), conf);
        if (dclass == null)
            dclass = dictionaryClassForString(driver, conf);
        if (dclass == null)
            return null;
        return newDBDictionary(conf, dclass, props);
    }

    /**
     * Create the dictionary using connection metadata to determine its type.
     */
    public static DBDictionary newDBDictionary(JDBCConfiguration conf,
        DataSource ds, String props) {
        Connection conn = null;
        try {
            conn = ds.getConnection();
            DatabaseMetaData meta = conn.getMetaData();
            String dclass = dictionaryClassForString(meta.getDatabaseProductName(), conf);            
            if (dclass == null)
                dclass = dictionaryClassForString(getProtocol(meta.getURL()), conf);
            if (dclass != null && dclass.contains("MySQL")) {
                // MariaDB returns "MySQL" for product name, need to verify by looking at product version.
                final String checkMariaDB = dictionaryClassForString(meta.getDatabaseProductVersion(), conf);
                if (checkMariaDB != null) {
                    dclass = checkMariaDB;
                }
            }
            if (dclass == null)
                dclass = DBDictionary.class.getName();
            return newDBDictionary(conf, dclass, props, conn);
        } catch (SQLException se) {
            throw new StoreException(se).setFatal(true);
        } finally {
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException se) {
                }
        }
    }

    /*
     * Returns the "jdbc:" protocol of the url parameter. Looks for the prefix
     * string up to the 3rd ':' or the 1st '@', '/' or '\', whichever comes
     * first.
     * 
     * This method is package qualified so that TestDictionaryFactory class can
     * access and test this method behavior.
     */
    static String getProtocol(String url) {
        String protocol = null;
        if (!StringUtils.isEmpty(url)) {
            if (url.startsWith("jdbc:")) {
                int colonCount = 1;
                int next = "jdbc:".length();
                int protoEnd = next;
                while (colonCount < 3 && next < url.length()) {
                    char c = url.charAt(next++);
                    if (c == ':') {
                        ++colonCount;
                        protoEnd = next;
                    } else if (c == '@' || c == '/' || c == '\\') {
                        --next;
                        break;
                    }
                }
                protocol = url.substring(0, protoEnd);
            }
        }
        return protocol;
    }

    /**
     * Create the dictionary using the given class name and properties; the
     * connection may be null if not supplied to the factory.
     */
    private static DBDictionary newDBDictionary(JDBCConfiguration conf,
        String dclass, String props, Connection conn) {
        DBDictionary dict = null;
        try {
            Class<?> c = Class.forName(dclass, true,
                AccessController.doPrivileged(
                    J2DoPrivHelper.getClassLoaderAction(
                        DBDictionary.class)));
            dict = (DBDictionary) AccessController.doPrivileged(
                J2DoPrivHelper.newInstanceAction(c));
        } catch (ClassNotFoundException cnfe) {
            // if the dictionary was not found, make another attempt
            // at loading the dictionary using the current thread.
            try {
                Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(dclass);
                dict = (DBDictionary) AccessController.doPrivileged(
                        J2DoPrivHelper.newInstanceAction(c));
            } catch (Exception e) {
                if (e instanceof PrivilegedActionException)
                    e = ((PrivilegedActionException) e).getException();
                throw new UserException(e).setFatal(true);
            }
        } catch (Exception e) {
            if (e instanceof PrivilegedActionException)
                e = ((PrivilegedActionException) e).getException();
            throw new UserException(e).setFatal(true);
        }

        // warn if we could not locate the appropriate dictionary
        Log log = conf.getLog(JDBCConfiguration.LOG_JDBC);
        if (log.isWarnEnabled() && dict.getClass() == DBDictionary.class)
            log.warn(_loc.get("warn-generic"));

        if (log.isInfoEnabled()) {
            String infoString = "";
            if (conn != null) {
                try {
                    DatabaseMetaData meta = conn.getMetaData();
                    infoString = " (" + meta.getDatabaseProductName() + " "
                        + meta.getDatabaseProductVersion() + " ,"
                        + meta.getDriverName() + " "
                        + meta.getDriverVersion() + ")";
                } catch (SQLException se) {
                    if (log.isTraceEnabled())
                        log.trace(se.toString(), se);
                }
            }

            log.info(_loc.get("using-dict", dclass, infoString));
        }

        // set the dictionary's metadata
        Configurations.configureInstance(dict, conf, props, "DBDictionary");
        if (conn != null) {
            try {
                dict.connectedConfiguration(conn);
            } catch (SQLException se) {
                throw new StoreException(se).setFatal(true);
            }
        }
        return dict;
    }

    /**
     * Guess the dictionary class name to use based on the product string.
     */
    private static String dictionaryClassForString(String prod
        , JDBCConfiguration conf) {
        if (StringUtils.isEmpty(prod))
            return null;
        prod = prod.toLowerCase();

        PluginValue dbdictionaryPlugin = ((JDBCConfigurationImpl) conf)
            .dbdictionaryPlugin;

        if (prod.indexOf("oracle") != -1)
            return dbdictionaryPlugin.unalias("oracle");
        if (prod.indexOf("sqlserver") != -1)
            return dbdictionaryPlugin.unalias("sqlserver");
        if (prod.indexOf("jsqlconnect") != -1)
            return dbdictionaryPlugin.unalias("sqlserver");
        if (prod.indexOf("mariadb") != -1)
            return dbdictionaryPlugin.unalias("mariadb");
        if (prod.indexOf("mysql") != -1)
            return dbdictionaryPlugin.unalias("mysql");
        if (prod.indexOf("postgres") != -1)
            return dbdictionaryPlugin.unalias("postgres");
        if (prod.indexOf("sybase") != -1)
            return dbdictionaryPlugin.unalias("sybase");
        if (prod.indexOf("adaptive server") != -1)
            return dbdictionaryPlugin.unalias("sybase");
        if (prod.indexOf("informix") != -1 || prod.indexOf("ids") != -1)
            return dbdictionaryPlugin.unalias("informix");
        if (prod.indexOf("ingres") != -1)
            return dbdictionaryPlugin.unalias("ingres");
        if (prod.indexOf("hsql") != -1)
            return dbdictionaryPlugin.unalias("hsql");
        if (prod.indexOf("foxpro") != -1)
            return dbdictionaryPlugin.unalias("foxpro");
        if (prod.indexOf("interbase") != -1)
            return InterbaseDictionary.class.getName();
        if (prod.indexOf("jdatastore") != -1)
            return JDataStoreDictionary.class.getName();
        if (prod.indexOf("borland") != -1)
            return JDataStoreDictionary.class.getName();
        if (prod.indexOf("access") != -1)
            return dbdictionaryPlugin.unalias("access");
        if (prod.indexOf("pointbase") != -1)
            return dbdictionaryPlugin.unalias("pointbase");
        if (prod.indexOf("empress") != -1)
            return dbdictionaryPlugin.unalias("empress");
        if (prod.indexOf("firebird") != -1)
            return FirebirdDictionary.class.getName();
        if (prod.indexOf("cache") != -1)
            return CacheDictionary.class.getName();
        if (prod.indexOf("derby") != -1)
            return dbdictionaryPlugin.unalias("derby");
        if (prod.indexOf("sapdb") != -1) {
            return dbdictionaryPlugin.unalias("maxdb");
        }
        // test h2 in a special way, because there's a decent chance the string 
        // h2 could appear in the URL of another database
        if (prod.indexOf("jdbc:h2:") != -1)
            return dbdictionaryPlugin.unalias("h2");
        if (prod.indexOf("h2 database") != -1)
            return dbdictionaryPlugin.unalias("h2");
        // test db2 last, because there's a decent chance this string could
        // appear in the URL of another database (like if the db is named
        // "testdb2" or something)
        if (prod.indexOf("db2") != -1 || prod.indexOf("as400") != -1)
            return dbdictionaryPlugin.unalias("db2");
        if (prod.indexOf("soliddb") != -1) 
            return dbdictionaryPlugin.unalias("soliddb");

        // known dbs that we don't support
        if (prod.indexOf("cloudscape") != -1)
            return DBDictionary.class.getName();
        if (prod.indexOf("daffodil") != -1)
            return DBDictionary.class.getName();
        if (prod.indexOf("idb") != -1) // instantdb
            return DBDictionary.class.getName();

        String prodClassName = dbdictionaryPlugin.unalias(prod);
        if (!StringUtils.equals(prod, prodClassName))
            return prodClassName;
        
        // give up
        return null;
    }

    /**
     * Return a string containing all the property values of the given
     * database metadata.
     */
    public static String toString(DatabaseMetaData meta)
        throws SQLException {
        String lineSep = J2DoPrivHelper.getLineSeparator();
        StringBuilder buf = new StringBuilder(4096);
        try {
            buf.append("catalogSeparator: ")
                .append(meta.getCatalogSeparator())
                .append(lineSep)
                .append("catalogTerm: ")
                .append(meta.getCatalogTerm())
                .append(lineSep)
                .append("databaseProductName: ")
                .append(meta.getDatabaseProductName())
                .append(lineSep)
                .append("databaseProductVersion: ")
                .append(meta.getDatabaseProductVersion())
                .append(lineSep)
                .append("driverName: ")
                .append(meta.getDriverName())
                .append(lineSep)
                .append("driverVersion: ")
                .append(meta.getDriverVersion())
                .append(lineSep)
                .append("extraNameCharacters: ")
                .append(meta.getExtraNameCharacters())
                .append(lineSep)
                .append("identifierQuoteString: ")
                .append(meta.getIdentifierQuoteString())
                .append(lineSep)
                .append("numericFunctions: ")
                .append(meta.getNumericFunctions())
                .append(lineSep)
                .append("procedureTerm: ")
                .append(meta.getProcedureTerm())
                .append(lineSep)
                .append("schemaTerm: ")
                .append(meta.getSchemaTerm())
                .append(lineSep)
                .append("searchStringEscape: ")
                .append(meta.getSearchStringEscape())
                .append(lineSep)
                .append("sqlKeywords: ")
                .append(meta.getSQLKeywords())
                .append(lineSep)
                .append("stringFunctions: ")
                .append(meta.getStringFunctions())
                .append(lineSep)
                .append("systemFunctions: ")
                .append(meta.getSystemFunctions())
                .append(lineSep)
                .append("timeDateFunctions: ")
                .append(meta.getTimeDateFunctions())
                .append(lineSep)
                .append("url: ")
                .append(meta.getURL())
                .append(lineSep)
                .append("userName: ")
                .append(meta.getUserName())
                .append(lineSep)
                .append("defaultTransactionIsolation: ")
                .append(meta.getDefaultTransactionIsolation())
                .append(lineSep)
                .append("driverMajorVersion: ")
                .append(meta.getDriverMajorVersion())
                .append(lineSep)
                .append("driverMinorVersion: ")
                .append(meta.getDriverMinorVersion())
                .append(lineSep)
                .append("maxBinaryLiteralLength: ")
                .append(meta.getMaxBinaryLiteralLength())
                .append(lineSep)
                .append("maxCatalogNameLength: ")
                .append(meta.getMaxCatalogNameLength())
                .append(lineSep)
                .append("maxCharLiteralLength: ")
                .append(meta.getMaxCharLiteralLength())
                .append(lineSep)
                .append("maxColumnNameLength: ")
                .append(meta.getMaxColumnNameLength())
                .append(lineSep)
                .append("maxColumnsInGroupBy: ")
                .append(meta.getMaxColumnsInGroupBy())
                .append(lineSep)
                .append("maxColumnsInIndex: ")
                .append(meta.getMaxColumnsInIndex())
                .append(lineSep)
                .append("maxColumnsInOrderBy: ")
                .append(meta.getMaxColumnsInOrderBy())
                .append(lineSep)
                .append("maxColumnsInSelect: ")
                .append(meta.getMaxColumnsInSelect())
                .append(lineSep)
                .append("maxColumnsInTable: ")
                .append(meta.getMaxColumnsInTable())
                .append(lineSep)
                .append("maxConnections: ")
                .append(meta.getMaxConnections())
                .append(lineSep)
                .append("maxCursorNameLength: ")
                .append(meta.getMaxCursorNameLength())
                .append(lineSep)
                .append("maxIndexLength: ")
                .append(meta.getMaxIndexLength())
                .append(lineSep)
                .append("maxProcedureNameLength: ")
                .append(meta.getMaxProcedureNameLength())
                .append(lineSep)
                .append("maxRowSize: ")
                .append(meta.getMaxRowSize())
                .append(lineSep)
                .append("maxSchemaNameLength: ")
                .append(meta.getMaxSchemaNameLength())
                .append(lineSep)
                .append("maxStatementLength: ")
                .append(meta.getMaxStatementLength())
                .append(lineSep)
                .append("maxStatements: ")
                .append(meta.getMaxStatements())
                .append(lineSep)
                .append("maxTableNameLength: ")
                .append(meta.getMaxTableNameLength())
                .append(lineSep)
                .append("maxTablesInSelect: ")
                .append(meta.getMaxTablesInSelect())
                .append(lineSep)
                .append("maxUserNameLength: ")
                .append(meta.getMaxUserNameLength())
                .append(lineSep)
                .append("isCatalogAtStart: ")
                .append(meta.isCatalogAtStart())
                .append(lineSep)
                .append("isReadOnly: ")
                .append(meta.isReadOnly())
                .append(lineSep)
                .append("nullPlusNonNullIsNull: ")
                .append(meta.nullPlusNonNullIsNull())
                .append(lineSep)
                .append("nullsAreSortedAtEnd: ")
                .append(meta.nullsAreSortedAtEnd())
                .append(lineSep)
                .append("nullsAreSortedAtStart: ")
                .append(meta.nullsAreSortedAtStart())
                .append(lineSep)
                .append("nullsAreSortedHigh: ")
                .append(meta.nullsAreSortedHigh())
                .append(lineSep)
                .append("nullsAreSortedLow: ")
                .append(meta.nullsAreSortedLow())
                .append(lineSep)
                .append("storesLowerCaseIdentifiers: ")
                .append(meta.storesLowerCaseIdentifiers())
                .append(lineSep)
                .append("storesLowerCaseQuotedIdentifiers: ")
                .append(meta.storesLowerCaseQuotedIdentifiers())
                .append(lineSep)
                .append("storesMixedCaseIdentifiers: ")
                .append(meta.storesMixedCaseIdentifiers())
                .append(lineSep)
                .append("storesMixedCaseQuotedIdentifiers: ")
                .append(meta.storesMixedCaseQuotedIdentifiers())
                .append(lineSep)
                .append("storesUpperCaseIdentifiers: ")
                .append(meta.storesUpperCaseIdentifiers())
                .append(lineSep)
                .append("storesUpperCaseQuotedIdentifiers: ")
                .append(meta.storesUpperCaseQuotedIdentifiers())
                .append(lineSep)
                .append("supportsAlterTableWithAddColumn: ")
                .append(meta.supportsAlterTableWithAddColumn())
                .append(lineSep)
                .append("supportsAlterTableWithDropColumn: ")
                .append(meta.supportsAlterTableWithDropColumn())
                .append(lineSep)
                .append("supportsANSI92EntryLevelSQL: ")
                .append(meta.supportsANSI92EntryLevelSQL())
                .append(lineSep)
                .append("supportsANSI92FullSQL: ")
                .append(meta.supportsANSI92FullSQL())
                .append(lineSep)
                .append("supportsANSI92IntermediateSQL: ")
                .append(meta.supportsANSI92IntermediateSQL())
                .append(lineSep)
                .append("supportsCatalogsInDataManipulation: ")
                .append(meta.supportsCatalogsInDataManipulation())
                .append(lineSep)
                .append("supportsCatalogsInIndexDefinitions: ")
                .append(meta.supportsCatalogsInIndexDefinitions())
                .append(lineSep)
                .append("supportsCatalogsInPrivilegeDefinitions: ")
                .append(meta.supportsCatalogsInPrivilegeDefinitions())
                .append(lineSep)
                .append("supportsCatalogsInProcedureCalls: ")
                .append(meta.supportsCatalogsInProcedureCalls())
                .append(lineSep)
                .append("supportsCatalogsInTableDefinitions: ")
                .append(meta.supportsCatalogsInTableDefinitions())
                .append(lineSep)
                .append("supportsColumnAliasing: ")
                .append(meta.supportsColumnAliasing())
                .append(lineSep)
                .append("supportsConvert: ")
                .append(meta.supportsConvert())
                .append(lineSep)
                .append("supportsCoreSQLGrammar: ")
                .append(meta.supportsCoreSQLGrammar())
                .append(lineSep)
                .append("supportsCorrelatedSubqueries: ")
                .append(meta.supportsCorrelatedSubqueries())
                .append(lineSep)
                .append(
                    "supportsDataDefinitionAndDataManipulationTransactions: ")
                .append(meta.
                    supportsDataDefinitionAndDataManipulationTransactions())
                .append(lineSep)
                .append("supportsDataManipulationTransactionsOnly: ")
                .append(meta.supportsDataManipulationTransactionsOnly())
                .append(lineSep)
                .append("supportsDifferentTableCorrelationNames: ")
                .append(meta.supportsDifferentTableCorrelationNames())
                .append(lineSep)
                .append("supportsExpressionsInOrderBy: ")
                .append(meta.supportsExpressionsInOrderBy())
                .append(lineSep)
                .append("supportsExtendedSQLGrammar: ")
                .append(meta.supportsExtendedSQLGrammar())
                .append(lineSep)
                .append("supportsFullOuterJoins: ")
                .append(meta.supportsFullOuterJoins())
                .append(lineSep)
                .append("supportsGroupBy: ")
                .append(meta.supportsGroupBy())
                .append(lineSep)
                .append("supportsGroupByBeyondSelect: ")
                .append(meta.supportsGroupByBeyondSelect())
                .append(lineSep)
                .append("supportsGroupByUnrelated: ")
                .append(meta.supportsGroupByUnrelated())
                .append(lineSep)
                .append("supportsIntegrityEnhancementFacility: ")
                .append(meta.supportsIntegrityEnhancementFacility())
                .append(lineSep)
                .append("supportsLikeEscapeClause: ")
                .append(meta.supportsLikeEscapeClause())
                .append(lineSep)
                .append("supportsLimitedOuterJoins: ")
                .append(meta.supportsLimitedOuterJoins())
                .append(lineSep)
                .append("supportsMinimumSQLGrammar: ")
                .append(meta.supportsMinimumSQLGrammar())
                .append(lineSep)
                .append("supportsMixedCaseIdentifiers: ")
                .append(meta.supportsMixedCaseIdentifiers())
                .append(lineSep)
                .append("supportsMixedCaseQuotedIdentifiers: ")
                .append(meta.supportsMixedCaseQuotedIdentifiers())
                .append(lineSep)
                .append("supportsMultipleResultSets: ")
                .append(meta.supportsMultipleResultSets())
                .append(lineSep)
                .append("supportsMultipleTransactions: ")
                .append(meta.supportsMultipleTransactions())
                .append(lineSep)
                .append("supportsNonNullableColumns: ")
                .append(meta.supportsNonNullableColumns())
                .append(lineSep)
                .append("supportsOpenCursorsAcrossCommit: ")
                .append(meta.supportsOpenCursorsAcrossCommit())
                .append(lineSep)
                .append("supportsOpenCursorsAcrossRollback: ")
                .append(meta.supportsOpenCursorsAcrossRollback())
                .append(lineSep)
                .append("supportsOpenStatementsAcrossCommit: ")
                .append(meta.supportsOpenStatementsAcrossCommit())
                .append(lineSep)
                .append("supportsOpenStatementsAcrossRollback: ")
                .append(meta.supportsOpenStatementsAcrossRollback())
                .append(lineSep)
                .append("supportsOrderByUnrelated: ")
                .append(meta.supportsOrderByUnrelated())
                .append(lineSep)
                .append("supportsOuterJoins: ")
                .append(meta.supportsOuterJoins())
                .append(lineSep)
                .append("supportsPositionedDelete: ")
                .append(meta.supportsPositionedDelete())
                .append(lineSep)
                .append("supportsPositionedUpdate: ")
                .append(meta.supportsPositionedUpdate())
                .append(lineSep)
                .append("supportsSchemasInDataManipulation: ")
                .append(meta.supportsSchemasInDataManipulation())
                .append(lineSep)
                .append("supportsSchemasInIndexDefinitions: ")
                .append(meta.supportsSchemasInIndexDefinitions())
                .append(lineSep)
                .append("supportsSchemasInPrivilegeDefinitions: ")
                .append(meta.supportsSchemasInPrivilegeDefinitions())
                .append(lineSep)
                .append("supportsSchemasInProcedureCalls: ")
                .append(meta.supportsSchemasInProcedureCalls())
                .append(lineSep)
                .append("supportsSchemasInTableDefinitions: ")
                .append(meta.supportsSchemasInTableDefinitions())
                .append(lineSep)
                .append("supportsSelectForUpdate: ")
                .append(meta.supportsSelectForUpdate())
                .append(lineSep)
                .append("supportsStoredProcedures: ")
                .append(meta.supportsStoredProcedures())
                .append(lineSep)
                .append("supportsSubqueriesInComparisons: ")
                .append(meta.supportsSubqueriesInComparisons())
                .append(lineSep)
                .append("supportsSubqueriesInExists: ")
                .append(meta.supportsSubqueriesInExists())
                .append(lineSep)
                .append("supportsSubqueriesInIns: ")
                .append(meta.supportsSubqueriesInIns())
                .append(lineSep)
                .append("supportsSubqueriesInQuantifieds: ")
                .append(meta.supportsSubqueriesInQuantifieds())
                .append(lineSep)
                .append("supportsTableCorrelationNames: ")
                .append(meta.supportsTableCorrelationNames())
                .append(lineSep)
                .append("supportsTransactions: ")
                .append(meta.supportsTransactions())
                .append(lineSep)
                .append("supportsUnion: ")
                .append(meta.supportsUnion())
                .append(lineSep)
                .append("supportsUnionAll: ")
                .append(meta.supportsUnionAll())
                .append(lineSep)
                .append("usesLocalFilePerTable: ")
                .append(meta.usesLocalFilePerTable())
                .append(lineSep)
                .append("usesLocalFiles: ")
                .append(meta.usesLocalFiles())
                .append(lineSep)
                .append("allProceduresAreCallable: ")
                .append(meta.allProceduresAreCallable())
                .append(lineSep)
                .append("allTablesAreSelectable: ")
                .append(meta.allTablesAreSelectable())
                .append(lineSep)
                .append("dataDefinitionCausesTransactionCommit: ")
                .append(meta.dataDefinitionCausesTransactionCommit())
                .append(lineSep)
                .append("dataDefinitionIgnoredInTransactions: ")
                .append(meta.dataDefinitionIgnoredInTransactions())
                .append(lineSep)
                .append("doesMaxRowSizeIncludeBlobs: ")
                .append(meta.doesMaxRowSizeIncludeBlobs())
                .append(lineSep)
                .append("supportsBatchUpdates: ")
                .append(meta.supportsBatchUpdates());
        } catch (Throwable t) {
            // maybe abstract method error for jdbc 3 metadata method, or
            // other error
            buf.append(lineSep).append("Caught throwable: ").append(t);
        }

        return buf.toString();
    }
}
