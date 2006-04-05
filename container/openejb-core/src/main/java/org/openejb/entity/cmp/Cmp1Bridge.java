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
package org.openejb.entity.cmp;


import java.lang.reflect.Field;
import java.util.Set;
import javax.ejb.EJBException;
import javax.ejb.EnterpriseBean;

/**
 * @version $Revision$ $Date$
 */
public class Cmp1Bridge {
    private final CmpField[] cmpFields;
    private final Field[] beanFields;

    public Cmp1Bridge(Class beanClass, Set cmpFields) {
        this.cmpFields = (CmpField[]) cmpFields.toArray(new CmpField[cmpFields.size()]);

        beanFields = new Field[cmpFields.size()];

        for (int i = 0; i < this.cmpFields.length; i++) {
            CmpField cmpField = this.cmpFields[i];

            String fieldName = cmpField.getName();

            this.cmpFields[i] = cmpField;

            try {
                beanFields[i] = beanClass.getField(fieldName);
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException("Missing bean field " + fieldName);
            }
        }
    }

    public void copyFromObjectToCmp(CmpInstanceContext ctx) {
        EnterpriseBean entityBean = ctx.getInstance();
        for (int i = 0; i < beanFields.length; i++) {
            Field beanField = beanFields[i];
            CmpField cmpField = cmpFields[i];

            Object value = null;
            try {
                value = beanField.get(entityBean);
            } catch (IllegalAccessException e) {
                throw new EJBException("Could not get the value of cmp a field: " + beanField.getName());
            }
            cmpField.setValue(ctx, value);
        }
    }

    public void copyFromCmpToObject(CmpInstanceContext ctx) {
        EnterpriseBean entityBean = ctx.getInstance();
        for (int i = 0; i < beanFields.length; i++) {
            Field beanField = beanFields[i];
            CmpField cmpField = cmpFields[i];

            Object value = cmpField.getValue(ctx);
            try {
                beanField.set(entityBean, value);
            } catch (IllegalAccessException e) {
                throw new EJBException("Could not get the value of cmp a field: " + beanField.getName());
            }
        }
    }
}
