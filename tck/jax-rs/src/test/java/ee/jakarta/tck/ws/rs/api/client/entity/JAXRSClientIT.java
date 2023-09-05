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

package ee.jakarta.tck.ws.rs.api.client.entity;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.Variant;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
public class JAXRSClientIT extends JAXRSCommonClient {

  private static final long serialVersionUID = 3872631127958907381L;

  @BeforeEach
  void logStartTest(TestInfo testInfo) {
    TestUtil.logMsg("STARTING TEST : "+testInfo.getDisplayName());
  }

  @AfterEach
  void logFinishTest(TestInfo testInfo) {
    TestUtil.logMsg("FINISHED TEST : "+testInfo.getDisplayName());
  }

  /*
   * @testName: entityMediaTypeTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:492; JAXRS:JAVADOC:504;
   * 
   * @test_Strategy: Create an entity using a supplied content media type.
   *
   * Get entity media type.
   */
  @Test
  public void entityMediaTypeTest() throws Fault {
    Entity<String> entity;
    MediaType[] mTypes = getMediaTypes(MediaType.class);
    for (MediaType type : mTypes) {
      entity = Entity.entity("entity", type);
      assertEntity(entity, "entity");
      assertMediaType(entity, type.toString());
    }
  }

  /*
   * @testName: entityMediaTypeGetEntityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:492; JAXRS:JAVADOC:502;
   * 
   * @test_Strategy: Create an entity using a supplied content media type.
   * 
   * Get entity data.
   */
  @Test
  public void entityMediaTypeGetEntityTest() throws Fault {
    Entity<?> entity;
    Object[] entities = getEntities();
    for (int i = 0; i != ENTITY_VALUES.length; i++) {
      entity = Entity.entity(entities[i], MediaType.WILDCARD_TYPE);
      assertEntity(entity, ENTITY_VALUES[i]);
    }
  }

  /*
   * @testName: entityMediaTypeAnnotationsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:493; JAXRS:JAVADOC:504; JAXRS:JAVADOC:500;
   * 
   * @test_Strategy: Create an entity using a supplied content media type.
   *
   * Get entity media type. Get the entity annotations.
   */
  @Test
  public void entityMediaTypeAnnotationsTest() throws Fault {
    Entity<String> entity;
    MediaType[] mTypes = getMediaTypes(MediaType.class);
    Annotation[] annotations = AnnotatedClass.class.getAnnotations();
    for (MediaType type : mTypes) {
      entity = Entity.entity("entity", type, annotations);
      assertEntity(entity, "entity");
      assertMediaType(entity, type.toString());
      assertAnnotations(entity, annotations);
    }
  }

  /*
   * @testName: entityMediaTypeAnnotationsDifferentEntitiesTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:493; JAXRS:JAVADOC:504; JAXRS:JAVADOC:500;
   * 
   * @test_Strategy: Create an entity using a supplied content media type.
   *
   * Get entity media type. Get the entity annotations.
   */
  @Test
  public void entityMediaTypeAnnotationsDifferentEntitiesTest() throws Fault {
    Entity<Object> entity;
    Annotation[] annotations = AnnotatedClass.class.getAnnotations();
    Object[] entities = getEntities();
    for (int i = 0; i != ENTITY_VALUES.length; i++) {
      entity = Entity.entity(entities[i], MediaType.WILDCARD_TYPE, annotations);
      assertEntity(entity, ENTITY_VALUES[i]);
      assertMediaType(entity, MediaType.WILDCARD);
      assertAnnotations(entity, annotations);
    }
  }

  /*
   * @testName: entityStringTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:494; JAXRS:JAVADOC:504;
   * 
   * @test_Strategy: Create an entity using a supplied content media type.
   *
   * Get entity media type.
   */
  @Test
  public void entityStringTest() throws Fault {
    Entity<String> entity;
    String[] mTypes = getMediaTypes(String.class);
    for (String type : mTypes) {
      entity = Entity.entity("entity", type);
      assertEntity(entity, "entity");
      assertMediaType(entity, type);
    }
  }

