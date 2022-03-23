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
package org.superbiz.cdi.inject.dynamic;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.util.AnnotationLiteral;

import org.superbiz.cdi.qualifier.PaymentQualifier;
import org.superbiz.cdi.qualifier.PaymentType;

/**
 * 
 * This class will help the CDI the choose correct implementation using qualifier.
 * <br>
 * To use <b>.select</b> of the method of the {@link Instance} object with qualifier, is need create a Class the extends {@link AnnotationLiteral}
 * 
 * */
public class PayByQualifier extends AnnotationLiteral<PaymentQualifier> implements PaymentQualifier {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private PaymentType type;

	public PayByQualifier(PaymentType type) {
		
		this.type = type;
	}
	
	@Override
	public PaymentType type() {
		return type;
	}
}
