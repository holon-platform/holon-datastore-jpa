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

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.TypedExpression;
import com.holonplatform.core.query.QueryFunction;
import com.holonplatform.datastore.jpa.jpql.context.JPQLContextExpressionResolver;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLExpression;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLFunction;

/**
 * {@link QueryFunction} resolver.
 *
 * @since 5.1.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public enum QueryFunctionResolver implements JPQLContextExpressionResolver<QueryFunction, JPQLExpression> {

	/**
	 * Singleton instance.
	 */
	INSTANCE;

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
	public Class<? extends JPQLExpression> getResolvedType() {
		return JPQLExpression.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.resolvers.JPQLContextExpressionResolver#resolve(com.holonplatform.core.
	 * Expression, com.holonplatform.datastore.jpa.context.JPQLResolutionContext)
	 */
	@Override
	public Optional<JPQLExpression> resolve(QueryFunction expression, JPQLResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// try to resolve as a JPQLFunction
		return context.resolve(expression, JPQLFunction.class)
				.map(function -> serializeJPQLFunction(context, expression, function));

	}

	/**
	 * Serialize given function using provided {@link QueryFunction} expression arguments.
	 * @param context JPQL context
	 * @param expression Function expression
	 * @param function JPQL function
	 * @return Serialized function expression
	 */
	private static JPQLExpression serializeJPQLFunction(JPQLResolutionContext context, QueryFunction expression,
			JPQLFunction function) {

		// validate function
		function.validate();

		// resolve arguments
		final List<String> arguments = new LinkedList<>();
		if (expression.getExpressionArguments() != null) {
			for (TypedExpression<?> argument : ((QueryFunction<?, ?>) expression).getExpressionArguments()) {
				// resolve argument
				arguments.add(context.resolveOrFail(argument, JPQLExpression.class).getValue());
			}
		}

		return JPQLExpression.create(function.serialize(arguments));

	}

}
