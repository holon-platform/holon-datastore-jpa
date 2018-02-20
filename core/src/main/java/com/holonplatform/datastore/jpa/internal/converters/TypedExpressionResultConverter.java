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
package com.holonplatform.datastore.jpa.internal.converters;

import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.exceptions.DataAccessException;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jpa.context.JpaExecutionContext;
import com.holonplatform.datastore.jpa.jpql.JPQLResultConverter;

/**
 * A {@link JPQLResultConverter} for single selection query results.
 * 
 * @param <T> Expression type
 * 
 * @since 5.0.0
 */
public class TypedExpressionResultConverter<T> implements JPQLResultConverter<Object, T> {

	private final TypedExpression<T> expression;

	public TypedExpressionResultConverter(TypedExpression<T> expression) {
		super();
		ObjectUtils.argumentNotNull(expression, "Expression must be not null");
		this.expression = expression;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.operation.JpaResultConverter#getQueryResultType()
	 */
	@Override
	public Class<? extends Object> getQueryResultType() {
		return Object.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.operation.JpaResultConverter#getConversionType()
	 */
	@Override
	public Class<? extends T> getConversionType() {
		return expression.getType();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.datastore.jpa.operation.JpaResultConverter#convert(com.holonplatform.datastore.jpa.context.
	 * JpaExecutionContext, java.lang.Object)
	 */
	@Override
	public T convert(JpaExecutionContext context, Object result) throws DataAccessException {
		return context.getValueDeserializer().deserialize(context, expression, result);
	}

}
