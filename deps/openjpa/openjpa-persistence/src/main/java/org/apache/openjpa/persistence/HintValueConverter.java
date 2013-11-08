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
package org.apache.openjpa.persistence;

import java.util.Arrays;


/**
 * Converts a given user-specified value to a target type consumable by the kernel.
 * Used by hint processing.
 * 
 * @author Pinaki Poddar
 * @since 2.0.0
 * @unpublished
 */
public interface HintValueConverter {
    /**
     * Convert the user-specified value to a kernel consumable value.
     *  
     * @param original the user-specified value
     * @return an equivalent value consumable by a kernel construct.
     * 
     * @exception IllegalArgumentException if the given value can not be converted.
     */
    Object convert(Object original);
    
    /**
     * Affirm if this receiver can convert the value of the given type.
     */
    boolean canConvert(Class<?> type);
    
    /**
     * Convert the enum value to an enumerated set of constants.
     * 
     * @author Pinaki Poddar
     *
     */
    static class EnumToInteger implements HintValueConverter {
        private Class<? extends Enum<?>> _type;
        private Integer[] map;
        
        public EnumToInteger(Class<? extends Enum<?>> enumType, int[] numbers) {
            try {
                _type = enumType;
                Enum<?>[] values = (Enum<?>[])enumType.getMethod("values", null).invoke(null, (Class<?>[])null);
                map = new Integer[values.length];
                int i = 0;
                for (Enum<?> v : values) {
                    map[v.ordinal()] = numbers[i++];
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        public Object convert(Object e) {
            if (e.getClass() == _type)
                return map[((Enum<?>)e).ordinal()];
            return e;
        }
        
        public boolean canConvert(Class<?> type) {
            return Enum.class.isAssignableFrom(type);
        }
    }
    
    /**
     * Converts an OpenJPA specific enum to an equivalent kernel constant.
     * 
     * @author Pinaki Poddar
     *
     */
    public static class OpenJPAEnumToInteger implements HintValueConverter {
        private OpenJPAEnum<?> _prototype;
        
        public OpenJPAEnumToInteger(OpenJPAEnum<?> prototype) {
            _prototype = prototype;
        }
            
        public Object convert(Object e) {
            if (e.getClass() == _prototype.getClass())
                return ((OpenJPAEnum<Enum<?>>)e).toKernelConstant();
            if (e instanceof String) {
                return _prototype.convertToKernelConstant(e.toString());
            }
            if (e instanceof Integer) {
                return _prototype.convertToKernelConstant((Integer)e);
            }
            return e;
        }
        
        public boolean canConvert(Class<?> type) {
            return OpenJPAEnum.class.isAssignableFrom(type) 
                || type == String.class 
                || type == Integer.class
                || type == int.class;
        }
    }

    /**
     * Converts a String to an integer.
     * 
     * @author Pinaki Poddar
     *
     */
    public static class StringToInteger implements HintValueConverter {
        private String[] strings;
        private Integer[] numbers;
        
        /**
         * Construct a converter that will simply translate a numeric string to a integer.
         */
        public StringToInteger() {
            
        }
        
        /**
         * Construct a converter that will translate any of the given strings to corresponding integer.
         * Both arrays must not be null, must not contain null elements and must have the same dimension.
         * 
         * @param strings
         * @param numbers
         */
        public StringToInteger(String[] strings, int[] numbers) {
            if (strings == null || numbers == null || strings.length != numbers.length)
                throw new IllegalArgumentException();
            this.strings = new String[strings.length];
            this.numbers = new Integer[numbers.length];
            for (int i = 0; i < strings.length; i++) {
                this.strings[i] = strings[i];
                this.numbers[i] = numbers[i];
            }
        }
        
        public Object convert(Object s) {
            if (s instanceof String == false)
                return s;
            String str = s.toString();
            if (strings == null) {
                try {
                    return Integer.parseInt(str);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Can not convert " + str + " . Expected a numeric string");
                }
            }
            for (int i = 0; i < strings.length; i++)  {
                if (strings[i].equalsIgnoreCase(str))
                    return numbers[i];
            }
            throw new IllegalArgumentException("Can not convert " + str + " . Valid input is " + 
                    Arrays.toString(strings));
        }
        
        public boolean canConvert(Class<?> cls) {
            return String.class == cls;
        }
    }
    
    public static class StringToBoolean implements HintValueConverter {
        public Object convert(Object v) {
            if (v instanceof String)
                return Boolean.valueOf(v.toString());
            if (v instanceof Boolean)
                return v;
            return v;
        }
        
        public boolean canConvert(Class<?> cls) {
            return String.class == cls || Boolean.class == cls || boolean.class == cls;
        }
    }

}
