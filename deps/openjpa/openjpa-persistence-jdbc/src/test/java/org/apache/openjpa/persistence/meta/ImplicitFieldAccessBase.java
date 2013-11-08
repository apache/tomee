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

package org.apache.openjpa.persistence.meta;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
// import javax.persistence.Transient;

/**
 * Domain classes used by meta-model testing.
 * 
 * Uses implicit, field based access type.
 * 
 * Implicit access is determined by placement of annotations of field or getters.
 * 
 * @author Pinaki Poddar
 *
 */
@Entity
public class ImplicitFieldAccessBase extends ImplicitFieldAccessMappedSuperclass {
    private String   f0;
    private int      primitiveInt;
    private Integer  boxedInt;
    
    @OneToOne
    private ExplicitFieldAccess one2oneRelation;
    
    @OneToMany
    private Collection<ExplicitFieldAccess> collectionRelation;
    
    @OneToMany
    private List<ExplicitFieldAccess> listRelation;
    
    @OneToMany
    private Set<ExplicitFieldAccess> setRelation;
    
    @ManyToMany
    private Map<ExplicitPropertyAccess, ExplicitFieldAccess> mapRelationKeyPC;
    
    @ManyToMany
    private Map<Integer, ExplicitFieldAccess> mapRelationKeyBasic;
    
	public String getF0() {
		return f0;
	}
	
	public void setF0(String string) {
		this.f0 = string;
	}
	
	public int getPrimitiveInt() {
		return primitiveInt;
	}
	
	public void setPrimitiveInt(int primitiveInt) {
		this.primitiveInt = primitiveInt;
	}
	
	public Integer getBoxedInt() {
		return boxedInt;
	}
	
	public void setBoxedInt(Integer boxedInt) {
		this.boxedInt = boxedInt;
	}
	
	public ExplicitFieldAccess getOne2oneRelation() {
		return one2oneRelation;
	}
	
	public void setOne2oneRelation(ExplicitFieldAccess one2oneRelation) {
		this.one2oneRelation = one2oneRelation;
	}
	
	public Collection<ExplicitFieldAccess> getCollectionRelation() {
		return collectionRelation;
	}
	
	public void setCollectionRelation(Collection<ExplicitFieldAccess> collection) {
		this.collectionRelation = collection;
	}
	
	public List<ExplicitFieldAccess> getListRelation() {
		return listRelation;
	}
	
	public void setListRelation(List<ExplicitFieldAccess> listRelation) {
		this.listRelation = listRelation;
	}
	
	public Set<ExplicitFieldAccess> getSetRelation() {
		return setRelation;
	}
	
	public void setSetRelation(Set<ExplicitFieldAccess> setRelation) {
		this.setRelation = setRelation;
	}
	
	public Map<ExplicitPropertyAccess, ExplicitFieldAccess> getMapRelationKeyPC() {
		return mapRelationKeyPC;
	}
	
	public void setMapRelationKeyPC(Map<ExplicitPropertyAccess, ExplicitFieldAccess> map) {
		this.mapRelationKeyPC = map;
	}
	
	public Map<Integer, ExplicitFieldAccess> getMapRelationKeyBasic() {
		return mapRelationKeyBasic;
	}
	
	public void setMapRelationKeyBasic(Map<Integer, ExplicitFieldAccess> map) {
		this.mapRelationKeyBasic = map;
	}
	
	/** This method is annotated but transient to verify that placement of
	 * annotation does not confuse the determination of implicit access
	 * style of this class.
	 * OPENJPA-1613 - per spec and OpenJPA 1.x behavior, the Transient annotation
	 * is now taken into consideration when making a default access determination.
	 * */
	//	@Transient
	public int getTransient() {
		return 42;
	}
	
	public void setTransient(int x) {
		
	}
}
