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

import javax.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.Path;
import com.holonplatform.core.datastore.relational.Join;
import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jpa.internal.expressions.FromExpression;
import com.holonplatform.datastore.jpa.internal.expressions.JPQLToken;
import com.holonplatform.datastore.jpa.internal.expressions.JpaResolutionContext;

/**
 * {@link RelationalTarget} expression resolver.
 *
 * @since 5.0.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public enum RelationalTargetResolver implements ExpressionResolver<RelationalTarget, FromExpression> {

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
	public Class<? extends FromExpression> getResolvedType() {
		return FromExpression.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression.ExpressionResolverFunction#resolve(com.holonplatform.core.Expression,
	 * com.holonplatform.core.ExpressionResolver.ResolutionContext)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Optional<FromExpression> resolve(RelationalTarget expression, ResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		final JpaResolutionContext jpaContext = JpaResolutionContext.checkContext(context);

		final StringBuilder sb = new StringBuilder();

		// root path
		sb.append(getJPQLPath(jpaContext, expression));

		// resolve joins
		sb.append(expression.getJoins().stream().map(j -> resolveJoin((Join<?>) j, jpaContext))
				.collect(Collectors.joining(" ")));

		return Optional.of(FromExpression.create(sb.toString().trim()));
	}

	/**
	 * Get the JPQL path representation, appending alias name if available
	 * @param context Resolution context
	 * @param path Path to convert
	 * @return JPQL expression
	 */
	private static String getJPQLPath(JpaResolutionContext context, final Path<?> path) {
		return context.getTargetAlias(path).map(a -> {
			StringBuilder pb = new StringBuilder();
			pb.append(path.getName());
			pb.append(" ");
			pb.append(a);
			return pb.toString();
		}).orElse(path.getName());
	}

	/**
	 * Resolve a {@link Join} clause.
	 * @param sb String builder to use to append the resolved join JPQL
	 * @param join Join to resolve
	 * @param context Resolution context
	 * @return Resolved join JPQL
	 * @throws InvalidExpressionException If an error occurred
	 */
	private static String resolveJoin(Join<?> join, JpaResolutionContext context) throws InvalidExpressionException {
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
			sb.append(context.resolveExpression(o, JPQLToken.class).getValue());
		});

		return sb.toString();
	}

}
