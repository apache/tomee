/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz

import javax.ejb.embeddable.EJBContainer
import javax.inject.Inject

import org.scalatest._

class GreetingServiceTest extends FunSuite with BeforeAndAfterAll with BeforeAndAfterEach {
  @Inject
  private var service: GreetingService = null

  test("Hi scala") {
    val message = service hi "scala"
    assert(message == "hi scala")
  }

  test("Default Hi") {
    val message = service.hi()
    assert(message == "hi Scala")
  }

  test("Hi null") {
    val message = service.hi(null)
    assert(message == "hi Scala") // when called with null we use default name
  }

  /** *************************************************************/
  /** the OpenEJB Hook to be able to inject beans in this class **/
  /** kind of internal of this test class, "hidden" being last  **/
  /** *************************************************************/

  private var container: EJBContainer = null

  override def beforeAll() {
    container = EJBContainer.createEJBContainer()
  }

  override def beforeEach() {
    container.getContext().bind("inject", this)
  }

  override def afterAll() {
    if (container != null) {
      container.close()
    }
  }
}
