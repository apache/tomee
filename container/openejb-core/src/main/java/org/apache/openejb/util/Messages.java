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
    private static Map<String, ResourceBundle> _rbBundles = new ConcurrentHashMap<String, ResourceBundle>();
    private static Map<String, Map<String, MessageFormat>> _rbFormats = new ConcurrentHashMap<String, Map<String, MessageFormat>>();
    private static Locale _globalLocale;

    private ResourceBundle messages;
    private Map<String, MessageFormat> formats;
    private final Locale locale;
    private final String resourceName;

    public Messages(final Class clazz) {
        this(packageName(clazz));
    }

    private static String packageName(final Class clazz) {
        final String name = clazz.getName();
        return name.substring(0, name.lastIndexOf('.'));
    }

    public Messages(final String resourceName) {
        this.resourceName = resourceName + ".Messages";
        synchronized (Messages.class) {
            locale = _globalLocale;
        }
    }

    protected void init() {
        if (formats != null && messages != null) {
            return;
        }

        final ResourceBundle rb = _rbBundles.get(resourceName);
        if (rb == null) {
            try {
                if (locale == null) {
                    messages = ResourceBundle.getBundle(resourceName);
                } else {
                    messages = ResourceBundle.getBundle(resourceName, locale);
                }
            } catch (final Exception except) {
                messages = new EmptyResourceBundle();
            }

            formats = new ConcurrentHashMap<>();

            _rbBundles.put(resourceName, messages);
            _rbFormats.put(resourceName, formats);
        } else {
            messages = rb;
            formats = _rbFormats.get(resourceName);
        }
    }

    public String format(final String message) {
        return message(message);
    }

    public String format(final String message, final Object... args) {
        init();
        if (locale != _globalLocale) {
            synchronized (Messages.class) {
                init();
            }
        }

        MessageFormat mf;
        final String msg;

        try {
            mf = (MessageFormat) formats.get(message);
            if (mf == null) {
                try {
                    msg = messages.getString(message);
                } catch (final MissingResourceException except) {
                    return message + (args != null ? " " + Arrays.toString(args) : "");
                }
                mf = new MessageFormat(msg);
                formats.put(message, mf);
            }
            return mf.format(args);
        } catch (final Exception except) {
            return "An internal error occured while processing message " + message;
        }
    }

    public String message(final String message) {
        init();
        if (locale != _globalLocale) {
            synchronized (Messages.class) {
                init();
            }
        }

        try {
            return messages.getString(message);
        } catch (final MissingResourceException except) {
            return message;
        }
    }

    public static void setLocale(final Locale locale) {
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

        protected Object handleGetObject(final String name) {
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
