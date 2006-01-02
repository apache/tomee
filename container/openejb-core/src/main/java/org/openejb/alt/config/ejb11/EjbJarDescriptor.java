/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: EjbJarDescriptor.java,v 1.2 2004/03/31 00:44:02 dblevins Exp $
 */

package org.openejb.alt.config.ejb11;


//---------------------------------/

import org.exolab.castor.mapping.AccessMode;
import org.exolab.castor.xml.TypeValidator;
import org.exolab.castor.xml.XMLFieldDescriptor;
import org.exolab.castor.xml.validators.*;

public class EjbJarDescriptor extends org.exolab.castor.xml.util.XMLClassDescriptorImpl {




    private java.lang.String nsPrefix;

    private java.lang.String nsURI;

    private java.lang.String xmlName;

    private org.exolab.castor.xml.XMLFieldDescriptor identity;




    public EjbJarDescriptor() {
        super();
        nsURI = "http://www.openejb.org/ejb-jar/1.1";
        xmlName = "ejb-jar";
        

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
                EjbJar target = (EjbJar) object;
                return target.getId();
            }
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    EjbJar target = (EjbJar) object;
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

        

        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(java.lang.String.class, "_description", "description", org.exolab.castor.xml.NodeType.Element);
        desc.setImmutable(true);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                EjbJar target = (EjbJar) object;
                return target.getDescription();
            }
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    EjbJar target = (EjbJar) object;
                    target.setDescription( (java.lang.String) value);
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
        desc.setMultivalued(false);
        addFieldDescriptor(desc);
        

        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        {
            StringValidator typeValidator = new StringValidator();
            typeValidator.setWhiteSpace("preserve");
            fieldValidator.setValidator(typeValidator);
        }
        desc.setValidator(fieldValidator);

        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(java.lang.String.class, "_displayName", "display-name", org.exolab.castor.xml.NodeType.Element);
        desc.setImmutable(true);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                EjbJar target = (EjbJar) object;
                return target.getDisplayName();
            }
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    EjbJar target = (EjbJar) object;
                    target.setDisplayName( (java.lang.String) value);
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
        desc.setMultivalued(false);
        addFieldDescriptor(desc);
        

        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        {
            StringValidator typeValidator = new StringValidator();
            typeValidator.setWhiteSpace("preserve");
            fieldValidator.setValidator(typeValidator);
        }
        desc.setValidator(fieldValidator);

        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(java.lang.String.class, "_smallIcon", "small-icon", org.exolab.castor.xml.NodeType.Element);
        desc.setImmutable(true);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                EjbJar target = (EjbJar) object;
                return target.getSmallIcon();
            }
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    EjbJar target = (EjbJar) object;
                    target.setSmallIcon( (java.lang.String) value);
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
        desc.setMultivalued(false);
        addFieldDescriptor(desc);
        

        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        {
            StringValidator typeValidator = new StringValidator();
            typeValidator.setWhiteSpace("preserve");
            typeValidator.setPattern(".*\\.(gif|jpeg)$");
            fieldValidator.setValidator(typeValidator);
        }
        desc.setValidator(fieldValidator);

        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(java.lang.String.class, "_largeIcon", "large-icon", org.exolab.castor.xml.NodeType.Element);
        desc.setImmutable(true);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                EjbJar target = (EjbJar) object;
                return target.getLargeIcon();
            }
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    EjbJar target = (EjbJar) object;
                    target.setLargeIcon( (java.lang.String) value);
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
        desc.setMultivalued(false);
        addFieldDescriptor(desc);
        

        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        {
            StringValidator typeValidator = new StringValidator();
            typeValidator.setWhiteSpace("preserve");
            typeValidator.setPattern(".*\\.(gif|jpeg)$");
            fieldValidator.setValidator(typeValidator);
        }
        desc.setValidator(fieldValidator);

        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.openejb.alt.config.ejb11.EnterpriseBeans.class, "_enterpriseBeans", "enterprise-beans", org.exolab.castor.xml.NodeType.Element);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                EjbJar target = (EjbJar) object;
                return target.getEnterpriseBeans();
            }
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    EjbJar target = (EjbJar) object;
                    target.setEnterpriseBeans( (org.openejb.alt.config.ejb11.EnterpriseBeans) value);
                }
                catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            public java.lang.Object newInstance( java.lang.Object parent ) {
                return new org.openejb.alt.config.ejb11.EnterpriseBeans();
            }
        } );
        desc.setHandler(handler);
        desc.setRequired(true);
        desc.setMultivalued(false);
        addFieldDescriptor(desc);
        

        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(1);
        {
        }
        desc.setValidator(fieldValidator);

        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.openejb.alt.config.ejb11.AssemblyDescriptor.class, "_assemblyDescriptor", "assembly-descriptor", org.exolab.castor.xml.NodeType.Element);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                EjbJar target = (EjbJar) object;
                return target.getAssemblyDescriptor();
            }
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    EjbJar target = (EjbJar) object;
                    target.setAssemblyDescriptor( (org.openejb.alt.config.ejb11.AssemblyDescriptor) value);
                }
                catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            public java.lang.Object newInstance( java.lang.Object parent ) {
                return new org.openejb.alt.config.ejb11.AssemblyDescriptor();
            }
        } );
        desc.setHandler(handler);
        desc.setMultivalued(false);
        addFieldDescriptor(desc);
        

        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        {
        }
        desc.setValidator(fieldValidator);

        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(java.lang.String.class, "_ejbClientJar", "ejb-client-jar", org.exolab.castor.xml.NodeType.Element);
        desc.setImmutable(true);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                EjbJar target = (EjbJar) object;
                return target.getEjbClientJar();
            }
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    EjbJar target = (EjbJar) object;
                    target.setEjbClientJar( (java.lang.String) value);
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
        desc.setMultivalued(false);
        addFieldDescriptor(desc);
        

        fieldValidator = new org.exolab.castor.xml.FieldValidator();
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
        return org.openejb.alt.config.ejb11.EjbJar.class;
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
