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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;

/**
 * This class is to be used instead of the ImportSql class, to import scripts
 * into the database.
 * 
 * @version $Rev$ $Date$
 */
public class ImportByFlyway {
	private static final Logger LOGGER = Logger.getLogger(ImportByFlyway.class.getName());

	public static final String IMPORT_FILE_PREFIX = "V_";
	public static final String IMPORT_FILE_EXTENSION = ".sql";

	private final DataSource dataSource;
	private final String resource;

	public ImportByFlyway(final ClassLoader cl, final String resource, final DataSource ds) {
		this.dataSource = ds;
		this.resource = resource;

		if (dataSource == null) {
			throw new NullPointerException("datasource can't be null");
		}

	}

	public void doImport() {

		try {

			try {
				
				List<String> sqlFiles = listFilteredFiles(resource, Integer.MAX_VALUE);

				if (Objects.nonNull(sqlFiles)) {
					if (sqlFiles.isEmpty()) {
						LOGGER.severe("The Resource directory for sql files, can not to be empty.");
						throw new Exception("The Resource directory for sql files, can not to be empty.");
					}
				}

			} catch (final IOException e) {
				throw new RuntimeException("The Resource directory for sql files, can not to be empty.", e);
			}

			Flyway flyway = Flyway.configure().locations("filesystem:src/test/resources").dataSource(dataSource)
					.cleanDisabled(false).load();

			flyway.clean();
			flyway.migrate();

		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Can not create a statement, import scripts will be ignored", e);
			return;
		}

	}

	public void doValidate() {
		String selectAllByMail = "SELECT id, description FROM table_test";

		try (PreparedStatement statement = dataSource.getConnection().prepareStatement(selectAllByMail)) {

			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				LOGGER.info("id:" + resultSet.getInt("id") + " description:" + resultSet.getString("description"));

			}
		} catch (Exception ex) {
			LOGGER.log(Level.SEVERE, "Can not create a statement, import scripts will be ignored", ex);			
		}

	}

	public List<String> listFilteredFiles(String dir, int depth) throws IOException {
		try (Stream<Path> stream = Files.walk(Paths.get(dir), depth)) {
			return stream.filter(file -> !Files.isDirectory(file))
					.filter(file -> !file.getFileName().toString().startsWith(".")).map(Path::toString)
					.map(s -> s.replaceFirst(resource, "")).collect(Collectors.toList());
		}
	}

}