/*
 * Copyright 2016-2017 Axioma srl.
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
package com.holonplatform.datastore.jpa.jpql.expression;

import com.holonplatform.core.Expression;
import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.datastore.jpa.internal.jpql.expression.DefaultJPQLLiteral;

/**
 * {@link Expression} which represents a JPQL literal value.
 * 
 * @param <T> Value type
 *
 * @since 5.0.0
 */
public interface JPQLLiteral<T> extends TypedExpression<T> {

	/**
	 * Get the value
	 * @return The value
	 */
	T getValue();

	/**
	 * Create a new {@link JPQLLiteral} using given value.
	 * @param <T> Value type
	 * @param value Literal value
	 * @return A new {@link JPQLLiteral}
	 */
	static <T> JPQLLiteral<T> create(T value) {
		return new DefaultJPQLLiteral<>(value);
	}

	/**
	 * Create an {@link JPQLLiteral} using given value.
	 * @param <T> Value type
	 * @param value Value
	 * @param temporalType Temporal type
	 * @return A new {@link JPQLLiteral}
	 */
	static <T> JPQLLiteral<T> create(T value, TemporalType temporalType) {
		return new DefaultJPQLLiteral<>(value, temporalType);
	}

}
