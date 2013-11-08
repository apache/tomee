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
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import javax.xml.parsers.SAXParser;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Tests the {@link DocTypeReader} by comparing the results of passing
 * various xml streams to the expected result with included doc type info.
 *
 * @author Abe White
 */
public class TestDocTypeReader extends TestCase {

    private String _docType = null;
    private String _validXML = null;
    private String _invalidXML = null;
    private String _expectedXML = null;
    private String _validHTML = null;
    private String _expectedHTML = null;
    private String _xmlWithDocType = null;

    public TestDocTypeReader(String test) {
        super(test);
    }

    public void setUp() {
        StringBuffer docType = new StringBuffer();
        docType.append("<!DOCTYPE foo [\n");
        docType.append("\t<!ELEMENT foo (bar)>\n");
        docType.append("\t<!ELEMENT bar EMPTY>\n");
        docType.append("\t<!ATTLIST bar attr CDATA \"value\">\n");
        docType.append("\t<!ATTLIST bar attr2 CDATA \"value2\">\n");
        docType.append("]>\n");
        _docType = docType.toString();

        StringBuffer expectedXML = new StringBuffer();
        String header = "<?xml version=\"1.0\"?>\n";
        String comment = "<!-- some < ... > <! funky -> - comment -->\n";
        expectedXML.append(header);
        expectedXML.append(comment);
        expectedXML.append(docType.toString());

        StringBuffer xmlWithDocType = new StringBuffer();
        xmlWithDocType.append(header);
        xmlWithDocType.append(comment);
        xmlWithDocType.append(docType.toString());

        StringBuffer validXML = new StringBuffer();
        validXML.append("<foo>\n");
        validXML.append("\t<bar attr=\"newValue\"/>\n");
        validXML.append("</foo>");
        expectedXML.append(validXML.toString());
        xmlWithDocType.append(validXML.toString());
        _validXML = header + comment + validXML.toString();
        _expectedXML = expectedXML.toString();
        _xmlWithDocType = xmlWithDocType.toString();

        StringBuffer invalidXML = new StringBuffer();
        invalidXML.append("<?xml version=\"1.0\"?>\n");
        invalidXML.append("<foo>\n");
        invalidXML.append("\t<xxx />\n");
        invalidXML.append("</foo>");
        _invalidXML = invalidXML.toString();

        StringBuffer expectedHTML = new StringBuffer();
        header = "   \n  ";
        expectedHTML.append(header);
        expectedHTML.append(docType.toString());
        StringBuffer validHTML = new StringBuffer();
        validHTML.append("some junk <html><body></body></html>  ");
        expectedHTML.append(validHTML.toString());
        _validHTML = header + validHTML.toString();
        _expectedHTML = expectedHTML.toString();
    }

    /**
     * Test against expected doc type inclusion behavior.
     */
    public void testIncludesDocType() throws IOException {
        assertEquals(_validXML, getIncludedString(_validXML, null, 1));
        assertEquals(_validXML, getIncludedString(_validXML, null, 7));
        assertEquals(_validHTML, getIncludedString(_validHTML, null, 1));
        assertEquals(_validHTML, getIncludedString(_validHTML, null, 7));

        assertEquals(_expectedXML, getIncludedString(_validXML, _docType, 1));
        assertEquals(_expectedXML, getIncludedString(_validXML, _docType, 7));
        assertEquals(_expectedHTML, getIncludedString
            (_validHTML, _docType, 1));
        assertEquals(_expectedHTML, getIncludedString
            (_validHTML, _docType, 7));

        assertEquals("   ", getIncludedString("   ", _docType, 1));
        assertEquals("   ", getIncludedString("   ", _docType, 7));
    }

    /**
     * Test that the doc type declaration is not included in files with
     * doc types.
     */
    public void testStreamWithDocType() throws IOException {
        assertEquals(_xmlWithDocType, getIncludedString
            (_xmlWithDocType, null, 1));
        assertEquals(_xmlWithDocType, getIncludedString
            (_xmlWithDocType, null, 7));
        assertEquals(_xmlWithDocType, getIncludedString
            (_xmlWithDocType, _docType, 1));
        assertEquals(_xmlWithDocType, getIncludedString
            (_xmlWithDocType, _docType, 7));
    }

    /**
     * Return the result of including the given doc type declaration within
     * the given XML by reading <code>bufSize</code> chars at a time.
     */
    private String getIncludedString(String xml, String docType, int bufSize)
        throws IOException {
        StringReader xmlReader = new StringReader(xml);
        StringReader docReader = null;
        if (docType != null)
            docReader = new StringReader(docType);

        Writer writer = new StringWriter();
        Reader reader = new DocTypeReader(xmlReader, docReader);
        if (bufSize == 1)
            for (int ch = reader.read(); ch != -1; ch = reader.read())
                writer.write((int) ch);
        else {
            char[] ch;
            int read;
            while (true) {
                ch = new char[bufSize];
                read = reader.read(ch);
                if (read == -1)
                    break;
                writer.write(ch, 0, read);
            }
        }

        return writer.toString();
    }

    /**
     * Test that validation occurs correctly.
     */
    public void testValidation() throws IOException, SAXException {
        SAXParser parser = XMLFactory.getSAXParser(false, false);
        InputSource source = new InputSource();

        source.setCharacterStream(new StringReader(_validXML));
        parser.parse(source, new Handler());
        source.setCharacterStream(new StringReader(_invalidXML));
        parser.parse(source, new Handler());

        parser = XMLFactory.getSAXParser(true, false);
        source.setCharacterStream(new DocTypeReader(new StringReader
            (_validXML), new StringReader(_docType)));
        parser.parse(source, new Handler());

        try {
            source.setCharacterStream(new DocTypeReader(new StringReader
                (_invalidXML), new StringReader(_docType)));
            parser.parse(source, new Handler());
            fail("Parsed invalid document");
        } catch (SAXException se) {
        }
    }

    public static Test suite() {
        return new TestSuite(TestDocTypeReader.class);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    private static class Handler extends DefaultHandler {

        public void error(SAXParseException spe) throws SAXException {
            throw spe;
        }

        public void fatalError(SAXParseException spe) throws SAXException {
            throw spe;
        }
    }
}
