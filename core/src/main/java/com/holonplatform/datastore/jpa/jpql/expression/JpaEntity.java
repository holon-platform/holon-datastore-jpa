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
package com.holonplatform.datastore.jpa.jpql.expression;

import java.io.Serializable;
import java.util.Optional;

import jakarta.persistence.metamodel.Metamodel;

import com.holonplatform.core.Expression;
import com.holonplatform.datastore.jpa.internal.jpql.expression.DefaultJpaEntity;

/**
 * JPA entity reference.
 * 
 * @param <T> Entity type
 *
 * @since 5.0.0
 */
public interface JpaEntity<T> extends Expression, Serializable {

	/**
	 * Get the entity class.
	 * @return the entity class
	 */
	Class<T> getEntityClass();

	/**
	 * Get the entity name.
	 * @return The entity name
	 */
	String getEntityName();

	/**
	 * Get the type of the entity id, if available.
	 * @return the entity id type
	 * @throws IllegalStateException If the entity identifier metadata are not available
	 */
	Optional<Class<?>> getIdType() throws IllegalStateException;

	/**
	 * Get whether the entity has a composite id.
	 * @return <code>true</code> if the entity has a composite (multi attribute) id, <code>false</code> if the entity
	 *         has a simple (single attribute) id or if the entity id is not available
	 * @throws IllegalStateException If the entity identifier metadata are not available
	 */
	boolean hasCompositeId() throws IllegalStateException;

	/**
	 * Get the id value of given entity istance.
	 * @param entity Entity istance (not null)
	 * @return The id value, if available
	 * @throws IllegalStateException If the entity identifier metadata are not available
	 */
	Optional<Object> getId(T entity) throws IllegalStateException;

	/**
	 * Gets whether given entity instance has to be considered new according to entity id value.
	 * <p>
	 * When entity id is not available, always returns <code>false</code>.
	 * </p>
	 * @param entity Entity instance (not null)
	 * @return <code>true</code> if entity instance has to be considered new, <code>false</code> otherwise
	 * @throws IllegalStateException If the entity identifier metadata are not available
	 */
	boolean isNew(T entity) throws IllegalStateException;

	/**
	 * Create a new {@link JpaEntity} from given <code>entityClass</code>.
	 * @param <T> Entity type
	 * @param metamodel JPA Metamodel (not null)
	 * @param entityClass Entity class (not null)
	 * @return A new {@link JpaEntity}
	 */
	static <T> JpaEntity<T> create(Metamodel metamodel, Class<T> entityClass) {
		return new DefaultJpaEntity<>(metamodel, entityClass);
	}

}
