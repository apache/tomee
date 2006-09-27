package org.apache.openejb.core;

public class Operations {
    final public static byte OP_BUSINESS = (byte) 1;
    final public static byte OP_AFTER_BEGIN = (byte) 2;
    final public static byte OP_AFTER_COMPLETION = (byte) 3;
    final public static byte OP_BEFORE_COMPLETION = (byte) 4;
    final public static byte OP_REMOVE = (byte) 5;
    final public static byte OP_SET_CONTEXT = (byte) 6;
    final public static byte OP_UNSET_CONTEXT = (byte) 7;
    final public static byte OP_CREATE = (byte) 8;
    final public static byte OP_POST_CREATE = (byte) 9;
    final public static byte OP_ACTIVATE = (byte) 10;
    final public static byte OP_PASSIVATE = (byte) 11;
    final public static byte OP_FIND = (byte) 12;
    final public static byte OP_HOME = (byte) 13;
    final public static byte OP_LOAD = (byte) 14;
    final public static byte OP_STORE = (byte) 15;

}