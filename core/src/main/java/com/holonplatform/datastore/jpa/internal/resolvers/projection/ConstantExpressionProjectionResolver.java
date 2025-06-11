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

import java.util.Optional;

import jakarta.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.internal.utils.TypeUtils;
import com.holonplatform.core.query.ConstantExpression;
import com.holonplatform.datastore.jpa.internal.converters.TypedExpressionResultConverter;
import com.holonplatform.datastore.jpa.internal.jpql.expression.DefaultJPQLProjection;
import com.holonplatform.datastore.jpa.jpql.context.JPQLContextExpressionResolver;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLExpression;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLLiteral;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLProjection;

/**
 * {@link ConstantExpression} projection resolver.
 *
 * @since 5.1.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE - 1000)
public enum ConstantExpressionProjectionResolver
		implements JPQLContextExpressionResolver<ConstantExpression, JPQLProjection> {

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
	public Optional<JPQLProjection> resolve(ConstantExpression expression, JPQLResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// resolve literal value
		final JPQLLiteral literal = JPQLLiteral.create(expression.getValue(),
				((ConstantExpression<?>) expression).getTemporalType().orElse(null));

		final String serialized = context.resolveOrFail(literal, JPQLExpression.class).getValue();

		final DefaultJPQLProjection<?, ?> projection = new DefaultJPQLProjection<>(context,
				TypeUtils.box(expression.getType()), expression.getType());
		projection.addSelection(serialized, false);
		projection.setConverter(new TypedExpressionResultConverter<>(expression));

		return Optional.of(projection);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends ConstantExpression> getExpressionType() {
		return ConstantExpression.class;
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
