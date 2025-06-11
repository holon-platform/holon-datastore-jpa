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

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.query.TemporalFunction.CurrentLocalDate;
import com.holonplatform.datastore.jpa.internal.converters.TypedExpressionResultConverter;
import com.holonplatform.datastore.jpa.internal.jpql.expression.DefaultJPQLProjection;
import com.holonplatform.datastore.jpa.jpql.context.JPQLContextExpressionResolver;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLExpression;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLProjection;

@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE - 200)
public enum CurrentLocalDateProjectionResolver
		implements JPQLContextExpressionResolver<CurrentLocalDate, JPQLProjection> {

	/**
	 * Singleton instance
	 */
	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends CurrentLocalDate> getExpressionType() {
		return CurrentLocalDate.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getResolvedType()
	 */
	@Override
	public Class<? extends JPQLProjection> getResolvedType() {
		return JPQLProjection.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.jpql.context.JPQLContextExpressionResolver#resolve(com.holonplatform.core.
	 * Expression, com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Optional<JPQLProjection> resolve(CurrentLocalDate expression, JPQLResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		final DefaultJPQLProjection projection = new DefaultJPQLProjection<>(context, Date.class, expression.getType());
		projection.addSelection(context.resolveOrFail(expression, JPQLExpression.class).getValue(), false);
		projection.setConverter(new TypedExpressionResultConverter(expression));

		return Optional.of(projection);
	}

}
