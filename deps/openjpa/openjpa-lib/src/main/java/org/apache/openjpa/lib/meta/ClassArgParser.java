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
package org.apache.openjpa.lib.meta;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.openjpa.lib.util.Files;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import serp.bytecode.lowlevel.ConstantPoolTable;
import serp.util.Strings;

/**
 * Parser used to resolve arguments into java classes.
 * Interprets command-line args as either class names, .class files or
 * resources, .java files or resources, or metadata files or resources
 * conforming to the common format defined by {@link CFMetaDataParser}.
 * Transforms the information in these args into {@link Class} instances.
 * Note that when parsing .java files, only the main class in the file
 * is detected. Other classes defined in the file, such as inner classes,
 * are not added to the returned classes list.
 *
 * @author Abe White
 * @nojavadoc
 */
public class ClassArgParser {

    private static final int TOKEN_EOF = -1;
    private static final int TOKEN_NONE = 0;
    private static final int TOKEN_PACKAGE = 1;
    private static final int TOKEN_CLASS = 2;
    private static final int TOKEN_PACKAGE_NOATTR = 3;
    private static final int TOKEN_CLASS_NOATTR = 4;

    private static final Localizer _loc = Localizer.forPackage
        (ClassArgParser.class);

    private ClassLoader _loader = null;
    private char[] _packageAttr = "name".toCharArray();
    private char[] _classAttr = "name".toCharArray();
    private char[][] _beginElements = { { 'p' }, { 'c' } };
    private char[][] _endElements = { "ackage".toCharArray(),
        "lass".toCharArray() };

    /**
     * The class loader with which to load parsed classes.
     */
    public ClassLoader getClassLoader() {
        return _loader;
    }

    /**
     * The class loader with which to load parsed classes.
     */
    public void setClassLoader(ClassLoader loader) {
        _loader = loader;
    }

    /**
     * Set the the relevant metadata file structure so that metadata files
     * containing class names can be parsed. Null attribute names indicate
     * that the text content of the element contains the data.
     */
    public void setMetaDataStructure(String packageElementName,
        String packageAttributeName, String[] classElementNames,
        String classAttributeName) {
        // calculate how many chars deep we have to go to identify each element
        // name as unique.  this is extremely inefficient for large N, but
        // should never be called for more than a few elements
        char[] buf = new char[classElementNames.length + 1];
        int charIdx = 0;
        for (; true; charIdx++) {
            for (int i = 0; i < buf.length; i++) {
                if (i == 0) {
                    if (charIdx == packageElementName.length())
                        throw new UnsupportedOperationException(_loc.get
                            ("cant-diff-elems").getMessage());
                    buf[i] = packageElementName.charAt(charIdx);
                } else {
                    if (charIdx == classElementNames[i - 1].length())
                        throw new UnsupportedOperationException(_loc.get
                            ("cant-diff-elems").getMessage());
                    buf[i] = classElementNames[i - 1].charAt(charIdx);
                }
            }
            if (charsUnique(buf))
                break;
        }

        _packageAttr = (packageAttributeName == null) ? null
            : packageAttributeName.toCharArray();
        _classAttr = (classAttributeName == null) ? null
            : classAttributeName.toCharArray();
        _beginElements = new char[classElementNames.length + 1][];
        _endElements = new char[classElementNames.length + 1][];
        _beginElements[0] = packageElementName.substring(0, charIdx + 1).
            toCharArray();
        _endElements[0] = packageElementName.substring(charIdx + 1).
            toCharArray();
        for (int i = 0; i < classElementNames.length; i++) {
            _beginElements[i + 1] = classElementNames[i].
                substring(0, charIdx + 1).toCharArray();
            _endElements[i + 1] = classElementNames[i].
                substring(charIdx + 1).toCharArray();
        }
    }

    /**
     * Return true if all characters in given buffer are unique.
     */
    private static boolean charsUnique(char[] buf) {
        for (int i = buf.length - 1; i >= 0; i--)
            for (int j = 0; j < i; j++)
                if (buf[j] == buf[i])
                    return false;
        return true;
    }

