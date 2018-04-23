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
package com.holonplatform.datastore.jpa.jpql;

import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.datastore.jpa.context.JpaExecutionContext;
import com.holonplatform.datastore.jpa.internal.jpql.DefaultJPQLValueDeserializer;

/**
 * JPA values deserializaer.
 *
 * @since 5.1.0
 */
public interface JPQLValueDeserializer {

	/**
	 * Deserialize the <code>value</code> associated to given <code>expression</code>, to obtain a value type which
	 * matches the expression type.
	 * @param <T> Expression type
	 * @param context Execution context
	 * @param expression Expression for which the deserialization is invoked
	 * @param value Value to deserialize
	 * @return Deserialized value
	 * @throws DataAccessException If value cannot be deserialized using given expression type
	 */
	<T> T deserialize(JpaExecutionContext context, TypedExpression<T> expression, Object value)
			throws DataAccessException;

	/**
	 * Add a deserialized value processor.
	 * @param valueProcessor the value processor to add (not null)
	 */
	void addValueProcessor(ValueProcessor valueProcessor);

	/**
	 * Create the default {@link JPQLValueDeserializer}.
	 * @return the default {@link JPQLValueDeserializer}
	 */
	static JPQLValueDeserializer getDefault() {
		return DefaultJPQLValueDeserializer.INSTANCE;
	}

	/**
	 * Processor to process a value before actual deserialization.
	 */
	@FunctionalInterface
	public interface ValueProcessor {

		/**
		 * Process a value to be deserialized.
		 * @param context Execution context
		 * @param expression Expression for which the deserialization is invoked
		 * @param value Value to deserialize
		 * @return Processed value
		 * @throws DataAccessException If an error occurred
		 */
		Object processValue(JpaExecutionContext context, TypedExpression<?> expression, Object value)
				throws DataAccessException;

	}

}
