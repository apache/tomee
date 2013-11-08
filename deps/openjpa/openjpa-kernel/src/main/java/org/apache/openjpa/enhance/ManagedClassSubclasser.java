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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.BytecodeWriter;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.Files;
import org.apache.openjpa.lib.util.Localizer.Message;
import org.apache.openjpa.meta.AccessCode;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.util.Exceptions;
import org.apache.openjpa.util.GeneratedClasses;
import org.apache.openjpa.util.ImplHelper;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.MetaDataException;
import org.apache.openjpa.util.UserException;
import serp.bytecode.BCClass;

/**
 * Redefines the method bodies of existing unenhanced classes to make them
 * notify state managers of mutations.
 *
 * @since 1.0.0
 */
public class ManagedClassSubclasser {
    private static final Localizer _loc = Localizer.forPackage(
        ManagedClassSubclasser.class);

    /**
     * For each element in <code>classes</code>, creates and registers a
     * new subclass that implements {@link PersistenceCapable}, and prepares
     * OpenJPA to handle new instances of the unenhanced type. If this is
     * invoked in a Java 6 environment, this method will redefine the methods
     * for each class in the argument list such that field accesses are
     * intercepted in-line. If invoked in a Java 5 environment, this
     * redefinition is not possible; in these contexts, when using field
     * access, OpenJPA will need to do state comparisons to detect any change
     * to any instance at any time, and when using property access, OpenJPA
     * will need to do state comparisons to detect changes to newly inserted
     * instances after a flush has been called.
     *
     * @return the new subclasses, or <code>null</code> if <code>classes</code>
     * is <code>null</code>.
     * @throws UserException if <code>conf</code> requires build-time
     * enhancement and <code>classes</code> includes unenhanced types.
     *
     * @since 1.0.0
     */
    public static List<Class<?>> prepareUnenhancedClasses(
        final OpenJPAConfiguration conf,
        final Collection<? extends Class<?>> classes,
        final ClassLoader envLoader) {
        if (classes == null)
            return null;
        if (classes.size() == 0)
            return Collections.emptyList();

        Log log = conf.getLog(OpenJPAConfiguration.LOG_ENHANCE);
        if (conf.getRuntimeUnenhancedClassesConstant() != RuntimeUnenhancedClassesModes.SUPPORTED) {
            Collection<Class<?>> unenhanced = new ArrayList<Class<?>>();
            for (Class<?> cls : classes)
                if (!PersistenceCapable.class.isAssignableFrom(cls))
                    unenhanced.add(cls);
            if (unenhanced.size() > 0) {
                if (PCEnhancerAgent.getLoadSuccessful() == true) {
                    // This means that the enhancer has been ran but we
                    // have some unenhanced classes. This can happen if an
                    // entity is loaded by the JVM before the EntityManger
                    // was created. Warn the user.
                    if (log.isWarnEnabled()) {
                        log.warn(_loc.get("entities-loaded-before-em"));
                    }
                    if (log.isTraceEnabled()) {
                        log.trace(ManagedClassSubclasser.class.getName()
                            + ".prepareUnenhancedClasses()"
                            + " - The following classes are unenhanced "
                            + unenhanced.toString());
                    }
                }
                Message msg = _loc.get("runtime-optimization-disabled", Exceptions.toClassNames(unenhanced));
                if (conf.getRuntimeUnenhancedClassesConstant() == RuntimeUnenhancedClassesModes.WARN) {
                    log.warn(msg);
                } else {
                    throw new UserException(msg);
                }
            }
            return null;
        }

        boolean redefine = ClassRedefiner.canRedefineClasses(log);
        if (redefine) {
            log.info(_loc.get("enhance-and-subclass-and-redef-start", classes));
        } else {
            log.warn(_loc.get("enhance-and-subclass-no-redef-start",  classes));
        }
        final Map<Class<?>, byte[]> map = new HashMap<Class<?>, byte[]>();
        final List<Class<?>> subs = new ArrayList<Class<?>>(classes.size());
        final List<Class<?>> ints = new ArrayList<Class<?>>(classes.size());
        Set<Class<?>> unspecified = null;
        for (Class<?> cls : classes) {
            final Class<?> c = cls;
            final PCEnhancer enhancer = new PCEnhancer(conf, cls); 

            enhancer.setBytecodeWriter(new BytecodeWriter() {
                public void write(BCClass bc) throws IOException {
                    ManagedClassSubclasser.write(bc, enhancer, map, c, subs, ints);
                }
            });
            if (redefine) {
                enhancer.setRedefine(true);
            }
            enhancer.setCreateSubclass(true);
            enhancer.setAddDefaultConstructor(true);

            // set this before enhancement as well as after since enhancement
            // uses a different metadata repository, and the metadata config
            // matters in the enhancement contract. In order to avoid a 
            // NullPointerException, check for no metadata and throw an
            // exception if none exists. Otherwise, don't do any warning here,
            // since we'll issue warnings when we do the final metadata
            // reconfiguration at the end of this method.
            ClassMetaData meta = enhancer.getMetaData();
            if (meta == null) {
                throw new MetaDataException(_loc.get("no-meta", cls)).setFatal(true);
            }
            configureMetaData(meta, conf, redefine, false);

            unspecified = collectRelatedUnspecifiedTypes(enhancer.getMetaData(), classes, unspecified);

            int runResult = enhancer.run();
            if (runResult == PCEnhancer.ENHANCE_PC) {
                try {
                    enhancer.record();
                } catch (IOException e) {
                    // our impl of BytecodeWriter doesn't throw IOException
                    throw new InternalException(e);
                }
            }
        }

        if (unspecified != null && !unspecified.isEmpty())
            throw new UserException(_loc.get("unspecified-unenhanced-types", Exceptions.toClassNames(classes), 
                    unspecified));

        ClassRedefiner.redefineClasses(conf, map);
        for (Class<?> cls : map.keySet()) {
            setIntercepting(conf, envLoader, cls);
            configureMetaData(conf, envLoader, cls, redefine);
        }
        for (Class<?> cls : subs)
            configureMetaData(conf, envLoader, cls, redefine);
        for (Class<?> cls : ints)
            setIntercepting(conf, envLoader, cls);

        return subs;
    }

