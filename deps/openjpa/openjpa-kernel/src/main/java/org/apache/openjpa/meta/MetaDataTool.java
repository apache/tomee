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
package org.apache.openjpa.meta;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.meta.ClassArgParser;
import org.apache.openjpa.lib.util.Files;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.Options;
import org.apache.openjpa.util.MetaDataException;

/**
 * Tool for generating default metadata.
 *
 * @since 0.3.0
 * @author Abe White
 */
public class MetaDataTool
    implements MetaDataModes {

    public static final String ACTION_ADD = "add";
    public static final String ACTION_DROP = "drop";

    public static final String[] ACTIONS = new String[]{
        ACTION_ADD,
        ACTION_DROP,
    };

    private static Localizer _loc = Localizer.forPackage(MetaDataTool.class);

    private final OpenJPAConfiguration _conf;
    private final String _action;

    private final Set _drop;
    private MetaDataRepository _repos = null;
    private File _file = null;
    private Writer _writer = null;
    private boolean _flush = false;

    /**
     * Constructor. Supply configuration and action.
     */
    public MetaDataTool(OpenJPAConfiguration conf, String action) {
        _conf = conf;
        _action = (action == null) ? ACTION_ADD : action;

        if (ACTION_DROP.equals(_action))
            _drop = new HashSet();
        else
            _drop = null;
    }

    /**
     * The action supplied on construction.
     */
    public String getAction() {
        return _action;
    }

    /**
     * The file to generate metadata to.
     */
    public File getFile() {
        return _file;
    }

    /**
     * The file to generate metadata to.
     */
    public void setFile(File file) {
        _file = file;
    }

    /**
     * The writer to generate metadata to.
     */
    public Writer getWriter() {
        return _writer;
    }

    /**
     * The writer to generate metadata to.
     */
    public void setWriter(Writer writer) {
        _writer = writer;
    }

    /**
     * The repository to use to hold metadata.
     */
    public MetaDataRepository getRepository() {
        if (_repos == null) {
            _repos = newRepository();
            _repos.setResolve(MODE_MAPPING, false);
            MetaDataFactory factory = _repos.getMetaDataFactory();
            factory.getDefaults().setIgnoreNonPersistent(false);
            factory.setStoreMode(MetaDataFactory.STORE_VERBOSE);
        }
        return _repos;
    }

    /**
     * Create a new metadata repository.
     */
    protected MetaDataRepository newRepository() {
        return _conf.newMetaDataRepositoryInstance();
    }

    /**
     * The repository to use to hold metadata.
     */
    public void setRepository(MetaDataRepository repos) {
        _repos = repos;
    }

    /**
     * Reset state. This is called automatically after every {@link #record}.
     */
    public void clear() {
        _repos = null;
        if (_drop != null)
            _drop.clear();
        _flush = false;
    }

    /**
     * Generate new metadata for the given class.
     */
    public void run(Class cls) {
        if (cls == null)
            return;
        if (ACTION_DROP.equals(_action))
            _drop.add(cls);
        else if (ACTION_ADD.equals(_action))
            add(cls);
        else
            throw new IllegalArgumentException("action == " + _action);
    }

    private void add(Class cls) {
        // assume all user-defined types are PCs
        ClassMetaData meta = getRepository().addMetaData(cls);
        FieldMetaData[] fmds = meta.getDeclaredFields();
        for (int i = 0; i < fmds.length; i++) {
            if (fmds[i].getDeclaredTypeCode() == JavaTypes.OBJECT
                && fmds[i].getDeclaredType() != Object.class)
                fmds[i].setDeclaredTypeCode(JavaTypes.PC);
        }
        meta.setSource(_file, meta.getSourceType(), _file == null ? "" : _file.getPath());
        _flush = true;
    }

    /**
     * Record metadata changes.
     */
    public void record() {
        MetaDataRepository repos = getRepository();
        MetaDataFactory mdf = repos.getMetaDataFactory();
        try {
            if (_drop != null && !_drop.isEmpty()
                && !mdf.drop((Class[]) _drop.toArray(new Class[_drop.size()]),
                MODE_META | MODE_MAPPING | MODE_QUERY, null)) {
                Log log = _conf.getLog(OpenJPAConfiguration.LOG_METADATA);
                if (log.isWarnEnabled())
                    log.warn(_loc.get("bad-drop", _drop));
            }
            if (!_flush)
                return;

            ClassMetaData[] metas = repos.getMetaDatas();
            Map output = null;

            // if we're outputting to stream, set all metas to same file so
            // they get placed in single string
            if (_writer != null) {
                output = new HashMap();
                File tmp = new File("openjpatmp");
                for (int i = 0; i < metas.length; i++)
                    metas[i].setSource(tmp, metas[i].getSourceType(), tmp.getPath());
            }
            if (!mdf.store(metas, new QueryMetaData[0],
                new SequenceMetaData[0], MODE_META, output))
                throw new MetaDataException(_loc.get("bad-store"));
            if (_writer != null) {
                PrintWriter out = new PrintWriter(_writer);
                for (Iterator itr = output.values().iterator();
                    itr.hasNext();)
                    out.println((String) itr.next());
                out.flush();
            }
        }
        finally {
            clear();
        }
    }

    /**
     * Usage: java org.apache.openjpa.meta.MetaDataTool [option]*
     * [-action/-a &lt;add | drop&gt;]
     * &lt;class name | .java file | .class file&gt;+
     *  Where the following options are recognized.
     * <ul>
     * <li><i>-properties/-p &lt;properties file or resource&gt;</i>: The path
     * or resource name of a OpenJPA properties file containing information
     * as outlined in {@link OpenJPAConfiguration}. Optional.</li>
     * <li><i>-&lt;property name&gt; &lt;property value&gt;</i>: All bean
     * properties of the OpenJPA {@link OpenJPAConfiguration} can be set by
     * using their names and supplying a value.</li>
     * <li><i>-file/-f &lt;stdout | output file or resource&gt;</i>: The path
     * or resource name of a file the metadata should be generated to.
     * If the given file already contains metadata, the generated
     * metadata will be merged into the existing document.</li>
     * </ul>
     *  The available actions are:
     * <ul>
     * <li><i>add</i>: Generate default metadata for the given classes. This
     * is the default action.</li>
     * <li><i>drop</i>: Remove existing metadata for the given classes.</li>
     * </ul>
     */
    public static void main(String[] args)
        throws IOException {
        Options opts = new Options();
        final String[] arguments = opts.setFromCmdLine(args);
        boolean ret = (args.length > 0) &&
            Configurations.runAgainstAllAnchors(opts,
            new Configurations.Runnable() {
            public boolean run(Options opts) throws Exception {
                OpenJPAConfiguration conf = new OpenJPAConfigurationImpl();
                try {
                    return MetaDataTool.run(conf, arguments, opts);
                } finally {
                    conf.close();
                }
            }
        });
        if (!ret) {
            // START - ALLOW PRINT STATEMENTS
            System.err.println(_loc.get("tool-usage"));
            // STOP - ALLOW PRINT STATEMENTS
        }
    }

    /**
     * Run the tool. Returns false if any invalid options were given.
     */
    public static boolean run(OpenJPAConfiguration conf, String[] args,
        Options opts)
        throws IOException {
        Flags flags = new Flags();
        flags.action = opts.removeProperty("action", "a", flags.action);
        String fileName = opts.removeProperty("file", "f", null);
        if ("stdout".equals(fileName)) {
            flags.writer = new PrintWriter(System.out);
            fileName = null;
        } else if ("stderr".equals(fileName)) {
            flags.writer = new PrintWriter(System.err);
            fileName = null;
        }

        Configurations.populateConfiguration(conf, opts);
        ClassLoader loader = conf.getClassResolverInstance().
            getClassLoader(MetaDataTool.class, null);

        if (fileName != null)
            flags.file = Files.getFile(fileName, loader);
        return run(conf, args, flags, null, loader);
    }

    /**
     * Run the tool. Return false if invalid options were given. The given
     * repository may be null.
     */
    public static boolean run(OpenJPAConfiguration conf, String[] args,
        Flags flags, MetaDataRepository repos, ClassLoader loader)
        throws IOException {
        if (args.length == 0)
            return false;
        if (flags.action == null)
            flags.action = ACTION_ADD;

        MetaDataTool tool = new MetaDataTool(conf, flags.action);
        if (repos != null) {
            MetaDataFactory factory = repos.getMetaDataFactory();
            factory.getDefaults().setIgnoreNonPersistent(false);
            factory.setStoreMode(MetaDataFactory.STORE_VERBOSE);
            tool.setRepository(repos);
        }
        if (flags.file != null)
            tool.setFile(flags.file);
        if (flags.writer != null)
            tool.setWriter(flags.writer);

        Log log = conf.getLog(OpenJPAConfiguration.LOG_TOOL);
        ClassArgParser cap = conf.getMetaDataRepositoryInstance().
            getMetaDataFactory().newClassArgParser();
        cap.setClassLoader(loader);
        Class[] classes;
        for (int i = 0; i < args.length; i++) {
            classes = cap.parseTypes(args[i]);
            for (int j = 0; j < classes.length; j++) {
                log.info(_loc.get("tool-running", classes[j], flags.action));
                try {
                    tool.run(classes[j]);
                } catch (IllegalArgumentException iae) {
                    return false;
                }
            }
        }

        log.info(_loc.get("tool-record"));
        tool.record();
        return true;
    }

    /**
     * Run flags.
     */
    public static class Flags {

        public String action = ACTION_ADD;
        public File file = null;
		public Writer writer = null;
	}
}
