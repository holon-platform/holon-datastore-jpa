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
import com.holonplatform.core.Path;
import com.holonplatform.core.datastore.relational.Join;
import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jpa.jpql.context.JPQLContextExpressionResolver;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLExpression;

/**
 * {@link RelationalTarget} expression resolver.
 *
 * @since 5.0.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public enum RelationalTargetResolver implements JPQLContextExpressionResolver<RelationalTarget, JPQLExpression> {

	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends RelationalTarget> getExpressionType() {
		return RelationalTarget.class;
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
	public Optional<JPQLExpression> resolve(RelationalTarget expression, JPQLResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		final StringBuilder sb = new StringBuilder();

		// root path
		sb.append(getJPQLPath(context, expression));

		// resolve joins
		// resolve joins
		sb.append(((RelationalTarget<?>) expression).getJoins().stream().map(j -> resolveJoin(context, j))
				.collect(Collectors.joining(" ")));

		return Optional.of(JPQLExpression.create(sb.toString().trim()));
	}

	/**
	 * Get the JPQL path representation, appending alias name if available
	 * @param context Resolution context
	 * @param path Path to convert
	 * @return JPQL expression
	 */
	private static String getJPQLPath(JPQLResolutionContext context, final Path<?> path) {
		return context.isStatementCompositionContext().flatMap(ctx -> ctx.getAlias(path, false).map(a -> {
			StringBuilder pb = new StringBuilder();
			pb.append(path.getName());
			pb.append(" ");
			pb.append(a);
			return pb.toString();
		})).orElse(path.getName());
	}

	/**
	 * Resolve a {@link Join} clause.
	 * @param sb String builder to use to append the resolved join JPQL
	 * @param context Resolution context
	 * @param join Join to resolve
	 * @return Resolved join JPQL
	 * @throws InvalidExpressionException If an error occurred
	 */
	private static String resolveJoin(JPQLResolutionContext context, Join<?> join) throws InvalidExpressionException {
		ObjectUtils.argumentNotNull(join, "Join must be not null");

		final StringBuilder sb = new StringBuilder();
		sb.append(" ");

		switch (join.getJoinType()) {
		case INNER:
			sb.append("JOIN ");
			break;
		case LEFT:
			sb.append("LEFT JOIN ");
			break;
		case RIGHT:
			sb.append("RIGHT JOIN ");
			break;
		default:
			break;
		}

		// join
		sb.append(getJPQLPath(context, join));

		// ON condition
		join.getOn().ifPresent(o -> {
			sb.append(" ON ");
			sb.append(context.resolveOrFail(o, JPQLExpression.class).getValue());
		});

		return sb.toString();
	}

}
