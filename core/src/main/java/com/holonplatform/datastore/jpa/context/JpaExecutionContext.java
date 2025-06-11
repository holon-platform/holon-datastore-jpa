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
package com.holonplatform.datastore.jpa.context;

import jakarta.persistence.EntityManager;

import com.holonplatform.datastore.jpa.internal.context.DefaultJpaExecutionContext;

/**
 * JPA operation execution context.
 *
 * @since 5.1.0
 */
public interface JpaExecutionContext extends JpaContext {

	/**
	 * Get the {@link EntityManager} used by current operation execution.
	 * @return the {@link EntityManager} instance
	 */
	EntityManager getEntityManager();

	/**
	 * Create a new {@link JpaExecutionContext}.
	 * @param context JPA Context
	 * @param entityManager Operation EntityManager
	 * @return A new {@link JpaExecutionContext}
	 */
	static JpaExecutionContext create(JpaContext context, EntityManager entityManager) {
		return new DefaultJpaExecutionContext(context, entityManager);
	}

}