  /*
   * @testName: entityStringGetEntityTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:494; JAXRS:JAVADOC:502;
   * 
   * @test_Strategy: Create an entity using a supplied content media type.
   * 
   * Get entity data.
   */
  @Test
  public void entityStringGetEntityTest() throws Fault {
    Entity<?> entity;
    Object[] entities = getEntities();
    for (int i = 0; i != ENTITY_VALUES.length; i++) {
      entity = Entity.entity(entities[i], MediaType.WILDCARD);
      assertEntity(entity, ENTITY_VALUES[i]);
    }
  }

  /*
   * @testName: entityStringThrowsExceptionWhenNullTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:494;
   * 
   * @test_Strategy: Create an entity using a supplied content media type.
   * throws IllegalArgumentException - if the supplied string cannot be parsed
   * or is null.
   */
  @Test
  public void entityStringThrowsExceptionWhenNullTest() throws Fault {
    try {
      Entity.entity("entity", (String) null);
      throw new Fault("IllegalArgumentException has not been thrown");
    } catch (IllegalArgumentException e) {
      logMsg("IllegalArgumentException has been thrown as expected");
    }
  }

  /*
   * @testName: entityStringThrowsExceptionWhenUnparsableTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:494;
   * 
   * @test_Strategy: Create an entity using a supplied content media type.
   * throws IllegalArgumentException - if the supplied string cannot be parsed
   * or is null.
   */
  @Test
  public void entityStringThrowsExceptionWhenUnparsableTest() throws Fault {
    try {
      Entity.entity("entity", "\\//\\");
      throw new Fault("IllegalArgumentException has not been thrown");
    } catch (IllegalArgumentException e) {
      logMsg("IllegalArgumentException has been thrown as expected");
    }
  }

  /*
   * @testName: entityVariantTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:496; JAXRS:JAVADOC:501; JAXRS:JAVADOC:502;
   * JAXRS:JAVADOC:503; JAXRS:JAVADOC:504; JAXRS:JAVADOC:505;
   * 
   * @test_Strategy: Create an entity using a supplied content media type.
   */
  @Test
  public void entityVariantTest() throws Fault {
    Entity<?> entity;
    Object[] pairs = getEntities();
    MediaType[] media = getMediaTypes(MediaType.class);
    for (int i = 0; i != pairs.length; i++)
      for (int j = 0; j != media.length; j++)
        for (int k = 0; k != LANGUAGES.length; k++)
          for (int l = 0; l != ENCODINGS.length; l++) {
            Variant variant = new Variant(media[j], LANGUAGES[k], ENCODINGS[l]);
            entity = Entity.entity(pairs[i], variant);
            assertEntity(entity, ENTITY_VALUES[i]);
            assertMediaType(entity, media[j].toString());
            assertLanguages(entity, LANGUAGES[k]);
            assertEncoding(entity, ENCODINGS[l]);
            assertVariant(entity, variant);
          }
  }

  /*
   * @testName: entityVariantAnnotationsTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:497; JAXRS:JAVADOC:500; JAXRS:JAVADOC:501;
   * JAXRS:JAVADOC:502; JAXRS:JAVADOC:503; JAXRS:JAVADOC:504; JAXRS:JAVADOC:505;
   * 
   * @test_Strategy: Create an entity using a supplied content media type.
   */
  @Test
  public void entityVariantAnnotationsTest() throws Fault {
    Entity<?> entity;
    Object[] pairs = getEntities();
    MediaType[] media = getMediaTypes(MediaType.class);
    Annotation[] annotations = AnnotatedClass.class.getAnnotations();
    for (int i = 0; i != pairs.length; i++)
      for (int j = 0; j != media.length; j++)
        for (int k = 0; k != LANGUAGES.length; k++)
          for (int l = 0; l != ENCODINGS.length; l++) {
            Variant variant = new Variant(media[j], LANGUAGES[k], ENCODINGS[l]);
            entity = Entity.entity(pairs[i], variant, annotations);
            logMsg(pairs[i], media[j], LANGUAGES[k], ENCODINGS[l]);
            assertEntity(entity, ENTITY_VALUES[i]);
            assertMediaType(entity, media[j].toString());
            assertLanguages(entity, LANGUAGES[k]);
            assertEncoding(entity, ENCODINGS[l]);
            assertVariant(entity, variant);
            assertAnnotations(entity, annotations);
          }
  }

