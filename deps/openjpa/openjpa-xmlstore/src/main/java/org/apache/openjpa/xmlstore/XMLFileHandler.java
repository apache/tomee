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
package org.apache.openjpa.xmlstore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import javax.xml.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.apache.openjpa.enhance.PCRegistry;
import org.apache.openjpa.lib.util.Base16Encoder;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.xml.XMLFactory;
import org.apache.openjpa.lib.xml.XMLWriter;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.util.Id;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.OpenJPAException;
import org.apache.openjpa.util.StoreException;
import org.apache.openjpa.util.UnsupportedException;

/**
 * Stores {@link ObjectData} objects by serializing a collection
 * of them into and out of an XML file.
 */
public class XMLFileHandler {

    private final XMLConfiguration _conf;

    /**
     * Constructor; supply configuration.
     */
    public XMLFileHandler(XMLConfiguration conf) {
        _conf = conf;
    }

    /**
     * Loads all instances of <code>meta</code> into a list of objects.
     * The given <code>meta</code> must represent a least-derived
     * persistence-capable type.
     */
    public Collection load(ClassMetaData meta) {
        File f = getFile(meta);
        if (!(AccessController.doPrivileged(
            J2DoPrivHelper.existsAction(f))).booleanValue() || 
            (AccessController.doPrivileged(
            J2DoPrivHelper.lengthAction(f))).longValue() == 0)
            return Collections.EMPTY_SET;
        try {
            return read(f);
        } catch (OpenJPAException ke) {
            throw ke;
        } catch (Exception e) {
            throw new StoreException(e);
        }
    }

    /**
     * Read a collection of {@link ObjectData}s from the contents of the
     * given file.
     */
    private Collection read(File f)
        throws Exception {
        // parse the file and return the objects it contains
        SAXParser parser = XMLFactory.getSAXParser(false, false);
        ObjectDataHandler handler = new ObjectDataHandler(_conf);
        parser.parse(f, handler);
        return handler.getExtent();
    }

    /**
     * Returns a {@link File} object that <code>meta</code> lives
     * in. This implementation creates a filename from the full class
     * name of the type identified by <code>meta</code>, and returns
     * a {@link File} object that has this filename and whose base
     * directory is the URL identified by the <code>ConnectionURL</code>
     * configuration property.
     */
    private File getFile(ClassMetaData meta) {
        if (_conf.getConnectionURL() == null) {
            throw new InternalException("Invalid ConnectionURL");
        }
        File baseDir = new File(_conf.getConnectionURL());
        return new File(baseDir, meta.getDescribedType().getName());
    }

    /**
     * Stores all instances in <code>datas</code> into the appropriate file,
     * as dictated by <code>meta</code>.
     *
     * @param meta the least-derived type of the instances being stored
     * @param datas a collection of {@link ObjectData} instances, each
     * of which represents an object of type <code>meta</code>
     */
    public void store(ClassMetaData meta, Collection datas) {
        if (meta.getPCSuperclass() != null)
            throw new InternalException();

        File f = getFile(meta);
        if (!(AccessController.doPrivileged(
            J2DoPrivHelper.existsAction(f.getParentFile()))).booleanValue())
            AccessController.doPrivileged(
                J2DoPrivHelper.mkdirsAction(f.getParentFile()));

        FileWriter fw = null;
        try {
            fw = new FileWriter(f);
            write(datas, fw);
        } catch (OpenJPAException ke) {
            throw ke;
        } catch (Exception e) {
            throw new StoreException(e);
        } finally {
            if (fw != null)
                try {
                    fw.close();
                } catch (IOException ioe) {
                }
        }
    }

