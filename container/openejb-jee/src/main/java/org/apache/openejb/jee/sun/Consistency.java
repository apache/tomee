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
package org.apache.openejb.jee.sun;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "noneOrCheckModifiedAtCommitOrLockWhenLoadedOrCheckAllAtCommitOrLockWhenModifiedOrCheckVersionOfAccessedInstances"
})
public class Consistency {
    @XmlElements({
        @XmlElement(name = "none", required = true, type = None.class),
        @XmlElement(name = "check-modified-at-commit", required = true, type = CheckModifiedAtCommit.class),
        @XmlElement(name = "lock-when-loaded", required = true, type = LockWhenLoaded.class),
        @XmlElement(name = "check-all-at-commit", required = true, type = CheckAllAtCommit.class),
        @XmlElement(name = "lock-when-modified", required = true, type = LockWhenModified.class),
        @XmlElement(name = "check-version-of-accessed-instances", required = true, type = CheckVersionOfAccessedInstances.class)
    })
    protected List<Object> noneOrCheckModifiedAtCommitOrLockWhenLoadedOrCheckAllAtCommitOrLockWhenModifiedOrCheckVersionOfAccessedInstances;

    public List<Object> getNoneOrCheckModifiedAtCommitOrLockWhenLoadedOrCheckAllAtCommitOrLockWhenModifiedOrCheckVersionOfAccessedInstances() {
        if (noneOrCheckModifiedAtCommitOrLockWhenLoadedOrCheckAllAtCommitOrLockWhenModifiedOrCheckVersionOfAccessedInstances == null) {
            noneOrCheckModifiedAtCommitOrLockWhenLoadedOrCheckAllAtCommitOrLockWhenModifiedOrCheckVersionOfAccessedInstances = new ArrayList<Object>();
        }
        return this.noneOrCheckModifiedAtCommitOrLockWhenLoadedOrCheckAllAtCommitOrLockWhenModifiedOrCheckVersionOfAccessedInstances;
    }
}
