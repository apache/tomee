/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: Query.java,v 1.2 2004/03/31 00:44:02 dblevins Exp $
 */

package org.openejb.alt.config.ejb11;

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

public class Query implements java.io.Serializable {


    private java.lang.String _description;

    private org.openejb.alt.config.ejb11.QueryMethod _queryMethod;

    private java.lang.String _objectQl;


    public Query() {
        super();
    }


    public java.lang.String getDescription() {
        return this._description;
    }

    public java.lang.String getObjectQl() {
        return this._objectQl;
    }

    public org.openejb.alt.config.ejb11.QueryMethod getQueryMethod() {
        return this._queryMethod;
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

    public void setDescription(java.lang.String description) {
        this._description = description;
    }

    public void setObjectQl(java.lang.String objectQl) {
        this._objectQl = objectQl;
    }

    public void setQueryMethod(org.openejb.alt.config.ejb11.QueryMethod queryMethod) {
        this._queryMethod = queryMethod;
    }

    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.openejb.alt.config.ejb11.Query) Unmarshaller.unmarshal(org.openejb.alt.config.ejb11.Query.class, reader);
    }

    public void validate()
            throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}
