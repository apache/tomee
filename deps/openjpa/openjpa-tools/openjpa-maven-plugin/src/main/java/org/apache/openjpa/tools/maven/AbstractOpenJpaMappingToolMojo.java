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
package org.apache.openjpa.tools.maven;


import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Entity;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.conf.JDBCConfigurationImpl;
import org.apache.openjpa.jdbc.meta.MappingTool;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.lib.meta.ClassArgParser;
import org.apache.openjpa.lib.util.Options;
import org.apache.openjpa.meta.MetaDataRepository;
import org.codehaus.plexus.util.FileUtils;

/**
 * Processes Application model classes and generate the DDL by running the 
 * OpenJPA MappingTool.
 * 
 * We have to split the generation of the SQL files and the mapping info
 * into 2 separate mojos, since the MappingTool struggles to generate both 
 * in one step.
 * 
 * @version $Id$
 * @since 1.0
 */
public abstract class AbstractOpenJpaMappingToolMojo extends AbstractOpenJpaMojo {
    /**
     * Argument to specify the action to take on each class. The available actions are:
     * buildSchema, validate
     *
     * @parameter
     */
    protected String action;
    /**
     * used for passing the action parameter to the mapping tool
     */
    protected static final String OPTION_ACTION = "action";

    /**
     * {@inheritDoc}
     *
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skipMojo()) {
            return;
        }

        if (!getEntityClasses().exists()) {
            FileUtils.mkdir(getEntityClasses().getAbsolutePath());
        }

        List<File> entities = findEntityClassFiles();

        mappingTool(entities);
    }

    /**
     * Processes a list of class file resources and perform the proper
     * mapping action.
     *
     * @param files class file resources to map.
     * @throws MojoExecutionException if the MappingTool detected an error
     */
    private void mappingTool(List<File> files) throws MojoExecutionException {
        extendRealmClasspath();

        Options opts = getOptions();

        filterPersistenceCapable(files, opts);

        // list of input files
        final String[] args = getFilePaths(files);

        boolean ok = Configurations.runAgainstAllAnchors(opts,
                new Configurations.Runnable() {
                    public boolean run(Options opts) throws IOException, SQLException {
                        JDBCConfiguration conf = new JDBCConfigurationImpl();
                        try {
                            return MappingTool.run(conf, args, opts);
                        } finally {
                            conf.close();
                        }
                    }
                }
        );

        if (!ok) {
            throw new MojoExecutionException("The OpenJPA MappingTool detected an error!");
        }

    }

    /**
     * Filter out all classes which are not PersistenceCapable.
     * This is needed since the MappingTool fails if it gets non-persistent capable classes.
     *
     * @param files List with classPath Files; non persistence classes will be removed
     * @param opts  filled configuration Options
     */
    private void filterPersistenceCapable(List<File> files, Options opts) {
        JDBCConfiguration conf = new JDBCConfigurationImpl();
        Configurations.populateConfiguration(conf, opts);
        MetaDataRepository repo = conf.newMetaDataRepositoryInstance();
        ClassArgParser cap = repo.getMetaDataFactory().newClassArgParser();

        Iterator<File> fileIt = files.iterator();
        while (fileIt.hasNext()) {
            File classPath = fileIt.next();

            Class[] classes = cap.parseTypes(classPath.getAbsolutePath());

            if (classes == null) {
                getLog().info("Found no classes for " + classPath.getAbsolutePath());
            } else {
                for (int i = 0; i < classes.length; i++) {
                    Class<?> cls = classes[i];

                    if (cls.getAnnotation(Entity.class) != null) {
                        getLog().debug("Found @Entity in class " + classPath);
                    } else if (implementsPersistenceCapable(cls)) {
                        getLog().debug("Found class " + classPath + " that implements interface "
                                + PersistenceCapable.class.getName());
                    } else {
                        getLog().debug("Removing non-entity class " + classPath);
                        fileIt.remove();
                    }
                }
            }
        }
    }


    /**
     * @param cls the Class to check
     * @return <code>true</code> if the given Class cls implements the interface {@link PersistenceCapable}
     */
    private boolean implementsPersistenceCapable(Class<?> cls) {
        boolean isPersistenceCapable = false;
        Class<?>[] interfaces = cls.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (interfaces[i].getName().equals(PersistenceCapable.class.getName())) {
                isPersistenceCapable = true;
                break;
            }
        }

        return isPersistenceCapable;
    }

}
