/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.entity.cmp;


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
