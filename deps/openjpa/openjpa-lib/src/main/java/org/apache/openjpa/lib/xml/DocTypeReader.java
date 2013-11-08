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
package org.apache.openjpa.lib.xml;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * The DocTypeReader can be used to dynamically include a
 * <code>DOCTYPE</code> declaration in an XML stream. Often it is
 * inconvenient to specify a <code>DOCTYPE</code> in XML files -- you many
 * want the option of parsing the files without reading the DTD, the files
 * may move around, making placing a <code>DOCTYPE</code> path to the DTD in
 * them unattractive, and you may have many files, making an in-line include
 * of the DTD unattractive as well. This class makes
 * it possible to maintain XML files without any <code>DOCTYPE</code>
 * declaration, then dynamically include the <code>DOCTYPE</code> information
 * at runtime.
 * If the XML stream already contains a <code>DOCTYPE</code> declaration,
 * the reader will not add an additional one.
 * The <code>DOCTYPE</code> information given to the reader will be placed
 * in the XML stream it wraps just before the root element of the document.
 * Note that all methods other than the various forms of <code>read</code>
 * apply onto the underlying XML stream and should not be used until the
 * header and doc type have been read.
 *
 * @author Abe White
 * @nojavadoc
 */
public class DocTypeReader extends Reader {

    private Reader _xml = null;
    private Reader _docType = null;

    // use to hold all header information until the doctype dec should be
    // inserted
    private char[] _header = null;
    private int _headerPos = 0;

    /**
     * Construct the reader with an XML stream, and set the
     * <code>DOCTYPE</code> information to be included. The given
     * reader should access an input source containing the exact declaration
     * to include, such as:<br />
     * <code>&lt;DOCTYPE schedule SYSTEM "schedule.dtd"&gt;</code><br />
     * <code>&lt;DOCTYPE html PUBLIC "-//W3C//DTD XHTML ...&gt;</code><br />
     * <code>&lt;DOCTYPE stock-price [ &lt;ELEMENT symb ... ]&gt;</code><br />
     * If the reader is null, no <code>DOCTYPE</code> information will be
     * included in the stream.
     */
    public DocTypeReader(Reader xml, Reader docType) throws IOException {
        _docType = docType;
        _xml = bufferHeader(xml);
    }

    public int read() throws IOException {
        int ch = readHeader();
        if (ch != -1)
            return ch;

        ch = readDocType();
        if (ch != -1)
            return ch;

        return _xml.read();
    }

    public int read(char[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    public int read(char[] buf, int off, int len) throws IOException {
        int headerRead = readHeader(buf, off, len);
        off += headerRead;
        len -= headerRead;

        int docRead = readDocType(buf, off, len);
        off += docRead;
        len -= docRead;

        return headerRead + docRead + _xml.read(buf, off, len);
    }

    public long skip(long len) throws IOException {
        return _xml.skip(len);
    }

    public boolean ready() throws IOException {
        return _xml.ready();
    }

    public boolean markSupported() {
        return _xml.markSupported();
    }

    public void mark(int readAheadLimit) throws IOException {
        _xml.mark(readAheadLimit);
    }

    public void reset() throws IOException {
        _xml.reset();
    }

    public void close() throws IOException {
        _xml.close();
        if (_docType != null)
            _docType.close();
    }

    /**
     * Buffer all text until the doc type declaration should be inserted.
     */
    private Reader bufferHeader(Reader origXML) throws IOException {
        // don't bother if no doc type declaration
        if (_docType == null) {
            _header = new char[0];
            return origXML;
        }

        // create buffer
        StringWriter writer = new StringWriter();
        PushbackReader xml = new PushbackReader(origXML, 3);
        int ch, ch2, ch3;
        boolean comment;

        while (true) {
            // read leading space
            for (ch = xml.read(); ch != -1
                && Character.isWhitespace((char) ch); ch = xml.read())
                writer.write(ch);
            if (ch == -1)
                return headerOnly(writer.toString());

            // if not XML, finish
            if (ch != '<') {
                xml.unread(ch);
                _header = writer.toString().toCharArray();
                return xml;
            }

            // if the root element, finish
            ch = xml.read();
            if (ch != '?' && ch != '!') {
                xml.unread(ch);
                xml.unread('<');
                _header = writer.toString().toCharArray();
                return xml;
            }

            // if a doc type element, finish
            ch2 = xml.read();
            if (ch == '!' && ch2 == 'D') {
                xml.unread(ch2);
                xml.unread(ch);
                xml.unread('<');
                _header = writer.toString().toCharArray();
                _docType = null; // make sure doc type not included
                return xml;
            }

            // is this a comment?
            ch3 = xml.read();
            comment = ch == '!' && ch2 == '-' && ch3 == '-';

            // place everything read into the header material
            writer.write('<');
            writer.write(ch);
            writer.write(ch2);
            writer.write(ch3);

            // read until the next '>' or '-->' if a comment
            ch2 = 0;
            ch3 = 0;
            while ((ch = xml.read()) != -1) {
                writer.write(ch);

                if ((!comment && ch == '>')
                    || (comment && ch == '>' && ch2 == '-' && ch3 == '-'))
                    break;

                // track last two chars so we can tell if comment is ending
                ch3 = ch2;
                ch2 = ch;
            }
            if (ch == -1)
                return headerOnly(writer.toString());

            // read the space after the declaration
            for (ch = xml.read(); ch != -1
                && Character.isWhitespace((char) ch); ch = xml.read())
                writer.write(ch);
            if (ch == -1)
                return headerOnly(writer.toString());
            xml.unread(ch);
        }
    }

    /**
     * If the stream contained only space, think of it as pure XML with no
     * header for consistency with the other methods.
     */
    private Reader headerOnly(String header) {
        _header = new char[0];
        _docType = null;
        return new StringReader(header);
    }

    /**
     * Return a single character from the buffered header, or -1 if none.
     */
    private int readHeader() {
        if (_headerPos == _header.length)
            return -1;
        return _header[_headerPos++];
    }

    /**
     * Read from the buffered header to the given array, returning the
     * number of characters read.
     */
    private int readHeader(char[] buf, int off, int len) {
        int read = 0;
        for (; len > 0 && _headerPos < _header.length; read++, off++, len--)
            buf[off] = _header[_headerPos++];

        return read;
    }

    /**
     * Return a single character from the doc type declaration, or -1 if none.
     */
    private int readDocType() throws IOException {
        if (_docType == null)
            return -1;

        int ch = _docType.read();
        if (ch == -1)
            _docType = null;

        return ch;
    }

    /**
     * Read from the doc type declaration to the given array, returning the
     * number of characters read.
     */
    private int readDocType(char[] buf, int off, int len) throws IOException {
        if (_docType == null)
            return 0;

        int read = _docType.read(buf, off, len);
        if (read < len)
            _docType = null;
        if (read == -1)
            read = 0;

        return read;
    }
}
