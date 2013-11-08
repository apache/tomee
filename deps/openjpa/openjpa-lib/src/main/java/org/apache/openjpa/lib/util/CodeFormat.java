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

/**
 * Encapsulates some common Java source code formatting options. The
 * class can also be used as a buffer for formatted Java code.
 *
 * @author Abe White
 */
public final class CodeFormat implements Cloneable {

    private static final String _sep = J2DoPrivHelper.getLineSeparator();

    private String _tab = "\t";
    private boolean _spaceBeforeParen = false;
    private boolean _spaceInParen = false;
    private boolean _braceOnSameLine = true;
    private boolean _braceAtSameTabLevel = false;
    private boolean _scoreBeforeFieldName = false;
    private int _linesBetweenSections = 1;

    private StringBuffer _buf = new StringBuffer();

    /**
     * The number of spaces to use for tabs; 0 means to use actual tab
     * characters. Defaults to 0.
     */
    public int getTabSpaces() {
        return (_tab.equals("\t")) ? 0 : _tab.length();
    }

    /**
     * The number of spaces to use for tabs; 0 means to use actual tab
     * characters. Defaults to 0.
     */
    public void setTabSpaces(int tab) {
        if (tab == 0)
            _tab = "\t";
        else {
            StringBuilder tabs = new StringBuilder(tab);
            for (int i = 0; i < tab; i++)
                tabs.append(" ");
            _tab = tabs.toString();
        }
    }

    /**
     * Whether to place a space before parentheses. Defaults to false.
     */
    public boolean getSpaceBeforeParen() {
        return _spaceBeforeParen;
    }

    /**
     * Whether to place a space before parentheses. Defaults to false.
     */
    public void setSpaceBeforeParen(boolean spaceBeforeParen) {
        _spaceBeforeParen = spaceBeforeParen;
    }

    /**
     * Whether to place a space within parentheses. Defaults to false.
     */
    public boolean getSpaceInParen() {
        return _spaceInParen;
    }

    /**
     * Whether to place a space within parentheses. Defaults to false.
     */
    public void setSpaceInParen(boolean spaceInParen) {
        _spaceInParen = spaceInParen;
    }

    /**
     * Whether to place opening braces on the same line as the
     * block declaration, or on the next line. Defaults to same line.
     */
    public boolean getBraceOnSameLine() {
        return _braceOnSameLine;
    }

    /**
     * Whether to place opening braces on the same line as the
     * block declaration, or on the next line. Defaults to same line.
     */
    public void setBraceOnSameLine(boolean braceOnSameLine) {
        _braceOnSameLine = braceOnSameLine;
    }

    /**
     * Whether to place braces at the same tab level as the code within
     * the block. Defaults to false.
     */
    public boolean getBraceAtSameTabLevel() {
        return _braceAtSameTabLevel;
    }

    /**
     * Whether to place braces at the same tab level as the code within
     * the block. Defaults to false.
     */
    public void setBraceAtSameTabLevel(boolean braceAtSameTabLevel) {
        _braceAtSameTabLevel = braceAtSameTabLevel;
    }

    /**
     * Whether to place an underscore before private field names. Defaults
     * to false.
     */
    public boolean getScoreBeforeFieldName() {
        return _scoreBeforeFieldName;
    }

    /**
     * Whether to place an underscore before private field names. Defaults
     * to false.
     */
    public void setScoreBeforeFieldName(boolean scoreBeforeFieldName) {
        _scoreBeforeFieldName = scoreBeforeFieldName;
    }

    /**
     * The number of empty lines between code sections. Defaults to 1.
     */
    public int getLinesBetweenSections() {
        return _linesBetweenSections;
    }

    /**
     * The number of empty lines between sections. Defaults to 1.
     */
    public void setLinesBetweenSections(int linesBetweenSections) {
        _linesBetweenSections = linesBetweenSections;
    }

    /**
     * Return a new line character.
     */
    public String getEndl() {
        return getEndl(1);
    }

    /**
     * Return the given number of new line characters.
     */
    public String getEndl(int num) {
        if (num == 0)
            return "";
        if (num == 1)
            return _sep;

        StringBuilder buf = new StringBuilder(_sep.length() * num);
        for (int i = 0; i < num; i++)
            buf.append(_sep);
        return buf.toString();
    }

    /**
     * Return the given number of new line characters, followed by
     * the given tab level indentation.
     */
    public String getEndl(int num, int tabs) {
        return getEndl(num) + getTab(tabs);
    }

    /**
     * Return {#getLinesBetweenSections} + 1 new line characters.
     */
    public String getAfterSection() {
        return getEndl(getLinesBetweenSections() + 1);
    }

