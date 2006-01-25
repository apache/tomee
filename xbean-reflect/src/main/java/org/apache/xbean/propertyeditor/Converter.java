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

import java.beans.PropertyEditor;

/**
 * @version $Rev: 6680 $ $Date: 2005-12-24T04:38:27.427468Z $
 */
public interface Converter extends PropertyEditor {
    /**
     * Gets the the type of object supported by this converter.
     * @return
     */
    Class getType();

    /**
     * Converts the supplied object to text.  If value is null, null will be returned.  If value is not an instance of
     * the this converter's type, a PropertyEditorException will be thrown.
     *
     * @param value an instance of the editor type
     * @return the text equivalent of the value
     * @throws PropertyEditorException if an error occurs while converting the value to a String (this is very rare)
     */
    String toString(Object value) throws PropertyEditorException;

    /**
     * Converts the supplied text in to an instance of the editor type.  If text is null, null will be returned.
     *
     * @param text the text to convert
     * @return an instance of the editor type
     * @throws PropertyEditorException if an error occurs while converting the text to an object
     */
    Object toObject(String text) throws PropertyEditorException;
}
