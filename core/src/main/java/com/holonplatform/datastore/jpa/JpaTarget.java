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
package com.holonplatform.datastore.jpa;

import java.util.Optional;

import com.holonplatform.core.ExpressionResolver;
import com.holonplatform.core.datastore.DataTarget;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.query.QueryProjection;
import com.holonplatform.datastore.jpa.internal.DefaultJpaTarget;

/**
 * A {@link DataTarget} bound to a JPA Entity
 * 
 * @param <T> Entity class type
 *
 * @since 4.5.0
 */
public interface JpaTarget<T> extends DataTarget<T>, QueryProjection<T> {

	/**
	 * Mapped entity class to which this data target is referred
	 * @return Entity class (not null)
	 */
	default Class<? extends T> getEntityClass() {
		return getType();
	}

	/**
	 * Build a {@link JpaTarget} using given <code>entityClass</code>
	 * @param <T> Entity class type
	 * @param entityClass JPA entity mapping class (not null)
	 * @return A JpaTarget on given entity class
	 */
	static <T> JpaTarget<T> of(Class<T> entityClass) {
		return new DefaultJpaTarget<>(entityClass);
	}

	/**
	 * Convenience method to build a JPA {@link DataTarget} resolver which translates a symbolic query target name into
	 * a valid JPA entity class.
	 * @param name Data target symbolic name (not null)
	 * @param entityClass JPA entity class associated to given name (not null)
	 * @return DataTarget resolver which translates a symbolic query target name into a JPA entity class
	 */
	@SuppressWarnings("rawtypes")
	static ExpressionResolver<DataTarget, DataTarget> nameResolver(final String name, final Class<?> entityClass) {
		ObjectUtils.argumentNotNull(name, "Data target name must be not null");
		ObjectUtils.argumentNotNull(entityClass, "Entity class must be not null");
		return DataTargetResolver.create(DataTarget.class, (target, context) -> name.equals(target.getName())
				? Optional.of(JpaTarget.of(entityClass)) : Optional.empty());
	}

}
