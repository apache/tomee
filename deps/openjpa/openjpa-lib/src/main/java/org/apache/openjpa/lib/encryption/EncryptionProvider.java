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
package org.apache.openjpa.lib.encryption;

/**
 * Interface for providing encryption/decryption capabilities to the OpenJPA
 * runtime.
 * 
 * Currently method is ONLY called to decrypt openjpa.ConnectionPassword and
 * openjpa.Connection2Password properties.
 */
public interface EncryptionProvider {

	/**
	 * This method will decrypt the provided string. If null is passed into this
	 * method it should noop and return null. No exceptions should ever escape
	 * from this method.
	 * 
	 * Note: Currently method is ONLY called to decrypt
	 * openjpa.ConnectionPassword and openjpa.Connection2Password properties.
	 */
	public String decrypt(String password);

	/**
	 * This method will encrypt the provided string. If null is passed into this
	 * method it should noop and return null. No exceptions should ever escape
	 * from this method.
	 * 
	 * NOTE : This method is not called by the OpenJPA runtime. It is here for
	 * possible future uses.
	 */
	public String encrypt(String password);
}
