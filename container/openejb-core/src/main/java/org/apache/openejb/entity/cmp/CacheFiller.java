/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */
package org.apache.openejb.entity.cmp;

import org.tranql.cache.AlreadyAssociatedException;
import org.tranql.cache.CacheRow;
import org.tranql.cache.InTxCache;
import org.tranql.field.Row;
import org.tranql.identity.GlobalIdentity;
import org.tranql.identity.IdentityDefiner;
import org.tranql.identity.UndefinedIdentityException;
import org.tranql.ql.QueryException;
import org.tranql.query.ResultHandler;

/**
 * 
 * 
 * @version $Revision$ $Date$
 */
class CacheFiller implements ResultHandler {
    private final ResultHandler delegate;
    private final IdentityDefiner idDefiner;
    private final IdentityDefiner idInjector;
    private final InTxCache cache;
    
    public CacheFiller(ResultHandler delegate, IdentityDefiner idDefiner, IdentityDefiner idInjector, InTxCache cache) {
        this.delegate = delegate;
        this.idDefiner = idDefiner;
        this.idInjector = idInjector;
        this.cache = cache;
    }

    public Object fetched(Row row, Object arg) throws QueryException {
        try {
            GlobalIdentity id = idDefiner.defineIdentity(row);
            if (null != id && null == cache.get(id)) {
                CacheRow cacheRow = id.getTable().emptyRow(id);
                idInjector.injectIdentity(cacheRow);
                cache.add(cacheRow);
            }
        } catch (UndefinedIdentityException e) {
            throw new QueryException(e);
        } catch (AlreadyAssociatedException e) {
            throw new QueryException(e);
        }
        return delegate.fetched(row, arg);
    }

    public Object endFetched(Object arg) throws QueryException {
        return delegate.endFetched(arg);
    }

}
