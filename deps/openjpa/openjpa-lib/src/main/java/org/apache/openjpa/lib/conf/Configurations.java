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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.Options;
import org.apache.openjpa.lib.util.ParseException;
import org.apache.openjpa.lib.util.StringDistance;
import org.apache.openjpa.lib.util.concurrent.ConcurrentReferenceHashMap;

import serp.util.Strings;

/**
 * Utility methods dealing with configuration.
 *
 * @author Abe White
 * @nojavadoc
 */
public class Configurations {

    private static final Localizer _loc = Localizer.forPackage
        (Configurations.class);
    
    private static final ConcurrentReferenceHashMap _loaders = new
        ConcurrentReferenceHashMap(ConcurrentReferenceHashMap.WEAK, 
                ConcurrentReferenceHashMap.HARD);

    private static final Object NULL_LOADER = "null-loader";

    public static final String CONFIG_RESOURCE_PATH = "configResourcePath";
    public static final String CONFIG_RESOURCE_ANCHOR = "configResourceAnchor";

    /**
     * Return the class name from the given plugin string, or null if none.
     */
    public static String getClassName(String plugin) {
        return getPluginComponent(plugin, true);
    }

    /**
     * Return the properties part of the given plugin string, or null if none.
     */
    public static String getProperties(String plugin) {
        return getPluginComponent(plugin, false);
    }

    /**
     * Return either the class name or properties string from a plugin string.
     */
    private static String getPluginComponent(String plugin, boolean clsName) {
        if (plugin != null)
            plugin = plugin.trim();
        if (StringUtils.isEmpty(plugin))
            return null;

        int openParen = -1;
        if (plugin.charAt(plugin.length() - 1) == ')')
            openParen = plugin.indexOf('(');
        if (openParen == -1) {
            int eq = plugin.indexOf('=');
            if (eq == -1)
                return (clsName) ? plugin : null;
            return (clsName) ? null : plugin;
        }

        // clsName(props) form
        if (clsName)
            return plugin.substring(0, openParen).trim();
        String prop = plugin.substring(openParen + 1,
            plugin.length() - 1).trim();
        return (prop.length() == 0) ? null : prop;
    }

    /**
     * Combine the given class name and properties into a plugin string.
     */
    public static String getPlugin(String clsName, String props) {
        if (StringUtils.isEmpty(clsName))
            return props;
        if (StringUtils.isEmpty(props))
            return clsName;
        return clsName + "(" + props + ")";
    }

    /**
     * Return a plugin string that combines the properties of the given plugin
     * strings, where properties of <code>override</code> will override the
     * same properties of <code>orig</code>.
     */
    public static String combinePlugins(String orig, String override) {
        if (StringUtils.isEmpty(orig))
            return override;
        if (StringUtils.isEmpty(override))
            return orig;

        String origCls = getClassName(orig);
        String overrideCls = getClassName(override);
        String cls;
        if (StringUtils.isEmpty(origCls))
            cls = overrideCls;
        else if (StringUtils.isEmpty(overrideCls))
            cls = origCls;
        else if (!origCls.equals(overrideCls))
            return override; // completely different plugin
        else
            cls = origCls;

        String origProps = getProperties(orig);
        String overrideProps = getProperties(override);
        if (StringUtils.isEmpty(origProps))
            return getPlugin(cls, overrideProps);
        if (StringUtils.isEmpty(overrideProps))
            return getPlugin(cls, origProps);

        Properties props = parseProperties(origProps);
        props.putAll(parseProperties(overrideProps));
        return getPlugin(cls, serializeProperties(props)); 
    }

    /**
     * Create the instance with the given class name, using the given
     * class loader. No configuration of the instance is performed by
     * this method.
     */
    public static Object newInstance(String clsName, ClassLoader loader) {
        return newInstance(clsName, null, null, loader, true);
    }

