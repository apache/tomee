package org.apache.openejb.config;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public final class ScanUtil {
    private static final SAXParserFactory SAX_FACTORY = SAXParserFactory.newInstance();

    private ScanUtil() {
        // no-op
    }

    public static ScanHandler read(final URL scanXml) throws IOException {
        final SAXParser parser;
        try {
            synchronized (SAX_FACTORY) {
                parser = SAX_FACTORY.newSAXParser();
            }
            final ScanHandler handler = new ScanHandler();
            parser.parse(new BufferedInputStream(scanXml.openStream()), handler);
            return handler;
        } catch (Exception e) {
            throw new IOException("can't parse " + scanXml.toExternalForm());
        }
    }

    public static final class ScanHandler extends DefaultHandler {
        private final Set<String> classes = new HashSet<String>();
        private final Set<String> packages = new HashSet<String>();
        private Set<String> current = null;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equals("class")) {
                current = classes;
            } else if (qName.equals("package")) {
                current = packages;
            }
        }

        @Override
        public void characters(char ch[], int start, int length) throws SAXException {
            if (current != null) {
                current.add(new String(ch, start, length));
            }
        }

        @Override
        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            current = null;
        }

        public Set<String> getPackages() {
            return packages;
        }

        public Set<String> getClasses() {
            return classes;
        }
    }
}
