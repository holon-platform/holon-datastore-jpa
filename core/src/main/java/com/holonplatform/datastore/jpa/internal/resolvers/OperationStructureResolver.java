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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.holonplatform.core.Expression;
import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.Path;
import com.holonplatform.core.datastore.Datastore.OperationType;
import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.core.internal.query.QueryUtils;
import com.holonplatform.core.internal.utils.TypeUtils;
import com.holonplatform.core.query.ConstantExpression;
import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.datastore.jpa.internal.JpaDatastoreUtils;
import com.holonplatform.datastore.jpa.internal.expressions.FromExpression;
import com.holonplatform.datastore.jpa.internal.expressions.JPQLToken;
import com.holonplatform.datastore.jpa.internal.expressions.JpaResolutionContext;
import com.holonplatform.datastore.jpa.internal.expressions.OperationStructure;

/**
 * {@link OperationStructure} expression resolver.
 *
 * @since 5.0.0
 */
public enum OperationStructureResolver implements ExpressionResolver<OperationStructure, JPQLToken> {

	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends OperationStructure> getExpressionType() {
		return OperationStructure.class;
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
	public Optional<JPQLToken> resolve(OperationStructure expression, ResolutionContext resolutionContext)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// context
		final JpaResolutionContext context = JpaResolutionContext.checkContext(resolutionContext);

		final StringBuilder operation = new StringBuilder();

		// check type
		final OperationType type = expression.getOperationType();

		switch (type) {
		case DELETE:
			operation.append("DELETE FROM");
			break;
		case UPDATE:
			operation.append("UPDATE");
			break;
		case INSERT:
			throw new InvalidExpressionException("INSERT bulk operation is not supported by JPQL");
		default:
			break;
		}

		operation.append(" ");

		// from

		RelationalTarget<?> target = JpaDatastoreUtils.resolveExpression(context, expression.getTarget(),
				RelationalTarget.class, context);

		context.setTarget(target);

		// configure statement
		operation
				.append(JpaDatastoreUtils.resolveExpression(context, target, FromExpression.class, context).getValue());

		// values
		if (type == OperationType.UPDATE) {

			final Map<Path<?>, Object> pathValues = expression.getValues();
			final List<String> paths = new ArrayList<>(pathValues.size());
			final List<String> values = new ArrayList<>(pathValues.size());

			// resolve path and value
			for (Entry<Path<?>, Object> entry : pathValues.entrySet()) {
				paths.add(resolveExpression(entry.getKey(), context));
				values.add(resolvePathValue(entry.getKey(), entry.getValue(), context, true));
			}

			operation.append(" SET ");
			for (int i = 0; i < paths.size(); i++) {
				if (i > 0) {
					operation.append(",");
				}
				operation.append(paths.get(i));
				operation.append("=");
				operation.append(values.get(i));
			}

		}

		// filter
		expression.getFilter().ifPresent(f -> {
			operation.append(" WHERE ");
			operation.append(JpaDatastoreUtils.resolveExpression(context, f, JPQLToken.class, context).getValue());
		});

		// return SQL statement
		return Optional.of(JPQLToken.create(operation.toString()));
	}

	/**
	 * Resolve given {@link Expression} to obtain the corresponding JPQL expression.
	 * @param expression Expression to resolve
	 * @param context Resolution context
	 * @param clause Resolution clause
	 * @return JPQL expression
	 * @throws InvalidExpressionException If expression cannot be resolved
	 */
	private static String resolveExpression(Expression expression, JpaResolutionContext context) {
		JPQLToken token = context.resolve(expression, JPQLToken.class, context)
				.orElseThrow(() -> new InvalidExpressionException("Failed to resolve expression [" + expression + "]"));
		token.validate();
		return token.getValue();
	}

	/**
	 * Resolve a value associated to a {@link Path} to obtain the corresponding JPQL expression.
	 * @param path Path
	 * @param value Value
	 * @param context Resolution context
	 * @param clause Resolution clause
	 * @param allowNull if <code>true</code>, null values are allowed and returned as <code>NULL</code> keyword
	 * @return JPQL expression
	 * @throws InvalidExpressionException If expression cannot be resolved
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static String resolvePathValue(Path<?> path, Object value, JpaResolutionContext context,
			boolean allowNull) {

		if (value != null && TypeUtils.isString(value.getClass())
				&& (value.toString().startsWith(":") || "?".equals(value))) {
			return value.toString();
		}

		QueryExpression<?> expression = (QueryExpression.class.isAssignableFrom(path.getClass()))
				? QueryUtils.asConstantExpression((QueryExpression) path, value) : ConstantExpression.create(value);

		return JpaDatastoreUtils.resolveExpression(context, expression, JPQLToken.class, context).getValue();
	}

}