    /**
     * Create and configure an instance with the given class name and
     * properties as a String.
     */
    public static Object newInstance(String clsName, Configuration conf,
        String props, ClassLoader loader) {
        Object obj = newInstance(clsName, null, conf, loader, true);
        configureInstance(obj, conf, props);
        return obj;
    }

    /**
     * Create and configure an instance with the given class name and
     * properties.
     */
    public static Object newInstance(String clsName, Configuration conf,
        Properties props, ClassLoader loader) {
        Object obj = newInstance(clsName, null, conf, loader, true);
        configureInstance(obj, conf, props);
        return obj;
    }

    /**
     * Loads the given class name by the given loader.
     * For efficiency, a cache per class loader is maintained of classes already loader. 
     * @param clsName
     * @param loader
     * @return
     */
    static Class<?> loadClass(String clsName, ClassLoader loader) {
        Class<?> cls = null; 
        Object key = loader == null ? NULL_LOADER : loader;
        Map<String,Class<?>> loaderCache = (Map<String,Class<?>>) _loaders.get(key);
        if (loaderCache == null) { // We don't have a cache for this loader.
            loaderCache = new ConcurrentHashMap<String,Class<?>>();
            _loaders.put(key, loaderCache);
        } else {  // We have a cache for this loader.
            cls = (Class<?>) loaderCache.get(clsName);
        }
        if (cls == null) {
            try {
                cls = Strings.toClass(clsName, loader);
                loaderCache.put(clsName, cls);
            } catch (RuntimeException re) {
            	
            }
        }
        return cls;
    }
    
    /**
     * Helper method used by members of this package to instantiate plugin
     * values.
     */
    static Object newInstance(String clsName, Value val, Configuration conf,
        ClassLoader loader, boolean fatal) {
        if (StringUtils.isEmpty(clsName))
            return null;

        Class<?> cls = loadClass(clsName, findDerivedLoader(conf, loader));
        if (cls == null) {
        	cls = loadClass(clsName, findDerivedLoader(conf, null));
        }
        if (cls == null && conf.getUserClassLoader() != null) {
        	cls = loadClass(clsName, conf.getUserClassLoader());
        }

        if (cls == null) {
            if (fatal)
              throw getCreateException(clsName, val, new ClassNotFoundException(clsName));
            Log log = (conf == null) ? null : conf.getConfigurationLog();
	        if (log != null && log.isErrorEnabled())
	            log.error(_loc.get("plugin-creation-exception", val));
	        return null;
       }

        try {
            return AccessController.doPrivileged(J2DoPrivHelper.newInstanceAction(cls));
        } catch (Exception e) {
            if (e instanceof PrivilegedActionException) {
                e = ((PrivilegedActionException) e).getException();   
            }
            RuntimeException re = new NestableRuntimeException(_loc.get("obj-create", cls).getMessage(), e);
            if (fatal)
                throw re;
            Log log = (conf == null) ? null : conf.getConfigurationLog();
            if (log != null && log.isErrorEnabled())
                log.error(_loc.get("plugin-creation-exception", val), re);
            return null;
        }
    }

    /**
     * Attempt to find a derived loader that delegates to our target loader.
     * This allows application loaders that delegate appropriately for known
     * classes first crack at class names.
     */
    private static ClassLoader findDerivedLoader(Configuration conf, ClassLoader loader) {
        // we always prefer the thread loader, because it's the only thing we
        // can access that isn't bound to the OpenJPA classloader, unless
        // the conf object is of a custom class
        ClassLoader ctxLoader = AccessController.doPrivileged(J2DoPrivHelper.getContextClassLoaderAction());
        if (loader == null) {
            if (ctxLoader != null) {
                return ctxLoader;
            } else if (conf != null) {
                return classLoaderOf(conf.getClass()); 
            } else {
            	return classLoaderOf(Configurations.class);
            }
        }

        for (ClassLoader parent = ctxLoader; parent != null; parent = parentClassLoaderOf(parent)) {
            if (parent == loader)
                return ctxLoader;
        }
        if (conf != null) {
            for (ClassLoader parent = classLoaderOf(conf.getClass()); parent != null; 
                    parent = parentClassLoaderOf(parent)) {
                if (parent == loader)
                    return classLoaderOf(conf.getClass()); 
            }
        }
        return loader;
    }
    
