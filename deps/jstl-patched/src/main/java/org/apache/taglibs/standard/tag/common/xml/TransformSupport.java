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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.taglibs.standard.tag.common.xml;

import org.apache.taglibs.standard.resources.Resources;
import org.apache.taglibs.standard.tag.common.core.ImportSupport;
import org.apache.taglibs.standard.tag.common.core.Util;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.List;
import java.util.MissingResourceException;

public abstract class TransformSupport extends BodyTagSupport {

    protected Object xml;

    protected String xmlSystemId;

    protected Object xslt;

    protected String xsltSystemId;

    protected Result result;

    private String var;

    private int scope;

    private Transformer t;

    private final TransformerFactory tf;

    private final DocumentBuilder db;

    public TransformSupport() {
        super();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setValidating(false);
            db = dbf.newDocumentBuilder();
            tf = TransformerFactory.newInstance();
        } catch (ParserConfigurationException e) {
            throw (AssertionError) new AssertionError("Unable to create DocumentBuilder").initCause(e);
        }

        init();
    }

    private void init() {
        xml = xslt = null;
        xmlSystemId = xsltSystemId = null;
        var = null;
        result = null;
        tf.setURIResolver(null);
        scope = PageContext.PAGE_SCOPE;
    }

    @Override
    public int doStartTag()
            throws JspException {

        t = getTransformer(xslt, xsltSystemId);
        return EVAL_BODY_BUFFERED;
    }

    @Override
    public int doEndTag()
            throws JspException {
        try {

            Object xml = this.xml;
            if (xml == null) {
                if (bodyContent != null && bodyContent.getString() != null) {
                    xml = bodyContent.getString().trim();
                } else {
                    xml = "";
                }
            }

            if (isNullOrEmpty(xml)) {
                throw new JspTagException("xml is null");
            }

            Source source = getSource(xml, xmlSystemId);

            if (result != null) {
                t.transform(source, result);
            } else if (var != null) {

                Document d = db.newDocument();
                Result doc = new DOMResult(d);
                t.transform(source, doc);
                pageContext.setAttribute(var, d, scope);
            } else {
                Result page = new StreamResult(new SafeWriter(pageContext.getOut()));
                t.transform(source, page);
            }

            return EVAL_PAGE;
        } catch (SAXException ex) {
            throw new JspException(ex);
        } catch (ParserConfigurationException ex) {
            throw new JspException(ex);
        } catch (IOException ex) {
            throw new JspException(ex);
        } catch (TransformerException ex) {
            throw new JspException(ex);
        }
    }


    @Override
    public void release() {
        super.release();
        init();
    }

    @Override
    public void setPageContext(PageContext pageContext) {
        super.setPageContext(pageContext);
        tf.setURIResolver(pageContext == null ? null : new JstlUriResolver(pageContext));
    }


    public void addParameter(String name, Object value) {
        t.setParameter(name, value);
    }

    private static String wrapSystemId(String systemId) {
        if (systemId == null) {
            return "jstl:";
        } else if (ImportSupport.isAbsoluteUrl(systemId)) {
            return systemId;
        } else {
            return ("jstl:" + systemId);
        }
    }

    Transformer getTransformer(final Object xslt, final String systemId)
            throws JspException {
        if (isNullOrEmpty(xslt)) {
            String name = "TRANSFORM_XSLT_IS_NULL";
            throw new JspTagException(getMessage(name));
        }

        try {

            final Source s = getSource(xslt, systemId);

            tf.setURIResolver(new JstlUriResolver(pageContext));
            return tf.newTransformer(s);

        } catch (SAXException ex) {
            throw new JspException(ex);
        } catch (ParserConfigurationException ex) {
            throw new JspException(ex);
        } catch (IOException ex) {
            throw new JspException(ex);
        } catch (TransformerConfigurationException ex) {
            throw new JspException(ex);
        }
    }

    private String getMessage(String name) {
        try {
            return Resources.getMessage(name);
        } catch (MissingResourceException e) {
            return name;
        }
    }

    protected boolean isNullOrEmpty(Object value) {
        if (value == null) {
            return true;
        }

        if (!(value instanceof String)) {
            return false;
        }

        String str = (String) value;
        str = str.trim();
        return str.isEmpty();
    }

    private Source getSource(Object o, String systemId)
            throws SAXException, ParserConfigurationException, IOException, JspTagException {
        if (o == null) {
            throw new JspTagException(getMessage("TRANSFORM_XML_IS_NULL"));
        }

        if (o instanceof List) {

            List<?> list = (List<?>) o;
            if (list.size() != 1) {
                throw new JspTagException(getMessage("TRANSFORM_XML_LIST_SIZE"));
            }
            return getSource(list.get(0), systemId);
        }

        if (o instanceof Source) {
            return (Source) o;
        }

        if (o instanceof String) {
            String s = (String) o;
            s = s.trim();
            if (s.length() == 0) {
                throw new JspTagException(getMessage("TRANSFORM_XML_IS_EMPTY"));
            }
            return getSource(new StringReader(s), systemId);
        }

        if (o instanceof Reader) {
            return getSource((Reader) o, systemId);
        }

        if (o instanceof Node) {
            return new DOMSource((Node) o, systemId);
        }
        throw new JspTagException(Resources.getMessage("TRANSFORM_XML_UNSUPPORTED_TYPE", o.getClass()));
    }

    Source getSource(Reader reader, String systemId)
            throws JspTagException {
        try {
            XMLReader xr = XMLReaderFactory.createXMLReader();
            xr.setEntityResolver(new ParseSupport.JstlEntityResolver(pageContext));
            InputSource s = new InputSource(reader);
            s.setSystemId(wrapSystemId(systemId));
            Source result = new SAXSource(xr, s);
            result.setSystemId(wrapSystemId(systemId));
            return result;
        } catch (SAXException e) {
            throw new JspTagException(e);
        }
    }


    public void setVar(String var) {
        this.var = var;
    }

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }

    private static class SafeWriter
            extends Writer {
        private final Writer w;

        public SafeWriter(Writer w) {
            this.w = w;
        }

        @Override
        public void close() {
        }

        @Override
        public void flush() {
        }

        @Override
        public void write(char[] cbuf, int off, int len)
                throws IOException {
            w.write(cbuf, off, len);
        }
    }

    private static class JstlUriResolver
            implements URIResolver {
        private final PageContext ctx;

        public JstlUriResolver(PageContext ctx) {
            this.ctx = ctx;
        }

        public Source resolve(String href, String base)
                throws TransformerException {

            if (href == null) {
                return null;
            }

            int index;
            if (base != null && (index = base.indexOf("jstl:")) != -1) {
                base = base.substring(index + 5);
            }

            if (ImportSupport.isAbsoluteUrl(href) || (base != null && ImportSupport.isAbsoluteUrl(base))) {
                return null;
            }

            if (base == null || base.lastIndexOf("/") == -1) {
                base = "";
            } else {
                base = base.substring(0, base.lastIndexOf("/") + 1);
            }

            String target = base + href;

            InputStream s;
            if (target.startsWith("/")) {
                s = ctx.getServletContext().getResourceAsStream(target);
                if (s == null) {
                    throw new TransformerException(Resources.getMessage("UNABLE_TO_RESOLVE_ENTITY", href));
                }
            } else {
                String pagePath = ((HttpServletRequest) ctx.getRequest()).getServletPath();
                String basePath = pagePath.substring(0, pagePath.lastIndexOf("/"));
                s = ctx.getServletContext().getResourceAsStream(basePath + "/" + target);
                if (s == null) {
                    throw new TransformerException(Resources.getMessage("UNABLE_TO_RESOLVE_ENTITY", href));
                }
            }
            return new StreamSource(s);
        }
    }

}
