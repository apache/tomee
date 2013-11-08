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

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer.Message;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.xml.Commentable;
import org.apache.openjpa.lib.xml.DocTypeReader;
import org.apache.openjpa.lib.xml.Location;
import org.apache.openjpa.lib.xml.XMLFactory;

/**
 * Custom SAX parser used by the system to quickly parse metadata files.
 * Subclasses should handle the processing of the content.
 *
 * @author Abe White
 * @nojavadoc
 */
public abstract class XMLMetaDataParser extends DefaultHandler
    implements LexicalHandler, MetaDataParser {

    private static final Localizer _loc = Localizer.forPackage
        (XMLMetaDataParser.class);
    private static boolean _schemaBug;
    
    private static final String OPENJPA_NAMESPACE = "http://www.apache.org/openjpa/ns/orm";
    protected int _extendedNamespace = 0;
    protected int _openjpaNamespace = 0;

    static {
        try {
            // check for Xerces version 2.0.2 to see if we need to disable
            // schema validation, which works around the bug reported at:
            // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4708859
            _schemaBug = "Xerces-J 2.0.2".equals(Class.forName
                ("org.apache.xerces.impl.Version").getField("fVersion").
                get(null));
        } catch (Throwable t) {
            // Xerces might not be available
            _schemaBug = false;
        }
    }

    // map of classloaders to sets of parsed locations, so that we don't parse
    // the same resource multiple times for the same class
    private Map<ClassLoader, Set<String>> _parsed = null;

    private Log _log = null;
    private boolean _validating = true;
    private boolean _systemId = true;
    private boolean _caching = true;
    private boolean _parseText = true;
    private boolean _parseComments = true;
    private String _suffix = null;
    private ClassLoader _loader = null;
    private ClassLoader _curLoader = null;

    // state for current parse
    private final Collection _curResults = new LinkedList();
    private List _results = null;
    private String _sourceName = null;
    private File _sourceFile = null;
    private StringBuffer _text = null;
    private List<String> _comments = null;
    private Location _location = new Location();
    private LexicalHandler _lh = null;
    private int _depth = -1;
    private int _ignore = Integer.MAX_VALUE;

    private boolean _parsing = false;
    
    private boolean _overrideContextClassloader = false;
    
    public boolean getOverrideContextClassloader() {
        return _overrideContextClassloader;
    }

    public void setOverrideContextClassloader(boolean overrideCCL) {
        _overrideContextClassloader = overrideCCL;
    }

    /*
     * Whether the parser is currently parsing.
     */
    public boolean isParsing() {
        return _parsing;
    }

    /*
     * Whether the parser is currently parsing.
     */
    public void setParsing(boolean parsing) {
        this._parsing = parsing;
    }

    /**
     * Whether to parse element text.
     */
    public boolean getParseText() {
        return _parseText;
    }

    /**
     * Whether to parse element text.
     */
    public void setParseText(boolean text) {
        _parseText = text;
    }

    /**
     * Whether to parse element comments.
     */
    public boolean getParseComments() {
        return _parseComments;
    }

    /**
     * Whether to parse element comments.
     */
    public void setParseComments(boolean comments) {
        _parseComments = comments;
    }

    /**
     * The XML document location.
     */
    public Location getLocation() {
        return _location;
    }

    /**
     * The lexical handler that should be registered with the SAX parser used
     * by this class. Since the <code>org.xml.sax.ext</code> package is not
     * a required part of SAX2, this handler might not be used by the parser.
     */
    public LexicalHandler getLexicalHandler() {
        return _lh;
    }

    /**
     * The lexical handler that should be registered with the SAX parser used
     * by this class. Since the <code>org.xml.sax.ext</code> package is not
     * a required part of SAX2, this handler might not be used by the parser.
     */
    public void setLexicalHandler(LexicalHandler lh) {
        _lh = lh;
    }

    /**
     * The XML document location.
     */
    public void setLocation(Location location) {
        _location = location;
    }

    /**
     * Whether to use the source name as the XML system id.
     */
    public boolean getSourceIsSystemId() {
        return _systemId;
    }

    /**
     * Whether to use the source name as the XML system id.
     */
    public void setSourceIsSystemId(boolean systemId) {
        _systemId = systemId;
    }

    /**
     * Whether this is a validating parser.
     */
    public boolean isValidating() {
        return _validating;
    }

    /**
     * Whether this is a validating parser.
     */
    public void setValidating(boolean validating) {
        _validating = validating;
    }

    /**
     * Expected suffix for metadata resources, or null if unknown.
     */
    public String getSuffix() {
        return _suffix;
    }

    /**
     * Expected suffix for metadata resources, or null if unknown.
     */
    public void setSuffix(String suffix) {
        _suffix = suffix;
    }

    /**
     * Whether parsed resource names are cached to avoid duplicate parsing.
     */
    public boolean isCaching() {
        return _caching;
    }

    /**
     * Whether parsed resource names are cached to avoid duplicate parsing.
     */
    public void setCaching(boolean caching) {
        _caching = caching;
        if (!caching)
            clear();
    }

    /**
     * The log to write to.
     */
    public Log getLog() {
        return _log;
    }

    /**
     * The log to write to.
     */
    public void setLog(Log log) {
        _log = log;
    }

    /**
     * Classloader to use for class name resolution.
     */
    public ClassLoader getClassLoader() {
        return _loader;
    }

    /**
     * Classloader to use for class name resolution.
     */
    public void setClassLoader(ClassLoader loader) {
        _loader = loader;
    }

    public List getResults() {
        if (_results == null)
            return Collections.emptyList();
        return _results;
    }

    public void parse(String rsrc) throws IOException {
        if (rsrc != null)
            parse(new ResourceMetaDataIterator(rsrc, _loader));
    }

    public void parse(URL url) throws IOException {
        if (url != null)
            parse(new URLMetaDataIterator(url));
    }

    public void parse(File file) throws IOException {
        if (file == null)
            return;
        if (!(AccessController.doPrivileged(J2DoPrivHelper
            .isDirectoryAction(file))).booleanValue())
            parse(new FileMetaDataIterator(file));
        else {
            String suff = (_suffix == null) ? "" : _suffix;
            parse(new FileMetaDataIterator(file,
                new SuffixMetaDataFilter(suff)));
        }
    }

    public void parse(Class cls, boolean topDown) throws IOException {
        String suff = (_suffix == null) ? "" : _suffix;
        parse(new ClassMetaDataIterator(cls, suff, topDown), !topDown);
    }

    public void parse(Reader xml, String sourceName) throws IOException {
        if (xml != null && (sourceName == null || !parsed(sourceName)))
            parseNewResource(xml, sourceName);
    }

    public void parse(MetaDataIterator itr) throws IOException {
        parse(itr, false);
    }

    /**
     * Parse the resources returned by the given iterator, optionally stopping
     * when the first valid resource is found.
     */
    private void parse(MetaDataIterator itr, boolean stopFirst)
        throws IOException {
        if (itr == null)
            return;
        try {
            String sourceName;
            while (itr.hasNext()) {
                sourceName = itr.next().toString();
                if (parsed(sourceName)) {
                    if (stopFirst)
                        break;
                    continue;
                }

                // individual files of the resource might already be parsed
                _sourceFile = itr.getFile();
                parseNewResource(new InputStreamReader(itr.getInputStream()),
                    sourceName);
                if (stopFirst)
                    break;
            }
        }
        finally {
            itr.close();
        }
    }

    /**
     * Parse a previously-unseen source. All parsing methods delegate
     * to this one.
     */
    protected void parseNewResource(Reader xml, String sourceName)
        throws IOException {
        if (_log != null && _log.isTraceEnabled())
            _log.trace(_loc.get("start-parse", sourceName));

        // even if we want to validate, specify that it won't happen
        // if we have neither a DocType not a Schema
        Object schemaSource = getSchemaSource();
        if (schemaSource != null && _schemaBug) {
            if (_log != null && _log.isTraceEnabled())
                _log.trace(_loc.get("parser-schema-bug"));
            schemaSource = null;
        }
        boolean validating = _validating && (getDocType() != null 
            || schemaSource != null);

        // parse the metadata with a SAX parser
        try {
            setParsing(true);
            _sourceName = sourceName;
            
            SAXParser parser = null;
            boolean overrideCL = _overrideContextClassloader;
            ClassLoader oldLoader = null;
            ClassLoader newLoader = null;           
            
            try {
                if (overrideCL == true) {
                    oldLoader =
                        (ClassLoader) AccessController.doPrivileged(J2DoPrivHelper.getContextClassLoaderAction());
                    newLoader = XMLMetaDataParser.class.getClassLoader();
                    AccessController.doPrivileged(J2DoPrivHelper.setContextClassLoaderAction(newLoader));
                    
                    if (_log != null && _log.isTraceEnabled()) {
                        _log.trace(_loc.get("override-contextclassloader-begin", oldLoader, newLoader));
                    }                   
                }
                
                parser = XMLFactory.getSAXParser(validating, true);
                Object schema = null;
                if (validating) {
                    schema = schemaSource;
                    if (schema == null && getDocType() != null)
                        xml = new DocTypeReader(xml, getDocType());
                }

                if (_parseComments || _lh != null)
                    parser.setProperty
                        ("http://xml.org/sax/properties/lexical-handler", this);

                if (schema != null) {
                    parser.setProperty
                        ("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                            "http://www.w3.org/2001/XMLSchema");
                    parser.setProperty
                        ("http://java.sun.com/xml/jaxp/properties/schemaSource",
                            schema);
                }

                InputSource is = new InputSource(xml);
                if (_systemId && sourceName != null)
                    is.setSystemId(sourceName);
                parser.parse(is, this);
                finish();
            } catch (SAXException se) {
                IOException ioe = new IOException(se.toString());
                ioe.initCause(se);
                throw ioe;
            } finally {
                if (overrideCL == true) {
                    // Restore the old ContextClassloader
                    try {
                        if (_log != null && _log.isTraceEnabled()) {
                            _log.trace(_loc.get("override-contextclassloader-end", newLoader, oldLoader));
                        }
                        AccessController.doPrivileged(J2DoPrivHelper.setContextClassLoaderAction(oldLoader));
                    } catch (Throwable t) {
                        if (_log != null && _log.isWarnEnabled()) {
                            _log.warn(_loc.get("restore-contextclassloader-failed"));
                        }
                    }
                }
            }
        } finally {
            reset();
        }
    }

    /**
     * Return true if the given source is parsed. Otherwise, record that
     * it will be parsed.
     */
    protected boolean parsed(String src) {
        if (!_caching)
            return false;
        if (_parsed == null)
            _parsed = new HashMap<ClassLoader, Set<String>>();

        ClassLoader loader = currentClassLoader();
        Set<String> set = _parsed.get(loader);
        if (set == null) {
            set = new HashSet<String>();
            _parsed.put(loader, set);
        }
        boolean added = set.add(src);
        if (!added && _log != null && _log.isTraceEnabled())
            _log.trace(_loc.get("already-parsed", src));
        return !added;
    }

    public void clear() {
        if (_log != null && _log.isTraceEnabled())
            _log.trace(_loc.get("clear-parser", this));
        if (_parsed != null)
            _parsed.clear();
    }

    public void error(SAXParseException se) throws SAXException {
        throw getException(se.toString());
    }

    public void fatalError(SAXParseException se) throws SAXException {
        throw getException(se.toString());
    }

    public void setDocumentLocator(Locator locator) {
        _location.setLocator(locator);
    }

    public void startElement(String uri, String name, String qName,
        Attributes attrs) throws SAXException {
        _depth++;
        if (_depth <= _ignore){
            if (uri.equals(OPENJPA_NAMESPACE)) {
                _extendedNamespace++;
                _openjpaNamespace++;
            }
            if (!startElement(qName, attrs))
                ignoreContent(true);
        }
    }

    public void endElement(String uri, String name, String qName)
        throws SAXException {
        if (_depth < _ignore) {
            endElement(qName);
            _extendedNamespace = (_extendedNamespace > 0) ? _extendedNamespace - 1 : 0;
            _openjpaNamespace = (_openjpaNamespace > 0) ? _openjpaNamespace - 1 : 0;
        }
        else if (_depth ==_ignore) {
            _extendedNamespace = (_extendedNamespace > 0) ? _extendedNamespace - 1 : 0;
            _openjpaNamespace = (_openjpaNamespace > 0) ? _openjpaNamespace - 1 : 0;
        }
        
        _text = null;
        if (_comments != null)
            _comments.clear();
        if (_depth == _ignore)
            _ignore = Integer.MAX_VALUE;
        _depth--;
    }

    public void characters(char[] ch, int start, int length) {
        if (_parseText && _depth <= _ignore) {
            if (_text == null)
                _text = new StringBuffer();
            _text.append(ch, start, length);
        }
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
        if (_parseComments && _depth <= _ignore) {
            if (_comments == null)
                _comments = new ArrayList<String>(3);
            _comments.add(String.valueOf(ch, start, length));
        }
        if (_lh != null)
            _lh.comment(ch, start, length);
    }

    public void startCDATA() throws SAXException {
        if (_lh != null)
            _lh.startCDATA();
    }

    public void endCDATA() throws SAXException {
        if (_lh != null)
            _lh.endCDATA();
    }

    public void startDTD(String name, String publicId, String systemId)
        throws SAXException {
        if (_lh != null)
            _lh.startDTD(name, publicId, systemId);
    }

    public void endDTD() throws SAXException {
        if (_lh != null)
            _lh.endDTD();
    }

    public void startEntity(String name) throws SAXException {
        if (_lh != null)
            _lh.startEntity(name);
    }

    public void endEntity(String name) throws SAXException {
        if (_lh != null)
            _lh.endEntity(name);
    }

    /**
     * Override this method marking the start of some element. If this method
     * returns false, the content of the element and the end element event will
     * be ignored.
     */
    protected abstract boolean startElement(String name, Attributes attrs)
        throws SAXException;

    /**
     * Override this method marking the end of some element.
     */
    protected abstract void endElement(String name) throws SAXException;

    /**
     * Add a result to be returned from the current parse.
     */
    protected void addResult(Object result) {
        if (_log != null && _log.isTraceEnabled())
            _log.trace(_loc.get("add-result", result));
        _curResults.add(result);
    }

    /**
     * Override this method to finish up after a parse; this is only
     * called if no errors are encountered during parsing. Subclasses should
     * call <code>super.finish()</code> to resolve superclass state.
     */
    protected void finish() {
        if (_log != null && _log.isTraceEnabled())
            _log.trace(_loc.get("end-parse", getSourceName()));
        _results = new ArrayList(_curResults);
    }

    /**
     * Override this method to clear any state and ready the parser for
     * a new document. Subclasses should call
     * <code>super.reset()</code> to clear superclass state.
     */
    protected void reset() {
        _curResults.clear();
        _curLoader = null;
        _sourceName = null;
        _sourceFile = null;
        _depth = -1;
        _ignore = Integer.MAX_VALUE;
        if (_comments != null)
            _comments.clear();
        clearDeferredMetaData();
        setParsing(false);
    }

    /**
     * Implement to return the XML schema source for the document. Returns
     * null by default. May return:
     * <ul>
     * <li><code>String</code> pointing to schema URI.</li>
     * <li><code>InputStream</code> containing schema contents.</li>
     * <li><code>InputSource</code> containing schema contents.</li>
     * <li><code>File</code> containing schema contents.</li>
     * <li>Array of any of the above elements.</li>
     * </ul>
     */
    protected Object getSchemaSource() throws IOException {
        return null;
    }

    /**
     * Override this method to return any <code>DOCTYPE</code> declaration
     * that should be dynamically included in xml documents that will be
     * validated. Returns null by default.
     */
    protected Reader getDocType() throws IOException {
        return null;
    }

    /**
     * Return the name of the source file being parsed.
     */
    protected String getSourceName() {
        return _sourceName;
    }

    /**
     * Return the file of the source being parsed.
     */
    protected File getSourceFile() {
        return _sourceFile;
    }

    /**
     * Add current comments to the given entity. By default, assumes entity
     * is {@link Commentable}.
     */
    protected void addComments(Object obj) {
        String[] comments = currentComments();
        if (comments.length > 0 && obj instanceof Commentable)
            ((Commentable) obj).setComments(comments);
    }

    /**
     * Array of comments for the current node, or empty array if none.
     */
    protected String[] currentComments() {
        if (_comments == null || _comments.isEmpty())
            return Commentable.EMPTY_COMMENTS;
        return _comments.toArray(new String[_comments.size()]);
    }

    /**
     * Return the text value within the current node.
     */
    protected String currentText() {
        if (_text == null)
            return "";
        return _text.toString().trim();
    }

    /**
     * Return the current location within the source file.
     */
    protected String currentLocation() {
        return " [" + _loc.get("loc-prefix") + _location.getLocation() + "]";
    }

    /**
     * Return the parse depth. Within the root element, the depth is 0,
     * within the first nested element, it is 1, and so forth.
     */
    protected int currentDepth() {
        return _depth;
    }

    /**
     * Return the class loader to use when resolving resources and loading
     * classes.
     */
    protected ClassLoader currentClassLoader() {
        if (_loader != null)
            return _loader;
        if (_curLoader == null)
            _curLoader = AccessController.doPrivileged(
                J2DoPrivHelper.getContextClassLoaderAction());
        return _curLoader;
    }

    /**
     * Ignore all content below the current element.
     *
     * @param ignoreEnd whether to ignore the end element event
     */
    protected void ignoreContent(boolean ignoreEnd) {
        _ignore = _depth;
        if (!ignoreEnd)
            _ignore++;
    }

    /**
     * Returns a SAXException with the source file name and the given error
     * message.
     */
    protected SAXException getException(String msg) {
        return new SAXException(getSourceName() + currentLocation() +
            ": " + msg);
    }

    /**
     * Returns a SAXException with the source file name and the given error
     * message.
     */
    protected SAXException getException(Message msg) {
        return new SAXException(getSourceName() + currentLocation() +
            ": " + msg.getMessage());
    }

    /**
     * Returns a SAXException with the source file name and the given error
     * message.
     */
    protected SAXException getException(Message msg, Throwable cause) {
        if (cause != null && _log != null && _log.isTraceEnabled())
            _log.trace(_loc.get("sax-exception",
                getSourceName(), _location.getLocation()), cause);
        SAXException e = new SAXException(getSourceName() + currentLocation() +
            ": " + msg + " [" + cause + "]");
        e.initCause(cause);
        return e;
    }

    protected void clearDeferredMetaData() {
    }
}
