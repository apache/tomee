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
package openbook.tools.converter;

import java.util.HashSet;
import java.util.Set;

import openbook.tools.parser.JavaParser;
import openbook.tools.util.TextProcessingUtility;

import org.antlr.runtime.Token;

/**
 * Renders Java source tokens as HTML tags.
 * This renderer renders the parsed Java tokens with HTML styles.
 * The styles of the tokens are determined by their types such as Java keywords, identifiers,
 * comments etc. Moreover, an identifier can be a <em>custom</em> type if it matched a given
 * list of identifiers. The actual HTML text is enclosed in &lt;span id="style"&gt;token&lt;\span&gt;
 * to apply the style. The styles are defined in a Cascaded Style Sheet (CSS). 
 * The cascaded style sheet by default is named <code>java.css</code>.
 * <br>
 * The rendering takes care of line breaks and white space in the following way to work
 * around some limitations of Swing based HTML editor kit's usage of CSS attributes.
 * <LI> Line Breaks: Swing Editor seems to require an explicit carriage return-line feed
 * character to render in separate line. While a normal browser works with &lt;br&gt;
 * tag alone. 
 * <LI> White space: The CSS property <code>white-space</code> is sufficient for browsers
 * to preserve white space within &lt;span&gt; tags. But Swing Editor seems to require 
 * explicit <code>nbsp;</code> for white spaces within &lt;span&gt; tags.
 * <br>
 * Two boolean properties are provided to control these two properties. 
 * <br>
 * <LI>Line Numbering: A boolean property controls whether line numbers will be printed.
 * Line numbers are printed in 4 digits with leading zeros, by default.
 * <LI>Line Number Anchoring: An anchor can be specified at every line. The anchor
 * is <code>line.nnn</code> where <code>nnn</code> is the actual line number without
 * any leading zero.
 * <LI> JavaDoc comment : The JavaDoc comments can use characters that if reproduced
 * exactly in HTML output can confuse the rendering process. On the other hand, the
 * JavaDoc tags that define an anchor in the source code or creates a hyperlink should
 * be preserved in the HTML output. The capacity and limitation of processing HTML tages
 * inside JavaDoc comments are described in {@linkplain TextProcessingUtility here}.
 * 
 * 
 * @author Pinaki Poddar
 *
 */
public class HTMLTokenRenderer implements TokenRenderer {
    private String stylesheet        = "java.css";
    private boolean showLineNumber   = true;
    private boolean anchorLineNumber = false;
    private boolean addLineBreak     = true;
    private boolean addExplicitSpace = true;
    private String lineNumberFormat  = "%04d";
    
    /**
     * The CSS named styles.
     */
    public static final String CSS_CUSTOM      = "custom";
    public static final String CSS_KEYWORD     = "keyword";
    public static final String CSS_ANNOTATION  = "annotation";
    public static final String CSS_ENUM        = "enum";
    public static final String CSS_COMMENT     = "comment";
    public static final String CSS_LITERAL     = "literal";
    public static final String CSS_DECIMAL     = "decimal";
    public static final String CSS_LINE_NO     = "lineno";
    
    private Set<String> customIdentifiers = new HashSet<String>();

    public static final String NEW_LINE = "\r\n";
    public static final String HTML_BR_TAG = "<br>";
    public static final String HTML_SPACE = "&nbsp;";
    
    /**
     * Gets a end-of-line string: a HTML &lt;br&gt; tag followed by carriage return and line feed.
     */
    public String endLine(int line) {
        return  addLineBreak ?  HTML_BR_TAG + NEW_LINE : NEW_LINE;
    }

    /**
     * Gets a string for beginning of a new line.  
     */
    public String newLine(int line) {
        String result = "";
        if (showLineNumber) {
            result = span(CSS_LINE_NO, String.format(lineNumberFormat, line) + fillWhiteSpace("    "));
        }
        if (anchorLineNumber) {
            result = "<A name=" + quote("line."+line) + ">" + result + "</A>";
        }
        return result;
    }

    @Override
    public String render(int decision, Token token) {
        String text = token.getText();
        String result = "";
        int type = token.getType();
        switch (type) {
        case JavaParser.Identifier:
        case 73: // annotation symbol @
            if (customIdentifiers.contains(text)) {
                result = span(CSS_CUSTOM, text);
            } else if (decision == 66 || decision == 14) {
                result = span(CSS_ANNOTATION, text);
            } else if (decision == 28) {
                result = span(CSS_ENUM, text);
            } else {
                result = text;
            }
            break;
        case JavaParser.COMMENT:
        case JavaParser.LINE_COMMENT :
            result = span(CSS_COMMENT, TextProcessingUtility.replaceHTMLSpecialCharacters(text));
            break;
        case JavaParser.StringLiteral :
        case JavaParser.CharacterLiteral:
            result = span(CSS_LITERAL, text);
            break;
        case JavaParser.DecimalLiteral:
        case JavaParser.HexDigit:
        case JavaParser.HexLiteral:
            result = span(CSS_DECIMAL, text);
            break;
        default:
            if (isWhiteSpace(text)) {
                result = fillWhiteSpace(text);
            } else if (text.length() == 1) {
                if (text.charAt(0) == '>') {
                    result = "&gt;";
                } else if (text.charAt(0) == '<') {
                    result = "&lt;";
                } else {
                    result = text;
                }
            } else {
                result = span(CSS_KEYWORD, text);
            }
        }
        return result;
    }
    
