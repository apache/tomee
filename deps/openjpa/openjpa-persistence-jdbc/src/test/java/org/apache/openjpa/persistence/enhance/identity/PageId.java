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
public final class PageId implements Serializable {
    private int number;
    private BookId book;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof PageId)) {
            return false;
        }
        
        PageId other = (PageId)o;
        
        if (!(getNumber() == other.getNumber())) {
            return false;
        }
      
        if (!getBook().equals(other.getBook())) {
            return false;
        }

        return true;
    }
    
    public int hashCode() {
        return number * (book != null ? getBook().hashCode() : 31);
    }

    
    public BookId getBook() {
        return book;
    }

    public void setBook(BookId book) {
        this.book = book;
    }
}
