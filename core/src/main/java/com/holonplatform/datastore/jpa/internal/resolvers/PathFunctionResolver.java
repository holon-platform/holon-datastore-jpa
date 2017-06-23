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
import com.holonplatform.core.query.FunctionExpression.PathFunctionExpression;
import com.holonplatform.core.query.QueryFunction;
import com.holonplatform.core.query.QueryFunction.Avg;
import com.holonplatform.core.query.QueryFunction.Count;
import com.holonplatform.core.query.QueryFunction.Max;
import com.holonplatform.core.query.QueryFunction.Min;
import com.holonplatform.core.query.QueryFunction.Sum;
import com.holonplatform.datastore.jpa.internal.JpaDatastoreUtils;
import com.holonplatform.datastore.jpa.internal.expressions.JPQLToken;

/**
 * JPA {@link PathFunctionExpression} expression resolver.
 *
 * @since 5.0.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public enum PathFunctionResolver implements ExpressionResolver<PathFunctionExpression, JPQLToken> {

	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends PathFunctionExpression> getExpressionType() {
		return PathFunctionExpression.class;
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
	@Override
	public Optional<JPQLToken> resolve(PathFunctionExpression expression, ResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// resolve path
		String path = JpaDatastoreUtils.resolveExpression(context, expression.getPath(), JPQLToken.class, context)
				.getValue();

		// resolve function
		return getFunctionName(expression.getFunction()).map(f -> {
			StringBuilder sb = new StringBuilder();
			sb.append(f);
			sb.append("(");
			sb.append(path);
			sb.append(")");
			return JPQLToken.create(sb.toString());
		});
	}

	private static Optional<String> getFunctionName(QueryFunction function) {
		if (Count.class.isAssignableFrom(function.getClass())) {
			return Optional.of("COUNT");
		}
		if (Avg.class.isAssignableFrom(function.getClass())) {
			return Optional.of("AVG");
		}
		if (Min.class.isAssignableFrom(function.getClass())) {
			return Optional.of("MIN");
		}
		if (Max.class.isAssignableFrom(function.getClass())) {
			return Optional.of("MAX");
		}
		if (Sum.class.isAssignableFrom(function.getClass())) {
			return Optional.of("SUM");
		}
		return Optional.empty();
	}

}