    /**
     * Write the given collection of {@link ObjectData}s to the given file.
     */
    private void write(Collection datas, FileWriter fw)
        throws Exception {
        // create an XML pretty printer to write out the objects
        Writer out = new XMLWriter(fw);

        // start the file; the root node is an "extent"
        out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.write("<extent>");

        // run through each object in the collection
        for (Iterator itr = datas.iterator(); itr.hasNext();) {
            ObjectData obj = (ObjectData) itr.next();
            ClassMetaData meta = obj.getMetaData();

            // write out the "object" element start
            out.write("<object class=\"");
            out.write(meta.getDescribedType().getName());
            out.write("\" oid=\"");
            out.write(obj.getId().toString());
            out.write("\" version=\"");
            out.write(obj.getVersion().toString());
            out.write("\">");

            // run through each field writing out the value
            FieldMetaData[] fmds = meta.getFields();
            for (int i = 0; i < fmds.length; i++) {
                if (fmds[i].getManagement() != fmds[i].MANAGE_PERSISTENT)
                    continue;

                out.write("<field name=\"");
                out.write(fmds[i].getName());
                out.write("\">");

                // write out the field data depending upon type
                switch (fmds[i].getTypeCode()) {
                    case JavaTypes.COLLECTION:
                    case JavaTypes.ARRAY:
                        Collection c = (Collection) obj.getField(i);
                        if (c == null)
                            break;

                        // write out each of the elements
                        int elemType = fmds[i].getElement().getTypeCode();
                        for (Iterator ci = c.iterator(); ci.hasNext();) {
                            out.write("<element>");
                            writeDataValue(out, elemType, ci.next());
                            out.write("</element>");
                        }
                        break;

                    case JavaTypes.MAP:
                        Map m = (Map) obj.getField(i);
                        if (m == null)
                            break;

                        // write out each of the map entries
                        Collection entries = m.entrySet();
                        int keyType = fmds[i].getKey().getTypeCode();
                        int valueType = fmds[i].getElement().getTypeCode();
                        for (Iterator ei = entries.iterator(); ei.hasNext();) {
                            Map.Entry e = (Map.Entry) ei.next();
                            out.write("<key>");
                            writeDataValue(out, keyType, e.getKey());
                            out.write("</key>");
                            out.write("<value>");
                            writeDataValue(out, valueType, e.getValue());
                            out.write("</value>");
                        }
                        break;

                    default:
                        writeDataValue(out, fmds[i].getTypeCode(),
                            obj.getField(i));
                }
                out.write("</field>");
            }
            out.write("</object>");
        }
        out.write("</extent>");
    }

    /**
     * Write out the data value. This method writes nulls as "null",
     * serializes (using Java serialization and base16 encoding) out non-
     * primitives/boxed primitives and non-persistent types, and writes
     * primitives/boxed primitives and oids using their toString.
     */
    public void writeDataValue(Writer out, int type, Object val)
        throws IOException {
        // write nulls as "null"
        if (val == null) {
            out.write("null");
            return;
        }

        switch (type) {
            case JavaTypes.OBJECT:
            case JavaTypes.OID:
                if (!(val instanceof Serializable))
                    throw new UnsupportedException(
                        "Cannot store non-serializable,"
                            + " non-persistence-capable value: " + val);

                // serialize out the object and encode the result with base16
                ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(val);
                oos.close();
                out.write(Base16Encoder.encode(baos.toByteArray()));
                break;

            case JavaTypes.CHAR:
            case JavaTypes.CHAR_OBJ:
                // quote chars so we can distinguish whitespace chars; special
                // case for \0
                char c = ((Character) val).charValue();
                out.write("'");
                if (c == '\0')
                    out.write("0x0");
                else
                    out.write(XMLEncoder.encode(val.toString()));
                out.write("'");
                break;

            case JavaTypes.STRING:
                // quote strings so we can distinguish leading and trailing
                // whitespace
                out.write("\"");
                out.write(XMLEncoder.encode(val.toString()));
                out.write("\"");
                break;

            case JavaTypes.PC:
            case JavaTypes.PC_UNTYPED:
                // write the type of oid object + ':' + oid string
                out.write(val.getClass().getName());
                out.write(':');
                out.write(XMLEncoder.encode(val.toString()));
                break;

            default:
                // must be a number of simple type; no need to encode
                out.write(val.toString());
        }
    }