    /**
     * Return the {@link Class} representation of the class(es) named in the
     * given arg.
     *
     * @param arg a class name, .java file, .class file, or metadata
     * file naming the type(s) to act on
     */
    public Class<?>[] parseTypes(String arg) {
        String[] names = parseTypeNames(arg);
        Class<?>[] objs = new Class[names.length];
        for (int i = 0; i < names.length; i++)
            objs[i] = Strings.toClass(names[i], _loader);
        return objs;
    }

    /**
     * Return the {@link Class} representation of the class(es) named in the
     * given metadatas.
     */
    public Class<?>[] parseTypes(MetaDataIterator itr) {
        String[] names = parseTypeNames(itr);
        Class<?>[] objs = new Class[names.length];
        for (int i = 0; i < names.length; i++)
            objs[i] = Strings.toClass(names[i], _loader);
        return objs;
    }

    /**
     * Return a mapping of each metadata resource to an array of its
     * contained classes.
     */
    public Map<Object, Class<?>[]> mapTypes(MetaDataIterator itr) {
        Map<Object, String[]> map = mapTypeNames(itr);
        Map<Object, Class<?>[]> rval = new HashMap<Object, Class<?>[]>();
        Map.Entry<Object, String[]> entry;
        String[] names;
        Class<?>[] objs;
        for (Iterator<Map.Entry<Object, String[]>> i =
            map.entrySet().iterator(); i.hasNext();) {
            entry = i.next();
            names = entry.getValue();
            objs = new Class[names.length];
            for (int j = 0; j < names.length; j++) {
                objs[j] = Strings.toClass(names[j], _loader);
            }
            rval.put(entry.getKey(), objs);
        }
        return rval;
    }

    /**
     * Return the names of the class(es) from the given arg.
     *
     * @param arg a class name, .java file, .class file, or metadata
     * file naming the type(s) to act on
     * @throws IllegalArgumentException with appropriate message on error
     */
    public String[] parseTypeNames(String arg) {
        if (arg == null)
            return new String[0];

        try {
            File file = Files.getFile(arg, _loader);
            if (arg.endsWith(".class"))
                return new String[]{ getFromClassFile(file) };
            if (arg.endsWith(".java"))
                return new String[]{ getFromJavaFile(file) };
            if ((AccessController.doPrivileged(
                J2DoPrivHelper.existsAction(file))).booleanValue()) {
                Collection<String> col = getFromMetaDataFile(file);
                return col.toArray(new String[col.size()]);
            }
        } catch (Exception e) {
            throw new NestableRuntimeException(
                _loc.get("class-arg", arg).getMessage(), e);
        }

        // must be a class name
        return new String[]{ arg };
    }

    /**
     * Return the names of the class(es) from the given metadatas.
     */
    public String[] parseTypeNames(MetaDataIterator itr) {
        if (itr == null)
            return new String[0];

        List<String> names = new ArrayList<String>();
        Object source = null;
        try {
            while (itr.hasNext()) {
                source = itr.next();
                appendTypeNames(source, itr.getInputStream(), names);
            }
        } catch (Exception e) {
            throw new NestableRuntimeException(
                _loc.get("class-arg", source).getMessage(), e);
        }
        return names.toArray(new String[names.size()]);
    }

    /**
     * Parse the names in the given metadata iterator stream, closing the
     * stream on completion.
     */
    private void appendTypeNames(Object source, InputStream in,
        List<String> names) throws IOException {
        try {
            if (source.toString().endsWith(".class"))
                names.add(getFromClass(in));
            names.addAll(getFromMetaData(new InputStreamReader(in)));
        } finally {
            try {
                in.close();
            } catch (IOException ioe) {
            }
        }
    }

    /**
     * Return a mapping of each metadata resource to an array of its contained
     * class names.
     */
    public Map<Object, String[]> mapTypeNames(MetaDataIterator itr) {
        if (itr == null)
            return Collections.emptyMap();

        Map<Object, String []> map = new HashMap<Object, String[]>();
        Object source = null;
        List<String> names = new ArrayList<String>();
        try {
            while (itr.hasNext()) {
                source = itr.next();
                appendTypeNames(source, itr.getInputStream(), names);
                if (!names.isEmpty()) {
                    map.put(source, names.toArray(new String[names.size()]));
                }
                names.clear();
            }
        } catch (Exception e) {
            throw new NestableRuntimeException(
                _loc.get("class-arg", source).getMessage(), e);
        }
        return map;
    }