    static ClassLoader classLoaderOf(Class<?> cls) {
    	return AccessController.doPrivileged(J2DoPrivHelper.getClassLoaderAction(cls)); 
    }
    
    static ClassLoader parentClassLoaderOf(ClassLoader loader) {
    	return AccessController.doPrivileged(J2DoPrivHelper.getParentAction(loader)); 
    }

    /**
     * Return a List<String> of all the fully-qualified anchors specified in the
     * properties location listed in <code>opts</code>. If no properties
     * location is listed in <code>opts</code>, this returns whatever the
     * product derivations can find in their default configurations.
     * If the properties location specified in <code>opts</code> already
     * contains an anchor spec, this returns that anchor. Note that in this
     * fully-qualified-input case, the logic involving product derivations
     * and resource parsing is short-circuited, so this method
     * should not be used as a means to test that a particular anchor is
     * defined in a given location by invoking with a fully-qualified anchor.
     *
     * This does not mutate <code>opts</code>.
     *
     * @since 1.1.0
     */
    public static List<String> getFullyQualifiedAnchorsInPropertiesLocation(
        Options opts) {
        String props = opts.getProperty("properties", "p", null);
        if (props != null) {
            int anchorPosition = props.indexOf("#");
            if (anchorPosition > -1)
                return Arrays.asList(new String[] { props });
        }

        return ProductDerivations.getFullyQualifiedAnchorsInPropertiesLocation(
            props);
    }

    /**
     * Set the given {@link Configuration} instance from the command line
     * options provided. All property names of the given configuration are
     * recognized; additionally, if a <code>properties</code> or
     * <code>p</code> argument exists, the resource it
     * points to will be loaded and set into the given configuration instance.
     * It can point to either a file or a resource name.
     */
    public static void populateConfiguration(Configuration conf, Options opts) {
        String props = opts.removeProperty("properties", "p", null);
        ConfigurationProvider provider;
        if (!StringUtils.isEmpty(props)) {
            Map<String, String> result = parseConfigResource(props);
            String path = result.get(CONFIG_RESOURCE_PATH);
            String anchor = result.get(CONFIG_RESOURCE_ANCHOR);

            File file = new File(path);
            if ((AccessController.doPrivileged(J2DoPrivHelper
                .isFileAction(file))).booleanValue())
                provider = ProductDerivations.load(file, anchor, null);
            else {
                file = new File("META-INF" + File.separatorChar + path);
                if ((AccessController.doPrivileged(J2DoPrivHelper
                    .isFileAction(file))).booleanValue())
                    provider = ProductDerivations.load(file, anchor, null);
                else
                    provider = ProductDerivations.load(path, anchor, null);
            }
            if (provider != null)
                provider.setInto(conf);
            else
                throw new MissingResourceException(_loc.get("no-provider",
                    props).getMessage(), Configurations.class.getName(), 
                    props);
        } else {
            provider = ProductDerivations.loadDefaults(null);
            if (provider != null)
                provider.setInto(conf);
        }
        opts.setInto(conf);
    }

    public static Map<String, String> parseConfigResource(String props) {
        String path = props;
        String anchor = null;
        int idx = path.lastIndexOf('#');
        if (idx != -1) {
            if (idx < path.length() - 1)
                anchor = path.substring(idx + 1);
            path = path.substring(0, idx);
            if (path.length() == 0)
                throw new MissingResourceException(_loc.get("anchor-only",
                    props).getMessage(), Configurations.class.getName(), 
                    props);
        }
        Map <String, String> result = new HashMap<String, String>();
        result.put(CONFIG_RESOURCE_PATH, path);
        result.put(CONFIG_RESOURCE_ANCHOR, anchor);
        return result;
    }

