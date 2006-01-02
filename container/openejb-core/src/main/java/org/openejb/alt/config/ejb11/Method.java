/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: Method.java,v 1.2 2004/03/31 00:44:02 dblevins Exp $
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

public class Method implements java.io.Serializable {




    private java.lang.String _id;

    private java.lang.String _description;

    private java.lang.String _ejbName;

    private java.lang.String _methodIntf;

    private java.lang.String _methodName;

    private org.openejb.alt.config.ejb11.MethodParams _methodParams;




    public Method() {
        super();
    }




    public java.lang.String getDescription()
    {
        return this._description;
    }

    public java.lang.String getEjbName()
    {
        return this._ejbName;
    }

    public java.lang.String getId()
    {
        return this._id;
    }

    public java.lang.String getMethodIntf()
    {
        return this._methodIntf;
    }

    public java.lang.String getMethodName()
    {
        return this._methodName;
    }

    public org.openejb.alt.config.ejb11.MethodParams getMethodParams()
    {
        return this._methodParams;
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

    public void setEjbName(java.lang.String ejbName)
    {
        this._ejbName = ejbName;
    }

    public void setId(java.lang.String id)
    {
        this._id = id;
    }

    public void setMethodIntf(java.lang.String methodIntf)
    {
        this._methodIntf = methodIntf;
    }

    public void setMethodName(java.lang.String methodName)
    {
        this._methodName = methodName;
    }

    public void setMethodParams(org.openejb.alt.config.ejb11.MethodParams methodParams)
    {
        this._methodParams = methodParams;
    }

    public static java.lang.Object unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.ejb11.Method) Unmarshaller.unmarshal(org.openejb.alt.config.ejb11.Method.class, reader);
    }

    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}
