/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: EjbLocalRef.java,v 1.1 2004/08/14 10:35:36 dblevins Exp $
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

public class EjbLocalRef implements java.io.Serializable {




    private java.lang.String _id;

    private java.lang.String _description;

    private java.lang.String _ejbRefName;

    private java.lang.String _ejbRefType;

    private java.lang.String _localHome;

    private java.lang.String _local;

    private java.lang.String _ejbLink;




    public EjbLocalRef() {
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

    public java.lang.String getId()
    {
        return this._id;
    }

    public java.lang.String getLocal()
    {
        return this._local;
    }

    public java.lang.String getLocalHome()
    {
        return this._localHome;
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

    public void setId(java.lang.String id)
    {
        this._id = id;
    }

    public void setLocal(java.lang.String local)
    {
        this._local = local;
    }

    public void setLocalHome(java.lang.String localHome)
    {
        this._localHome = localHome;
    }

    public static java.lang.Object unmarshalEjbLocalRef(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.ejb11.EjbLocalRef) Unmarshaller.unmarshal(org.openejb.alt.config.ejb11.EjbLocalRef.class, reader);
    }

    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}
