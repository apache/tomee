/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: EjbJar.java,v 1.2 2004/03/31 00:44:02 dblevins Exp $
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

public class EjbJar implements java.io.Serializable {




    private java.lang.String _id;

    private java.lang.String _description;

    private java.lang.String _displayName;

    private java.lang.String _smallIcon;

    private java.lang.String _largeIcon;

    private org.openejb.alt.config.ejb11.EnterpriseBeans _enterpriseBeans;

    private org.openejb.alt.config.ejb11.AssemblyDescriptor _assemblyDescriptor;

    private java.lang.String _ejbClientJar;




    public EjbJar() {
        super();
    }




    public org.openejb.alt.config.ejb11.AssemblyDescriptor getAssemblyDescriptor()
    {
        return this._assemblyDescriptor;
    }

    public java.lang.String getDescription()
    {
        return this._description;
    }

    public java.lang.String getDisplayName()
    {
        return this._displayName;
    }

    public java.lang.String getEjbClientJar()
    {
        return this._ejbClientJar;
    }

    public org.openejb.alt.config.ejb11.EnterpriseBeans getEnterpriseBeans()
    {
        return this._enterpriseBeans;
    }

    public java.lang.String getId()
    {
        return this._id;
    }

    public java.lang.String getLargeIcon()
    {
        return this._largeIcon;
    }

    public java.lang.String getSmallIcon()
    {
        return this._smallIcon;
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

    public void setAssemblyDescriptor(org.openejb.alt.config.ejb11.AssemblyDescriptor assemblyDescriptor)
    {
        this._assemblyDescriptor = assemblyDescriptor;
    }

    public void setDescription(java.lang.String description)
    {
        this._description = description;
    }

    public void setDisplayName(java.lang.String displayName)
    {
        this._displayName = displayName;
    }

    public void setEjbClientJar(java.lang.String ejbClientJar)
    {
        this._ejbClientJar = ejbClientJar;
    }

    public void setEnterpriseBeans(org.openejb.alt.config.ejb11.EnterpriseBeans enterpriseBeans)
    {
        this._enterpriseBeans = enterpriseBeans;
    }

    public void setId(java.lang.String id)
    {
        this._id = id;
    }

    public void setLargeIcon(java.lang.String largeIcon)
    {
        this._largeIcon = largeIcon;
    }

    public void setSmallIcon(java.lang.String smallIcon)
    {
        this._smallIcon = smallIcon;
    }

    public static java.lang.Object unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.ejb11.EjbJar) Unmarshaller.unmarshal(org.openejb.alt.config.ejb11.EjbJar.class, reader);
    }

    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}
