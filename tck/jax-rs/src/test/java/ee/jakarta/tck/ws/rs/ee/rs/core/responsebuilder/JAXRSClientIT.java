/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.rs.core.responsebuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.util.Calendar;
import java.util.Date;
import java.io.InputStream;

import ee.jakarta.tck.ws.rs.common.client.JaxrsCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

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
public class JAXRSClientIT extends JaxrsCommonClient {

  private static final long serialVersionUID = 1L;

  public JAXRSClientIT() {
    setup();
    setContextRoot("/jaxrs_ee_core_responsebuilder_web/resource");
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

    InputStream inStream = JAXRSClientIT.class.getClassLoader().getResourceAsStream("ee/jakarta/tck/ws/rs/ee/rs/core/responsebuilder/web.xml.template");
    String webXml = editWebXmlString(inStream);

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_ee_core_responsebuilder_web.war");
    archive.addClasses(TSAppConfig.class, Resource.class, AnnotatedClass.class, DateContainerReaderWriter.class);
    archive.setWebXML(new StringAsset(webXml));
    return archive;

  }


  /* Run test */

  /*
   * @testName: entityObjectTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:879;
   * 
   * @test_Strategy: Set the message entity content encoding.
   */
  @Test
  public void entityObjectTest() throws Fault {
    Date date = Calendar.getInstance().getTime();
    String entity = DateContainerReaderWriter.dateToString(date);
    StringBuilder sb = new StringBuilder();
    DateClientReaderWriter rw = new DateClientReaderWriter(sb);
    addProvider(rw);

    setProperty(Property.REQUEST, buildRequest(Request.POST, "entity"));
    setProperty(Property.CONTENT, entity);
    invoke();

    Response response = getResponse();
    Date responseDate = response.readEntity(Date.class);
    assertTrue(date.equals(responseDate), "entity date"+ date+
        "differs from acquired"+ responseDate);

    Annotation[] annotations = AnnotatedClass.class.getAnnotations();
    for (Annotation annotation : annotations) {
      String name = annotation.annotationType().getName();
      assertTrue(sb.toString().contains(name), sb+ "does not contain"+ name+
          ", annotations not passed to MessageBodyWriter?");
    }
  }

  // ////////////////////////////////////////////////////////////////////
  protected <T> GenericType<T> generic(Class<T> clazz) {
    return new GenericType<T>(clazz);
  }

  protected String readLine(Reader reader) throws Fault {
    String line = null;
    BufferedReader buffered = new BufferedReader(reader);
    try {
      line = buffered.readLine();
    } catch (IOException e) {
      try {
        buffered.close();
      } catch (IOException ie) {
      }
      throw new Fault(e);
    }
    return line;
  }
}
