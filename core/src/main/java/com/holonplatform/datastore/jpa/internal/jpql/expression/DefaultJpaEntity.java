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
package com.holonplatform.datastore.jpa.internal.jpql.expression;

import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jpa.jpql.expression.JpaEntity;

/**
 * Default {@link JpaEntity} implementation.
 *
 * @param <T> Entity type
 *
 * @since 5.0.0
 */
public class DefaultJpaEntity<T> implements JpaEntity<T> {

	private static final long serialVersionUID = -2297497762504490622L;

	/**
	 * Entity class
	 */
	private final Class<? extends T> entityClass;

	/**
	 * Entity name
	 */
	private final String entityName;

	/**
	 * Constructor.
	 * @param entityClass Entity class (not null)
	 * @param entityName Entity name (not null)
	 */
	public DefaultJpaEntity(Class<? extends T> entityClass, String entityName) {
		super();
		ObjectUtils.argumentNotNull(entityClass, "Entity class must be not null");
		ObjectUtils.argumentNotNull(entityName, "Entity name must be not null");
		this.entityClass = entityClass;
		this.entityName = entityName;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.JpaEntity#getEntityClass()
	 */
	@Override
	public Class<? extends T> getEntityClass() {
		return entityClass;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.datastore.jpa.internal.JpaEntity#getEntityName()
	 */
	@Override
	public String getEntityName() {
		return entityName;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.Expression#validate()
	 */
	@Override
	public void validate() throws InvalidExpressionException {
		if (getEntityClass() == null) {
			throw new InvalidExpressionException("Null entity class");
		}
		if (getEntityName() == null) {
			throw new InvalidExpressionException("Null entity name");
		}
	}
}
