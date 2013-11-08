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
package org.apache.openjpa.lib.conf;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.MultiClassLoader;
import org.apache.openjpa.lib.util.Services;

/**
 * Utilities for running product derivations.
 *
 * @author Abe White
 * @author Pinaki Poddar
 * @nojavadoc
 */
public class ProductDerivations {

    private static final Localizer _loc = Localizer.forPackage
        (ProductDerivations.class);

    private static final ProductDerivation[] _derivations;
    private static final String[] _derivationNames;
    private static final Throwable[] _derivationErrors;
    private static String[] _prefixes;
    static {
        MultiClassLoader l = AccessController.doPrivileged(J2DoPrivHelper.newMultiClassLoaderAction());
        l.addClassLoader(0, AccessController
            .doPrivileged(J2DoPrivHelper.getClassLoaderAction(ProductDerivations.class)));
        l.addClassLoader(1, AccessController.doPrivileged(J2DoPrivHelper.getContextClassLoaderAction()));
        _derivationNames = Services.getImplementors(ProductDerivation.class, l);
        _derivationErrors = new Throwable[_derivationNames.length];
        List<ProductDerivation> derivations =
            new ArrayList<ProductDerivation>(_derivationNames.length);
        boolean errors = false; 
        for (int i = 0; i < _derivationNames.length; i++) {
            try {
                ProductDerivation d = (ProductDerivation)
                    AccessController.doPrivileged(
                        J2DoPrivHelper.newInstanceAction(
                            Class.forName(_derivationNames[i], true, l)));
                d.validate();
                derivations.add(d);
            } catch (Throwable t) {
                if (t instanceof PrivilegedActionException)
                    t = ((PrivilegedActionException) t).getException();
                _derivationErrors[i] = t;
                errors = true;
            }
        }

        // must be at least one product derivation to define metadata factories,
        // etc. 
        if (derivations.isEmpty()) {
            throw new MissingResourceException(_loc.get
                ("no-product-derivations", ProductDerivation.class.getName(),
                derivationErrorsToString()).getMessage(), 
                ProductDerivations.class.getName(),"derivations");
        }

        // START - ALLOW PRINT STATEMENTS
        // if some derivations weren't instantiable, warn
        if (errors)
            System.err.println(_loc.get("bad-product-derivations",
                ProductDerivations.class.getName()));
        for (int i = 0; i < _derivationErrors.length; i++) {
            if (_derivationErrors[i] == null)
                continue;
            System.err.println(_derivationNames[i] + ":" +
                    _derivationErrors[i]);
            break;
        }
        // STOP - ALLOW PRINT STATEMENTS

        Collections.sort(derivations, new ProductDerivationComparator());
        _derivations =
            derivations.toArray(new ProductDerivation[derivations.size()]);

        List<String> prefixes = new ArrayList<String>(2);
        prefixes.add("openjpa");
        for (int i = 0; i < _derivations.length; i++) {
            String prefix = _derivations[i].getConfigurationPrefix();
            if (prefix != null && !"openjpa".equals(prefix))
                prefixes.add(prefix);
        }
        _prefixes = prefixes.toArray(new String[prefixes.size()]);
    }

    /**
     * Return all the product derivations registered in the current classloader
     */
    public static ProductDerivation[] getProductDerivations() {
        return _derivations;
    }

    /**
     * Return the recognized prefixes for configuration properties.
     */
    public static String[] getConfigurationPrefixes() {
        return _prefixes;
    }

    /**
     * Set the configuration prefix array. This is package-visible for 
     * testing purposes.
     * 
     * @since 0.9.7
     */
    static void setConfigurationPrefixes(String[] prefixes) {
        _prefixes = prefixes;
    }
    
    /**
     * Determine the full key name for <code>partialKey</code>, given the 
     * registered prefixes and the entries in <code>map</code>. This method
     * computes the appropriate configuration prefix to use by looking 
     * through <code>map</code> for a key starting with any of the known
     * configuration prefixes and ending with <code>partialKey</code> and, if a
     * value is found, using the prefix of that key. Otherwise, it uses
     * the first registered prefix. 
     * 
     * The given <code>partialKey</code> is first tested for containment in the
     * given map without any prefix.
     *  
     * @since 0.9.7
     */
    public static String getConfigurationKey(String partialKey, Map map) {
        String firstKey = (map != null && map.containsKey(partialKey)) 
            ? partialKey : null;
        for (int i = 0; map != null && i < _prefixes.length; i++) {
            String fullKey = _prefixes[i] + "." + partialKey;
            if (map.containsKey(fullKey)) {
                if (firstKey == null) 
                    firstKey = fullKey;
                else {
                    // if we've already found a property with a previous 
                    // prefix, then this is a collision.
                    throw new IllegalStateException(_loc.get(
                        "dup-with-different-prefixes", firstKey, fullKey)
                        .getMessage());
                }
            }
        }
        
        if (firstKey == null)
            return _prefixes[0] + "." + partialKey;
        else
            return firstKey;
    }

