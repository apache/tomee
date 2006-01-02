/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: ContainerTransaction.java,v 1.2 2004/03/31 00:44:02 dblevins Exp $
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

public class ContainerTransaction implements java.io.Serializable {


    private java.lang.String _id;

    private java.lang.String _description;

    private java.util.Vector _methodList;

    private java.lang.String _transAttribute;


    public ContainerTransaction() {
        super();
        _methodList = new Vector();
    }


    public void addMethod(org.openejb.alt.config.ejb11.Method vMethod)
            throws java.lang.IndexOutOfBoundsException {
        _methodList.addElement(vMethod);
    }

    public void addMethod(int index, org.openejb.alt.config.ejb11.Method vMethod)
            throws java.lang.IndexOutOfBoundsException {
        _methodList.insertElementAt(vMethod, index);
    }

    public java.util.Enumeration enumerateMethod() {
        return _methodList.elements();
    }

    public java.lang.String getDescription() {
        return this._description;
    }

    public java.lang.String getId() {
        return this._id;
    }

    public org.openejb.alt.config.ejb11.Method getMethod(int index)
            throws java.lang.IndexOutOfBoundsException {

        if ((index < 0) || (index > _methodList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.openejb.alt.config.ejb11.Method) _methodList.elementAt(index);
    }

    public org.openejb.alt.config.ejb11.Method[] getMethod() {
        int size = _methodList.size();
        org.openejb.alt.config.ejb11.Method[] mArray = new org.openejb.alt.config.ejb11.Method[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.openejb.alt.config.ejb11.Method) _methodList.elementAt(index);
        }
        return mArray;
    }

    public int getMethodCount() {
        return _methodList.size();
    }

    public java.lang.String getTransAttribute() {
        return this._transAttribute;
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

    public void removeAllMethod() {
        _methodList.removeAllElements();
    }

    public org.openejb.alt.config.ejb11.Method removeMethod(int index) {
        java.lang.Object obj = _methodList.elementAt(index);
        _methodList.removeElementAt(index);
        return (org.openejb.alt.config.ejb11.Method) obj;
    }

    public void setDescription(java.lang.String description) {
        this._description = description;
    }

    public void setId(java.lang.String id) {
        this._id = id;
    }

    public void setMethod(int index, org.openejb.alt.config.ejb11.Method vMethod)
            throws java.lang.IndexOutOfBoundsException {

        if ((index < 0) || (index > _methodList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _methodList.setElementAt(vMethod, index);
    }

    public void setMethod(org.openejb.alt.config.ejb11.Method[] methodArray) {

        _methodList.removeAllElements();
        for (int i = 0; i < methodArray.length; i++) {
            _methodList.addElement(methodArray[i]);
        }
    }

    public void setTransAttribute(java.lang.String transAttribute) {
        this._transAttribute = transAttribute;
    }

    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.openejb.alt.config.ejb11.ContainerTransaction) Unmarshaller.unmarshal(org.openejb.alt.config.ejb11.ContainerTransaction.class, reader);
    }

    public void validate()
            throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}
