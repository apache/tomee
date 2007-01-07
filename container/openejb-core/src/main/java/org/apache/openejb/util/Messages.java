/**
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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Arrays;

public class Messages {
    static private Hashtable _rbBundles = new Hashtable();
    static private Hashtable _rbFormats = new Hashtable();
    static private Locale _globalLocale;

    private ResourceBundle _messages;
    private Hashtable _formats;
    private Locale _locale;
    private String _resourceName;

    public Messages(String resourceName) {
        synchronized (Messages.class) {
            _locale = _globalLocale;
            _resourceName = resourceName + ".Messages";

            ResourceBundle rb = (ResourceBundle) _rbBundles.get(_resourceName);
            if (rb == null) {
                init();
            } else {
                _messages = rb;
                _formats = (Hashtable) _rbFormats.get(_resourceName);
            }
        }

    }

    protected void init() {
        try {
            if (_locale == null)
                _messages = ResourceBundle.getBundle(_resourceName);
            else
                _messages = ResourceBundle.getBundle(_resourceName, _locale);
        } catch (Exception except) {
            _messages = new EmptyResourceBundle();
        }

        _formats = new Hashtable();

        _rbBundles.put(_resourceName, _messages);
        _rbFormats.put(_resourceName, _formats);
    }

    public String format(String message) {
        return message(message);
    }

    public String format(String message, Object... args) {
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