    /**
     * Helper method to throw an informative description on instantiation error.
     */
    private static RuntimeException getCreateException(String clsName, Value val, Exception e) {
        // re-throw the exception with some better information
        final String msg;
        final Object[] params;

        String alias = val.alias(clsName);
        String[] aliases = val.getAliases();
        String[] keys;
        if (aliases.length == 0)
            keys = aliases;
        else {
            keys = new String[aliases.length / 2];
            for (int i = 0; i < aliases.length; i += 2)
                keys[i / 2] = aliases[i];
        }

        String closest;
        if (keys.length == 0) {
            msg = "invalid-plugin";
            params = new Object[]{ val.getProperty(), alias, e.toString(), };
        } else if ((closest = StringDistance.getClosestLevenshteinDistance
            (alias, keys, 0.5f)) == null) {
            msg = "invalid-plugin-aliases";
            params = new Object[]{
                val.getProperty(), alias, e.toString(),
                new TreeSet<String>(Arrays.asList(keys)), };
        } else {
            msg = "invalid-plugin-aliases-hint";
            params = new Object[]{
                val.getProperty(), alias, e.toString(),
                new TreeSet<String>(Arrays.asList(keys)), closest, };
        }
        return new ParseException(_loc.get(msg, params), e);
    }

    /**
     * Configures the given object with the given properties by
     * matching the properties string to the object's setter
     * methods. The properties string should be in the form
     * "prop1=val1, prop2=val2 ...". Does not validate that setter
     * methods exist for the properties.
     *
     * @throws RuntimeException on configuration error
     */
    public static void configureInstance(Object obj, Configuration conf,
        String properties) {
        configureInstance(obj, conf, properties, null);
    }

    /**
     * Configures the given object with the given properties by
     * matching the properties string to the object's setter
     * methods. The properties string should be in the form
     * "prop1=val1, prop2=val2 ...". Validates that setter methods
     * exist for the properties.
     *
     * @throws RuntimeException on configuration error
     */
    public static void configureInstance(Object obj, Configuration conf,
        String properties, String configurationName) {
        if (obj == null)
            return;

        Properties props = null;
        if (!StringUtils.isEmpty(properties))
            props = parseProperties(properties);
        configureInstance(obj, conf, props, configurationName);
    }

    /**
     * Configures the given object with the given properties by
     * matching the properties string to the object's setter
     * methods. Does not validate that setter methods exist for the properties.
     *
     * @throws RuntimeException on configuration error
     */
    public static void configureInstance(Object obj, Configuration conf,
        Properties properties) {
        configureInstance(obj, conf, properties, null);
    }

    /**
     * Configures the given object with the given properties by
     * matching the properties string to the object's setter
     * methods. If <code>configurationName</code> is
     * non-<code>null</code>, validates that setter methods exist for
     * the properties.
     *
     * @throws RuntimeException on configuration error
     */
    public static void configureInstance(Object obj, Configuration conf,
        Properties properties, String configurationName) {
        if (obj == null)
            return;

        Options opts;
        if (properties instanceof Options)
            opts = (Options) properties;
        else { 
            opts = new Options();
            if (properties != null)
                opts.putAll(properties);
        }

        Configurable configurable = null;
        if (conf != null && obj instanceof Configurable)
            configurable = (Configurable) obj;

        if (configurable != null) {
            configurable.setConfiguration(conf);
            configurable.startConfiguration();
        }
        Options invalidEntries = opts.setInto(obj);
        if (obj instanceof GenericConfigurable)
            ((GenericConfigurable) obj).setInto(invalidEntries);

		if (!invalidEntries.isEmpty() && configurationName != null) {
			Localizer.Message msg = null;
            String first = (String) invalidEntries.keySet().iterator().next();
			if (invalidEntries.keySet().size() == 1 &&
				first.indexOf('.') == -1) {
                // if there's just one misspelling and this is not a
				// path traversal, check for near misses.
                Collection<String> options = findOptionsFor(obj.getClass());
                String close = StringDistance.getClosestLevenshteinDistance
					(first, options, 0.75f);
				if (close != null)
                    msg = _loc.get("invalid-config-param-hint", new Object[]{
                            configurationName, obj.getClass(), first, close,
						    options, });
			}

            if (msg == null) {
                msg = _loc.get("invalid-config-params", new String[]{
                    configurationName, obj.getClass().getName(),
                    invalidEntries.keySet().toString(),
                    findOptionsFor(obj.getClass()).toString(), });
            }
            throw new ParseException(msg);
        }
        if (configurable != null)
            configurable.endConfiguration();
    }

