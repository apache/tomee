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

import java.awt.*;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyDescriptor;
import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.log.LogFactory;
import org.apache.openjpa.lib.log.LogFactoryImpl;
import org.apache.openjpa.lib.log.NoneLogFactory;
import org.apache.openjpa.lib.util.Closeable;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.MultiClassLoader;
import org.apache.openjpa.lib.util.ParseException;
import org.apache.openjpa.lib.util.Services;
import org.apache.openjpa.lib.util.StringDistance;
import serp.util.Strings;

/**
 * Default implementation of the {@link Configuration} interface.
 * Subclasses can choose to obtain configuration
 * information from JNDI, Properties, a Bean-builder, etc. This class
 * provides base configuration functionality, including serialization,
 * the <code>equals</code> and <code>hashCode</code> contracts, and default
 * property loading.
 * Property descriptors for {@link Value} instances are constructed from
 * the {@link Localizer} for the package of the configuration class. The
 * following localized strings will be used for describing a value, where
 * <em>name</em> is the last token of the value's property string:
 * <ul>
 * <li><em>name</em>-name: The name that will be displayed for the
 * option in a user interface; required.</li>
 * <li><em>name</em>-desc: A brief description of the option; required.</li>
 * <li><em>name</em>-type: The type or category name for this option;
 * required.</li>
 * <li><em>name</em>-expert: True if this is an expert option, false
 * otherwise; defaults to false.</li>
 * <li><em>name</em>-values: Set of expected or common values, excluding
 * alias keys; optional.</li>
 * <li><em>name</em>-interface: The class name of an interface whose
 * discoverable implementations should be included in the set of expected
 * or common values; optional.</li>
 * <li><em>name</em>-cat: The hierarchical category for the property
 * name, separated by ".".
 * <li><em>name</em>-displayorder: The order in which the property should
 * be displayer.</li>
 * </ul>
 *
 * @author Abe White
 */
