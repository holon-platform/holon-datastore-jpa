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

import com.holonplatform.core.ConstantConverterExpression;
import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLExpression;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLLiteral;

/**
 * {@link ConstantConverterExpression} resolver.
 *
 * @since 5.0.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public enum ConstantExpressionResolver implements ExpressionResolver<ConstantConverterExpression, JPQLExpression> {

	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends ConstantConverterExpression> getExpressionType() {
		return ConstantConverterExpression.class;
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
	 * @see com.holonplatform.core.ExpressionResolver#resolve(com.holonplatform.core.Expression,
	 * com.holonplatform.core.ExpressionResolver.ResolutionContext)
	 */
	@Override
	public Optional<JPQLExpression> resolve(ConstantConverterExpression expression, ResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// resolve as Literal
		return context.resolve(
				JPQLLiteral.create(expression.getModelValue(),
						((ConstantConverterExpression<?, ?>) expression).getTemporalType().orElse(null)),
				JPQLExpression.class, context);
	}

}
