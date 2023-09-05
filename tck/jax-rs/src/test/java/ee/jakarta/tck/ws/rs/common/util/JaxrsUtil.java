/*
 * Copyright (c) 2012, 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package ee.jakarta.tck.ws.rs.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

public abstract class JaxrsUtil {

  public static final int freePort() throws IOException {
    try (final ServerSocket serverSocket = new ServerSocket(0)) {
      return serverSocket.getLocalPort();
    }
  }

  public static final//
  String readFromStream(InputStream stream) throws IOException {
    InputStreamReader isr = new InputStreamReader(stream);
    return readFromReader(isr);
  }

  public static final//
  String readFromReader(Reader reader) throws IOException {
    BufferedReader br = new BufferedReader(reader);
    String entity = br.readLine();
    br.close();
    return entity;
  }

  public static final//
  String readFromFile(File file) throws IOException {
    FileReader reader = new FileReader(file);
    return readFromReader(reader);
  }

  public static final <T> //
  String iterableToString(String separator, Iterable<T> collection) {
    if (collection != null)
      return iterableToString(separator, collection.iterator());
    return "";
  }

  public static final <T> //
  String iterableToString(String separator, Iterator<T> iterator) {
    StringBuilder sb = new StringBuilder();
    while (iterator.hasNext()) {
      T item = iterator.next();
      if (item != null) {
        String appendable = item.toString();
        sb.append(appendable);
        if (iterator.hasNext())
          sb.append(separator);
      }
    }
    return sb.toString();
  }

  public static final <T> //
  String enumerationToString(String separator, Enumeration<T> enumeration) {
    StringBuilder sb = new StringBuilder();
    if (enumeration != null)
      while (enumeration.hasMoreElements()) {
        T item = enumeration.nextElement();
        if (item != null) {
          String appendable = item.toString();
          sb.append(appendable);
          if (enumeration.hasMoreElements())
            sb.append(separator);
        }
      }
    return sb.toString();
  }

  public static final //
  String iterableToString(String separator, Object... collection) {
    StringBuilder sb = new StringBuilder();
    if (collection != null)
      for (int i = 0; i != collection.length; i++) {
        Object item = collection[i];
        if (item != null) {
          String appendable = item.toString();
          sb.append(appendable);
          if (i != collection.length - 1)
            sb.append(separator);
        }
      }
    return sb.toString();
  }

  public static final TimeZone findTimeZoneInDate(String date) {
    StringBuilder sb = new StringBuilder();
    StringBuilder dateBuilder = new StringBuilder(date.trim()).reverse();
    int index = 0;
    char c;
    while ((c = dateBuilder.charAt(index++)) != ' ') {
      sb.append(c);
    }
    TimeZone timezone = TimeZone.getTimeZone(sb.reverse().toString());
    return timezone;
  }

  public static final String mapToString(java.util.Map<?, ?> map) {
    StringBuilder sb = new StringBuilder();
    if (map != null)
      for (Object key : map.keySet()) {
        sb.append("[").append(key).append(" : ");
        Object value = map.get(key);
        sb.append(toString(value)).append("]");
      }
    return sb.toString();
  }

  public static final DateFormat createDateFormat(TimeZone timezone) {
    SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",
        Locale.US);
    sdf.setTimeZone(timezone);
    return sdf;
  }

  public static String toString(Object object) {
    String to = null;
    if (object == null)
      to = "null";
    else if (Iterable.class.isInstance(object))
      to = iterableToString(" ", (Iterable<?>) object);
    else if (Enumeration.class.isInstance(object))
      to = enumerationToString(" ", (Enumeration<?>) object);
    else if (java.util.Map.class.isInstance(object))
      to = mapToString((java.util.Map<?, ?>) object);
    else if (Iterator.class.isInstance(object))
      to = iterableToString(" ", (Iterator<?>) object);
    else if (Array.class.isInstance(object))
      to = iterableToString(" ", (Object[]) object);
    else if (object.getClass().isArray())
      to = primitiveArrayToString(object);
    else
      to = String.valueOf(object);
    return to;
  }

  public static String primitiveArrayToString(Object array) {
    String to = null;
    if (array == null)
      to = "null";
    else if (array.getClass().isArray()) {
      String sarray = array.toString();
      if (sarray.startsWith("[I"))
        to = Arrays.toString((int[]) array);
      else if (sarray.startsWith("[B"))
        to = Arrays.toString((byte[]) array);
      else if (sarray.startsWith("[J"))
        to = Arrays.toString((long[]) array);
      else if (sarray.startsWith("[C"))
        to = Arrays.toString((char[]) array);
      else if (sarray.startsWith("[S"))
        to = Arrays.toString((short[]) array);
      else if (sarray.startsWith("[F"))
        to = Arrays.toString((float[]) array);
      else if (sarray.startsWith("[D"))
        to = Arrays.toString((double[]) array);
      else if (sarray.startsWith("[Z"))
        to = Arrays.toString((boolean[]) array);
      else if (sarray.startsWith("[L"))
        to = Arrays.toString((Object[]) array);
    } else
      to = String.valueOf(array);
    return to;
  }
}
