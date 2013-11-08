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
package org.apache.openjpa.lib.ant;

import java.io.File;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.conf.ConfigurationImpl;
import org.apache.openjpa.lib.conf.ConfigurationProvider;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.lib.conf.ProductDerivations;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.MultiClassLoader;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

/**
 * Ant tasks all have a nested <code>&lt;config&rt;</code> tag, which uses
 * the configuration as a bean-like task. E.g., you can do:
 *
 * <code>
 * &lt;mytask&rt;<br />
 * &nbsp;&nbsp;&lt;config connectionUserName="foo"/&rt;<br />
 * &lt;/mytask&rt;
 * </code>
 *
 * The default configuration for the system will be used if the
 * <code>&lt;config&rt;</code> subtask is excluded.
 *
 * @nojavadoc
 */
public abstract class AbstractTask extends MatchingTask {

    private static final Localizer _loc = Localizer.forPackage
        (AbstractTask.class);

    protected final List<FileSet> fileSets = new ArrayList<FileSet>();
    protected boolean haltOnError = true;
    protected Path classpath = null;
    protected boolean useParent = false;
    protected boolean isolate = false;

    private ConfigurationImpl _conf = null;
    private AntClassLoader _cl = null;

    /**
     * Set whether we want the task to ignore all errors.
     */
    public void setHaltOnError(boolean haltOnError) {
        this.haltOnError = haltOnError;
    }

    /**
     * Whether we want the ClassLoader to be isolated from
     * all other ClassLoaders
     */
    public void setIsolate(boolean isolate) {
        this.isolate = isolate;
    }

    /**
     * Whether we want to delegate to the parent ClassLoader
     * for resolveing classes. This may "taint" classes.
     */
    public void setUseParentClassloader(boolean useParent) {
        this.useParent = useParent;
    }

    /**
     * The task configuration.
     */
    public Configuration getConfiguration() {
        if (_conf == null) {
             _conf = newConfiguration();
            _conf.setDeferResourceLoading(true);
        }    
        return _conf;
    }

    /**
     * Implement this method to return a configuration object for the
     * product in use.
     */
    protected abstract ConfigurationImpl newConfiguration();

    /**
     * Perform the task action on the given files.
     */
    protected abstract void executeOn(String[] files) throws Exception;

    /**
     * Return the classloader to use.
     */
    protected ClassLoader getClassLoader() {
        if (_cl != null)
            return _cl;

        if (classpath != null)
            _cl = new AntClassLoader(getProject(), classpath, useParent);
        else
            _cl = new AntClassLoader(getProject().getCoreLoader(), getProject(),
                new Path(getProject()), useParent);
        _cl.setIsolated(isolate);

        return _cl;
    }

    /**
     * Helper method to throw a standard exception if the task is not given
     * any files to execute on. Implementations might call this method as
     * the first step in {@link #executeOn} to validate that they are given
     * files to work on.
     */
    protected void assertFiles(String[] files) {
        if (files.length == 0)
            throw new BuildException(_loc.get("no-filesets").getMessage());
    }

    public void setClasspath(Path classPath) {
        createClasspath().append(classPath);
    }

    public Path createClasspath() {
        if (classpath == null)
            classpath = new Path(getProject());
        return classpath.createPath();
    }

    public Object createConfig() {
        return getConfiguration();
    }

    public void addFileset(FileSet set) {
        fileSets.add(set);
    }

    public void execute() throws BuildException {
        // if the user didn't supply a conf file, load the default
        if (_conf == null)
            _conf = newConfiguration();
        ConfigurationProvider cp = null;
        String propertiesResource = _conf.getPropertiesResource();
        if ( propertiesResource == null) {            
            cp = ProductDerivations.loadDefaults(getConfigPropertiesResourceLoader());           
        } else if (_conf.isDeferResourceLoading() && !StringUtils.isEmpty(propertiesResource)) {
            Map<String, String> result = Configurations.parseConfigResource(propertiesResource);
            String path = result.get(Configurations.CONFIG_RESOURCE_PATH);
            String anchor = result.get(Configurations.CONFIG_RESOURCE_ANCHOR);
            cp = ProductDerivations.load(path, anchor, getConfigPropertiesResourceLoader());
        }

        if (cp != null){
            cp.setInto(_conf);
        }

        String[] files = getFiles();
        try {
            executeOn(files);
        } catch (Throwable e) {
            e.printStackTrace();
            if (haltOnError)
                throw new BuildException(e);
        } finally {
            _conf.close();
            _conf = null;
        }
    }

    private MultiClassLoader getConfigPropertiesResourceLoader() {
        MultiClassLoader loader = AccessController
                .doPrivileged(J2DoPrivHelper.newMultiClassLoaderAction());
        loader.addClassLoader(getClassLoader());
        loader.addClassLoader(AccessController.doPrivileged(
                J2DoPrivHelper.getClassLoaderAction(_conf.getClass())));        
        return loader;
    }

    private String[] getFiles() {
        List<String> files = new ArrayList<String>();
        for(FileSet fs : fileSets) { 
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());

            String[] dsFiles = ds.getIncludedFiles();
            for (int j = 0; j < dsFiles.length; j++) {
                File f = new File(dsFiles[j]);
                if (!( AccessController.doPrivileged(J2DoPrivHelper
                    .isFileAction(f))).booleanValue())
                    f = new File(ds.getBasedir(), dsFiles[j]);
                files.add(AccessController.doPrivileged(
                    J2DoPrivHelper.getAbsolutePathAction(f)));
            }
        }
        return (String[]) files.toArray(new String[files.size()]);
    }
}

