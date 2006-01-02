/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: Session.java,v 1.3 2004/08/14 10:35:36 dblevins Exp $
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

public class Session implements java.io.Serializable {




    private java.lang.String _id;

    private java.lang.String _description;

    private java.lang.String _displayName;

    private java.lang.String _smallIcon;

    private java.lang.String _largeIcon;

    private java.lang.String _ejbName;

    private java.lang.String _home;

    private java.lang.String _remote;

    private java.lang.String _localHome;

    private java.lang.String _local;

    private java.lang.String _ejbClass;

    private java.lang.String _sessionType;

    private java.lang.String _transactionType;

    private java.util.Vector _envEntryList;

    private java.util.Vector _ejbRefList;

    private java.util.Vector _ejbLocalRefList;

    private java.util.Vector _securityRoleRefList;

    private java.util.Vector _resourceRefList;




    public Session() {
        super();
        _envEntryList = new Vector();
        _ejbRefList = new Vector();
        _ejbLocalRefList = new Vector();
        _securityRoleRefList = new Vector();
        _resourceRefList = new Vector();
    }




    public void addEjbLocalRef(org.openejb.alt.config.ejb11.EjbLocalRef vEjbLocalRef)
        throws java.lang.IndexOutOfBoundsException
    {
        _ejbLocalRefList.addElement(vEjbLocalRef);
    }

    public void addEjbLocalRef(int index, org.openejb.alt.config.ejb11.EjbLocalRef vEjbLocalRef)
        throws java.lang.IndexOutOfBoundsException
    {
        _ejbLocalRefList.insertElementAt(vEjbLocalRef, index);
    }

    public void addEjbRef(org.openejb.alt.config.ejb11.EjbRef vEjbRef)
        throws java.lang.IndexOutOfBoundsException
    {
        _ejbRefList.addElement(vEjbRef);
    }

    public void addEjbRef(int index, org.openejb.alt.config.ejb11.EjbRef vEjbRef)
        throws java.lang.IndexOutOfBoundsException
    {
        _ejbRefList.insertElementAt(vEjbRef, index);
    }

    public void addEnvEntry(org.openejb.alt.config.ejb11.EnvEntry vEnvEntry)
        throws java.lang.IndexOutOfBoundsException
    {
        _envEntryList.addElement(vEnvEntry);
    }

    public void addEnvEntry(int index, org.openejb.alt.config.ejb11.EnvEntry vEnvEntry)
        throws java.lang.IndexOutOfBoundsException
    {
        _envEntryList.insertElementAt(vEnvEntry, index);
    }

    public void addResourceRef(org.openejb.alt.config.ejb11.ResourceRef vResourceRef)
        throws java.lang.IndexOutOfBoundsException
    {
        _resourceRefList.addElement(vResourceRef);
    }

    public void addResourceRef(int index, org.openejb.alt.config.ejb11.ResourceRef vResourceRef)
        throws java.lang.IndexOutOfBoundsException
    {
        _resourceRefList.insertElementAt(vResourceRef, index);
    }

    public void addSecurityRoleRef(org.openejb.alt.config.ejb11.SecurityRoleRef vSecurityRoleRef)
        throws java.lang.IndexOutOfBoundsException
    {
        _securityRoleRefList.addElement(vSecurityRoleRef);
    }

    public void addSecurityRoleRef(int index, org.openejb.alt.config.ejb11.SecurityRoleRef vSecurityRoleRef)
        throws java.lang.IndexOutOfBoundsException
    {
        _securityRoleRefList.insertElementAt(vSecurityRoleRef, index);
    }

    public java.util.Enumeration enumerateEjbLocalRef()
    {
        return _ejbLocalRefList.elements();
    }

    public java.util.Enumeration enumerateEjbRef()
    {
        return _ejbRefList.elements();
    }

    public java.util.Enumeration enumerateEnvEntry()
    {
        return _envEntryList.elements();
    }

