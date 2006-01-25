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

import java.net.Inet6Address;
import java.net.UnknownHostException;

/**
 * @version $Rev: 6687 $ $Date: 2005-12-28T21:08:56.733437Z $
 */
public class Inet6AddressEditor extends AbstractConverter {
    public Inet6AddressEditor() {
        super(Inet6Address.class);
    }

    protected Object toObjectImpl(String text) {
        try {
            return Inet6Address.getByName(text);
        } catch (UnknownHostException e) {
            throw new PropertyEditorException(e);
        }
    }
}
