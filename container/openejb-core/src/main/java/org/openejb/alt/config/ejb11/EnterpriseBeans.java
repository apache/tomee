/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: EnterpriseBeans.java,v 1.2 2004/03/31 00:44:02 dblevins Exp $
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

public class EnterpriseBeans implements java.io.Serializable {


    private java.lang.String _id;

    private java.util.Vector _items;


    public EnterpriseBeans() {
        super();
        _items = new Vector();
    }


    public void addEnterpriseBeansItem(org.openejb.alt.config.ejb11.EnterpriseBeansItem vEnterpriseBeansItem)
            throws java.lang.IndexOutOfBoundsException {
        _items.addElement(vEnterpriseBeansItem);
    }

    public void addEnterpriseBeansItem(int index, org.openejb.alt.config.ejb11.EnterpriseBeansItem vEnterpriseBeansItem)
            throws java.lang.IndexOutOfBoundsException {
        _items.insertElementAt(vEnterpriseBeansItem, index);
    }

    public java.util.Enumeration enumerateEnterpriseBeansItem() {
        return _items.elements();
    }

    public org.openejb.alt.config.ejb11.EnterpriseBeansItem getEnterpriseBeansItem(int index)
            throws java.lang.IndexOutOfBoundsException {

        if ((index < 0) || (index > _items.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.openejb.alt.config.ejb11.EnterpriseBeansItem) _items.elementAt(index);
    }

    public org.openejb.alt.config.ejb11.EnterpriseBeansItem[] getEnterpriseBeansItem() {
        int size = _items.size();
        org.openejb.alt.config.ejb11.EnterpriseBeansItem[] mArray = new org.openejb.alt.config.ejb11.EnterpriseBeansItem[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.openejb.alt.config.ejb11.EnterpriseBeansItem) _items.elementAt(index);
        }
        return mArray;
    }

    public int getEnterpriseBeansItemCount() {
        return _items.size();
    }

    public java.lang.String getId() {
        return this._id;
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

    public void removeAllEnterpriseBeansItem() {
        _items.removeAllElements();
    }

    public org.openejb.alt.config.ejb11.EnterpriseBeansItem removeEnterpriseBeansItem(int index) {
        java.lang.Object obj = _items.elementAt(index);
        _items.removeElementAt(index);
        return (org.openejb.alt.config.ejb11.EnterpriseBeansItem) obj;
    }

    public void setEnterpriseBeansItem(int index, org.openejb.alt.config.ejb11.EnterpriseBeansItem vEnterpriseBeansItem)
            throws java.lang.IndexOutOfBoundsException {

        if ((index < 0) || (index > _items.size())) {
            throw new IndexOutOfBoundsException();
        }
        _items.setElementAt(vEnterpriseBeansItem, index);
    }

    public void setEnterpriseBeansItem(org.openejb.alt.config.ejb11.EnterpriseBeansItem[] enterpriseBeansItemArray) {

        _items.removeAllElements();
        for (int i = 0; i < enterpriseBeansItemArray.length; i++) {
            _items.addElement(enterpriseBeansItemArray[i]);
        }
    }

    public void setId(java.lang.String id) {
        this._id = id;
    }

    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.openejb.alt.config.ejb11.EnterpriseBeans) Unmarshaller.unmarshal(org.openejb.alt.config.ejb11.EnterpriseBeans.class, reader);
    }

    public void validate()
            throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}
