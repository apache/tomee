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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * A property editor for Date typed properties.
 *
 * @version $Rev: 6680 $
 */
public class DateEditor extends AbstractConverter {
    private final DateFormat formatter;

    public DateEditor() {
        super(Date.class);

        // Get a date formatter to parse this.
        // This retrieves the formatter using the current execution locale,
        // which could present an intererting problem when applied to deployment
        // plans written in other locales.  Sort of a Catch-22 situation.
        formatter = DateFormat.getDateInstance();
    }

    /**
     * Convert the text value of the property into a Date object instance.
     *
     * @return a Date object constructed from the property text value.
     * @throws PropertyEditorException Unable to parse the string value into a Date.
     */
    protected Object toObjectImpl(String text) {
        try {
            return formatter.parse(text);
        } catch (ParseException e) {
            // any format errors show up as a ParseException, which we turn into a PropertyEditorException.
            throw new PropertyEditorException(e);
        }
    }

    protected String toStringImpl(Object value) {
        Date date = (Date) value;
        String text = formatter.format(date);
        return text;
    }
}
