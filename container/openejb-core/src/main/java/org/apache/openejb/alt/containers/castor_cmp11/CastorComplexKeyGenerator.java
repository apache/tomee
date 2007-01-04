/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.alt.containers.castor_cmp11;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.cmp.ComplexKeyGenerator;
import org.exolab.castor.persist.spi.Complex;

public class CastorComplexKeyGenerator extends ComplexKeyGenerator implements CastorKeyGenerator {
    public CastorComplexKeyGenerator(Class entityBeanClass, Class pkClass) throws OpenEJBException {
        super(entityBeanClass, pkClass);
    }

    public Complex getJdoComplex(Object primaryKey) {
        Object[] pkValues = new Object[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            PkField pkField = fields.get(i);
            Object pkValue = pkField.getPkFieldValue(primaryKey);
            pkValues[i] = pkValue;
        }
        Complex complex = new Complex(pkValues);
        return complex;
    }

    public boolean isKeyComplex() {
        return true;
    }
}
