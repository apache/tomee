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

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * A property editor for {@link javax.management.ObjectName}.
 *
 * @version $Rev: 6680 $ $Date: 2005-12-24T04:38:27.427468Z $
 */
public class ObjectNameEditor extends AbstractConverter {
    public ObjectNameEditor() {
        super(ObjectName.class);
    }

    /**
     * Returns a ObjectName for the input object converted to a string.
     *
     * @return a ObjectName object
     * @throws PropertyEditorException An MalformedObjectNameException occured.
     */
    protected Object toObjectImpl(String text) {
        try {
            return new ObjectName(text);
        } catch (MalformedObjectNameException e) {
            throw new PropertyEditorException(e);
        }
    }
}