    /**
     * Used to reconstruct {@link ObjectData} instances from SAX events.
     */
    private static class ObjectDataHandler
        extends DefaultHandler {

        private static final Class[] ARGS = new Class[]{ String.class };

        private final XMLConfiguration _conf;
        private final Collection _extent = new ArrayList();

        // parse state
        private ObjectData _object;
        private FieldMetaData _fmd;
        private Object _fieldVal;
        private Object _keyVal;
        private StringBuffer _buf;

        /**
         * Constructor; supply configuration.
         */
        public ObjectDataHandler(XMLConfiguration conf) {
            _conf = conf;
        }

        /**
         * Return the results of the parsing.
         */
        public Collection getExtent() {
            return _extent;
        }

        public void startElement(String uri, String localName, String qName,
            Attributes attrs)
            throws SAXException {
            try {
                startElement(qName, attrs);
            } catch (RuntimeException re) {
                throw re;
            } catch (SAXException se) {
                throw se;
            } catch (Exception e) {
                throw new SAXException(e);
            }
        }

        private void startElement(String qName, Attributes attrs)
            throws Exception {
            switch (qName.charAt(0)) {
                case 'o': // object
                    // get the metadata for the type we're reading
                    String type = attrs.getValue("class");
                    ClassMetaData meta = _conf.getMetaDataRepositoryInstance().
                        getMetaData(classForName(type), null, true);

                    // construct the oid object
                    Object oid;
                    if (meta.getIdentityType() == meta.ID_DATASTORE)
                        oid = new Id(attrs.getValue("oid"), _conf, null);
                    else
                        oid = PCRegistry.newObjectId(meta.getDescribedType(),
                            attrs.getValue("oid"));

                    // create an ObjectData that will contain the information
                    // for this instance, and set the version
                    _object = new ObjectData(oid, meta);
                    _object.setVersion(new Long(attrs.getValue("version")));
                    break;

                case 'f': // field
                    // start parsing a field element: for container types,
                    // initialize the container; for other types, initialize a
                    // buffer
                    _fmd =
                        _object.getMetaData().getField(attrs.getValue("name"));
                    switch (_fmd.getTypeCode()) {
                        case JavaTypes.COLLECTION:
                        case JavaTypes.ARRAY:
                            _fieldVal = new ArrayList();
                            break;
                        case JavaTypes.MAP:
                            _fieldVal = new HashMap();
                            break;
                        default:
                            _buf = new StringBuffer();
                    }
                    break;

                case 'e': // element
                case 'k': // key
                case 'v': // value
                    // initialize a buffer for the element value
                    _buf = new StringBuffer();
                    break;
            }
        }

        public void endElement(String uri, String localName, String qName)
            throws SAXException {
            try {
                endElement(qName);
            } catch (RuntimeException re) {
                throw re;
            } catch (SAXException se) {
                throw se;
            } catch (Exception e) {
                throw new SAXException(e);
            }
        }

        private void endElement(String qName)
            throws Exception {
            Object val;
            switch (qName.charAt(0)) {
                case 'o': // object
                    // add the object to our results
                    _extent.add(_object);

                case 'f': // field
                    switch (_fmd.getTypeCode()) {
                        case JavaTypes.COLLECTION:
                        case JavaTypes.ARRAY:
                        case JavaTypes.MAP:
                            // field value already constructed
                            break;
                        default:
                            // construct the field value from text within the
                            // element
                            _fieldVal = fromXMLString(_fmd.getTypeCode(),
                                _fmd.getTypeMetaData(), _buf.toString());
                    }

                    // set the field value into the object being parsed
                    _object.setField(_fmd.getIndex(), _fieldVal);
                    break;

                case 'e': // element
                    // cache element value
                    val = fromXMLString(_fmd.getElement().getTypeCode(),
                        _fmd.getElement().getTypeMetaData(), _buf.toString());
                    ((Collection) _fieldVal).add(val);
                    break;

                case 'k': // key
                    // cache key value
                    _keyVal = fromXMLString(_fmd.getKey().getTypeCode(),
                        _fmd.getKey().getTypeMetaData(), _buf.toString());
                    break;

                case 'v': // value
                    // create value and put cached key and value into map
                    val = fromXMLString(_fmd.getElement().getTypeCode(),
                        _fmd.getElement().getTypeMetaData(), _buf.toString());
                    Map map = (Map) _fieldVal;
                    map.put(_keyVal, val);
                    break;
            }

            // don't cache text between elements
            _buf = null;
        }

        public void characters(char[] ch, int start, int length) {
            if (_buf != null)
                _buf.append(ch, start, length);
        }

        /**
         * Recreate a field value from its XML string.
         */
        public Object fromXMLString(int type, ClassMetaData rel, String str)
            throws Exception {
            str = str.trim();
            if (str.equals("null"))
                return null;

            switch (type) {
                case JavaTypes.BOOLEAN:
                case JavaTypes.BOOLEAN_OBJ:
                    return Boolean.valueOf(str);

                case JavaTypes.BYTE:
                case JavaTypes.BYTE_OBJ:
                    return new Byte(str);

                case JavaTypes.CHAR:
                case JavaTypes.CHAR_OBJ:
                    // strip quotes; special case for 0x0
                    str = str.substring(1, str.length() - 1);
                    if (str.equals("0x0"))
                        return new Character('\0');
                    return new Character(XMLEncoder.decode(str).charAt(0));

                case JavaTypes.DOUBLE:
                case JavaTypes.DOUBLE_OBJ:
                    return new Double(str);

                case JavaTypes.FLOAT:
                case JavaTypes.FLOAT_OBJ:
                    return new Float(str);

                case JavaTypes.INT:
                case JavaTypes.INT_OBJ:
                    return new Integer(str);

                case JavaTypes.LONG:
                case JavaTypes.LONG_OBJ:
                    return new Long(str);

                case JavaTypes.SHORT:
                case JavaTypes.SHORT_OBJ:
                    return new Short(str);

                case JavaTypes.NUMBER:
                case JavaTypes.BIGDECIMAL:
                    return new BigDecimal(str);

                case JavaTypes.BIGINTEGER:
                    return new BigInteger(str);

                case JavaTypes.STRING:
                    // strip quotes
                    str = str.substring(1, str.length() - 1);
                    return XMLEncoder.decode(str);

                case JavaTypes.OBJECT:
                case JavaTypes.OID:
                    // convert the chars into bytes, and run them through an 
                    // ObjectInputStream in order to get the serialized object
                    byte[] bytes = Base16Encoder.decode(str);
                    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    Object data = ois.readObject();
                    ois.close();
                    return data;

                case JavaTypes.DATE:
                    return new Date(str);

                case JavaTypes.PC:
                case JavaTypes.PC_UNTYPED:
                    // parse out oid class name and value
                    int idx = str.indexOf(':');
                    Class idClass = classForName(str.substring(0, idx));
                    String idStr = XMLEncoder.decode(str.substring(idx + 1));
                    Constructor cons = idClass.getConstructor(ARGS);
                    return cons.newInstance(new Object[]{ idStr });

                case JavaTypes.LOCALE:
                    int under1 = str.indexOf('_');
                    if (under1 == -1)
                        return (new Locale(str, ""));

                    int under2 = str.indexOf('_', under1 + 1);
                    if (under2 == -1)
                        return new Locale(str.substring(0, under1),
                            str.substring(under1 + 1));

                    String lang = str.substring(0, under1);
                    String country = str.substring(under1 + 1, under2);
                    String variant = str.substring(under2 + 1);
                    return new Locale(lang, country, variant);

                default:
                    throw new InternalException();
            }
        }

        /**
         * Return the class for the specified name.
         */
        private Class classForName(String name)
            throws Exception {
            ClassLoader loader = _conf.getClassResolverInstance().
                getClassLoader(getClass(), null);
            return Class.forName(name, true, loader);
        }
    }

