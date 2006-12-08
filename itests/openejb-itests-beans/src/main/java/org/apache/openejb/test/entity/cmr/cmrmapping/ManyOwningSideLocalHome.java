/**
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
package org.apache.openejb.test.entity.cmr.cmrmapping;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;
import javax.ejb.FinderException;


/**
 * @version $Revision: 477809 $ $Date: 2006-11-21 10:41:16 -0800 (Tue, 21 Nov 2006) $
 */
public interface ManyOwningSideLocalHome extends EJBLocalHome {
    public ManyOwningSideLocal create(Integer id, Integer field1) throws CreateException;
    
    public ManyOwningSideLocal findByPrimaryKey(Integer pk) throws FinderException;
}
