/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: MethodParamsDescriptor.java,v 1.2 2004/03/31 00:44:02 dblevins Exp $
 */

package org.openejb.alt.config.ejb11;


//---------------------------------/

import org.exolab.castor.mapping.AccessMode;
import org.exolab.castor.xml.TypeValidator;
import org.exolab.castor.xml.XMLFieldDescriptor;
import org.exolab.castor.xml.validators.*;

public class MethodParamsDescriptor extends org.exolab.castor.xml.util.XMLClassDescriptorImpl {




    private java.lang.String nsPrefix;

    private java.lang.String nsURI;

    private java.lang.String xmlName;

    private org.exolab.castor.xml.XMLFieldDescriptor identity;




    public MethodParamsDescriptor() {
        super();
        nsURI = "http://www.openejb.org/ejb-jar/1.1";
        xmlName = "method-params";
        

        setCompositorAsSequence();
        org.exolab.castor.xml.util.XMLFieldDescriptorImpl  desc           = null;
        org.exolab.castor.xml.XMLFieldHandler              handler        = null;
        org.exolab.castor.xml.FieldValidator               fieldValidator = null;

        

        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(java.lang.String.class, "_id", "id", org.exolab.castor.xml.NodeType.Attribute);
        this.identity = desc;
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                MethodParams target = (MethodParams) object;
                return target.getId();
            }
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    MethodParams target = (MethodParams) object;
                    target.setId( (java.lang.String) value);
                }
                catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            public java.lang.Object newInstance( java.lang.Object parent ) {
                return new java.lang.String();
            }
        } );
        desc.setHandler(handler);
        addFieldDescriptor(desc);
        

        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        {
        }
        desc.setValidator(fieldValidator);

        

        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(java.lang.String.class, "_methodParamList", "method-param", org.exolab.castor.xml.NodeType.Element);
        desc.setImmutable(true);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                MethodParams target = (MethodParams) object;
                return target.getMethodParam();
            }
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    MethodParams target = (MethodParams) object;
                    target.addMethodParam( (java.lang.String) value);
                }
                catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            public java.lang.Object newInstance( java.lang.Object parent ) {
                return null;
            }
        } );
        desc.setHandler(handler);
        desc.setMultivalued(true);
        addFieldDescriptor(desc);
        

        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(0);
        {
            StringValidator typeValidator = new StringValidator();
            typeValidator.setWhiteSpace("preserve");
            fieldValidator.setValidator(typeValidator);
        }
        desc.setValidator(fieldValidator);
    }




    public org.exolab.castor.mapping.AccessMode getAccessMode()
    {
        return null;
    }

    public org.exolab.castor.mapping.ClassDescriptor getExtends()
    {
        return null;
    }

    public org.exolab.castor.mapping.FieldDescriptor getIdentity()
    {
        return identity;
    }

    public java.lang.Class getJavaClass()
    {
        return org.openejb.alt.config.ejb11.MethodParams.class;
    }

    public java.lang.String getNameSpacePrefix()
    {
        return nsPrefix;
    }

    public java.lang.String getNameSpaceURI()
    {
        return nsURI;
    }

    public org.exolab.castor.xml.TypeValidator getValidator()
    {
        return this;
    }

    public java.lang.String getXMLName()
    {
        return xmlName;
    }

}