    /**
     * Utility methods for encoding and decoding XML strings.
     */
    private static class XMLEncoder {

        /**
         * Encode the given string as XML text.
         */
        public static String encode(String s) {
            StringBuffer buf = null;
            for (int i = 0; i < s.length(); i++) {
                switch (s.charAt(i)) {
                    case '<':
                        buf = initializeBuffer(buf, s, i);
                        buf.append("&lt;");
                        break;
                    case '>':
                        buf = initializeBuffer(buf, s, i);
                        buf.append("&gt;");
                        break;
                    case '&':
                        buf = initializeBuffer(buf, s, i);
                        buf.append("&amp;");
                        break;
                    default:
                        if (buf != null)
                            buf.append(s.charAt(i));
                }
            }
            if (buf != null)
                return buf.toString();
            return s;
        }

        /**
         * Decode the given XML string.
         */
        public static String decode(String s) {
            StringBuffer buf = null;
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '&' && s.length() > i + 3) {
                    if ((s.charAt(i + 1) == 'l' || s.charAt(i + 1) == 'g')
                        && s.charAt(i + 2) == 't' && s.charAt(i + 3) == ';') {
                        // &lt; or &gt;
                        buf = initializeBuffer(buf, s, i);
                        c = (s.charAt(i) == 'l') ? '<' : '>';
                        i += 3;
                    } else if (s.length() > i + 4 && s.charAt(i + 1) == 'a'
                        && s.charAt(i + 2) == 'm' && s.charAt(i + 3) == 'p'
                        && s.charAt(i + 4) == ';') {
                        // &amp;
                        buf = initializeBuffer(buf, s, i);
                        c = '&';
                        i += 4;
                    }
                }
                if (buf != null)
                    buf.append(c);
            }
            if (buf != null)
                return buf.toString();
            return s;
        }

        /**
         * Create and initialize a buffer for the encoded/decoded string if
         * needed.
         */
        private static StringBuffer initializeBuffer(StringBuffer buf,
            String s, int i) {
            if (buf == null) {
                buf = new StringBuffer();
                if (i > 0)
                    buf.append (s.substring (0, i));
			}
			return buf;
		}
	}
}
