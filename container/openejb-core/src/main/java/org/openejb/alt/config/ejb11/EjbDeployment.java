/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: EjbDeployment.java,v 1.2 2004/03/31 00:44:02 dblevins Exp $
 */

package org.openejb.alt.config.ejb11;


//---------------------------------/

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Vector;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.ContentHandler;

public class EjbDeployment implements java.io.Serializable {




    private java.lang.String _ejbName;

    private java.lang.String _deploymentId;

    private java.lang.String _containerId;

    private java.util.Vector _resourceLinkList;

    private java.util.Vector _queryList;




    public EjbDeployment() {
        super();
        _resourceLinkList = new Vector();
        _queryList = new Vector();
    }




    public void addQuery(org.openejb.alt.config.ejb11.Query vQuery)
        throws java.lang.IndexOutOfBoundsException
    {
        _queryList.addElement(vQuery);
    }

    public void addQuery(int index, org.openejb.alt.config.ejb11.Query vQuery)
        throws java.lang.IndexOutOfBoundsException
    {
        _queryList.insertElementAt(vQuery, index);
    }

    public void addResourceLink(org.openejb.alt.config.ejb11.ResourceLink vResourceLink)
        throws java.lang.IndexOutOfBoundsException
    {
        _resourceLinkList.addElement(vResourceLink);
    }

    public void addResourceLink(int index, org.openejb.alt.config.ejb11.ResourceLink vResourceLink)
        throws java.lang.IndexOutOfBoundsException
    {
        _resourceLinkList.insertElementAt(vResourceLink, index);
    }

    public java.util.Enumeration enumerateQuery()
    {
        return _queryList.elements();
    }

    public java.util.Enumeration enumerateResourceLink()
    {
        return _resourceLinkList.elements();
    }

    public java.lang.String getContainerId()
    {
        return this._containerId;
    }

    public java.lang.String getDeploymentId()
    {
        return this._deploymentId;
    }

    public java.lang.String getEjbName()
    {
        return this._ejbName;
    }

    public org.openejb.alt.config.ejb11.Query getQuery(int index)
        throws java.lang.IndexOutOfBoundsException
    {

        if ((index < 0) || (index > _queryList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (org.openejb.alt.config.ejb11.Query) _queryList.elementAt(index);
    }

    public org.openejb.alt.config.ejb11.Query[] getQuery()
    {
        int size = _queryList.size();
        org.openejb.alt.config.ejb11.Query[] mArray = new org.openejb.alt.config.ejb11.Query[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.openejb.alt.config.ejb11.Query) _queryList.elementAt(index);
        }
        return mArray;
    }

    public int getQueryCount()
    {
        return _queryList.size();
    }

    public org.openejb.alt.config.ejb11.ResourceLink getResourceLink(int index)
        throws java.lang.IndexOutOfBoundsException
    {

        if ((index < 0) || (index > _resourceLinkList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (org.openejb.alt.config.ejb11.ResourceLink) _resourceLinkList.elementAt(index);
    }

    public org.openejb.alt.config.ejb11.ResourceLink[] getResourceLink()
    {
        int size = _resourceLinkList.size();
        org.openejb.alt.config.ejb11.ResourceLink[] mArray = new org.openejb.alt.config.ejb11.ResourceLink[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.openejb.alt.config.ejb11.ResourceLink) _resourceLinkList.elementAt(index);
        }
        return mArray;
    }

    public int getResourceLinkCount()
    {
        return _resourceLinkList.size();
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

    public void removeAllQuery()
    {
        _queryList.removeAllElements();
    }

    public void removeAllResourceLink()
    {
        _resourceLinkList.removeAllElements();
    }

    public org.openejb.alt.config.ejb11.Query removeQuery(int index)
    {
        java.lang.Object obj = _queryList.elementAt(index);
        _queryList.removeElementAt(index);
        return (org.openejb.alt.config.ejb11.Query) obj;
    }

    public org.openejb.alt.config.ejb11.ResourceLink removeResourceLink(int index)
    {
        java.lang.Object obj = _resourceLinkList.elementAt(index);
        _resourceLinkList.removeElementAt(index);
        return (org.openejb.alt.config.ejb11.ResourceLink) obj;
    }

    public void setContainerId(java.lang.String containerId)
    {
        this._containerId = containerId;
    }

    public void setDeploymentId(java.lang.String deploymentId)
    {
        this._deploymentId = deploymentId;
    }

    public void setEjbName(java.lang.String ejbName)
    {
        this._ejbName = ejbName;
    }

    public void setQuery(int index, org.openejb.alt.config.ejb11.Query vQuery)
        throws java.lang.IndexOutOfBoundsException
    {

        if ((index < 0) || (index > _queryList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _queryList.setElementAt(vQuery, index);
    }

    public void setQuery(org.openejb.alt.config.ejb11.Query[] queryArray)
    {

        _queryList.removeAllElements();
        for (int i = 0; i < queryArray.length; i++) {
            _queryList.addElement(queryArray[i]);
        }
    }

    public void setResourceLink(int index, org.openejb.alt.config.ejb11.ResourceLink vResourceLink)
        throws java.lang.IndexOutOfBoundsException
    {

        if ((index < 0) || (index > _resourceLinkList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _resourceLinkList.setElementAt(vResourceLink, index);
    }

    public void setResourceLink(org.openejb.alt.config.ejb11.ResourceLink[] resourceLinkArray)
    {

        _resourceLinkList.removeAllElements();
        for (int i = 0; i < resourceLinkArray.length; i++) {
            _resourceLinkList.addElement(resourceLinkArray[i]);
        }
    }

    public static java.lang.Object unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.ejb11.EjbDeployment) Unmarshaller.unmarshal(org.openejb.alt.config.ejb11.EjbDeployment.class, reader);
    }

    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}