public class ConfigurationImpl
    implements Configuration, Externalizable, ValueListener {

    private static final String SEP = J2DoPrivHelper.getLineSeparator();

    private static final Localizer _loc = Localizer.forPackage(ConfigurationImpl.class);

    public ObjectValue logFactoryPlugin;
    public StringValue id;

    private String _product = null;
    private int _readOnlyState = INIT_STATE_LIQUID;
    private Map _props = null;
    private boolean _globals = false;
    private String _auto = null;
    private final List<Value> _vals = new ArrayList<Value>();
    private Set<String> _supportedKeys = new TreeSet<String>();
    
    // property listener helper
    private PropertyChangeSupport _changeSupport = null;

    // cache descriptors
    private PropertyDescriptor[] _pds = null;
    private MethodDescriptor[] _mds = null;
    
    // An additional (and optional) classloader to load custom plugins.
    private ClassLoader _userCL;

    //Ant task needs to defer the resource loading 
    //until the classpath setting is loaded properly
    private boolean _deferResourceLoading = false; 


    /**
     * Default constructor. Attempts to load default properties through
     * system's configured {@link ProductDerivation}s.
     */
    public ConfigurationImpl() {
        this(true);
    }

    /**
     * Constructor.
     *
     * @param loadGlobals whether to attempt to load the global properties
     */
    public ConfigurationImpl(boolean loadGlobals) {
        setProductName("openjpa");

        logFactoryPlugin = addPlugin("Log", true);
        String[] aliases = new String[]{
            "true", LogFactoryImpl.class.getName(),
            "openjpa", LogFactoryImpl.class.getName(),
            "commons", "org.apache.openjpa.lib.log.CommonsLogFactory",
            "log4j", "org.apache.openjpa.lib.log.Log4JLogFactory",
            "slf4j", "org.apache.openjpa.lib.log.SLF4JLogFactory",
            "none", NoneLogFactory.class.getName(),
            "false", NoneLogFactory.class.getName(),
        };
        logFactoryPlugin.setAliases(aliases);
        logFactoryPlugin.setDefault(aliases[0]);
        logFactoryPlugin.setString(aliases[0]);
        logFactoryPlugin.setInstantiatingGetter("getLogFactory");

        id = addString("Id");
        
        if (loadGlobals)
            loadGlobals();
    }

    /**
     * Automatically load global values from the system's
     * {@link ProductDerivation}s, and from System properties.
     */
    public boolean loadGlobals() {
        MultiClassLoader loader = AccessController
            .doPrivileged(J2DoPrivHelper.newMultiClassLoaderAction()); 
        loader.addClassLoader(AccessController.doPrivileged(
            J2DoPrivHelper.getContextClassLoaderAction()));
        loader.addClassLoader(getClass().getClassLoader());
        ConfigurationProvider provider = ProductDerivations.loadGlobals(loader);
        if (provider != null)
            provider.setInto(this);

        // let system properties override other globals
        try {
            fromProperties(new HashMap(
                AccessController.doPrivileged(
                    J2DoPrivHelper.getPropertiesAction())));
        } catch (SecurityException se) {
            // security manager might disallow
        }

        _globals = true;
        if (provider == null) {
            Log log = getConfigurationLog();
            if (log.isTraceEnabled())
                log.trace(_loc.get("no-default-providers"));
            return false;
        }
        return true;
    }

    public String getProductName() {
        return _product;
    }

    public void setProductName(String name) {
        _product = name;
    }

    public LogFactory getLogFactory() {
        if (logFactoryPlugin.get() == null)
            logFactoryPlugin.instantiate(LogFactory.class, this);
        return (LogFactory) logFactoryPlugin.get();
    }

    public void setLogFactory(LogFactory logFactory) {
        logFactoryPlugin.set(logFactory);
    }

    public String getLog() {
        return logFactoryPlugin.getString();
    }

    public void setLog(String log) {
        logFactoryPlugin.setString(log);
    }

    public Log getLog(String category) {
        return getLogFactory().getLog(category);
    }

    public String getId() {
        return id.get();
    }
    
    public void setId(String id) {
        this.id.set(id);
    }

    /**
     * Returns the logging channel <code>openjpa.Runtime</code> by default.
     */
    public Log getConfigurationLog() {
        return getLog("openjpa.Runtime");
    }

    public Value[] getValues() {
        return (Value[]) _vals.toArray(new Value[_vals.size()]);
    }

    /**
     * Gets the registered Value for the given propertyName.
     * 
     * @param propertyName can be either fully-qualified name or the simple name
     * with which the value has been registered. A value may have multiple
     * equivalent names and this method searches with all equivalent names.
     */
    public Value getValue(String property) {
        if (property == null)
            return null;

        // search backwards so that custom values added after construction
        // are found quickly, since this will be the std way of accessing them
        for (int i = _vals.size()-1; i >= 0; i--) { 
            if (_vals.get(i).matches(property))
                return _vals.get(i);
        }
        return null;
    }

    public void setReadOnly(int newState) {
        if (newState >= _readOnlyState) {
        	_readOnlyState = newState;
        }
    }

    public boolean isDeferResourceLoading() {
        return _deferResourceLoading;
    }
 
    public void setDeferResourceLoading(boolean deferResourceLoading) {
        this._deferResourceLoading = deferResourceLoading;
    }

    public void instantiateAll() {
        StringWriter errs = null;
        PrintWriter stack = null;
        String getterName;
        Method getter;
        Object getterTarget;
        for(Value val : _vals) { 
            getterName = val.getInstantiatingGetter();
            if (getterName == null)
                continue;

            getterTarget = this;
            if (getterName.startsWith("this.")) {
                getterName = getterName.substring("this.".length());
                getterTarget = val;
            }

            try {
                getter = getterTarget.getClass().getMethod(getterName,
                    (Class[]) null);
                getter.invoke(getterTarget, (Object[]) null);
            } catch (Throwable t) {
                if (t instanceof InvocationTargetException)
                    t = ((InvocationTargetException) t).getTargetException();
                if (errs == null) {
                    errs = new StringWriter();
                    stack = new PrintWriter(errs);
                } else
                    errs.write(SEP);
                t.printStackTrace(stack);
                stack.flush();
            }
        }
        if (errs != null)
            throw new RuntimeException(_loc.get("get-prop-errs",
                errs.toString()).getMessage());
    }

    public boolean isReadOnly() {
        return _readOnlyState==INIT_STATE_FROZEN;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (_changeSupport == null)
            _changeSupport = new PropertyChangeSupport(this);
        _changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (_changeSupport != null)
            _changeSupport.removePropertyChangeListener(listener);
    }

    public void valueChanged(Value val) {
        if (_changeSupport == null && _props == null)
            return;

        String newString = val.getString();
        if (_changeSupport != null)
            _changeSupport.firePropertyChange(val.getProperty(), null, newString);

        // keep cached props up to date
        if (_props != null) {
            if (newString == null)
                Configurations.removeProperty(val.getProperty(), _props);
            else if (Configurations.containsProperty(val, _props)
                || val.getDefault() == null
                || !val.getDefault().equals(newString))
                setValue(_props, val);
        }
    }

    /**
     * Closes all closeable values and plugins.
     */
    public final void close() {
        ProductDerivations.beforeClose(this);
        
        preClose();
        
        ObjectValue val;
        for(Value v : _vals) { 
            if (v instanceof Closeable) {
                try { ((Closeable)v).close(); }
                catch (Exception e) {} 
                continue;
            }

            if (!(v  instanceof ObjectValue))
                continue;

            val = (ObjectValue) v;
            if (val.get() instanceof Closeable) {
                try {
                    ((Closeable) val.get()).close();
                } catch (Exception e) {
                }
            }
        }
    }
    
    /**
     * Invoked by final method {@link #close} after invoking the 
     * {@link ProductDerivation#beforeConfigurationClose} callbacks
     * but before performing internal close operations.
     * 
     * @since 0.9.7
     */
    protected void preClose() {
    }

    ///////////////////////////
    // BeanInfo implementation
    ///////////////////////////

    public BeanInfo[] getAdditionalBeanInfo() {
        return new BeanInfo[0];
    }

    public BeanDescriptor getBeanDescriptor() {
        return new BeanDescriptor(getClass());
    }

    public int getDefaultEventIndex() {
        return 0;
    }

    public int getDefaultPropertyIndex() {
        return 0;
    }

    public EventSetDescriptor[] getEventSetDescriptors() {
        return new EventSetDescriptor[0];
    }

    public Image getIcon(int kind) {
        return null;
    }

    public synchronized MethodDescriptor[] getMethodDescriptors() {
        if (_mds != null)
            return _mds;

        PropertyDescriptor[] pds = getPropertyDescriptors();
        List<MethodDescriptor> descs = new ArrayList<MethodDescriptor>(); 
        for (int i = 0; i < pds.length; i++) {
            Method write = pds[i].getWriteMethod();
            Method read = pds[i].getReadMethod();
            if (read != null && write != null) {
                descs.add(new MethodDescriptor(write));
                descs.add(new MethodDescriptor(read));
            }
        }
        _mds = (MethodDescriptor[]) descs.
            toArray(new MethodDescriptor[descs.size()]);
        return _mds;
    }

    public synchronized PropertyDescriptor[] getPropertyDescriptors() {
        if (_pds != null)
            return _pds;

        _pds = new PropertyDescriptor[_vals.size()];
        
        List<String> failures = null;
        Value val;
        for (int i = 0; i < _vals.size(); i++) {
            val = (Value) _vals.get(i);
            try {
                _pds[i] = getPropertyDescriptor(val);
            } catch (MissingResourceException mre) {
                if (failures == null)
                    failures = new ArrayList<String>();
                failures.add(val.getProperty());
            } catch (IntrospectionException ie) {
                if (failures == null)
                    failures = new ArrayList<String>();
                failures.add(val.getProperty());
            }
        }
        if (failures != null)
            throw new ParseException(_loc.get("invalid-property-descriptors",
                failures));

        return _pds;
    }

    /**
     * Create a property descriptor for the given value.
     */
    private PropertyDescriptor getPropertyDescriptor(Value val)
        throws IntrospectionException {
        String prop = val.getProperty();
        prop = prop.substring(prop.lastIndexOf('.') + 1);

        // set up property descriptor
        PropertyDescriptor pd;
        try {
            pd = new PropertyDescriptor(Introspector.decapitalize(prop), 
                getClass());
        } catch (IntrospectionException ie) {
            // if there aren't any methods for this value(i.e., if it's a
            // dynamically-added value), then an IntrospectionException will
            // be thrown. Try to create a PD with no read or write methods.
            pd = new PropertyDescriptor(Introspector.decapitalize(prop),
                (Method) null, (Method) null);
        }
        pd.setDisplayName(findLocalized(prop + "-name", true, val.getScope()));
        pd.setShortDescription(findLocalized(prop + "-desc", true,
            val.getScope()));
        pd.setExpert("true".equals(findLocalized(prop + "-expert", false,
            val.getScope())));

        try {
            pd.setReadMethod(getClass().getMethod("get"
                + StringUtils.capitalize(prop), (Class[]) null));
            pd.setWriteMethod(getClass().getMethod("set"
                + StringUtils.capitalize(prop), new Class[]
                { pd.getReadMethod().getReturnType() }));
        } catch (Throwable t) {
            // if an error occurs, it might be because the value is a
            // dynamic property.
        }

        String type = findLocalized(prop + "-type", true, val.getScope());
        if (type != null)
            pd.setValue(ATTRIBUTE_TYPE, type);

        String cat = findLocalized(prop + "-cat", false, val.getScope());
        if (cat != null)
            pd.setValue(ATTRIBUTE_CATEGORY, cat);
        
        pd.setValue(ATTRIBUTE_XML, toXMLName(prop));

        String order = findLocalized(prop + "-displayorder", false,
            val.getScope());
        if (order != null)
            pd.setValue(ATTRIBUTE_ORDER, order);

        // collect allowed values from alias keys, listed values, and
        // interface implementors
        Collection<String> allowed = new TreeSet<String>();
        List<String> aliases = Collections.emptyList();
        if (val.getAliases() != null) {
            aliases = Arrays.asList(val.getAliases());
            for (int i = 0; i < aliases.size(); i += 2)
                allowed.add(aliases.get(i));
        }
        String[] vals = Strings.split(findLocalized(prop
            + "-values", false, val.getScope()), ",", 0);
        for (int i = 0; i < vals.length; i++)
            if (!aliases.contains(vals[i]))
                allowed.add(vals[i]);
        try {
            Class<?> intf = Class.forName(findLocalized(prop
                + "-interface", true, val.getScope()), false,
                getClass().getClassLoader());
            pd.setValue(ATTRIBUTE_INTERFACE, intf.getName());
            String[] impls = Services.getImplementors(intf);
            for (int i = 0; i < impls.length; i++)
                if (!aliases.contains(impls[i]))
                    allowed.add(impls[i]);
        } catch (Throwable t) {
        }
        if (!allowed.isEmpty())
            pd.setValue(ATTRIBUTE_ALLOWED_VALUES, (String[]) allowed.toArray
                (new String[allowed.size()]));

        return pd;
    }

    /**
     * Find the given localized string, or return null if not found.
     */
    @SuppressWarnings("unchecked")
    private String findLocalized(String key, boolean fatal, Class<?> scope) {
        // find the localizer package that contains this key
        Localizer loc = null;

        // check the package that the value claims to be defined in, if
        // available, before we start guessing.
        if (scope != null) {
            loc = Localizer.forPackage(scope);
            try {
                return loc.getFatal(key).getMessage();
            } catch (MissingResourceException mse) {
            }
        }

        for (Class cls = getClass(); cls != Object.class;
            cls = cls.getSuperclass()) {
            loc = Localizer.forPackage(cls);
            try {
                return loc.getFatal(key).getMessage();
            } catch (MissingResourceException mse) {
            }
        }

        if (fatal)
            throw new MissingResourceException(key, getClass().getName(), key);
        return null;
    }

    ////////////////
    // To/from maps
    ////////////////

    /**
     * An internal method to retrieve properties, to support 2 public methods,
     * getAllProperties() and toProperties(boolean).
     * 
     * @param storeDefaults
     *            whether or not to retrieve a property if its value is the
     *            default value.
     * @return
     */
    public Map toProperties(boolean storeDefaults) {
        // clone properties before making any modifications; we need to keep
        // the internal properties instance consistent to maintain equals and
        // hashcode contracts
        Map<String, String> clone;
        if (_props == null)
            clone = new TreeMap<String, String>();
        else if (_props instanceof Properties)
            clone = (Map) ((Properties) _props).clone();
        else
            clone = new TreeMap<String, String>(_props);

        // if no existing properties or the properties should contain entries
        // with default values, add values to properties
        if (_props == null || storeDefaults) {
            String str;
            for (Value val : _vals) {
                // NOTE: Following was removed to hide Value.INVISIBLE properties, like connectionPassword
                // if key in existing properties, we already know value is up to date
                //if (_props != null && Configurations.containsProperty(val, _props) && val.isVisible())
                //    continue;
                str = val.getString();
                if ((str != null && (storeDefaults || !str.equals(val.getDefault()))))
                    setValue(clone, val);
            }
            if (_props == null)
                _props = new TreeMap(clone);
        }
        return clone;
    }
    
    public void fromProperties(Map map) {
        if (map == null || map.isEmpty())
            return;
        if (isReadOnly())
            throw new IllegalStateException(_loc.get("read-only").getMessage());

        // if the only previous call was to load defaults, forget them.
        // this way we preserve the original formatting of the user's props
        // instead of the defaults.  this is important for caching on
        // configuration objects
        if (_globals) {
            _props = null;
            _globals = false;
        }

        // copy the input to avoid mutation issues
        if (map instanceof HashMap)
            map = (Map) ((HashMap) map).clone();
        else if (map instanceof Properties)
            map = (Map) ((Properties) map).clone();
        else
            map = new LinkedHashMap(map);

        Map remaining = new HashMap(map);
        boolean ser = true;
        Object o;
        for (Value val : _vals) {
            o = findValue(map, val);
            if (o == null)
                continue;
            if (o instanceof String) {
                // OPENJPA-1830 Do not overwrite existing string values with "******"
                if ((!StringUtils.equals((String) o, val.getString())) &&
                        (!StringUtils.equals((String) o, Value.INVISIBLE)))
                    val.setString((String) o);
            } else {
                ser &= o instanceof Serializable;
                val.setObject(o);
            }
            Configurations.removeProperty(val.getProperty(), remaining);
        }
        
        // convention is to point product at a resource with the
        // <prefix>.properties System property; remove that property so we
        // we don't warn about it
        Configurations.removeProperty("properties", remaining);
        Configurations.removeProperty("Id", remaining, map);

        // now warn if there are any remaining properties that there
        // is an unhandled prop, and remove the unknown properties
        Map.Entry entry;
        for (Iterator itr = remaining.entrySet().iterator(); itr.hasNext();) {
            entry = (Map.Entry) itr.next();
            Object key = entry.getKey();
            if (key != null) {
                warnInvalidProperty((String) key);
                map.remove(key);
            }
        }

        // cache properties
        if (_props == null && ser)
            _props = map;
    }
    
    public List<String> getPropertyKeys(String propertyName) {
        Value value = getValue(propertyName);
        return value == null ? Collections.EMPTY_LIST : value.getPropertyKeys();
    }
    
    /**
     * Gets all known property keys.
     * The keys are harvested from the property names (including the equivalent names) of the registered values.
     * A key may be prefixed if the corresponding property name was without a prefix.
     * @see #fixPrefix(String)
     * The Values that are {@linkplain Value#makePrivate() marked private} are filtered out. 
     */
    public Set<String> getPropertyKeys() {
        synchronized (_supportedKeys) {
            if (_supportedKeys.size() == 0) {
                for (Value val : _vals) {
                    if (val.isPrivate())
                        continue;
                    List<String> keys = val.getPropertyKeys();
                    for (String key : keys) {
                        _supportedKeys.add(fixPrefix(key));
                    }
                }
            }
        }
        //OJ2257: Return a copy of _supportedKeys as calls to this method (e.g. 
        //BrokerImpl.getSupportedProperties()) may add to this set.
        return new TreeSet<String>(_supportedKeys);
    }
    
    /**
     * Adds a prefix <code>"openjpa."</code> to the given key, if necessary. A key is 
     * considered without prefix if it starts neither of <code>"openjpa."</code>, 
     * <code>"java."</code> and <code>"javax."</code>. 
     */
    String fixPrefix(String key) {
        return (key == null || hasKnownPrefix(key)) ? key : "openjpa."+key;
    }
    
    boolean hasKnownPrefix(String key) {
        String[] prefixes = ProductDerivations.getConfigurationPrefixes();
        for (String prefix : prefixes) {
            if (key.startsWith(prefix))
                return true;
        }
        return false;
    }

    /**
     * Adds <code>o</code> to <code>map</code> under key for <code>val</code>.
     * Use this method instead of attempting to add the value directly because 
     * this will account for the property prefix.
     */
    private void setValue(Map map, Value val) {
        Object key = val.getLoadKey();
        if (key == null) {
            List<String> keys = val.getPropertyKeys();
            for (String k : keys) {
                if (hasKnownPrefix(k)) {
                    key = k;
                    break;
                }
            }
            if (key == null) {
                key = "openjpa." + val.getProperty();
            }
        }
        Object external = val.isHidden() ? Value.INVISIBLE : 
            val instanceof ObjectValue ? val.getString() : val.get();
        map.put(key, external);
    }

    /**
     * Look up the given value, testing all available prefixes and all possible
     * property names. Detects if the given map contains multiple keys that
     * are equivalent names for the given value. 
     */
    private Object findValue(Map map, Value val) {
        Object result = null;
        List<String> partialKeys = val.getPropertyKeys();
        for (String partialKey : partialKeys) {
            String key = ProductDerivations.getConfigurationKey(
                partialKey, map);
            if (map.containsKey(key)) {
                // do not return immediately. Looping through all equivalent
                // property names will detect if the Map contains multiple keys
                // that are equivalent as it tries to set load key.
                val.setLoadKey(key);
                result = map.get(key);
            }
        }
        return result;
    }

    /**
     * Issue a warning that the specified property is not valid.
     */
    private void warnInvalidProperty(String propName) {
        if (propName != null && 
           (propName.startsWith("java.") || propName.startsWith("javax.persistence")|| propName.startsWith("sun."))) 
            return;
        if (!isInvalidProperty(propName))
            return;
        Log log = getConfigurationLog();
        if (log == null || !log.isWarnEnabled())
            return;

        // try to find the closest string to the invalid property
        // so that we can provide a hint in case of a misspelling
        String closest = StringDistance.getClosestLevenshteinDistance
            (propName, newPropertyList(), 15);

        if (closest == null)
            log.warn(_loc.get("invalid-property", propName));
        else
            log.warn(_loc.get("invalid-property-hint", propName, closest));
    }

    /**
     * Return a comprehensive list of recognized map keys.
     */
    private Collection<String> newPropertyList() {
        String[] prefixes = ProductDerivations.getConfigurationPrefixes();
        List<String> l = new ArrayList<String>(_vals.size() * prefixes.length);
        for(Value v : _vals) { 
            for (int j = 0; j < prefixes.length; j++)
                l.add(prefixes[j] + "." + v.getProperty());
        }
        return l;
    }

    /**
     * Returns true if the specified property name should raise a warning
     * if it is not found in the list of known properties.
     */
    protected boolean isInvalidProperty(String propName) {
        // handle warnings for openjpa.SomeString, but not for
        // openjpa.some.subpackage.SomeString, since it might be valid for some
        // specific implementation of OpenJPA
        String[] prefixes = ProductDerivations.getConfigurationPrefixes();
        for (String prefix : prefixes) {
            if (propName.toLowerCase().startsWith(prefix)
                && propName.length() > prefix.length() + 1
                && propName.indexOf('.', prefix.length()) == prefix.length()
                && propName.indexOf('.', prefix.length() + 1) == -1
                && "openjpa".equals(prefix))
                return true;
        }
        return false;
    }

    //////////////////////
    // Auto-configuration
    //////////////////////

    /**
     * This method loads the named resource as a properties file. It is
     * useful for auto-configuration tools so users can specify a
     * <code>properties</code> value with the name of a resource.
     */
    public void setProperties(String resourceName) throws IOException {

        if (!_deferResourceLoading) {
            String anchor = null;
            if (resourceName.indexOf("#") != -1) {
                anchor = resourceName.substring(resourceName.lastIndexOf("#") + 1);
                resourceName = resourceName.substring(0, resourceName.length() - anchor.length() - 1);
            }

            ProductDerivations.load(resourceName, anchor, getClass().getClassLoader()).setInto(this);
        }

        _auto = resourceName;
    }

    /**
     * This method loads the named file as a properties file. It is
     * useful for auto-configuration tools so users can specify a
     * <code>propertiesFile</code> value with the name of a file.
     */
    public void setPropertiesFile(File file) throws IOException {
        ProductDerivations.load(file, null, getClass().getClassLoader()).
            setInto(this);
        setDeferResourceLoading(false);
        _auto = file.toString();
    }

    /**
     * Return the resource that was set via auto-configuration methods
     * {@link #setProperties} or {@link #setPropertiesFile}, or null if none.
     */
    public String getPropertiesResource() {
        return _auto;
    }

    /////////////
    // Utilities
    /////////////

    /**
     * Performs an equality check based on equality of values.
     * {@link Value#equals(Object) Equality} of Values varies if the Value is
     * {@link Value#isDynamic() dynamic}.  
     */
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (other == null)
            return false;
        if (!getClass().equals(other.getClass()))
            return false;

        // compare properties
        ConfigurationImpl conf = (ConfigurationImpl) other;
        if (_vals.size() != conf.getValues().length)
        	return false;
        for(Value v : _vals) {
            String propName = v.getProperty();
        	Value thisV = this.getValue(propName);
            Value thatV = conf.getValue(propName);
        	if (!thisV.equals(thatV)) {
        		return false;
        	}
        }
        return true;
    }

    /**
     * Computes hash code based on the hashCodes of the values.
     * {@link Value#hashCode() HashCode} of a Value varies if the Value is
     * {@link Value#isDynamic() dynamic}.  
     */
    public int hashCode() {
        int hash = 0;
        for(Value v : _vals) { 
        	hash += v.hashCode();
        }
        return hash;
    }

    /**
     * Convert <code>propName</code> to a lowercase-with-hyphens-style string.
     * This algorithm is only designed for mixes of uppercase and lowercase 
     * letters and lone digits. A more sophisticated conversion should probably 
     * be handled by a proper parser generator or regular expressions.
     */
    public static String toXMLName(String propName) {
        if (propName == null)
            return null;
        StringBuilder buf = new StringBuilder();
        char c;
        for (int i = 0; i < propName.length(); i++) {
            c = propName.charAt(i);

            // convert sequences of all-caps to downcase with dashes around 
            // them. put a trailing cap that is followed by downcase into the
            // downcase word.
            if (i != 0 && Character.isUpperCase(c) 
                && (Character.isLowerCase(propName.charAt(i-1))
                || (i > 1 && i < propName.length() - 1
                && Character.isUpperCase(propName.charAt(i-1)) 
                && Character.isLowerCase(propName.charAt(i+1)))))
                buf.append('-');
            
            // surround sequences of digits with dashes.
            if (i != 0
                && ((!Character.isLetter(c) && Character.isLetter(propName
                    .charAt(i - 1))) 
                || (Character.isLetter(c) && !Character.isLetter(propName
                    .charAt(i - 1)))))
                buf.append('-');
            
            buf.append(Character.toLowerCase(c));
        }
        return buf.toString();
    }
    
    /**
     * Implementation of the {@link Externalizable} interface to read from
     * the properties written by {@link #writeExternal}.
     */
    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException {
        fromProperties((Map) in.readObject());
        _props = (Map) in.readObject();
        _globals = in.readBoolean();
    }

    /**
     * Implementation of the {@link Externalizable} interface to write
     * the properties returned by {@link #toProperties}.
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(toProperties(true));
        out.writeObject(_props);
        out.writeBoolean(_globals);
    }

    /**
     * Uses {@link #toProperties} and {@link #fromProperties} to clone
     * configuration.
     */
    public Object clone() {
        try {
            Constructor cons = getClass().getConstructor
                (new Class[]{ boolean.class });
            ConfigurationImpl clone = (ConfigurationImpl) cons.newInstance
                (new Object[]{ Boolean.FALSE });
            clone.fromProperties(toProperties(true));
            clone._props = (_props == null) ? null : new HashMap(_props);
            clone._globals = _globals;
            return clone;
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new ParseException(e);
        }
    }

    public boolean removeValue(Value val) {
        if (!_vals.remove(val))
            return false;
        val.removeListener(this);
        return true;
    }

    public <T extends Value> T addValue(T val) {
        _vals.add(val);
        val.addListener(this);
        return val;
    }

    /**
     * Add the given value to the set of configuration properties.
     */
    public StringValue addString(String property) {
        StringValue val = new StringValue(property);
        addValue(val);
        return val;
    }

    /**
     * Add the given value to the set of configuration properties.
     */
    public FileValue addFile(String property) {
        FileValue val = new FileValue(property);
        addValue(val);
        return val;
    }

    /**
     * Add the given value to the set of configuration properties.
     */
    public IntValue addInt(String property) {
        IntValue val = new IntValue(property);
        addValue(val);
        return val;
    }

    /**
     * Add the given value to the set of configuration properties.
     */
    public DoubleValue addDouble(String property) {
        DoubleValue val = new DoubleValue(property);
        addValue(val);
        return val;
    }

    /**
     * Add the given value to the set of configuration properties.
     */
    public BooleanValue addBoolean(String property) {
        BooleanValue val = new BooleanValue(property);
        addValue(val);
        val.setDefault("false");
        return val;
    }

    /**
     * Add the given value to the set of configuration properties.
     */
    public StringListValue addStringList(String property) {
        StringListValue val = new StringListValue(property);
        addValue(val);
        return val;
    }

    /**
     * Add the given value to the set of configuration properties.
     */
    public ObjectValue addObject(String property) {
        ObjectValue val = new ObjectValue(property);
        addValue(val);
        return val;
    }

    /**
     * Add the given value to the set of configuration properties.
     */
    public PluginValue addPlugin(String property, boolean singleton) {
        PluginValue val = new PluginValue(property, singleton);
        addValue(val);
        return val;
    }

    /**
     * Add the given value to the set of configuration properties.
     */
    public PluginListValue addPluginList(String property) {
        PluginListValue val = new PluginListValue(property);
        addValue(val);
        return val;
    }
    
    public ClassLoader getUserClassLoader() {
    	return _userCL;
    }
    
    public void setUserClassLoader(ClassLoader cl) {
    	_userCL = cl;
    }
}
