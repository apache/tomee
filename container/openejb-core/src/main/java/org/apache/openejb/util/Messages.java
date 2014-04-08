/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.util;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public class Messages {
    static private Map<String, ResourceBundle> _rbBundles = new ConcurrentHashMap<String, ResourceBundle>();
    static private Map<String, Map<String, MessageFormat>> _rbFormats = new ConcurrentHashMap<String, Map<String, MessageFormat>>();
    static private Locale _globalLocale;

    private ResourceBundle _messages;
    private Map<String, MessageFormat> _formats;
    private Locale _locale;
    private String _resourceName;

    public Messages(Class clazz) {
        this(packageName(clazz));
    }

    private static String packageName(Class clazz) {
        String name = clazz.getName();
        return name.substring(0, name.lastIndexOf("."));
    }

    public Messages(String resourceName) {
        _resourceName = resourceName + ".Messages";
        synchronized (Messages.class) {
            _locale = _globalLocale;
        }
    }

    protected void init() {
        if (_formats != null && _messages != null) {
            return;
        }

        final ResourceBundle rb = _rbBundles.get(_resourceName);
        if (rb == null) {
            try {
                if (_locale == null) {
                    _messages = ResourceBundle.getBundle(_resourceName);
                } else {
                    _messages = ResourceBundle.getBundle(_resourceName, _locale);
                }
            } catch (Exception except) {
                _messages = new EmptyResourceBundle();
            }

            _formats = new ConcurrentHashMap<String, MessageFormat>();

            _rbBundles.put(_resourceName, _messages);
            _rbFormats.put(_resourceName, _formats);
        } else {
            _messages = rb;
            _formats = _rbFormats.get(_resourceName);
        }
    }

    public String format(String message) {
        return message(message);
    }

    public String format(String message, Object... args) {
        init();
        if (_locale != _globalLocale) {
            synchronized (Messages.class) {
                init();
            }
        }

        MessageFormat mf;
        String msg;

        try {
            mf = (MessageFormat) _formats.get(message);
            if (mf == null) {
                try {
                    msg = _messages.getString(message);
                } catch (MissingResourceException except) {
                    return message + (args != null ? " " + Arrays.toString(args) : "");
                }
                mf = new MessageFormat(msg);
                _formats.put(message, mf);
            }
            return mf.format(args);
        } catch (Exception except) {
            return "An internal error occured while processing message " + message;
        }
    }

    public String message(String message) {
        init();
        if (_locale != _globalLocale) {
            synchronized (Messages.class) {
                init();
            }
        }

        try {
            return _messages.getString(message);
        } catch (MissingResourceException except) {
            return message;
        }
    }

    static public void setLocale(Locale locale) {
        synchronized (Messages.class) {
            _globalLocale = locale;
            _rbBundles = new Hashtable();
            _rbFormats = new Hashtable();
        }
    }

    static {
        setLocale(Locale.getDefault());
    }

    private static final class EmptyResourceBundle extends ResourceBundle implements Enumeration {

        public Enumeration getKeys() {
            return this;
        }

        protected Object handleGetObject(String name) {
            return "[Missing message " + name + "]";
        }

        public boolean hasMoreElements() {
            return false;
        }

        public Object nextElement() {
            return null;
        }

    }

}
