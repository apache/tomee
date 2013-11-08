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
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/*
 * ### things to add: - should probably be a SourceTracker
 * - if an entry is removed, should there be an option to remove comments
 * just before the entry(a la javadoc)?
 * - should we have an option to clean up whitespace?
 * - potentially would be interesting to add comments about each
 * property that OpenJPA adds to this object. We'd want to make the
 * automatic comment-removing code work first, though, so that if
 * someone then removed the property, the comments would go away.
 * - would be neat if DuplicateKeyException could report line numbers of
 * offending entries.
 * - putAll() with another FormatPreservingProperties should be smarter
 */

/**
 * A specialization of {@link Properties} that stores its contents
 * in the same order and with the same formatting as was used to read
 * the contents from an input stream. This is useful because it means
 * that a properties file loaded via this object and then written
 * back out later on will only be different where changes or
 * additions were made.
 * By default, the {@link #store} method in this class does not
 * behave the same as {@link Properties#store}. You can cause an
 * instance to approximate the behavior of {@link Properties#store}
 * by invoking {@link #setDefaultEntryDelimiter} with <code>=</code>,
 * {@link #setAddWhitespaceAfterDelimiter} with <code>false</code>, and
 * {@link #setAllowDuplicates} with <code>true</code>. However, this
 * will only influence how the instance will write new values, not how
 * it will write existing key-value pairs that are modified.
 * In conjunction with a conservative output writer, it is
 * possible to only write to disk changes / additions.
 * This implementation does not permit escaped ' ', '=', ':'
 * characters in key names.
 *
 * @since 0.3.3
 */
public class FormatPreservingProperties extends Properties {

    private static Localizer _loc = Localizer.forPackage
        (FormatPreservingProperties.class);

    private char defaultEntryDelimiter = ':';
    private boolean addWhitespaceAfterDelimiter = true;
    private boolean allowDuplicates = false;
    private boolean insertTimestamp = false;

    private PropertySource source;
    private LinkedHashSet newKeys = new LinkedHashSet();
    private HashSet modifiedKeys = new HashSet();

    // marker that indicates that we're not deserializing
    private transient boolean isNotDeserializing = true;
    private transient boolean isLoading = false;

    public FormatPreservingProperties() {
        this(null);
    }

    public FormatPreservingProperties(Properties defaults) {
        super(defaults);
    }

    /**
     * The character to use as a delimiter between property keys and values.
     *
     * @param defaultEntryDelimiter either ':' or '='
     */
    public void setDefaultEntryDelimiter(char defaultEntryDelimiter) {
        this.defaultEntryDelimiter = defaultEntryDelimiter;
    }

    /**
     * See {@link #setDefaultEntryDelimiter}
     */
    public char getDefaultEntryDelimiter() {
        return this.defaultEntryDelimiter;
    }

    /**
     * If set to <code>true</code>, this properties object will add a
     * space after the delimiter character(if the delimiter is not
     * the space character). Else, this will not add a space.
     * Default value: <code>true</code>. Note that {@link
     * Properties#store} never writes whitespace.
     */
    public void setAddWhitespaceAfterDelimiter(boolean add) {
        this.addWhitespaceAfterDelimiter = add;
    }

    /**
     * If set to <code>true</code>, this properties object will add a
     * space after the delimiter character(if the delimiter is not
     * the space character). Else, this will not add a space.
     * Default value: <code>true</code>. Note that {@link
     * Properties#store} never writes whitespace.
     */
    public boolean getAddWhitespaceAfterDelimiter() {
        return this.addWhitespaceAfterDelimiter;
    }

    /**
     * If set to <code>true</code>, this properties object will add a
     * timestamp to the beginning of the file, just after the header
     * (if any) is printed. Else, this will not add a timestamp.
     * Default value: <code>false</code>. Note that {@link
     * Properties#store} always writes a timestamp.
     */
    public void setInsertTimestamp(boolean insertTimestamp) {
        this.insertTimestamp = insertTimestamp;
    }

    /**
     * If set to <code>true</code>, this properties object will add a
     * timestamp to the beginning of the file, just after the header
     * (if any) is printed. Else, this will not add a timestamp.
     * Default value: <code>false</code>. Note that {@link
     * Properties#store} always writes a timestamp.
     */
    public boolean getInsertTimestamp() {
        return this.insertTimestamp;
    }

