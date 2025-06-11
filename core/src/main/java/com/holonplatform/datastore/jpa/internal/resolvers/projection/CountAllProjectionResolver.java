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
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.query.CountAllProjection;
import com.holonplatform.core.query.QueryFunction.Count;
import com.holonplatform.datastore.jpa.jpql.context.JPQLContextExpressionResolver;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;
import com.holonplatform.datastore.jpa.jpql.context.JPQLStatementResolutionContext;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLProjection;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLToken;

/**
 * {@link CountAllProjection} resolver.
 *
 * @since 5.1.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE - 100)
public enum CountAllProjectionResolver implements JPQLContextExpressionResolver<CountAllProjection, JPQLProjection> {

	/**
	 * Singleton instance
	 */
	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.resolvers.JPQLContextExpressionResolver#resolve(com.holonplatform.core.
	 * Expression, com.holonplatform.datastore.jpa.context.JPQLResolutionContext)
	 */
	@Override
	public Optional<JPQLProjection> resolve(CountAllProjection expression, JPQLResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		final JPQLStatementResolutionContext sctx = context.isStatementCompositionContext()
				.orElseThrow(() -> new InvalidExpressionException("JPQL context is not a StatementCompositionContext"));

		// use root target entity
		final DataTarget<?> target = sctx.getRootTarget();

		final String rootName = context.isStatementCompositionContext().flatMap(ctx -> ctx.getAlias(target, false))
				.orElse(target.getName());

		return Optional
				.ofNullable(context.resolveOrFail(Count.create(JPQLToken.create(rootName)), JPQLProjection.class));
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends CountAllProjection> getExpressionType() {
		return CountAllProjection.class;
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
