/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: AssemblyDescriptor.java,v 1.2 2004/03/31 00:44:02 dblevins Exp $
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

public class AssemblyDescriptor implements java.io.Serializable {




    private java.lang.String _id;

    private java.util.Vector _securityRoleList;

    private java.util.Vector _methodPermissionList;

    private java.util.Vector _containerTransactionList;




    public AssemblyDescriptor() {
        super();
        _securityRoleList = new Vector();
        _methodPermissionList = new Vector();
        _containerTransactionList = new Vector();
    }




    public void addContainerTransaction(org.openejb.alt.config.ejb11.ContainerTransaction vContainerTransaction)
        throws java.lang.IndexOutOfBoundsException
    {
        _containerTransactionList.addElement(vContainerTransaction);
    }

    public void addContainerTransaction(int index, org.openejb.alt.config.ejb11.ContainerTransaction vContainerTransaction)
        throws java.lang.IndexOutOfBoundsException
    {
        _containerTransactionList.insertElementAt(vContainerTransaction, index);
    }

    public void addMethodPermission(org.openejb.alt.config.ejb11.MethodPermission vMethodPermission)
        throws java.lang.IndexOutOfBoundsException
    {
        _methodPermissionList.addElement(vMethodPermission);
    }

    public void addMethodPermission(int index, org.openejb.alt.config.ejb11.MethodPermission vMethodPermission)
        throws java.lang.IndexOutOfBoundsException
    {
        _methodPermissionList.insertElementAt(vMethodPermission, index);
    }

    public void addSecurityRole(org.openejb.alt.config.ejb11.SecurityRole vSecurityRole)
        throws java.lang.IndexOutOfBoundsException
    {
        _securityRoleList.addElement(vSecurityRole);
    }

    public void addSecurityRole(int index, org.openejb.alt.config.ejb11.SecurityRole vSecurityRole)
        throws java.lang.IndexOutOfBoundsException
    {
        _securityRoleList.insertElementAt(vSecurityRole, index);
    }

    public java.util.Enumeration enumerateContainerTransaction()
    {
        return _containerTransactionList.elements();
    }

    public java.util.Enumeration enumerateMethodPermission()
    {
        return _methodPermissionList.elements();
    }

    public java.util.Enumeration enumerateSecurityRole()
    {
        return _securityRoleList.elements();
    }

