package org.apache.openejb.util;

public class LinkedListStack implements Stack {

    private LinkedEntry occupiedEntries;

    private LinkedEntry vacantEntries;

    private int size;

    public LinkedListStack(int initialSize) {
        for (int i = 0; i < initialSize; i++)
            vacantEntries = new LinkedEntry(null, vacantEntries);
    }

    public synchronized Object push(Object object) {
        /* Take an entry from the vacant list and move it to the occupied list. */

        if (vacantEntries == null)
            occupiedEntries = new LinkedEntry(object, occupiedEntries);
        else {

            LinkedEntry entry = vacantEntries;

            vacantEntries = vacantEntries.next;

            occupiedEntries = entry.set(object, occupiedEntries);
        }
        ++size;
        return object;
    }

    public synchronized Object pop() throws java.util.EmptyStackException {
        /* Take an entry from the occupied list and move it to the vacant list. */

        LinkedEntry entry = occupiedEntries;
        if (entry == null) return null;

        occupiedEntries = occupiedEntries.next;

        Object value = entry.value;
        vacantEntries = entry.set(null, vacantEntries);
        --size;
        return value;
    }

    public synchronized int size() {
        return size;
    }

    static class LinkedEntry {

        LinkedEntry next;
        Object value;

        LinkedEntry(Object value, LinkedEntry next) {
            set(value, next);
        }

        LinkedEntry set(Object value, LinkedEntry next) {
            this.next = next;
            this.value = value;
            return this;
        }
    }

}