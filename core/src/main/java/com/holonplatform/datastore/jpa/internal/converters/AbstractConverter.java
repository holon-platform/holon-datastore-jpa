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
package com.holonplatform.datastore.jpa.internal.converters;

import com.holonplatform.core.Expression;
import com.holonplatform.core.Path;
import com.holonplatform.core.internal.utils.ConversionUtils;
import com.holonplatform.core.internal.utils.TypeUtils;
import com.holonplatform.core.property.Property;
import com.holonplatform.core.property.PropertyValueConverter;
import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.datastore.jpa.internal.expressions.QueryResultConverter;

/**
 * Base {@link QueryResultConverter}.
 * 
 * @param <Q> Query result type
 * @param <R> Expected result type
 * 
 * @since 5.0.0
 */
public abstract class AbstractConverter<Q, R> implements QueryResultConverter<Q, R> {

	/**
	 * Get a result value, applying any suitable property value conversion and value conversion.
	 * @param path Path to which the value is bound
	 * @param value Query result value
	 * @return Result value
	 * @throws IllegalArgumentException Cannot obtain the value with given label from the tuple
	 */
	protected Object getResult(Path<?> path, Object value) throws IllegalArgumentException {
		return processQueryResult(path.getType(), processExpressionResult(path, value));
	}

	/**
	 * Get a result value, applying any suitable property value conversion and value conversion.
	 * @param property Property
	 * @param value Query result value
	 * @return Result value
	 * @throws IllegalArgumentException Cannot obtain the value with given label from the tuple
	 */
	protected Object getResult(Property<?> property, Object value) throws IllegalArgumentException {
		return processQueryResult(property.getType(), processPropertyResult(property, value));
	}

	/**
	 * Get a result value, applying any suitable property value conversion and value conversion.
	 * @param expression Expression
	 * @param value Query result value
	 * @return Result value
	 * @throws IllegalArgumentException Cannot obtain the value with given label from the tuple
	 */
	protected Object getResult(QueryExpression<?> expression, Object value) throws IllegalArgumentException {
		return processQueryResult(expression.getType(), processExpressionResult(expression, value));
	}

	/**
	 * Process a result value, applying any suitable property value conversion.
	 * @param expression Expression
	 * @param value Query result value
	 * @return Result value
	 * @throws IllegalArgumentException Cannot obtain the value with given label from the tuple
	 */
	protected Object processExpressionResult(Expression expression, Object value) {
		return (expression instanceof Property) ? processPropertyResult((Property<?>) expression, value) : value;
	}

	/**
	 * Process a query result value bound to given <code>property</code>, applying any suitable property value
	 * conversion.
	 * @param property Property
	 * @param value Result value
	 * @return Processed value
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Object processPropertyResult(Property property, Object value) {
		return property.getConverter()
				.filter(c -> value == null
						|| TypeUtils.isAssignable(value.getClass(), ((PropertyValueConverter<?, ?>) c).getModelType()))
				.map(c -> ((PropertyValueConverter<?, Object>) c).fromModel(value, property)).orElse(value);
	}

	/**
	 * Process a query result and apply conversions if required.
	 * @param type Expected result type
	 * @param result Result value
	 * @return Processed result
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Object processQueryResult(Class type, Object result) {
		if (type != null && result != null) {
			if (TypeUtils.isNumber(type) && TypeUtils.isNumber(result.getClass())) {
				return ConversionUtils.convertNumberToTargetClass((Number) result, type);
			}
		}
		return result;
	}

}
