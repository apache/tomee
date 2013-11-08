/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.persistence.enhance.identity;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;


/**
 * Entity identity used to test compound primary keys using entity as 
 * relationship to more than one level.
 * 
 * Test case and domain classes were originally part of the reported issue
 * <A href="https://issues.apache.org/jira/browse/OPENJPA-207">OPENJPA-207</A>
 *  
 * @author Jeffrey Blattman
 * @author Pinaki Poddar
 *
 */

@Embeddable
public final class LineId1 implements Serializable {
    @Column(name="LINE_NUM")
    private int lineNum;

    @Embedded
    @AttributeOverride(name="number", column=@Column(name="PAGE_NUM"))
    private PageId1 page;

    public LineId1() {}
    
    public LineId1(int lineNum, PageId1 page) {
        this.lineNum = lineNum;
        this.page = page;
    }
    
    public int getLineNum() {
        return lineNum;
    }

    public void setLineNum(int lineNum) {
        this.lineNum = lineNum;
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof LineId1)) {
            return false;
        }
        
        LineId1 other = (LineId1)o;
        
        if (!(getLineNum() == other.getLineNum())) {
            return false;
        }
      
        if (!getPage().equals(other.getPage())) {
            return false;
        }

        return true;
    }
    
    public int hashCode() {
        return lineNum * (page != null ? getPage().hashCode() : 31);
    }
    
    public PageId1 getPage() {
        return page;
    }

    public void setPage(PageId1 page) {
        this.page = page;
    }
}