  /*
   * @testName: formFormTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:498;
   * 
   * @test_Strategy: Create an form entity.
   * rs.MediaType#APPLICATION_FORM_URLENCODED form entity.
   */
  @Test
  public void formFormTest() throws Fault {
    Entity<?> entity;
    entity = Entity.form(new Form());
    assertMediaType(entity, MediaType.APPLICATION_FORM_URLENCODED);
  }

  /*
   * @testName: formMultivaluedMapTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:499;
   * 
   * @test_Strategy: Create an form entity.
   * rs.MediaType#APPLICATION_FORM_URLENCODED form entity.
   */
  @Test
  public void formMultivaluedMapTest() throws Fault {
    Entity<?> entity;
    entity = Entity.form(new MultivaluedHashMap<String, String>());
    assertMediaType(entity, MediaType.APPLICATION_FORM_URLENCODED);
  }

  /*
   * @testName: htmlTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:506;
   * 
   * @test_Strategy: Create an form entity. .rs.core.MediaType#TEXT_HTML entity
   */
  @Test
  public void htmlTest() throws Fault {
    Entity<?> entity;
    Object[] entities = getEntities();
    for (int i = 0; i != entities.length; i++) {
      entity = Entity.html(entities[i]);
      assertMediaType(entity, MediaType.TEXT_HTML);
      assertEntity(entity, ENTITY_VALUES[i]);
    }
  }

  /*
   * @testName: jsonTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:507;
   * 
   * @test_Strategy: Create an form entity. .rs.core.MediaType#APPLICATION_JSON
   * entity
   */
  @Test
  public void jsonTest() throws Fault {
    Entity<?> entity;
    Object[] entities = getEntities();
    for (int i = 0; i != entities.length; i++) {
      entity = Entity.json(entities[i]);
      assertMediaType(entity, MediaType.APPLICATION_JSON);
      assertEntity(entity, ENTITY_VALUES[i]);
    }
  }

  /*
   * @testName: textTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:508;
   * 
   * @test_Strategy: Create an form entity. .rs.core.MediaType#TEXT_PLAIN
   * entity.
   */
  @Test
  public void textTest() throws Fault {
    Entity<?> entity;
    Object[] entities = getEntities();
    for (int i = 0; i != entities.length; i++) {
      entity = Entity.text(entities[i]);
      assertMediaType(entity, MediaType.TEXT_PLAIN);
      assertEntity(entity, ENTITY_VALUES[i]);
    }
  }

  /*
   * @testName: xhtmlTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:509;
   * 
   * @test_Strategy: Create an form entity.
   * .rs.core.MediaType#APPLICATION_XHTML_XML entity
   */
  @Test
  public void xhtmlTest() throws Fault {
    Entity<?> entity;
    Object[] entities = getEntities();
    for (int i = 0; i != entities.length; i++) {
      entity = Entity.xhtml(entities[i]);
      assertMediaType(entity, MediaType.APPLICATION_XHTML_XML);
      assertEntity(entity, ENTITY_VALUES[i]);
    }
  }

  /*
   * @testName: xmlTest
   * 
   * @assertion_ids: JAXRS:JAVADOC:510;
   * 
   * @test_Strategy: Create an form entity. .rs.core.MediaType#APPLICATION_XML
   * entity
   */
  @Test
  public void xmlTest() throws Fault {
    Entity<?> entity;
    Object[] entities = getEntities();
    for (int i = 0; i != entities.length; i++) {
      entity = Entity.xml(entities[i]);
      assertMediaType(entity, MediaType.APPLICATION_XML);
      assertEntity(entity, ENTITY_VALUES[i]);
    }
  }

  // ///////////////////////////////////////////////////////////////////////

  protected <T> void assertEntity(Entity<T> entity, String original)
      throws Fault {
    assertTrue(entity.getEntity().toString().contains(original),
        entity.getEntity().toString() + " does not contain expected " + original);
    logMsg("Found expected", original);
  }

