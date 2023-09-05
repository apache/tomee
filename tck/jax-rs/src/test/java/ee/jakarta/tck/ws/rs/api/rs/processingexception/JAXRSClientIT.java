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

package ee.jakarta.tck.ws.rs.api.rs.processingexception;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.ProcessingException;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
public class JAXRSClientIT extends JAXRSCommonClient {

  private static final long serialVersionUID = -9156519616224592459L;

  public JAXRSClientIT() {
    setContextRoot("/jaxrs_api_rs_processingexception_web");
  }

  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : "+testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : "+testInfo.getDisplayName());
  }

  /*
   * @testName: constructorWithRuntimeExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1010;
   * 
   * @test_Strategy: Constructs a new JAX-RS runtime processing exception with
   * the specified cause and a detail message of (cause==null ? null :
   * cause.toString())
   */
  @Test
  public void constructorWithRuntimeExceptionTest() throws Fault {
    IllegalStateException ile = new IllegalStateException("TCK exception");
    ProcessingException mpe = new ProcessingException(ile);
    assertTrue(mpe.getCause().equals(ile),
        "getCause does not work for ProcessingException and RuntimeException cause");
    assertTrue(mpe.getMessage().equals(ile.toString()),
        "getMessage does not work for ProcessingException and RuntimeException cause");
  }

  /*
   * @testName: constructorWithCheckedExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1010;
   * 
   * @test_Strategy: Constructs a new JAX-RS runtime processing exception with
   * the specified cause and a detail message of (cause==null ? null :
   * cause.toString())
   */
  @Test
  public void constructorWithCheckedExceptionTest() throws Fault {
    IOException ioe = new IOException("TCK exception");
    ProcessingException mpe = new ProcessingException(ioe);
    assertTrue(mpe.getCause().equals(ioe),
        "getCause does not work for ProcessingException and CheckedException cause");
    assertTrue(mpe.getMessage().equals(ioe.toString()),
        "getMessage does not work for ProcessingException and CheckedException cause");
  }

  /*
   * @testName: constructorWithNullThrowableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1010;
   * 
   * @test_Strategy: Constructs a new JAX-RS runtime processing exception with
   * the specified cause and a detail message of (cause==null ? null :
   * cause.toString())
   */
  @Test
  public void constructorWithNullThrowableTest() throws Fault {
    ProcessingException mpe = new ProcessingException((Throwable) null);
    assertTrue(mpe.getCause() == null,
        "getCause does not work for ProcessingException and null cause");
    assertTrue(mpe.getMessage() == null,
        "getMessage does not work for ProcessingException and null cause");
  }

  /*
   * @testName: constructorWithNullThrowableNullMessageTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1011;
   * 
   * @test_Strategy: Constructs a new JAX-RS runtime processing exception with
   * the specified detail message and cause.
   */
  @Test
  public void constructorWithNullThrowableNullMessageTest() throws Fault {
    ProcessingException mpe = new ProcessingException((String) null,
        (Throwable) null);
    assertTrue(mpe.getCause() == null,
        "getCause does not work for ProcessingException and null cause and null message");
    assertTrue(mpe.getMessage() == null,
        "getMessage does not work for ProcessingException and null cause and null message");
  }

  /*
   * @testName: constructorWithNullThrowableNotNullMessageTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1011;
   * 
   * @test_Strategy: Constructs a new JAX-RS runtime processing exception with
   * the specified detail message and cause.
   */
  @Test
  public void constructorWithNullThrowableNotNullMessageTest() throws Fault {
    String msg = "TCK Message";
    ProcessingException mpe = new ProcessingException(msg, (Throwable) null);
    assertTrue(mpe.getCause() == null,
        "getCause does not work for ProcessingException and null cause and not null message");
    assertTrue(mpe.getMessage().equals(msg),
        "getMessage does not work for ProcessingException and null cause and not null message");
  }

  /*
   * @testName: constructorWithRuntimeExceptionNullMessageTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1011;
   * 
   * @test_Strategy: Constructs a new JAX-RS runtime processing exception with
   * the specified detail message and cause.
   */
  @Test
  public void constructorWithRuntimeExceptionNullMessageTest() throws Fault {
    IllegalStateException ise = new IllegalStateException(
        "JAXRS TCK exception");
    ProcessingException mpe = new ProcessingException((String) null, ise);
    assertTrue(mpe.getCause().equals(ise),
        "getCause does not work for ProcessingException and RuntimeException and null message");
    assertTrue(mpe.getMessage() == null,
        "getMessage does not work for ProcessingException and RuntimeException and null message");
  }

  /*
   * @testName: constructorWithCheckedExceptionNullMessageTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1011;
   * 
   * @test_Strategy: Constructs a new JAX-RS runtime processing exception with
   * the specified detail message and cause.
   */
  @Test
  public void constructorWithCheckedExceptionNullMessageTest() throws Fault {
    IOException ioe = new IOException("JAXRS TCK exception");
    ProcessingException mpe = new ProcessingException((String) null, ioe);
    assertTrue(mpe.getCause().equals(ioe),
        "getCause does not work for ProcessingException and CheckedException and null message");
    assertTrue(mpe.getMessage() == null,
        "getMessage does not work for ProcessingException and CheckedException and null message");
  }

  /*
   * @testName: constructorWithRuntimeExceptionAndNotNullMessageTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1011;
   * 
   * @test_Strategy: Constructs a new JAX-RS runtime processing exception with
   * the specified detail message and cause.
   */
  @Test
  public void constructorWithRuntimeExceptionAndNotNullMessageTest()
      throws Fault {
    String msg = "TCK Message";
    IllegalStateException ise = new IllegalStateException(
        "JAXRS TCK exception");
    ProcessingException mpe = new ProcessingException(msg, ise);
    assertTrue(mpe.getCause().equals(ise),
        "getCause does not work for ProcessingException and RuntimeException and not null message");
    assertTrue(mpe.getMessage().equals(msg),
        "getMessage does not work for ProcessingException and RuntimeException and not null message");
  }

  /*
   * @testName: constructorWithCheckedExceptionAndNotNullMessageTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1011;
   * 
   * @test_Strategy: Constructs a new JAX-RS runtime processing exception with
   * the specified detail message and cause.
   */
  @Test
  public void constructorWithCheckedExceptionAndNotNullMessageTest()
      throws Fault {
    String msg = "TCK Message";
    IOException ioe = new IOException("JAXRS TCK exception");
    ProcessingException mpe = new ProcessingException(msg, ioe);
    assertTrue(mpe.getCause().equals(ioe),
        "getCause does not work for ProcessingException and CheckedException and not null message");
    assertTrue(mpe.getMessage().equals(msg),
        "getMessage does not work for ProcessingException and CheckedException and not null message");
  }

  /*
   * @testName: constructorWithNotNullMessageTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1012;
   * 
   * @test_Strategy: Constructs a new JAX-RS runtime processing exception with
   * the specified detail message.
   */
  @Test
  public void constructorWithNotNullMessageTest() throws Fault {
    String msg = "TCK Message";
    ProcessingException mpe = new ProcessingException(msg);
    assertTrue(mpe.getCause() == null,
        "getCause does not work for ProcessingException and not null message");
    assertTrue(mpe.getMessage().equals(msg),
        "getMessage does not work for ProcessingException and not null message");
  }

  /*
   * @testName: constructorWithNullMessageTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:1012;
   * 
   * @test_Strategy: Constructs a new JAX-RS runtime processing exception with
   * the specified detail message.
   */
  @Test
  public void constructorWithNullMessageTest() throws Fault {
    ProcessingException mpe = new ProcessingException((String) null);
    assertTrue(mpe.getCause() == null,
        "getCause does not work for ProcessingException and null message");
    assertTrue(mpe.getMessage() == null,
        "getMessage does not work for ProcessingException and null message");
  }
}