    private static Set<Class<?>> collectRelatedUnspecifiedTypes(ClassMetaData meta,
        Collection<? extends Class<?>> classes, Set<Class<?>> unspecified) {
        unspecified = collectUnspecifiedType(meta.getPCSuperclass(), classes,
            unspecified);

        for (FieldMetaData fmd : meta.getFields()) {
            if (fmd.isTransient())
                continue;
            if (fmd.isTypePC())
                unspecified = collectUnspecifiedType(fmd.getType(), classes,
                    unspecified);
            if (fmd.getElement() != null && fmd.getElement().isTypePC())
                unspecified = collectUnspecifiedType(fmd.getElement().getType(),
                    classes, unspecified);
            if (fmd.getKey() != null && fmd.getKey().isTypePC())
                unspecified = collectUnspecifiedType(fmd.getKey().getType(),
                    classes, unspecified);
            if (fmd.getValue() != null && fmd.getValue().isTypePC())
                unspecified = collectUnspecifiedType(fmd.getValue().getType(),
                    classes, unspecified);
        }
        return unspecified;
    }

    private static Set<Class<?>> collectUnspecifiedType(Class<?> cls,
        Collection<? extends Class<?>> classes, Set<Class<?>> unspecified) {
        if (cls != null && !classes.contains(cls)
            && !ImplHelper.isManagedType(null, cls)
            && !cls.isInterface()) {
            if (unspecified == null)
                unspecified = new HashSet<Class<?>>();
            unspecified.add(cls);
        }
        return unspecified;
    }