    /**
     * Returns the class named in the given .class file.
     */
    private String getFromClassFile(File file) throws IOException {
        FileInputStream fin = null;
        try {
            fin = AccessController.doPrivileged(
                J2DoPrivHelper.newFileInputStreamAction(file));
            return getFromClass(fin);
        } catch (PrivilegedActionException pae) {
            throw (FileNotFoundException) pae.getException();
        } finally {
            if (fin != null)
                try {
                    fin.close();
                } catch (IOException ioe) {
                }
        }
    }

    /**
     * Returns the class name in the given .class bytecode.
     */
    private String getFromClass(InputStream in) throws IOException {
        ConstantPoolTable table = new ConstantPoolTable(in);
        int idx = table.getEndIndex();
        idx += 2; // access flags
        int clsEntry = table.readUnsignedShort(idx);
        int utfEntry = table.readUnsignedShort(table.get(clsEntry));
        return table.readString(table.get(utfEntry)).replace('/', '.');
    }

    /**
     * Returns the class named in the given .java file.
     */
    private String getFromJavaFile(File file) throws IOException {
        BufferedReader in = null;
        try {
            // find the line with the package declaration
            in = new BufferedReader(new FileReader(file));
            String line;
            StringBuilder pack = null;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("package ")) {
                    line = line.substring(8).trim();

                    // strip off anything beyond the package declaration
                    pack = new StringBuilder();
                    for (int i = 0; i < line.length(); i++) {
                        if (Character.isJavaIdentifierPart(line.charAt(i))
                            || line.charAt(i) == '.')
                            pack.append(line.charAt(i));
                        else
                            break;
                    }
                    break;
                }
            }

            // strip '.java'
            String clsName = file.getName();
            clsName = clsName.substring(0, clsName.length() - 5);

            // prefix with package
            if (pack != null && pack.length() > 0)
                clsName = pack + "." + clsName;

