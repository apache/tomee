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
package org.apache.openjpa.enhance;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Set;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.Options;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.util.GeneralException;

import serp.bytecode.BCClass;
import serp.bytecode.Project;
import serp.bytecode.lowlevel.ConstantPoolTable;

/**
 * Transformer that makes persistent classes implement the
 * {@link PersistenceCapable} interface at runtime.
 *
 * @author Abe White
 * @nojavadoc
 */
public class PCClassFileTransformer
    implements ClassFileTransformer {

    private static final Localizer _loc = Localizer.forPackage
        (PCClassFileTransformer.class);

    private final MetaDataRepository _repos;
    private final PCEnhancer.Flags _flags;
    private final ClassLoader _tmpLoader;
    private final Log _log;
    private final Set _names;
    private boolean _transforming = false;

    /**
     * Constructor.
     *
     * @param repos metadata repository to use internally
     * @param opts enhancer configuration options
     * @param loader temporary class loader for loading intermediate classes
     */
    public PCClassFileTransformer(MetaDataRepository repos, Options opts,
        ClassLoader loader) {
        this(repos, toFlags(opts), loader, opts.removeBooleanProperty
            ("scanDevPath", "ScanDevPath", false));
    }

    /**
     * Create enhancer flags from the given options.
     */
    private static PCEnhancer.Flags toFlags(Options opts) {
        PCEnhancer.Flags flags = new PCEnhancer.Flags();
        flags.addDefaultConstructor = opts.removeBooleanProperty
            ("addDefaultConstructor", "AddDefaultConstructor",
                flags.addDefaultConstructor);
        flags.enforcePropertyRestrictions = opts.removeBooleanProperty
            ("enforcePropertyRestrictions", "EnforcePropertyRestrictions",
                flags.enforcePropertyRestrictions);
        return flags;
    }

    /**
     * Constructor.
     *
     * @param repos metadata repository to use internally
     * @param flags enhancer configuration
     * @param tmpLoader temporary class loader for loading intermediate classes
     * @param devscan whether to scan the dev classpath for persistent types
     * if none are configured
     */
    public PCClassFileTransformer(MetaDataRepository repos,
        PCEnhancer.Flags flags, ClassLoader tmpLoader, boolean devscan) {
        _repos = repos;
        _tmpLoader = tmpLoader;

        _log = repos.getConfiguration().
            getLog(OpenJPAConfiguration.LOG_ENHANCE);
        _flags = flags;

        _names = repos.getPersistentTypeNames(devscan, tmpLoader);
        if (_names == null && _log.isInfoEnabled())
            _log.info(_loc.get("runtime-enhance-pcclasses"));
    }

    public byte[] transform(ClassLoader loader, String className,
        Class redef, ProtectionDomain domain, byte[] bytes)
        throws IllegalClassFormatException {
        if (loader == _tmpLoader)
            return null;

        // JDK bug -- OPENJPA-1676
        if (className == null) {
            return null;
        }
        // prevent re-entrant calls, which can occur if the enhancing
        // loader is used to also load OpenJPA libraries; this is to prevent 
        // recursive enhancement attempts for internal openjpa libraries
        if (_transforming)
            return null;

        _transforming = true;
        
        return transform0(className, redef, bytes);
    }

    /**
     * We have to split the transform method into two methods to avoid
     * ClassCircularityError when executing method using pure-JIT JVMs
     * such as JRockit.
     */
    private byte[] transform0(String className, Class redef, byte[] bytes)
        throws IllegalClassFormatException {
        
        byte[] returnBytes = null;
        try {
            Boolean enhance = needsEnhance(className, redef, bytes);
            if (enhance != null && _log.isTraceEnabled())
                _log.trace(_loc.get("needs-runtime-enhance", className,
                    enhance));
            if (enhance != Boolean.TRUE)
                return null;

            PCEnhancer enhancer = new PCEnhancer(_repos.getConfiguration(),
                new Project().loadClass(new ByteArrayInputStream(bytes),
                    _tmpLoader), _repos);
            enhancer.setAddDefaultConstructor(_flags.addDefaultConstructor);
            enhancer.setEnforcePropertyRestrictions
                (_flags.enforcePropertyRestrictions);

            if (enhancer.run() == PCEnhancer.ENHANCE_NONE)
                return null;
            BCClass pcb = enhancer.getPCBytecode();
            returnBytes = AsmAdaptor.toByteArray(pcb, pcb.toByteArray());
            return returnBytes;
        } catch (Throwable t) {
            _log.warn(_loc.get("cft-exception-thrown", className), t);
            if (t instanceof RuntimeException)
                throw (RuntimeException) t;
            if (t instanceof IllegalClassFormatException)
                throw (IllegalClassFormatException) t;
            throw new GeneralException(t);
        } finally {
            _transforming = false;
            if (returnBytes != null && _log.isTraceEnabled())
                _log.trace(_loc.get("runtime-enhance-complete", className,
                    bytes.length, returnBytes.length));
        }
    }

    /**
     * Return whether the given class needs enhancement.
     */
    private Boolean needsEnhance(String clsName, Class redef, byte[] bytes) {
        if (redef != null) {
            Class[] intfs = redef.getInterfaces();
            for (int i = 0; i < intfs.length; i++)
                if (PersistenceCapable.class.getName().
                    equals(intfs[i].getName()))
                    return Boolean.valueOf(!isEnhanced(bytes));
            return null;
        }

        if (_names != null) {
            if (_names.contains(clsName.replace('/', '.')))
                return Boolean.valueOf(!isEnhanced(bytes));
            return null;
        }

        if (clsName.startsWith("java/") || clsName.startsWith("javax/"))
            return null;
        if (isEnhanced(bytes))
            return Boolean.FALSE;

        try {
            Class c = Class.forName(clsName.replace('/', '.'), false,
                _tmpLoader);
            if (_repos.getMetaData(c, null, false) != null)
                return Boolean.TRUE;
            return null;
        } catch (ClassNotFoundException cnfe) {
            // cannot load the class: this might mean that it is a proxy
            // or otherwise inaccessible class which can't be an entity
            return Boolean.FALSE;
        } catch (LinkageError cce) {
            // this can happen if we are loading classes that this
            // class depends on; these will never be enhanced anyway
            return Boolean.FALSE;
        } catch (RuntimeException re) {
            throw re;
        } catch (Throwable t) {
            throw new GeneralException(t);
        }
    }

    /**
     * Analyze the bytecode to see if the given class definition implements
     * {@link PersistenceCapable}.
     */
    private static boolean isEnhanced(byte[] b) {
        ConstantPoolTable table = new ConstantPoolTable(b);
        int idx = table.getEndIndex();

        idx += 6; // skip access, cls, super
        int ifaces = table.readUnsignedShort(idx);
        int clsEntry, utfEntry;
        String name;
        for (int i = 0; i < ifaces; i++) {
            idx += 2;
            clsEntry = table.readUnsignedShort(idx);
            utfEntry = table.readUnsignedShort(table.get(clsEntry));
            name = table.readString(table.get(utfEntry));
            if ("org/apache/openjpa/enhance/PersistenceCapable".equals(name))
                return true;
        }
        return false;
    }
}