    private static void configureMetaData(OpenJPAConfiguration conf,
        ClassLoader envLoader, Class<?> cls, boolean redefineAvailable) {
        ClassMetaData meta = conf.getMetaDataRepositoryInstance()
            .getMetaData(cls, envLoader, true);
        configureMetaData(meta, conf, redefineAvailable, true);
    }

    private static void configureMetaData(ClassMetaData meta,
        OpenJPAConfiguration conf, boolean redefineAvailable, boolean warn) {

        setDetachedState(meta);

        // If warn & (implicit field access | mixed access) & noredef
        if (warn && ((AccessCode.isField(meta.getAccessType())
            && !meta.isMixedAccess()) ||  meta.isMixedAccess())
            && !redefineAvailable) {
            // only warn about declared fields; superclass fields will be
            // warned about when the superclass is handled
            for (FieldMetaData fmd : meta.getDeclaredFields()) {
                if (AccessCode.isProperty(fmd.getAccessType()))
                    continue;
                switch (fmd.getTypeCode()) {
                    case JavaTypes.COLLECTION:
                    case JavaTypes.MAP:
                        // we can lazily load these, since we own the
                        // relationship container
                        break;
                    default:
                        if (!fmd.isInDefaultFetchGroup()
                            && !(fmd.isVersion() || fmd.isPrimaryKey())) {
                            Log log = conf.getLog(
                                OpenJPAConfiguration.LOG_ENHANCE);
                            log.warn(_loc.get("subclasser-fetch-group-override",
                                meta.getDescribedType().getName(),
                                fmd.getName()));
                            fmd.setInDefaultFetchGroup(true);
                        }
                }
            }
        }
    }

    private static void write(BCClass bc, PCEnhancer enhancer,
        Map<Class<?>, byte[]> map, Class<?> cls, List<Class<?>> subs, List<Class<?>> ints)
        throws IOException {

        if (bc == enhancer.getManagedTypeBytecode()) {
            // if it was already defined, don't put it in the map,
            // but do set the metadata accordingly.
            if (enhancer.isAlreadyRedefined())
                ints.add(bc.getType());
            else {
                map.put(bc.getType(), bc.toByteArray());
                debugBytecodes(bc);
            }
        } else {
            if (!enhancer.isAlreadySubclassed()) {
                debugBytecodes(bc);
                
                // this is the new subclass
                ClassLoader loader = GeneratedClasses.getMostDerivedLoader(
                    cls, PersistenceCapable.class);
                subs.add(GeneratedClasses.loadBCClass(bc, loader));
            }
        }
    }

    public static void debugBytecodes(BCClass bc) throws IOException {
        // Write the bytecodes to disk for debugging purposes.
        if ("true".equals(System.getProperty(
            ManagedClassSubclasser.class.getName() + ".dumpBytecodes")))
        {
            File tmp = new File(System.getProperty("java.io.tmpdir"));
            File dir = new File(tmp, "openjpa");
            dir = new File(dir, "pcsubclasses");
            dir.mkdirs();
            dir = Files.getPackageFile(dir, bc.getPackageName(), true);
            File f = new File(dir, bc.getClassName() + ".class");
            // START - ALLOW PRINT STATEMENTS
            System.err.println("Writing to " + f);
            // STOP - ALLOW PRINT STATEMENTS
            AsmAdaptor.write(bc, f);
        }
    }

    private static void setIntercepting(OpenJPAConfiguration conf,
        ClassLoader envLoader, Class<?> cls) {
        ClassMetaData meta = conf.getMetaDataRepositoryInstance()
            .getMetaData(cls, envLoader, true);
        meta.setIntercepting(true);
    }

    /**
     * If the metadata is configured to use a synthetic
     * detached state, reset it to not use a detached
     * state field, since we can't add fields when redefining.
     */
    private static void setDetachedState(ClassMetaData meta) {
        if (ClassMetaData.SYNTHETIC.equals(meta.getDetachedState()))
            meta.setDetachedState(null);
    }
}
