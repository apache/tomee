/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.rs.ext.interceptor.containerwriter.writerinterceptorcontext;

import java.io.InputStream;
import java.io.IOException;

import ee.jakarta.tck.ws.rs.common.impl.ReplacingOutputStream;
import ee.jakarta.tck.ws.rs.common.provider.StringBean;
import ee.jakarta.tck.ws.rs.common.provider.StringBeanEntityProvider;
import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;
import ee.jakarta.tck.ws.rs.ee.rs.ext.interceptor.writer.TemplateWriterInterceptor;
import ee.jakarta.tck.ws.rs.api.rs.ext.interceptor.TemplateInterceptorBody;
import ee.jakarta.tck.ws.rs.ee.rs.ext.interceptor.containerwriter.WriterClient;
import ee.jakarta.tck.ws.rs.ee.rs.ext.interceptor.writer.writerinterceptorcontext.ContextOperation;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 */
@ExtendWith(ArquillianExtension.class)
public class JAXRSClientIT extends WriterClient<ContextOperation> {

  private static final long serialVersionUID = -8158424518609416304L;

  public JAXRSClientIT() {
    setup();
    setContextRoot(
        "/jaxrs_ee_rs_ext_interceptor_containerwriter_writerinterceptorcontext_web/resource");
  }

  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : "+testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : "+testInfo.getDisplayName());
  }

  @Deployment(testable = false)
  public static WebArchive createDeployment() throws IOException{

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/ext/interceptor/containerwriter/writerinterceptorcontext/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_rs_ext_interceptor_containerwriter_writerinterceptorcontext_web.war");
    archive.addClasses(TSAppConfig.class, 
    JaxrsUtil.class,
    StringBean.class,
    StringBeanEntityProvider.class,
    ReplacingOutputStream.class,
    TemplateWriterInterceptor.class,
    Resource.class);
    archive.addPackages(false, 
      "ee.jakarta.tck.ws.rs.api.rs.ext.interceptor",
      "ee.jakarta.tck.ws.rs.ee.rs.ext.interceptor.writer.writerinterceptorcontext");

    archive.setWebXML(new StringAsset(webXml));
    return archive;

  }


  /* Run test */
  /*
   * @testName: getEntityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:933; JAXRS:JAVADOC:930;
   * 
   * @test_Strategy: Get object to be written as HTTP entity.
   *
   * WriterInterceptor.aroundWriteTo
   */
  @Test
  public void getEntityTest() throws Fault {
    setProperty(Property.SEARCH_STRING, TemplateInterceptorBody.ENTITY);
    invoke(ContextOperation.GETENTITY);
  }

  /*
   * @testName: getHeadersOperationOnlyTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:934; JAXRS:JAVADOC:930;
   * 
   * @test_Strategy: Get mutable map of HTTP headers.
   * 
   * WriterInterceptor.aroundWriteTo
   */
  @Test
  public void getHeadersOperationOnlyTest() throws Fault {
    setProperty(Property.SEARCH_STRING, TemplateInterceptorBody.OPERATION);
    invoke(ContextOperation.GETHEADERS);
  }

  /*
   * @testName: getHeadersTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:934; JAXRS:JAVADOC:930;
   * 
   * @test_Strategy: Get mutable map of HTTP headers.
   *
   * WriterInterceptor.aroundWriteTo
   */
  @Test
  public void getHeadersTest() throws Fault {
    Property p = Property.UNORDERED_SEARCH_STRING;
    setProperty(p, TemplateInterceptorBody.OPERATION);
    for (int i = 0; i != 5; i++)
      setProperty(p, TemplateInterceptorBody.PROPERTY + i);
    invoke(ContextOperation.GETHEADERS);
  }

  /*
   * @testName: getHeadersIsMutableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:934; JAXRS:JAVADOC:930;
   * 
   * @test_Strategy: Get mutable map of HTTP headers.
   *
   * WriterInterceptor.aroundWriteTo
   */
  @Test
  public void getHeadersIsMutableTest() throws Fault {
    setProperty(Property.SEARCH_STRING, TemplateInterceptorBody.PROPERTY);
    invoke(ContextOperation.GETHEADERSISMUTABLE);
  }

  /*
   * @testName: getOutputStreamTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:935; JAXRS:JAVADOC:930;
   * 
   * @test_Strategy: Get the output stream for the object to be written.
   *
   * WriterInterceptor.aroundWriteTo
   */
  @Test
  public void getOutputStreamTest() throws Fault {
    Property p = Property.UNORDERED_SEARCH_STRING;
    setProperty(p, TemplateInterceptorBody.ENTITY);
    setProperty(p, TemplateInterceptorBody.NULL);
    invoke(ContextOperation.GETOUTPUTSTREAM);
  }

  /*
   * @testName: proceedThrowsIOExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:936; JAXRS:JAVADOC:937; JAXRS:JAVADOC:930;
   * JAXRS:JAVADOC:931;
   * 
   * @test_Strategy: Proceed to the next interceptor in the chain.
   * Throws:IOException - if an IO exception arises.
   * 
   * proceed is actually called in every clientwriter.writerinterceptorcontext
   * test
   *
   * WriterInterceptor.aroundWriteTo
   * 
   * WriterInterceptor.aroundWriteTo throws IOException
   */
  @Test
  public void proceedThrowsIOExceptionTest() throws Fault {
    setProperty(Property.SEARCH_STRING, TemplateInterceptorBody.IOE);
    invoke(ContextOperation.PROCEEDTHROWSIOEXCEPTION);
  }

  /*
   * @testName: proceedThrowsWebApplicationExceptionTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:936; JAXRS:JAVADOC:1009; JAXRS:JAVADOC:930;
   * 
   * @test_Strategy: Proceed to the next interceptor in the chain.
   * Throws:WebApplicationException thrown by the wrapped {@code
   * MessageBodyWriter.writeTo} method.
   * 
   * proceed is actually called in every clientwriter.writerinterceptorcontext
   * test
   *
   * WriterInterceptor.aroundWriteTo
   */
  @Test
  public void proceedThrowsWebApplicationExceptionTest() throws Fault {
    setProperty(Property.SEARCH_STRING, TemplateInterceptorBody.WAE);
    invoke(ContextOperation.PROCEEDTHROWSWEBAPPEXCEPTION);
  }

  /*
   * @testName: setEntityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:938; JAXRS:JAVADOC:930;
   * 
   * @test_Strategy: Update object to be written as HTTP entity.
   *
   * WriterInterceptor.aroundWriteTo
   */
  @Test
  public void setEntityTest() throws Fault {
    setProperty(Property.SEARCH_STRING, TemplateInterceptorBody.OPERATION);
    invoke(ContextOperation.SETENTITY);
  }

  /*
   * @testName: setOutputStreamTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:939; JAXRS:JAVADOC:930;
   * 
   * @test_Strategy: Update the output stream for the object to be written.
   *
   * WriterInterceptor.aroundWriteTo
   */
  @Test
  public void setOutputStreamTest() throws Fault {
    setProperty(Property.SEARCH_STRING,
        TemplateInterceptorBody.ENTITY.replace('t', 'x'));
    invoke(ContextOperation.SETOUTPUTSTREAM);
  }

}
