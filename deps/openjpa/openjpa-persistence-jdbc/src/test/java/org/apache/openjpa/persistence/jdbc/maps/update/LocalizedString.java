/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.persistence.jdbc.maps.update;

import java.io.Serializable;

import javax.persistence.Embeddable;

/**
 * A LocalizedString is any text string combined with a language code. The
 * language codes are two lower-case characters according to ISO-639-1, e.g.
 * "de" = German, "en" = English.
 * 
 * The language may be null for strings like phone numbers.
 * 
 * @author Harald Wellmann
 * 
 */
@Embeddable
public class LocalizedString implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Language code. */
    private String language;

    private String string;

    public LocalizedString() {
    }

    public LocalizedString(String language, String text) {
        this.language = language;
        this.string = text;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getString() {
        return string;
    }

    public void setString(String text) {
        this.string = text;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((language == null) ? 0 : language.hashCode());
        result = prime * result + ((string == null) ? 0 : string.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LocalizedString other = (LocalizedString) obj;
        if (language == null) {
            if (other.language != null)
                return false;
        } else if (!language.equals(other.language))
            return false;
        if (string == null) {
            if (other.string != null)
                return false;
        } else if (!string.equals(other.string))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return string;
    }
}
