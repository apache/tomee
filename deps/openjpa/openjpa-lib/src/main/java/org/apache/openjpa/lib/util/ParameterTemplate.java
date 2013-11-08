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
package org.apache.openjpa.lib.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.security.AccessController;
import java.util.HashMap;
import java.util.Map;

/**
 * A template that allows parameter substitutions. Parameters should be
 * placed in the template in the form ${param-name}. Use the
 * {@link #setParameter} method to set the parameter values, which will be
 * substituted into the template on calls to {@link #write} and
 * {@link #toString}. If a parameter is encountered that hasn't been set, then
 * the parameter key is used to lookup the corresponding System property.
 *
 * @author Abe White
 * @nojavadoc
 */
public class ParameterTemplate {

    private static final String SEP = J2DoPrivHelper.getLineSeparator();

    private final StringBuffer _buf = new StringBuffer();
    private final Map _params = new HashMap();

    /**
     * Add the given value to the internal template.
     */
    public ParameterTemplate append(String value) {
        _buf.append(value);
        return this;
    }

    /**
     * Add the given value to the internal template.
     */
    public ParameterTemplate append(boolean value) {
        _buf.append(value);
        return this;
    }

    /**
     * Add the given value to the internal template.
     */
    public ParameterTemplate append(char value) {
        _buf.append(value);
        return this;
    }

    /**
     * Add the given value to the internal template.
     */
    public ParameterTemplate append(double value) {
        _buf.append(value);
        return this;
    }

    /**
     * Add the given value to the internal template.
     */
    public ParameterTemplate append(float value) {
        _buf.append(value);
        return this;
    }

    /**
     * Add the given value to the internal template.
     */
    public ParameterTemplate append(int value) {
        _buf.append(value);
        return this;
    }

    /**
     * Add the given value to the internal template.
     */
    public ParameterTemplate append(long value) {
        _buf.append(value);
        return this;
    }

    /**
     * Add the given value to the internal template.
     */
    public ParameterTemplate append(short value) {
        _buf.append(value);
        return this;
    }

    /**
     * Add the given value to the internal template.
     */
    public ParameterTemplate append(Object value) {
        _buf.append(value);
        return this;
    }

    /**
     * Add the given value to the internal template.
     */
    public ParameterTemplate append(InputStream in) throws IOException {
        return append(new InputStreamReader(in));
    }

    /**
     * Add the given value to the internal template.
     */
    public ParameterTemplate append(Reader reader) throws IOException {
        BufferedReader buf = new BufferedReader(reader);
        String line;
        while ((line = buf.readLine()) != null)
            _buf.append(line).append(SEP);
        return this;
    }

    /**
     * Add the given value to the internal template.
     */
    public ParameterTemplate append(File file) throws IOException {
        FileReader reader = new FileReader(file);
        try {
            return append(reader);
        } finally {
            try {
                reader.close();
            } catch (IOException ioe) {
            }
        }
    }

    /**
     * Return true if the given parameter has been given a value.
     */
    public boolean hasParameter(String name) {
        return _params.containsKey(name);
    }

    /**
     * Return the value set for the given parameter.
     */
    public Object getParameter(String name) {
        return _params.get(name);
    }

    /**
     * Set the value for the given parameter.
     */
    public Object setParameter(String name, Object val) {
        return _params.put(name, val);
    }

    /**
     * Set the values for all the parameters in the given map.
     */
    public void setParameters(Map params) {
        _params.putAll(params);
    }

    /**
     * Clear the recorded parameter values.
     */
    public void clearParameters() {
        _params.clear();
    }

    /**
     * Return a copy of the internal value template with all parameters
     * substituted with their current values.
     */
    public String toString() {
        if (_buf.length() == 0 || _params.isEmpty())
            return _buf.toString();

        StringBuilder copy = new StringBuilder();
        StringBuilder param = null;
        char ch, last = 0;
        for (int i = 0; i < _buf.length(); i++) {
            ch = _buf.charAt(i);
            if (last == '$' && ch == '{') {
                copy.deleteCharAt(copy.length() - 1);
                param = new StringBuilder();
            } else if (ch == '}' && param != null) {
                if (_params.containsKey(param.toString()))
                    copy.append(_params.get(param.toString()));
                else
                    copy.append(AccessController.doPrivileged(
                        J2DoPrivHelper.getPropertyAction(param.toString())));
                param = null;
            } else if (param != null)
                param.append(ch);
            else
                copy.append(ch);

            last = ch;
        }
        return copy.toString();
    }

    /**
     * Write the internal value template with all parameters
     * substituted with their current values.
     */
    public void write(OutputStream out) throws IOException {
        write(new OutputStreamWriter(out));
    }

    /**
     * Write the internal value template with all parameters
     * substituted with their current values.
     */
    public void write(Writer writer) throws IOException {
        writer.write(toString());
        writer.flush();
    }

    /**
     * Write the internal value template with all parameters
     * substituted with their current values.
     */
    public void write(File file) throws IOException {
        FileWriter writer = new FileWriter(file);
        try {
            write(writer);
        } finally {
            try {
                writer.close();
            } catch (IOException ioe) {
            }
        }
    }
}
