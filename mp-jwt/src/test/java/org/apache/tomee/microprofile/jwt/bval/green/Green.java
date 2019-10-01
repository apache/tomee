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
package org.apache.tomee.microprofile.jwt.bval.green;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class Green {

    @ReturnValidation("bar")
    @TokenValidation("http://foo.bar.com")
    public URL emerald() {
        try {
            return new URL("foo://bar");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @ReturnValidation("int")
    @TokenValidation("http://foo.bar.com/int")
    public URL emerald(final int i) {
        try {
            return new URL("foo://bar");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @ReturnValidation("URI")
    @TokenValidation("http://foo.bar.com/URI")
    public URL emerald(final URI i) {
        try {
            return new URL("foo://bar");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @ReturnValidation("URI")
    @TokenValidation("http://foo.bar.com/URI")
    public URL emerald(final List<URI> i) {
        try {
            return new URL("foo://bar");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @ReturnValidation("bar")
    public void sage() {
    }

    @TokenValidation("http://foo.bar.com")
    public void olive() {
    }

    public java.util.Collection<URI> mint() {
        return Arrays.asList(URI.create("hello://world"), URI.create("three://four"));
    }

}
