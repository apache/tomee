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

/**
 * @version $Rev: 6680 $ $Date: 2005-12-24T04:38:27.427468Z $
 */
public class CharacterEditor extends AbstractConverter {
    public CharacterEditor() {
        super(Character.class);
    }

    protected Object toObjectImpl(String text) {
        try {
            if (text.length() != 1) {
                throw new IllegalArgumentException("wrong size: " + text);
            }
            return new Character(text.charAt(0));
        } catch (Exception e) {
            throw new PropertyEditorException(e);
        }
    }
}
