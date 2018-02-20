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
	Class<? extends T> getEntityClass();

	/**
	 * Get the JPQL entity name.
	 * @return The entity name
	 */
	String getEntityName();

	/**
	 * Create a new {@link JpaEntity} from given <code>entityClass</code>.
	 * @param <T> Entity type
	 * @param entityClass Entity class (not null)
	 * @param entityName Entity name
	 * @return A new {@link JpaEntity}
	 */
	static <T> JpaEntity<T> create(Class<? extends T> entityClass, String entityName) {
		return new DefaultJpaEntity<>(entityClass, entityName);
	}

}
