/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id$
 */

package org.openejb.alt.config.sys;

//---------------------------------/

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.ContentHandler;

public class ProxyFactory implements java.io.Serializable, org.openejb.alt.config.Service {


    private java.lang.String _id;

    private java.lang.String _provider;

    private java.lang.String _jar;

    private java.lang.String _content = "";


    public ProxyFactory() {
        super();
        setContent("");
    }


    public java.lang.String getContent() {
        return this._content;
    }

    public java.lang.String getId() {
        return this._id;
    }

    public java.lang.String getJar() {
        return this._jar;
    }

    public java.lang.String getProvider() {
        return this._provider;
    }

    public boolean isValid() {
        try {
            validate();
        }
        catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    }

    public void marshal(java.io.Writer out)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {

        Marshaller.marshal(this, out);
    }

    public void marshal(org.xml.sax.ContentHandler handler)
            throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {

        Marshaller.marshal(this, handler);
    }

    public void setContent(java.lang.String content) {
        this._content = content;
    }

    public void setId(java.lang.String id) {
        this._id = id;
    }

    public void setJar(java.lang.String jar) {
        this._jar = jar;
    }

    public void setProvider(java.lang.String provider) {
        this._provider = provider;
    }

    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.openejb.alt.config.sys.ProxyFactory) Unmarshaller.unmarshal(org.openejb.alt.config.sys.ProxyFactory.class, reader);
    }

    public void validate()
            throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}