  protected <T> void assertMediaType(Entity<T> entity, String original)
      throws Fault {
    assertTrue(entity.getMediaType().toString().equals(original),
        "MediaType retrieved from entity " + entity.getMediaType() +
        " differes from " + original);
    logMsg("Sucessfully retrieved MediaType", original);
  }

  protected <T> void assertAnnotation(Entity<T> entity, Annotation original)
      throws Fault {
    Comparator<Annotation> comparator = new Comparator<Annotation>() {
      @Override
      public int compare(Annotation o1, Annotation o2) {
        return o1.toString().compareTo(o2.toString());
      }
    };
    Annotation[] annotations = entity.getAnnotations();
    Arrays.sort(annotations, comparator);
    int index = Arrays.binarySearch(annotations, original, comparator);
    assertTrue(index != -1, "Annotation " + original + " not found");
    logMsg("Sucessfully retrieved Annotation", original);
  }

  protected <T> void assertAnnotations(Entity<T> entity, Annotation[] original)
      throws Fault {
    for (Annotation annotation : original)
      assertAnnotation(entity, annotation);
  }

  protected <T> void assertLanguages(Entity<T> entity, Locale original)
      throws Fault {
    assertTrue(entity.getLanguage().equals(original),
        "Language retrieved from entity " + entity.getLanguage() + " differes from " +
        original);
    logMsg("Sucessfully retrieved Language", original);
  }

  protected <T> void assertEncoding(Entity<T> entity, String original)
      throws Fault {
    assertTrue(entity.getEncoding().equals(original),
        "Encoding retrieved from entity " + entity.getEncoding() + " differes from " +
        original);
    logMsg("Sucessfully retrieved Encoding", original);
  }

  protected <T> void assertVariant(Entity<T> entity, Variant original)
      throws Fault {
    assertTrue(entity.getVariant() == original,
        "Variant retrieved from entity " + entity.getVariant() + " differes from " +
        original);
    logMsg("Sucessfully retrieved Variant", original);
  }

  /**
   * MediaType should either be an enum or have the values method It's neither
   * so this method uses reflection to acquire public static fields of given
   * class, either MediaType or String.
   * 
   * @param clazz
   *          Class of the public static Field
   * @return array of the Fields of a given class
   * @throws Fault
   */
  @SuppressWarnings("unchecked")
  protected <T> T[] getMediaTypes(Class<T> clazz) throws Fault {
    MediaType type = MediaType.WILDCARD_TYPE;
    List<T> list = new LinkedList<T>();
    for (Field field : MediaType.class.getFields())
      if (Modifier.isStatic(field.getModifiers())
          && Modifier.isPublic(field.getModifiers())
          && field.getType().equals(clazz))
        try {
          T value = (T) field.get(type);
          if (value.toString().contains("/"))
            list.add(value);
        } catch (Exception e) {
          throw new Fault(e);
        }
    T[] array = (T[]) Array.newInstance(clazz, 0);
    return list.toArray(array);
  }

  /** Get different objects and their class for entities to be created */
  protected Object[] getEntities() {
    String sEntity = ENTITY_VALUES[0];
    InputStream isEntity = new ByteArrayInputStream(
        ENTITY_VALUES[1].getBytes()) {
      @Override
      public String toString() {
        String line = null;
        try {
          line = JaxrsUtil.readFromStream(this);
          reset();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        return line;
      }
    };
    // Cannot have serializable inner class - findbugs
    Serializable serEntity = new SerializableClass();
    StringBuilder builderEntity = new StringBuilder().append(ENTITY_VALUES[3]);
    StringBuffer bufferEntity = new StringBuffer().append(ENTITY_VALUES[4]);

    Object[] array = new Object[] { sEntity, isEntity, serEntity, builderEntity,
        bufferEntity };
    return array;
  }

  static final String[] ENTITY_VALUES = { "string", "inputstream",
      "serializable", "stringbuilder", "stringbuffer" };

  static final Locale[] LANGUAGES = { Locale.FRENCH, Locale.GERMAN };

  static final String[] ENCODINGS = { "UTF-16", "ISO-8859-2", "CP1250" };

}