    /**
     * Open parentheses string. Users can choose to place spaces before
     * and within parentheses.
     */
    public String getOpenParen(boolean methodOrIf) {
        if ((_spaceBeforeParen && methodOrIf) && _spaceInParen)
            return " ( ";
        if (_spaceBeforeParen && methodOrIf)
            return " (";
        if (_spaceInParen)
            return "( ";
        return "(";
    }

    /**
     * Close parentheses string. Users can choose to place spaces within
     * parentheses.
     */
    public String getCloseParen() {
        if (_spaceInParen)
            return " )";
        return ")";
    }

    /**
     * Paired parentheses for empty method parameters. Users can choose
     * to place spaces before parentheses.
     */
    public String getParens() {
        if (_spaceBeforeParen)
            return " ()";
        return "()";
    }

    /**
     * Open brace string. Users can choose to place braces on the same
     * line, or on a new line, and can choose the indenting level.
     *
     * @param tabLevel the tab level of code within the brace
     */
    public String getOpenBrace(int tabLevel) {
        if (_braceOnSameLine)
            return " {";
        if (_braceAtSameTabLevel)
            return getEndl() + getTab(tabLevel) + "{";
        return getEndl() + getTab(tabLevel - 1) + "{";
    }

    /**
     * Close brace string. Users can choose to place braces on the same
     * line, or on a new line, and can choose the indenting level.
     *
     * @param tabLevel the tab level of code within the brace
     */
    public String getCloseBrace(int tabLevel) {
        if (_braceAtSameTabLevel)
            return getTab(tabLevel) + "}";
        return getTab(tabLevel - 1) + "}";
    }

    /**
     * Extends declaration. Uses configuration of {@link #openBrace},
     * but prints "extends" instead of a brace.
     */
    public String getExtendsDec(int tabLevel) {
        if (_braceOnSameLine)
            return " extends";
        if (_braceAtSameTabLevel)
            return getEndl() + getTab(tabLevel) + "extends";
        return getEndl() + getTab(tabLevel) + "extends";
    }

    /**
     * Implements declaration. Uses configuration of {@link #openBrace},
     * but prints "implements" instead of a brace.
     */
    public String getImplementsDec(int tabLevel) {
        if (_braceOnSameLine)
            return " implements";
        if (_braceAtSameTabLevel)
            return getEndl() + getTab(tabLevel) + "implements";
        return getEndl() + getTab(tabLevel) + "implements";
    }

    /**
     * Throws declaration. Uses configuration of {@link #openBrace},
     * but prints "throws" instead of a brace.
     */
    public String getThrowsDec(int tabLevel) {
        if (_braceOnSameLine)
            return " throws";
        if (_braceAtSameTabLevel)
            return getEndl() + getTab(tabLevel) + "throws";
        return getEndl() + getTab(tabLevel) + "throws";
    }

    /**
     * Tab string. Users can choose to use spaces or tab characters.
     */
    public String getTab() {
        return getTab(1);
    }

    /**
     * Tab string. Users can choose to use spaces or tab characters.
     *
     * @param tabLevel the number of tabs
     */
    public String getTab(int tabLevel) {
        if (tabLevel == 0)
            return "";
        if (tabLevel == 1)
            return _tab;

        StringBuilder tabs = new StringBuilder(_tab.length() * tabLevel);
        for (int i = 0; i < tabLevel; i++)
            tabs.append(_tab);
        return tabs.toString();
    }


    /**
     * Returns parametrized type string for given type(s).
     */
    public String getParametrizedType(String[] typenames) {
        StringBuilder buf = new StringBuilder();
        buf.append("<");
        for (int i = 0; i < typenames.length; i++) {
            if (i > 0)
                buf.append(", ");
            buf.append(typenames[i]);
        }
        buf.append(">");
        return buf.toString();
    }

    /**
     * Return the field name for given suggested name, possibly adding
     * leading underscore.
     */
    public String getFieldName(String fieldName) {
        return (_scoreBeforeFieldName) ? "_" + fieldName : fieldName;
    }

    /**
     * Return the internal code buffer.
     */
    public StringBuffer getBuffer() {
        return _buf;
    }

    /**
     * Append the given value to the internal buffer.
     */
    public CodeFormat append(boolean val) {
        _buf.append(val);
        return this;
    }

    /**
     * Append the given value to the internal buffer.
     */
    public CodeFormat append(byte val) {
        _buf.append(val);
        return this;
    }

    /**
     * Append the given value to the internal buffer.
     */
    public CodeFormat append(char val) {
        _buf.append(val);
        return this;
    }

    /**
     * Append the given value to the internal buffer.
     */
    public CodeFormat append(double val) {
        _buf.append(val);
        return this;
    }

