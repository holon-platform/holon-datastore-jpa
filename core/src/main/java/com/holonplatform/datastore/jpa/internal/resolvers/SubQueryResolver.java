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
import com.holonplatform.core.datastore.relational.SubQuery;
import com.holonplatform.core.query.QueryOperation;
import com.holonplatform.datastore.jpa.jpql.context.JPQLContextExpressionResolver;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLExpression;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLQueryDefinition;

/**
 * {@link SubQuery} expression resolver.
 *
 * @since 5.0.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE - 100)
public enum SubQueryResolver implements JPQLContextExpressionResolver<SubQuery, JPQLExpression> {

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
	public Class<? extends JPQLExpression> getResolvedType() {
		return JPQLExpression.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.resolvers.JPQLContextExpressionResolver#resolve(com.holonplatform.core.
	 * Expression, com.holonplatform.datastore.jpa.context.JPQLResolutionContext)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Optional<JPQLExpression> resolve(SubQuery expression, JPQLResolutionContext context)
			throws InvalidExpressionException {

		// resolve as JPQLQueryDefinition
		final JPQLQueryDefinition definition = context.resolveOrFail(
				QueryOperation.create(expression.getQueryConfiguration(), expression.getSelection()),
				JPQLQueryDefinition.class);

		// serialize query
		return context.resolve(definition, JPQLExpression.class);
	}

}