            return clsName;
        } finally {
            if (in != null)
                try { in.close(); } catch (IOException ioe) {}
        }
    }

    /**
     * Returns the classes named in the given common format metadata file.
     */
    private Collection<String> getFromMetaDataFile(File file)
        throws IOException {
        FileReader in = null;
        try {
            in = new FileReader(file);
            return getFromMetaData(in);
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException ioe) {
                }
        }
    }

    /**
     * Returns the classes named in the given common format metadata stream.
     */
    private Collection<String> getFromMetaData(Reader xml) throws IOException {
        Collection<String> names = new ArrayList<String>();
        BufferedReader in = new BufferedReader(xml);

        boolean comment = false;
        int token = TOKEN_NONE;
        String pkg = "";
        String name;
        read:
        for (int ch = 0, last = 0, last2 = 0;
            ch == '<' || (ch = in.read()) != -1; last2 = last, last = ch) {
            // handle comments
            if (comment && last2 == '-' && last == '-' && ch == '>') {
                comment = false;
                continue;
            }
            if (comment) {
                if (ch == '<') {
                    ch = in.read();
                    if (ch == -1)
                        break read;
                }
                continue;
            }
            if (last2 == '<' && last == '!' && ch == '-') {
                comment = true;
                continue;
            }

            // if not an element start, skip it
            if (ch != '<')
                continue;
            token = TOKEN_NONE; // reset token
            last = ch; // update needed for comment detection
            ch = readThroughWhitespace(in);
            if (ch == '/' || ch == '!' || ch == '?')
                continue;

            // read element name; look for packages and classes
            token = readElementToken(ch, in);
            switch (token) {
                case TOKEN_EOF:
                    break read;
                case TOKEN_PACKAGE:
                    pkg = readAttribute(in, _packageAttr);
                    if (pkg == null)
                        break read;
                    break;
                case TOKEN_PACKAGE_NOATTR:
                    pkg = readElementText(in);
                    if (pkg == null)
                        break read;
                    ch = '<'; // reading element text reads to next '<'
                    break;
                case TOKEN_CLASS:
                    name = readAttribute(in, _classAttr);
                    if (name == null)
                        break read;
                    if (pkg.length() > 0 && name.indexOf('.') == -1)
                        names.add(pkg + "." + name);
                    else
                        names.add(name);
                    break;
                case TOKEN_CLASS_NOATTR:
                    name = readElementText(in);
                    if (name == null)
                        break read;
                    ch = '<'; // reading element text reads to next '<'
                    if (pkg.length() > 0 && name.indexOf('.') == -1)
                        names.add(pkg + "." + name);
                    else
                        names.add(name);
                    break;
            }
        }
        return names;
    }

    /**
     * Read the name of the current XML element and return the matching token.
     */
    private int readElementToken(int ch, Reader in) throws IOException {
        // look through the beginning element names to find what element this
        // might be(if any)
        int matchIdx = -1;
        int matched = 0;
        int dq = 0;
        for (int beginIdx = 0; beginIdx < _beginElements[0].length; beginIdx++)
        {
            if (beginIdx != 0)
                ch = in.read();
            if (ch == -1)
                return TOKEN_EOF;

            matched = 0;
            for (int i = 0; i < _beginElements.length; i++) {
                if ((dq & (2 << i)) != 0)
                    continue;

                if (ch == _beginElements[i][beginIdx]) {
                    matchIdx = i;
                    matched++;
                } else
                    dq |= 2 << i;
            }

            if (matched == 0)
                break;
        }
        if (matched != 1)
            return TOKEN_NONE;

        // make sure the rest of the element name matches
        char[] match = _endElements[matchIdx];
        for (int i = 0; i < match.length; i++) {
            ch = in.read();
            if (ch == -1)
                return TOKEN_EOF;
            if (ch != match[i])
                return TOKEN_NONE;
        }

        // read the next char to make sure we finished the element name
        ch = in.read();
        if (ch == -1)
            return TOKEN_EOF;
        if (ch == '>') {
            if (matchIdx == 0 && _packageAttr == null)
                return TOKEN_PACKAGE_NOATTR;
            if (matchIdx != 0 && _classAttr == null)
                return TOKEN_CLASS_NOATTR;
        } else if (Character.isWhitespace((char) ch)) {
            if (matchIdx == 0 && _packageAttr != null)
                return TOKEN_PACKAGE;
            if (matchIdx != 0 && _classAttr != null)
                return TOKEN_CLASS;
        }
        return TOKEN_NONE;
    }

    /**
     * Read the attribute with the given name in chars of the current XML
     * element.
     */
    private String readAttribute(Reader in, char[] name) throws IOException {
        int expected = 0;
        for (int ch, last = 0; true; last = ch) {
            ch = in.read();
            if (ch == -1)
                return null;
            if (ch == '>')
                return "";

            // if not expected char or still looking for 'n' and previous
            // char is not whitespace, keep looking
            if (ch != name[expected] || (expected == 0 && last != 0
                && !Character.isWhitespace((char) last))) {
                expected = 0;
                continue;
            }

            // found expected char; have we found the whole "name"?
            expected++;
            if (expected == name.length) {
                // make sure the next char is '='
                ch = readThroughWhitespace(in);
                if (ch == -1)
                    return null;
                if (ch != '=') {
                    expected = 0;
                    continue;
                }

                // toss out any subsequent whitespace and the next char, which
                // is the opening quote for the attr value, then read until the
                // closing quote
                readThroughWhitespace(in);
                return readAttributeValue(in);
            }
        }
    }

    /**
     * Read the current text value until the next element.
     */
    private String readElementText(Reader in) throws IOException {
        StringBuilder buf = null;
        int ch;
        while (true) {
            ch = in.read();
            if (ch == -1)
                return null;
            if (ch == '<')
                break;
            if (Character.isWhitespace((char) ch))
                continue;
            if (buf == null)
                buf = new StringBuilder();
            buf.append((char) ch);
        }
        return (buf == null) ? "" : buf.toString();
    }

    /**
     * Read until the next non-whitespace character.
     */
    private int readThroughWhitespace(Reader in) throws IOException {
        int ch;
        while (true) {
            ch = in.read();
            if (ch == -1 || !Character.isWhitespace((char) ch))
                return ch;
        }
    }

    /**
     * Return the current attribute value.
     */
    private String readAttributeValue(Reader in) throws IOException {
        StringBuilder buf = new StringBuilder();
        int ch;
        while (true) {
            ch = in.read();
            if (ch == -1)
                return null;
            if (ch == '\'' || ch == '"')
                return buf.toString();
            buf.append((char) ch);
        }
    }
}