    public java.util.Enumeration enumerateResourceRef()
    {
        return _resourceRefList.elements();
    }

    public java.util.Enumeration enumerateSecurityRoleRef()
    {
        return _securityRoleRefList.elements();
    }

    public java.lang.String getDescription()
    {
        return this._description;
    }

    public java.lang.String getDisplayName()
    {
        return this._displayName;
    }

    public java.lang.String getEjbClass()
    {
        return this._ejbClass;
    }

    public org.openejb.alt.config.ejb11.EjbLocalRef getEjbLocalRef(int index)
        throws java.lang.IndexOutOfBoundsException
    {

        if ((index < 0) || (index > _ejbLocalRefList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (org.openejb.alt.config.ejb11.EjbLocalRef) _ejbLocalRefList.elementAt(index);
    }

    public org.openejb.alt.config.ejb11.EjbLocalRef[] getEjbLocalRef()
    {
        int size = _ejbLocalRefList.size();
        org.openejb.alt.config.ejb11.EjbLocalRef[] mArray = new org.openejb.alt.config.ejb11.EjbLocalRef[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.openejb.alt.config.ejb11.EjbLocalRef) _ejbLocalRefList.elementAt(index);
        }
        return mArray;
    }

    public int getEjbLocalRefCount()
    {
        return _ejbLocalRefList.size();
    }

    public java.lang.String getEjbName()
    {
        return this._ejbName;
    }

    public org.openejb.alt.config.ejb11.EjbRef getEjbRef(int index)
        throws java.lang.IndexOutOfBoundsException
    {

        if ((index < 0) || (index > _ejbRefList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (org.openejb.alt.config.ejb11.EjbRef) _ejbRefList.elementAt(index);
    }

    public org.openejb.alt.config.ejb11.EjbRef[] getEjbRef()
    {
        int size = _ejbRefList.size();
        org.openejb.alt.config.ejb11.EjbRef[] mArray = new org.openejb.alt.config.ejb11.EjbRef[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.openejb.alt.config.ejb11.EjbRef) _ejbRefList.elementAt(index);
        }
        return mArray;
    }

    public int getEjbRefCount()
    {
        return _ejbRefList.size();
    }

    public org.openejb.alt.config.ejb11.EnvEntry getEnvEntry(int index)
        throws java.lang.IndexOutOfBoundsException
    {

        if ((index < 0) || (index > _envEntryList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (org.openejb.alt.config.ejb11.EnvEntry) _envEntryList.elementAt(index);
    }

    public org.openejb.alt.config.ejb11.EnvEntry[] getEnvEntry()
    {
        int size = _envEntryList.size();
        org.openejb.alt.config.ejb11.EnvEntry[] mArray = new org.openejb.alt.config.ejb11.EnvEntry[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.openejb.alt.config.ejb11.EnvEntry) _envEntryList.elementAt(index);
        }
        return mArray;
    }

    public int getEnvEntryCount()
    {
        return _envEntryList.size();
    }

    public java.lang.String getHome()
    {
        return this._home;
    }

    public java.lang.String getId()
    {
        return this._id;
    }

    public java.lang.String getLargeIcon()
    {
        return this._largeIcon;
    }

    public java.lang.String getLocal()
    {
        return this._local;
    }

    public java.lang.String getLocalHome()
    {
        return this._localHome;
    }

    public java.lang.String getRemote()
    {
        return this._remote;
    }

    public org.openejb.alt.config.ejb11.ResourceRef getResourceRef(int index)
        throws java.lang.IndexOutOfBoundsException
    {

        if ((index < 0) || (index > _resourceRefList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (org.openejb.alt.config.ejb11.ResourceRef) _resourceRefList.elementAt(index);
    }

    public org.openejb.alt.config.ejb11.ResourceRef[] getResourceRef()
    {
        int size = _resourceRefList.size();
        org.openejb.alt.config.ejb11.ResourceRef[] mArray = new org.openejb.alt.config.ejb11.ResourceRef[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.openejb.alt.config.ejb11.ResourceRef) _resourceRefList.elementAt(index);
        }
        return mArray;
    }

    public int getResourceRefCount()
    {
        return _resourceRefList.size();
    }

    public org.openejb.alt.config.ejb11.SecurityRoleRef getSecurityRoleRef(int index)
        throws java.lang.IndexOutOfBoundsException
    {

        if ((index < 0) || (index > _securityRoleRefList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (org.openejb.alt.config.ejb11.SecurityRoleRef) _securityRoleRefList.elementAt(index);
    }

    public org.openejb.alt.config.ejb11.SecurityRoleRef[] getSecurityRoleRef()
    {
        int size = _securityRoleRefList.size();
        org.openejb.alt.config.ejb11.SecurityRoleRef[] mArray = new org.openejb.alt.config.ejb11.SecurityRoleRef[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.openejb.alt.config.ejb11.SecurityRoleRef) _securityRoleRefList.elementAt(index);
        }
        return mArray;
    }

    public int getSecurityRoleRefCount()
    {
        return _securityRoleRefList.size();
    }

    public java.lang.String getSessionType()
    {
        return this._sessionType;
    }

    public java.lang.String getSmallIcon()
    {
        return this._smallIcon;
    }

    public java.lang.String getTransactionType()
    {
        return this._transactionType;
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

    public void removeAllEjbLocalRef()
    {
        _ejbLocalRefList.removeAllElements();
    }

    public void removeAllEjbRef()
    {
        _ejbRefList.removeAllElements();
    }

    public void removeAllEnvEntry()
    {
        _envEntryList.removeAllElements();
    }

    public void removeAllResourceRef()
    {
        _resourceRefList.removeAllElements();
    }

    public void removeAllSecurityRoleRef()
    {
        _securityRoleRefList.removeAllElements();
    }

    public org.openejb.alt.config.ejb11.EjbLocalRef removeEjbLocalRef(int index)
    {
        java.lang.Object obj = _ejbLocalRefList.elementAt(index);
        _ejbLocalRefList.removeElementAt(index);
        return (org.openejb.alt.config.ejb11.EjbLocalRef) obj;
    }

    public org.openejb.alt.config.ejb11.EjbRef removeEjbRef(int index)
    {
        java.lang.Object obj = _ejbRefList.elementAt(index);
        _ejbRefList.removeElementAt(index);
        return (org.openejb.alt.config.ejb11.EjbRef) obj;
    }

    public org.openejb.alt.config.ejb11.EnvEntry removeEnvEntry(int index)
    {
        java.lang.Object obj = _envEntryList.elementAt(index);
        _envEntryList.removeElementAt(index);
        return (org.openejb.alt.config.ejb11.EnvEntry) obj;
    }

    public org.openejb.alt.config.ejb11.ResourceRef removeResourceRef(int index)
    {
        java.lang.Object obj = _resourceRefList.elementAt(index);
        _resourceRefList.removeElementAt(index);
        return (org.openejb.alt.config.ejb11.ResourceRef) obj;
    }

    public org.openejb.alt.config.ejb11.SecurityRoleRef removeSecurityRoleRef(int index)
    {
        java.lang.Object obj = _securityRoleRefList.elementAt(index);
        _securityRoleRefList.removeElementAt(index);
        return (org.openejb.alt.config.ejb11.SecurityRoleRef) obj;
    }

    public void setDescription(java.lang.String description)
    {
        this._description = description;
    }

    public void setDisplayName(java.lang.String displayName)
    {
        this._displayName = displayName;
    }

    public void setEjbClass(java.lang.String ejbClass)
    {
        this._ejbClass = ejbClass;
    }

    public void setEjbLocalRef(int index, org.openejb.alt.config.ejb11.EjbLocalRef vEjbLocalRef)
        throws java.lang.IndexOutOfBoundsException
    {

        if ((index < 0) || (index > _ejbLocalRefList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _ejbLocalRefList.setElementAt(vEjbLocalRef, index);
    }

    public void setEjbLocalRef(org.openejb.alt.config.ejb11.EjbLocalRef[] ejbLocalRefArray)
    {

        _ejbLocalRefList.removeAllElements();
        for (int i = 0; i < ejbLocalRefArray.length; i++) {
            _ejbLocalRefList.addElement(ejbLocalRefArray[i]);
        }
    }

    public void setEjbName(java.lang.String ejbName)
    {
        this._ejbName = ejbName;
    }

    public void setEjbRef(int index, org.openejb.alt.config.ejb11.EjbRef vEjbRef)
        throws java.lang.IndexOutOfBoundsException
    {

        if ((index < 0) || (index > _ejbRefList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _ejbRefList.setElementAt(vEjbRef, index);
    }

    public void setEjbRef(org.openejb.alt.config.ejb11.EjbRef[] ejbRefArray)
    {

        _ejbRefList.removeAllElements();
        for (int i = 0; i < ejbRefArray.length; i++) {
            _ejbRefList.addElement(ejbRefArray[i]);
        }
    }

    public void setEnvEntry(int index, org.openejb.alt.config.ejb11.EnvEntry vEnvEntry)
        throws java.lang.IndexOutOfBoundsException
    {

        if ((index < 0) || (index > _envEntryList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _envEntryList.setElementAt(vEnvEntry, index);
    }

    public void setEnvEntry(org.openejb.alt.config.ejb11.EnvEntry[] envEntryArray)
    {

        _envEntryList.removeAllElements();
        for (int i = 0; i < envEntryArray.length; i++) {
            _envEntryList.addElement(envEntryArray[i]);
        }
    }

    public void setHome(java.lang.String home)
    {
        this._home = home;
    }

    public void setId(java.lang.String id)
    {
        this._id = id;
    }

    public void setLargeIcon(java.lang.String largeIcon)
    {
        this._largeIcon = largeIcon;
    }

    public void setLocal(java.lang.String local)
    {
        this._local = local;
    }

    public void setLocalHome(java.lang.String localHome)
    {
        this._localHome = localHome;
    }

    public void setRemote(java.lang.String remote)
    {
        this._remote = remote;
    }

    public void setResourceRef(int index, org.openejb.alt.config.ejb11.ResourceRef vResourceRef)
        throws java.lang.IndexOutOfBoundsException
    {

        if ((index < 0) || (index > _resourceRefList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _resourceRefList.setElementAt(vResourceRef, index);
    }

    public void setResourceRef(org.openejb.alt.config.ejb11.ResourceRef[] resourceRefArray)
    {

        _resourceRefList.removeAllElements();
        for (int i = 0; i < resourceRefArray.length; i++) {
            _resourceRefList.addElement(resourceRefArray[i]);
        }
    }

    public void setSecurityRoleRef(int index, org.openejb.alt.config.ejb11.SecurityRoleRef vSecurityRoleRef)
        throws java.lang.IndexOutOfBoundsException
    {

        if ((index < 0) || (index > _securityRoleRefList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _securityRoleRefList.setElementAt(vSecurityRoleRef, index);
    }

    public void setSecurityRoleRef(org.openejb.alt.config.ejb11.SecurityRoleRef[] securityRoleRefArray)
    {

        _securityRoleRefList.removeAllElements();
        for (int i = 0; i < securityRoleRefArray.length; i++) {
            _securityRoleRefList.addElement(securityRoleRefArray[i]);
        }
    }

    public void setSessionType(java.lang.String sessionType)
    {
        this._sessionType = sessionType;
    }

    public void setSmallIcon(java.lang.String smallIcon)
    {
        this._smallIcon = smallIcon;
    }

    public void setTransactionType(java.lang.String transactionType)
    {
        this._transactionType = transactionType;
    }

    public static java.lang.Object unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.ejb11.Session) Unmarshaller.unmarshal(org.openejb.alt.config.ejb11.Session.class, reader);
    }

    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}
