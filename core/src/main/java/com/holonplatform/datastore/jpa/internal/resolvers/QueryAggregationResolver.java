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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.Path;
import com.holonplatform.core.query.QueryAggregation;
import com.holonplatform.datastore.jpa.internal.JpaDatastoreUtils;
import com.holonplatform.datastore.jpa.internal.expressions.JPQLToken;

/**
 * {@link QueryAggregation} expression resolver.
 *
 * @since 5.0.0
 */
public enum QueryAggregationResolver implements ExpressionResolver<QueryAggregation, JPQLToken> {

	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends QueryAggregation> getExpressionType() {
		return QueryAggregation.class;
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
	 * @see com.holonplatform.core.Expression.ExpressionResolverFunction#resolve(com.holonplatform.core.Expression,
	 * com.holonplatform.core.ExpressionResolver.ResolutionContext)
	 */
	@Override
	public Optional<JPQLToken> resolve(QueryAggregation expression,
			com.holonplatform.core.ExpressionResolver.ResolutionContext context) throws InvalidExpressionException {

		// validate
		expression.validate();

		final StringBuilder sb = new StringBuilder();

		// group by
		List<String> groupBys = new ArrayList<>(expression.getAggregationPaths().length);
		for (Path<?> path : expression.getAggregationPaths()) {
			groupBys.add(JpaDatastoreUtils.resolveExpression(context, path, JPQLToken.class, context).getValue());
		}
		sb.append(groupBys.stream().collect(Collectors.joining(",")));
		// having
		expression.getAggregationFilter().ifPresent(f -> {
			sb.append(" HAVING ");
			sb.append(JpaDatastoreUtils.resolveExpression(context, f, JPQLToken.class, context).getValue());
		});

		return Optional.of(JPQLToken.create(sb.toString()));
	}
}
