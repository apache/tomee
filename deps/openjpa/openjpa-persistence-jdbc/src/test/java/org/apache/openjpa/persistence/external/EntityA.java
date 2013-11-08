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
package org.apache.openjpa.persistence.external;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.openjpa.persistence.ExternalValues;
import org.apache.openjpa.persistence.Type;

@Entity
@Table(name = "ExternalizationEntityA")
public class EntityA implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private int id;

	@ExternalValues( { "SMALL=SML", "MEDIUM=MID", "LARGE=LRG" })
	@Column(length = 3)
	private String s1;

	@ExternalValues( { "SMALL=5", "MEDIUM=8", "LARGE=15" })
	@Type(int.class)
	private String s2;
	
	/*
	 * By default, OpenJPA stores Chars as Ints, so to force the use of
	 * a char(1) field, you would need to override this processing via
	 * a DBDictionary property:
	 * -Dopenjpa.jdbc.DBDictionary=derby(StoreCharsAsNumbers=false)
	 */
    @Column(name="USE_STREAMING")
    @ExternalValues({"true=T", "false=F"})
    @Type(char.class)
    private boolean _useStreaming = false;
    
	public EntityA() {
	}

	public int getId() {
		return id;
	}

	public String getS1() {
		return s1;
	}

	public void setS1(String s1) {
		this.s1 = s1;
	}

	public String getS2() {
		return s2;
	}

	public void setS2(String s2) {
		this.s2 = s2;
	}
	
    public boolean getUseStreaming() {
        return _useStreaming;
    }

    public void setUseStreaming(boolean useStreaming) {
        _useStreaming = useStreaming;
    }
}
