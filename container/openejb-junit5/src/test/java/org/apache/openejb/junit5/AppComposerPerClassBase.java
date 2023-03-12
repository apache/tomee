/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.junit5;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testing.PersistenceRootUrl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

@RunWithApplicationComposer(mode = ExtensionMode.PER_EACH)
@Classes(cdi = true, innerClassesAsBean = true)
@ExtendWith(AppComposerSnifferExtension.class)
public class AppComposerPerClassBase {
  
  @Module
  @PersistenceRootUrl(value = "")
  public Persistence jpa() throws Exception {
      return new Persistence(new PersistenceUnit("jpa"));
  }

  @Inject
  private Marker bean;

  @Test
  public void run() {
      assertNotNull(bean);
      assertEquals(ApplicationComposerPerEachExtension.class, AppComposerSnifferExtension.COMPOSER_CLASS);
  }

  public static class Marker {}

}
