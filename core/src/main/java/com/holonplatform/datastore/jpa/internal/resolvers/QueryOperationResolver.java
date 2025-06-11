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
import java.util.stream.Collectors;

import jakarta.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.core.query.QueryConfiguration;
import com.holonplatform.core.query.QueryOperation;
import com.holonplatform.datastore.jpa.internal.jpql.expression.DefaultJPQLQueryDefinition;
import com.holonplatform.datastore.jpa.jpql.context.JPQLContextExpressionResolver;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;
import com.holonplatform.datastore.jpa.jpql.context.JPQLStatementResolutionContext;
import com.holonplatform.datastore.jpa.jpql.context.JPQLStatementResolutionContext.AliasMode;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLExpression;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLProjection;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLQueryDefinition;

/**
 * {@link QueryOperation} to {@link JPQLQueryDefinition} resolver.
 *
 * @since 5.1.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public enum QueryOperationResolver implements JPQLContextExpressionResolver<QueryOperation, JPQLQueryDefinition> {

	/**
	 * Singleton instance.
	 */
	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends QueryOperation> getExpressionType() {
		return QueryOperation.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getResolvedType()
	 */
	@Override
	public Class<? extends JPQLQueryDefinition> getResolvedType() {
		return JPQLQueryDefinition.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.resolvers.JPQLContextExpressionResolver#resolve(com.holonplatform.core.
	 * Expression, com.holonplatform.datastore.jpa.context.JPQLResolutionContext)
	 */
	@Override
	public Optional<JPQLQueryDefinition> resolve(QueryOperation expression, JPQLResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		final QueryConfiguration configuration = expression.getConfiguration();

		// build query clauses
		final DefaultJPQLQueryDefinition clauses = new DefaultJPQLQueryDefinition();

		// check and resolve target
		RelationalTarget<?> target = context.resolveOrFail(
				configuration.getTarget().orElseThrow(() -> new InvalidExpressionException("Missing query target")),
				RelationalTarget.class);

		// build a statement context
		final JPQLStatementResolutionContext queryContext = JPQLStatementResolutionContext.asChild(context, target,
				AliasMode.AUTO);

		// ------- from
		clauses.setFrom(queryContext.resolveOrFail(target, JPQLExpression.class).getValue());

		// ------- where
		configuration.getFilter().ifPresent(f -> {
			// add clause
			clauses.setWhere(queryContext.resolveOrFail(f, JPQLExpression.class).getValue());
		});

		// ------- group by
		configuration.getAggregation().ifPresent(a -> {
			// add clause
			clauses.setGroupBy(queryContext.resolveOrFail(a, JPQLExpression.class).getValue());
		});

		// ------- order by
		configuration.getSort().ifPresent(s -> {
			// add clause
			clauses.setOrderBy(queryContext.resolveOrFail(s, JPQLExpression.class).getValue());
		});

		// ------- select
		clauses.setDistinct(configuration.isDistinct());

		final JPQLProjection<?, ?> projection = queryContext.resolveOrFail(expression.getProjection(),
				JPQLProjection.class);

		// add clause
		clauses.setSelect(projection.getSelection().stream()
				.map(s -> s + projection.getSelectionAlias(s).map(a -> " AS " + a).orElse(""))
				.collect(Collectors.joining(", ")));

		// query result type
		clauses.setQueryResultType(projection.getQueryResultType());
		// result converter
		projection.getConverter().ifPresent(rc -> {
			clauses.setResultConverter(rc);
		});

		// return query definition
		return Optional.of(clauses);
	}

}
