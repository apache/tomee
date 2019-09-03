/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.microprofile.jwt.bval.red;

import org.apache.tomee.microprofile.jwt.bval.Generated;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;
import java.util.Set;


public class Red$$JwtConstraints {

    public Red$$JwtConstraints() {
    }

    @Generated("org.apache.tomee.microprofile.jwt.bval.JwtValidationGenerator")
    @TwoTokenValidation("http://foo.bar.com")
    public JsonWebToken cherry() {
        return null;
    }

    @Generated("org.apache.tomee.microprofile.jwt.bval.JwtValidationGenerator")
    @TwoTokenValidation("http://child.com")
    @OneTokenValidation("http://blue.com")
    public JsonWebToken color(List var1) {
        return null;
    }

    @Generated("org.apache.tomee.microprofile.jwt.bval.JwtValidationGenerator")
    @TwoTokenValidation("http://parent.com")
    public JsonWebToken color(Set var1) {
        return null;
    }

    @Generated("org.apache.tomee.microprofile.jwt.bval.JwtValidationGenerator")
    @TwoTokenValidation("http://foo.bar.com")
    public JsonWebToken ruby() {
        return null;
    }
}