    /**
     * Apply {@link ProductDerivation#beforeConfigurationConstruct} callbacks
     * to the the given instance. Exceptions other than fatal
     * {@link BootstrapException} are swallowed.
     */
    public static void beforeConfigurationConstruct(ConfigurationProvider cp) {
        for (int i = 0; i < _derivations.length; i++) {
            try {
                _derivations[i].beforeConfigurationConstruct(cp);
            } catch (BootstrapException be) {
            	if (be.isFatal())
            		throw be;
            } catch (Exception e) {
                // can't log; no configuration yet
                e.printStackTrace();
            }
        }
    }

    /**
     * Apply {@link ProductDerivation#beforeConfigurationLoad} callbacks
     * to the the given instance. Exceptions other than fatal
     * {@link BootstrapException} are swallowed.
     */
    public static void beforeConfigurationLoad(Configuration conf) {
        for (int i = 0; i < _derivations.length; i++) {
            try {
                _derivations[i].beforeConfigurationLoad(conf);
            } catch (BootstrapException be) {
            	if (be.isFatal())
            		throw be;
            } catch (Exception e) {
                // logging not configured yet
                e.printStackTrace();
            }
        }
    }

    /**
     * Apply {@link ProductDerivation#afterSpecificationSet} callbacks
     * to the the given instance. Exceptions other than fatal
     * {@link BootstrapException} are swallowed.
     */
    public static void afterSpecificationSet(Configuration conf) {
        for (int i = 0; i < _derivations.length; i++) {
            try {
                _derivations[i].afterSpecificationSet(conf);
            } catch (BootstrapException be) {
            	if (be.isFatal())
            		throw be;
            } catch (Exception e) {
                // logging not configured yet
                e.printStackTrace();
            }
        }
    }

    /**
     * Called as the first step of a Configuration's close() method. 
     * Exceptions are swallowed.
     * 
     * @since 0.9.7
     */
    public static void beforeClose(Configuration conf) {
        for (int i = 0; i < _derivations.length; i++) {
            try {
                _derivations[i].beforeConfigurationClose(conf);
            } catch (Exception e) {
                conf.getConfigurationLog().warn(_loc.get("before-close-ex"), e);
            }
        }
    }

    /**
     * Load the given given resource, or return false if it is not a resource
     * this provider understands. The given class loader may be null.
     *
     * @param anchor optional named anchor within a multiple-configuration
     * resource
     */
    public static ConfigurationProvider load(String resource, String anchor, 
        ClassLoader loader) {
        if (StringUtils.isEmpty(resource))
            return null;
        if (loader == null)
            loader = AccessController.doPrivileged(
                J2DoPrivHelper.getContextClassLoaderAction());
        ConfigurationProvider provider = null;
        StringBuilder errs = null;
        // most specific to least
        Throwable err = null;
        for (int i = _derivations.length - 1; i >= 0; i--) {
            try {
                provider = _derivations[i].load(resource, anchor, loader);
                if (provider != null)
                    return provider;
            } catch (Throwable t) {
                err = t;
                errs = (errs == null) ? new StringBuilder() : errs.append("\n");
                errs.append(_derivations[i].getClass().getName() + ":" + t);
            }
        }
        reportErrors(errs, resource, err);
        String rsrc = resource + "#" + anchor;
        MissingResourceException ex = new MissingResourceException(rsrc,
                ProductDerivations.class.getName(), rsrc);
        ex.initCause(err);
        throw ex;
    }

    /**
     * Load given file, or return false if it is not a file this provider
     * understands.
     *
     * @param anchor optional named anchor within a multiple-configuration file
     */
    public static ConfigurationProvider load(File file, String anchor, 
        ClassLoader loader) {
        if (file == null)
            return null;
        if (loader == null)
            loader = AccessController.doPrivileged(
                J2DoPrivHelper.getContextClassLoaderAction());
        ConfigurationProvider provider = null;
        StringBuilder errs = null;
        Throwable err = null;
        // most specific to least
        for (int i = _derivations.length - 1; i >= 0; i--) {
            try {
                provider = _derivations[i].load(file, anchor);
                if (provider != null)
                    return provider;
            } catch (Throwable t) {
                err = t;
                errs = (errs == null) ? new StringBuilder() : errs.append("\n");
                errs.append(_derivations[i].getClass().getName() + ":" + t);
            }
        }
        String aPath = AccessController.doPrivileged(
            J2DoPrivHelper.getAbsolutePathAction(file));
        reportErrors(errs, aPath, err);
        String rsrc = aPath + "#" + anchor;
        MissingResourceException ex = new MissingResourceException(rsrc,
                ProductDerivations.class.getName(), rsrc);
        ex.initCause(err);
        throw ex;
    }
   
    /**
     * Return a {@link ConfigurationProvider} that has parsed system defaults.
     */
    public static ConfigurationProvider loadDefaults(ClassLoader loader) {
        return load(loader, false);
    }

    /**
     * Return a {@link ConfigurationProvider} that has parsed system globals.
     */
    public static ConfigurationProvider loadGlobals(ClassLoader loader) {
        return load(loader, true);
    }
            
