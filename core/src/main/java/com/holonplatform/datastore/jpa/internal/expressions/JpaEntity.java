/*
 * Copyright 2000-2016 Holon TDCN.
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

import java.io.Serializable;

import javax.persistence.Entity;

import com.holonplatform.core.Expression;

/**
 * JPA entity reference.
 * 
 * @param <T> Entity type
 *
 * @since 5.0.0
 */
public interface JpaEntity<T> extends Expression, Serializable {

	/**
	 * Get the entity class
	 * @return the entity class
	 */
	Class<? extends T> getEntityClass();

	/**
	 * Get the JPQL entity name, using {@link Entity} annotation or entity class simple name.
	 * @return The entity name
	 */
	String getEntityName();

	/**
	 * Create a new {@link JpaEntity} bound to given <code>entityClass</code>.
	 * @param <T> Entity type
	 * @param entityClass Entity class
	 * @return A new {@link JpaEntity} bound to given entityClass
	 */
	static <T> JpaEntity<T> create(Class<? extends T> entityClass) {
		return new DefaultJpaEntity<>(entityClass);
	}

}
