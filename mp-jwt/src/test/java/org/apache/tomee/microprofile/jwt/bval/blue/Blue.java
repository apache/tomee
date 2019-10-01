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
package org.apache.tomee.microprofile.jwt.bval.blue;

import org.apache.tomee.microprofile.jwt.bval.green.ReturnValidation;
import org.apache.tomee.microprofile.jwt.bval.green.TokenValidation;

import java.net.URI;
import java.util.List;


public class Blue {

    @ReturnValidation("bar")
    @TokenValidation("http://foo.bar.com")
    public void sapphire(final char mychar) {
    }

    @ReturnValidation("bar")
    @TokenValidation("http://foo.bar.com")
    public void sapphire(final byte mybyte) {
    }

    @ReturnValidation("bar")
    @TokenValidation("http://foo.bar.com")
    public void sapphire(final short myshort) {
    }

    @ReturnValidation("bar")
    @TokenValidation("http://foo.bar.com")
    public void sapphire(final int myint) {
    }

    @ReturnValidation("bar")
    @TokenValidation("http://foo.bar.com")
    public void sapphire(final long mylong) {
    }

    @ReturnValidation("bar")
    @TokenValidation("http://foo.bar.com")
    public void sapphire(final float myfloat) {
    }

    @ReturnValidation("bar")
    @TokenValidation("http://foo.bar.com")
    public void sapphire(final double mydouble) {
    }

    @ReturnValidation("bar")
    @TokenValidation("http://foo.bar.com")
    public void sapphire(final boolean myboolean) {
    }

    @ReturnValidation("bar")
    @TokenValidation("http://foo.bar.com")
    public void sapphire(final URI myURI) {
    }

    @ReturnValidation("char")
    @TokenValidation("http://foo.bar.com/char")
    public char sapphire(final URI uri, char mychar) {
        return 0;
    }

    @ReturnValidation("byte")
    @TokenValidation("http://foo.bar.com/byte")
    public byte sapphire(final URI uri, byte mybyte) {
        return 0;
    }

    @ReturnValidation("short")
    @TokenValidation("http://foo.bar.com/short")
    public short sapphire(final URI uri, short myshort) {
        return 0;
    }

    @ReturnValidation("int")
    @TokenValidation("http://foo.bar.com/int")
    public int sapphire(final URI uri, int myint) {
        return 0;
    }

    @ReturnValidation("long")
    @TokenValidation("http://foo.bar.com/long")
    public long sapphire(final URI uri, long mylong) {
        return 0;
    }

    @ReturnValidation("float")
    @TokenValidation("http://foo.bar.com/float")
    public float sapphire(final URI uri, float myfloat) {
        return 0;
    }

    @ReturnValidation("double")
    @TokenValidation("http://foo.bar.com/double")
    public double sapphire(final URI uri, double mydouble) {
        return 0;
    }

    @ReturnValidation("boolean")
    @TokenValidation("http://foo.bar.com/boolean")
    public boolean sapphire(final URI uri, boolean myboolean) {
        return false;
    }

    @ReturnValidation("URI")
    @TokenValidation("http://foo.bar.com/URI")
    public URI sapphire(final URI uri, URI myURI) {
        return null;
    }

    @ReturnValidation("List<URI>")
    @TokenValidation("http://foo.bar.com/URI")
    public URI sapphire(final List<URI> uri, URI myURI) {
        return null;
    }

    @ReturnValidation("URI...")
    @TokenValidation("http://foo.bar.com/URIs")
    public URI sapphire(final URI... uris) {
        return null;
    }

    @ReturnValidation("int, URI...")
    @TokenValidation("http://foo.bar.com/URIs")
    public URI sapphire(final int i, final URI... uris) {
        return null;
    }

}