    public org.openejb.alt.config.ejb11.ContainerTransaction getContainerTransaction(int index)
        throws java.lang.IndexOutOfBoundsException
    {

        if ((index < 0) || (index > _containerTransactionList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (org.openejb.alt.config.ejb11.ContainerTransaction) _containerTransactionList.elementAt(index);
    }

    public org.openejb.alt.config.ejb11.ContainerTransaction[] getContainerTransaction()
    {
        int size = _containerTransactionList.size();
        org.openejb.alt.config.ejb11.ContainerTransaction[] mArray = new org.openejb.alt.config.ejb11.ContainerTransaction[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.openejb.alt.config.ejb11.ContainerTransaction) _containerTransactionList.elementAt(index);
        }
        return mArray;
    }

    public int getContainerTransactionCount()
    {
        return _containerTransactionList.size();
    }

    public java.lang.String getId()
    {
        return this._id;
    }

    public org.openejb.alt.config.ejb11.MethodPermission getMethodPermission(int index)
        throws java.lang.IndexOutOfBoundsException
    {

        if ((index < 0) || (index > _methodPermissionList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (org.openejb.alt.config.ejb11.MethodPermission) _methodPermissionList.elementAt(index);
    }

    public org.openejb.alt.config.ejb11.MethodPermission[] getMethodPermission()
    {
        int size = _methodPermissionList.size();
        org.openejb.alt.config.ejb11.MethodPermission[] mArray = new org.openejb.alt.config.ejb11.MethodPermission[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.openejb.alt.config.ejb11.MethodPermission) _methodPermissionList.elementAt(index);
        }
        return mArray;
    }

    public int getMethodPermissionCount()
    {
        return _methodPermissionList.size();
    }

    public org.openejb.alt.config.ejb11.SecurityRole getSecurityRole(int index)
        throws java.lang.IndexOutOfBoundsException
    {

        if ((index < 0) || (index > _securityRoleList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (org.openejb.alt.config.ejb11.SecurityRole) _securityRoleList.elementAt(index);
    }

    public org.openejb.alt.config.ejb11.SecurityRole[] getSecurityRole()
    {
        int size = _securityRoleList.size();
        org.openejb.alt.config.ejb11.SecurityRole[] mArray = new org.openejb.alt.config.ejb11.SecurityRole[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.openejb.alt.config.ejb11.SecurityRole) _securityRoleList.elementAt(index);
        }
        return mArray;
    }

    public int getSecurityRoleCount()
    {
        return _securityRoleList.size();
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

    public void removeAllContainerTransaction()
    {
        _containerTransactionList.removeAllElements();
    }

    public void removeAllMethodPermission()
    {
        _methodPermissionList.removeAllElements();
    }

    public void removeAllSecurityRole()
    {
        _securityRoleList.removeAllElements();
    }

    public org.openejb.alt.config.ejb11.ContainerTransaction removeContainerTransaction(int index)
    {
        java.lang.Object obj = _containerTransactionList.elementAt(index);
        _containerTransactionList.removeElementAt(index);
        return (org.openejb.alt.config.ejb11.ContainerTransaction) obj;
    }

    public org.openejb.alt.config.ejb11.MethodPermission removeMethodPermission(int index)
    {
        java.lang.Object obj = _methodPermissionList.elementAt(index);
        _methodPermissionList.removeElementAt(index);
        return (org.openejb.alt.config.ejb11.MethodPermission) obj;
    }

    public org.openejb.alt.config.ejb11.SecurityRole removeSecurityRole(int index)
    {
        java.lang.Object obj = _securityRoleList.elementAt(index);
        _securityRoleList.removeElementAt(index);
        return (org.openejb.alt.config.ejb11.SecurityRole) obj;
    }

    public void setContainerTransaction(int index, org.openejb.alt.config.ejb11.ContainerTransaction vContainerTransaction)
        throws java.lang.IndexOutOfBoundsException
    {

        if ((index < 0) || (index > _containerTransactionList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _containerTransactionList.setElementAt(vContainerTransaction, index);
    }

    public void setContainerTransaction(org.openejb.alt.config.ejb11.ContainerTransaction[] containerTransactionArray)
    {

        _containerTransactionList.removeAllElements();
        for (int i = 0; i < containerTransactionArray.length; i++) {
            _containerTransactionList.addElement(containerTransactionArray[i]);
        }
    }

    public void setId(java.lang.String id)
    {
        this._id = id;
    }

    public void setMethodPermission(int index, org.openejb.alt.config.ejb11.MethodPermission vMethodPermission)
        throws java.lang.IndexOutOfBoundsException
    {

        if ((index < 0) || (index > _methodPermissionList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _methodPermissionList.setElementAt(vMethodPermission, index);
    }

    public void setMethodPermission(org.openejb.alt.config.ejb11.MethodPermission[] methodPermissionArray)
    {

        _methodPermissionList.removeAllElements();
        for (int i = 0; i < methodPermissionArray.length; i++) {
            _methodPermissionList.addElement(methodPermissionArray[i]);
        }
    }

    public void setSecurityRole(int index, org.openejb.alt.config.ejb11.SecurityRole vSecurityRole)
        throws java.lang.IndexOutOfBoundsException
    {

        if ((index < 0) || (index > _securityRoleList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _securityRoleList.setElementAt(vSecurityRole, index);
    }

    public void setSecurityRole(org.openejb.alt.config.ejb11.SecurityRole[] securityRoleArray)
    {

        _securityRoleList.removeAllElements();
        for (int i = 0; i < securityRoleArray.length; i++) {
            _securityRoleList.addElement(securityRoleArray[i]);
        }
    }

    public static java.lang.Object unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.ejb11.AssemblyDescriptor) Unmarshaller.unmarshal(org.openejb.alt.config.ejb11.AssemblyDescriptor.class, reader);
    }

    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}
