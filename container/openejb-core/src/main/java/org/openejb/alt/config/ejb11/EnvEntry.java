/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: EnvEntry.java,v 1.2 2004/03/31 00:44:02 dblevins Exp $
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

public class EnvEntry implements java.io.Serializable {




    private java.lang.String _id;

    private java.lang.String _description;

    private java.lang.String _envEntryName;

    private java.lang.String _envEntryType;

    private java.lang.String _envEntryValue;




    public EnvEntry() {
        super();
    }




    public java.lang.String getDescription()
    {
        return this._description;
    }

    public java.lang.String getEnvEntryName()
    {
        return this._envEntryName;
    }

    public java.lang.String getEnvEntryType()
    {
        return this._envEntryType;
    }

    public java.lang.String getEnvEntryValue()
    {
        return this._envEntryValue;
    }

    public java.lang.String getId()
    {
        return this._id;
    }

    public boolean isValid()
    {
        try {
            validate();
        }
        catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    }

    public void marshal(java.io.Writer out)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, out);
    }

    public void marshal(org.xml.sax.ContentHandler handler)
        throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, handler);
    }

    public void setDescription(java.lang.String description)
    {
        this._description = description;
    }

    public void setEnvEntryName(java.lang.String envEntryName)
    {
        this._envEntryName = envEntryName;
    }

    public void setEnvEntryType(java.lang.String envEntryType)
    {
        this._envEntryType = envEntryType;
    }

    public void setEnvEntryValue(java.lang.String envEntryValue)
    {
        this._envEntryValue = envEntryValue;
    }

    public void setId(java.lang.String id)
    {
        this._id = id;
    }

    public static java.lang.Object unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.ejb11.EnvEntry) Unmarshaller.unmarshal(org.openejb.alt.config.ejb11.EnvEntry.class, reader);
    }

    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}
