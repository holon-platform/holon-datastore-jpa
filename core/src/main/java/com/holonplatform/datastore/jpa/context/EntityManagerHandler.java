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

import com.holonplatform.core.exceptions.DataAccessException;

/**
 * JPA {@link EntityManager} handler.
 * <p>
 * The concrete handler should consistently manage {@link EntityManager} lifecycle, including entity manager creation
 * and closing.
 * </p>
 *
 * @since 5.1.0
 */
public interface EntityManagerHandler {

	/**
	 * Execute given <code>operation</code> using an {@link EntityManager} instance provided by the Datastore and return
	 * the operation result.
	 * <p>
	 * The {@link EntityManager} lifecycle is managed by the JPA Datastore, including creation and closing.
	 * </p>
	 * @param <R> Operation result type
	 * @param operation The operation to execute (not null)
	 * @return Operation result
	 * @throws DataAccessException If an error occurred during {@link EntityManager} management or operation execution
	 */
	<R> R withEntityManager(EntityManagerOperation<R> operation);

	/**
	 * Execute given <code>operation</code> using an {@link EntityManager} instance provided by the Datastore.
	 * <p>
	 * The {@link EntityManager} lifecycle is managed by the JPA Datastore, including creation and closing.
	 * </p>
	 * @param operation The operation to execute (not null)
	 * @throws DataAccessException If an error occurred during {@link EntityManager} management or operation execution
	 */
	default void withEntityManager(EntityManagerRunnable operation) {
		withEntityManager(em -> {
			operation.execute(em);
			return null;
		});
	}

}
