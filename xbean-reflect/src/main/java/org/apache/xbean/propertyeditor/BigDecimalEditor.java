/**
 *
 * Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.xbean.propertyeditor;

import java.math.BigDecimal;

/**
 * A property editor for BigDecimal typed properties.
 *
 * @version $Rev: 6680 $
 */
public class BigDecimalEditor extends AbstractConverter {
    public BigDecimalEditor() {
        super(BigDecimal.class);
    }

    /**
     * Convert the text value of the property into a BigDecimal instance.
     *
     * @return a BigDecimal object constructed from the property text value.
     */
    protected Object toObjectImpl(String text) {
        try {
            // just instantiate a BigDecimal instance from the test string value.
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            // any format errors show up as a NumberFormatException, which we turn into a PropertyEditorException.
            throw new PropertyEditorException(e);
        }
    }
}
