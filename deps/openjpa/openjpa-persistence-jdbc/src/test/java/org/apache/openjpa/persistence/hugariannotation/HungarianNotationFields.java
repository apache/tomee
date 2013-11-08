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
package org.apache.openjpa.persistence.hugariannotation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Created by IntelliJ IDEA.
 * User: Ben
 * Date: 30-Oct-2007
 * Time: 22:11:00
 */
@Entity
public class HungarianNotationFields {

    @Id
    private Long mFooBar7;

    private String mFooBar1;

    private String strFooBar2;

    private Integer intFooBar3;

    private Long lgFooBar4;

    private int m_intFooBar5;

    @ManyToOne(targetEntity = OtherClass.class)
    private OtherClass m_clzFooBar6;

    @Column(name="M_INTFOOBAR7_CUSTOM_NAME")
    private int m_intFooBar7;
}
