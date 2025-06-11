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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

import jakarta.annotation.Priority;
import jakarta.persistence.EntityManagerFactory;

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

	/**
	 * Singleton instance
	 */
	INSTANCE;

	private final static WeakHashMap<EntityManagerFactory, Map<Class<?>, JpaEntity<?>>> ENTITY_CACHE = new WeakHashMap<>();

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

		final EntityManagerFactory emf = context.getEntityManagerFactory();

		final Class<?> entityClass;

		// get entity class
		if (target instanceof JpaTarget) {
			entityClass = ((JpaTarget<?>) target).getEntityClass();
		} else {
			entityClass = EntityTargetCache.resolveEntityClass(emf, target.getName())
					.orElseThrow(() -> new InvalidExpressionException("Invalid data target name [" + target.getName()
							+ "]: an entity class with given entity name is not available from JPA metamodel"));
		}

		// check cache
		Map<Class<?>, JpaEntity<?>> cached = ENTITY_CACHE.getOrDefault(emf, Collections.emptyMap());
		if (cached.containsKey(entityClass)) {
			return Optional.of(cached.get(entityClass));
		}

		// create JpaEntity and cache it
		final JpaEntity entity = JpaEntity.create(emf.getMetamodel(), entityClass);

		ENTITY_CACHE.computeIfAbsent(emf, c -> new HashMap<>()).put(entityClass, entity);

		return Optional.of(entity);
	}

}
