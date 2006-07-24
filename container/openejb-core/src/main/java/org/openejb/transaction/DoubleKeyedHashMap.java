/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2006 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id: file,v 1.1 2005/02/18 23:22:00 user Exp $
 */
package org.openejb.transaction;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

/**
 * <b>Really</b> stupid implementation of a double keyed map.
 *
 * @version $Rev: 355877 $ $Date: 2005-12-10 18:48:27 -0800 (Sat, 10 Dec 2005) $
 */
public final class DoubleKeyedHashMap {
    private final Map map = new HashMap();

    public Object put(Object key1, Object key2, Object value) {
        return map.put(new org.openejb.transaction.DoubleKeyedHashMap.Key(key1, key2), value);
    }

    public Object get(Object key1, Object key2) {
        return map.get(new org.openejb.transaction.DoubleKeyedHashMap.Key(key1, key2));
    }

    public Object remove(Object key1, Object key2) {
        return map.remove(new org.openejb.transaction.DoubleKeyedHashMap.Key(key1, key2));
    }

    public Collection values() {
        return map.values();
    }

    public void clear() {
        map.clear();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    private final static class Key {
        private final Object part1;
        private final Object part2;

        public Key(Object part1, Object part2) {
            this.part1 = part1;
            this.part2 = part2;
        }

        public int hashCode() {
            return part1.hashCode() ^ part2.hashCode();
        }

        public boolean equals(Object obj) {
            if (obj instanceof org.openejb.transaction.DoubleKeyedHashMap.Key) {
                org.openejb.transaction.DoubleKeyedHashMap.Key other = (org.openejb.transaction.DoubleKeyedHashMap.Key) obj;
                return this.part1.equals(other.part1) && this.part2.equals(other.part2);
            } else {
                return false;
            }
        }
    }
}
