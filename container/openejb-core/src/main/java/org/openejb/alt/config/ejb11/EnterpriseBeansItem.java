/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: EnterpriseBeansItem.java,v 1.2 2004/03/31 00:44:02 dblevins Exp $
 */

package org.openejb.alt.config.ejb11;

//---------------------------------/

import java.io.Serializable;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

public class EnterpriseBeansItem implements java.io.Serializable {


    private org.openejb.alt.config.ejb11.Session _session;

    private org.openejb.alt.config.ejb11.Entity _entity;


    public EnterpriseBeansItem() {
        super();
    }


    public org.openejb.alt.config.ejb11.Entity getEntity() {
        return this._entity;
    }

    public org.openejb.alt.config.ejb11.Session getSession() {
        return this._session;
    }

    public void setEntity(org.openejb.alt.config.ejb11.Entity entity) {
        this._entity = entity;
    }

    public void setSession(org.openejb.alt.config.ejb11.Session session) {
        this._session = session;
    }

}
