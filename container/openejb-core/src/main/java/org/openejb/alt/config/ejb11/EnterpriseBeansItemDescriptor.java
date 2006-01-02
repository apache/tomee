/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: EnterpriseBeansItemDescriptor.java,v 1.2 2004/03/31 00:44:02 dblevins Exp $
 */

package org.openejb.alt.config.ejb11;

//---------------------------------/

import org.exolab.castor.mapping.AccessMode;
import org.exolab.castor.xml.TypeValidator;
import org.exolab.castor.xml.XMLFieldDescriptor;
import org.exolab.castor.xml.validators.*;

public class EnterpriseBeansItemDescriptor extends org.exolab.castor.xml.util.XMLClassDescriptorImpl {


    private java.lang.String nsPrefix;

    private java.lang.String nsURI;

    private java.lang.String xmlName;

    private org.exolab.castor.xml.XMLFieldDescriptor identity;


    public EnterpriseBeansItemDescriptor() {
        super();
        nsURI = "http://www.openejb.org/ejb-jar/1.1";
        xmlName = "enterprise-beans";


        setCompositorAsChoice();
        org.exolab.castor.xml.util.XMLFieldDescriptorImpl desc = null;
        org.exolab.castor.xml.XMLFieldHandler handler = null;
        org.exolab.castor.xml.FieldValidator fieldValidator = null;


        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.openejb.alt.config.ejb11.Session.class, "_session", "session", org.exolab.castor.xml.NodeType.Element);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue(java.lang.Object object)
                    throws IllegalStateException {
                EnterpriseBeansItem target = (EnterpriseBeansItem) object;
                return target.getSession();
            }

            public void setValue(java.lang.Object object, java.lang.Object value)
                    throws IllegalStateException, IllegalArgumentException {
                try {
                    EnterpriseBeansItem target = (EnterpriseBeansItem) object;
                    target.setSession((org.openejb.alt.config.ejb11.Session) value);
                }
                catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }

            public java.lang.Object newInstance(java.lang.Object parent) {
                return new org.openejb.alt.config.ejb11.Session();
            }
        });
        desc.setHandler(handler);
        desc.setMultivalued(false);
        addFieldDescriptor(desc);


        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        {
        }
        desc.setValidator(fieldValidator);

        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.openejb.alt.config.ejb11.Entity.class, "_entity", "entity", org.exolab.castor.xml.NodeType.Element);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue(java.lang.Object object)
                    throws IllegalStateException {
                EnterpriseBeansItem target = (EnterpriseBeansItem) object;
                return target.getEntity();
            }

            public void setValue(java.lang.Object object, java.lang.Object value)
                    throws IllegalStateException, IllegalArgumentException {
                try {
                    EnterpriseBeansItem target = (EnterpriseBeansItem) object;
                    target.setEntity((org.openejb.alt.config.ejb11.Entity) value);
                }
                catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }

            public java.lang.Object newInstance(java.lang.Object parent) {
                return new org.openejb.alt.config.ejb11.Entity();
            }
        });
        desc.setHandler(handler);
        desc.setMultivalued(false);
        addFieldDescriptor(desc);


        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        {
        }
        desc.setValidator(fieldValidator);
    }


    public org.exolab.castor.mapping.AccessMode getAccessMode() {
        return null;
    }

    public org.exolab.castor.mapping.ClassDescriptor getExtends() {
        return null;
    }

    public org.exolab.castor.mapping.FieldDescriptor getIdentity() {
        return identity;
    }

    public java.lang.Class getJavaClass() {
        return org.openejb.alt.config.ejb11.EnterpriseBeansItem.class;
    }

    public java.lang.String getNameSpacePrefix() {
        return nsPrefix;
    }

    public java.lang.String getNameSpaceURI() {
        return nsURI;
    }

    public org.exolab.castor.xml.TypeValidator getValidator() {
        return this;
    }

    public java.lang.String getXMLName() {
        return xmlName;
    }

}