    private static Collection<String> findOptionsFor(Class<?> cls) {
        Collection<String> c = Options.findOptionsFor(cls);
        
        // remove Configurable.setConfiguration() and 
        // GenericConfigurable.setInto() from the set, if applicable.
        if (Configurable.class.isAssignableFrom(cls))
            c.remove("Configuration");
        if (GenericConfigurable.class.isAssignableFrom(cls))
            c.remove("Into");
        
        return c;
    }

    /**
     * Turn a set of properties into a comma-separated string.
     */
    public static String serializeProperties(Map map) {
        if (map == null || map.isEmpty())
            return null;

        StringBuilder buf = new StringBuilder();
        Map.Entry entry;
        String val;
        for (Iterator itr = map.entrySet().iterator(); itr.hasNext();) {
            entry = (Map.Entry) itr.next();
            if (buf.length() > 0)
                buf.append(", ");
            buf.append(entry.getKey()).append('=');
            val = String.valueOf(entry.getValue());
            if (val.indexOf(',') != -1)
                buf.append('"').append(val).append('"');
            else
                buf.append(val);
        }
        return buf.toString();
    }

    /**
     * Parse a set of properties from a comma-separated string.
     */
    public static Options parseProperties(String properties) {
        Options opts = new Options();
        properties = StringUtils.trimToNull(properties);
        if (properties == null)
            return opts;

        try {
            String[] props = Strings.split(properties, ",", 0);
            int idx;
            char quote;
            String prop;
            String val;
            for (int i = 0; i < props.length; i++) {
                idx = props[i].indexOf('=');
                if (idx == -1) {
                    // if the key is not assigned to any value, set the
                    // value to the same thing as the key, and continue.
                    // This permits GenericConfigurable instances to
                    // behave meaningfully. We might consider setting the
                    // value to some well-known "value was not set, but
                    // key is present" string so that instances getting
                    // values injected can differentiate between a mentioned
                    // property and one set to a particular value.
                    prop = props[i];
                    val = prop;
                } else {
                    prop = props[i].substring(0, idx).trim();
                    val = props[i].substring(idx + 1).trim();
                }

                // if the value is quoted, read until the end quote
                if (((val.startsWith("\"") && val.endsWith("\""))
                    || (val.startsWith("'") && val.endsWith("'")))
                    && val.length() > 1)
                    val = val.substring(1, val.length() - 1);
                else if (val.startsWith("\"") || val.startsWith("'")) {
                    quote = val.charAt(0);
                    StringBuilder buf = new StringBuilder(val.substring(1));
                    int quotIdx;
                    while (++i < props.length) {
                        buf.append(",");

                        quotIdx = props[i].indexOf(quote);
                        if (quotIdx != -1) {
                            buf.append(props[i].substring(0, quotIdx));
                            if (quotIdx + 1 < props[i].length())
                                buf.append(props[i].substring(quotIdx + 1));
                            break;
                        } else
                            buf.append(props[i]);
                    }
                    val = buf.toString();
                }
                opts.put(prop, val);
            }
            return opts;
        } catch (RuntimeException re) {
            throw new ParseException(_loc.get("prop-parse", properties), re);
        }
    }