    /**
     * Load a built-in resource location.
     */
    private static ConfigurationProvider load(ClassLoader loader, 
       boolean globals) {
        if (loader == null)
            loader = AccessController.doPrivileged(
                J2DoPrivHelper.getContextClassLoaderAction());
        
        ConfigurationProvider provider = null;
        StringBuilder errs = null;
        String type = (globals) ? "globals" : "defaults";
        Throwable err = null;
        // most specific to least
        for (int i = _derivations.length - 1; i >= 0; i--) {
            try {
                provider = (globals) ? _derivations[i].loadGlobals(loader) 
                    : _derivations[i].loadDefaults(loader);
                if (provider != null)
                   return provider;
            } catch (Throwable t) {
                err = t;
                errs = (errs == null) ? new StringBuilder() : errs.append("\n");
                errs.append(_derivations[i].getClass().getName() + ":" + t);
            }
        }
        reportErrors(errs, type, err);
        return null;
    }
 
    /**
     * Thrown proper exception for given errors.
     */
    private static void reportErrors(StringBuilder errs, String resource,
        Throwable nested) {
        if (errs == null)
            return;
        MissingResourceException ex = new MissingResourceException(errs.toString(),
                ProductDerivations.class.getName(), resource);
        ex.initCause(nested);
        throw ex;
    }

    /**
     * Return a List<String> of all the fully-qualified anchors specified in
     * <code>propertiesLocation</code>. The return values must be used in
     * conjunction with <code>propertiesLocation</code>. If there are no
     * product derivations or if no product derivations could find anchors,
     * this returns an empty list.
     *
     * @since 1.1.0
     */
    public static List<String> getFullyQualifiedAnchorsInPropertiesLocation(
        final String propertiesLocation) {
        List<String> fqAnchors = new ArrayList<String>();
        StringBuilder errs = null;
        Throwable err = null;
        for (int i = _derivations.length - 1; i >= 0; i--) {
            try {
                if (propertiesLocation == null) {
                    String loc = _derivations[i].getDefaultResourceLocation();
                    addAll(fqAnchors, loc,
                        _derivations[i].getAnchorsInResource(loc));
                    continue;
                }

                File f = new File(propertiesLocation);
                if (((Boolean) J2DoPrivHelper.isFileAction(f).run())
                    .booleanValue()) {
                    addAll(fqAnchors, propertiesLocation,
                        _derivations[i].getAnchorsInFile(f));
                } else {
                    f = new File("META-INF" + File.separatorChar
                        + propertiesLocation);
                    if (((Boolean) J2DoPrivHelper.isFileAction(f).run())
                        .booleanValue()) {
                        addAll(fqAnchors, propertiesLocation,
                            _derivations[i].getAnchorsInFile(f));
                    } else {
                        addAll(fqAnchors, propertiesLocation,
                            _derivations[i].getAnchorsInResource(
                                propertiesLocation));
                    }
                }
            } catch (Throwable t) {
                err = t;
                errs = (errs == null) ? new StringBuilder() : errs.append("\n");
                errs.append(_derivations[i].getClass().getName() + ":" + t);
            }
        }
        reportErrors(errs, propertiesLocation, err);
        return fqAnchors;
    }

    private static void addAll(Collection collection, String base,
        Collection newMembers) {
        if (newMembers == null || collection == null)
            return;
        for (Iterator iter = newMembers.iterator(); iter.hasNext(); ) {
            String fqLoc = base + "#" + iter.next();
            if (!collection.contains(fqLoc))
                collection.add(fqLoc);
        }
    }
    
    
    public static Set<String> getSupportedQueryHints() {
        Set<String> result = new TreeSet<String>();
        // most specific to least
        for (int i = _derivations.length - 1; i >= 0; i--) {
            Set<String> members = _derivations[i].getSupportedQueryHints();
            if (members != null && !members.isEmpty())
                result.addAll(members);
        }
        return result;
    }


    /**
     * Compare {@link ProductDerivation}s.
     */
    private static class ProductDerivationComparator
        implements Comparator<ProductDerivation> {

        public int compare(ProductDerivation o1, ProductDerivation o2) {
            int type1 = o1.getType();
            int type2 = o2.getType();
            if (type1 != type2)
                return type1 - type2;

            // arbitrary but consistent order
            return o1.getClass().getName().compareTo(o2.getClass().
                getName());
		}
	}

    /**
     * Prints product derivation information.
     */
    public static void main(String[] args) {
        // START - ALLOW PRINT STATEMENTS
        System.err.println(derivationErrorsToString());
        // STOP - ALLOW PRINT STATEMENTS
    }

    /**
     * Return a message about the status of each product derivation.
     */
    private static String derivationErrorsToString() {
        StringBuilder buf = new StringBuilder();
        buf.append("ProductDerivations: ").append(_derivationNames.length);
        for (int i = 0; i < _derivationNames.length; i++) {
            buf.append("\n").append(i + 1).append(". ").
                append(_derivationNames[i]).append(": ");
            if (_derivationErrors[i] == null)
                buf.append("OK");
            else
                buf.append(_derivationErrors[i].toString());
        }
        return buf.toString();
    }
}

