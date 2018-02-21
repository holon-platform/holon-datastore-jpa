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

import javax.annotation.Priority;

import com.holonplatform.core.Expression.InvalidExpressionException;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.datastore.jpa.JpaTarget;
import com.holonplatform.datastore.jpa.internal.EntityTargetCache;
import com.holonplatform.datastore.jpa.jpql.context.JPQLContextExpressionResolver;
import com.holonplatform.datastore.jpa.jpql.context.JPQLResolutionContext;
import com.holonplatform.datastore.jpa.jpql.expression.JpaEntity;

/**
 * {@link DataTarget} as {@link JpaEntity} resolver.
 *
 * @since 5.0.0
 */
@SuppressWarnings("rawtypes")
@Priority(Integer.MAX_VALUE)
public enum DataTargetEntityResolver implements JPQLContextExpressionResolver<DataTarget, JpaEntity> {

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
	public Class<? extends JpaEntity> getResolvedType() {
		return JpaEntity.class;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.resolvers.JPQLContextExpressionResolver#resolve(com.holonplatform.core.
	 * Expression, com.holonplatform.datastore.jpa.context.JPQLResolutionContext)
	 */
	@Override
	public Optional<JpaEntity> resolve(DataTarget expression, JPQLResolutionContext context)
			throws InvalidExpressionException {

		// validate
		expression.validate();

		// intermediate resolution and validation
		DataTarget target = context.resolve(expression, DataTarget.class).orElse(expression);

		// check JPA target
		if (target instanceof JpaTarget) {
			return Optional
					.of(JpaEntity.create(((JpaTarget<?>) target).getEntityClass(), ((JpaTarget<?>) target).getName()));
		}

		final String entityName = target.getName();
		// resolve entity class
		Class<?> entityClass = EntityTargetCache
				.resolveEntityClass(context.getEntityManagerFactory(), entityName)
				.orElseThrow(() -> new InvalidExpressionException("Invalid data target name [" + entityName
						+ "]: an entity class with given entity name is not available from JPA metamodel"));

		return Optional.of(JpaEntity.create(entityClass, entityName));
	}

}
