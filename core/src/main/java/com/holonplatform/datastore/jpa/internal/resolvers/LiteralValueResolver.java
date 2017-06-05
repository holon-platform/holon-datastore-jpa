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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.datastore.jpa.internal.expressions.JPQLToken;
import com.holonplatform.datastore.jpa.internal.expressions.JpaResolutionContext;
import com.holonplatform.datastore.jpa.internal.expressions.LiteralValue;
import com.holonplatform.datastore.jpa.internal.support.ParameterValue;

/**
 * {@link LiteralValue} expression resolver.
 *
 * @since 5.0.0
 */
@Priority(Integer.MAX_VALUE)
public enum LiteralValueResolver implements ExpressionResolver<LiteralValue, JPQLToken> {

	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends LiteralValue> getExpressionType() {
		return LiteralValue.class;
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
	public Optional<JPQLToken> resolve(LiteralValue expression,
			com.holonplatform.core.ExpressionResolver.ResolutionContext context) throws InvalidExpressionException {

		final JpaResolutionContext ctx = JpaResolutionContext.checkContext(context);

		String serialized = null;

		// validate
		expression.validate();

		if (expression.getValue() != null && Collection.class.isAssignableFrom(expression.getValue().getClass())) {
			// collection of values
			final List<String> tokens = new ArrayList<>(((Collection<?>) expression.getValue()).size());
			for (Object value : (Collection<?>) expression.getValue()) {
				tokens.add(serialize(
						LiteralValue.create(value, expression.getType(), expression.getTemporalType().orElse(null)),
						ctx));
			}
			serialized = tokens.stream().collect(Collectors.joining(","));
		} else {
			// single value
			serialized = serialize(expression, ctx);
		}

		return Optional.of(JPQLToken.create(serialized));
	}

	/**
	 * Serialize given expression value using a named parameter.
	 * @param value Value to serialize
	 * @param context Resolution context
	 * @return Serialized value
	 */
	private static String serialize(LiteralValue value, JpaResolutionContext context) {
		final Class<?> type = (value.getType() != null) ? value.getType()
				: ((value.getValue() != null ? value.getValue().getClass() : Object.class));

		final ParameterValue parameter = ParameterValue.create(type, value.getValue(),
				value.getTemporalType().orElse(null));
		final String serialized = context.addNamedParameter(parameter);

		return ":" + serialized;
	}

}
