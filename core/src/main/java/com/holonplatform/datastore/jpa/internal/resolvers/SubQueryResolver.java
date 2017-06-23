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
package com.holonplatform.datastore.jpa.internal.resolvers;

import java.util.Optional;

import javax.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.datastore.relational.SubQuery;
import com.holonplatform.core.internal.query.QueryStructure;
import com.holonplatform.datastore.jpa.internal.JpaDatastoreUtils;
import com.holonplatform.datastore.jpa.internal.expressions.JPQLQueryComposition;
import com.holonplatform.datastore.jpa.internal.expressions.JPQLToken;
import com.holonplatform.datastore.jpa.internal.expressions.JpaResolutionContext;
import com.holonplatform.datastore.jpa.internal.expressions.JpaResolutionContext.AliasMode;

/**
 * {@link SubQuery} expression resolver.
 *
 * @since 5.0.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE - 100)
public enum SubQueryResolver implements ExpressionResolver<SubQuery, JPQLToken> {

	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends SubQuery> getExpressionType() {
		return SubQuery.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getResolvedType()
	 */
	@Override
	public Class<? extends JPQLToken> getResolvedType() {
		return JPQLToken.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#resolve(com.holonplatform.core.Expression,
	 * com.holonplatform.core.ExpressionResolver.ResolutionContext)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Optional<JPQLToken> resolve(SubQuery expression, ResolutionContext context)
			throws InvalidExpressionException {

		try {
			JpaResolutionContext subQueryContext = JpaResolutionContext.checkContext(context)
					.childContext(AliasMode.AUTO);

			// resolve query
			final JPQLQueryComposition<?, ?> query = JpaDatastoreUtils.resolveExpression(subQueryContext,
					QueryStructure.create(expression.getQueryConfiguration(), expression.getSelection()),
					JPQLQueryComposition.class, subQueryContext);

			// return subquery expression
			return Optional.of(JPQLToken.create(query.serialize()));

		} catch (Exception e) {
			throw new InvalidExpressionException("Failed to resolve sub query [" + expression + "]", e);
		}
	}

}