    /**
     * If set to <code>true</code>, duplicate properties are allowed, and
     * the last property setting in the input will overwrite any previous
     * settings. If set to <code>false</code>, duplicate property definitions
     * in the input will cause an exception to be thrown during {@link #load}.
     * Default value: <code>false</code>. Note that {@link
     * Properties#store} always allows duplicates.
     */
    public void setAllowDuplicates(boolean allowDuplicates) {
        this.allowDuplicates = allowDuplicates;
    }

    /**
     * If set to <code>true</code>, duplicate properties are allowed, and
     * the last property setting in the input will overwrite any previous
     * settings. If set to <code>false</code>, duplicate property definitions
     * in the input will cause an exception to be thrown during {@link #load}.
     * Default value: <code>false</code>. Note that {@link
     * Properties#store} always allows duplicates.
     */
    public boolean getAllowDuplicates() {
        return this.allowDuplicates;
    }

    public String getProperty(String key) {
        return super.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return super.getProperty(key, defaultValue);
    }

    public Object setProperty(String key, String value) {
        return put(key, value);
    }

    /**
     * Circumvents the superclass {@link #putAll} implementation,
     * putting all the key-value pairs via {@link #put}.
     */
    public synchronized void putAll(Map m) {
        Map.Entry e;
        for (Iterator iter = m.entrySet().iterator(); iter.hasNext();) {
            e = (Map.Entry) iter.next();
            put(e.getKey(), e.getValue());
        }
    }

    /**
     * Removes the key from the bookkeeping collectiotns as well.
     */
    public synchronized Object remove(Object key) {
        newKeys.remove(key);
        return super.remove(key);
    }

    public synchronized void clear() {
        super.clear();

        if (source != null)
            source.clear();

        newKeys.clear();
        modifiedKeys.clear();
    }

    public synchronized Object clone() {
        FormatPreservingProperties c = (FormatPreservingProperties)
            super.clone();

        if (source != null)
            c.source = (PropertySource) source.clone();

        if (modifiedKeys != null)
            c.modifiedKeys = (HashSet) modifiedKeys.clone();

        if (newKeys != null) {
            c.newKeys = new LinkedHashSet();
            c.newKeys.addAll(newKeys);
        }

        return c;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        isNotDeserializing = true;
    }

    public synchronized Object put(Object key, Object val) {
        Object o = super.put(key, val);

        // if we're no longer loading from properties and this put
        // represents an actual change in value, mark the modification
        // or addition in the bookkeeping collections.
        if (!isLoading && isNotDeserializing && !val.equals(o)) {
            if (o != null)
                modifiedKeys.add(key);
            else if (!newKeys.contains(key))
                newKeys.add(key);
        }
        return o;
    }

    /**
     * Loads the properties in <code>in</code>, according to the rules
     * described in {@link Properties#load}. If {@link #getAllowDuplicates}
     * returns <code>true</code>, this will throw a {@link
     * DuplicateKeyException} if duplicate property declarations are
     * encountered.
     *
     * @see Properties#load
     */
    public synchronized void load(InputStream in) throws IOException {
        isLoading = true;
        try {
            loadProperties(in);
        } finally {
            isLoading = false;
        }
    }

    private void loadProperties(InputStream in) throws IOException {
        source = new PropertySource();

        PropertyLineReader reader = new PropertyLineReader(in, source);

        Set loadedKeys = new HashSet();

        for (PropertyLine l;
            (l = reader.readPropertyLine()) != null && source.add(l);) {
            String line = l.line.toString();

            char c = 0;
            int pos = 0;

            while (pos < line.length() && isSpace(c = line.charAt(pos)))
                pos++;

            if ((line.length() - pos) == 0
                || line.charAt(pos) == '#' || line.charAt(pos) == '!')
                continue;

            StringBuilder key = new StringBuilder();
            while (pos < line.length() && !isSpace(c = line.charAt(pos++))
                && c != '=' && c != ':') {
                if (c == '\\') {
                    if (pos == line.length()) {
                        l.append(line = reader.readLine());
                        pos = 0;
                        while (pos < line.length()
                            && isSpace(c = line.charAt(pos)))
                            pos++;
                    } else {
                        pos = readEscape(line, pos, key);
                    }
                } else {
                    key.append(c);
                }
            }

            boolean isDelim = (c == ':' || c == '=');

            for (; pos < line.length()
                && isSpace(c = line.charAt(pos)); pos++)
                ;

            if (!isDelim && (c == ':' || c == '=')) {
                pos++;
                while (pos < line.length() && isSpace(c = line.charAt(pos)))
                    pos++;
            }

            StringBuilder element = new StringBuilder(line.length() - pos);

            while (pos < line.length()) {
                c = line.charAt(pos++);
                if (c == '\\') {
                    if (pos == line.length()) {
                        l.append(line = reader.readLine());

                        if (line == null)
                            break;

                        pos = 0;
                        while (pos < line.length()
                            && isSpace(c = line.charAt(pos)))
                            pos++;
                        element.ensureCapacity(line.length() - pos +
                            element.length());
                    } else {
                        pos = readEscape(line, pos, element);
                    }
                } else
                    element.append(c);
            }

            if (!loadedKeys.add(key.toString()) && !allowDuplicates)
                throw new DuplicateKeyException(key.toString(),
                    getProperty(key.toString()), element.toString());

            l.setPropertyKey(key.toString());
            l.setPropertyValue(element.toString());
            put(key.toString(), element.toString());
        }
    }

