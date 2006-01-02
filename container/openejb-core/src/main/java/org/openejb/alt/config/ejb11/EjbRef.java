/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: EjbRef.java,v 1.2 2004/03/31 00:44:02 dblevins Exp $
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

public class EjbRef implements java.io.Serializable {




    private java.lang.String _id;

    private java.lang.String _description;

    private java.lang.String _ejbRefName;

    private java.lang.String _ejbRefType;

    private java.lang.String _home;

    private java.lang.String _remote;

    private java.lang.String _ejbLink;




    public EjbRef() {
        super();
    }




    public java.lang.String getDescription()
    {
        return this._description;
    }

    public java.lang.String getEjbLink()
    {
        return this._ejbLink;
    }

    public java.lang.String getEjbRefName()
    {
        return this._ejbRefName;
    }

    public java.lang.String getEjbRefType()
    {
        return this._ejbRefType;
    }

    public java.lang.String getHome()
    {
        return this._home;
    }

    public java.lang.String getId()
    {
        return this._id;
    }

    public java.lang.String getRemote()
    {
        return this._remote;
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

    public void setEjbLink(java.lang.String ejbLink)
    {
        this._ejbLink = ejbLink;
    }

    public void setEjbRefName(java.lang.String ejbRefName)
    {
        this._ejbRefName = ejbRefName;
    }

    public void setEjbRefType(java.lang.String ejbRefType)
    {
        this._ejbRefType = ejbRefType;
    }

    public void setHome(java.lang.String home)
    {
        this._home = home;
    }

    public void setId(java.lang.String id)
    {
        this._id = id;
    }

    public void setRemote(java.lang.String remote)
    {
        this._remote = remote;
    }

    public static java.lang.Object unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.ejb11.EjbRef) Unmarshaller.unmarshal(org.openejb.alt.config.ejb11.EjbRef.class, reader);
    }

    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}
