/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.entity.cmp;

import java.util.Set;
import javax.ejb.DuplicateKeyException;
import javax.ejb.RemoveException;

import org.openejb.transaction.EjbTransactionContext;
import org.openejb.transaction.CmpTxData;

/**
 * @version $Revision$ $Date$
 */
public interface EjbCmpEngine {
    /**
     * Gets the all of the field accessor objects for both CMP and CMR fields.
     * @return the field accessors
     */
    Set getCmpFields();

    /**
     * Gets all of the select qureies for both the ejbSelect and finders.
     * @return the select queries
     */
    Set getSelectQueries();

    /**
     * Initialized an instance context before the ejbCreate callback is invoked.
     */
    void beforeCreate(CmpInstanceContext ctx);

    /**
     * Defines the primary key after the ejbCreate callback has been invoked.  After this method is invoked the instance
     * context will have an id set.
     */
    void afterCreate(CmpInstanceContext ctx, EjbTransactionContext ejbTransactionContext) throws DuplicateKeyException, Exception;

    /**
     * Removes the instance and handles cascade delete.  After this method returns the instance context will not have
     * an id set, nor will it contain any cmp data.
     */
    void afterRemove(CmpInstanceContext ctx, EjbTransactionContext ejbTransactionContext) throws RemoveException;

    void beforeLoad(CmpInstanceContext ctx) throws Exception;

    void afterStore(CmpInstanceContext ctx) throws Exception;

    CmpTxData createCmpTxData();
}
