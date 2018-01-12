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
package com.holonplatform.datastore.jpa.internal.resolvers;

import java.util.Optional;

import javax.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.internal.utils.TypeUtils;
import com.holonplatform.core.query.QueryFunction;
import com.holonplatform.datastore.jpa.internal.JpaDatastoreUtils;
import com.holonplatform.datastore.jpa.internal.converters.SingleSelectionResultConverter;
import com.holonplatform.datastore.jpa.internal.expressions.DefaultProjectionContext;
import com.holonplatform.datastore.jpa.internal.expressions.JPQLToken;
import com.holonplatform.datastore.jpa.internal.expressions.JpaResolutionContext;
import com.holonplatform.datastore.jpa.internal.expressions.ProjectionContext;

/**
 * {@link QueryFunction} resolver.
 *
 * @since 5.1.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public enum QueryFunctionProjectionResolver implements ExpressionResolver<QueryFunction, ProjectionContext> {

	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression.ExpressionResolverFunction#resolve(com.holonplatform.core.Expression,
	 * com.holonplatform.core.ExpressionResolver.ResolutionContext)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Optional<ProjectionContext> resolve(QueryFunction expression, ResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// context
		final JpaResolutionContext jpaContext = JpaResolutionContext.checkContext(context);

		DefaultProjectionContext ctx = new DefaultProjectionContext(jpaContext, TypeUtils.box(expression.getType()));
		ctx.addSelection(JpaDatastoreUtils.resolveExpression(context, expression, JPQLToken.class, context).getValue(),
				false);
		ctx.setConverter(new SingleSelectionResultConverter(expression));

		return Optional.of(ctx);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends QueryFunction> getExpressionType() {
		return QueryFunction.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getResolvedType()
	 */
	@Override
	public Class<? extends ProjectionContext> getResolvedType() {
		return ProjectionContext.class;
	}

}
