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

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.log.LogFactory;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.ParseException;

/**
 * A plugin {@link Value} consisting of plugin name and properties.
 * Plugins should be specified in the form:<br />
 * <code>&lt;plugin-name&gt;(&lt;prop1&gt;=&lt;val1&gt;, ...)</code><br />
 * Both the plugin name and prop list are optional, so that the following
 * forms are also valid:<br />
 * <code>&lt;plugin-name&gt;</code><br />
 * <code>&lt;prop1&gt;=&lt;val1&gt; ...</code>
 * Defaults and aliases on plugin values apply only to the plugin name.
 *
 * @author Abe White
 */
public class PluginValue extends ObjectValue {

    private static final Localizer _loc = Localizer.forPackage
        (PluginValue.class);

    private final boolean _singleton;
    private String _name = null;
    private String _props = null;

    public PluginValue(String prop, boolean singleton) {
        super(prop);
        _singleton = singleton;
    }

    /**
     * Whether this value is a singleton.
     */
    public boolean isSingleton() {
        return _singleton;
    }

    /**
     * The plugin class name.
     */
    public String getClassName() {
        return _name;
    }

    /**
     * The plugin class name.
     */
    public void setClassName(String name) {
        assertChangeable();
        String oldName = _name;
        _name = name;
        if (!StringUtils.equals(oldName, name)) {
            if (_singleton)
                set(null, true);
            valueChanged();
        }
    }

    /**
     * The plugin properties.
     */
    public String getProperties() {
        return _props;
    }

    /**
     * The plugin properties.
     */
    public void setProperties(String props) {
        String oldProps = _props;
        _props = props;
        if (!StringUtils.equals(oldProps, props)) {
            if (_singleton)
                set(null, true);
            valueChanged();
        }
    }

    /**
     * Instantiate the plugin as an instance of the given class.
     */
    public Object instantiate(Class<?> type, Configuration conf, boolean fatal)
    {
        Object obj = newInstance(_name, type, conf, fatal);
        
        // ensure plugin value is compatible with plugin type
        if (obj != null && !type.isAssignableFrom(obj.getClass())) {
            Log log = (conf == null || type.equals(LogFactory.class)) ? null : conf.getConfigurationLog();
            String msg = getIncompatiblePluginMessage(obj, type);
            if (log != null && log.isErrorEnabled()) {
            	log.error(msg);
            }
            if (fatal) {
            	throw new ParseException(msg);
            }
            return null;
        }
        
        Configurations.configureInstance(obj, conf, _props,
            (fatal) ? getProperty() : null);
        if (_singleton)
            set(obj, true);
        return obj;
    }

    private String getIncompatiblePluginMessage(Object obj, Class<?> type) {
		return _loc.get("incompatible-plugin", 
            new Object[]{ _name, 
                          obj == null ? null : obj.getClass().getName(),
                          type == null ? null : type.getName()
                          }).toString();
	}

	/**
     * Configure the given object.
     */
    public Object configure(Object obj, Configuration conf, boolean fatal) {
        Configurations.configureInstance(obj, conf, _props,
            (fatal) ? getProperty() : null);
        if (_singleton)
            set(obj, true);
        return obj;
    }
    

    public void set(Object obj, boolean derived) {
        if (!_singleton)
            throw new IllegalStateException(_loc.get("not-singleton",
                getProperty()).getMessage());
        super.set(obj, derived);
    }

    public String getString() {
        return Configurations.getPlugin(alias(_name), _props);
    }

    public void setString(String str) {
    	assertChangeable();
        _name = Configurations.getClassName(str);
        _name = unalias(_name);
        _props = Configurations.getProperties(str);
        if (_singleton)
            set(null, true);
        valueChanged();
    }

    public Class<Object> getValueType() {
        return Object.class;
    }

    protected void objectChanged() {
        Object obj = get();
        _name = (obj == null) ? unalias(null) : obj.getClass().getName();
        _props = null;
    }

    protected String getInternalString() {
        // should never get called
        throw new IllegalStateException();
    }

    protected void setInternalString(String str) {
        // should never get called
        throw new IllegalStateException();
    }
}
