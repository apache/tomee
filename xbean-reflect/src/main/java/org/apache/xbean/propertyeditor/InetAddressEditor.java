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

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @version $Rev: 6680 $ $Date: 2005-12-24T04:38:27.427468Z $
 */
public class InetAddressEditor extends AbstractConverter {
    public InetAddressEditor() {
        super(InetAddress.class);
    }

    protected Object toObjectImpl(String text) {
        try {
            return InetAddress.getByName(text);
        } catch (UnknownHostException e) {
            throw new PropertyEditorException(e);
        }
    }
}
