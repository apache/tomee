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
package org.apache.openjpa.meta;

import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.UserException;

/**
 * Represents access styles for members of a class and field through a 
 * 5-bit integer.
 * <br>
 * 
 * The bits designate following aspects of access style being used at class
 * level:<br>
 * 
 * <LI>Bit position 0 (UNKNOWN): generally 0. All bits as zero represent    
 *              that no access style has been set. 1 denotes that the 
 *              class has no property at all and its access can not be
 *              determined.
 * <LI>Bit position 1 (FIELD): Field based access is default
 * <LI>Bit position 2 (PROPERTY): Property based access is default 
 * <LI>Bit position 3 (EXPLICIT): whether explicit or implicit 
 *              Explicit access style allows members to use mixed access style,
 *              implicit access style does not
 * <LI>Bit position 4 (MIXED): whether all members are using the same  
 *              access style or not. Can only be set if EXPLICT bit is set.
 *              If set, then bit 1 or 2 denotes what is the default.   
 * <br> 
 * The same bits designate following aspects of access style being used at field
 * level:<br>
 * 
 * <LI>Bit position 0 (UNKNOWN): always 0. All bits as zero represent    
 *              that no access style has been set. 
 * <LI>Bit position 1 (FIELD): Field based access is default
 * <LI>Bit position 2 (PROPERTY): Property based access is default 
 * <LI>Bit position 3 (EXPLICIT): whether the access is explicit or implicit 
 * <LI>Bit position 4 (MIXED): not used  
 * <br>             
 *             
 * <p>
 * Validation Rules for the bits:<br>
 * <LI>1. Only one of the position 1 (FIELD) and 2 (PROPERTY) can  
 *    be set. A single bit is not used for legacy reason to cope with the access
 *    constants used in ClassMetaData which this class now refactors to address
 *    new behaviors specified in JPA 2.0 specification. 
 * <LI>2. if position 3 (EXPLICIT) is set then one of position 1 
 *    (FIELD) and 2 (PROPERTY) must be set.
 * <LI>3. If position 4 (MIXED) is set then the set position of either 
 *    FIELD or PROPERTY designates the default access of the
 *    member.
 *  
 * @author Pinaki Poddar
 * 
 * @since 2.0.0
 */
public class AccessCode {
	public static int UNKNOWN   = 0;
	public static int EMPTY     = 1;
	public static int FIELD     = 2 << 0;
	public static int PROPERTY  = 2 << 1;
	public static int EXPLICIT  = 2 << 2;
	public static int MIXED     = 2 << 3;
	
	private static Localizer _loc = Localizer.forPackage(AccessCode.class);
	
	/**
	 * Affirms if the given code is valid. 
	 */
	public static boolean isValidClassCode(int code) {
		if (code == EMPTY)
			return true;
		return (code%2 == 0 || code == EMPTY) 
		 && code >= UNKNOWN 
		 && code <= (MIXED|EXPLICIT|PROPERTY)
         && !(isProperty(code) && isField(code)) // both 1 & 2 can not be set 
		 && (isProperty(code) || isField(code) || isUnknown(code))
		 && ((isMixed(code) && isExplicit(code)) || !isMixed(code));
	}
	
	public static boolean isValidFieldCode(int code) {
		return code%2 == 0 // code must be even
		    && code >= UNKNOWN 
		    && code <= (EXPLICIT|PROPERTY)
            && !(isProperty(code) && isField(code))
            && (isProperty(code) || isField(code) || isUnknown(code));
	}
	
	/**
	 * Affirms if the given code designates that members can use both 
	 * FIELD and PROPERTY based access. 
	 */
	public static boolean isMixed(int code) {
		return (code & MIXED) != 0;
	}
	
	public static boolean isExplicit(int code) {
		return (code & EXPLICIT) != 0;
	}
	
	public static boolean isProperty(int code) {
		return (code & PROPERTY) != 0;
	}
	
	public static boolean isField(int code) {
		return (code & FIELD) != 0;
	}
	
