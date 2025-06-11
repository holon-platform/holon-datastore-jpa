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

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.datastore.jpa.jpql.context.JPQLContextExpressionResolver;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLExpression;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLLiteral;

/**
 * {@link JPQLLiteral} expression resolver.
 *
 * @since 5.0.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public enum JPQLLiteralValueResolver implements JPQLContextExpressionResolver<JPQLLiteral, JPQLExpression> {

	/**
	 * Singleton instance
	 */
	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends JPQLLiteral> getExpressionType() {
		return JPQLLiteral.class;
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
	public Optional<JPQLExpression> resolve(JPQLLiteral expression, JPQLResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		final Object value = expression.getValue();

		if (value == null) {
			return Optional.of(JPQLExpression.create("NULL"));
		}

		final String serialized;
		if (Collection.class.isAssignableFrom(value.getClass())) {
			serialized = ((Collection<?>) value).stream()
					.map(element -> context.getValueSerializer().serialize(element,
							((JPQLLiteral<?>) expression).getTemporalType().orElse(null)))
					.collect(Collectors.joining(","));
		} else {
			serialized = context.getValueSerializer().serialize(value,
					((JPQLLiteral<?>) expression).getTemporalType().orElse(null));
		}

		return Optional.of(JPQLExpression.create(serialized));
	}

}
