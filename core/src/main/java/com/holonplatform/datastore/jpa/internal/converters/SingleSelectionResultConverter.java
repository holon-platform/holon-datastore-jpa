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

import com.holonplatform.core.internal.utils.TypeUtils;
import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.core.query.QueryResults.QueryResultConversionException;
import com.holonplatform.datastore.jpa.internal.expressions.QueryResultConverter;

/**
 * A {@link QueryResultConverter} for single selection query results.
 * 
 * @since 5.0.0
 */
public class SingleSelectionResultConverter<Q, R> extends AbstractConverter<Q, R> {

	private final QueryExpression<?> expression;

	public SingleSelectionResultConverter(QueryExpression<?> expression) {
		super();
		this.expression = expression;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.jpql.expressions.QueryResultConverter#convert(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public R convert(Q result) throws QueryResultConversionException {
		try {
			final Object value = getResult(expression, result);

			// check type
			if (value != null && !TypeUtils.isAssignable(value.getClass(), expression.getType())) {
				throw new QueryResultConversionException("Expected a value of projection type ["
						+ expression.getType().getName() + "], got a value of type: " + value.getClass().getName());
			}

			return (R) value;

		} catch (Exception e) {
			throw new QueryResultConversionException(
					"Failed to convert query result for expression [" + expression + "]", e);
		}

	}

}
