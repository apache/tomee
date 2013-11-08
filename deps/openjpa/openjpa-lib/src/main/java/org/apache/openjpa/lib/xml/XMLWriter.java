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

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.openjpa.lib.util.J2DoPrivHelper;

/**
 * The XMLWriter is a writer type for pretty-printing XML.
 * It assumes that the streamed XML will be given without any whitespace,
 * other than the space within text blocks.
 *
 * @author Abe White
 * @nojavadoc
 */
public class XMLWriter extends FilterWriter {

    private static String _endl = J2DoPrivHelper.getLineSeparator();

    private int _lastChar = ' ';
    private int _lastChar2 = ' ';
    private int _lastChar3 = ' ';
    private int _depth = 0;

    /**
     * Construct an XMLWriter that will write to the given stream.
     */
    public XMLWriter(Writer out) {
        super(out);
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
        for (int i = 0; i < len; i++)
            write(cbuf[off + i]);
    }

    public void write(String str, int off, int len) throws IOException {
        for (int i = 0; i < len; i++)
            write(str.charAt(off + i));
    }

    public void write(int c) throws IOException {
        // the basic idea of this method is to make sure that when a tag
        // or a text block starts, it is placed on a separate line and
        // indented an amount appropriate to the XML tree depth

        if (_lastChar == '<') {
            // tag or processing instruction?
            if (c != '?' && c != '!') {
                // end tag; decrease depth before writing spaces
                if (c == '/')
                    _depth--;

                // tags are always on separate lines
                out.write(_endl);
                writeSpaces();

                // beginning tag; increase depth for tag body
                if (c != '/')
                    _depth++;
            }

            // if this is not a processing instruction / comment,
            // write the chars
            if (c != '!') {
                out.write('<');
                out.write(c);
            }
        } else if (c == '>') {
            // if unary tag decrease depth to undo the increase at tag start
            if (_lastChar == '/')
                _depth--;

            // check for the comment-processing conditions
            if (_lastChar2 == '<' && _lastChar == '!')
                out.write("<!");
            else if (_lastChar3 == '<' && _lastChar2 == '!' && _lastChar == '-')
                out.write("<!-");

            out.write('>');
        } else if (c != '<') {
            // if we're at "<!--", indent and put in the beginning of
            // the comment. if it's "<!-?" where ? is something other
            // than -, dump what we've gotten so far
            if (_lastChar3 == '<' && _lastChar2 == '!' && _lastChar == '-') {
                if (c == '-') {
                    out.write(_endl);
                    writeSpaces();
                    out.write("<!--");
                } else {
                    out.write("<!-");
                    out.write(c);
                }
            }
            // if we're at "<!-", keep on not writing data
            else if (!(_lastChar2 == '<' && _lastChar == '!' && c == '-')) {
                // if just ended a tag and about to print text, put on
                // separate line
                if (_lastChar == '>' && _lastChar2 != '?' && _lastChar2 != '!')
                {
                    out.write(_endl);
                    writeSpaces();
                }
                out.write(c);
            }
        }

        _lastChar3 = _lastChar2;
        _lastChar2 = _lastChar;
        _lastChar = c;
    }

    private void writeSpaces() throws IOException {
        for (int i = 0; i < _depth; i++)
            out.write("    ");
    }
}
