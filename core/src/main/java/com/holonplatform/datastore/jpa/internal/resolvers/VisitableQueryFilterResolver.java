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
import java.util.stream.Collectors;

import javax.annotation.Priority;

import com.holonplatform.core.Expression;
import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.internal.query.QueryFilterVisitor;
import com.holonplatform.core.internal.query.QueryFilterVisitor.VisitableQueryFilter;
import com.holonplatform.core.internal.query.filter.AndFilter;
import com.holonplatform.core.internal.query.filter.BetweenFilter;
import com.holonplatform.core.internal.query.filter.EqualFilter;
import com.holonplatform.core.internal.query.filter.GreaterFilter;
import com.holonplatform.core.internal.query.filter.InFilter;
import com.holonplatform.core.internal.query.filter.LessFilter;
import com.holonplatform.core.internal.query.filter.NotEqualFilter;
import com.holonplatform.core.internal.query.filter.NotFilter;
import com.holonplatform.core.internal.query.filter.NotInFilter;
import com.holonplatform.core.internal.query.filter.NotNullFilter;
import com.holonplatform.core.internal.query.filter.NullFilter;
import com.holonplatform.core.internal.query.filter.OperationQueryFilter;
import com.holonplatform.core.internal.query.filter.OrFilter;
import com.holonplatform.core.internal.query.filter.StringMatchFilter;
import com.holonplatform.core.query.ConstantExpression;
import com.holonplatform.core.query.QueryExpression;
import com.holonplatform.core.query.QueryFilter;
import com.holonplatform.datastore.jpa.internal.expressions.JPQLToken;
import com.holonplatform.datastore.jpa.internal.expressions.JpaResolutionContext;
import com.holonplatform.datastore.jpa.internal.support.ParameterValue;

/**
 * JPA {@link VisitableQueryFilter} expression resolver.
 *
 * @since 5.0.0
 */
