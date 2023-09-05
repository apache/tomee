/*
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id$
 */

package ee.jakarta.tck.ws.rs.signaturetest;

/**
 * <p>
 * This class is a bit overloaded in that it serves as both a factory and entry
 * point from Ant to handle signature recording.
 * </p>
 *
 * <p>
 * The desired <code>type</code> is provided using a system property with a key
 * of <code>recorder.type</code>. Valid values for the
 * <code>recorder.type</code> property are:
 * <ul>
 * <li>apicheck</li>
 * <li>sigtest</li>
 * </ul>
 * </p>
 * 
 * <p>
 * If the <code>recorder.type</code> property is not set, this factory will
 * return a <code>SignatureTestRecorder</code> using the SigTest framework.
 * </p>
 */
public class RecorderFactory {

  public static final String API_CHECK_RECORDER = "apicheck";

  public static final String SIG_TEST_RECORDER = "sigtest";

  // ---------------------------------------------------------- Public Methods

  /**
   * Returns a {@link Recorder} instance to handle recording signatures based on
   * the value specified via the <code>type</code> argument.
   * 
   * @param type
   *          the type of {@link Recorder} to use
   * @param args
   *          the args to pass to the {@link Recorder}
   * @return a {@link Recorder} instanced based on the <code>type</code>
   *         provided
   */
  public static Recorder getRecorder(String type, String[] args) {

    if (type == null) {
      throw new IllegalArgumentException("'type' cannot be null");
    }

    if (type.equals(API_CHECK_RECORDER)) {
      return new ApiCheckRecorder(args);
    } else if (type.equals(SIG_TEST_RECORDER)) {
      return new SigTestRecorder(args);
    } else {
      throw new IllegalArgumentException("Unknown type: " + type);
    }

  } // END getRecorder

  public static void main(String[] args) {

    String type = System.getProperty("recorder.type", SIG_TEST_RECORDER);
    Recorder recorder = getRecorder(type, args);
    recorder.batchRecord();

  } // END main

}
