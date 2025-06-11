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
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.datastore.jpa.jpql.context.JPQLContextExpressionResolver;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;

/**
 * {@link DataTarget} expression resolver.
 *
 * @since 5.0.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public enum DataTargetResolver implements JPQLContextExpressionResolver<DataTarget, RelationalTarget> {

	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends DataTarget> getExpressionType() {
		return DataTarget.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getResolvedType()
	 */
	@Override
	public Class<? extends RelationalTarget> getResolvedType() {
		return RelationalTarget.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.jpql.context.JPQLContextExpressionResolver#resolve(com.holonplatform.core.
	 * Expression, com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext)
	 */
	@Override
	public Optional<RelationalTarget> resolve(DataTarget expression, JPQLResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		if (expression instanceof RelationalTarget) {
			return Optional.of((RelationalTarget) expression);
		}

		// intermediate resolution
		DataTarget<?> target = context.resolve(expression, DataTarget.class).orElse(expression);

		// resolve as RelationalTarget
		return Optional.of(RelationalTarget.of(target));
	}

}
