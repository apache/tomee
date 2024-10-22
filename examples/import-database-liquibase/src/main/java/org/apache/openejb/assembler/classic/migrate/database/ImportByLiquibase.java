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

package org.apache.openejb.assembler.classic.migrate.database;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.database.jvm.HsqlConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.DirectoryResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.resource.SearchPathResourceAccessor;

/**
 * This class is to be used instead of the ImportSql class, to import scripts
 * into the database.
 * 
 * @version $Rev$ $Date$
 */
public class ImportByLiquibase {
	private static final Logger LOGGER = Logger.getLogger(ImportByLiquibase.class.getName());

	public static final String IMPORT_FILE_PREFIX = "V_";
	public static final String IMPORT_FILE_EXTENSION = ".sql";

	private final DataSource dataSource;
	private final String resource;

	public ImportByLiquibase(final ClassLoader cl, final String resource, final DataSource ds) {
		this.dataSource = ds;
		this.resource = resource;

		if (dataSource == null) {
			throw new NullPointerException("datasource can't be null");
		}

	}

	@SuppressWarnings("deprecation")
	public void doImport() {

		try {

			List<String> sqlFiles = new ArrayList<String>();

			try {

				sqlFiles = listFilteredFiles(resource, Integer.MAX_VALUE);

				if (Objects.nonNull(sqlFiles)) {
					if (sqlFiles.isEmpty()) {
						LOGGER.severe("The Resource directory for sql files, can not to be empty.");
						throw new Exception("The Resource directory for sql files, can not to be empty.");
					}
				}

			} catch (final IOException e) {
				throw new RuntimeException("The Resource directory for sql files, can not to be empty.", e);
			}
			ChangeLogHistoryServiceFactory.getInstance().resetAll();
			for (String changelogPath : sqlFiles) {

				try (Liquibase liquibase = getLiquibase(changelogPath)) {
					liquibase.update("test");

				} catch (Exception e) {
					LOGGER.log(Level.SEVERE,"Error running Liquibase changelog",e.getCause());
					throw new RuntimeException("Error running Liquibase changelog", e);
				}
			}

		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE,"Can not create a statement, import scripts will be ignored",e.getCause());			
			return;
		}

	}
	
	public List<String> listFilteredFiles(String dir, int depth) throws IOException {
		try (Stream<Path> stream = Files.walk(Paths.get(dir), depth)) {
			return stream.filter(file -> !Files.isDirectory(file))
					.filter(file -> !file.getFileName().toString().startsWith(".")).map(Path::toString)
					.map(s -> s.replaceFirst(resource, "")).collect(Collectors.toList());
		}
	}
	
	private Liquibase getLiquibase(final String changelogPath) throws DatabaseException, SQLException {
		Liquibase liquibase = null;
		HsqlConnection hsqlConnection = new HsqlConnection(dataSource.getConnection());

		try {
			Path path = Paths.get(this.resource);

			ResourceAccessor resourceAccessor = new SearchPathResourceAccessor(
					new DirectoryResourceAccessor(path.toFile()));

			liquibase = new Liquibase(changelogPath, resourceAccessor, hsqlConnection);

		} catch (FileNotFoundException ex) {
			LOGGER.log(Level.SEVERE,"Can't create a statement, import scripts will be ignored",ex.getCause());			

		} catch (LiquibaseException ex) {
			LOGGER.log(Level.SEVERE,"Can't create a statement, import scripts will be ignored",ex.getCause());
		}
		return liquibase;
	}

}