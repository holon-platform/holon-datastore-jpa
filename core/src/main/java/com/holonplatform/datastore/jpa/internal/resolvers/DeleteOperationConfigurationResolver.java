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
import com.holonplatform.core.datastore.relational.RelationalTarget;
import com.holonplatform.core.internal.datastore.operation.common.DeleteOperationConfiguration;
import com.holonplatform.datastore.jpa.jpql.context.JPQLContextExpressionResolver;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;
import com.holonplatform.datastore.jpa.jpql.context.JPQLStatementResolutionContext;
import com.holonplatform.datastore.jpa.jpql.context.JPQLStatementResolutionContext.AliasMode;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLExpression;
import com.holonplatform.datastore.jpa.jpql.expression.JPQLStatement;

/**
 * {@link DeleteOperationConfiguration} resolver.
 *
 * @since 5.1.0
 */
@Priority(Integer.MAX_VALUE)
public enum DeleteOperationConfigurationResolver
		implements JPQLContextExpressionResolver<DeleteOperationConfiguration, JPQLStatement> {

	/**
	 * Singleton instance
	 */
	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getExpressionType()
	 */
	@Override
	public Class<? extends DeleteOperationConfiguration> getExpressionType() {
		return DeleteOperationConfiguration.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.ExpressionResolver#getResolvedType()
	 */
	@Override
	public Class<? extends JPQLStatement> getResolvedType() {
		return JPQLStatement.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.resolvers.JPQLContextExpressionResolver#resolve(com.holonplatform.core.
	 * Expression, com.holonplatform.datastore.jpa.context.JPQLResolutionContext)
	 */
	@Override
	public Optional<JPQLStatement> resolve(DeleteOperationConfiguration expression, JPQLResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// check and resolve target
		RelationalTarget<?> target = context.resolveOrFail(expression.getTarget(), RelationalTarget.class);

		// build a statement context
		final JPQLStatementResolutionContext operationContext = JPQLStatementResolutionContext.asChild(context, target,
				context.getDialect().deleteStatementAliasSupported() ? AliasMode.AUTO : AliasMode.UNSUPPORTED);

		final StringBuilder operation = new StringBuilder();

		operation.append("DELETE FROM");
		operation.append(" ");

		// target
		operation.append(operationContext.resolveOrFail(target, JPQLExpression.class).getValue());

		// filter
		expression.getFilter().ifPresent(f -> {
			operation.append(" WHERE ");
			operation.append(operationContext.resolveOrFail(f, JPQLExpression.class).getValue());
		});

		return Optional.of(JPQLStatement.create(operation.toString(),
				operationContext.getNamedParametersHandler().getNamedParameters()));
	}

}
