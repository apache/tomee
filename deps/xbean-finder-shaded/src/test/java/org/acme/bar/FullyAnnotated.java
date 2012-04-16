/**
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
package org.acme.bar;

import java.util.List;
import java.util.ArrayList;

/**
 * @version $Revision$ $Date$
 */
@Type public class FullyAnnotated<Cheese extends java.util.Stack,Fun extends java.util.Iterator<java.lang.Exception>,Beeer> {
    @Field private String field;
    @Field private char[] characters;
    @Field private String[] strings;
    @Field private String[][] moreStrings;
    @Field private List<String> stringList;
    @Field private Cheese spam;
    @Field private Direction direction;

    @Construct public FullyAnnotated(@ParamA String constructorParam, @ParamB @Optional int anInt) {
        this.field = constructorParam;
        this.stringList = new ArrayList();
    }

    @Method public void doIt(int i, boolean b, double d, short s){}

    @Method public void doMore(Cheese cheese, Fun fun){}

    @Type enum Direction {
        NORTH, SOUTH, EAST, WEST
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public Cheese getSpam() {
        return spam;
    }

    public void setSpam(Cheese spam) {
        this.spam = spam;
    }

    public void setSpam(Object spam) {
        this.spam = (Cheese)spam;
    }

    @Get @Method public String getField() {
        @Variable String theField = this.field;
        return theField;
    }

    @Set @Method public void setField(@ParamB String methodParam) {
        this.field = methodParam;
    }

    @Get @Method public char[] getCharacters() {
        return characters;
    }

    @Set @Method public void setCharacters(char[] characters) {
        this.characters = characters;
    }

    @Get @Method public String[] getStrings() {
        return strings;
    }

    @Set @Method public void setStrings(String[] strings) {
        this.strings = strings;
    }

    @Get @Method public String[][] getMoreStrings() {
        return moreStrings;
    }

    @Set @Method public void setMoreStrings(@ParamA String[][] moreStrings) {
        this.moreStrings = moreStrings;
    }

    @Get @Method public List<String> getStringList() {
        return stringList;
    }

    @Set @Method public void setStringList(List<String> stringList) {
        this.stringList = stringList;
    }

    @Set @Method public void setStringList(ArrayList stringList) {
        this.stringList = stringList;
    }
}
