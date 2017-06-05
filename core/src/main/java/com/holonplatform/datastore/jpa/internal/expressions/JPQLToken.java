/*
 * Copyright 2000-2016 Holon TDCN.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.holonplatform.datastore.jpa.internal.expressions;

import java.io.Serializable;

import com.holonplatform.core.Expression;

/**
 * Represents a JPQL statement element.
 * 
 * @since 5.0.0
 */
public interface JPQLToken extends Expression, Serializable {

	/**
	 * Get the JPQ token {@link String} representation.
	 * @return JPQ token value
	 */
	String getValue();

	/**
	 * Create a new {@link JPQLToken} with given value.
	 * @param value JPQL token value
	 * @return A new {@link JPQLToken} with given value
	 */
	static JPQLToken create(String value) {
		return new DefaultJPQLToken(value);
	}

}
