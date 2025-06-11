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
package com.holonplatform.datastore.jpa.internal.resolvers.projection;

import java.util.Date;
import java.util.Optional;

import jakarta.annotation.Priority;

import com.holonplatform.core.ConverterExpression;
import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionValueConverter;
import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.internal.utils.TypeUtils;
import com.holonplatform.datastore.jpa.internal.converters.TypedExpressionResultConverter;
import com.holonplatform.datastore.jpa.internal.jpql.expression.DefaultJPQLProjection;
import com.holonplatform.datastore.jpa.jpql.context.JPQLContextExpressionResolver;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLExpression;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLProjection;

/**
 * {@link TypedExpression} resolver.
 *
 * @since 5.1.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE - 10)
public enum TypedExpressionProjectionResolver
		implements JPQLContextExpressionResolver<TypedExpression, JPQLProjection> {

	/**
	 * Singleton instance
	 */
	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.resolvers.JPQLContextExpressionResolver#resolve(com.holonplatform.core.
	 * Expression, com.holonplatform.datastore.jpa.context.JPQLResolutionContext)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Optional<JPQLProjection> resolve(TypedExpression expression, JPQLResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// check converter
		final ExpressionValueConverter<?, ?> converter = (expression instanceof ConverterExpression)
				? ((ConverterExpression<?>) expression).getExpressionValueConverter().orElse(null)
				: null;

		// query result type
		Class<?> queryResultType = TypeUtils.box((converter != null) ? converter.getModelType() : expression.getType());

		if (TypeUtils.isTemporal(queryResultType) && !context.getDialect().temporalTypeProjectionSupported()) {
			queryResultType = Date.class;
		}

		final DefaultJPQLProjection<?, ?> projection = new DefaultJPQLProjection<>(context, queryResultType,
				expression.getType());
		projection.addSelection(context.resolveOrFail(expression, JPQLExpression.class).getValue(), false);
		projection.setConverter(new TypedExpressionResultConverter<>(expression));

		return Optional.of(projection);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends TypedExpression> getExpressionType() {
		return TypedExpression.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getResolvedType()
	 */
	@Override
	public Class<? extends JPQLProjection> getResolvedType() {
		return JPQLProjection.class;
	}

}
