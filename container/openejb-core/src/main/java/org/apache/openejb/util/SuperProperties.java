/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.InvalidPropertiesFormatException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Properties is a Hashtable where the keys and values must be Strings. Each Properties can have a default
 * Properties which specifies the default values which are used if the key is not in this Properties.
 *
 * @see Hashtable
 * @see java.lang.System#getProperties
 */
public class SuperProperties extends Properties {

    private static final String PROP_DTD_NAME = "http://java.sun.com/dtd/properties.dtd";

    private static final String PROP_DTD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "    <!ELEMENT properties (comment?, entry*) >"
            + "    <!ATTLIST properties version CDATA #FIXED \"1.0\" >"
            + "    <!ELEMENT comment (#PCDATA) >"
            + "    <!ELEMENT entry (#PCDATA) >"
            + "    <!ATTLIST entry key CDATA #REQUIRED >";



    /**
     * Actual property values.
     */
    protected LinkedHashMap<Object,Object> properties = new LinkedHashMap<Object,Object>();

    /**
     * Comments for the properties.
     */
    protected LinkedHashMap<String,String> comments = new LinkedHashMap<String,String>();

    /**
     * Attributes for the properties.
     */
    protected LinkedHashMap<String,LinkedHashMap<String,String>> attributes = new LinkedHashMap<String,LinkedHashMap<String,String>>();

    /**
     * The default property values.
     */
    protected Properties defaults;

    /**
     * Are lookups case insensitive?
     */
    protected boolean caseInsensitive;

    /**
     * The text between a key and the value.
     */
    protected String keyValueSeparator = "=";

    /**
     * The line separator to use when storing.  Defaults to system line separator.
     */
    protected String lineSeparator = System.getProperty("line.separator");

    /**
     * Number of spaces to indent each line of the properties file.
     */
    protected String indent = "";

    /**
     * Number of spaces to indent comment after '#' character.
     */
    protected String commentIndent = " ";

    /**
     * Should there be a blank line between properties.
     */
    protected boolean spaceBetweenProperties = true;

    /**
     * Should there be a blank line between a comment and the property.
     */
    protected boolean spaceAfterComment = false;

    /**
     * Used for loadFromXML.
     */
    private DocumentBuilder builder = null;

    /**
     * Constructs a new Properties object.
     */
    public SuperProperties() {
        super();
    }

    /**
     * Constructs a new Properties object using the specified default properties.
     *
     * @param properties the default properties
     */
    public SuperProperties(Properties properties) {
        super(properties);
        defaults = properties;
    }

