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
package org.apache.openejb.assembler.spring.xmlwriter;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @version $Revision$ $Date$
 */
public class PrettyPrintXmlWriter implements XmlWriter {
    private static final String LS = System.getProperty("line.separator");

    private PrintWriter writer;

    private LinkedList elementStack = new LinkedList();

    private boolean tagInProgress;

    private int depth;

    private String lineIndenter;

    private String encoding;

    private String docType;

    private boolean readyForNewLine;

    private boolean tagIsEmpty;

    public PrettyPrintXmlWriter(PrintWriter writer, String lineIndenter) {
        this(writer, lineIndenter, null, null);
    }

    public PrettyPrintXmlWriter(Writer writer, String lineIndenter) {
        this(new PrintWriter(writer), lineIndenter);
    }

    public PrettyPrintXmlWriter(PrintWriter writer) {
        this(writer, null, null);
    }

    public PrettyPrintXmlWriter(Writer writer) {
        this(new PrintWriter(writer));
    }

    public PrettyPrintXmlWriter(PrintWriter writer, String lineIndenter, String encoding, String doctype) {
        setWriter(writer);

        setLineIndenter(lineIndenter);

        setEncoding(encoding);

        setDocType(doctype);

        if (doctype != null || encoding != null) {
            writeDocumentHeaders();
        }
    }

    public PrettyPrintXmlWriter(Writer writer, String lineIndenter, String encoding, String doctype) {
        this(new PrintWriter(writer), lineIndenter, encoding, doctype);
    }

    public PrettyPrintXmlWriter(PrintWriter writer, String encoding, String doctype) {
        this(writer, "  ", encoding, doctype);
    }

    public PrettyPrintXmlWriter(Writer writer, String encoding, String doctype) {
        this(new PrintWriter(writer), encoding, doctype);
    }

    public void startElement(String name) {
        tagIsEmpty = false;

        finishTag();

        write("<");

        write(name);

        elementStack.addLast(name);

        tagInProgress = true;

        setDepth(getDepth() + 1);

        readyForNewLine = true;

        tagIsEmpty = true;
    }

    public void writeText(String text) {
        writeText(text, true);
    }

    public void writeMarkup(String text) {
        writeText(text, false);
    }

    private void writeText(String text, boolean escapeXml) {
        readyForNewLine = false;

        tagIsEmpty = false;

        finishTag();

        if (escapeXml) {
            text = escapeXml(text);
        }

        write(text);
    }

    private static String escapeXml(String text) {
        text = text.replaceAll("&", "&amp;");

        text = text.replaceAll("<", "&lt;");

        text = text.replaceAll(">", "&gt;");

        text = text.replaceAll("\"", "&quot;");

        text = text.replaceAll("\'", "&apos;");

        return text;
    }

    private static String escapeXmlAttribute(String text) {
        text = escapeXml(text);

        text = text.replaceAll("\n\r", "&#10;");

        Pattern pattern = Pattern.compile("([\000-\037])");
        Matcher m = pattern.matcher(text);
        StringBuffer b = new StringBuffer();
        while (m.find()) {
            m = m.appendReplacement(b, "&#" + Integer.toString(m.group(1).charAt(0)) + ";");
        }
        m.appendTail(b);

        return b.toString();
    }

    public void addAttribute(String key, String value) {
        write(" ");

        write(key);

        write("=\"");

        write(escapeXmlAttribute(value));

        write("\"");
    }

    public void endElement() {
        setDepth(getDepth() - 1);

        if (tagIsEmpty) {
            write("/");

            readyForNewLine = false;

            finishTag();

            elementStack.removeLast();
        } else {
            finishTag();

            write("</" + elementStack.removeLast() + ">");
        }

        readyForNewLine = true;
    }

    /**
     * Write a string to the underlying writer
     */
    private void write(String str) {
        getWriter().write(str);
    }

    private void finishTag() {
        if (tagInProgress) {
            write(">");
        }

        tagInProgress = false;

        if (readyForNewLine) {
            endOfLine();
        }
        readyForNewLine = false;

        tagIsEmpty = false;
    }

    /**
     * Get the string used as line indenter
     *
     * @return the line indenter
     */
    protected String getLineIndenter() {
        return lineIndenter;
    }

    /**
     * Set the string used as line indenter
     */
    protected void setLineIndenter(String lineIndenter) {
        this.lineIndenter = lineIndenter;
    }

    /**
     * Write the end of line character (using system line separator)
     * and start new line with indentation
     */
    protected void endOfLine() {
        write(LS);

        for (int i = 0; i < getDepth(); i++) {
            write(getLineIndenter());
        }
    }

    private void writeDocumentHeaders() {
        write("<?xml version=\"1.0\"");

        if (getEncoding() != null) {
            write(" encoding=\"" + getEncoding() + "\"");
        }

        write("?>");

        endOfLine();

        if (getDocType() != null) {
            write("<!DOCTYPE ");

            write(getDocType());

            write(">");

            endOfLine();
        }
    }

    /**
     * Set the underlying writer
     */
    protected void setWriter(PrintWriter writer) {
        this.writer = writer;
    }

    /**
     * Get the underlying writer
     *
     * @return the underlying writer
     */
    protected PrintWriter getWriter() {
        return writer;
    }

    /**
     * Set the current depth in the xml indentation
     */
    protected void setDepth(int depth) {
        this.depth = depth;
    }

    /**
     * Get the current depth in the xml indentation
     */
    protected int getDepth() {
        return depth;
    }

    protected void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    protected String getEncoding() {
        return encoding;
    }

    protected void setDocType(String docType) {
        this.docType = docType;
    }

    protected String getDocType() {
        return docType;
    }

}
