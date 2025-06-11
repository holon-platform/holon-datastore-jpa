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

import jakarta.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.query.QueryOperation;
import com.holonplatform.datastore.jpa.internal.jpql.expression.DefaultJPQLQuery;
import com.holonplatform.datastore.jpa.jpql.JPQLResultConverter;
import com.holonplatform.datastore.jpa.jpql.context.JPQLContextExpressionResolver;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLExpression;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLQuery;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLQueryDefinition;

/**
 * {@link QueryOperation} to {@link JPQLQuery} resolver.
 *
 * @since 5.1.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public enum QueryResolver implements JPQLContextExpressionResolver<QueryOperation, JPQLQuery> {

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
	public Class<? extends JPQLQuery> getResolvedType() {
		return JPQLQuery.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.resolvers.JPQLContextExpressionResolver#resolve(com.holonplatform.core.
	 * Expression, com.holonplatform.datastore.jpa.context.JPQLResolutionContext)
	 */
	@SuppressWarnings({ "unchecked", "cast" })
	@Override
	public Optional<JPQLQuery> resolve(QueryOperation expression, JPQLResolutionContext context)
			throws InvalidExpressionException {

		// resolve as JPQLQueryDefinition
		final JPQLQueryDefinition clauses = context.resolveOrFail(expression, JPQLQueryDefinition.class);

		// serialize query clauses to JPQL
		String jpql = context.resolveOrFail(clauses, JPQLExpression.class).getValue();

		// build SQLQuery
		return Optional.of(new DefaultJPQLQuery(jpql, (Class) clauses.getQueryResultType(),
				(JPQLResultConverter) clauses.getResultConverter()
						.orElseThrow(() -> new InvalidExpressionException("Missing query results converter")),
				context.getNamedParametersHandler().getNamedParameters()));
	}

}
