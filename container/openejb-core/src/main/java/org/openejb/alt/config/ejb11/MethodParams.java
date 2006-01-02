/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: MethodParams.java,v 1.2 2004/03/31 00:44:02 dblevins Exp $
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

public class MethodParams implements java.io.Serializable {


    private java.lang.String _id;

    private java.util.Vector _methodParamList;


    public MethodParams() {
        super();
        _methodParamList = new Vector();
    }


    public void addMethodParam(java.lang.String vMethodParam)
            throws java.lang.IndexOutOfBoundsException {
        _methodParamList.addElement(vMethodParam);
    }

    public void addMethodParam(int index, java.lang.String vMethodParam)
            throws java.lang.IndexOutOfBoundsException {
        _methodParamList.insertElementAt(vMethodParam, index);
    }

    public java.util.Enumeration enumerateMethodParam() {
        return _methodParamList.elements();
    }

    public java.lang.String getId() {
        return this._id;
    }

    public java.lang.String getMethodParam(int index)
            throws java.lang.IndexOutOfBoundsException {

        if ((index < 0) || (index > _methodParamList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (String) _methodParamList.elementAt(index);
    }

    public java.lang.String[] getMethodParam() {
        int size = _methodParamList.size();
        java.lang.String[] mArray = new java.lang.String[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (String) _methodParamList.elementAt(index);
        }
        return mArray;
    }

    public int getMethodParamCount() {
        return _methodParamList.size();
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

    public void removeAllMethodParam() {
        _methodParamList.removeAllElements();
    }

    public java.lang.String removeMethodParam(int index) {
        java.lang.Object obj = _methodParamList.elementAt(index);
        _methodParamList.removeElementAt(index);
        return (String) obj;
    }

    public void setId(java.lang.String id) {
        this._id = id;
    }

    public void setMethodParam(int index, java.lang.String vMethodParam)
            throws java.lang.IndexOutOfBoundsException {

        if ((index < 0) || (index > _methodParamList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _methodParamList.setElementAt(vMethodParam, index);
    }

    public void setMethodParam(java.lang.String[] methodParamArray) {

        _methodParamList.removeAllElements();
        for (int i = 0; i < methodParamArray.length; i++) {
            _methodParamList.addElement(methodParamArray[i]);
        }
    }

    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.openejb.alt.config.ejb11.MethodParams) Unmarshaller.unmarshal(org.openejb.alt.config.ejb11.MethodParams.class, reader);
    }

    public void validate()
            throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}