    /**
     * Read the next escaped character: handle newlines, tabs, returns, and
     * form feeds with the appropriate escaped character, then try to
     * decode unicode characters. Finally, just add the character explicitly.
     *
     * @param source the source of the characters
     * @param pos the position at which to start reading
     * @param value the value we are appending to
     * @return the position after the reading is done
     */
    private static int readEscape(String source, int pos, StringBuilder value) {
        char c = source.charAt(pos++);
        switch (c) {
            case 'n':
                value.append('\n');
                break;
            case 't':
                value.append('\t');
                break;
            case 'f':
                value.append('\f');
                break;
            case 'r':
                value.append('\r');
                break;
            case 'u':
                if (pos + 4 <= source.length()) {
                    char uni = (char) Integer.parseInt
                        (source.substring(pos, pos + 4), 16);
                    value.append(uni);
                    pos += 4;
                }
                break;
            default:
                value.append(c);
                break;
        }

        return pos;
    }

    private static boolean isSpace(char ch) {
        return Character.isWhitespace(ch);
    }

    public void save(OutputStream out, String header) {
        try {
            store(out, header);
        } catch (IOException ex) {
        }
    }

    public void store(OutputStream out, String header) throws IOException {
        boolean endWithNewline = source != null && source.endsInNewline;

        // Must be ISO-8859-1 ecoding according to Properties.load javadoc
        PrintWriter writer = new PrintWriter
            (new OutputStreamWriter(out, "ISO-8859-1"), false);

        if (header != null)
            writer.println("#" + header);

        if (insertTimestamp)
            writer.println("#" + Calendar.getInstance().getTime());

        List lines = new LinkedList();
        // first write all the existing props as they were initially read
        if (source != null)
            lines.addAll(source);

        // next write out new keys, then the rest of the keys
        LinkedHashSet keys = new LinkedHashSet();
        keys.addAll(newKeys);
        keys.addAll(keySet());

        lines.addAll(keys);

        keys.remove(null);

        boolean needsNewline = false;

        for (Iterator i = lines.iterator(); i.hasNext();) {
            Object next = i.next();

            if (next instanceof PropertyLine) {
                if (((PropertyLine) next).write(writer, keys, needsNewline))
                    needsNewline = i.hasNext();
            } else if (next instanceof String) {
                String key = (String) next;
                if (keys.remove(key)) {
                    if (writeProperty(key, writer, needsNewline)) {
                        needsNewline = i.hasNext() && keys.size() > 0;

                        // any new or modified properties will cause
                        // the file to end with a newline
                        endWithNewline = true;
                    }
                }
            }
        }

        // make sure we end in a newline if the source ended in it
        if (endWithNewline)
            writer.println();

        writer.flush();
    }

    private boolean writeProperty(String key, PrintWriter writer,
        boolean needsNewline) {
        StringBuilder s = new StringBuilder();

        if (key == null)
            return false;

        String val = getProperty(key);
        if (val == null)
            return false;

        formatValue(key, s, true);
        s.append(defaultEntryDelimiter);
        if (addWhitespaceAfterDelimiter)
            s.append(' ');
        formatValue(val, s, false);

        if (needsNewline)
            writer.println();

        writer.print(s);

        return true;
    }