    /**
     * Append the given value to the internal buffer.
     */
    public CodeFormat append(float val) {
        _buf.append(val);
        return this;
    }

    /**
     * Append the given value to the internal buffer.
     */
    public CodeFormat append(int val) {
        _buf.append(val);
        return this;
    }

    /**
     * Append the given value to the internal buffer.
     */
    public CodeFormat append(long val) {
        _buf.append(val);
        return this;
    }

    /**
     * Append the given value to the internal buffer.
     */
    public CodeFormat append(short val) {
        _buf.append(val);
        return this;
    }

    /**
     * Append the given value to the internal buffer.
     */
    public CodeFormat append(Object val) {
        _buf.append(val);
        return this;
    }

    /**
     * Append the given value to the internal buffer.
     *
     * @see #getEndl()
     */
    public CodeFormat endl() {
        _buf.append(getEndl());
        return this;
    }

    /**
     * Append the given value to the internal buffer.
     *
     * @see #getEndl(int)
     */
    public CodeFormat endl(int num) {
        _buf.append(getEndl(num));
        return this;
    }

    /**
     * Append the given value to the internal buffer.
     *
     * @see #getEndl(int, int)
     */
    public CodeFormat endl(int num, int tabs) {
        _buf.append(getEndl(num, tabs));
        return this;
    }

    /**
     * Append the given value to the internal buffer.
     *
     * @see #getAfterSection
     */
    public CodeFormat afterSection() {
        _buf.append(getAfterSection());
        return this;
    }

    /**
     * Append the given value to the internal buffer.
     *
     * @see #getOpenParen
     */
    public CodeFormat openParen(boolean methodOrIf) {
        _buf.append(getOpenParen(methodOrIf));
        return this;
    }

    /**
     * Append the given value to the internal buffer.
     *
     * @see #getCloseParen
     */
    public CodeFormat closeParen() {
        _buf.append(getCloseParen());
        return this;
    }

    /**
     * Append the given value to the internal buffer.
     *
     * @see #getParens
     */
    public CodeFormat parens() {
        _buf.append(getParens());
        return this;
    }

    /**
     * Append the given value to the internal buffer.
     *
     * @see #getOpenBrace
     */
    public CodeFormat openBrace(int tabLevel) {
        _buf.append(getOpenBrace(tabLevel));
        return this;
    }

    /**
     * Append the given value to the internal buffer.
     *
     * @see #getCloseBrace
     */
    public CodeFormat closeBrace(int tabLevel) {
        _buf.append(getCloseBrace(tabLevel));
        return this;
    }

    /**
     * Append the given value to the internal buffer.
     *
     * @see #getExtendsDec
     */
    public CodeFormat extendsDec(int tabLevel) {
        _buf.append(getExtendsDec(tabLevel));
        return this;
    }

    /**
     * Append the given value to the internal buffer.
     *
     * @see #getImplementsDec
     */
    public CodeFormat implementsDec(int tabLevel) {
        _buf.append(getImplementsDec(tabLevel));
        return this;
    }

    /**
     * Append the given value to the internal buffer.
     *
     * @see #getThrowsDec
     */
    public CodeFormat throwsDec(int tabLevel) {
        _buf.append(getThrowsDec(tabLevel));
        return this;
    }

    /**
     * Append the given value to the internal buffer.
     *
     * @see #getTab
     */
    public CodeFormat tab() {
        _buf.append(getTab());
        return this;
    }

    /**
     * Append the given value to the internal buffer.
     *
     * @see #getTab
     */
    public CodeFormat tab(int tabLevel) {
        _buf.append(getTab(tabLevel));
        return this;
    }

    /**
     * Append the given value to the internal buffer.
     *
     * @see #getFieldName
     */
    public CodeFormat fieldName(String name) {
        _buf.append(getFieldName(name));
        return this;
    }

    /**
     * Clear the internal code buffer.
     */
    public void clear() {
        _buf = new StringBuffer();
    }

    /**
     * Return the internal buffer as a string.
     */
    public String toString() {
        return _buf.toString();
    }

    /**
     * Return the length of the internal buffer.
     */
    public int length() {
        return _buf.length();
    }

    /**
     * Make a copy of this code format object with all the same formatting
     * settings.
     */
    public Object clone() {
        CodeFormat format = new CodeFormat();
        format._tab = _tab;
        format._spaceBeforeParen = _spaceBeforeParen;
        format._spaceInParen = _spaceInParen;
        format._braceOnSameLine = _braceOnSameLine;
        format._braceAtSameTabLevel = _braceAtSameTabLevel;
        format._scoreBeforeFieldName = _scoreBeforeFieldName;
        format._linesBetweenSections = _linesBetweenSections;
        return format;
    }
}