@Priority(Integer.MAX_VALUE - 10)
public enum VisitableQueryFilterResolver implements ExpressionResolver<VisitableQueryFilter, JPQLToken>,
		QueryFilterVisitor<JPQLToken, JpaResolutionContext> {

	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends VisitableQueryFilter> getExpressionType() {
		return VisitableQueryFilter.class;
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
	public Optional<JPQLToken> resolve(VisitableQueryFilter expression, ResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// resolve using visitor
		return Optional.ofNullable(expression.accept(this, JpaResolutionContext.checkContext(context)));
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * NullFilter, java.lang.Object)
	 */
	@Override
	public JPQLToken visit(NullFilter filter, JpaResolutionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append(resolve(filter.getLeftOperand(), context));
		sb.append(" IS NULL");
		return JPQLToken.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * NotNullFilter, java.lang.Object)
	 */
	@Override
	public JPQLToken visit(NotNullFilter filter, JpaResolutionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append(resolve(filter.getLeftOperand(), context));
		sb.append(" IS NOT NULL");
		return JPQLToken.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * EqualFilter, java.lang.Object)
	 */
	@Override
	public <T> JPQLToken visit(EqualFilter<T> filter, JpaResolutionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append(resolve(filter.getLeftOperand(), context));
		sb.append("=");
		sb.append(resolveRightOperand(filter, context));
		return JPQLToken.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * NotEqualFilter, java.lang.Object)
	 */
	@Override
	public <T> JPQLToken visit(NotEqualFilter<T> filter, JpaResolutionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append(resolve(filter.getLeftOperand(), context));
		sb.append("<>");
		sb.append(resolveRightOperand(filter, context));
		return JPQLToken.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * GreaterFilter, java.lang.Object)
	 */
	@Override
	public <T> JPQLToken visit(GreaterFilter<T> filter, JpaResolutionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append(resolve(filter.getLeftOperand(), context));
		sb.append(filter.isIncludeEquals() ? ">=" : ">");
		sb.append(resolveRightOperand(filter, context));
		return JPQLToken.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * LessFilter, java.lang.Object)
	 */
	@Override
	public <T> JPQLToken visit(LessFilter<T> filter, JpaResolutionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append(resolve(filter.getLeftOperand(), context));
		sb.append(filter.isIncludeEquals() ? "<=" : "<");
		sb.append(resolveRightOperand(filter, context));
		return JPQLToken.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * InFilter, java.lang.Object)
	 */
	@Override
	public <T> JPQLToken visit(InFilter<T> filter, JpaResolutionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append(resolve(filter.getLeftOperand(), context));
		sb.append(" IN (");
		sb.append(resolveRightOperand(filter, context));
		sb.append(")");
		return JPQLToken.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * NotInFilter, java.lang.Object)
	 */
	@Override
	public <T> JPQLToken visit(NotInFilter<T> filter, JpaResolutionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append(resolve(filter.getLeftOperand(), context));
		sb.append(" NOT IN (");
		sb.append(resolveRightOperand(filter, context));
		sb.append(")");
		return JPQLToken.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * BetweenFilter, java.lang.Object)
	 */
	@Override
	public <T> JPQLToken visit(BetweenFilter<T> filter, JpaResolutionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append(resolve(filter.getLeftOperand(), context));
		sb.append(" BETWEEN ");
		sb.append(resolve(ConstantExpression.create(filter.getFromValue()), context));
		sb.append(" AND ");
		sb.append(resolve(ConstantExpression.create(filter.getToValue()), context));
		return JPQLToken.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * LikeFilter, java.lang.Object)
	 */
	@Override
	public JPQLToken visit(StringMatchFilter filter, JpaResolutionContext context) {

		// right operand
		if (!filter.getRightOperand().isPresent()) {
			throw new InvalidExpressionException("Invalid StringMatchFilter right operand");
		}
		if (!(filter.getRightOperand().get() instanceof ConstantExpression)) {
			throw new InvalidExpressionException(
					"Invalid right operand expression for StringMatchFilter: [" + filter.getRightOperand().get() + "]");
		}
		Object resolved = ((ConstantExpression<?, ?>) filter.getRightOperand().get()).getModelValue();

		String value = resolved.toString();

		// escape
		value = value.replace("!", "!!").replace("%", "!%").replace("_", "!_").replace("[", "![");

		// add wildcards
		switch (filter.getMatchMode()) {
		case CONTAINS:
			value = "%" + value + "%";
			break;
		case ENDS_WITH:
			value = "%" + value;
			break;
		case STARTS_WITH:
			value = value + "%";
			break;
		default:
			break;
		}

		final String path = resolve(filter.getLeftOperand(), context);

		StringBuilder sb = new StringBuilder();

		if (filter.isIgnoreCase()) {
			sb.append("lower(");
			sb.append(path);
			sb.append(")");
			value = value.toLowerCase();
		} else {
			sb.append(path);
		}

		sb.append(" LIKE ");
		sb.append(":" + context.addNamedParameter(ParameterValue.create(String.class, value)));

		sb.append(" ESCAPE '!'");

		return JPQLToken.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * AndFilter, java.lang.Object)
	 */
	@Override
	public JPQLToken visit(AndFilter filter, JpaResolutionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(resolveFilterList(filter.getComposition(), ") AND (", context));
		sb.append(")");
		return JPQLToken.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * OrFilter, java.lang.Object)
	 */
	@Override
	public JPQLToken visit(OrFilter filter, JpaResolutionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(resolveFilterList(filter.getComposition(), ") OR (", context));
		sb.append(")");
		return JPQLToken.create(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.internal.query.QueryFilterVisitor#visit(com.holonplatform.core.internal.query.filter.
	 * NotFilter, java.lang.Object)
	 */
	@Override
	public JPQLToken visit(NotFilter filter, JpaResolutionContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append("NOT (");
		sb.append(context.resolveExpression(filter.getComposition().get(0), JPQLToken.class).getValue());
		sb.append(")");
		return JPQLToken.create(sb.toString());
	}

	/**
	 * Resolve a list of filters into {@link JPQLToken}s and returns the resolved tokens joined with given
	 * <code>separator</code>.
	 * @param filters Filters to resolve
	 * @param separator Token separator
	 * @param context Resolution context
	 * @return The resolved tokens joined with given <code>separator</code>
	 * @throws InvalidExpressionException Failed to resolve a filter
	 */
	private static String resolveFilterList(List<QueryFilter> filters, String separator, JpaResolutionContext context)
			throws InvalidExpressionException {
		List<String> resolved = new LinkedList<>();
		filters.forEach(f -> {
			resolved.add(context.resolveExpression(f, JPQLToken.class).getValue());
		});
		return resolved.stream().collect(Collectors.joining(separator));
	}

	/**
	 * Resolve given expression as {@link JPQLToken} and return the JPQL value
	 * @param expression Expression to resolve
	 * @param context Resolution context
	 * @return JPQL value
	 * @throws InvalidExpressionException Failed to resolve the expression
	 */
	private static String resolve(Expression expression, JpaResolutionContext context)
			throws InvalidExpressionException {
		return context.resolveExpression(expression, JPQLToken.class).getValue();
	}

	/**
	 * Resolve the right operand of a {@link OperationQueryFilter} and return the JPQL value
	 * @param filter Filter
	 * @param context Resolution context
	 * @return JPQL value
	 * @throws InvalidExpressionException Failed to resolve the expression
	 */
	private static String resolveRightOperand(OperationQueryFilter<?> filter, JpaResolutionContext context)
			throws InvalidExpressionException {
		QueryExpression<?> operand = filter.getRightOperand()
				.orElseThrow(() -> new InvalidExpressionException("Missing right operand in filter [" + filter + "]"));
		return context.resolveExpression(operand, JPQLToken.class).getValue();
	}

}
