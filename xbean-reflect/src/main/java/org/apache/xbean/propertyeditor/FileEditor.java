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

import java.io.File;
import java.io.IOException;

/**
 * A property editor for {@link File}.
 *
 * @version $Rev: 6680 $ $Date: 2005-12-24T04:38:27.427468Z $
 */
public class FileEditor extends AbstractConverter {
    public FileEditor() {
        super(File.class);
    }

    /**
     * Returns a URL for the input object converted to a string.
     *
     * @return a URL object
     * @throws PropertyEditorException An IOException occured.
     */
    protected Object toObjectImpl(String text) {
        try {
            return new File(text).getCanonicalFile();
        } catch (IOException e) {
            throw new PropertyEditorException(e);
        }
    }
}
