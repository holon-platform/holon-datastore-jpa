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
package com.holonplatform.datastore.jpa.internal.expressions;

import java.util.Optional;

import javax.persistence.Entity;

/**
 * Default {@link JpaEntity} implementation.
 *
 * @param <T> Entity type
 *
 * @since 5.0.0
 */
public class DefaultJpaEntity<T> implements JpaEntity<T> {

	private static final long serialVersionUID = -2297497762504490622L;

	private final Class<? extends T> entityClass;

	private String entityName;

	public DefaultJpaEntity(Class<? extends T> entityClass) {
		super();
		this.entityClass = entityClass;
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
		if (entityName == null) {
			validate();
			entityName = getEntityNameFromAnnotation().orElse(getEntityClass().getSimpleName());
		}
		return entityName;
	}

	private Optional<String> getEntityNameFromAnnotation() {
		if (getEntityClass().isAnnotationPresent(Entity.class)) {
			String name = getEntityClass().getAnnotation(Entity.class).name();
			if (name != null && !name.trim().equals("")) {
				return Optional.of(name);
			}
		}
		return Optional.empty();
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
	}
}