    /**
     * Format the given string as an encoded value for storage. This will
     * perform any necessary escaping of special characters.
     *
     * @param str the value to encode
     * @param buf the buffer to which to append the encoded value
     * @param isKey if true, then the string is a Property key, otherwise
     * it is a value
     */
    private static void formatValue(String str, StringBuilder buf,
        boolean isKey) {
        if (isKey) {
            buf.setLength(0);
            buf.ensureCapacity(str.length());
        } else {
            buf.ensureCapacity(buf.length() + str.length());
        }

        boolean escapeSpace = true;
        int size = str.length();

        for (int i = 0; i < size; i++) {
            char c = str.charAt(i);

            if (c == '\n')
                buf.append("\\n");
            else if (c == '\r')
                buf.append("\\r");
            else if (c == '\t')
                buf.append("\\t");
            else if (c == '\f')
                buf.append("\\f");
            else if (c == ' ')
                buf.append(escapeSpace ? "\\ " : " ");
            else if (c == '\\' || c == '!' || c == '#' || c == '=' || c == ':')
                buf.append('\\').append(c);
            else if (c < ' ' || c > '~')
                buf.append("\\u0000".substring(0, 6 - Integer.toHexString(c).
                    length())).append(Integer.toHexString(c));
            else
                buf.append(c);

            if (c != ' ')
                escapeSpace = isKey;
        }
    }

    public static class DuplicateKeyException extends RuntimeException {

        public DuplicateKeyException(String key, Object firstVal,
            String secondVal) {
            super(_loc.get("dup-key", key, firstVal, secondVal).getMessage());
        }
    }

    /**
     * Contains the original line of the properties file: can be a
     * proper key/value pair, or a comment, or just whitespace.
     */
    private class PropertyLine implements Serializable {

        private final StringBuilder line = new StringBuilder();
        private String propertyKey;
        private String propertyValue;

        public PropertyLine(String line) {
            this.line.append(line);
        }

        public void append(String newline) {
            line.append(J2DoPrivHelper.getLineSeparator());
            line.append(newline);
        }

        public void setPropertyKey(String propertyKey) {
            this.propertyKey = propertyKey;
        }

        public String getPropertyKey() {
            return this.propertyKey;
        }

        public void setPropertyValue(String propertyValue) {
            this.propertyValue = propertyValue;
        }

        public String getPropertyValue() {
            return this.propertyValue;
        }

        /**
         * Write the given line. It will only be written if the line is a
         * comment, or if it is a property and its value is unchanged
         * from the original.
         *
         * @param pw the PrintWriter to which the write
         * @return whether or not this was a known key
         */
        public boolean write(PrintWriter pw, Collection keys,
            boolean needsNewline) {
            // no property? It may be a comment or just whitespace
            if (propertyKey == null) {
                if (needsNewline)
                    pw.println();
                pw.print(line.toString());
                return true;
            }

            // check to see if we are the same value we initially read:
            // if so, then just write it back exactly as it was read
            if (propertyValue != null && containsKey(propertyKey) &&
                (propertyValue.equals(getProperty(propertyKey)) ||
                    (!newKeys.contains(propertyKey) &&
                        !modifiedKeys.contains(propertyKey)))) {
                if (needsNewline)
                    pw.println();
                pw.print(line.toString());

                keys.remove(propertyKey);

                return true;
            }

            // if we have modified or added the specified key, then write
            // it back to the same location in the file from which it
            // was originally read, so that it will be in the proximity
            // to the comment
            if (containsKey(propertyKey) &&
                (modifiedKeys.contains(propertyKey) ||
                    newKeys.contains(propertyKey))) {
                while (keys.remove(propertyKey)) ;
                return writeProperty(propertyKey, pw, needsNewline);
            }

            // this is a new or changed property: don't do anything
            return false;
        }
    }

    private class PropertyLineReader extends BufferedReader {

        public PropertyLineReader(InputStream in, PropertySource source)
            throws IOException {
            // Must be ISO-8859-1 ecoding according to Properties.load javadoc
            super(new InputStreamReader(new LineEndingStream(in, source),
                "ISO-8859-1"));
        }

        public PropertyLine readPropertyLine() throws IOException {
            String l = readLine();
            if (l == null)
                return null;

            PropertyLine pl = new PropertyLine(l);
            return pl;
        }
    }

    /**
     * Simple FilterInputStream that merely remembers if the last
     * character that it read was a newline or not.
     */
    private static class LineEndingStream extends FilterInputStream {

        private final PropertySource source;

        LineEndingStream(InputStream in, PropertySource source) {
            super(in);

            this.source = source;
        }

        public int read() throws IOException {
            int c = super.read();
            source.endsInNewline = (c == '\n' || c == '\r');
            return c;
        }

        public int read(byte[] b, int off, int len) throws IOException {
            int n = super.read(b, off, len);
            if (n > 0)
                source.endsInNewline =
                    (b[n + off - 1] == '\n' || b[n + off - 1] == '\r');
            return n;
        }
    }

    static class PropertySource extends LinkedList
        implements Cloneable, Serializable {

        private boolean endsInNewline = false;
    }
}
