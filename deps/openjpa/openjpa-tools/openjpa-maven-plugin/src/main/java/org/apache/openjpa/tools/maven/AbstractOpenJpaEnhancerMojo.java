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


import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;

import org.apache.openjpa.enhance.PCEnhancer;
import org.apache.openjpa.lib.util.Options;

import java.io.File;
import java.util.List;

/**
 * The base class for all enhancement mojos.
 * @version $Id: AbstractOpenJpaTestEnhancerMojo.java 9137 2009-02-28 21:55:03Z struberg $
 * @since 1.1
 */
public abstract class AbstractOpenJpaEnhancerMojo extends AbstractOpenJpaMojo {

    /**
     * The JPA spec requires that all persistent classes define a no-arg constructor.
     * This flag tells the enhancer whether to add a protected no-arg constructor
     * to any persistent classes that don't already have one.
     *
     * @parameter default-value="true"
     */
    protected boolean addDefaultConstructor;
    /**
     * used for passing the addDefaultConstructor parameter to the enhnacer tool
     */
    private static final String OPTION_ADD_DEFAULT_CONSTRUCTOR = "addDefaultConstructor";

    /**
     * Whether to throw an exception when it appears that a property access entity
     * is not obeying the restrictions placed on property access.
     *
     * @parameter default-value="false"
     */
    protected boolean enforcePropertyRestrictions;
    /**
     * used for passing the enforcePropertyRestrictions parameter to the enhnacer tool
     */
    private static final String OPTION_ENFORCE_PROPERTY_RESTRICTION = "enforcePropertyRestrictions";

    /**
     * Tell the PCEnhancer to use a temporary classloader for enhancement.
     * If you enable this feature, then no depending artifacts from the classpath will be used!
     * Please note that you have to disable the tmpClassLoader for some cases in OpenJPA-1.2.1
     * due to an extended parsing strategy.
     *
     * @parameter default-value="false"
     */
    protected boolean tmpClassLoader;
    /**
     * used for passing the tmpClassLoader parameter to the enhnacer tool
     */
    private static final String OPTION_USE_TEMP_CLASSLOADER = "tcl";


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

        enhance(entities);
    }

    /**
     * Get the options for the OpenJPA enhancer tool.
     *
     * @return populated Options
     */
    protected Options getOptions() throws MojoExecutionException {
        // options
        Options opts = createOptions();

        // put the standard options into the list also
        opts.put(OPTION_ADD_DEFAULT_CONSTRUCTOR, Boolean.toString(addDefaultConstructor));
        opts.put(OPTION_ENFORCE_PROPERTY_RESTRICTION, Boolean.toString(enforcePropertyRestrictions));
        opts.put(OPTION_USE_TEMP_CLASSLOADER, Boolean.toString(tmpClassLoader));

        return opts;
    }

    /**
     * Processes a list of class file resources that are to be enhanced.
     *
     * @param files class file resources to enhance.
     * @throws MojoExecutionException if the enhancer encountered a failure
     */
    private void enhance(List<File> files) throws MojoExecutionException {
        Options opts = getOptions();

        // list of input files
        String[] args = getFilePaths(files);

        boolean ok = false;

        if (!tmpClassLoader) {
            extendRealmClasspath();
        }

        ok = PCEnhancer.run(args, opts);

        if (!ok) {
            throw new MojoExecutionException("The OpenJPA Enhancer tool detected an error!");
        }
    }

}
