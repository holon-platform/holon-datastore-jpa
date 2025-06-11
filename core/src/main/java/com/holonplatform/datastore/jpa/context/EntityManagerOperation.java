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

/**
 * Represents an operation to be executed using a Datastore managed {@link EntityManager}.
 * <p>
 * If returning a result is not required, the {@link EntityManagerRunnable} can be used.
 * </p>
 * 
 * @param <R> Operation result type
 * 
 * @since 5.1.0
 * 
 * @see EntityManagerRunnable
 */
@FunctionalInterface
public interface EntityManagerOperation<R> {

	/**
	 * Execute an operation and returns a result.
	 * @param entityManager EntityManager to use
	 * @return Operation result
	 * @throws Exception If an operation execution error occurred
	 */
	R execute(EntityManager entityManager) throws Exception;

}
