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
package org.apache.openjpa.jdbc.meta.strats;

import org.apache.openjpa.jdbc.meta.ValueMapping;

/**
 * Primitive wrapper array helper methods.
 *
 * @author Marc Prud'hommeaux
 */
class PrimitiveWrapperArrays {

    /**
     * Convert the given primitive array to a wrapper array if needed.
     */
    public static Object toObjectValue(ValueMapping vm, char[] array) {
        if (array == null)
            return null;
        if (vm.getType().getComponentType() == Character.class) {
            Character[] objectArray = new Character[array.length];
            for (int i = 0; i < array.length; i++)
                objectArray[i] = Character.valueOf(array[i]);
            return objectArray;
        }
        return array;
    }

    /**
     * Convert the given value to a primitive array if needed.
     */
    public static char[] toCharArray(Object ob) {
        if (ob instanceof Character[]) {
            Character[] charOb = (Character[]) ob;
            char[] chars = new char[charOb.length];
            for (int i = 0; i < charOb.length; i++)
                chars[i] = charOb[i] == null ? 0 : charOb[i].charValue();
            return chars;
        }
        return (char[]) ob;
    }

    /**
     * Convert the given primitive array to a wrapper array if needed.
     */
    public static Object toObjectValue(ValueMapping vm, byte[] array) {
        if (array == null)
            return null;
        if (vm.getType().getComponentType() == Byte.class) {
            Byte[] objectArray = new Byte[array.length];
            for (int i = 0; i < array.length; i++)
                objectArray[i] = Byte.valueOf(array[i]);
            return objectArray;
        }
        return array;
    }

    /**
     * Convert the given value to a primitive array if needed.
     */
    public static byte[] toByteArray(Object ob) {
        if (ob instanceof Byte[]) {
            Byte[] byteOb = (Byte[]) ob;
            byte[] bytes = new byte[byteOb.length];
            for (int i = 0; i < byteOb.length; i++)
                bytes[i] = byteOb[i] == null ? 0 : byteOb[i].byteValue();
            return bytes;
        }
        return (byte[]) ob;
    }
}

