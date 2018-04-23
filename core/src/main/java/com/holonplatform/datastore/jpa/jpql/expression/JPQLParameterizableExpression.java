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

import com.holonplatform.core.TypedExpression;
import com.holonplatform.datastore.jpa.internal.jpql.expression.DefaultJPQLParameterizableExpression;

/**
 * Represents a {@link TypedExpression} which should be resolved using a JPQL statement parameter if the expression type
 * and the resolution context are suitable to use a JPQL parameter for its representation.
 *
 * @param <T> Expression type
 *
 * @since 5.1.0
 */
public interface JPQLParameterizableExpression<T> extends TypedExpression<T> {

	/**
	 * Get the actual expression which should be resolved using a JPQL statement parameter if the expression type and the
	 * resolution context are suitable to use a JPQL parameter for its representation.
	 * @return The expression
	 */
	TypedExpression<T> getExpression();

	/**
	 * Create a new {@link JPQLParameterizableExpression}.
	 * @param <T> Expression type
	 * @param expression Wrapped expression (not null)
	 * @return A new {@link JPQLParameterizableExpression}
	 */
	static <T> JPQLParameterizableExpression<T> create(TypedExpression<T> expression) {
		return new DefaultJPQLParameterizableExpression<>(expression);
	}

}
