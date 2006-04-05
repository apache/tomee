/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */
package org.openejb.cache;

import java.util.HashMap;

/**
 * This is a very simple implementation of InstanceCache designed for raw flat
 * out speed.  It does not directly support passivation or have any storage
 * limits.
 *
 *
 * @version $Revision$ $Date$
 */
public final class LRUInstanceCache implements InstanceCache {
    private final HashMap active = new HashMap();
    private final HashMap inactive = new HashMap();
    private final Entry header = new Entry();

    public synchronized int size() {
        return active.size() + inactive.size();
    }

    public synchronized void putActive(Object key, Object value) {
        //assert (key != null);
        //assert (value != null);

        // if it is in the inactive list remove it and add it to the active list
        Entry entry = (Entry) inactive.remove(key);
        if (entry != null) {
            // if we have an entry we remove it from the linked list
            entry.remove();
        }

        // now stick it in the active map
        active.put(key, value);
    }

    public synchronized void putInactive(Object key, Object value) {
        //assert (key != null);
        //assert (value != null);

        // remove it from the active list if it was there
        active.remove(key);

        // create a new link entry, put it in the hashTable and add it to the linked list
        Entry entry = new Entry(key, value);
        inactive.put(key, entry);
        header.addAfter(entry);
    }

    public synchronized Object get(Object key) {
        //assert (key != null);
        Object value = active.get(key);
        if (value != null) {
            return value;
        }

        // if it is in the inactive list remove it and add it to the active list
        Entry entry = (Entry) inactive.remove(key);
        if (entry != null) {
            // if we have an entry we need to unwrap it
            value = entry.getValue();

            // remove the entry from the list
            entry.remove();

            // now put it in the active map
            active.put(key, value);
        }
        return value;
    }

    public synchronized Object remove(Object key) {
        //assert (key != null);

        // first check the active map
        Object value = active.remove(key);

        // also check for an entry in the inactive map
        Entry entry = (Entry) inactive.remove(key);
        if (entry != null) {
            // this should never happen because we don't let a key be in both maps
            //assert (value == null);

            // unwrap the entry and remove it from thhe linked list
            value = entry.getValue();
            entry.remove();
        }
        return value;
    }

    public synchronized Object peek(Object key) {
        //assert (key != null);

        // first check the active map
        Object value = active.get(key);
        if (value != null) {
            return value;
        }

        // wasn't there - check for an entry in the inactive map
        Entry entry = (Entry) inactive.get(key);
        if (entry != null) {
            // unwrap the entry
            return entry.getValue();
        }
        return null;
    }

    public synchronized boolean isActive(Object key) {
        //assert (key != null);
        return active.containsKey(key);
    }

    public void run(LRURunner runner) {
        Entry entry = header;
        Object key;
        Object value;

        while (runner.shouldContinue()) {
            synchronized (this) {
                do {
                    // even though this entry may have been removed from the list in
                    // the previous iteration, it maintains a link to the previous node
                    // in the hierarchy, so it can alyways find its way back
                    entry = entry.getPrevious();

                    // we only allow the runner to walk the list once
                    if (entry == header) {
                        return;
                    }
                    key = entry.getKey();
                    value = entry.getValue();
                } while (entry.isRemoved() || !runner.shouldRemove(key, value));

                // remove the entry from the map
                inactive.remove(key);

                // remove the entry from the list
                entry.remove();
            }
            runner.remove(key, value);
        }
    }

    private static final class Entry {
        private Entry next;
        private Entry previous;
        private Object key;
        private Object value;
        private boolean removed = false;

        public Entry() {
            next = this;
            previous = this;
        }

        public Entry(Object key, Object value) {
            //assert (key!=null);
            //assert (value!=null);
            this.key = key;
            this.value = value;
        }

        public Entry getPrevious() {
            return previous;
        }

        public Entry getNext() {
            return next;
        }

        public Object getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        public boolean isRemoved() {
            return removed;
        }

        public void addAfter(Entry entry) {
            //assert (!removed);
            //assert (!entry.removed);
            //assert (entry.next == null);
            //assert (entry.previous == null);

            // update the entry's pointers
            entry.previous = this;
            entry.next = next;

            // update my next's pointers
            next.previous = entry;

            // update my pointers
            next = entry;
        }

        public void addBefore(Entry entry) {
            //assert (!removed);
            //assert (!entry.removed);
            //assert (entry.next == null);
            //assert (entry.previous == null);

            // update the entry's pointers
            entry.next = this;
            entry.previous = previous;

            // update my previous's pointer
            previous.next = entry;

            // update my pointer
            previous = entry;
        }

        public void remove() {
            if (!removed) {
                previous.next = next;
                next.previous = previous;
                next = null;
                // leave previous pointer so we can find our way back into the list

                key = null;
                value = null;
                removed = true;
            }
        }
    }
}
