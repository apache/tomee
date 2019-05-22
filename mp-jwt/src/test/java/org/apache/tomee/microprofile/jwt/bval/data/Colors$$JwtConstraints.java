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
package org.apache.tomee.microprofile.jwt.bval.data;

import org.apache.tomee.microprofile.jwt.bval.Name;
import org.apache.tomee.microprofile.jwt.bval.ann.Audience;
import org.apache.tomee.microprofile.jwt.bval.ann.Issuer;
import org.eclipse.microprofile.jwt.JsonWebToken;

public class Colors$$JwtConstraints {

    public Colors$$JwtConstraints() {
    }

    @Name("public void org.apache.tomee.microprofile.jwt.bval.data.Colors.blue()")
    @Issuer("http://foo.bar.com")
    public JsonWebToken blue$$0() {
        return null;
    }

    @Name("public void org.apache.tomee.microprofile.jwt.bval.data.Colors.red()")
    @Audience("bar")
    @Issuer("http://foo.bar.com")
    public JsonWebToken red$$1() {
        return null;
    }

}
