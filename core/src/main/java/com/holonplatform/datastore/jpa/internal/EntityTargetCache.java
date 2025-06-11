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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

import jakarta.persistence.EntityManagerFactory;

import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.internal.utils.ObjectUtils;

/**
 * Resolved {@link DataTarget} names to JPA entity class cache.
 * 
 * @since 5.0.0
 */
public final class EntityTargetCache implements Serializable {

	private static final long serialVersionUID = 1780346234469898465L;

	/**
	 * Data target name - entity class mappings cache
	 */
	private final static WeakHashMap<EntityManagerFactory, Map<String, Class<?>>> ENTITY_TARGETS = new WeakHashMap<>();

	private EntityTargetCache() {
	}

	/**
	 * Try to obtain JPA Entity mapping class from given path name using given ClassLoader
	 * @param entityManagerFactory EntityManagerFactory to use
	 * @param name Path name (not null)
	 * @return Entity class, or <code>null</code> target was null
	 */
	public synchronized static Optional<Class<?>> resolveEntityClass(EntityManagerFactory entityManagerFactory,
			String name) {

		ObjectUtils.argumentNotNull(name, "Name must be not null");
		ObjectUtils.argumentNotNull(entityManagerFactory, "EntityManagerFactory must be not null");

		// check cache
		Map<String, Class<?>> mappings = ENTITY_TARGETS.getOrDefault(entityManagerFactory, Collections.emptyMap());
		if (mappings.containsKey(name)) {
			return Optional.of(mappings.get(name));
		}

		// try to resolve by entity name
		Optional<Class<?>> entityClass = entityManagerFactory.getMetamodel().getEntities().stream()
				.filter(e -> e.getName().equals(name)).findFirst().map(e -> e.getJavaType());
		// try to resolve by entity type
		if (!entityClass.isPresent()) {
			entityClass = entityManagerFactory.getMetamodel().getEntities().stream()
					.filter(e -> e.getJavaType().getName().equals(name)).findFirst().map(e -> e.getJavaType());
		}

		// cache value
		entityClass.ifPresent(
				e -> ENTITY_TARGETS.computeIfAbsent(entityManagerFactory, c -> new HashMap<>()).put(name, e));

		return entityClass;

	}

}
