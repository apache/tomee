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
package org.apache.openjpa.persistence.distinctjoin;

import javax.persistence.Embeddable;
import javax.persistence.Lob;

/**
 * Embeddable for being used in  {@link Course}
 * This entity contains Lob columns which cause OpenJPA to create
 * single sub selects for each Embedded field in Oracle.
 */
@Embeddable
public class LocalizedText {
    @Lob
    String textEn;

    @Lob
    String textDe;

    public LocalizedText() {
        System.out.println("Just for having a debug breakpoint...");
    }


    public String getTextDe() {
        return textDe;
    }

    public void setTextDe(String textDe) {
        this.textDe = textDe;
    }

    public String getTextEn() {
        return textEn;
    }

    public void setTextEn(String textEn) {
        this.textEn = textEn;
    }
}
