/*
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
package org.apache.openejb.sxc;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

/**
 * @version $Rev$ $Date$
 */
class DebugStreamReaderDelegate extends StreamReaderDelegate {
    public DebugStreamReaderDelegate(final XMLStreamReader streamReader) {
        super(streamReader);
    }

    @Override
    public Object getProperty(final String name) {
        return super.getProperty(name);
    }

    @Override
    public String getPIData() {
        final String s = super.getPIData();
        System.out.println("getPIData = " + s);
        return s;
    }

    @Override
    public String getPITarget() {
        final String s = super.getPITarget();
        System.out.println("getPITarget = " + s);
        return s;
    }

    @Override
    public String getCharacterEncodingScheme() {
        final String s = super.getCharacterEncodingScheme();
        System.out.println("getCharacterEncodingScheme = " + s);
        return s;
    }

    @Override
    public boolean standaloneSet() {
        return super.standaloneSet();
    }

    @Override
    public boolean isStandalone() {
        return super.isStandalone();
    }

    @Override
    public String getVersion() {
        final String s = super.getVersion();
        System.out.println("getVersion = " + s);
        return s;
    }

    @Override
    public String getPrefix() {
        final String s = super.getPrefix();
        System.out.println("getPrefix = " + s);
        return s;
    }

    @Override
    public String getNamespaceURI() {
        final String s = super.getNamespaceURI();
        System.out.println("getNamespaceURI() = " + s);
        return s;
    }

    @Override
    public boolean hasName() {
        return super.hasName();
    }

    @Override
    public String getLocalName() {
        final String s = super.getLocalName();
        System.out.println("getLocalName = " + s);
        return s;
    }

    @Override
    public QName getName() {
        return super.getName();
    }

    @Override
    public Location getLocation() {
        return super.getLocation();
    }

    @Override
    public boolean hasText() {
        return super.hasText();
    }

    @Override
    public String getEncoding() {
        final String s = super.getEncoding();
        System.out.println("getEncoding = " + s);
        return s;
    }

    @Override
    public int getTextLength() {
        return super.getTextLength();
    }

    @Override
    public int getTextStart() {
        return super.getTextStart();
    }

    @Override
    public char[] getTextCharacters() {
        return super.getTextCharacters();
    }

    @Override
    public int getTextCharacters(final int sourceStart, final char[] target, final int targetStart, final int length) throws XMLStreamException {
        return super.getTextCharacters(sourceStart, target, targetStart, length);
    }

    @Override
    public String getText() {
        final String s = super.getText();
        System.out.println("getText = " + s);
        return s;
    }

    @Override
    public int getEventType() {
        return super.getEventType();
    }

    @Override
    public String getNamespaceURI(final int index) {
        final String s = super.getNamespaceURI(index);
        System.out.println("getNamespaceURI(int) = " + s);
        return s;
    }

    @Override
    public String getNamespacePrefix(final int index) {
        final String s = super.getNamespacePrefix(index);
        System.out.println("getNamespacePrefix = " + s);
        return s;
    }

    @Override
    public int getNamespaceCount() {
        return super.getNamespaceCount();
    }

    @Override
    public boolean isAttributeSpecified(final int index) {
        return super.isAttributeSpecified(index);
    }

    @Override
    public String getAttributeValue(final int index) {
        final String s = super.getAttributeValue(index);
        System.out.println("getAttributeValue = " + s);
        return s;
    }

    @Override
    public String getAttributeType(final int index) {
        final String s = super.getAttributeType(index);
        System.out.println("getAttributeType = " + s);
        return s;
    }

    @Override
    public String getAttributeLocalName(final int index) {
        final String s = super.getAttributeLocalName(index);
        System.out.println("getAttributeLocalName = " + s);
        return s;
    }

    @Override
    public String getAttributeNamespace(final int index) {
        final String s = super.getAttributeNamespace(index);
        System.out.println("getAttributeNamespace = " + s);
        return s;
    }

    @Override
    public String getAttributePrefix(final int index) {
        final String s = super.getAttributePrefix(index);
        System.out.println("getAttributePrefix = " + s);
        return s;
    }

    @Override
    public QName getAttributeName(final int index) {
        return super.getAttributeName(index);
    }

    @Override
    public int getAttributeCount() {
        return super.getAttributeCount();
    }

    @Override
    public String getAttributeValue(final String namespaceUri, final String localName) {
        final String s = super.getAttributeValue(namespaceUri, localName);
        System.out.println("getAttributeValue = " + s);
        return s;
    }

    @Override
    public boolean isWhiteSpace() {
        return super.isWhiteSpace();
    }

    @Override
    public boolean isCharacters() {
        return super.isCharacters();
    }

    @Override
    public boolean isEndElement() {
        return super.isEndElement();
    }

    @Override
    public boolean isStartElement() {
        return super.isStartElement();
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return super.getNamespaceContext();
    }

    @Override
    public String getNamespaceURI(final String prefix) {
        final String s = super.getNamespaceURI(prefix);
        System.out.println("getNamespaceURI(string) = " + s);
        return s;
    }

    @Override
    public void close() throws XMLStreamException {
        super.close();
    }

    @Override
    public boolean hasNext() throws XMLStreamException {
        return super.hasNext();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    @Override
    public void require(final int type, final String namespaceURI, final String localName) throws XMLStreamException {
        super.require(type, namespaceURI, localName);
    }

    @Override
    public String getElementText() throws XMLStreamException {
        final String s = super.getElementText();
        System.out.println("getElementText = " + s);
        return s;
    }

    @Override
    public int nextTag() throws XMLStreamException {
        return super.nextTag();
    }

    @Override
    public int next() throws XMLStreamException {
        return super.next();
    }

    @Override
    public XMLStreamReader getParent() {
        return super.getParent();
    }

    @Override
    public String toString() {
        final String s = super.toString();
        System.out.println("toString = " + s);
        return s;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public void setParent(final XMLStreamReader reader) {
        super.setParent(reader);
    }

    @Override
    public boolean equals(final Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