	public static boolean isUnknown(int code) {
		return code == UNKNOWN;
	}
	
	public static boolean isEmpty(int code) {
		return code == EMPTY;
	}
	
	public static boolean isField(ClassMetaData meta) {
		return isField(meta.getAccessType());
	}
	
	public static boolean isProperty(ClassMetaData meta) {
		return isProperty(meta.getAccessType());
	}
	
	public static boolean isUnknown(ClassMetaData meta) {
		return isUnknown(meta.getAccessType());
	}
	
	public static boolean isEmpty(ClassMetaData meta) {
		return isEmpty(meta.getAccessType());
	}
	
	public static boolean isField(FieldMetaData meta) {
		return isField(meta.getAccessType());
	}
	
	public static boolean isProperty(FieldMetaData meta) {
		return isProperty(meta.getAccessType());
	}
	
	public static boolean isUnknown(FieldMetaData meta) {
		return isUnknown(meta.getAccessType());
	}
	
	/**
	 * Affirms if the sub class access type is compatible with super class
	 * access style.
	 */
	public static boolean isCompatibleSuper(int subCode, int superCode) {
		if (isEmpty(superCode))
			return true;
		if (isValidClassCode(subCode) && isValidClassCode(superCode)) {
			if (isExplicit(subCode))
				return true;
			return subCode == superCode;
		}
		return false;
	}
	
	public static int mergeFieldCode(ClassMetaData meta, FieldMetaData fmd, 
			int fCode) {
		int cCode = meta.getAccessType();
		try {
			return mergeFieldCode(cCode, fCode);
		} catch (IllegalStateException e) {
            throw new UserException(_loc.get("access-illegal-merge",
                fmd.getFullName(false), toFieldString(fCode), 
                toClassString(cCode)));
		}
	}
	
	/**
     * Merges the field access type with the class access type provided such
     * merge is valid.
     * 
     * @return the modified class access code.
     * @exception if the given codes are not compatible
	 */
	public static int mergeFieldCode(int cCode, int fCode) {
		if (isValidClassCode(cCode) && isValidFieldCode(fCode)) {
			if (isUnknown(cCode)) {
				if (isUnknown(fCode))
					return UNKNOWN;
				return isProperty(fCode) ? PROPERTY : FIELD;
			}
			boolean mixed = isProperty(cCode) != isProperty(fCode);
			if (isExplicit(cCode)) {
				if (mixed) {
					return MIXED | cCode;
				} else {
					return cCode;
				}
			} else { // default, implicit access
				if (fCode == cCode)
					return cCode;
				else
                    throw new IllegalStateException(
                        (_loc.get("access-cannot-merge",
                        toFieldString(fCode), 
                        toClassString(cCode)).toString()));
			}
		}
		return cCode;
	}
	
	public static int getMixedCode(int cCode, int fCode) {
		if (isMixed(cCode) || (isProperty(cCode) == isProperty(fCode)))
			return cCode;
		return MIXED | cCode;
	}
	
	public static int toFieldCode(int code) {
		if (isProperty(code))
			return PROPERTY;
		if (isField(code))
			return FIELD;
		return UNKNOWN;
	}

    public static String toFieldString(int code) {
        if (!isValidFieldCode(code))
            return "invalid code " + code;
        if (code == UNKNOWN)
            return "unknown access";
        if (code == EMPTY)
            return "empty access";
        return (isMixed(code) ? "mixed " : "") 
            + (isExplicit(code) ? "explicit " : "implicit ") 
            + (isField(code) ? "field" : "property")
            + " access";
    }

	public static String toClassString(int code) {
		if (!isValidClassCode(code))
			return "invalid code " + code;
		if (code == UNKNOWN)
			return "unknown access";
		if (code == EMPTY)
			return "empty access";
		return (isMixed(code) ? "mixed " : "") 
		    + (isExplicit(code) ? "explicit " : "implicit ") 
		    + (isField(code) ? "field" : "property")
		    + " access";
	}
}