    /**
     * Looks up the given name in JNDI. If the name is null, null is returned.
     */
    public static Object lookup(String name, String userKey, Log log) {
        if (StringUtils.isEmpty(name))
            return null;

        Context ctx = null;
        try {
            ctx = new InitialContext();
            Object result = ctx.lookup(name);
            if (result == null && log != null && log.isWarnEnabled())
            	log.warn(_loc.get("jndi-lookup-failed", userKey, name));
            return result;
        } catch (NamingException ne) {
            throw new NestableRuntimeException(
                _loc.get("naming-err", name).getMessage(), ne);
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException ne) {
                    // ignore
                }
            }
        }
    }

    /**
     * Test whether the map contains the given partial key, prefixed with any
     * possible configuration prefix.
     */
    public static boolean containsProperty(Value value, Map props) {
        if (value == null || props == null || props.isEmpty())
            return false;
        List<String> partialKeys = value.getPropertyKeys();
        for (String partialKey : partialKeys) {
            if (props.containsKey(
                ProductDerivations.getConfigurationKey(partialKey, props)))
                return true;
        }
        return false;
    }
    
    /**
     * Test whether the map contains the given partial key, prefixed with any
     * possible configuration prefix.
     */
    public static boolean containsProperty(String partialKey, Map props) {
        if (partialKey == null || props == null || props.isEmpty())
            return false;
        else
            return props.containsKey(
                ProductDerivations.getConfigurationKey(partialKey, props));
    }

    /**
     * Get the property under the given partial key, prefixed with any possible
     * configuration prefix.
     */
    public static Object getProperty(String partialKey, Map m) {
        if (partialKey == null || m == null || m.isEmpty())
            return null;
        else 
            return m.get(ProductDerivations.getConfigurationKey(partialKey, m));
    }

    /**
     * Remove the property under the given partial key, prefixed with any
     * possible configuration prefix.
     */
    public static Object removeProperty(String partialKey, Map props) {
        if (partialKey == null || props == null || props.isEmpty())
            return null;
        if (containsProperty(partialKey, props))
            return props.remove(ProductDerivations.getConfigurationKey(partialKey, props));
        else 
            return null;
    }

    public static void removeProperty(String partialKey, Map<?,?> remaining, Map<?,?> props) {
        if (removeProperty(partialKey, remaining) != null) {
            removeProperty(partialKey, props);
        }
    }

    /**
     * Runs <code>runnable</code> against all the anchors in the configuration
     * pointed to by <code>opts</code>. Each invocation gets a fresh clone of 
     * <code>opts</code> with the <code>properties</code> option set
     * appropriately.
     *
     * @since 1.1.0
     */
    public static boolean runAgainstAllAnchors(Options opts,
        Configurations.Runnable runnable) {
        if (opts.containsKey("help") || opts.containsKey("-help")) {
            return false;
        }
        List<String> anchors =
            Configurations.getFullyQualifiedAnchorsInPropertiesLocation(opts);

        // We use 'properties' below; get rid of 'p' to avoid conflicts. This
        // relies on knowing what getFullyQualifiedAnchorsInPropertiesLocation
        // looks for.
        if (opts.containsKey("p"))
            opts.remove("p");

        boolean ret = true;
        if (anchors.size() == 0) {
            ret = launchRunnable(opts, runnable);
        } else {
            for(String s : anchors ) { 
                Options clonedOptions = (Options) opts.clone();
                clonedOptions.setProperty("properties", s);
                ret &= launchRunnable(clonedOptions, runnable);
            }
        }
        return ret;
    }

    private static boolean launchRunnable(Options opts,
        Configurations.Runnable runnable) {
        boolean ret = true;
        try {
            ret = runnable.run(opts);
        } catch (Exception e) {
            if (!(e instanceof RuntimeException))
                throw new RuntimeException(e);
            else
                throw (RuntimeException) e;
        }
        return ret;
    }

    public interface Runnable {
        public boolean run(Options opts) throws Exception;
    }
}