    String span(String id, String txt) {
        return "<span id=" + quote(id) + ">" + txt + "</span>";
    }
    
    String quote(String s) {
        return "\""+s+"\"";
    }
    
    boolean isWhiteSpace(String txt) {
        for (int i = 0; i < txt.length(); i++) {
            if (!Character.isWhitespace(txt.charAt(i)))
               return false;
        }
        return true;
    }
    
    String fillWhiteSpace(String txt) {
        StringBuilder space = new StringBuilder();
        for (int i = 0; i < txt.length(); i++) {
            char ch = txt.charAt(i);
            if (ch != '\r' && ch != '\n')
                space.append(addExplicitSpace ? HTML_SPACE : ch);
        }
        return space.toString();
    }
    

    /**
     * Gets the opening &lt;BODY&gt; and &lt;HTML&gt; tags and the &lt;link type="stylesheet"&gt; clause.
     */
    public String getPrologue() {
        return insertLines(
                "<HTML>", 
                "<HEAD>", 
                "<link rel="+ quote("stylesheet")+ " type=" + quote("text/css") + " href=" + quote(stylesheet) + ">",
                "</HEAD>",
                "<BODY>");
    }
    
    /**
     * Gets the closing &lt;BODY&gt; and &lt;HTML&gt; tags
     */
    public String getEpilogue() {
        return insertLines(" ", "</BODY>", "</HTML>");
    }

    private String insertLines(String...lines) {
        StringBuilder buf = new StringBuilder();
        for (String line : lines) {
            if (buf.length() != 0) buf.append(NEW_LINE);
            buf.append(line);
        }
        return buf.toString();
    }

    // Bean Style setters for auto-configuration
    
    /**
     * Gets the stylesheet to be linked at HTML output. 
     */
    public String getStylesheet() {
        return stylesheet;
    }

    /**
     * Sets the stylesheet to be linked at the HTML output.
     */
    public void setStylesheet(String stylesheet) {
        this.stylesheet = stylesheet;
    }

    /**
     * Affirms if a line number will be added in HTML output.
     */
    public boolean getShowLineNumber() {
        return showLineNumber;
    }

    /**
     * Sets if a line number will be added in HTML output.
     */
    public void setShowLineNumber(boolean showLineNumber) {
        this.showLineNumber = showLineNumber;
    }

    /**
     * Affirms if an anchor will be created on every line.
     */
    public boolean getAnchorLineNumber() {
        return anchorLineNumber;
    }

    /**
     * Sets if an anchor will be created on every line.
     */
    public void setAnchorLineNumber(boolean anchorLineNumber) {
        this.anchorLineNumber = anchorLineNumber;
    }

    /**
     * Affirms if explicit line break (carriage return and line feed) will be added  
     * at the HTML output.
     * 
     * @see #endLine(int)
     */
    public boolean getAddLineBreak() {
        return addLineBreak;
    }

    /**
     * Sets if explicit line break (carriage return and line feed) will be added  
     * at the HTML output.
     * 
     * @see #endLine(int)
     */
    public void setAddLineBreak(boolean addLineBreak) {
        this.addLineBreak = addLineBreak;
    }

    /**
     * Affirms if explicit <code>&nbsp;</code> will be added at the HTML output.
     */
    public boolean getAddExplicitSpace() {
        return addExplicitSpace;
    }

    /**
     * Sets if explicit <code>&nbsp;</code> will be added at the HTML output.
     */
    public void setAddExplicitSpace(boolean addSpace) {
        this.addExplicitSpace = addSpace;
    }

    /**
     * Gets the format string to format line number such as <code>"%%0%4d"</code>
     * for a 4-digit number with leading zeros.
     */
    public String getLineNumberFormat() {
        return lineNumberFormat;
    }

    /**
     * Sets the format string to format line number such as <code>"%%0%4d"</code>
     * for a 4-digit number with leading zeros.     
     */
    public void setLineNumberFormat(String lineNumberFormat) {
        this.lineNumberFormat = lineNumberFormat;
    }   
}
