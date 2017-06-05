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
import java.util.stream.Collectors;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.core.internal.query.QueryStructure;
import com.holonplatform.core.query.Query.QueryBuildException;
import com.holonplatform.core.query.QueryConfiguration;
import com.holonplatform.datastore.jpa.internal.JpaDatastoreUtils;
import com.holonplatform.datastore.jpa.internal.expressions.DefaultJPQLQueryComposition;
import com.holonplatform.datastore.jpa.internal.expressions.FromExpression;
import com.holonplatform.datastore.jpa.internal.expressions.JPQLQueryComposition;
import com.holonplatform.datastore.jpa.internal.expressions.JPQLToken;
import com.holonplatform.datastore.jpa.internal.expressions.JpaResolutionContext;
import com.holonplatform.datastore.jpa.internal.expressions.ProjectionContext;

/**
 * {@link QueryStructure} expression resolver.
 *
 * @since 5.0.0
 */
@SuppressWarnings("rawtypes")
public enum QueryStructureResolver implements ExpressionResolver<QueryStructure, JPQLQueryComposition> {

	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends QueryStructure> getExpressionType() {
		return QueryStructure.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getResolvedType()
	 */
	@Override
	public Class<? extends JPQLQueryComposition> getResolvedType() {
		return JPQLQueryComposition.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression.ExpressionResolverFunction#resolve(com.holonplatform.core.Expression,
	 * com.holonplatform.core.ExpressionResolver.ResolutionContext)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Optional<JPQLQueryComposition> resolve(QueryStructure expression, ResolutionContext resolutionContext)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		final JpaResolutionContext context = JpaResolutionContext.checkContext(resolutionContext);

		// build query composition

		final DefaultJPQLQueryComposition query = new DefaultJPQLQueryComposition();

		final QueryConfiguration configuration = expression.getConfiguration();

		// limit and offset
		configuration.getLimit().ifPresent(l -> query.setLimit(l));
		configuration.getOffset().ifPresent(o -> query.setOffset(o));

		// from
		RelationalTarget<?> target = JpaDatastoreUtils.resolveExpression(context,
				configuration.getTarget().orElseThrow(() -> new QueryBuildException("Missing query target")),
				RelationalTarget.class, context);
		context.setTarget(target);

		query.setFrom(JpaDatastoreUtils.resolveExpression(context, target, FromExpression.class, context).getValue());

		// where
		configuration.getFilter().ifPresent(f -> {
			query.setWhere(JpaDatastoreUtils.resolveExpression(context, f, JPQLToken.class, context).getValue());
		});

		// group by
		configuration.getAggregation().ifPresent(a -> {
			query.setGroupBy(JpaDatastoreUtils.resolveExpression(context, a, JPQLToken.class, context).getValue());
		});

		// order by
		configuration.getSort().ifPresent(s -> {
			query.setOrderBy(JpaDatastoreUtils.resolveExpression(context, s, JPQLToken.class, context).getValue());
		});

		// select
		final ProjectionContext<?, ?> projectionContext = context
				.resolve(expression.getProjection(), ProjectionContext.class, context)
				.orElseThrow(() -> new InvalidExpressionException(
						"Failed to resolve projection [" + expression.getProjection() + "]"));
		projectionContext.validate();

		// check selection
		if (projectionContext.getSelection() == null || projectionContext.getSelection().isEmpty()) {
			throw new InvalidExpressionException("Null or empty query selection");
		}

		// select clause
		query.setSelect(projectionContext.getSelection().stream()
				.map(s -> s + projectionContext.getSelectionAlias(s).map(a -> " AS " + a).orElse(""))
				.collect(Collectors.joining(", ")));

		// projection context
		query.setProjection(projectionContext);

		return Optional.of(query);
	}
}