    /**
     * Are lookups case insensitive?
     * @return true if lookups are insensitive
     */
    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    /**
     * Sets the sensitive of lookups.
     * @param caseInsensitive if looks are insensitive
     */
    public void setCaseInsensitive(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    /**
     * Gets the text that separates keys and values.
     * The default is "=".
     * @return the text that separates keys and values
     */
    public String getKeyValueSeparator() {
        return keyValueSeparator;
    }

    /**
     * Sets the text that separates keys and values.
     * @param keyValueSeparator the text that separates keys and values
     */
    public void setKeyValueSeparator(String keyValueSeparator) {
        if (keyValueSeparator == null) throw new NullPointerException("keyValueSeparator is null");
        if (keyValueSeparator.length() == 0) throw new NullPointerException("keyValueSeparator is empty");
        this.keyValueSeparator = keyValueSeparator;
    }

    /**
     * Gets the text that separates lines while storing.
     * The default is the system line.separator.
     * @return the text that separates keys and values
     */
    public String getLineSeparator() {
        return lineSeparator;
    }

    /**
     * Sets the text that separates lines while storing
     * @param lineSeparator the text that separates lines
     */
    public void setLineSeparator(String lineSeparator) {
        if (lineSeparator == null) throw new NullPointerException("lineSeparator is null");
        if (lineSeparator.length() == 0) throw new NullPointerException("lineSeparator is empty");
        this.lineSeparator = lineSeparator;
    }

    /**
     * Gets the number of spaces to indent each line of the properties file.
     * @return the number of spaces to indent each line of the properties file
     */
    public int getIndent() {
        return indent.length();
    }

    /**
     * Sets the number of spaces to indent each line of the properties file.
     * @param  indent the number of spaces to indent each line of the properties file
     */
    public void setIndent(int indent) {
        char[] chars = new char[indent];
        Arrays.fill(chars, ' ');
        this.indent = new String(chars);
    }

    /**
     * Gets the number of spaces to indent comment after '#' character.
     * @return the number of spaces to indent comment after '#' character
     */
    public int getCommentIndent() {
        return commentIndent.length();
    }

    /**
     * Sets the number of spaces to indent comment after '#' character.
     * @param commentIndent the number of spaces to indent comment after '#' character
     */
    public void setCommentIndent(int commentIndent) {
        char[] chars = new char[commentIndent];
        Arrays.fill(chars, ' ');
        this.commentIndent = new String(chars);
    }

    /**
     * Should a blank line be added between properties?
     * @return true if a blank line should be added between properties; false otherwise
     */
    public boolean isSpaceBetweenProperties() {
        return spaceBetweenProperties;
    }

    /**
     * If true a blank line will be added between properties.
     * @param spaceBetweenProperties if true a blank line will be added between properties
     */
    public void setSpaceBetweenProperties(boolean spaceBetweenProperties) {
        this.spaceBetweenProperties = spaceBetweenProperties;
    }

    /**
     * Should there be a blank line between a comment and the property?
     * @return true if a blank line should be added between a comment and the property
     */
    public boolean isSpaceAfterComment() {
        return spaceAfterComment;
    }

    /**
     * If true a blank line will be added between a comment and the property.
     * @param spaceAfterComment if true a blank line will be added between a comment and the property
     */
    public void setSpaceAfterComment(boolean spaceAfterComment) {
        this.spaceAfterComment = spaceAfterComment;
    }

    public String getProperty(String name) {
        Object result = get(name);
        String property = result instanceof String ? (String) result : null;
        if (property == null && defaults != null) {
            property = defaults.getProperty(name);
        }
        return property;
    }

    public String getProperty(String name, String defaultValue) {
        Object result = get(name);
        String property = result instanceof String ? (String) result : null;
        if (property == null && defaults != null) {
            property = defaults.getProperty(name);
        }
        if (property == null) {
            return defaultValue;
        }
        return property;
    }

    public synchronized Object setProperty(String name, String value) {
        return put(name, value);
    }

    /**
     * Searches for the comment associated with the specified property. If the property is not found, look
     * in the default properties. If the property is not found in the default properties, answer null.
     *
     * @param name the name of the property to find
     * @return the named property value
     */
    public String getComment(String name) {
        name = normalize(name);
        String comment = comments.get(name);
        if (comment == null && defaults instanceof SuperProperties) {
            comment = ((SuperProperties) defaults).getComment(name);
        }
        return comment;
    }

    /**
     * Sets the comment associated with a property.
     * @param name the property name; not null
     * @param comment the comment; not null
     */
    public void setComment(String name, String comment) {
        if (name == null) throw new NullPointerException("name is null");
        if (comment == null) throw new NullPointerException("comment is null");

        name = normalize(name);
        comments.put(name, comment);
    }

    /**
     * Searches for the attributes associated with the specified property. If the property is not found, look
     * in the default properties. If the property is not found in the default properties, answer null.
     *
     * @param name the name of the property to find
     * @return the attributes for an existing property (not null); null for non-existant properties
     */
    public Map<String,String> getAttributes(String name) {
        if (name == null) throw new NullPointerException("name is null");

        name = normalize(name);
        Map<String, String> attributes = this.attributes.get(name);
        if (attributes == null && defaults instanceof SuperProperties) {
            attributes = ((SuperProperties) defaults).getAttributes(name);
        }
        return attributes;
    }

    public void list(PrintStream out) {
        if (out == null) {
            throw new NullPointerException();
        }
        StringBuffer buffer = new StringBuffer(80);
        Enumeration<?> keys = propertyNames();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            buffer.append(key);
            buffer.append('=');
            String property = (String) get(key);
            if (property == null) {
                property = defaults.getProperty(key);
            }
            if (property.length() > 40) {
                buffer.append(property.substring(0, 37));
                buffer.append("...");
            } else {
                buffer.append(property);
            }
            out.println(buffer.toString());
            buffer.setLength(0);
        }
    }

