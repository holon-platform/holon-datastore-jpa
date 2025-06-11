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
package com.holonplatform.datastore.jpa.internal;

import java.util.Optional;

import jakarta.persistence.Entity;

import com.holonplatform.core.internal.datastore.DefaultDataTarget;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.datastore.jpa.JpaTarget;

/**
 * Default {@link JpaTarget} implementation
 *
 * @since 4.5.0
 */
public class DefaultJpaTarget<T> extends DefaultDataTarget<T> implements JpaTarget<T> {

	private static final long serialVersionUID = 1979070987995081298L;

	/**
	 * Constructor.
	 * @param entityClass Entity class (not null)
	 */
	public DefaultJpaTarget(Class<? extends T> entityClass) {
		super(getEntityNameFromAnnotation(entityClass).orElse(entityClass.getSimpleName()), entityClass);
	}

	/**
	 * Get the entity name using {@link Entity#name()} annotation attribute, if available.
	 * @param entityClass Entity class (not null)
	 * @return The entity name as specified using {@link Entity#name()} annotation attribute, or an empty Optional if
	 *         the {@link Entity} annotation is not present or the <code>name</code> attribute has no value
	 */
	private static Optional<String> getEntityNameFromAnnotation(Class<?> entityClass) {
		ObjectUtils.argumentNotNull(entityClass, "Entity class must be not null");
		if (entityClass.isAnnotationPresent(Entity.class)) {
			String name = entityClass.getAnnotation(Entity.class).name();
			if (name != null && !name.trim().equals("")) {
				return Optional.of(name);
			}
		}
		return Optional.empty();
	}

}
