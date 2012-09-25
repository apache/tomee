/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.test.component.inheritance;

import org.apache.webbeans.test.component.inheritance.types.InhBinding1;
import org.apache.webbeans.test.component.inheritance.types.InhBinding2;
import org.apache.webbeans.test.component.inheritance.types.InhIntBinding1;
import org.apache.webbeans.test.component.inheritance.types.InhIntBinding2;
import org.apache.webbeans.test.component.inheritance.types.InhScopeType1;
import org.apache.webbeans.test.component.inheritance.types.InhStereo1;
import org.apache.webbeans.test.component.inheritance.types.InhStereo2;

@InhBinding1
@InhBinding2
@InhStereo1
@InhStereo2
@InhScopeType1
@InhIntBinding1
@InhIntBinding2
public class ParentComponent
{

}