    public void list(PrintWriter writer) {
        if (writer == null) {
            throw new NullPointerException();
        }
        StringBuffer buffer = new StringBuffer(80);
        Enumeration<?> keys = propertyNames();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            buffer.append(key);
            buffer.append('=');
            String property = (String) get(key);
            while (property == null) {
                property = defaults.getProperty(key);
            }
            if (property.length() > 40) {
                buffer.append(property.substring(0, 37));
                buffer.append("...");
            } else {
                buffer.append(property);
            }
            writer.println(buffer.toString());
            buffer.setLength(0);
        }
    }

    public synchronized void load(InputStream in) throws IOException {
        // never null, when empty we are processing the white space at the beginning of the line
        StringBuilder key = new StringBuilder();
        // null when processing key
        StringBuilder value = null;
        // never null, contains the comment for the property or nothing if no comment
        StringBuilder comment = new StringBuilder();
        // never null, contains attributes for a property
        LinkedHashMap<String,String> attributes = new LinkedHashMap<String,String>();

        int indent = 0;
        boolean globalIndentSet = false;

        int commentIndent = -1;
        boolean globalCommentIndentSet = false;

        // true when processing the separator between a key and value
        boolean inSeparator = false;

        while (true) {
            int nextByte = decodeNextCharacter(in);
            if (nextByte == EOF) break;
            char nextChar = (char) (nextByte & 0xff);

            switch (nextByte) {
                case ' ':
                case '\t':
                    //
                    // End of key if parsing key
                    //
                    // if parsing the key, this is the end of the key
                    if (key.length() > 0 && value == null) {
                        inSeparator = true;
                        value = new StringBuilder();
                        continue;
                    }
                    break;
                case ':':
                case '=':
                    //
                    // End of key
                    //
                    if (inSeparator) {
                        inSeparator = false;
                        continue;
                    }
                    if (value == null) {
                        value = new StringBuilder();
                        continue;
                    }
                    break;
                case LINE_ENDING:
                    //
                    // End of Line
                    //
                    if (key.length() > 0) {
                        // add property
                        put(key.toString(), value == null ? "" : value.toString());
                        // add comment
                        if (comment.length() > 0) {
                            setComment(key.toString(), comment.toString());
                            comment = new StringBuilder();
                        }
                        // add attributes
                        this.attributes.put(normalize(key.toString()), attributes);
                        attributes = new LinkedHashMap<String,String>();
                        // set line indent
                        if (!globalIndentSet) {
                            setIndent(indent);
                            globalIndentSet = true;
                        }
                        indent = 0;
                    }
                    key = new StringBuilder();
                    value = null;
                    continue;
                case '#':
                case '!':
                    //
                    // Comment
                    //
                    if (key.length() == 0) {
                        // set global line indent
                        if (!globalIndentSet) {
                            setIndent(indent);
                            globalIndentSet = true;
                        }
                        indent = 0;

                        // read comment Line
                        StringBuilder commentLine = new StringBuilder();
                        int commentLineIndent = 0;
                        boolean inIndent = true;
                        while (true) {
                            nextByte = in.read();
                            if (nextByte < 0) break;
                            nextChar = (char) nextByte; // & 0xff

                            if (inIndent && nextChar == ' ') {
                                commentLineIndent++;
                                commentLine.append(' ');
                            } else if (inIndent && nextChar == '\t') {
                                commentLineIndent += 4;
                                commentLine.append("    ");
                            } else if (nextChar == '\r' || nextChar == '\n') {
                                break;
                            } else {
                                inIndent = false;
                                commentLine.append(nextChar);
                            }
                        }

                        // Determine indent
                        if (comment.length() == 0) {
                            // if this is a new comment block, the comment indent size for this
                            // block is based the first line of the comment
                            commentIndent = commentLineIndent;
                            if (!globalCommentIndentSet) {
                                setCommentIndent(commentIndent);
                                globalCommentIndentSet = true;
                            }
                        }
                        commentLineIndent = Math.min(commentIndent, commentLineIndent);

                        if (commentLine.toString().trim().startsWith("@")) {
                            // process property attribute
                            String attribute = commentLine.toString().trim().substring(1);
                            String[] parts = attribute.split("=", 2);
                            String attributeName = parts[0].trim();
                            String attributeValue = parts.length == 2 ? parts[1].trim() : "";
                            attributes.put(attributeName, attributeValue);
                        } else {
                            // append comment
                            if (comment.length() != 0) {
                                comment.append(lineSeparator);
                            }
                            comment.append(commentLine.toString().substring(commentLineIndent));
                        }
                        continue;
                    }
                    break;
            }

            if (Character.isWhitespace(nextChar)) {
                // count leading white space
                if (key.length() == 0) {
                    if (nextChar == '\t') {
                        indent += 4;
                    } else {
                        indent++;
                    }
                }

                // if key length == 0 or value length == 0
                if (key.length() == 0 || value == null || value.length() == 0) {
                    continue;
                }
            }

            inSeparator = false;
            if (value == null) {
                key.append(nextChar);
            } else {
                value.append(nextChar);
            }
        }

        // if buffer has data, there is a property we still need toadd
        if (key.length() > 0) {
            // add property
            put(key.toString(), value == null ? "" : value.toString());
            // add comment
            if (comment.length() > 0) {
                setComment(key.toString(), comment.toString());
            }
            // add attributes
            this.attributes.put(normalize(key.toString()), attributes);
            // set line indent
            if (!globalIndentSet) {
                setIndent(indent);
            }
        }
    }

    private static final int EOF = -1;
    private static final int LINE_ENDING = -4200;

    private int decodeNextCharacter(InputStream in) throws IOException {
        boolean lineContinuation = false;
        boolean carriageReturnLineContinuation  = false;
        while (true) {
            // read character
            int nextByte = in.read();
            if (nextByte < 0) return EOF;
            char nextChar = (char) (nextByte & 0xff);

            // if line continuation character was '\r', we need to ignore an optional '\n'
            // immediately following the \r
            if (carriageReturnLineContinuation) {
                carriageReturnLineContinuation = false;
                if (nextChar == '\n') {
                    continue;
                }
            }

            // If escape sequence \x or line continuation, decode it
            if (nextChar == '\\') {
                // next character is the escaped character
                nextByte = in.read();
                if (nextByte < 0) {
                    // line continuation to end of stream
                    // sun vm returns 0 character for this case
                    nextChar = '\u0000';
                } else {
                    nextChar = (char) (nextByte & 0xff);
                }

                switch (nextChar) {
                    case '\r':
                        // line continuation using '\r', which optionally can have a following '\n'
                        carriageReturnLineContinuation = true;
                        // fall through
                    case '\n':
                        // line continuation
                        lineContinuation = true;
                        continue;
                    case 'u':
                        nextChar = readUnicode(in);
                        break;
                    default:
                        nextChar = decodeEscapeChar(nextChar);
                        break;
                }
            } else {
                // if line ending character, we return the special value LINE_ENDING so
                // caller can differentiate between an encoded "\n" sequence and a real
                // line ending character in the file
                if (nextChar == '\n' || nextChar == '\r') {
                    return LINE_ENDING;
                }
            }

            // in a line continuation we ignore spaces and tabs until the first real character
            if (lineContinuation && (nextChar == ' ' || nextChar == '\t')) {
                continue;
            }

            return nextChar;
        }
    }

    private char decodeEscapeChar(char nextChar) {
        switch (nextChar) {
            case 'b':
                return '\b';
            case 'f':
                return'\f';
            case 'n':
                return '\n';
            case 'r':
                return '\r';
            case 't':
                return '\t';
            case 'u':
                throw new IllegalArgumentException("decodeEscapeChar can not decode an unicode sequence");
            default:
                return nextChar;
        }
    }

    private char readUnicode(InputStream in) throws IOException {
        char[] buf = new char[4];
        int unicode = 0;
        for (int i = 0; i < buf.length; i++) {
            int nextByte = in.read();

            // we must get exactally 4 bytes
            if (nextByte < 0) {
                throw new IllegalArgumentException("Invalid unicode sequence: expected format \\uxxxx, but got \\u" + new String(buf, 0, i));
            }

            // convert to character
            char nextChar = (char) (nextByte & 0xff);
            buf[i] = nextChar;

            // convert to digit
            int nextDigit = Character.digit(nextChar, 16);

            // all bytes must be valid hex digits
            if (nextDigit < 0) {
                throw new IllegalArgumentException("Illegal character " + nextChar + " in unicode sequence \\u" + new String(buf, 0, i + 1));
            }


            unicode = (unicode << 4) + nextDigit;
        }

        return (char) unicode;
    }

    public Enumeration<?> propertyNames() {
        if (defaults == null) {
            return keys();
        }

        Hashtable<Object, Object> set = new Hashtable<Object, Object>(defaults.size() + size());
        Enumeration<?> keys = defaults.propertyNames();
        while (keys.hasMoreElements()) {
            set.put(keys.nextElement(), set);
        }
        keys = keys();
        while (keys.hasMoreElements()) {
            set.put(keys.nextElement(), set);
        }
        return set.keys();
    }

    @SuppressWarnings({"deprecation"})
    public void save(OutputStream out, String comment) {
        try {
            store(out, comment);
        } catch (IOException e) {
        }
    }

    public synchronized void store(OutputStream out, String headComment) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(out, "ISO8859_1");
        if (headComment != null) {
            writer.write(indent);
            writer.write("#");
            writer.write(commentIndent);
            writer.write(headComment);
            writer.write(lineSeparator);
        }

        boolean firstProperty = true;
        StringBuilder buffer = new StringBuilder(200);
        for (Map.Entry<Object, Object> entry : entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            if (!firstProperty && spaceBetweenProperties) {
                buffer.append(lineSeparator);
            }

            String comment = comments.get(key);
            Map<String, String> attributes = this.attributes.get(key);
            if (comment != null || !attributes.isEmpty()) {
                dumpComment(buffer, comment, attributes, "#");
                if (spaceAfterComment) {
                    buffer.append(lineSeparator);
                }
            }

            // ${indent}${key}=${value}
            buffer.append(indent);
            dumpString(buffer, key, true);
            if (value != null && value.length() > 0) {
                buffer.append(keyValueSeparator);
                dumpString(buffer, value, false);
            }
            buffer.append(lineSeparator);

            writer.write(buffer.toString());
            buffer.setLength(0);

            firstProperty = false;
        }
        writer.flush();
    }

    private void dumpString(StringBuilder buffer, String string, boolean key) {
        int i = 0;
        if (!key && i < string.length() && string.charAt(i) == ' ') {
            buffer.append("\\ ");
            i++;
        }

        for (; i < string.length(); i++) {
            char ch = string.charAt(i);
            switch (ch) {
                case '\t':
                    buffer.append("\\t");
                    break;
                case '\n':
                    buffer.append("\\n");
                    break;
                case '\f':
                    buffer.append("\\f");
                    break;
                case '\r':
                    buffer.append("\\r");
                    break;
                default:
                    if ("\\#!=:".indexOf(ch) >= 0 || (key && ch == ' '))
                    {
                        buffer.append('\\');
                    }
                    if (ch >= ' ' && ch <= '~') {
                        buffer.append(ch);
                    } else {
                        String hex = Integer.toHexString(ch);
                        buffer.append("\\u");
                        for (int j = 0; j < 4 - hex.length(); j++) {
                            buffer.append("0");
                        }
                        buffer.append(hex);
                    }
            }
        }
    }

    private void dumpComment(StringBuilder buffer, String comment, Map<String, String> attributes, String commentToken) {
        if (comment != null) {
            boolean startOfLine = true;

            char ch = 0;
            for (int i = 0; i < comment.length(); i++) {
                ch = comment.charAt(i);

                if (startOfLine) {
                    buffer.append(indent);
                    buffer.append(commentToken);
                    buffer.append(commentIndent);
                    startOfLine = false;
                }

                switch (ch) {
                    case '\r':
                        // if next character is not \n, this is the line break
                        if (i+1 < comment.length() && comment.charAt(i+1) != '\n') {
                            buffer.append(lineSeparator);
                            startOfLine = true;
                        }
                        break;
                    case '\n':
                        buffer.append(lineSeparator);
                        startOfLine = true;
                        break;
                    default:
                        buffer.append(ch);
                }
            }

            // if the last character written was not a line break, write one now
            if (ch != '\r' && ch != '\n'){
                buffer.append(lineSeparator);
            }
        }

        // ${indent}#${commentIndent}@${attributeName}=${attributeValue}
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            buffer.append(indent);
            buffer.append("#");
            buffer.append(commentIndent);
            buffer.append("@");
            buffer.append(entry.getKey());
            if (entry.getValue() != null && entry.getValue().length() > 0) {
                buffer.append("=");
                buffer.append(entry.getValue());
            }
            buffer.append(lineSeparator);
        }
    }

    public synchronized void loadFromXML(InputStream in) throws IOException {
        if (in == null) {
            throw new NullPointerException();
        }

        DocumentBuilder builder = getDocumentBuilder();

        try {
            Document doc = builder.parse(in);
            NodeList entries = doc.getElementsByTagName("entry");
            if (entries == null) {
                return;
            }

            int entriesListLength = entries.getLength();
            for (int i = 0; i < entriesListLength; i++) {
                Element entry = (Element) entries.item(i);
                String key = entry.getAttribute("key");
                String value = entry.getTextContent();
                put(key, value);

                // search backwards for a comment
                for (Node node = entry.getPreviousSibling(); node != null && !(node instanceof Element); node = node.getPreviousSibling()) {
                    if (node instanceof Comment) {
                        InputStream cin = new ByteArrayInputStream(((Comment) node).getData().getBytes());

                        // read comment line by line
                        StringBuilder comment = new StringBuilder();
                        LinkedHashMap<String,String> attributes = new LinkedHashMap<String,String>();

                        int nextByte;
                        char nextChar;
                        boolean firstLine = true;
                        int commentIndent = Integer.MAX_VALUE;
                        do {
                            // read one line
                            StringBuilder commentLine = new StringBuilder();
                            int commentLineIndent = 0;
                            boolean inIndent = true;
                            while (true) {
                                nextByte = cin.read();
                                if (nextByte < 0) break;
                                nextChar = (char) nextByte; // & 0xff
                                if (inIndent && nextChar == ' ') {
                                    commentLineIndent++;
                                    commentLine.append(' ');
                                } else if (inIndent && nextChar == '\t') {
                                    commentLineIndent += 4;
                                    commentLine.append("    ");
                                } else if (nextChar == '\r' || nextChar == '\n') {
                                    break;
                                } else {
                                    inIndent = false;
                                    commentLine.append(nextChar);
                                }
                            }

                            // Determine indent
                            if (!firstLine && commentIndent == Integer.MAX_VALUE && commentLine.length() > 0) {
                                // if this is a new comment block, the comment indent size for this
                                // block is based the first full line of the comment (ignoring the
                                // line with the <!--
                                commentIndent = commentLineIndent;
                            }
                            commentLineIndent = Math.min(commentIndent, commentLineIndent);

                            if (commentLine.toString().trim().startsWith("@")) {
                                // process property attribute
                                String attribute = commentLine.toString().trim().substring(1);
                                String[] parts = attribute.split("=", 2);
                                String attributeName = parts[0].trim();
                                String attributeValue = parts.length == 2 ? parts[1].trim() : "";
                                attributes.put(attributeName, attributeValue);
                            } else {
                                // append comment
                                if (comment.length() != 0) {
                                    comment.append(lineSeparator);
                                }
                                comment.append(commentLine.toString().substring(commentLineIndent));
                            }

                            firstLine = false;
                        } while (nextByte > 0);
                        
                        if (comment.length() > 0) {
                            setComment(key, comment.toString());
                        }
                        this.attributes.put(normalize(key), attributes);

                        break;
                    }
                }

            }
        } catch (SAXException e) {
            throw new InvalidPropertiesFormatException(e);
        }
    }

    private DocumentBuilder getDocumentBuilder() {
        if (builder == null) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(true);

            try {
                builder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                throw new Error(e);
            }

            builder.setErrorHandler(new ErrorHandler() {
                public void warning(SAXParseException e) throws SAXException {
                    throw e;
                }

                public void error(SAXParseException e) throws SAXException {
                    throw e;
                }

                public void fatalError(SAXParseException e) throws SAXException {
                    throw e;
                }
            });

            builder.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity(String publicId,
                        String systemId) throws SAXException, IOException {
                    if (systemId.equals(PROP_DTD_NAME)) {
                        InputSource result = new InputSource(new StringReader(PROP_DTD));
                        result.setSystemId(PROP_DTD_NAME);
                        return result;
                    }
                    throw new SAXException("Invalid DOCTYPE declaration: " + systemId);
                }
            });
        }
        return builder;
    }

    public void storeToXML(OutputStream os, String comment) throws IOException {
        storeToXML(os, comment, "UTF-8");
    }

    public synchronized void storeToXML(OutputStream os, String headComment, String encoding) throws IOException {
        if (os == null || encoding == null) {
            throw new NullPointerException();
        }

        // for somereason utf-8 is always used
        String encodingCanonicalName = "UTF-8";

        // header
        OutputStreamWriter osw = new OutputStreamWriter(os, encodingCanonicalName);
        StringBuilder buf = new StringBuilder(200);
        buf.append("<?xml version=\"1.0\" encoding=\"").append(encodingCanonicalName).append("\"?>").append(lineSeparator);
        buf.append("<!DOCTYPE properties SYSTEM \"" + PROP_DTD_NAME + "\">").append(lineSeparator);
        buf.append("<properties>").append(lineSeparator);

        // comment
        if (headComment != null) {
            buf.append(indent);
            buf.append("<comment>");
            buf.append(substitutePredefinedEntries(headComment));
            buf.append("</comment>");
            buf.append(lineSeparator);

            if (!isEmpty() && (spaceBetweenProperties || spaceAfterComment)) {
                buf.append(lineSeparator);
            }
        }

        // properties
        boolean firstProperty = true;
        for (Map.Entry<Object, Object> entry : entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            if (!firstProperty && spaceBetweenProperties) {
                buf.append(lineSeparator);
            }

            // property comment
            String comment = comments.get(key);
            Map<String, String> attributes = this.attributes.get(key);
            if (comment != null || !attributes.isEmpty()) {
                buf.append(indent);
                buf.append("<!--");
                buf.append(lineSeparator);

                // comments can't contain "--" so we shrink all sequences of them to a single "-"
                comment = comment.replaceAll("--*", "-");
                dumpComment(buf, comment, attributes, "");

                buf.append(indent);
                buf.append("-->");
                buf.append(lineSeparator);

                if (spaceAfterComment) {
                    buf.append(lineSeparator);
                }
            }


            // property
            buf.append(indent);
            buf.append("<entry key=\"");
            buf.append(substitutePredefinedEntries(key));
            buf.append("\">");
            buf.append(substitutePredefinedEntries(value));
            buf.append("</entry>");
            buf.append(lineSeparator);

            firstProperty = false;
        }


        buf.append("</properties>").append(lineSeparator);

        osw.write(buf.toString());
        osw.flush();
    }

    private String substitutePredefinedEntries(String s) {
        /*
        * substitution for predefined character entities
        * to use them safely in XML
        */
        return s.replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll("\u0027", "&apos;")
            .replaceAll("\"", "&quot;");
    }

    //
    // Delegate all remaining methods to the properties object
    //

    public boolean isEmpty() {
        return properties.isEmpty();
    }

    public int size() {
        return properties.size();
    }

    public Object get(Object key) {
        key = normalize(key);
        return properties.get(key);
    }

    public Object put(Object key, Object value) {
        key = normalize(key);
        if (key instanceof String) {
            String name = (String) key;
            if (!attributes.containsKey(name)) {
                attributes.put(name, new LinkedHashMap<String,String>());
            }
        }
        return properties.put(key, value);
    }

    public Object remove(Object key) {
        key = normalize(key);
        comments.remove(key);
        attributes.remove(key);
        return properties.remove(key);
    }

    public void putAll(Map<?, ?> t) {
        for (Map.Entry<?, ?> entry : t.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
        if (t instanceof SuperProperties) {
            SuperProperties superProperties = (SuperProperties) t;
            for (Map.Entry<String, String> entry : superProperties.comments.entrySet()) {
                comments.put(normalize(entry.getKey()), entry.getValue());
            }
            for (Map.Entry<String, LinkedHashMap<String, String>> entry : superProperties.attributes.entrySet()) {
                attributes.put(normalize(entry.getKey()), entry.getValue());
            }
        }
    }

    /**
     * Returns an unmodifiable view of the keys.
     * @return an unmodifiable view of the keys
     */
    public Set<Object> keySet() {
        return Collections.unmodifiableSet(properties.keySet());
    }

    public Enumeration<Object> keys() {
        return Collections.enumeration(properties.keySet());
    }

    /**
     * Returns an unmodifiable view of the values.
     * @return an unmodifiable view of the values
     */
    public Collection<Object> values() {
        return Collections.unmodifiableCollection(properties.values());
    }

    /**
     * Returns an unmodifiable view of the entries.
     * @return an unmodifiable view of the entries
     */
    public Set<Map.Entry<Object, Object>> entrySet() {
        return Collections.unmodifiableSet(properties.entrySet());
    }

    public Enumeration<Object> elements() {
        return Collections.enumeration(properties.values());
    }

    public boolean containsKey(Object key) {
        key = normalize(key);
        return properties.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return properties.containsValue(value);
    }

    public boolean contains(Object value) {
        return properties.containsValue(value);
    }

    public void clear() {
        properties.clear();
        comments.clear();
        attributes.clear();
    }

    @SuppressWarnings({"unchecked"})
    public Object clone() {
        SuperProperties clone = (SuperProperties) super.clone();
        clone.properties = (LinkedHashMap<Object,Object>) properties.clone();
        clone.comments = (LinkedHashMap<String,String>) comments.clone();
        clone.attributes = (LinkedHashMap<String,LinkedHashMap<String,String>>) attributes.clone();
        for (Map.Entry<String, LinkedHashMap<String, String>> entry : clone.attributes.entrySet()) {
            entry.setValue((LinkedHashMap<String, String>) entry.getValue().clone());
        }
        return clone;
    }

    @SuppressWarnings({"EqualsWhichDoesntCheckParameterClass"})
    public boolean equals(Object o) {
        return properties.equals(o);
    }

    public int hashCode() {
        return properties.hashCode();
    }

    public String toString() {
        return properties.toString();
    }

    protected void rehash() {
    }

    private Object normalize(Object key){
        if (key instanceof String) {
            return normalize((String)key);
        }
        return key;
    }

    private String normalize(String property){
        if (!caseInsensitive) {
            return property;
        }

        if (super.containsKey(property)){
            return property;
        }

        for (Object o : keySet()) {
            if (o instanceof String) {
                String key = (String) o;
                if (key.equalsIgnoreCase(property)) return key;
            }
        }

        if (defaults != null) {
            for (Object o : defaults.keySet()) {
                if (o instanceof String) {
                    String key = (String) o;
                    if (key.equalsIgnoreCase(property)) return key;
                }
            }
        }

        return property;
    }
}
